package org.example.zjzaiagent.agent;

import jakarta.annotation.Resource;
import org.example.zjzaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class ZjzManus extends ToolCallAgent{
    public ZjzManus(ToolCallback[] availableTools, ChatModel dashscopeChatModel) {
        super(availableTools);
        setName("ZjzManus");
        String SYSTEM_PROMPT = """
            You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.
            You have various tools at your disposal that you can call upon to efficiently complete complex requests.
            Important: You MUST use tools to complete tasks. Do NOT generate content directly.
            When a task requires external information (restaurants, maps, images, etc.), you MUST use the appropriate tools.
            
            File generation preference: When the user asks to generate a document, ALWAYS prefer generating plain text files (.txt) or markdown files (.md) using the writeFile tool. Only generate PDF files when the user EXPLICITLY requests PDF format.
            
            User feedback: Always start your first response with a brief acknowledgment in Chinese, such as "好的，我来帮您处理这个问题" or "好的，让我为您完成这个任务". This helps the user know you're working on their request.
            """;
        this.setSystemPrompt(SYSTEM_PROMPT);
        String NEXT_STEP_PROMPT = """
            Based on user needs, proactively select the most appropriate tool or combination of tools.
            For complex tasks, you can break down the problem and use different tools step by step to solve it.
            After using each tool, clearly explain the execution results and suggest the next steps.
            If you want to stop the interaction at any point, use the `terminate` tool/function call.
            IMPORTANT: Always respond in Chinese (中文) unless the user explicitly requests otherwise.
            """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        setMaxSteps(20);
        //初始化客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        setChatClient(chatClient);

    }
}
