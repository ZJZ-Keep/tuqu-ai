package org.example.zjzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
/**
 * 文件操作工具类
 */
@Slf4j
public class FileOperationTool {
    private final String FILE_DIR = FileConstant.FILE_SAVE_DIR+"/file";
    // Read file
    @Tool(description = "Read content from a file")
    public String readFile(@ToolParam(description = "Name of the file to read") String fileName) {
        String filePath = FILE_DIR+"/"+fileName;
        try {
            return FileUtil.readUtf8String(filePath);
        } catch (Exception e) {
            return "Error reading file: " + e.getMessage();
        }
    }

    @Tool(description = "Write content to a text file. Preferred for generating documents, reports, or any text-based content. Supports .txt, .md, .csv, .json and other text formats.")
    public String writeFile(@ToolParam(description = "Name of the file to write (include extension like .txt, .md, .csv)") String fileName,
                            @ToolParam(description = "Content to write to the file") String content){
        String filePath = FILE_DIR+"/"+fileName;
        try {
            FileUtil.mkdir(FILE_DIR);
            FileUtil.writeUtf8String(content,filePath);
            log.info("文件写入成功: {}", filePath);
            return "File written successfully to: " + filePath;
        } catch (Exception e) {
            log.error("文件写入失败: {}", e.getMessage());
            return "Error writing file: " + e.getMessage();
        }

    }
}
