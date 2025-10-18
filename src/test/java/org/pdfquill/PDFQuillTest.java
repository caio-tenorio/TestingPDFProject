package org.pdfquill;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;
import org.pdfquill.exceptions.PrinterException;
import org.pdfquill.paper.PaperType;
import org.pdfquill.settings.font.FontSettings;
import org.pdfquill.settings.font.FontType;

import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PDFQuillTest {

    @Test
    void getBase64PDFBytesIsIdempotentAfterClose() throws Exception {
        PDFQuill quill = new PDFQuill();
        quill.printLine("Sample line");

        String first = quill.getBase64PDFBytes();
        String second = quill.getBase64PDFBytes();

        assertThat(second).isEqualTo(first);
    }

    @Test
    void builderRejectsNullPaperType() {
        PDFQuill.Builder builder = PDFQuill.builder();

        assertThatThrownBy(() -> builder.withPaperType(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paperType");
    }

    @Test
    void builderRejectsNegativeMargins() {
        PDFQuill.Builder builder = PDFQuill.builder();

        assertThatThrownBy(() -> builder.withMargins(-1f, 1f, 1f, 1f))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("marginLeft");
    }

    @Test
    void updateFontSettingsAppliesNewDefaults() throws Exception {
        PDFQuill quill = new PDFQuill();
        FontSettings settings = new FontSettings();
        settings.setFontSize(16);
        settings.setDefaultFont(settings.getFontByFontType(FontType.DEFAULT));

        quill.updateFontSettings(settings);
        quill.printLine("Custom font size");

        String base64 = quill.getBase64PDFBytes();
        byte[] bytes = Base64.getDecoder().decode(base64);
        try (PDDocument document = PDDocument.load(bytes)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }

    @Test
    void printLineWithCustomBuilderWritesPdf() throws IOException, PrinterException {
        FontSettings fontSettings = new FontSettings();
        fontSettings.setFontSize(10);

        PDFQuill quill = PDFQuill.builder()
                .withPaperType(PaperType.A5)
                .withFontSettings(fontSettings)
                .build();

        quill.printLine("Line one");
        quill.printLine("Line two", FontType.BOLD);

        String base64 = quill.getBase64PDFBytes();
        byte[] pdfBytes = Base64.getDecoder().decode(base64);

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            assertThat(document.getNumberOfPages()).isEqualTo(1);
        }
    }
}
