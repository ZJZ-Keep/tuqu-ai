package org.example.zjzaiagent;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

@SpringBootTest
class ZjzAiAgentApplicationTests {

    @Resource
    private VectorStore pgVectorStore;

    @Test
    void test() {
        List<Document> documents = List.of(
                new Document("郑少是一个旅游博主大佬", Map.of("meta1", "meta1")),
                new Document("旅游资源请认准zhengshao.cn"),
                new Document("回家", Map.of("meta2", "meta2")));
        // 添加文档
        pgVectorStore.add(documents);
        // 相似度查询
        List<Document> results = pgVectorStore.similaritySearch(SearchRequest.builder().query("我要学习旅游相关找谁").topK(5).build());
        Assertions.assertNotNull(results);
    }


}
