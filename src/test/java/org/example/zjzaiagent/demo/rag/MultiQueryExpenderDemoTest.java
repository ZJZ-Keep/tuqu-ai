package org.example.zjzaiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.preretrieval.query.expansion.MultiQueryExpander;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class MultiQueryExpenderDemoTest {

    @Resource
    private MultiQueryExpenderDemo multiQueryExpenderDemo;
    @Test
    void multiQueryExpander() {
        MultiQueryExpander expander = multiQueryExpenderDemo.multiQueryExpander();
        List<Query> queries = expander.expand(new Query("谁是郑少"));
        Assertions.assertNotNull( queries);
    }
}