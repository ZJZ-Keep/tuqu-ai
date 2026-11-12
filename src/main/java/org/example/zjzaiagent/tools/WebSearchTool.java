package org.example.zjzaiagent.tools;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
/**
 * 网页搜索工具类
 */
@Slf4j
public class WebSearchTool {

    private final String apiKey;

    public WebSearchTool(String apiKey) {
        this.apiKey = apiKey;
    }
    // SearchAPI 的搜索接口地址
    private static final String SEARCH_API_URL = "https://api.tavily.com/search";

    @Tool(description = "Search for information from the web. Returns search results with titles, links, and snippets.")
    public String searchWeb(@ToolParam(description = "Search query keyword") String query) {
        Map<String, Object> body = new HashMap<>();
        body.put("api_key", apiKey);
        body.put("query", query);
        body.put("max_results", 5);
        body.put("search_depth", "basic");   // basic=1 credit, advanced=2 credits
        try {
            String response = HttpUtil.post(SEARCH_API_URL, JSONUtil.toJsonStr(body), 15000);
            JSONObject jsonObject = JSONUtil.parseObj(response);
            if (jsonObject.containsKey("error")) {
                String error = jsonObject.getStr("error");
                log.error("Tavily返回错误: {}", error);
                return "搜索服务暂时不可用，错误信息：" + error;
            }
            JSONArray results = jsonObject.getJSONArray("results");
            if (results == null || results.isEmpty()) {
                return "未搜索到相关结果。建议：1) 使用更具体的关键词 2) 减少关键词数量。";
            }
            int count = Math.min(results.size(), 5);
            StringBuilder sb = new StringBuilder("搜索结果（共").append(count).append("条）：\n\n");
            for (int i = 0; i < count; i++) {
                JSONObject item = (JSONObject) results.get(i);
                String title = item.getStr("title", "无标题");
                String link = item.getStr("url", "");
                String content = item.getStr("content", "无摘要");
                if (content.length() > 200) content = content.substring(0, 200) + "...";
                sb.append("【").append(i + 1).append("】").append(title).append("\n");
                sb.append("链接：").append(link).append("\n");
                sb.append("摘要：").append(content).append("\n\n");
            }
            log.info("搜索成功，返回{}条结果", count);
            return sb.toString();
        } catch (Exception e) {
            log.error("搜索出错: {}", e.getMessage());
            return "搜索出错：" + e.getMessage() + "。请检查API密钥是否有效。";
        }
    }

}
