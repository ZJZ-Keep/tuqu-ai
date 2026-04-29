package org.example.zjzaiagent.app;

import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetrievalAdvisor;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.advisor.MyLoggerAdvisor;
import org.example.zjzaiagent.advisor.MySafeGuardAdvisor;
import org.example.zjzaiagent.chatmemoy.MySQLChatMemory;
import org.example.zjzaiagent.rag.QueryRewriter;
import org.example.zjzaiagent.tools.ToolRegistration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;


@Component
@Slf4j
public class PlanApp {
    private final ChatClient chatClient;


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
        return content;
    }

    /**
     * 执行对话(sse流式输出)
     * @param message
     * @param chatId
     * @return
     */
    public Flux<String> doChatStream(String message, String chatId) {
        Flux<String> content = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .stream()
                .content();
        log.info("[PlanApp] content: {}", content);
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
    /**
     * rag检索对话
     * @param message
     * @param chatId
     * @return
     */
    @Resource
    @Lazy //  注入时也加懒加载
    private VectorStore planAppVectorStore;
    @Resource
    private DashScopeDocumentRetrievalAdvisor planAppDashScopeAdvisor;
    @Resource
    private QueryRewriter queryRewriter;
    /*@Resource
    private VectorStore pgVectorStore;*/
    public String doChatWithRag(String message, String chatId) {
        // 重写查询
        String rewriteMessage = queryRewriter.doQueryRewrite(message);
        // 调整检索参数，降低阈值提高灵敏度
        SearchRequest searchRequest = SearchRequest.builder()
                .similarityThreshold(0.5)//相似度阈值
                .topK(3)
                .build();
        // 自定义提示模板，确保检索到的文档内容被正确使用
        /*String RAG_USER_TEXT_ADVISE = """
        你是【途趣AI行程规划大师】，专业旅行规划AI。
        请根据以下提供的文档内容回答用户的问题。
        如果文档内容中没有相关信息，请回答"根据提供的文档，无法回答该问题"。
        
        文档内容：
        {question_answer_context}
        
        用户问题：
        {query}
        """;

        QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(pgVectorStore)
                .searchRequest(searchRequest)
                .userTextAdvise(RAG_USER_TEXT_ADVISE)
                .build();*/
        // 先调用 RAG 检索，再调用其他 advisor
        ChatResponse response = chatClient.prompt()
                .user(rewriteMessage)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                //  rag 检索对话(内存向量数据库)
                .advisors(new QuestionAnswerAdvisor(planAppVectorStore, searchRequest))
                // rag 检索增强(云知识库)
                //.advisors(planAppDashScopeAdvisor)
                //rag 检索增强(pg向量数据库,QuestionAnswerAdvisor)
                //.advisors(new QuestionAnswerAdvisor(pgVectorStore, searchRequest))
                //.advisors(qaAdvisor)
                //rag 检索增强(Retrieval检索顾问)
                /*.advisors(
                        PlanAppRagCustomAdvisorFactory.createPlanAppRagCustomAdvisor(planAppVectorStore,"家人")
                )*/
                .advisors(MyLoggerAdvisor.builder().build())
                .call()
                .chatResponse();
        assert response != null;
        String content = response.getResult().getOutput().getText();
        log.info("[PlanApp] content: {}", content);
        return content;
    }

    /**
     * 工具调用对话
     * @param message
     * @param chatId
     * @return
     */
    @Resource
    private ToolRegistration toolRegistration;
    public String doChatWithTools(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(toolRegistration.allTools())
                .call()
                .chatResponse();
        assert response != null;
        String content = response.getResult().getOutput().getText();
        log.info("[PlanApp] content: {}", content);
        return content;
    }

    /**
     * MCP调用对话
     * @param message
     * @param chatId
     * @return
     */
    @Resource
    private ToolCallbackProvider mcpToolCallbackProvider;
    public String doChatWithMCP(String message, String chatId) {
        ChatResponse response = chatClient.prompt()
                .user(message)
                .advisors(spec -> spec.param(ChatMemory.CONVERSATION_ID, chatId))
                .toolCallbacks(mcpToolCallbackProvider)
                .call()
                .chatResponse();
        assert response != null;
        String content = response.getResult().getOutput().getText();
        log.info("[PlanApp] content: {}", content);
        return content;
    }
}