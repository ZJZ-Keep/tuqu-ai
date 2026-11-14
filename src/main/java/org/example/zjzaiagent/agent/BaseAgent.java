package org.example.zjzaiagent.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 抽象基础代理类，用于管理代理状态和执行流程。

 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {

    /** SSE 连接最长保持时间：30 分钟（复杂任务可能执行十几分钟） */
    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L;

    /** 心跳间隔（秒）。用于避免浏览器/代理把长时间无数据的连接判定为超时 */
    private static final long HEARTBEAT_SECONDS = 15L;

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    /** SSE 事件统一按 UTF-8 推送，避免中文被按 ISO-8859-1 编码成乱码 */
    private static final MediaType EVENT_MEDIA_TYPE = MediaType.valueOf("text/plain;charset=UTF-8");

    /** 每个会话用独立的虚拟线程执行，避免阻塞公共线程池 */
    private static final ExecutorService AGENT_EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    /** 心跳线程 */
    private static final ScheduledExecutorService HEARTBEAT_EXECUTOR =
            Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, "agent-sse-heartbeat");
                thread.setDaemon(true);
                return thread;
            });

    // 核心属性
    protected String name;

    //提示词
    private String systemPrompt;
    private String nextStepPrompt;

    //默认状态
    private AgentStatus agentStatus = AgentStatus.IDLE;

    //LLM 模型
    private ChatClient chatClient;

    //步骤
    private int currentStep = 0;
    private int MaxSteps = 10;

    //记录记忆
    List<Message> messages = new ArrayList<>();

    /** 当前步骤产生的、需要推送给前端的结构化事件 */
    private final List<AgentEvent> pendingEvents = new ArrayList<>();

    /**
     * 子类在 step() 中调用，把结构化进度交给 runStream 推送给前端。
     */
    protected void emit(AgentEvent event) {
        if (event != null) {
            pendingEvents.add(event);
        }
    }

    private List<AgentEvent> drainEvents() {
        List<AgentEvent> events = new ArrayList<>(pendingEvents);
        pendingEvents.clear();
        return events;
    }

    /**
     * 执行流程（阻塞式，供测试/命令行使用）
     */
    public String run(String userPrompt) {
        //校验
        if (this.agentStatus != AgentStatus.IDLE) {
            throw new RuntimeException("当前状态不允许执行：" + this.agentStatus);
        }
        if (userPrompt == null) {
            throw new RuntimeException("用户提示词为空：");
        }
        //更改状态
        this.agentStatus = AgentStatus.RUNNING;
        //添加用户提示
        this.messages.add(new UserMessage(userPrompt));
        //结果列表
        List<String> resultList = new ArrayList<>();
        try {
            while (currentStep < MaxSteps && this.agentStatus == AgentStatus.RUNNING) {
                currentStep++;
                log.info("开始执行第" + currentStep + "步");
                //执行单个步骤
                String stepResult = this.step();
                resultList.add(stepResult);
                drainEvents();
            }
            //检查是否超出步骤限制
            if (currentStep >= MaxSteps && this.agentStatus == AgentStatus.RUNNING) {
                this.agentStatus = AgentStatus.FINISH;
                resultList.add("超出最大步骤限制：" + MaxSteps);
            }
            return String.join("\n", resultList);
        } catch (Exception e) {
            this.agentStatus = AgentStatus.ERROR;
            log.error("执行出错", e);
            return "执行出错：" + e.getMessage();
        } finally {
            this.cleanup();
        }
    }

    /**
     * 执行流程（SSE 流式输出）
     *
     * <p>推送的事件均为 JSON，包含 type/message 等字段，前端只需渲染人能看懂的内容：
     * progress（进度）、thinking（模型说明）、tool（工具执行情况，可折叠原始输出）、
     * file（生成的文件）、final（最终答复）、error（错误）、done（结束）。</p>
     */
    public SseEmitter runStream(String userPrompt) {
        SseEmitter sseEmitter = new SseEmitter(SSE_TIMEOUT_MS);
        AtomicBoolean clientGone = new AtomicBoolean(false);

        if (this.agentStatus != AgentStatus.IDLE) {
            sendEvent(sseEmitter, clientGone, AgentEvent.error("当前状态不允许执行：" + this.agentStatus));
            completeQuietly(sseEmitter);
            return sseEmitter;
        }
        if (userPrompt == null || userPrompt.isBlank()) {
            sendEvent(sseEmitter, clientGone, AgentEvent.error("用户提示词为空"));
            completeQuietly(sseEmitter);
            return sseEmitter;
        }

        // 注意：客户端断开/超时不能改变 agent 状态，否则后台任务会被“半路掐死”
        sseEmitter.onTimeout(() -> log.warn("SSE 连接超时，任务仍会继续执行，前端可稍后重新发起查询"));
        sseEmitter.onCompletion(() -> log.info("SSE 连接关闭"));

        // 心跳：让前端知道任务还在跑，也避免中间代理断开空闲连接
        ScheduledFuture<?> heartbeat = HEARTBEAT_EXECUTOR.scheduleAtFixedRate(
                () -> sendEvent(sseEmitter, clientGone, AgentEvent.builder().type("ping").build()),
                HEARTBEAT_SECONDS, HEARTBEAT_SECONDS, TimeUnit.SECONDS);

        AGENT_EXECUTOR.execute(() -> {
            try {
                //更改状态
                this.agentStatus = AgentStatus.RUNNING;
                //添加用户提示
                this.messages.add(new UserMessage(userPrompt));

                sendEvent(sseEmitter, clientGone, AgentEvent.progress("已收到您的请求，正在为您处理…", 0));

                while (currentStep < MaxSteps && this.agentStatus == AgentStatus.RUNNING) {
                    currentStep++;
                    log.info("开始执行第" + currentStep + "步");
                    sendEvent(sseEmitter, clientGone,
                            AgentEvent.progress("正在执行第" + currentStep + "步…", currentStep));
                    try {
                        String stepResult = this.step();
                        List<AgentEvent> events = drainEvents();
                        if (events.isEmpty()) {
                            // 没有结构化事件时退化为一行可读提示，绝不输出原始 step 文本
                            if (stepResult != null && !stepResult.isBlank()) {
                                sendEvent(sseEmitter, clientGone, AgentEvent.thinking(stepResult.strip()));
                            }
                        } else {
                            events.forEach(event -> sendEvent(sseEmitter, clientGone, event));
                        }
                    } catch (Exception stepError) {
                        log.error("第{}步执行失败", currentStep, stepError);
                        drainEvents().forEach(event -> sendEvent(sseEmitter, clientGone, event));
                        sendEvent(sseEmitter, clientGone,
                                AgentEvent.error("第" + currentStep + "步执行失败：" + stepError.getMessage()));
                        this.agentStatus = AgentStatus.ERROR;
                    }
                }

                if (this.agentStatus == AgentStatus.RUNNING) {
                    this.agentStatus = AgentStatus.FINISH;
                    sendEvent(sseEmitter, clientGone,
                            AgentEvent.progress("已达到最大步骤限制（" + MaxSteps + "），任务结束。", currentStep));
                }
                sendEvent(sseEmitter, clientGone, AgentEvent.builder().type("done").build());
            } catch (Exception e) {
                this.agentStatus = AgentStatus.ERROR;
                log.error("任务执行出错", e);
                sendEvent(sseEmitter, clientGone, AgentEvent.error("执行出错：" + e.getMessage()));
                sendEvent(sseEmitter, clientGone, AgentEvent.builder().type("done").build());
            } finally {
                heartbeat.cancel(true);
                this.cleanup();
                completeQuietly(sseEmitter);
            }
        });

        return sseEmitter;
    }

    /**
     * 推送一条事件。客户端断开时只记录日志并继续执行任务（邮件、文件等副作用不能丢）。
     */
    private void sendEvent(SseEmitter sseEmitter, AtomicBoolean clientGone, AgentEvent event) {
        if (event == null || clientGone.get()) {
            return;
        }
        String json;
        try {
            json = OBJECT_MAPPER.writeValueAsString(event);
        } catch (Exception e) {
            log.error("SSE 事件序列化失败：{}", e.getMessage(), e);
            return;
        }
        try {
            sseEmitter.send(SseEmitter.event().data(json, EVENT_MEDIA_TYPE));
        } catch (Exception e) {
            clientGone.set(true);
            log.warn("SSE 推送失败（客户端可能已断开），任务继续执行：{}", e.getMessage());
        }
    }

    private void completeQuietly(SseEmitter sseEmitter) {
        try {
            sseEmitter.complete();
        } catch (Exception e) {
            log.debug("关闭 SSE 连接时出现异常：{}", e.getMessage());
        }
    }

    /**
     * 执行单个步骤
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){};
}
