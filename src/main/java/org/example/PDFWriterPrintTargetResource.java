package org.example;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.EnumMap;
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

    public PDFWriterPrintTargetResource() {
        this.os = new ByteArrayOutputStream();
        this.document = new PDDocument();
        PDPage page = new PDPage(new PDRectangle(mmToPt(98), mmToPt(2000)));
        this.document.addPage(page);
    }

    /**
     *
     * @return
     */
    public String getBase64PDFBytes() throws PrinterException {
        try {
            this.document.save(this.os);
            this.document.close();
            this.pdf = this.os.toByteArray();
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
            PDPage page = this.document.getPage(0);
            PDPageContentStream contentStream = new PDPageContentStream(this.document, page, PDPageContentStream.AppendMode.APPEND, true);
            contentStream.beginText();
            contentStream.setFont((param == 1) ? FONT_BOLD : FONT_DEFAULT, 8);
            contentStream.newLineAtOffset(mmToPt(3), mmToPt(3));
            contentStream.showText(text);
            contentStream.endText();
            contentStream.close();
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    /**
     *
     */
    @Override
    public TargetResourceAdpter<PDDocument> printImage(ByteArrayInputStream imgBytes) {
        try {
            BufferedImage image = ImageIO.read(imgBytes);
            PDImageXObject pdImage = LosslessFactory.createFromImage(this.document, image);
            PDPage page = this.document.getPage(0);
            PDPageContentStream contentStream = new PDPageContentStream(this.document, page, PDPageContentStream.AppendMode.APPEND, true);
            contentStream.drawImage(pdImage, mmToPt(3), mmToPt(3), mmToPt(98), mmToPt(7));
            contentStream.close();
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

            if (params[2] == TiposCodigoBarra.QRCODE) {
                byteMatrix = writer.encode(code, BarcodeFormat.QR_CODE, width, height, hintMap);
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            } else {
                byteMatrix = writer.encode(code, BarcodeFormat.CODE_128, width, height, hintMap);
                image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            }

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

            PDImageXObject pdImage = LosslessFactory.createFromImage(this.document, image);

            PDPage page = this.document.getPage(0);
            PDPageContentStream contentStream = new PDPageContentStream(this.document, page, PDPageContentStream.AppendMode.APPEND, true);

            if (params[2] == TiposCodigoBarra.QRCODE) {
                contentStream.drawImage(pdImage, mmToPt(3), mmToPt(3), mmToPt(130), mmToPt(130));
            } else {
                contentStream.drawImage(pdImage, mmToPt(3), mmToPt(3), mmToPt(80), mmToPt(30));
            }

            contentStream.close();
        } catch (Throwable e) {
            throw new PrinterException("Erro ao criar linha para PDF", e);
        }
        return this;
    }

    /**
     *
     */
    @Override
    public TargetResourceAdpter<PDDocument> cutSignal() throws PrinterException {
        try {
            PDPage page = this.document.getPage(0);
            PDPageContentStream contentStream = new PDPageContentStream(this.document, page, PDPageContentStream.AppendMode.APPEND, true);
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
            PDRectangle cropBox = new PDRectangle(mediaBox.getLowerLeftX(), mediaBox.getLowerLeftY(), mediaBox.getUpperRightX() - 3, mediaBox.getUpperRightY() - 3);
            page.setCropBox(cropBox);
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        document.save(out);
        document.close();

        return out.toByteArray();
    }

    /**
     * Retorna o cálculo do tamanho do retângulo cropado que o PDF deverá ser replicado
     *
     * @param pageSize Tamanho da página original
     * @param reader PDF original em leitura
     * @param page Quantidade de páginas do PDF
     * @return Rectangle
     * @throws IOException
     */
//    private Rectangle getOutputPageSize(Rectangle pageSize, PDDocument reader, int page) throws IOException {
//        PDPageContentStream contentStream = new PDPageContentStream(reader, reader.getPage(page));
//        TextMarginFinder finder = new TextMarginFinder(contentStream);
//        finder.processPage(reader.getPage(page));
//        PDRectangle mediaBox = reader.getPage(page).getMediaBox();
//        Rectangle result = new Rectangle(mediaBox.getLowerLeftX(), mediaBox.getLowerLeftY(), finder.getLlx(), finder.getLly());
//
//        return result;
//    }

    /**
     *
     * @param mm
     * @return
     */
    private float mmToPt(float mm) {
        return (72f * mm) / 25.4f;
    }
}
