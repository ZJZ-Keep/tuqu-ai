package org.example.zjzaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 *向量转换存储配置
 * @Description:
 * @Author: zjz
 **/
@Configuration
@Slf4j
public class PlanAppVectorStoreConfig {

    @Resource
    private PlanAppDocumentLoader planAppDocumentLoader;

    @Bean
    VectorStore planAppVectorStore(EmbeddingModel dashscopeEmbeddingModel){
        // 创建向量存储
        SimpleVectorStore vectorStore = SimpleVectorStore.builder(dashscopeEmbeddingModel)
                .build();
        // 获取加载文档
        List<Document> documents = planAppDocumentLoader.load();
        log.info("Loaded {} documents", documents.size());
        // 向量存储添加文档
        vectorStore.add(documents);
        log.info("Documents added to vector store");
        return vectorStore;
    }
}
