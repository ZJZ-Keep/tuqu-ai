package org.example.zjzaiagent.rag;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 *文档加载器
 * @Description:
 * @Author: zjz
 **/
@Component
@Slf4j
public class PlanAppDocumentLoader {

    private final ResourcePatternResolver planAppDocumentLoader;

    public PlanAppDocumentLoader(ResourcePatternResolver planAppDocumentLoader) {
        this.planAppDocumentLoader = planAppDocumentLoader;
    }

    // 加载文档
    public List<Document> load() {
        ArrayList<Document> documents = new ArrayList<>();
        try {
            // 获取所有md文件
            Resource[] resources = planAppDocumentLoader.getResources("classpath:documents/*.md");
            for (Resource resource : resources) {
                String filename = resource.getFilename();
                // 创建MarkdownDocumentReaderConfig
                MarkdownDocumentReaderConfig config = MarkdownDocumentReaderConfig.builder()
                        .withIncludeBlockquote(false)
                        .withIncludeCodeBlock(false)
                        .withAdditionalMetadata("filename", filename)
                        .build();
                // 创建MarkdownDocumentReader
                MarkdownDocumentReader reader = new MarkdownDocumentReader(resource,config);
                // 读取文档
                documents.addAll(reader.get());
            }
        } catch (IOException e) {
            log.error("加载文档失败", e);
        }
        return documents;
    }
}
