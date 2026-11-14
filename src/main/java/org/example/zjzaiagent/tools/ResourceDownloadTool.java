package org.example.zjzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;

/**
 * 资源下载工具类
 */
@Slf4j
public class ResourceDownloadTool {
    @Tool(description = "Download a resource (image, document, etc.) from a URL and save it locally")
    public String downloadResource(@ToolParam(description = "URL of the resource to download") String url,
                                   @ToolParam(description = "Name of the file to save the resource as (include extension)") String fileName) {
        try {
            String safeName = sanitize(fileName);
            String FILE_DIR = FileConstant.FILE_SAVE_DIR + "/download";
            String filePath = FILE_DIR + "/" + safeName;
            // 创建目录
            FileUtil.mkdir(FILE_DIR);
            // 下载资源（设置超时15秒）
            long size = HttpUtil.downloadFile(url, FileUtil.file(filePath), 15000);
            log.info("资源下载成功: {} -> {}（{} 字节）", url, filePath, size);
            return "资源已下载：" + filePath + "（" + FileUtil.readableFileSize(size) + "）";
        } catch (Exception e) {
            log.error("资源下载失败: {}, 错误: {}", url, e.getMessage());
            return "资源下载失败：" + e.getMessage() + "。请检查 URL 是否正确且可访问。";
        }
    }

    private String sanitize(String fileName) {
        String name = fileName == null ? "" : fileName.strip().replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isBlank() || name.contains("..")) {
            name = "download_" + System.currentTimeMillis();
        }
        return name;
    }
}
