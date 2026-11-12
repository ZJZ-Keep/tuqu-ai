package org.example.zjzaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.IOException;

/**
 * 网页抓取工具类
 */
@Slf4j
public class WebScrapingTool {
    @Tool(description = "Scrape text content from a web page. Returns the main text content of the page.")
    public String scrapeWeb(@ToolParam(description = "URL of the web page to scrape") String url)  {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(15000)
                    .followRedirects(true)
                    .get();

            // 移除脚本、样式和导航元素以获取纯文本内容
            document.select("script, style, nav, header, footer, iframe, noscript").remove();

            // 尝试获取主要内容
            Elements mainContent = document.select("article, main, .content, .article, #content, .post-content");
            String text;
            if (!mainContent.isEmpty()) {
                text = mainContent.first().text();
            } else {
                text = document.body() != null ? document.body().text() : document.text();
            }

            // 限制返回长度
            if (text.length() > 5000) {
                text = text.substring(0, 5000) + "\n...(内容已截断，共" + document.text().length() + "字符)";
            }

            if (text.trim().isEmpty()) {
                return "未能从该网页提取到文本内容。可能是动态加载的页面。";
            }

            log.info("网页抓取成功: {}, 内容长度: {}", url, text.length());
            return "网页标题: " + document.title() + "\n\n内容:\n" + text;
        } catch (IOException e) {
            log.error("网页抓取失败: {}, 错误: {}", url, e.getMessage());
            return "网页抓取失败：" + e.getMessage() + "。请确认URL是否正确且可访问。";
        }
    }
}
