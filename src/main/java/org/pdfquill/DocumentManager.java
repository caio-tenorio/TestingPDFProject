package org.pdfquill;

import org.pdfquill.settings.page.PageLayout;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Coordinates the lifecycle of the underlying {@link PDDocument}, including page management,
 * content streams, and post-processing steps such as cropping thermal receipts.
 */
public class DocumentManager {
    private final PDDocument document;
    private final ByteArrayOutputStream os;
    private final PDRectangle pageSize;
    private final PageLayout pageLayout;

    private PDPage currentPage;
    private PDPageContentStream contentStream;
    private int currentLine = 0;

    /**
     * Creates a manager responsible for writing content according to the supplied layout.
     *
     * @param pageLayout layout describing page dimensions and metrics
     */
    public DocumentManager(PageLayout pageLayout) {
        this.pageLayout = pageLayout;
        this.os = new ByteArrayOutputStream();
        this.document = new PDDocument();
        this.pageSize = new PDRectangle(pageLayout.getPageWidth(), pageLayout.getPageHeight());
        this.currentPage = null;
        this.contentStream = null;
    }

    /**
     * @return active {@link PDPageContentStream} or {@code null} when none exists yet
     */
    public PDPageContentStream getContentStream() {
        return contentStream;
    }

    /**
     * @return underlying {@link PDDocument}
     */
    public PDDocument getDocument() {
        return document;
    }

    /**
     * @return {@code true} when the document has been closed already
     */
    public boolean isClosed() {
        return this.document.getDocument().isClosed();
    }

    /**
     * @return the page currently targeted by the content stream
     */
    public PDPage getCurrentPage() {
        return currentPage;
    }

    /**
     * @return current line index being written on the active page
     */
    public int getCurrentLine() {
        return currentLine;
    }

    /**
     * Advances the line counter by one.
     */
    public void incrementCurrentLine() {
        this.currentLine++;
    }

    /**
     * Advances the line counter by the provided amount.
     *
     * @param lines number of logical lines consumed
     */
    public void incrementCurrentLine(int lines) {
        this.currentLine += lines;
    }

    /**
     * Ensures there is a writable page and creates a new one when the current page is full.
     *
     * @return {@code true} when a new page was created
     * @throws IOException when PDFBox cannot initialise the page
     */
    public boolean addNewPageIfNeeded() throws IOException {
        if (document.getNumberOfPages() == 0 || currentLine >= pageLayout.getLinesPerPage()) {
            addNewPage();
            return true;
        }
        return false;
    }

    /**
     * Same as {@link #addNewPageIfNeeded()} but considers the number of upcoming lines.
     *
     * @param finalLine anticipated line index after the next operation
     * @return {@code true} when a new page was created
     * @throws IOException when PDFBox cannot initialise the page
     */
    public boolean addNewPageIfNeeded(int finalLine) throws IOException {
        if (document.getNumberOfPages() == 0 || finalLine >= pageLayout.getLinesPerPage()) {
            addNewPage();
            return true;
        }
        return false;
    }

    /**
     * Closes the active content stream (if any) and starts a new page.
     *
     * @throws IOException when PDFBox fails to create the page or stream
     */
    public void addNewPage() throws IOException {
        if (this.contentStream != null) {
            this.contentStream.close();
        }
        this.currentPage = new PDPage(this.pageSize);
        this.document.addPage(this.currentPage);
        this.contentStream = new PDPageContentStream(this.document, this.currentPage);
        this.currentLine = 0;
    }

    /**
     * Finalises the document, applying post-processing, and returns the resulting bytes.
     *
     * @return generated PDF bytes
     * @throws IOException when saving or cropping fails
     */
    public byte[] saveAndGetBytes() throws IOException {
        if (this.contentStream != null) {
            this.contentStream.close();
        }
        if (!this.isClosed()) {
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
                        - (lineHeight * currentLine) - lineHeight,
                        mediaBox.getUpperRightX() - 3, (lineHeight * currentLine) + lineHeight);

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

    /**
     * Closes streams and the underlying document if still open.
     *
     * @throws IOException when closing the document fails
     */
    public void close() throws IOException {
        if (this.contentStream != null) {
            this.contentStream.close();
        }
        if (!this.isClosed()) {
            this.document.close();
        }
    }
}
