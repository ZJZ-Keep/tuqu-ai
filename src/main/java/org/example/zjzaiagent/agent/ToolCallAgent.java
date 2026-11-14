package org.example.zjzaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingChatOptions;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent {

    /** 一次工具调用的原始输出最多在详情里展示这么多字符 */
    private static final int MAX_DETAIL_LENGTH = 4000;

    /** 匹配工具输出中的生成文件，用于前端渲染下载卡片 */
    private static final Pattern FILE_PATH_PATTERN = Pattern.compile(
            "(?:^|[\\\\/])tmp[\\\\/](file|pdf|download)[\\\\/]([^\\\\/:*?\"<>|\\r\\n]+?\\.(?:pdf|txt|md|html?|docx?|xlsx?|csv|json|png|jpe?g|gif|webp|bmp|svg|zip|log))",
            Pattern.CASE_INSENSITIVE);

    /** 可用的工具列表 */
    private final ToolCallback[] availableTools;

    /** 工具管理 */
    private ToolCallingManager toolCallingManager;

    /** 调用工具的响应 */
    private ChatResponse toolCallChatResponse;

    /** 连续多少步没有调用工具（连续两次说明模型确实没有下一步动作了） */
    private int noToolCallStreak = 0;

    /** 禁用 spring ai 内置的工具调用机制，改用自定义的 */
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools) {
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = ToolCallingChatOptions.builder()
                .internalToolExecutionEnabled(false)
                .build();
    }

    /**
     处理当前状态并决定下一步行动
     @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        //校验
        if (getNextStepPrompt() != null && !getNextStepPrompt().isEmpty()) {
            getMessages().add(new UserMessage(getNextStepPrompt()));
        }
        List<Message> messageList = getMessages();
        Prompt prompt = new Prompt(messageList, chatOptions);
        try {
            //获取带工具选项的响应
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            //记录响应，用于Act
            this.toolCallChatResponse = chatResponse;

            if (chatResponse == null || chatResponse.getResult() == null
                    || chatResponse.getResult().getOutput() == null) {
                log.warn("{}的思考过程无结果", getName());
                emit(AgentEvent.error("模型没有返回内容，请稍后重试"));
                setAgentStatus(AgentStatus.ERROR);
                return false;
            }

            AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
            String text = assistantMessage.getText();
            List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
            log.info("{}的思考：{}", getName(), text);
            log.info("{}计划调用工具 {} 个：{}", getName(), toolCallList.size(), describeToolCalls(toolCallList));

            boolean noToolCall = toolCallList.isEmpty();
            if (noToolCall && (containsFinishSignal(text) || ++noToolCallStreak >= 2)) {
                // 没有工具调用 + 明确的结束信号（或连续两轮无动作）=> 任务结束
                setAgentStatus(AgentStatus.FINISH);
                getMessages().add(assistantMessage);
                String finalText = (text == null || text.isBlank()) ? "任务已完成。" : text.strip();
                log.info("{}任务完成，输出最终答复", getName());
                emit(AgentEvent.finalAnswer(finalText));
                return false;
            }

            if (text != null && !text.isBlank()) {
                emit(AgentEvent.thinking(text.strip()));
            }

            if (noToolCall) {
                // 本轮没有工具调用，也不算结束：把回复记入上下文，下一步继续引导
                getMessages().add(assistantMessage);
                return false;
            }

            // 关键修复：只要模型请求了工具，就一定要执行，绝不能被“已成功生成”之类的话提前终止
            noToolCallStreak = 0;
            return true;
        } catch (Exception e) {
            log.error("{}的思考过程遇到问题：{}", getName(), e.getMessage(), e);
            emit(AgentEvent.error("模型调用失败：" + e.getMessage()));
            getMessages().add(new AssistantMessage("思考过程遇到问题：" + e.getMessage()));
            setAgentStatus(AgentStatus.ERROR);
            return false;
        }
    }

    /**
     执行工具调用并获取结果
     @return 执行结果
     */
    @Override
    public String act() {
        if (toolCallChatResponse == null || !toolCallChatResponse.hasToolCalls()) {
            return "没有工具调用";
        }
        //调用工具
        Prompt prompt = new Prompt(getMessages(), chatOptions);
        ToolExecutionResult toolExecutionResult;
        try {
            toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        } catch (Exception e) {
            log.error("{}执行工具失败：{}", getName(), e.getMessage(), e);
            emit(AgentEvent.error("工具执行失败：" + e.getMessage()));
            setAgentStatus(AgentStatus.ERROR);
            return "工具执行失败：" + e.getMessage();
        }
        //获取消息上下文
        setMessages(toolExecutionResult.conversationHistory());

        //当前工具调用结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        if (toolResponseMessage == null || toolResponseMessage.getResponses() == null) {
            emit(AgentEvent.error("工具没有返回结果"));
            return "工具没有返回结果";
        }

        List<String> readableResults = new ArrayList<>();
        for (ToolResponseMessage.ToolResponse toolResponse : toolResponseMessage.getResponses()) {
            String toolName = toolResponse.name();
            String rawData = toolResponse.responseData();
            boolean success = isSuccess(rawData);
            String summary = summarize(toolName, rawData, success);

            emit(AgentEvent.builder()
                    .type("tool")
                    .name(toolName)
                    .message(summary)
                    .detail(buildDetail(toolName, rawData))
                    .success(success)
                    .build());

            emitFileEvents(rawData);
            readableResults.add(summary);
        }

        //判断是否结束
        boolean anyMatch = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (anyMatch) {
            setAgentStatus(AgentStatus.FINISH);
        }
        String results = String.join("\n", readableResults);
        log.info("{}的调用结果：{}", getName(), results);
        return results;
    }

    /**
     * 把工具原始输出整理成一句人话
     */
    private String summarize(String toolName, String rawData, boolean success) {
        String text = rawData == null ? "" : rawData.strip();
        if (text.isEmpty()) {
            return "工具 " + toolName + (success ? " 执行完成" : " 执行失败");
        }
        // 工具本身已经返回了中文可读信息，优先取第一行
        String firstLine = text.lines().findFirst().orElse(text).strip();
        // 兼容历史英文输出
        if (firstLine.startsWith("File written successfully to:")) {
            firstLine = "文件已保存：" + firstLine.substring("File written successfully to:".length()).strip();
        } else if (firstLine.startsWith("PDF generated successfully to:")) {
            firstLine = "PDF 已生成：" + firstLine.substring("PDF generated successfully to:".length()).strip();
        } else if (firstLine.startsWith("Error writing file:")) {
            firstLine = "文件写入失败：" + firstLine.substring("Error writing file:".length()).strip();
        } else if (firstLine.startsWith("Error reading file:")) {
            firstLine = "文件读取失败：" + firstLine.substring("Error reading file:".length()).strip();
        }
        if (firstLine.length() > 200) {
            firstLine = firstLine.substring(0, 200) + "…";
        }
        return firstLine;
    }

    /**
     * 只有终端输出、搜索抓取、读文件这类内容才值得折叠展示
     */
    private String buildDetail(String toolName, String rawData) {
        if (rawData == null || rawData.isBlank()) {
            return null;
        }
        String data = rawData.strip();
        boolean verbose = switch (toolName) {
            case "executeTerminalCommand", "searchWeb", "scrapeWeb", "readFile", "downloadResource" -> true;
            default -> data.length() > 300;
        };
        if (!verbose) {
            return null;
        }
        return data.length() > MAX_DETAIL_LENGTH
                ? data.substring(0, MAX_DETAIL_LENGTH) + "\n…（内容过长，已截断）"
                : data;
    }

    private boolean isSuccess(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            return true;
        }
        String firstLine = rawData.strip().lines().findFirst().orElse("").strip();
        if (firstLine.startsWith("Error") || firstLine.startsWith("error")) {
            return false;
        }
        return !(firstLine.contains("失败")
                || firstLine.contains("出错")
                || firstLine.contains("不可用")
                || firstLine.contains("执行超时"));
    }

    /**
     * 从工具输出里识别生成的文件，交给前端渲染下载卡片
     */
    private void emitFileEvents(String rawData) {
        if (rawData == null || rawData.isBlank()) {
            return;
        }
        Matcher matcher = FILE_PATH_PATTERN.matcher(rawData);
        while (matcher.find()) {
            String subDir = matcher.group(1);
            String fileName = matcher.group(2);
            emit(AgentEvent.file(subDir, fileName));
        }
    }

    private boolean containsFinishSignal(String text) {
        if (text == null) {
            return false;
        }
        String lower = text.toLowerCase();
        return text.contains("任务结束")
                || text.contains("圆满结束")
                || text.contains("任务已完成")
                || text.contains("任务已经完成")
                || lower.contains("terminate");
    }

    private String describeToolCalls(List<AssistantMessage.ToolCall> toolCallList) {
        if (toolCallList == null || toolCallList.isEmpty()) {
            return "无";
        }
        return toolCallList.stream()
                .map(toolCall -> {
                    String args = toolCall.arguments() == null ? "" : toolCall.arguments();
                    if (args.length() > 200) {
                        args = args.substring(0, 200) + "…";
                    }
                    return toolCall.name() + "(" + args + ")";
                })
                .collect(Collectors.joining("\n"));
    }
}
