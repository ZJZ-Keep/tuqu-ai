package org.example.zjzaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * 终端操作工具类
 *
 * <p>修复两个问题：
 * 1. Windows 下 cmd 默认输出 GBK，用 UTF-8 读取会出现乱码；
 * 2. 命令卡死时没有超时，会把整个智能体任务拖死。</p>
 */
@Slf4j
public class TerminalOperationTool {

    /** 单条命令最长执行时间 */
    private static final long TIMEOUT_SECONDS = 180;

    /** 返回给模型/前端的最大字符数，避免一次输出占满上下文 */
    private static final int MAX_OUTPUT_LENGTH = 8000;

    @Tool(description = "Execute a command in the Windows terminal (cmd.exe) and return its output. "
            + "The command has a 180s timeout and output is truncated to 8000 characters.")
    public String executeTerminalCommand(@ToolParam(description = "Command to execute in the terminal") String command) {
        if (command == null || command.isBlank()) {
            return "命令执行失败：命令为空";
        }
        Process process = null;
        ExecutorService reader = Executors.newSingleThreadExecutor();
        try {
            ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/d", "/c", command);
            builder.redirectErrorStream(true);
            process = builder.start();

            Process finalProcess = process;
            Future<byte[]> outputFuture = reader.submit(() -> finalProcess.getInputStream().readAllBytes());

            boolean finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            byte[] bytes;
            if (!finished) {
                process.destroyForcibly();
                process.waitFor(5, TimeUnit.SECONDS);
                bytes = readOutputQuietly(outputFuture);
                String output = normalize(decode(bytes));
                return "命令执行超时（" + TIMEOUT_SECONDS + " 秒），已强制结束\n" + truncate(output);
            }

            bytes = readOutputQuietly(outputFuture);
            int exitCode = process.exitValue();
            String output = truncate(normalize(decode(bytes)));
            if (exitCode == 0) {
                return "命令执行完成（退出码 0）\n" + output;
            }
            return "命令执行失败（退出码 " + exitCode + "）\n" + output;
        } catch (IOException e) {
            log.error("执行命令失败：{}", command, e);
            return "命令执行失败：" + e.getMessage();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (process != null) {
                process.destroyForcibly();
            }
            return "命令执行被中断：" + e.getMessage();
        } finally {
            reader.shutdownNow();
        }
    }

    private byte[] readOutputQuietly(Future<byte[]> outputFuture) {
        try {
            return outputFuture.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("读取命令输出失败：{}", e.getMessage());
            return new byte[0];
        }
    }

    /**
     * 优先按 UTF-8 解码，失败则按 GBK（Windows 中文环境默认编码）解码
     */
    private String decode(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "";
        }
        try {
            return StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .onUnmappableCharacter(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes))
                    .toString();
        } catch (CharacterCodingException e) {
            try {
                return Charset.forName("GBK").decode(ByteBuffer.wrap(bytes)).toString();
            } catch (Exception ex) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
        }
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }
        // 去掉 ANSI 颜色控制符，避免前端出现 \u001b[...m 这样的乱码
        String cleaned = text.replaceAll("\\u001B\\[[;\\d]*[ -/]*[@-~]", "");
        cleaned = cleaned.replace("\r\n", "\n").replace('\r', '\n');
        return cleaned.strip();
    }

    private String truncate(String text) {
        if (text.length() <= MAX_OUTPUT_LENGTH) {
            return text;
        }
        return text.substring(0, MAX_OUTPUT_LENGTH) + "\n…（输出过长，已截断）";
    }
}
