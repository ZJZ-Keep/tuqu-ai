package org.example.zjzaiagent.rag;

import jakarta.annotation.Resource;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.model.transformer.KeywordMetadataEnricher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 自定义云信息增强器
 */
@Component
public class MyKeyWordEnricher {
    @Resource
    private ChatModel dashscopeChatModel;
    String chineseKeywordsPrompt = """
                请从以下文档内容中提取最相关的5个中文关键词。
                
                要求：
                1. 必须使用简体中文
                2. 关键词应该准确反映文档的核心主题和内容
                3. 每个关键词应该是2-8个字的词语或短语
                4. 关键词之间用逗号分隔
                5. 不要包含任何解释，只输出关键词
                
                文档内容：
                {context_str}
                
                关键词：
                """;
    public List<Document> enrichKeyWords(List<Document> documents){
        PromptTemplate template = PromptTemplate.builder().template(chineseKeywordsPrompt).build();
        KeywordMetadataEnricher metadataEnricher = KeywordMetadataEnricher.builder(dashscopeChatModel)
                .keywordsTemplate(template)
                .build();
        return metadataEnricher.apply(documents);
    }

}
