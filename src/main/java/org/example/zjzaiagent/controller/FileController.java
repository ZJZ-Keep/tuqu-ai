package org.example.zjzaiagent.controller;

import cn.hutool.core.io.FileUtil;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 生成文件的预览/下载接口。
 *
 * <p>?download=true 时返回 attachment，浏览器会真正保存文件；
 * 否则返回 inline 供 iframe 预览。中文文件名使用 RFC 5987 的 filename* 传输。</p>
 */
@RestController
@RequestMapping("/files")
public class FileController {

    @GetMapping("/{subDir}/{fileName:.+}")
    public ResponseEntity<byte[]> getFile(@PathVariable String subDir,
                                          @PathVariable String fileName,
                                          @RequestParam(name = "download", defaultValue = "false") boolean download) {
        if (subDir == null || !subDir.matches("[A-Za-z0-9_-]+")) {
            return ResponseEntity.badRequest().build();
        }
        File file = resolveFile(subDir, fileName);
        if (file == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] content = FileUtil.readBytes(file);
            MediaType mediaType = mediaTypeOf(fileName);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(mediaType);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(fileName, download));
            headers.setCacheControl("no-store");
            headers.add("X-Content-Type-Options", "nosniff");
            if (isHtml(fileName)) {
                // 生成出来的 HTML 只做展示，禁止执行脚本
                headers.add("Content-Security-Policy", "sandbox");
            }
            return ResponseEntity.ok().headers(headers).body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 解析并校验路径，禁止 ../ 穿越出 tmp 目录
     */
    private File resolveFile(String subDir, String fileName) {
        try {
            File base = new File(FileConstant.FILE_SAVE_DIR).getCanonicalFile();
            File target = new File(new File(base, subDir), fileName).getCanonicalFile();
            String basePath = base.getPath();
            boolean inside = target.getPath().equals(basePath)
                    || target.getPath().startsWith(basePath + File.separator);
            if (!inside || !target.isFile()) {
                return null;
            }
            return target;
        } catch (Exception e) {
            return null;
        }
    }

    private String contentDisposition(String fileName, boolean download) {
        String encoded = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replace("+", "%20");
        String asciiFallback = fileName.replaceAll("[^\\x20-\\x7E]", "_").replace("\"", "'");
        String type = download ? "attachment" : "inline";
        return type + "; filename=\"" + asciiFallback + "\"; filename*=UTF-8''" + encoded;
    }

    private boolean isHtml(String fileName) {
        String ext = extension(fileName);
        return "html".equals(ext) || "htm".equals(ext);
    }

    private String extension(String fileName) {
        int dot = fileName.lastIndexOf('.');
        return dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 明确指定 Content-Type（并给文本类带上 charset），避免浏览器把中文按错误编码解析成乱码
     */
    private MediaType mediaTypeOf(String fileName) {
        String ext = extension(fileName);
        return switch (ext) {
            case "pdf" -> MediaType.APPLICATION_PDF;
            case "html", "htm" -> MediaType.valueOf("text/html;charset=UTF-8");
            case "txt", "log", "csv", "yml", "yaml", "xml", "java", "sql" ->
                    MediaType.valueOf("text/plain;charset=UTF-8");
            case "md", "markdown" -> MediaType.valueOf("text/markdown;charset=UTF-8");
            case "json" -> MediaType.valueOf("application/json;charset=UTF-8");
            case "png" -> MediaType.IMAGE_PNG;
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "gif" -> MediaType.IMAGE_GIF;
            case "webp" -> MediaType.valueOf("image/webp");
            case "bmp" -> MediaType.valueOf("image/bmp");
            case "svg" -> MediaType.valueOf("image/svg+xml");
            case "doc" -> MediaType.valueOf("application/msword");
            case "docx" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
            case "xls" -> MediaType.valueOf("application/vnd.ms-excel");
            case "xlsx" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case "ppt" -> MediaType.valueOf("application/vnd.ms-powerpoint");
            case "pptx" -> MediaType.valueOf("application/vnd.openxmlformats-officedocument.presentationml.presentation");
            case "zip" -> MediaType.valueOf("application/zip");
            default -> MediaType.APPLICATION_OCTET_STREAM;
        };
    }
}
