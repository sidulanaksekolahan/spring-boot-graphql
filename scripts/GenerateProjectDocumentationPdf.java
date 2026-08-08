import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/** Generates the PDF version of docs/PROJECT_DOCUMENTATION.md. */
public class GenerateProjectDocumentationPdf {
    private static final float LEFT = 52, RIGHT = 52, TOP = 790, BOTTOM = 52;
    private static final PDFont REGULAR = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
    private static final PDFont BOLD = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
    private static final PDFont CODE = new PDType1Font(Standard14Fonts.FontName.COURIER);

    private PDDocument document;
    private PDPageContentStream stream;
    private float y;
    private int pageNumber;

    private void newPage() throws IOException {
        if (stream != null) stream.close();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        stream = new PDPageContentStream(document, page);
        pageNumber++;
        y = TOP;
        stream.setStrokingColor(188 / 255f, 204 / 255f, 220 / 255f);
        stream.moveTo(LEFT, 36); stream.lineTo(page.getMediaBox().getWidth() - RIGHT, 36); stream.stroke();
        text("Spring Boot GraphQL - Dokumentasi Project", LEFT, 23, REGULAR, 8, 72, 0x486581);
        text("Halaman " + pageNumber, page.getMediaBox().getWidth() - RIGHT - 45, 23, REGULAR, 8, 72, 0x486581);
    }

    private void text(String value, float x, float baseline, PDFont font, float size, int r, int color) throws IOException {
        stream.beginText(); stream.setFont(font, size);
        stream.setNonStrokingColor(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f);
        stream.newLineAtOffset(x, baseline); stream.showText(value); stream.endText();
    }

    private void line(String value, PDFont font, float size, float leading, int color) throws IOException {
        if (y - leading < BOTTOM) newPage();
        text(value, LEFT, y, font, size, 72, color);
        y -= leading;
    }

    private List<String> wrap(String raw, PDFont font, float size) throws IOException {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        float max = PDRectangle.A4.getWidth() - LEFT - RIGHT;
        for (String word : raw.split("\\s+")) {
            String candidate = current.length() == 0 ? word : current + " " + word;
            if (font.getStringWidth(candidate) / 1000 * size > max && current.length() > 0) {
                result.add(current.toString());
                current.setLength(0);
                current.append(word);
            } else {
                current.setLength(0);
                current.append(candidate);
            }
        }
        if (current.length() > 0) result.add(current.toString());
        return result;
    }

    private void paragraph(String raw, PDFont font, float size, float leading, int color) throws IOException {
        for (String row : wrap(raw, font, size)) line(row, font, size, leading, color);
        y -= 3;
    }

    private List<String> codeLines(String raw) {
        List<String> result = new ArrayList<>();
        String remaining = raw;
        while (remaining.length() > 110) {
            int split = remaining.lastIndexOf(' ', 110);
            if (split < 20) split = 110;
            result.add(remaining.substring(0, split));
            remaining = "  " + remaining.substring(split).trim();
        }
        result.add(remaining);
        return result;
    }

    private List<String> cells(String row) {
        String content = row.trim();
        if (content.startsWith("|")) content = content.substring(1);
        if (content.endsWith("|")) content = content.substring(0, content.length() - 1);
        List<String> values = new ArrayList<>();
        for (String value : content.split("\\|", -1)) values.add(value.trim().replace("`", ""));
        return values;
    }

    private void drawTable(List<String> markdownRows) throws IOException {
        if (markdownRows.isEmpty()) return;
        List<List<String>> rows = new ArrayList<>();
        int columns = 0;
        for (String markdownRow : markdownRows) {
            List<String> row = cells(markdownRow);
            rows.add(row); columns = Math.max(columns, row.size());
        }
        // Keep the short tables in this project together whenever possible.
        if (rows.size() <= 8 && y - rows.size() * 28 < BOTTOM) newPage();
        float width = (PDRectangle.A4.getWidth() - LEFT - RIGHT) / columns;
        for (int index = 0; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            List<List<String>> wrapped = new ArrayList<>();
            int maxLines = 1;
            for (int column = 0; column < columns; column++) {
                String value = column < row.size() ? row.get(column) : "";
                List<String> cellLines = wrap(value, index == 0 ? BOLD : REGULAR, 7.4f);
                if (cellLines.isEmpty()) cellLines.add("");
                wrapped.add(cellLines); maxLines = Math.max(maxLines, cellLines.size());
            }
            float height = maxLines * 9.2f + 8;
            if (y - height < BOTTOM) newPage();
            for (int column = 0; column < columns; column++) {
                float x = LEFT + column * width;
                if (index == 0) {
                    stream.setNonStrokingColor(18 / 255f, 53 / 255f, 91 / 255f);
                    stream.addRect(x, y - height, width, height);
                    stream.fill();
                }
                stream.setStrokingColor(188 / 255f, 204 / 255f, 220 / 255f);
                stream.addRect(x, y - height, width, height);
                stream.stroke();
                float baseline = y - 10;
                for (String cellLine : wrapped.get(column)) {
                    text(cellLine, x + 4, baseline, index == 0 ? BOLD : REGULAR, 7.4f, 72,
                            index == 0 ? 0xFFFFFF : 0x243B53);
                    baseline -= 9.2f;
                }
            }
            y -= height;
        }
        y -= 7;
    }

    private void render(Path markdown) throws Exception {
        boolean codeBlock = false;
        List<String> tableRows = new ArrayList<>();
        for (String original : Files.readAllLines(markdown, StandardCharsets.UTF_8)) {
            String s = original.trim();
            if (!codeBlock && s.matches("\\|?\\s*[-: ]+\\|[-|: ]*")) continue;
            if (!codeBlock && s.startsWith("|")) { tableRows.add(s); continue; }
            if (!tableRows.isEmpty()) { drawTable(tableRows); tableRows.clear(); }
            if (s.startsWith("```")) { codeBlock = !codeBlock; y -= 2; continue; }
            if (codeBlock) {
                for (String codeLine : codeLines(original.length() == 0 ? " " : original)) line(codeLine, CODE, 7.5f, 9.5f, 0x243B53);
                continue;
            }
            if (s.isEmpty()) { y -= 3; continue; }
            if (s.startsWith("# ")) { y -= 8; paragraph(s.substring(2), BOLD, 21, 25, 0x12355B); continue; }
            if (s.startsWith("## ")) { y -= 8; paragraph(s.substring(3), BOLD, 15, 19, 0x12355B); continue; }
            if (s.startsWith("### ")) { y -= 5; paragraph(s.substring(4), BOLD, 11.5f, 15, 0x1D4E89); continue; }
            if (s.startsWith("#### ")) { y -= 3; paragraph(s.substring(5), BOLD, 10.3f, 13.5f, 0x1D4E89); continue; }
            s = s.replace("**", "").replace("`", "");
            if (s.startsWith("- ")) s = "- " + s.substring(2);
            paragraph(s, REGULAR, 9.2f, 13, 0x243B53);
        }
        if (!tableRows.isEmpty()) drawTable(tableRows);
    }

    public static void main(String[] args) throws Exception {
        Path root = Path.of(args.length > 0 ? args[0] : ".").toAbsolutePath();
        Path output = root.resolve("output/pdf/dokumentasi-project-spring-boot-graphql.pdf");
        Files.createDirectories(output.getParent());
        GenerateProjectDocumentationPdf generator = new GenerateProjectDocumentationPdf();
        generator.document = new PDDocument();
        generator.newPage();
        generator.render(root.resolve("docs/PROJECT_DOCUMENTATION.md"));
        generator.stream.close();
        generator.document.save(output.toFile());
        generator.document.close();
        System.out.println(output);
    }
}
