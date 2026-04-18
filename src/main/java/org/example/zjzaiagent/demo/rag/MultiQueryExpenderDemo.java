package org.example.zjzaiagent.demo.rag;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.stereotype.Component;
/**
 * 多查询扩展
 */
@Component
public class MultiQueryExpenderDemo {
    private final ChatClient.Builder chatClientBuilder;
    public MultiQueryExpenderDemo(ChatClient.Builder chatClientBuilder) {
        this.chatClientBuilder = chatClientBuilder;
    }

    public MultiQueryExpander multiQueryExpander(){
        MultiQueryExpander expander = MultiQueryExpander.builder()
                .chatClientBuilder(chatClientBuilder)
                .numberOfQueries(3)
                .build();
        return expander;
    }
}
