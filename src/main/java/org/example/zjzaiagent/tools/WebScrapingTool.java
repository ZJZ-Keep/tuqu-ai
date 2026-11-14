package org.example.zjzaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.UnsupportedMimeTypeException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.net.SocketTimeoutException;

/**
 * 网页抓取工具类
 */
@Slf4j
public class WebScrapingTool {

    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /** 单页最多读取 2MB，避免大文件拖慢任务 */
    private static final int MAX_BODY_BYTES = 2 * 1024 * 1024;

    @Tool(description = "Scrape text content from a web page. Returns the main text content of the page. "
            + "Some sites reject non-browser clients during the TLS handshake or return 403/429; "
            + "if scraping fails, use searchWeb results instead of retrying the same URL.")
    public String scrapeWeb(@ToolParam(description = "URL of the web page to scrape") String url) {
        try {
            Document document = Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,*/*;q=0.8")
                    .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                    .header("Cache-Control", "no-cache")
                    .header("Pragma", "no-cache")
                    .header("Upgrade-Insecure-Requests", "1")
                    .timeout(20000)
                    .followRedirects(true)
                    .ignoreContentType(true)
                    .maxBodySize(MAX_BODY_BYTES)
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
        } catch (HttpStatusException e) {
            int status = e.getStatusCode();
            log.warn("网页抓取返回异常状态: {} -> HTTP {}", url, status);
            if (status == 403 || status == 429) {
                return "网页抓取失败：目标站点返回 HTTP " + status
                        + "（被反爬或限流拦截）。不要重试该地址，请改用 searchWeb 的搜索结果摘要或换一个来源。";
            }
            return "网页抓取失败：目标站点返回 HTTP " + status + "，请确认 URL 是否正确或页面是否已删除。";
        } catch (UnsupportedMimeTypeException e) {
            log.warn("网页抓取内容类型不支持: {} -> {}", url, e.getMimeType());
            return "网页抓取失败：该链接返回的内容类型是 " + e.getMimeType()
                    + "，不是网页，请确认链接是否正确。";
        } catch (SSLHandshakeException e) {
            // 常见于目标站点 WAF/反爬按 TLS 指纹拒绝，或站点只支持特定 TLS 版本
            log.warn("网页抓取 TLS 握手失败: {} -> {}", url, e.getMessage());
            return "网页抓取失败：目标站点拒绝了 TLS 握手（" + e.getMessage() + "）。"
                    + "这通常是站点反爬/WAF 或只支持特定 TLS 版本导致的，不是 URL 拼写问题；"
                    + "不要重复抓取该地址，请改用 searchWeb 的搜索结果摘要或换一个可访问的来源。";
        } catch (SocketTimeoutException e) {
            log.warn("网页抓取超时: {}", url);
            return "网页抓取失败：访问超时（20 秒），站点可能限流或网络不可达。请改用 searchWeb 的结果或换一个来源。";
        } catch (IOException e) {
            log.error("网页抓取失败: {}, 错误: {}", url, e.getMessage());
            return "网页抓取失败：" + e.getMessage() + "。请确认 URL 是否正确且可访问。";
        }
    }
}
