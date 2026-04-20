package org.example.zjzaiagent.tools;

import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * 工具注册类
 */
@Configuration
public class ToolRegistration {

    @Value("${search-api.api-key}")
    private String searchApiKey;
    @Value("${spring.mail.host}")
    private String SMTP_HOST;
    @Value("${spring.mail.port}")
    private String SMTP_PORT;
    @Value("${spring.mail.username}")
    private String FROM_EMAIL;
    @Value("${spring.mail.password}")
    private String AUTH_CODE;

    @Bean
    public ToolCallback[] allTools() {
        FileOperationTool fileOperationTool = new FileOperationTool();
        WebSearchTool webSearchTool = new WebSearchTool(searchApiKey);
        WebScrapingTool webScrapingTool = new WebScrapingTool();
        ResourceDownloadTool resourceDownloadTool = new ResourceDownloadTool();
        TerminalOperationTool terminalOperationTool = new TerminalOperationTool();
        PDFGenerationTool pdfGenerationTool = new PDFGenerationTool();
        QQEmailSendTool qqEmailSendTool = new QQEmailSendTool(FROM_EMAIL, AUTH_CODE,SMTP_HOST,SMTP_PORT);
        return MethodToolCallbackProvider.builder().toolObjects(
            fileOperationTool,
            webSearchTool,
            webScrapingTool,
            resourceDownloadTool,
            terminalOperationTool,
            pdfGenerationTool,
                qqEmailSendTool
        ).build().getToolCallbacks();
    }
}
