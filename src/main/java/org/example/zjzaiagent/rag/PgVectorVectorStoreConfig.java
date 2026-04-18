package org.example.zjzaiagent.rag;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.pgvector.PgVectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;

/**
 * 向量转换存储配置(pgVector向量存储)
 */
//@Configuration
@Slf4j
public class PgVectorVectorStoreConfig {

    @Resource
    @Qualifier("pgDataSource")
    private DataSource pgDataSource;

    @Resource
    private PlanAppDocumentLoader planAppDocumentLoader;

    @Resource
    private MyKeyWordEnricher myKeyWordEnricher;
    @Bean
    public VectorStore pgVectorStore(EmbeddingModel dashscopeEmbeddingModel){
        JdbcTemplate jdbcTemplate = new JdbcTemplate();
        jdbcTemplate.setDataSource(pgDataSource);
        PgVectorStore pgVectorStore = PgVectorStore.builder(jdbcTemplate, dashscopeEmbeddingModel)
                .dimensions(1024)
                .distanceType(PgVectorStore.PgDistanceType.COSINE_DISTANCE)
                .indexType(PgVectorStore.PgIndexType.HNSW)
                .initializeSchema(true)
                .schemaName("public")
                .vectorTableName("vector_store")
                .maxDocumentBatchSize(10)
                .build();
        // 加载数据
        List<Document> documents = planAppDocumentLoader.load();
        log.info("Loaded {} documents", documents.size());
        //自动补充关键元信息
        List<Document> enrichDocuments = myKeyWordEnricher.enrichKeyWords(documents);
        // 分批添加文档，每批不超过10个
        int batchSize = 10;
        for (int i = 0; i < enrichDocuments.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, enrichDocuments.size());
            List<Document> batch = enrichDocuments.subList(i, endIndex);
            log.info("Adding batch {}-{} of {}", i+1, endIndex, enrichDocuments.size());
            pgVectorStore.add(batch);
        }
        log.info("All documents added to PGVectorStore");
        return pgVectorStore;
    }
}
