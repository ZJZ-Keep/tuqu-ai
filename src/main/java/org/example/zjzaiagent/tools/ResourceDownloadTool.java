package org.example.zjzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 资源下载工具类
 */
public class ResourceDownloadTool {
    @Tool(description = "Download a resource from a URL")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url,
                                   @ToolParam(description = "Name of the file to save the resource as") String fileName) {
        try {
            String FILE_DIR = FileConstant.FILE_SAVE_DIR+"/download";
            String filePath = FILE_DIR+"/"+fileName;
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            // 下载资源
            HttpUtil.downloadFile(url, filePath);
            return "Resource downloaded successfully: " + filePath;
        } catch (Exception e) {
            return "Error downloading resource: " + e.getMessage();
        }
    }
}
