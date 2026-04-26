package org.example.zjzaiagent.agent;

import cn.hutool.core.collection.CollUtil;
import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatOptions;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.tool.ToolCallingManager;
import org.springframework.ai.model.tool.ToolExecutionResult;
import org.springframework.ai.tool.ToolCallback;
import java.util.List;
import java.util.stream.Collectors;
/**
 处理工具调用的基础代理类，具体实现了 think 和 act 方法，可以用作创建实例的父类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class ToolCallAgent extends ReActAgent{
    //可用的工具列表
    private final ToolCallback[] availableTools;

    //工具管理
    private ToolCallingManager toolCallingManager;

    //调用工具的响应
    //todo
    private ChatResponse toolCallChatResponse;

    //禁用spring ai 内置的工具调用机制，改用自定义的
    private final ChatOptions chatOptions;

    public ToolCallAgent(ToolCallback[] availableTools){
        super();
        this.availableTools = availableTools;
        this.toolCallingManager = ToolCallingManager.builder().build();
        this.chatOptions = DashScopeChatOptions.builder()
                .withInternalToolExecutionEnabled(false)
                .build();

    }

    /**
     处理当前状态并决定下一步行动
     @return 是否需要执行行动
     */
    @Override
    public boolean think() {
        //校验
        if (getNextStepPrompt()!=null&&!getNextStepPrompt().isEmpty()){
            getMessages().add(new UserMessage(getNextStepPrompt()));
        }
        List<Message> messageList = getMessages();
        Prompt prompt = new Prompt(messageList,chatOptions);
        try {
            //获取带工具选项的响应
            ChatResponse chatResponse = getChatClient().prompt(prompt)
                    .system(getSystemPrompt())
                    .toolCallbacks(availableTools)
                    .call()
                    .chatResponse();
            //记录响应，用于Act
            this.toolCallChatResponse = chatResponse;
            if (chatResponse != null) {
                AssistantMessage assistantMessage = chatResponse.getResult().getOutput();
                //输出提示信息
                String result = assistantMessage.getText();
                List<AssistantMessage.ToolCall> toolCallList = assistantMessage.getToolCalls();
                log.info(getName()+"的思考:"+result);
                log.info(getName()+"的调用工具:"+toolCallList.size()+"个");
                String tooCalls = toolCallList.stream()
                        .map(toolCall -> String.format("调用的工具:%s，参数:%s", toolCall.name(), toolCall.arguments()))
                        .collect(Collectors.joining("\n"));
                log.info(tooCalls);
                boolean shouldTerminate = result != null &&
                        (result.contains("任务结束") || result.contains("圆满结束") ||
                                result.toLowerCase().contains("terminate") ||
                                result.contains("任务已完成") || result.contains("已成功生成"));
                if (shouldTerminate) {
                    log.info("检测到终止信号，结束任务");
                    setAgentStatus(AgentStatus.FINISH);
                    getMessages().add(assistantMessage);
                    return false;
                }
                if (toolCallList.isEmpty()){
                    //如果没有工具调用，则返回助手信息
                    getMessages().add(assistantMessage);
                    return false;
                }else {
                    return true;
                }
            }else {
                log.info(getName()+"的思考过程无结果");
                return false;
            }
        } catch (Exception e) {
            log.error(getName()+"的思考过程遇到问题："+e.getMessage());
            getMessages().add(new AssistantMessage("思考过程遇到问题："+e.getMessage()));
            return false;
        }
    }

    /**
     执行工具调用并获取结果
     * @return 执行结果
     */
    @Override
    public String act() {
        if (!toolCallChatResponse.hasToolCalls()){
            return "没有工具调用";
        }
        //调用工具
        Prompt prompt = new Prompt(getMessages(),chatOptions);
        ToolExecutionResult toolExecutionResult = toolCallingManager.executeToolCalls(prompt, toolCallChatResponse);
        //获取消息上下文
        setMessages(toolExecutionResult.conversationHistory());
        //当前工具调用结果
        ToolResponseMessage toolResponseMessage = (ToolResponseMessage) CollUtil.getLast(toolExecutionResult.conversationHistory());
        String results = toolResponseMessage.getResponses().stream()
                .map(toolResponse -> "工具：" + toolResponse.name() + "，结果：" + toolResponse.responseData())
                .collect(Collectors.joining("\n"));
        //判断是否结束
        boolean anyMatch = toolResponseMessage.getResponses().stream()
                .anyMatch(response -> "doTerminate".equals(response.name()));
        if (anyMatch){
            setAgentStatus(AgentStatus.FINISH);
        }
        log.info(getName()+"的调用结果："+results);
        return results;
    }
}
