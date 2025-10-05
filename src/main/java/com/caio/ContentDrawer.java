package com.caio;

import com.caio.barcode.BarcodeType;
import com.caio.barcode.BarcodeUtils;
import com.caio.measurements.MeasurementUtils;
import com.caio.settings.page.PageLayout;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ContentDrawer {
    private final DocumentManager documentManager;
    private final PageLayout pageLayout;
    private final boolean preserveSpaces;

    public ContentDrawer(DocumentManager documentManager, PageLayout pageLayout, boolean preserveSpaces) {
        this.documentManager = documentManager;
        this.pageLayout = pageLayout;
        this.preserveSpaces = preserveSpaces;
    }

    public void print(String text) throws PrinterException {
        try {
            List<String> lines = wordWrapping(text);
            for (String line : lines) {
                printLine(line);
            }
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
    }

    private void printLine(String line) throws IOException {
        documentManager.addNewPageIfNeeded();
        float lineY = this.pageLayout.getStartY() - (this.documentManager.getCurrentLine() % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight();
        addTextLine(line, this.pageLayout.getStartX(), lineY);
        this.documentManager.incrementCurrentLine();
    }

    private void addTextLine(String text, float x, float y) throws IOException {
        try {
            documentManager.getContentStream().beginText();
            documentManager.getContentStream().setFont(this.pageLayout.getFontSettings().getDefaultFont(), this.pageLayout.getFontSettings().getFontSize());
            documentManager.getContentStream().newLineAtOffset(x, y);
            documentManager.getContentStream().showText(text);
            documentManager.getContentStream().endText();
        } catch (IllegalArgumentException e) {
            documentManager.getContentStream().endText();
        }
    }

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

    public void printImage(ByteArrayInputStream imgBytes) throws PrinterException {
        try {
            if (documentManager.getCurrentPage() == null || documentManager.getContentStream() == null) {
                documentManager.addNewPage();
            }

            BufferedImage image = ImageIO.read(imgBytes);
            PDImageXObject pdImage = LosslessFactory.createFromImage(documentManager.getDocument(), image);
            float imageHeight = MeasurementUtils.mmToPt(7);

            float lineY = (this.pageLayout.getStartY() - (documentManager.getCurrentLine() % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight()) - imageHeight;
            int imageLines = (int) Math.ceil(imageHeight / this.pageLayout.getLineHeight());
            int finalLine = documentManager.getCurrentLine() + imageLines + 1;

            if (documentManager.addNewPageIfNeeded(finalLine)) {
                lineY = (this.pageLayout.getStartY() - (documentManager.getCurrentLine() % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight()) - imageHeight;
            }

            float barcodeStartX = (this.pageLayout.getMaxLineWidth() - MeasurementUtils.mmToPt(98)) / 2;

            documentManager.getContentStream().drawImage(pdImage, barcodeStartX, lineY, MeasurementUtils.mmToPt(98), imageHeight);

            documentManager.incrementCurrentLine(imageLines + 1);
        } catch (IOException e) {
            throw new PrinterException("Erro ao desenhar imagem no PDF", e);
        }
    }

    public void printBarcode(String code, BarcodeType barcodeType, int height, int width) throws PrinterException {
        try {
            if (height == 0) height = 350;
            if (width == 0) width = 350;

            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(EncodeHintType.MARGIN, 0);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            MultiFormatWriter writer = new MultiFormatWriter();
            BarcodeFormat barcodeFormat = BarcodeUtils.getBarcodeFormat(barcodeType);
            BitMatrix byteMatrix = writer.encode(code, barcodeFormat, width, height, hintMap);

            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            createGraphics(image, byteMatrix, width, height);

            PDImageXObject pdImage = LosslessFactory.createFromImage(documentManager.getDocument(), image);

            float imageHeight = BarcodeUtils.isQrCode(barcodeType) ? MeasurementUtils.mmToPt(48f) : MeasurementUtils.mmToPt(12f);
            float imageWidth = BarcodeUtils.isQrCode(barcodeType) ? MeasurementUtils.mmToPt(48f) : MeasurementUtils.mmToPt(80f);
            drawImage(pdImage, imageHeight, imageWidth);
        } catch (Throwable e) {
            throw new PrinterException("Erro ao criar código de barras para PDF", e);
        }
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
        float barcodeStartX = (this.pageLayout.getMaxLineWidth() - imageWidth) / 2;
        float lineY = (this.pageLayout.getStartY() - (documentManager.getCurrentLine() % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight()) - imageHeight;
        int finalLine = documentManager.getCurrentLine() + barcodeLines + 1;

        if (documentManager.addNewPageIfNeeded(finalLine)) {
            lineY = (this.pageLayout.getStartY() - (documentManager.getCurrentLine() % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight()) - imageHeight;
        }

        documentManager.getContentStream().drawImage(pdImage, barcodeStartX, lineY, imageWidth, imageHeight);
        documentManager.incrementCurrentLine(barcodeLines + 1);
    }

    public void cutSignal() throws PrinterException {
        try {
            float lineY = this.pageLayout.getStartY() - (documentManager.getCurrentLine() % this.pageLayout.getLinesPerPage()) * this.pageLayout.getLineHeight();

            documentManager.getContentStream().beginText();
            documentManager.getContentStream().setFont(this.pageLayout.getFontSettings().getDefaultFont(), this.pageLayout.getFontSettings().getFontSize());
            documentManager.getContentStream().newLineAtOffset(this.pageLayout.getStartX(), lineY - this.pageLayout.getLineHeight() * 2);
            documentManager.getContentStream().showText(createFullWidthString(" ", this.pageLayout.getMaxLineWidth()));
            documentManager.getContentStream().endText();

            documentManager.incrementCurrentLine(2);

            if (this.pageLayout.isNonThermalPaper()) {
                documentManager.addNewPage();
            }
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar sinal de corte para PDF", e);
        }
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
}
