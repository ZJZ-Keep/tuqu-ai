package org.example.zjzaiagent.controller;

import cn.hutool.core.io.FileUtil;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/files")
public class FileController {

    @GetMapping("/{subDir}/{fileName}")
    public ResponseEntity<byte[]> getFile(@PathVariable String subDir, @PathVariable String fileName) {
        String filePath = FileConstant.FILE_SAVE_DIR + "/" + subDir + "/" + fileName;
        File file = new File(filePath);
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] content = FileUtil.readBytes(file);
            String contentType = Files.probeContentType(Path.of(filePath));
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(content);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
