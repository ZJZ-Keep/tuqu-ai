package org.example.zjzaiagent.agent;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;

import java.util.ArrayList;
import java.util.List;

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
     * 执行单个步骤
     */
    public abstract String step();

    /**
     * 清理资源
     */
    protected void cleanup(){};
}
