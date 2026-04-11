package org.example.zjzaiagent.advisor;

import java.util.List;
import java.util.Map;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.CallAdvisor;
import org.springframework.ai.chat.client.advisor.api.CallAdvisorChain;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisor;
import org.springframework.ai.chat.client.advisor.api.StreamAdvisorChain;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

public class MySafeGuardAdvisor implements CallAdvisor, StreamAdvisor {

    private static final String DEFAULT_FAILURE_RESPONSE ="由于包含敏感内容，我无法回应此问题。能否换个方式表达或讨论其他话题?" ;
    private static final int DEFAULT_ORDER = 0;
    private final String failureResponse;
    private final List<String> sensitiveWords;
    private final int order;

    public MySafeGuardAdvisor(List<String> sensitiveWords) {
        this(sensitiveWords,DEFAULT_FAILURE_RESPONSE, 0);
    }

    public MySafeGuardAdvisor(List<String> sensitiveWords, String failureResponse, int order) {
        Assert.notNull(sensitiveWords, "Sensitive words must not be null!");
        Assert.notNull(failureResponse, "Failure response must not be null!");
        this.sensitiveWords = sensitiveWords;
        this.failureResponse = failureResponse;
        this.order = order;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return this.getClass().getSimpleName();
    }

    public ChatClientResponse adviseCall(ChatClientRequest chatClientRequest, CallAdvisorChain callAdvisorChain) {
        return !CollectionUtils.isEmpty(this.sensitiveWords) && this.sensitiveWords.stream().anyMatch((w) -> chatClientRequest.prompt().getContents().contains(w)) ? this.createFailureResponse(chatClientRequest) : callAdvisorChain.nextCall(chatClientRequest);
    }

    public Flux<ChatClientResponse> adviseStream(ChatClientRequest chatClientRequest, StreamAdvisorChain streamAdvisorChain) {
        return !CollectionUtils.isEmpty(this.sensitiveWords) && this.sensitiveWords.stream().anyMatch((w) -> chatClientRequest.prompt().getContents().contains(w)) ? Flux.just(this.createFailureResponse(chatClientRequest)) : streamAdvisorChain.nextStream(chatClientRequest);
    }

    private ChatClientResponse createFailureResponse(ChatClientRequest chatClientRequest) {
        return ChatClientResponse.builder().chatResponse(ChatResponse.builder().generations(List.of(new Generation(new AssistantMessage(this.failureResponse)))).build()).context(Map.copyOf(chatClientRequest.context())).build();
    }

    public int getOrder() {
        return this.order;
    }

    public static final class Builder {
        private List<String> sensitiveWords;
        private String failureResponse = "由于包含敏感内容，我无法回应此问题。能否换个方式表达或讨论其他话题?";
        private int order = 0;

        private Builder() {
        }

        public Builder sensitiveWords(List<String> sensitiveWords) {
            this.sensitiveWords = sensitiveWords;
            return this;
        }

        public Builder failureResponse(String failureResponse) {
            this.failureResponse = failureResponse;
            return this;
        }

        public Builder order(int order) {
            this.order = order;
            return this;
        }

        public MySafeGuardAdvisor build() {
            return new MySafeGuardAdvisor(this.sensitiveWords, this.failureResponse, this.order);
        }
    }
}
