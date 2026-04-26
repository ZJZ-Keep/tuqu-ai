package org.example.zjzaiagent.controller;

import jakarta.annotation.Resource;
import org.example.zjzaiagent.agent.ZjzManus;
import org.example.zjzaiagent.app.PlanApp;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;

@RestController
@RequestMapping("/ai")
public class AiController {


    @Resource
    private PlanApp planApp;

    @Resource
    private ToolCallback[] allTools;

    @Resource
    private ChatModel dashscopeChatModel;

    /**
     * 流式输出对话（produces）
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/plan_app/chat/Sse",produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamSSE(String message, String chatId) {
        return planApp.doChatStream(message, chatId)
                .map(chunk -> "data: " + chunk + "\n\n")
                .concatWith(Flux.just("data: [DONE]\n\n"));
    }
    /**
     * 流式输出对话（map）
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping(value = "/plan_app/chat/ServerSetEvent")
    public Flux<ServerSentEvent<String>> chatStreamServerSetEvent(String message, String chatId) {
         return planApp.doChatStream(message, chatId)
                 .map(chunk->ServerSentEvent.<String>builder()
                         .data(chunk)
                         .build());
    }

    /**
     * 流式输出对话（SseEmitter）
     * @param message
     * @param chatId
     * @return
     */
    @GetMapping("/plan_app/chat/SseEmitter")
    public SseEmitter chatStreamSseEmitter(String message, String chatId) {
        // 创建SseEmitter对象
        SseEmitter sseEmitter = new SseEmitter(180000L);
         planApp.doChatStream(message, chatId)
                .subscribe(
                      chunk->{
                          try {
                              sseEmitter.send(chunk);
                          } catch (IOException e) {
                              sseEmitter.completeWithError(e);
                          }
                      },
                      sseEmitter::completeWithError,
                        sseEmitter::complete
                );
         return sseEmitter;
    }
    /**
     * 流式输出对话（ZjzManus）
     * @param message
     * @return
     */
    @GetMapping("/zjz_manus/chat/SseEmitter")
    public SseEmitter ZjzManusStream(String message) {
        ZjzManus zjzManus = new ZjzManus(allTools, dashscopeChatModel);
        return zjzManus.runStream(message);
    }
}
