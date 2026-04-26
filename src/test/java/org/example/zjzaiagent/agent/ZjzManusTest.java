package org.example.zjzaiagent.agent;

import jakarta.annotation.Resource;
import org.example.zjzaiagent.agent.ZjzManus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
class ZjzManusTest {
    @Resource
    private ZjzManus zjzManus;
    @Test
    void run() {
        String userPrompt = "我想要去黄山，请帮我找到五公里内的合适的饭店，" +
                "并结合网上的图片，制定一份详细的旅游计划，" +
                "并已PDF格式输出。";
        String result = zjzManus.run(userPrompt);
        Assertions.assertNotNull( result);
    }

}