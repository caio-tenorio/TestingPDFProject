package com.caio;

import com.caio.settings.page.PageLayout;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class DocumentManager {
    private final PDDocument document;
    private final ByteArrayOutputStream os;
    private final PDRectangle pageSize;
    private final PageLayout pageLayout;

    private PDPage currentPage;
    private PDPageContentStream contentStream;
    private int currentLine = 0;

    public DocumentManager(PageLayout pageLayout) {
        this.pageLayout = pageLayout;
        this.os = new ByteArrayOutputStream();
        this.document = new PDDocument();
        this.pageSize = new PDRectangle(pageLayout.getPageWidth(), pageLayout.getPageHeight());
        this.currentPage = null;
        this.contentStream = null;
    }

    public PDPageContentStream getContentStream() {
        return contentStream;
    }

    public PDDocument getDocument() {
        return document;
    }

    public boolean isClosed() {
        return this.document.getDocument().isClosed();
    }

    public PDPage getCurrentPage() {
        return currentPage;
    }

    public int getCurrentLine() {
        return currentLine;
    }

    public void incrementCurrentLine() {
        this.currentLine++;
    }

    public void incrementCurrentLine(int lines) {
        this.currentLine += lines;
    }

    public boolean addNewPageIfNeeded() throws IOException {
        if (document.getNumberOfPages() == 0 || currentLine >= pageLayout.getLinesPerPage()) {
            addNewPage();
            return true;
        }
        return false;
    }

    public boolean addNewPageIfNeeded(int finalLine) throws IOException {
        if (document.getNumberOfPages() == 0 || finalLine >= pageLayout.getLinesPerPage()) {
            addNewPage();
            return true;
        }
        return false;
    }

    public void addNewPage() throws IOException {
        if (this.contentStream != null) {
            this.contentStream.close();
        }
        this.currentPage = new PDPage(this.pageSize);
        this.document.addPage(this.currentPage);
        this.contentStream = new PDPageContentStream(this.document, this.currentPage);
        this.currentLine = 0;
    }

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

    public void close() throws IOException {
        if (this.contentStream != null) {
            this.contentStream.close();
        }
        if (!this.isClosed()) {
            this.document.close();
        }
    }
}
