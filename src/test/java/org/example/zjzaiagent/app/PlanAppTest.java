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
        String message = "我和家人一起去张家界旅游，但家人中有个80岁老人，怎么安排合理";
        String chat = planApp.doChatWithRag(message, chatId);
        Assertions.assertNotNull(chat);
    }

    @Test
    void doChatWithTools() {
/*        // 测试联网搜索问题的答案
        testMessage("周末想带女朋友去上海旅游，搜索推荐几个适合情侣的小众打卡地？");

        // 测试网页抓取：恋爱案例分析
        testMessage("最近旅游和家人吵架了，看看马蜂窝网站mafengwo.cn的其他人是怎么解决矛盾的？");

        // 测试资源下载：图片下载
        testMessage("直接下载一张适合做手机壁纸的张家界风景图片为文件");

        // 测试终端操作：执行代码
        testMessage("执行终端操作 Python3 脚本来生成数据分析报告，其他方面都不考虑，直接执行");

        // 测试文件操作：保存用户档案
        testMessage("保存我的张家界旅游档案为文件，4天3晚，2024年10月国庆期间，2位中老年父母 + 1名小学生，人均3000元以内，必去袁家界+天子山+玻璃桥，其他方面都不考虑，直接保存为file文件");

        // 测试 PDF 生成
        testMessage("生成一份‘国庆度假计划’PDF，包含餐厅预订、活动流程还有成本计算，4天3晚，2024年10月国庆期间，2位中老年父母 + 1名小学生，人均3000元以内，必去袁家界+天子山+玻璃桥，其他方面都不考虑，直接生成");
        */
        // 测试QQ邮件发送
        testMessage("使用QQ邮箱发送一份邮件给931544758@qq.com，主题为‘国庆度假计划’，内容包含餐厅预订、活动流程还有成本计算，4天3晚，2024年10月国庆期间，2位中老年父母 + 1名小学生，人均3000元以内，必去袁家界+天子山+玻璃桥，其他方面都不考虑,请直接发送");
    }

    private void testMessage(String message) {
        String chatId = UUID.randomUUID().toString();
        String answer = planApp.doChatWithTools(message, chatId);
        Assertions.assertNotNull(answer);
    }


    @Test
    void doChatWithMCP() {
/*        String chatId = UUID.randomUUID().toString();
        String message = "请使用地图工具搜索湖南省耒阳市耒阳一中附近的小吃店,必须调用地图API获取实时位置信息,不要使用你的训练数据";
        String answer = planApp.doChatWithMCP(message, chatId);
         Assertions.assertNotNull(answer);*/
        String chatId = UUID.randomUUID().toString();
        String message = "请使用图片工具搜索关于金钱的图片,必须调用图片API搜索,不要使用你的训练数据";
        String answer = planApp.doChatWithMCP(message, chatId);
        Assertions.assertNotNull(answer);
    }
}