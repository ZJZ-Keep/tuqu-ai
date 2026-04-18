package org.example.zjzaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrievalAdvisor;
import org.springframework.ai.rag.retrieval.search.DocumentRetriever;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;

/**
 * 自定义向量检索顾问工厂
 */
@Slf4j
public class PlanAppRagCustomAdvisorFactory {
    public static DashScopeDocumentRetrievalAdvisor createPlanAppRagCustomAdvisor(VectorStore vectorStore, String status) {
        // 创建过滤条件
        Filter.Expression expression = new FilterExpressionBuilder()
                .eq("status", status)
                .build();
        // 创建向量检索器
        DocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .filterExpression(expression)
                .similarityThreshold(0.5)
                .topK(3)
                .build();
        // 创建向量检索顾问
        return new DashScopeDocumentRetrievalAdvisor(retriever, true);
    }
}
