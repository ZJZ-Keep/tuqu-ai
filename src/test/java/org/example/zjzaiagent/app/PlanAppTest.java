package org.example.zjzaiagent.app;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Base64;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class PlanAppTest {
    @Resource
    private PlanApp planApp;

    @Test
    void doChat() {
        //第一轮
        String chatId = UUID.randomUUID().toString();
        //String message = "我要去上海，7天";
        String message = "你是我做的一个ai智能体，现在我要存到github的Description里，给我生成一个你的介绍，以便让别人了解我做的你这个智能体";
        String chat = planApp.doChat(message, chatId);
        Assertions.assertNotNull(chat);
/*        //第二轮
        message = "上海的景点";
        chat = planApp.doChat(message, chatId);
        Assertions.assertNotNull(chat);
        //第三轮
        message = "我要去哪来着";
        chat = planApp.doChat(message, chatId);
        Assertions.assertNotNull(chat);*/
    }

    @Test
    void doChatWithReport() {
        String chatId = UUID.randomUUID().toString();
        String message = "我是郑少，我要和家人去张家界玩5天，但不知道哪里有啥值得玩的";
        PlanApp.Report report = planApp.doChatWithReport(message, chatId);
        Assertions.assertNotNull(report);
    }

    @Test
    void testBase64() {
        String message ="AQBvcmcuc3ByaW5nZnJhbWV3b3JrLmFpLmNoYXQubWVzc2FnZXMuQXNzaXN0YW50TWVzc2Fn5QEBamF2YS51dGlsLkltbXV0YWJsZUNvbGxlY3Rpb25zJExpc3TOAQIBAmphdmEudXRpbC5IYXNoTWHwB2ZpbmlzaFJlYXNv7gNTVE/Qc2VhcmNoX2luZu8DgXJvbOUDQVNTSVNUQU7UaeQDpTBlMjBmYjVjLWM5MDMtOTRmMC1hNzBjLTc0OWZlMDFiNWJmNW1lc3NhZ2VUeXDlAQNvcmcuc3ByaW5nZnJhbWV3b3JrLmFpLmNoYXQubWVzc2FnZXMuTWVzc2FnZVR5cOUCcmVhc29uaW5nQ29udGVu9AOBzQHmoLnmja7pnZLnpr7lj6TplYfmnIDmlrDlhoXpg6jov5DokKXpgJrnn6XvvIzku4Xmr4/lubQz5pyIMTjml6XjgIE35pyIM+aXpeOAgTEx5pyIOeaXpeS4ieWkqeaJp+ihjOWNiuelqOaUv+etlu+8jDXmnIgxMuaXpeS4jeWcqOatpOWIl++8jOmcgOaMieWFqOS7t+i0reelqOWFpeWbrTxyZWY+WzFdPC9yZWY+44CCAQEB";
        //1. 解码Base64字符串
        byte[] decodedBytes = Base64.getDecoder().decode(message);
        //2. 将字节数组转换为字符串
        String decodedString = new String(decodedBytes);
        System.out.println(decodedString);
    }

    @Test
    void doChatWithRag() {
        String chatId = UUID.randomUUID().toString();
        String message = "我一个人计划 5 月 12 日去青禾古镇，当天景区是否执行半票开放政策？";
        String chat = planApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(chat);
    }
}