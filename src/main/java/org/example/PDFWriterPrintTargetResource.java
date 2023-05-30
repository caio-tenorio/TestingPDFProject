package org.example;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 *
 * @author igor.ramos - Jul 26, 2018
 */
public class PDFWriterPrintTargetResource implements TargetResourceAdpter<PDDocument> {
    private static final PDType1Font FONT_DEFAULT = PDType1Font.COURIER;
    private static final PDType1Font FONT_BOLD = PDType1Font.COURIER_BOLD;
    private PDDocument document;
    private byte[] pdf;
    private ByteArrayOutputStream os;
    private static int FONT_SIZE = 8;
    private PDRectangle pageSize;
    private float pageWidth;
    private float pageHeight;
    private float startX;
    private float startY;
    private float lineHeight;
    private float maxLineWidth;
    private int linesPerPage;
    private int currentLine;
    private PDPageContentStream contentStream;
    private PDPage currentPage;

    public PDFWriterPrintTargetResource() {
        this.os = new ByteArrayOutputStream();
        this.document = new PDDocument();
        this.pageSize = new PDRectangle(mmToPt(98), mmToPt(2000));
        this.pageWidth = pageSize.getWidth();
        this.pageHeight = pageSize.getHeight();

        // Define as posicoes dos textos
        this.startX = 3;
        this.startY = pageHeight - 10;
        this.maxLineWidth = pageWidth - startX * 2;

        float lineHeightPercentage = 1.12f; // 112% da altura da fonte
        this.lineHeight = FONT_SIZE * lineHeightPercentage;
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

    @Override
    public void close() throws Exception {
        this.getBase64PDFBytes();
    }

    @Override
    public TargetResourceAdpter<PDDocument> print(String text) throws PrinterException {
        return this.print(text, -1);
    }

    @Override
    public TargetResourceAdpter<PDDocument> print(String text, int param) throws PrinterException {
        try {

            if (this.currentLine % this.linesPerPage == 0) {
                // Cria nova pagina quando necessario
                if (this.contentStream != null) {
                    this.contentStream.close();
                }
                this.currentPage = new PDPage(this.pageSize);
                this.document.addPage(this.currentPage);
                this.contentStream = new PDPageContentStream(this.document, this.currentPage);
            }

            float lineY = this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight;
            addTextLine(this.contentStream, text, this.startX, lineY, param);
            this.currentLine++;
//            List<String> wrappedLines = wrapTextLine(text, this.maxLineWidth);
//            for (String wrappedLine : wrappedLines) {
//                if (this.currentLine % this.linesPerPage == 0) {
//                    // Cria nova pagina quando necessario
//                    if (this.contentStream != null) {
//                        this.contentStream.close();
//                    }
//                    this.currentPage = new PDPage(this.pageSize);
//                    this.document.addPage(this.currentPage);
//                    this.contentStream = new PDPageContentStream(this.document, this.currentPage);
//                }
//
//                float lineY = this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight;
//                addTextLine(this.contentStream, wrappedLine, this.startX, lineY, param);
//                this.currentLine++;
//            }
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    private static void addTextLine(PDPageContentStream contentStream, String text, float x, float y, int param) throws IOException {
        contentStream.beginText();
        contentStream.setFont((param == 1) ? FONT_BOLD : FONT_DEFAULT, FONT_SIZE);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private static List<String> wrapTextLine(String text, float maxLineWidth) throws IOException {
        List<String> wrappedLines = new ArrayList<>();

        StringBuilder currentTextLine = new StringBuilder();
        String[] words = text.split(" ");

        for (String word : words) {
            if (currentTextLine.length() == 0 || FONT_DEFAULT.getStringWidth(currentTextLine.toString() + " " + word) / 1000 * FONT_SIZE <= maxLineWidth) {
                currentTextLine.append(word).append(" ");
            } else {
                wrappedLines.add(currentTextLine.toString().trim());
                currentTextLine = new StringBuilder(word + " ");
            }
        }

        if (currentTextLine.length() > 0) {
            wrappedLines.add(currentTextLine.toString().trim());
        }

        return wrappedLines;
    }

    /**
     *
     */
    @Override
    public TargetResourceAdpter<PDDocument> printImage(ByteArrayInputStream imgBytes) {
        //TODO: Checar espaço disponível na página e criar página nova caso necessário
        try {
            if (this.currentPage == null || this.contentStream == null) {
                this.currentPage = new PDPage(this.pageSize);
                this.document.addPage(this.currentPage);
                this.contentStream = new PDPageContentStream(this.document, this.currentPage);
            }

            BufferedImage image = ImageIO.read(imgBytes);
            PDImageXObject pdImage = LosslessFactory.createFromImage(this.document, image);
            float imageHeight = mmToPt(7);

            float lineY = (this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight) - imageHeight;
            contentStream.drawImage(pdImage, mmToPt(3), lineY, mmToPt(98), imageHeight);
            // Quantidade de linhas ocupadas pelo codigo de barra arredonado para cima
            int imageLines = (int) Math.ceil(imageHeight / this.lineHeight);
            // Atualizando linha atual baseado na quantidade de linhas ocupadas pel imagem
            currentLine += imageLines + 1;
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    /**
     *
     */
    @Override
    public TargetResourceAdpter<PDDocument> printBarcode(String code, Integer... params) throws PrinterException {
        //TODO: Checar espaço disponível na página e criar página nova caso necessário
        try {
            int width = 350;
            int height = 350;
            Map<EncodeHintType, Object> hintMap = new EnumMap<>(EncodeHintType.class);
            hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
            hintMap.put(EncodeHintType.MARGIN, 0);
            hintMap.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

            MultiFormatWriter writer = new MultiFormatWriter();
            BitMatrix byteMatrix;
            BufferedImage image;

            if (params != null && params.length > 1 && params[2] == TiposCodigoBarra.QRCODE) {
                byteMatrix = writer.encode(code, BarcodeFormat.QR_CODE, width, height, hintMap);
            } else {
                byteMatrix = writer.encode(code, BarcodeFormat.CODE_128, width, height, hintMap);
            }

            image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

            createGraphics(image, byteMatrix, width, height);

            PDImageXObject pdImage = LosslessFactory.createFromImage(this.document, image);
            drawImage(pdImage, params);
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

    private void drawImage(PDImageXObject pdImage, Integer... params) throws IOException {
        float lineY = this.startY - (this.currentLine % this.linesPerPage) * this.lineHeight;
        float qrCodeHeight = mmToPt(50);
        float barcodeHeight = mmToPt(15);
        int barcodeLines;
        float barcodeStartX;

        if (params != null && params.length > 1 && params[2] == TiposCodigoBarra.QRCODE) {
            lineY = lineY - qrCodeHeight;
            barcodeLines = (int) Math.ceil(qrCodeHeight / this.lineHeight);
            //Centralizar barcode
            barcodeStartX = (maxLineWidth - mmToPt(50)) / 2;
            contentStream.drawImage(pdImage, barcodeStartX, lineY, mmToPt(50), qrCodeHeight); //TODO: Checar tamanhos
        } else {
            lineY = lineY - barcodeHeight;
            barcodeLines = (int) Math.ceil(barcodeHeight / this.lineHeight);
            //Centralizar barcode
            barcodeStartX = (maxLineWidth - mmToPt(80)) / 2;
            contentStream.drawImage(pdImage, barcodeStartX, lineY, mmToPt(80), barcodeHeight); //TODO: Checar tamanhos
        }
        //Atualiza currentLine
        currentLine += barcodeLines + 1;
    }

    /**
     *
     */
    @Override
    public TargetResourceAdpter<PDDocument> cutSignal() throws PrinterException {
        try {
            contentStream.beginText();
            contentStream.newLine();
            contentStream.newLine();
            contentStream.endText();
            contentStream.close();
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    @Override
    public PDDocument getResource() {
        return this.document;
    }

    /**
     * Método responsável por realizar o recorte do PDF para o tamanho necessário
     *
     * @param bs dados do PDF
     * @throws IOException
     */
    private byte[] cropPDF(byte[] bs) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(bs);
        PDDocument document = PDDocument.load(in);
        PDPageTree pages = document.getDocumentCatalog().getPages();

        for (PDPage page : pages) {
            PDRectangle mediaBox = page.getMediaBox();
            PDRectangle cropBox = new PDRectangle(mediaBox.getLowerLeftX(), pageHeight - (lineHeight * currentLine) - lineHeight, mediaBox.getUpperRightX() - 3, (lineHeight * currentLine) + lineHeight);
            page.setCropBox(cropBox);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();

        return out.toByteArray();
    }

    /**
     *
     * @param mm
     * @return
     */
    private float mmToPt(float mm) {
        return (72f * mm) / 25.4f;
    }
}