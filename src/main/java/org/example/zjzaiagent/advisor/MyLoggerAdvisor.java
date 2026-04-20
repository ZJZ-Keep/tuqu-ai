package org.example.zjzaiagent.advisor;//

import java.util.function.Function;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientMessageAggregator;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;
/**
 * 自定义日志 Advisor
 * 打印 info 级别日志、只输出单次用户提示词和 AI 回复的文本
 */
@Slf4j
public class MyLoggerAdvisor implements CallAdvisor, StreamAdvisor {
    @NotNull
    public ChatClientResponse adviseCall(@NotNull ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        this.logRequest(chatClientRequest);
        ChatClientResponse chatClientResponse = callAdvisorChain.nextCall(chatClientRequest);
        this.logResponse(chatClientResponse);
        return chatClientResponse;
    }

    @NotNull
    public Flux<ChatClientResponse> adviseStream(@NotNull ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        this.logRequest(chatClientRequest);
        Flux<ChatClientResponse> chatClientResponses = streamAdvisorChain.nextStream(chatClientRequest);
        return (new ChatClientMessageAggregator()).aggregateChatClientResponse(chatClientResponses, this::logResponse);
    }

    protected ChatClientRequest logRequest(ChatClientRequest request) {
        log.info("AI Request: {}", request.prompt().getUserMessage().getText());
        return request;
    }

    protected void logResponse(ChatClientResponse chatClientResponse) {
        if (chatClientResponse.chatResponse() != null) {
            log.info("AI Response: {}", chatClientResponse.chatResponse().getResult().getOutput().getText());
            // 记录工具调用信息
            var response = chatClientResponse.chatResponse();
            if (response.getResults() != null && !response.getResults().isEmpty()) {
                var toolCalls = response.getResults().get(0).getOutput().getToolCalls();
                if (toolCalls != null && !toolCalls.isEmpty()) {
                    log.info("Tool calls triggered: {}", toolCalls);
                } else {
                    log.info("No tool calls were triggered");
                }
            }
        }
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public int getOrder() {
        return 0;
    }

    public String toString() {
        return MyLoggerAdvisor.class.getName();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Builder() {
        }

        public MyLoggerAdvisor build() {
            return new MyLoggerAdvisor();
        }
    }
}
