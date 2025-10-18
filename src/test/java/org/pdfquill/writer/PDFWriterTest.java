package org.pdfquill.writer;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.pdfquill.paper.PaperType;
import org.pdfquill.settings.PageLayout;
import org.pdfquill.settings.font.FontType;

import static org.assertj.core.api.Assertions.assertThat;

class PDFWriterTest {

    @Test
    void writeLineAddsNewPageWhenContentExceedsAvailableHeight() throws Exception {
        PageLayout layout = new PageLayout(PaperType.A4);
        PDFWriter writer = new PDFWriter(layout);

        int linesPerPage = (int) Math.floor(layout.getPageWritingHeight() / layout.getLineHeight());
        assertThat(linesPerPage).isGreaterThan(0);

        int totalLines = linesPerPage + 5;

        for (int i = 0; i < totalLines; i++) {
            writer.writeLine("Line " + i, FontType.DEFAULT);
        }

        byte[] pdfBytes = writer.saveAndGetBytes();

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            assertThat(document.getNumberOfPages()).isEqualTo(2);
        }
    }
}
