package org.pdfquill;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.text.PDFTextStripper;
import org.pdfquill.settings.font.FontType;
import org.pdfquill.settings.page.PageLayout;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Manages the PDF document lifecycle, providing a cursor-like interface for writing content.
 * It handles page creation, content streams, and final document processing.
 */
public class PDFWriter {
    private final PDDocument document;
    private final ByteArrayOutputStream os;
    private final PDRectangle pageSize;
    private final PageLayout pageLayout;

    private PDPage currentPage;
    private PDPageContentStream contentStream;
    private float writtenHeight = 0;

    /**
     * Creates a writer responsible for generating a PDF according to the supplied layout.
     *
     * @param pageLayout layout describing page dimensions and metrics
     */
    public PDFWriter(PageLayout pageLayout) {
        this.pageLayout = pageLayout;
        this.os = new ByteArrayOutputStream();
        this.document = new PDDocument();
        this.pageSize = new PDRectangle(pageLayout.getPageWidth(), pageLayout.getPageHeight());
        this.currentPage = null;
        this.contentStream = null;
    }

    private void incrementWrittenHeight() {
        this.writtenHeight += this.pageLayout.getLineHeight();
    }

    private void incrementWrittenHeight(float height) {
        this.writtenHeight += height + this.pageLayout.getLineHeight();
    }

    private float getCurrentY() {
        return this.pageLayout.getStartY() - this.writtenHeight;
    }

    /**
     * Writes a single line of text to the document, automatically handling pagination.
     *
     * @param line The text line to be written.
     * @throws IOException if writing to the content stream fails.
     */
    public void writeLine(String line, FontType fontType) throws IOException {
        addNewPageIfNeeded();
        float lineY = getCurrentY();
        addTextLine(line, this.pageLayout.getStartX(), lineY, fontType);
        incrementWrittenHeight();
    }

    /**
     * Writes an image to the document, centering it and handling pagination.
     *
     * @param image       The image to write.
     * @param imageWidth  The desired width of the image in points.
     * @param imageHeight The desired height of the image in points.
     * @throws IOException if writing to the content stream fails.
     */
    public void writeImage(BufferedImage image, float imageWidth, float imageHeight) throws IOException {
        PDImageXObject pdImage = LosslessFactory.createFromImage(this.document, image);
        addNewPageIfNeeded(imageHeight);

        float lineY = (getCurrentY()) - imageHeight;
        float imageStartX = this.pageLayout.getStartX() + (this.pageLayout.getMaxLineWidth() - imageWidth) / 2;

        contentStream.drawImage(pdImage, imageStartX, lineY, imageWidth, imageHeight);
        incrementWrittenHeight(imageHeight);
    }

    public void writeImage(ByteArrayInputStream imgBytes, int width, int height) throws IOException {
        BufferedImage image = ImageIO.read(imgBytes);
        writeImage(image, width, height);
    }

    /**
     * Emits a visual indicator representing the cut mark typically used for receipts.
     *
     * @throws IOException when drawing the signal fails
     */
    public void writeCutSignal() throws IOException {
        float lineY = getCurrentY();

        contentStream.beginText();
        contentStream.setFont(this.pageLayout.getFontSettings().getDefaultFont(), this.pageLayout.getFontSettings().getFontSize());
        contentStream.newLineAtOffset(this.pageLayout.getStartX(), lineY - this.pageLayout.getLineHeight() * 2);
        contentStream.showText(createFullWidthString(" "));
        contentStream.endText();

        incrementWrittenHeight(this.pageLayout.getLineHeight() * 2);

        if (this.pageLayout.isNonThermalPaper()) {
            addNewPage();
        }
    }

    private String createFullWidthString(String text) throws IOException {
        float textWidth = this.pageLayout.getFontSettings().getDefaultFont().getStringWidth(text) * this.pageLayout.getFontSettings().getFontSize() / 1000f;
        int repetitions = (int) Math.ceil(this.pageLayout.getMaxLineWidth() / textWidth);
        StringBuilder sb = new StringBuilder(text.length() * repetitions);
        for (int i = 0; i < repetitions; i++) {
            sb.append(text);
        }
        return sb.toString();
    }

    private void addTextLine(String text, float x, float y, FontType fontType) throws IOException {
        try {
            contentStream.beginText();
            contentStream.setFont(this.pageLayout.getFontSettings().getFontByFontType(fontType),
                    this.pageLayout.getFontSettings().getFontSize());
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();
        } catch (IllegalArgumentException e) {
            contentStream.endText();
        }
    }

    private void addText(String text, float x, float y, FontType fontType) throws IOException {
        contentStream.setFont(this.pageLayout.getFontSettings().getFontByFontType(fontType),
                this.pageLayout.getFontSettings().getFontSize());
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
    }

    private void writeFromTextBuilder(TextBuilder textBuilder) throws IOException {
        float x = this.pageLayout.getStartX();
        float y = this.writtenHeight;
        if (textBuilder != null) {
            for (Text text : textBuilder.getTextList()) {
                // TODO: checar se texto vai exceder a largura de escrita da página, se sim, quebrar página e mover cursor
                // TODO: checar se texto vai exceder a altura de escrita da página, se sim, adicionar página nova e mover cursor
                getWid
            }
        }
    }

    private void beginText() throws IOException {
        this.contentStream.beginText();
    }

    private void endText() throws IOException {
        this.contentStream.endText();
    }

    private boolean addNewPageIfNeeded() throws IOException {
        if (document.getNumberOfPages() == 0 || willNewContentExceedPageWritingHeight(this.pageLayout.getLineHeight())) {
            addNewPage();
            return true;
        }
        return false;
    }

    private boolean addNewPageIfNeeded(float height) throws IOException {
        if (document.getNumberOfPages() == 0 || willNewContentExceedPageWritingHeight(height)) {
            addNewPage();
            return true;
        }
        return false;
    }

    private boolean willNewContentExceedPageWritingHeight(float height) {
        return this.writtenHeight + height > this.pageLayout.getPageWritingHeight();
    }

    private void addNewPage() throws IOException {
        if (this.contentStream != null) {
            this.contentStream.close();
        }
        this.currentPage = new PDPage(this.pageSize);
        this.document.addPage(this.currentPage);
        this.contentStream = new PDPageContentStream(this.document, this.currentPage);
        this.writtenHeight = 0;
    }

    public byte[] saveAndGetBytes() throws IOException {
        if (this.contentStream != null) {
            this.contentStream.close();
        }
        if (!isClosed()) {
            this.document.save(this.os);
            this.document.close();
            return cropPDF(this.os.toByteArray());
        }
        return this.os.toByteArray();
    }

    private byte[] cropPDF(byte[] bs) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bs);
        PDDocument document = PDDocument.load(in);
        PDPageTree pages = document.getDocumentCatalog().getPages();
        float lineHeight = this.pageLayout.getLineHeight();

        for (PDPage page : pages) {
            if (this.pageLayout.isThermalPaper()) {
                PDRectangle mediaBox = page.getMediaBox();
                PDRectangle cropBox = new PDRectangle(mediaBox.getLowerLeftX(), this.pageLayout.getPageHeight()
                        - this.writtenHeight - lineHeight,
                        mediaBox.getUpperRightX() - 3, this.writtenHeight + lineHeight);

                page.setCropBox(cropBox);
            } else {
                if (isPageBlank(document, page)) {
                    document.removePage(page);
                }
            }
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();

        return out.toByteArray();
    }

    private static boolean isPageBlank(PDDocument document, PDPage page) throws IOException {
        PDFTextStripper textStripper = new PDFTextStripper();
        textStripper.setStartPage(document.getPages().indexOf(page) + 1);
        textStripper.setEndPage(document.getPages().indexOf(page) + 1);

        String pageText = textStripper.getText(document).trim();

        return pageText.isEmpty();
    }

    public void close() throws IOException {
        if (this.contentStream != null) {
            this.contentStream.close();
        }
        if (!isClosed()) {
            this.document.close();
        }
    }

    public boolean isClosed() {
        return this.document.getDocument().isClosed();
    }
}