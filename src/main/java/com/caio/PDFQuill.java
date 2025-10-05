package com.caio;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

import com.caio.measurements.MeasurementUtils;
import com.caio.settings.font.FontSettings;
import com.caio.settings.page.PageLayout;
import com.caio.settings.permissions.PermissionSettings;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.PDPageTree;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.text.PDFTextStripper;
import com.caio.barcode.BarcodeType;
import com.caio.barcode.BarcodeUtils;
import com.caio.paper.PaperType;


public class PDFQuill {
    // Configurable attrs
    private final PageLayout pageLayout;
    private final PermissionSettings permissionSettings;
    private byte[] pdf;
    private boolean preserveSpaces = false;


    // PDF Box Classes // Internal attrs
    private final ByteArrayOutputStream os;
    private final PDRectangle pageSize;
    private final PDDocument document;
    private PDPageContentStream contentStream;
    private PDPage currentPage;
    private int currentLine;

    public PDFQuill() {
        this(new Builder());
    }

    private PDFQuill(Builder builder) {
        this.preserveSpaces = builder.preserveSpaces;
        PermissionSettings basePermissionSettings = builder.permissionSettings != null ? builder.permissionSettings : new PermissionSettings();
        this.permissionSettings = copyPermissionSettings(basePermissionSettings);
        this.os = new ByteArrayOutputStream();
        this.document = new PDDocument();
        PageLayout layout = builder.pageLayout != null ? builder.pageLayout : createDefaultPageLayout(builder.paperType);

        if (builder.pageLayout != null && builder.paperType != null) {
            layout.setPaperType(builder.paperType);
        }

        if (builder.fontSettings != null) {
            layout.setFontSettings(copyFontSettings(builder.fontSettings));
        }
        if (builder.fontSettingsCustomizer != null) {
            builder.fontSettingsCustomizer.accept(layout.getFontSettings());
            layout.recalculate();
        }

        this.pageLayout = layout;
        this.pageSize = new PDRectangle(this.pageLayout.getPageWidth(), this.pageLayout.getPageHeight());

        if (builder.permissionSettingsCustomizer != null) {
            builder.permissionSettingsCustomizer.accept(this.permissionSettings);
        }

        this.currentLine = 0;
        this.contentStream = null;
        this.currentPage = null;
    }

    public static Builder builder() {
        return new Builder();
    }

    private static PageLayout createDefaultPageLayout(PaperType paperType) {
        PaperType resolvedPaperType = paperType != null ? paperType : PaperType.A4;
        return new PageLayout(resolvedPaperType);
    }

    /**
     *
     * @return
     */
    public String getBase64PDFBytes() throws PrinterException {
        try {
            if (this.contentStream != null) {
                this.contentStream.close();
            }

            if (!this.document.getDocument().isClosed()) {
                this.document.save(this.os);
                this.document.close();
                this.pdf = cropPDF(this.os.toByteArray());
            }
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar PDF", e);
        }
        return DatatypeConverter.printBase64Binary(this.pdf);
    }

    public void close() throws Exception {
        this.getBase64PDFBytes();
    }

    public void print(String text) throws PrinterException {
        try {
//            List<String> lines = splitLine(text);
            List<String> lines = wordWrapping(text);
            for (String line : lines) {
                printLine(line);
            }
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
    }

    private void printLine(String line) throws IOException {
        addNewPageIfNeeded();
        float lineY = this.pageLayout.getStartY() - (this.currentLine % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight();
        addTextLine(this.contentStream, line, this.pageLayout.getStartX(), lineY);
        this.currentLine++;
    }

//    private List<String> getLines(String text) throws IOException {
//        List<String> lines = new ArrayList<>();
//
//        var isLargerThanMax = true;
//        var currentText = text;
//        while (isLargerThanMax) {
//            if (getTextWidth(currentText) > this.pageLayout.getMaxLineWidth()) {
//                int cutIdx =
//                lines.add(currentText.substring(0, cutIdx));
//                currentText = currentText.substring(cutIdx);
//            } else {
//                isLargerThanMax = false;
//            }
//        }
//    }

    private float getTextWidth(String text) throws IOException {
        return this.pageLayout.getFontSettings().getDefaultFont().getStringWidth(text) * this.pageLayout.getFontSettings().getFontSize() / 1000f;
    }

    private List<String> wordWrapping(String text) throws IOException {
        List<String> lines = new ArrayList<>();

        if (getTextWidth(text) <= this.pageLayout.getMaxLineWidth()) {
            lines.add(text);
            return lines;
        }

        final int n = text.length();
        float[] widths = new float[n];
        for (int i = 0; i < n; i++) {
            widths[i] = getTextWidth(text.substring(i, i + 1));
        }

        float[] prefix = new float[n + 1];
        for (int i = 1; i <= n; i++) {
            prefix[i] = prefix[i - 1] + widths[i - 1];
        }

        int start = 0;

        while (start < n) {
            if (!this.preserveSpaces)
                while (start < n && Character.isWhitespace(text.charAt(start))) start++;

            if (start >= n) break;

            int lo = start + 1, hi = n, best = start + 1;
            while (lo <= hi) {
                int mid = (lo + hi) >>> 1;
                float w = prefix[mid] - prefix[start];
                if (w <= this.pageLayout.getMaxLineWidth()) {
                    best = mid;
                    lo = mid + 1;
                } else {
                    hi = mid - 1;
                }
            }

            int end = best;

            int breakIdx = end;
            if (end < n && !Character.isWhitespace(text.charAt(end - 1)) && !Character.isWhitespace(text.charAt(end))) {
                int lastSpace = lastWhitespaceBetween(text, start, end - 1);
                if (lastSpace >= start + 1) {
                    breakIdx = lastSpace;
                }
            }

            if (breakIdx == start) breakIdx = end;

            lines.add(text.substring(start, breakIdx).stripTrailing());

            // próximo início: pula o(s) espaço(s) depois do break
            start = breakIdx;
            while (start < n && Character.isWhitespace(text.charAt(start))) start++;
        }

        return lines;
    }

    private static int lastWhitespaceBetween(String s, int from, int toInclusive) {
        for (int i = toInclusive; i >= from; i--) {
            if (Character.isWhitespace(s.charAt(i))) return i;
        }
        return -1;
    }

    private boolean addNewPageIfNeeded() throws IOException {
        if (this.document.getNumberOfPages() == 0 || this.currentLine >= this.pageLayout.getLinesPerPage()) {
            addNewPage();
            return true;
        }
        return false;
    }

    private boolean addNewPageIfNeeded(int finalLine) throws IOException {
        if (this.document.getNumberOfPages() == 0 || finalLine >= this.pageLayout.getLinesPerPage()) {
            addNewPage();
            return true;
        }
        return false;
    }

    private void addNewPage() throws IOException {
        if (this.contentStream != null) {
            this.contentStream.close();
        }
        this.currentPage = new PDPage(this.pageSize);
        this.document.addPage(this.currentPage);
        this.contentStream = new PDPageContentStream(this.document, this.currentPage);
        this.currentLine = 0;
    }

    private List<String> splitLine(String text) {
        text = text.replace("\r\n", "\n").replace("\r", "\n");
        return Arrays.asList(text.split("\n"));
    }

    private void addTextLine(PDPageContentStream contentStream, String text, float x, float y) throws IOException {
        try {
            contentStream.beginText();
            contentStream.setFont(this.pageLayout.getFontSettings().getDefaultFont(), this.pageLayout.getFontSettings().getFontSize());
            contentStream.newLineAtOffset(x, y);
            contentStream.showText(text);
            contentStream.endText();
        } catch (IllegalArgumentException e) {
            contentStream.endText();
        }
    }

    /**
     *
     */
    public PDFQuill printImage(ByteArrayInputStream imgBytes) {
        try {
            if (this.currentPage == null || this.contentStream == null) {
                this.currentPage = new PDPage(this.pageSize);
                this.document.addPage(this.currentPage);
                this.contentStream = new PDPageContentStream(this.document, this.currentPage);
            }

            BufferedImage image = ImageIO.read(imgBytes);
            PDImageXObject pdImage = LosslessFactory.createFromImage(this.document, image);
            float imageHeight = MeasurementUtils.mmToPt(7);

            //Calcula coordenada y
            float lineY = (this.pageLayout.getStartY()- (this.currentLine % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight()) - imageHeight;
            // Quantidade de linhas ocupadas pelo codigo de barra arredonado para cima
            int imageLines = (int) Math.ceil(imageHeight / this.pageLayout.getLineHeight());
            int finalLine = this.currentLine + imageLines + 1;

            if (addNewPageIfNeeded(finalLine)) {
                //Recalcula coordenada y caso uma nova página seja adicionada
                lineY = (this.pageLayout.getStartY()- (this.currentLine % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight()) - imageHeight;
            }

            // Centraliza imagem
            float barcodeStartX = (this.pageLayout.getMaxLineWidth() - MeasurementUtils.mmToPt(98)) / 2;

            this.contentStream.drawImage(pdImage, barcodeStartX, lineY, MeasurementUtils.mmToPt(98), imageHeight);

            // Atualizando linha atual baseado na quantidade de linhas ocupadas pela imagem
            this.currentLine += imageLines + 1;
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    public PDFQuill printBarcode(String code, BarcodeType barcodeType) throws PrinterException {
        return this.printBarcode(code, barcodeType, 0, 0);
    }

    /**
     *
     */
    public PDFQuill printBarcode(String code, BarcodeType barcodeType, int height, int width) throws PrinterException {
        try {

            if (height == 0) {
                height = 350;
            }

            if (width == 0) {
                width = 350;
            }

            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(EncodeHintType.MARGIN, 0);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            MultiFormatWriter writer = new MultiFormatWriter();
            BarcodeFormat barcodeFormat = BarcodeUtils.getBarcodeFormat(barcodeType);
            BitMatrix byteMatrix = writer.encode(code, barcodeFormat, width, height, hintMap);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            createGraphics(image, byteMatrix, width, height);

            PDImageXObject pdImage = LosslessFactory.createFromImage(this.document, image);

            float imageHeight = BarcodeUtils.isQrCode(barcodeType) ? MeasurementUtils.mmToPt(48f) : MeasurementUtils.mmToPt(12f);
            float imageWidth = BarcodeUtils.isQrCode(barcodeType) ? MeasurementUtils.mmToPt(48f) : MeasurementUtils.mmToPt(80f);
            drawImage(pdImage, imageHeight, imageWidth);
        } catch (Throwable e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    private void createGraphics(BufferedImage image, BitMatrix byteMatrix, int width, int height) {
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, width, height);
        graphics.setColor(Color.BLACK);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (byteMatrix.get(i, j)) {
                    graphics.fillRect(i, j, 1, 1);
                }
            }
        }
        graphics.dispose();
    }

    private void drawImage(PDImageXObject pdImage, float imageHeight, float imageWidth) throws IOException {
        int barcodeLines = (int) Math.ceil(imageHeight / this.pageLayout.getLineHeight());
        // Centralize barcode
        float barcodeStartX = (this.pageLayout.getMaxLineWidth() - imageWidth) / 2;

        // Calculate coordinate y
        float lineY = (this.pageLayout.getStartY()- (this.currentLine % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight()) - imageHeight;

        int finalLine = this.currentLine + barcodeLines + 1;

        if (addNewPageIfNeeded(finalLine)) {
            // Recalculates coordinate y if a new page was added
            lineY = (this.pageLayout.getStartY()- (this.currentLine % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight()) - imageHeight;
        }

        contentStream.drawImage(pdImage, barcodeStartX, lineY, imageWidth, imageHeight);
        // Updates current line
        this.currentLine += barcodeLines + 1;
    }

    /**
     *
     */
    public PDFQuill cutSignal() throws PrinterException {
        try {
            float lineY = this.pageLayout.getStartY()- (this.currentLine % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight();

            contentStream.beginText();
            contentStream.setFont(this.pageLayout.getFontSettings().getDefaultFont(), this.pageLayout.getFontSettings().getFontSize());
            contentStream.newLineAtOffset(this.pageLayout.getStartX(),lineY - this.pageLayout.getLineHeight() * 2);
            contentStream.showText(createFullWidthString(" ", this.pageLayout.getMaxLineWidth()));
            contentStream.endText();

            currentLine = currentLine + 2;

            if (this.pageLayout.isNonThermalPaper()) {
                addNewPage();
            }
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    /**
     * Cuts PDF to correct size
     *
     * @param bs dados do PDF
     * @throws IOException
     */
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



    public String createFullWidthString(String text, float maxLineWidth) {
        float textWidth = 0;
        try {
            textWidth = getTextWidth(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int repetitions = (int) Math.ceil(maxLineWidth / textWidth);
        return repeatString(text, repetitions);
    }

    private String repeatString(String text, int repetitions) {
        StringBuilder sb = new StringBuilder(text.length() * repetitions);
        for (int i = 0; i < repetitions; i++) {
            sb.append(text);
        }
        return sb.toString();
    }

    private static PermissionSettings copyPermissionSettings(PermissionSettings source) {
        PermissionSettings copy = new PermissionSettings();
        copy.setCanPrint(source.isCanPrint());
        copy.setCanModify(source.isCanModify());
        copy.setCanExtractContent(source.isCanExtractContent());
        return copy;
    }

    private static FontSettings copyFontSettings(FontSettings source) {
        FontSettings copy = new FontSettings();
        copy.setFontSize(source.getFontSize());
        copy.setDefaultFont(source.getDefaultFont());
        copy.setBoldFont(source.getBoldFont());
        copy.setItalicFont(source.getItalicFont());
        copy.setBoldItalicFont(source.getBoldItalicFont());
        return copy;
    }

    public static final class Builder {
        private PaperType paperType;
        private boolean preserveSpaces;
        private PermissionSettings permissionSettings;
        private Consumer<PermissionSettings> permissionSettingsCustomizer;
        private PageLayout pageLayout;
        private FontSettings fontSettings;
        private Consumer<FontSettings> fontSettingsCustomizer;

        public Builder withPaperType(PaperType paperType) {
            if (paperType == null) {
                throw new IllegalArgumentException("paperType cannot be null");
            }
            this.paperType = paperType;
            return this;
        }

        public Builder preserveSpaces(boolean preserveSpaces) {
            this.preserveSpaces = preserveSpaces;
            return this;
        }

        public Builder withPermissionSettings(PermissionSettings permissionSettings) {
            this.permissionSettings = permissionSettings;
            return this;
        }

        public Builder configurePermissionSettings(Consumer<PermissionSettings> permissionSettingsCustomizer) {
            this.permissionSettingsCustomizer = permissionSettingsCustomizer;
            return this;
        }

        public Builder withPageLayout(PageLayout pageLayout) {
            this.pageLayout = pageLayout;
            return this;
        }

        public Builder withFontSettings(FontSettings fontSettings) {
            this.fontSettings = fontSettings;
            return this;
        }

        public Builder configureFontSettings(Consumer<FontSettings> fontSettingsCustomizer) {
            this.fontSettingsCustomizer = fontSettingsCustomizer;
            return this;
        }

        public PDFQuill build() {
            return new PDFQuill(this);
        }
    }
}
