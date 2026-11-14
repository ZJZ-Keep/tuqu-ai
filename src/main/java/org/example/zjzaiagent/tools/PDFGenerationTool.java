package org.example.zjzaiagent.tools;

import cn.hutool.core.io.FileUtil;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import lombok.extern.slf4j.Slf4j;
import org.example.zjzaiagent.constant.FileConstant;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF 生成工具类
 *
 * <p>关键点：必须使用真正支持中文的字体，并在生成后校验 PDF 是否有内容。
 * 生成失败时返回明确的失败信息（而不是静默回退成 txt 后让模型误以为 PDF 已生成）。</p>
 */
@Slf4j
public class PDFGenerationTool {

    /** 常见中文字体路径，按优先级排列（支持 ttc 的 “路径,索引” 写法） */
    private static final String[] CJK_FONT_PATHS = {
            "C:/Windows/Fonts/simhei.ttf",
            "C:/Windows/Fonts/Deng.ttf",
            "C:/Windows/Fonts/simkai.ttf",
            "C:/Windows/Fonts/simfang.ttf",
            "C:/Windows/Fonts/simsun.ttc,1",
            "C:/Windows/Fonts/msyh.ttc,0",
            "/usr/share/fonts/truetype/wqy/wqy-zenhei.ttc,0",
            "/usr/share/fonts/opentype/noto/NotoSansCJK-Regular.ttc,0",
            "/usr/share/fonts/truetype/noto/NotoSansCJK-Regular.ttc,0",
            "/System/Library/Fonts/PingFang.ttc,0"
    };

    @Tool(description = "Generate a PDF file with given content. Returns a success message containing the real file path, "
            + "or a failure message starting with 'PDF 生成失败'. Always check the returned text: if it reports a failure, "
            + "tell the user the PDF was NOT generated. Prefer writeFile for .txt/.md unless PDF is explicitly requested.")
    public String generatePDF(
            @ToolParam(description = "Name of the file to save the generated PDF (must end with .pdf)") String fileName,
            @ToolParam(description = "Content to be included in the PDF") String content) {

        if (content == null || content.isBlank()) {
            return fail("内容为空，无法生成 PDF", null);
        }
        String safeFileName = sanitizeFileName(fileName);
        String fileDir = FileConstant.FILE_SAVE_DIR + "/pdf";
        String filePath = fileDir + "/" + safeFileName;

        try {
            FileUtil.mkdir(fileDir);
            // 先清掉同名旧文件，避免把上一次的残留误认为本次成功
            FileUtil.del(filePath);

            PdfFont font = resolveChineseFont();
            if (font == null) {
                return fail("服务器没有可用的中文字体，无法排版中文 PDF", null);
            }

            try (PdfWriter writer = new PdfWriter(filePath);
                 PdfDocument pdf = new PdfDocument(writer);
                 Document document = new Document(pdf)) {
                pdf.getDocumentInfo().setTitle(safeFileName.replaceFirst("\\.pdf$", ""));
                pdf.getDocumentInfo().setCreator("ZJZ AI Agent");
                document.setMargins(56f, 52f, 56f, 52f);
                document.setFont(font);
                document.setFontSize(10.5f);

                for (String rawLine : content.split("\\R")) {
                    String line = rawLine.stripTrailing();
                    if (line.isBlank()) {
                        document.add(new Paragraph(" ").setFont(font).setFontSize(6));
                        continue;
                    }
                    boolean heading = isHeading(line);
                    Paragraph paragraph = new Paragraph(line)
                            .setFont(font)
                            .setFontSize(heading ? 13f : 10.5f)
                            .setMultipliedLeading(1.55f)
                            .setMarginBottom(heading ? 6f : 2f);
                    if (heading) {
                        paragraph.setTextAlignment(TextAlignment.LEFT);
                    }
                    document.add(paragraph);
                }
            }

            verifyPdf(filePath);
            long size = new File(filePath).length();
            log.info("PDF 生成成功: {}（{} 字节）", filePath, size);
            return "PDF 已生成：" + filePath + "（" + FileUtil.readableFileSize(size) + "）";
        } catch (Exception e) {
            // 失败时删除残缺文件，避免前端/用户下载到 0 页的空 PDF
            FileUtil.del(filePath);
            log.error("PDF 生成失败：{}", e.getMessage(), e);
            return fail(e.getMessage(), filePath);
        }
    }

    private String fail(String reason, String filePath) {
        String cleanReason = reason == null || reason.isBlank() ? "未知错误" : reason;
        log.warn("PDF 未生成（{}），目标路径：{}", cleanReason, filePath);
        return "PDF 生成失败：" + cleanReason + "。未生成 PDF 文件，请如实告知用户 PDF 生成失败并说明原因，不要声称已生成 PDF。";
    }

    private String sanitizeFileName(String fileName) {
        String name = fileName == null ? "document" : fileName.strip();
        name = name.replaceAll("[\\\\/:*?\"<>|]", "_");
        if (name.isBlank()) {
            name = "document";
        }
        if (!name.toLowerCase().endsWith(".pdf")) {
            name = name + ".pdf";
        }
        return name;
    }

    /**
     * 解析可用的中文字体：优先嵌入系统 TTF，其次使用 iText 自带的 CJK 字体。
     */
    private PdfFont resolveChineseFont() {
        List<String> failures = new ArrayList<>();
        for (String path : CJK_FONT_PATHS) {
            if (!fontFileExists(path)) {
                continue;
            }
            try {
                PdfFont font = PdfFontFactory.createFont(path, PdfEncodings.IDENTITY_H,
                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                if (supportsChinese(font)) {
                    log.info("使用中文字体：{}", path);
                    return font;
                }
                failures.add(path + " 缺少中文字形");
            } catch (Exception e) {
                failures.add(path + "：" + e.getMessage());
            }
        }
        // iText font-asian 自带的 CJK 字体（不依赖系统字体）
        try {
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H",
                    PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);
            if (supportsChinese(font)) {
                log.info("使用 iText 内置中文字体：STSong-Light");
                return font;
            }
            failures.add("STSong-Light 缺少中文字形");
        } catch (Exception e) {
            failures.add("STSong-Light：" + e.getMessage());
        }
        log.error("没有找到可用的中文字体，尝试记录：{}", failures);
        return null;
    }

    private boolean fontFileExists(String path) {
        String realPath = path;
        int idx = realPath.lastIndexOf(',');
        if (idx > 0 && realPath.substring(idx + 1).matches("\\d+")) {
            realPath = realPath.substring(0, idx);
        }
        return new File(realPath).isFile();
    }

    private boolean supportsChinese(PdfFont font) {
        try {
            return font != null && font.containsGlyph('埃') && font.containsGlyph('及');
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 校验 PDF 确实有页面、有文字，避免生成“空壳 PDF”
     */
    private void verifyPdf(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("PDF 文件未创建");
        }
        if (file.length() < 1024) {
            throw new IOException("PDF 文件异常（仅 " + file.length() + " 字节）");
        }
        byte[] header = new byte[5];
        try (FileInputStream in = new FileInputStream(file)) {
            if (in.read(header) != header.length
                    || !"%PDF-".equals(new String(header, StandardCharsets.ISO_8859_1))) {
                throw new IOException("生成的不是有效的 PDF 文件");
            }
        }
        try (PdfDocument pdf = new PdfDocument(new PdfReader(filePath))) {
            int pages = pdf.getNumberOfPages();
            if (pages <= 0) {
                throw new IOException("PDF 没有任何页面");
            }
            boolean hasText = false;
            for (int i = 1; i <= Math.min(pages, 3); i++) {
                String text = PdfTextExtractor.getTextFromPage(pdf.getPage(i));
                if (text != null && !text.replaceAll("\\s", "").isEmpty()) {
                    hasText = true;
                    break;
                }
            }
            if (!hasText) {
                throw new IOException("PDF 页面中没有可读文本（字体或排版异常）");
            }
        }
    }

    private boolean isHeading(String line) {
        String trimmed = line.strip();
        if (trimmed.isEmpty() || trimmed.length() > 40) {
            return false;
        }
        return trimmed.matches("^[一二三四五六七八九十]+、.*")
                || trimmed.matches("^第[一二三四五六七八九十\\d]+[天章节部分].*")
                || trimmed.matches("^D\\d+.*")
                || trimmed.startsWith("【")
                || trimmed.matches("^[=—\\-]{6,}$");
    }
}
