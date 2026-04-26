package org.example.zjzaiagent.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 抽象基础代理类，用于管理代理状态和执行流程。

 提供状态转换、内存管理和基于步骤的执行循环的基础功能。
 子类必须实现step方法。
 */
@Data
@Slf4j
public abstract class BaseAgent {
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
    List<Message> messages=new ArrayList<>();

    /**
     * 执行流程
     * @param userPrompt
     * @return
     */
    public String run(String userPrompt){
        //校验
        if (this.agentStatus != AgentStatus.IDLE){
            throw new RuntimeException("当前状态不允许执行："+this.agentStatus);
        }
        if (userPrompt == null){
            throw new RuntimeException("用户提示词为空：");
        }
        //更改状态
        this.agentStatus = AgentStatus.RUNNING;
        //添加用户提示
        this.messages.add(new UserMessage(userPrompt));
        //结果列表
        List<String> resultList = new ArrayList<>();
        try {
            for (int i = 0; i <MaxSteps&&this.agentStatus!=AgentStatus.FINISH; i++) {
                currentStep++;
                log.info("开始执行第" + currentStep + "步");
                //执行单个步骤
                String stepResult = this.step();
                String result = "step" + currentStep + "结果：" + stepResult;
                resultList.add(result);
            }
            //检查是否超出步骤限制
            if (currentStep >= MaxSteps) {
                this.agentStatus = AgentStatus.FINISH;
                resultList.add("超出最大步骤限制：" + MaxSteps);
            }
            return String.join("\n",resultList);
        } catch (Exception e) {
            this.agentStatus = AgentStatus.ERROR;
            log.info("执行出错：" + e.getMessage());
            return "执行出错：" + e.getMessage();
        } finally {
            this.cleanup();
        }
    }

    /**
     * 执行流程(sse流式输出)
     * @param userPrompt
     * @return
     */
    public SseEmitter runStream(String userPrompt){
        //创建SseEmitter对象
        SseEmitter sseEmitter = new SseEmitter(300000L);
        try {
            //校验
            if (this.agentStatus != AgentStatus.IDLE){
                sseEmitter.send("当前状态不允许执行："+this.agentStatus);
                sseEmitter.complete();
                return sseEmitter;
            }
            if (userPrompt == null){
                sseEmitter.send("用户提示词为空：");
                sseEmitter.complete();
                return sseEmitter;
            }
        } catch (IOException e) {
            sseEmitter.completeWithError(e);
        }
        CompletableFuture.runAsync(()->{
            //更改状态
            this.agentStatus = AgentStatus.RUNNING;
            //添加用户提示
            this.messages.add(new UserMessage(userPrompt));
            //结果列表
            List<String> resultList = new ArrayList<>();
            try {
                for (int i = 0; i <MaxSteps&&this.agentStatus!=AgentStatus.FINISH; i++) {
                    currentStep++;
                    log.info("开始执行第" + currentStep + "步");
                    //执行单个步骤
                    String stepResult = this.step();
                    String result = "step" + currentStep + "结果：" + stepResult;
                    resultList.add(result);
                    sseEmitter.send(result);
                }
                //检查是否超出步骤限制
                if (currentStep >= MaxSteps) {
                    this.agentStatus = AgentStatus.FINISH;
                    resultList.add("超出最大步骤限制：" + MaxSteps);
                    sseEmitter.send("超出最大步骤限制：" + MaxSteps);
                }
                // 任务正常完成，关闭SSE连接
                sseEmitter.complete();
            } catch (Exception e) {
                this.agentStatus = AgentStatus.ERROR;
                log.info("执行出错：" + e.getMessage());
                try {
                    sseEmitter.send("执行出错：" + e.getMessage());
                    sseEmitter.complete();
                } catch (IOException ex) {
                    sseEmitter.completeWithError(ex);
                }
            } finally {
                this.cleanup();
            }
        });
        //设置超时和完成回调
        sseEmitter.onTimeout(() -> {
            this.agentStatus = AgentStatus.ERROR;
            this.cleanup();
            log.warn("SSE连接超时");
        });
        sseEmitter.onCompletion(() -> {
            if (this.agentStatus == AgentStatus.RUNNING) {
                this.agentStatus = AgentStatus.FINISH;
            }
            this.cleanup();
            log.info("SSE连接完成");
        });
        return sseEmitter;
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
