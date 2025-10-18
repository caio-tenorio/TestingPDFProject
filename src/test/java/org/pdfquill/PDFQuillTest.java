package org.pdfquill;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.Test;
import org.pdfquill.exceptions.PrinterException;
import org.pdfquill.paper.PaperType;
import org.pdfquill.settings.font.FontSettings;
import org.pdfquill.settings.font.FontType;
import org.pdfquill.settings.PageLayout;

import java.io.IOException;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

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

    @Test
    void skipLinesViaFacadeProducesBlankSpace() throws Exception {
        PDFQuill quill = new PDFQuill();

        quill.printLine("Alpha");
        quill.skipLines(2);
        quill.printLine("Omega");

        String base64 = quill.getBase64PDFBytes();
        byte[] pdfBytes = Base64.getDecoder().decode(base64);

        try (PDDocument document = PDDocument.load(pdfBytes)) {
            RecordingStripper stripper = new RecordingStripper();
            stripper.getText(document);
            assertThat(stripper.getYPositions()).hasSizeGreaterThanOrEqualTo(2);
            float alphaY = stripper.getYPositions().get(0);
            float omegaY = stripper.getYPositions().get(1);
            PageLayout expectedLayout = new PageLayout(PaperType.A4);
            assertThat(Math.abs(alphaY - omegaY)).isCloseTo(expectedLayout.getLineHeight() * 3, within(0.5f));
        }
    }

    private static final class RecordingStripper extends PDFTextStripper {
        private final java.util.List<Float> yPositions = new java.util.ArrayList<>();

        private RecordingStripper() throws IOException {
            super();
        }

        @Override
        protected void writeString(String text, java.util.List<org.apache.pdfbox.text.TextPosition> textPositions) throws IOException {
            if (!text.isBlank() && !textPositions.isEmpty()) {
                yPositions.add(textPositions.get(0).getY());
            }
            super.writeString(text, textPositions);
        }

        java.util.List<Float> getYPositions() {
            return yPositions;
        }
    }
}
