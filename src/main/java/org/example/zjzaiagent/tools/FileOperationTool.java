package org.example.zjzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * 文件操作工具类
 */
@Slf4j
public class FileOperationTool {
    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/file";

    // Read file
    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName) {
        try {
            String filePath = safePath(fileName);
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "文件读取失败：" + e.getMessage();
        }
    }

    @Tool(description = "Write content to a text file. Preferred for generating documents, reports, or any text-based content. Supports .txt, .md, .csv, .json and other text formats.")
    public String writeFile(@ToolParam(description = "Name of the file to write (include extension like .txt, .md, .csv)") String fileName,
                            @ToolParam(description = "Content to write to the file") String content) {
        try {
            String filePath = safePath(fileName);
            FileUtil.mkdir(new File(filePath).getParentFile());
            FileUtil.writeUtf8String(content == null ? "" : content, filePath);
            long size = new File(filePath).length();
            log.info("文件写入成功: {}（{} 字节）", filePath, size);
            return "文件已保存：" + filePath + "（" + FileUtil.readableFileSize(size) + "）";
        } catch (Exception e) {
            log.error("文件写入失败: {}", e.getMessage());
            return "文件写入失败：" + e.getMessage();
        }
    }

    /**
     * 只允许写入 tmp/file 目录内，防止模型写出 ../../
     */
    private String safePath(String fileName) throws Exception {
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalArgumentException("文件名为空");
        }
        File base = new File(FILE_DIR).getCanonicalFile();
        File target = new File(base, fileName).getCanonicalFile();
        String basePath = base.getPath();
        if (!target.getPath().equals(basePath)
                && !target.getPath().startsWith(basePath + File.separator)) {
            throw new IllegalArgumentException("非法的文件路径：" + fileName);
        }
        return target.getPath();
    }
}
