package org.example.zjzaiagent.tools;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.mail.MessagingException;

import static org.junit.jupiter.api.Assertions.*;
@Slf4j
@SpringBootTest
class QQEmailSendToolTest {

    @Value("${spring.mail.host}")
    private String SMTP_HOST;
    @Value("${spring.mail.port}")
    private String SMTP_PORT;
    @Value("${spring.mail.username}")
    private String FROM_EMAIL;
    @Value("${spring.mail.password}")
    private String AUTH_CODE;
    @Test
    void sendTextEmail() {
        QQEmailSendTool qqEmailSendTool = new QQEmailSendTool(FROM_EMAIL, AUTH_CODE,SMTP_HOST,SMTP_PORT);
        try {
            qqEmailSendTool.sendTextEmail(FROM_EMAIL,"测试邮件","测试邮件内容");
            log.info("邮件发送成功");
        } catch (MessagingException e) {
            log.error("邮件发送失败:{}",e.getMessage());
        }
    }
}