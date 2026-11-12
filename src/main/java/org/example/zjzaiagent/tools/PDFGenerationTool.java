package org.example.zjzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import java.io.IOException;
/**
 * PDF 生成工具类
 */
@Slf4j
public class PDFGenerationTool {

    @Tool(description = "Generate a PDF file with given content. Note: Prefer using writeFile tool for .txt or .md files unless PDF is explicitly requested.")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF (must end with .pdf)") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {
        if (!fileName.endsWith(".pdf")) {
            fileName = fileName + ".pdf";
        }
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + fileName;
        try {
            // 创建目录
            FileUtil.mkdir(fileDir);
            // 创建 PdfWriter 和 PdfDocument 对象
            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                // 尝试多种中文字体方案
                PdfFont font = createChineseFont();
                document.setFont(font);
                // 按换行符分段处理内容
                String[] lines = content.split("\n");
                for (String line : lines) {
                    Paragraph paragraph = new Paragraph(line);
                    document.add(paragraph);
                }
            }
            log.info("PDF生成成功: {}", filePath);
            return "PDF generated successfully to: " + filePath;
        } catch (Exception e) {
            log.error("PDF生成失败，回退到生成文本文件: {}", e.getMessage());
            // 回退到生成文本文件
            return fallbackToTextFile(fileName.replace(".pdf", ".txt"), content);
        }
    }

    /**
     * 尝试创建中文字体，兼容多种iText版本
     */
    private PdfFont createChineseFont() throws IOException {
        // 方案1: iText 9.x 推荐方式（单参数）
        try {
            return PdfFontFactory.createFont("STSongStd-Light");
        } catch (Exception e) {
            log.debug("方案1字体加载失败: {}", e.getMessage());
        }
        // 方案2: 使用EmbeddingStrategy
        try {
            return PdfFontFactory.createFont("STSongStd-Light",
                    PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
        } catch (Exception e) {
            log.debug("方案2字体加载失败: {}", e.getMessage());
        }
        // 方案3: 使用系统字体
        try {
            return PdfFontFactory.createFont("C:\\Windows\\Fonts\\simsun.ttc,1",
                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
        } catch (Exception e) {
            log.debug("方案3字体加载失败: {}", e.getMessage());
        }
        // 方案4: 使用默认字体（不支持中文但至少不会崩溃）
        return PdfFontFactory.createFont();
    }

    /**
     * PDF生成失败时回退到生成文本文件
     */
    private String fallbackToTextFile(String fileName, String content) {
        String fileDir = FileConstant.FILE_SAVE_DIR + "/file";
        String filePath = fileDir + "/" + fileName;
        try {
            FileUtil.mkdir(fileDir);
            FileUtil.writeUtf8String(content, filePath);
            return "File written successfully to: " + filePath;
        } catch (Exception ex) {
            return "Error generating file: " + ex.getMessage();
        }
    }
}
