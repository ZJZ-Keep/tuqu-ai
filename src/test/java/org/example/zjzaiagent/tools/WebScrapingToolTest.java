package org.example.zjzaiagent.tools;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class WebScrapingToolTest {

    @Test
    void scrapeWeb() {
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        String scrapeWeb = webScrapingTool.scrapeWeb("https://www.baidu.com");
        Assertions.assertNotNull(scrapeWeb);
    }
}