package org.example.zjzaiagent.rag;

import com.alibaba.cloud.ai.dashscope.api.DashScopeApi;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrievalAdvisor;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrieverOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.RetrievalAugmentationAdvisor;
import org.springframework.aop.Advisor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * rag云知识库增强检索
 */
@Configuration
@Slf4j
public class PlanAppRagCloudConfig {

    @Value("${spring.ai.dashscope.api-key}")
    private String dashscopeApiKey;

    @Bean
    public
     DashScopeDocumentRetrievalAdvisor planAppDashScopeAdvisor() {
        final String KNOWLEDGE_NAME = "行程规划大师";
        DashScopeApi dashScopeApi = DashScopeApi.builder()
                .apiKey(dashscopeApiKey)
                .build();
        
        // 创建 DashScopeDocumentRetriever
        DashScopeDocumentRetriever retriever = new DashScopeDocumentRetriever(
            dashScopeApi, 
            DashScopeDocumentRetrieverOptions.builder()
                .indexName(KNOWLEDGE_NAME)
                .build()
        );
        
        // 创建并返回 DashScopeDocumentRetrievalAdvisor
        return new DashScopeDocumentRetrievalAdvisor(retriever, true);

    }
}
