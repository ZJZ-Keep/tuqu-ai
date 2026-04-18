package org.example.zjzaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * QQ邮箱邮件发送工具类
 */
@Slf4j
public class QQEmailSendTool {

    // 发件人邮箱地址
    private final String FROM_EMAIL;
    // 邮箱授权码
    private final String AUTH_CODE;
    // QQ邮箱SMTP配置
    private final String SMTP_HOST;
    // SMTP端口
    private final String SMTP_PORT;

    public QQEmailSendTool(String FROM_EMAIL, String AUTH_CODE,String SMTP_HOST, String SMTP_PORT){
        this.FROM_EMAIL = FROM_EMAIL;
        this.AUTH_CODE = AUTH_CODE;
        this.SMTP_HOST = SMTP_HOST;
        this.SMTP_PORT = SMTP_PORT;
    }
    /**
     * 发送纯文本邮件
     *
     * @param to      收件人邮箱地址
     * @param subject 邮件主题
     * @param content 邮件正文（纯文本）
     * @throws MessagingException 发送失败时抛出异常
     */
    @Tool(description = "Sends an text formatted email using QQ Mail's SMTP service. " +
            "Requires the recipient's email address, subject line, and HTML content. " +
            "The sender's QQ email address and SMTP authorization code must be pre-configured in the system.")
    public void sendTextEmail(
            @ToolParam(description = "Recipient's email address") String to,
            @ToolParam(description = "Email subject line") String subject,
            @ToolParam(description = "text content of the email") String content) throws MessagingException {

        // 创建SMTP配置
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true"); // 启用STARTTLS加密（推荐）

        // 创建Session
        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, AUTH_CODE);
            }
        });

        // 创建邮件
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject);
        message.setText(content);

        // 发送邮件
        Transport.send(message);
        log.info("✅ 文本邮件发送成功！收件人：{}", to);
    }

}

