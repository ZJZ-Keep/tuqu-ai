package org.example.zjzaiagentmcpserver.tools;

import jakarta.annotation.Resource;
import lombok.Locked;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class ImageSearchToolTest {

    @Resource
    private ImageSearchTool imageSearchTool;
    @Test
    void searchImage() {
        String result = imageSearchTool.searchImage("cat");
        Assertions.assertNotNull(result);
    }
}