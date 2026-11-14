package org.example.zjzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Date;
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

    public QQEmailSendTool(String FROM_EMAIL, String AUTH_CODE, String SMTP_HOST, String SMTP_PORT) {
        this.FROM_EMAIL = FROM_EMAIL;
        this.AUTH_CODE = AUTH_CODE;
        this.SMTP_HOST = SMTP_HOST;
        this.SMTP_PORT = SMTP_PORT;
    }

    /**
     * 发送纯文本邮件
     *
     * @return 给智能体看的结果文本：成功说明已发送，失败说明原因（不要抛异常，避免任务整体中断）
     */
    @Tool(description = "Sends a plain text email using QQ Mail's SMTP service. "
            + "Returns a message starting with '邮件已发送' on success, or '邮件发送失败' with the reason. "
            + "The sender's QQ email address and SMTP authorization code must be pre-configured in the system.")
    public String sendTextEmail(
            @ToolParam(description = "Recipient's email address") String to,
            @ToolParam(description = "Email subject line") String subject,
            @ToolParam(description = "text content of the email") String content) {
        try {
            MimeMessage message = buildMessage(to, subject);
            message.setText(content == null ? "" : content, StandardCharsets.UTF_8.name());
            Transport.send(message);
            log.info("✅ 文本邮件发送成功！收件人：{}", to);
            return "邮件已发送：收件人 " + to + "，主题《" + subject + "》";
        } catch (Exception e) {
            log.error("❌ 邮件发送失败，收件人：{}，原因：{}", to, e.getMessage(), e);
            return "邮件发送失败：" + rootMessage(e) + "。请告知用户邮件未发送成功，并说明原因。";
        }
    }

    /**
     * 发送带附件的邮件（例如把生成的 PDF 作为附件发送）
     */
    @Tool(description = "Sends an email with one local file as attachment using QQ Mail's SMTP service. "
            + "attachmentPath must be the real path returned by a file generation tool (absolute, or relative to tmp/). "
            + "Returns '邮件已发送...' on success or '邮件发送失败...' with the reason.")
    public String sendEmailWithAttachment(
            @ToolParam(description = "Recipient's email address") String to,
            @ToolParam(description = "Email subject line") String subject,
            @ToolParam(description = "text content of the email") String content,
            @ToolParam(description = "Local path of the attachment, e.g. D:/xxx/tmp/pdf/plan.pdf") String attachmentPath) {
        try {
            File attachment = resolveAttachment(attachmentPath);
            if (attachment == null || !attachment.isFile()) {
                return "邮件发送失败：附件不存在（" + attachmentPath + "）。请先用工具生成文件，再使用工具返回的真实路径。";
            }
            if (attachment.length() == 0) {
                return "邮件发送失败：附件是空文件（" + attachmentPath + "），请重新生成后再发送。";
            }

            MimeMessage message = buildMessage(to, subject);

            MimeBodyPart textPart = new MimeBodyPart();
            textPart.setText(content == null ? "" : content, StandardCharsets.UTF_8.name());

            MimeBodyPart attachmentPart = new MimeBodyPart();
            attachmentPart.setDataHandler(new DataHandler(new FileDataSource(attachment)));
            attachmentPart.setFileName(MimeUtility.encodeText(attachment.getName(),
                    StandardCharsets.UTF_8.name(), "B"));

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(textPart);
            multipart.addBodyPart(attachmentPart);
            message.setContent(multipart);

            Transport.send(message);
            log.info("✅ 带附件邮件发送成功！收件人：{}，附件：{}", to, attachment.getAbsolutePath());
            return "邮件已发送：收件人 " + to + "，主题《" + subject + "》，附件 " + attachment.getName()
                    + "（" + FileUtil.readableFileSize(attachment.length()) + "）";
        } catch (Exception e) {
            log.error("❌ 带附件邮件发送失败，收件人：{}，原因：{}", to, e.getMessage(), e);
            return "邮件发送失败：" + rootMessage(e) + "。请告知用户邮件未发送成功，并说明原因。";
        }
    }

    private MimeMessage buildMessage(String to, String subject) throws MessagingException {
        Session session = Session.getInstance(smtpProperties(), new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, AUTH_CODE);
            }
        });
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_EMAIL));
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        message.setSubject(subject == null ? "AI 助手邮件" : subject, StandardCharsets.UTF_8.name());
        message.setSentDate(new Date());
        return message;
    }

    private Properties smtpProperties() {
        Properties props = new Properties();
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        // 兼容新版 JDK 的 TLS 握手
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        // 避免 SMTP 卡死导致整个任务无响应
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "30000");
        props.put("mail.smtp.writetimeout", "30000");
        return props;
    }

    /**
     * 支持绝对路径，也支持相对 tmp 目录 / 项目根目录的写法
     */
    private File resolveAttachment(String attachmentPath) {
        if (attachmentPath == null || attachmentPath.isBlank()) {
            return null;
        }
        String path = attachmentPath.strip().replace('\\', '/');
        File file = new File(path);
        if (file.isAbsolute() && file.isFile()) {
            return file;
        }
        file = new File(FileConstant.FILE_SAVE_DIR, path);
        if (file.isFile()) {
            return file;
        }
        file = new File(System.getProperty("user.dir"), path);
        return file.isFile() ? file : null;
    }

    private String rootMessage(Throwable e) {
        Throwable current = e;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return (message == null || message.isBlank()) ? current.getClass().getSimpleName() : message;
    }
}
