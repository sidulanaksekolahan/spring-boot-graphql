import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

public class RenderPdfPreview {
    public static void main(String[] args) throws Exception {
        Path pdf = Path.of(args[0]);
        Path output = Path.of(args[1]);
        Files.createDirectories(output);
        try (PDDocument document = Loader.loadPDF(pdf.toFile())) {
            PDFRenderer renderer = new PDFRenderer(document);
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, 120);
                ImageIO.write(image, "png", output.resolve("page-" + (i + 1) + ".png").toFile());
            }
            System.out.println("pages=" + document.getNumberOfPages());
        }
    }
}
