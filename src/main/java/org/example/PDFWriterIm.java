package org.example;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;

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
import org.example.barcode.BarcodeType;
import org.example.barcode.BarcodeUtils;
import org.example.font.FontSettings;
import org.example.paper.PaperType;
import org.example.paper.PaperUtils;


public class PDFWriterIm {
    private FontSettings fontSettings = new FontSettings();
    private byte[] pdf;
    private float pageWidth;
    private float pageHeight;
    private float startX;
    private float startY;
    private float lineHeight;
    private float maxLineWidth;
    private int linesPerPage;
    private int currentLine;
    private PaperType paperType;

    private ByteArrayOutputStream os;

    private PDRectangle pageSize;
    private PDDocument document;
    private PDPageContentStream contentStream;
    private PDPage currentPage;

    private void setFontSettings(FontSettings fontSettings) {
        this.fontSettings = fontSettings;
    }

    public PDFWriterIm(PaperType paperType) {
        this.paperType = paperType;
        this.os = new ByteArrayOutputStream();
        this.document = new PDDocument();
        this.pageSize = PDRectangle.A4;
        this.pageWidth = pageSize.getWidth();
        this.pageHeight = pageSize.getHeight();

        // Define as posicoes dos textos
        this.startX = 3;
        this.startY = pageHeight - 8;
        this.maxLineWidth = pageWidth - startX * 2;
        float lineHeightPercentage = 1.40f; // 140% da altura da fonte
        this.lineHeight = fontSettings.getFontSize() * lineHeightPercentage;
        this.linesPerPage = (int) Math.floor(startY / lineHeight);
        this.currentLine = 0;
        this.contentStream = null;
        this.currentPage = null;

    }

    private void loadDefaultSettings() {
        this.os = new ByteArrayOutputStream();
        this.document = new PDDocument();
        this.pageSize = new PDRectangle(mmToPt(98), mmToPt(2000));
        this.pageWidth = pageSize.getWidth();
        this.pageHeight = pageSize.getHeight();

        // Define as posicoes dos textos
        // TODO: That should be defined by margins
        this.startX = 3;
        this.startY = pageHeight - 8;
        // TODO: We have to consider left margin (if exists), right margin (if exists), fontsize and paper type
        this.maxLineWidth = pageWidth - startX * 2;

        float lineHeightPercentage = 1.40f; // 140% da altura da fonte
        this.lineHeight = this.fontSettings.getFontSize() * lineHeightPercentage;
        this.linesPerPage = (int) Math.floor(startY / lineHeight);
        this.currentLine = 0;
        this.contentStream = null;
        this.currentPage = null;
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

    public PDFWriterIm print(String text) throws PrinterException {
        return this.print(text, -1);
    }

    public PDFWriterIm print(String text, int param) throws PrinterException {
        try {
            List<String> lines = splitLine(text);
            for (String line : lines) {
                addNewPageIfNeeded();
                float lineY = this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight;
                addTextLine(this.contentStream, line, this.startX, lineY);
                this.currentLine++;
            }
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    private boolean addNewPageIfNeeded() throws IOException {
        if (this.document.getNumberOfPages() == 0 || this.currentLine >= this.linesPerPage) {
            addNewPage();
            return true;
        }
        return false;
    }

    private boolean addNewPageIfNeeded(int finalLine) throws IOException {
        if (this.document.getNumberOfPages() == 0 || finalLine >= this.linesPerPage) {
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
            contentStream.setFont(this.fontSettings.getDefaultFont(), this.fontSettings.getFontSize());
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
    public PDFWriterIm printImage(ByteArrayInputStream imgBytes) {
        try {
            if (this.currentPage == null || this.contentStream == null) {
                this.currentPage = new PDPage(this.pageSize);
                this.document.addPage(this.currentPage);
                this.contentStream = new PDPageContentStream(this.document, this.currentPage);
            }

            BufferedImage image = ImageIO.read(imgBytes);
            PDImageXObject pdImage = LosslessFactory.createFromImage(this.document, image);
            float imageHeight = mmToPt(7);

            //Calcula coordenada y
            float lineY = (this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight) - imageHeight;
            // Quantidade de linhas ocupadas pelo codigo de barra arredonado para cima
            int imageLines = (int) Math.ceil(imageHeight / this.lineHeight);
            int finalLine = this.currentLine + imageLines + 1;

            if (addNewPageIfNeeded(finalLine)) {
                //Recalcula coordenada y caso uma nova página seja adicionada
                lineY = (this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight) - imageHeight;
            }

            // Centraliza imagem
            float barcodeStartX = (maxLineWidth - mmToPt(98)) / 2;

            this.contentStream.drawImage(pdImage, barcodeStartX, lineY, mmToPt(98), imageHeight);

            // Atualizando linha atual baseado na quantidade de linhas ocupadas pela imagem
            this.currentLine += imageLines + 1;
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    public PDFWriterIm printBarcode(String code, BarcodeType barcodeType) throws PrinterException {
        return  this.printBarcode(code, barcodeType, 0, 0);
    }

    /**
     *
     */
    public PDFWriterIm printBarcode(String code, BarcodeType barcodeType, int height, int width) throws PrinterException {
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

            float imageHeight = BarcodeUtils.isQrCode(barcodeType) ? mmToPt(48f) : mmToPt(12f);
            float imageWidth = BarcodeUtils.isQrCode(barcodeType) ? mmToPt(48f) : mmToPt(80f);
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
        int barcodeLines = (int) Math.ceil(imageHeight / this.lineHeight);
        //Centralizar barcode
        float barcodeStartX = (maxLineWidth - imageWidth) / 2;

        //Calcula coordenada y
        float lineY = (this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight) - imageHeight;

        int finalLine = this.currentLine + barcodeLines + 1;

        if (addNewPageIfNeeded(finalLine)) {
            //Recalcula coordenada y se uma nova pagina tiver sido adicionada
            lineY = (this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight) - imageHeight;
        }

        contentStream.drawImage(pdImage, barcodeStartX, lineY, imageWidth, imageHeight);
        // Atualiza currentLine
        this.currentLine += barcodeLines + 1;
    }

    /**
     *
     */
    public PDFWriterIm cutSignal() throws PrinterException {
        try {
            float lineY = this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight;

            contentStream.beginText();
            contentStream.setFont(this.fontSettings.getDefaultFont(), this.fontSettings.getFontSize());
            contentStream.newLineAtOffset(startX,lineY - this.lineHeight * 2);
            contentStream.showText(createFullWidthString(" ", this.maxLineWidth));
            contentStream.endText();

            currentLine = currentLine + 2;

            if (PaperUtils.isNotThermal(this.paperType)) {
                addNewPage();
            }
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    public PDDocument getResource() {
        return this.document;
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

        for (PDPage page : pages) {
            if (!PaperUtils.isThermal(this.paperType)) {
                PDRectangle mediaBox = page.getMediaBox();
                PDRectangle cropBox = new PDRectangle(mediaBox.getLowerLeftX(), pageHeight - (lineHeight * currentLine) - lineHeight,
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
     *
     * @param mm
     * @return
     */
    private float mmToPt(float mm) {
        return (72f * mm) / 25.4f;
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

    private float getTextWidth(String text) throws IOException {
        return this.fontSettings.getDefaultFont().getStringWidth(text) * this.fontSettings.getFontSize() / 1000f;
    }

    private String repeatString(String text, int repetitions) {
        StringBuilder sb = new StringBuilder(text.length() * repetitions);
        for (int i = 0; i < repetitions; i++) {
            sb.append(text);
        }
        return sb.toString();
    }
}
