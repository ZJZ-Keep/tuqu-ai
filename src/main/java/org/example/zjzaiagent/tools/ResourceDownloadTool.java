package org.example.zjzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * 资源下载工具类
 */
@Slf4j
public class ResourceDownloadTool {
    @Tool(description = "Download a resource (image, document, etc.) from a URL and save it locally")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url,
                                   @ToolParam(description = "Name of the file to save the resource as (include extension)") String fileName) {
        try {
            String FILE_DIR = FileConstant.FILE_SAVE_DIR+"/download";
            String filePath = FILE_DIR+"/"+fileName;
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            // 下载资源（设置超时15秒）
            HttpUtil.downloadFile(url, FileUtil.file(filePath), 15000);
            log.info("资源下载成功: {} -> {}", url, filePath);
            return "Resource downloaded successfully to: " + filePath;
        } catch (Exception e) {
            log.error("资源下载失败: {}, 错误: {}", url, e.getMessage());
            return "Error downloading resource: " + e.getMessage() + "。请检查URL是否正确且可访问。";
        }
    }
}
