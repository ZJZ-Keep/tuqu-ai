package org.example.zjzaiagent.app;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.advisor.MyLoggerAdvisor;
import org.example.zjzaiagent.advisor.MySafeGuardAdvisor;
import org.example.zjzaiagent.chatmemoy.FileBasedChatMemory;
import org.example.zjzaiagent.chatmemoy.MySQLChatMemory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@Slf4j
public class PlanApp {
    private final ChatClient chatClient;

    @Resource
    private MySQLChatMemory mySQLChatMemory;

    private final String SYSTEM_PROMPT = "你是【途趣AI行程规划大师】，专业旅行规划AI。\n" +
            "核心：按目的地、天数、人群、预算、偏好，生成**详细、合理、可落地**行程。\n" +
            "输出：行程概览 + 每日时间表（表格）+ 住宿+美食+贴士。\n" +
            "风格：清晰、专业、贴心、结构化。\n" +
            "信息不全时主动提问，不模糊生成。";
    private final List<String> sensitiveWords = List.of("黄", "赌","毒");
    // ai初始化
    public PlanApp(ChatModel dashscopeChatModel,MySQLChatMemory mySQLChatMemory) {
        //初始化基于内存的会话记忆，设置窗口大小为10
       /* MessageWindowChatMemory chatMemory = MessageWindowChatMemory.builder()
                .maxMessages(10)
                .build();*/
        //初始化基于文件系统的会话记忆
        /*String fileDir = System.getProperty("user.dir") + "/chat_memory";
        FileBasedChatMemory chatMemory = new FileBasedChatMemory(fileDir,10);*/
        //初始化基于数据库的会话记忆
        chatClient = org.springframework.ai.chat.client.ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        //会话记忆拦截器
                        MessageChatMemoryAdvisor.builder(mySQLChatMemory).build(),
                        // 日志拦截器
                        MyLoggerAdvisor.builder().build(),
                        // 敏感词拦截器
                        MySafeGuardAdvisor.builder().sensitiveWords(sensitiveWords).build()
                        /*// 重读拦截器
                        ,new ReReadingAdvisor()*/
                )
                .build();
    }

    /**
     * 执行对话
     * @param message
     * @param chatId
     * @return
     */
    public String doChat(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .chatResponse();
        assert response != null;
        String content = response.getResult().getOutput().getText();
        log.info("[PlanApp] content: {}", content);
       // mySQLChatMemory.add(chatId, response.getResult().getOutput());
        return content;
    }



    /**
     * 结构化输出对话
     * @return
     */
    record Report(String title, List< String>suggestions){}
    public Report doChatWithReport(String message, String chatId) {
        Report report = chatClient.prompt()
                .user(message)
                .system(SYSTEM_PROMPT + "请将返回结果生成一个计划报告，包含结果标题含{用户名}，内容为建议列表")
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .call()
                .entity(Report.class);
        assert report != null;
        log.info("[PlanApp] report: {}", report);
        return report;
    }
}