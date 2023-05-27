package org.example;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    private static int fontSize = 12;
    private static PDType1Font font = PDType1Font.HELVETICA;

    public static void main(String[] args) throws IOException {
        // Create a new document
        PDDocument document = new PDDocument();


        // Define the page dimensions
        PDRectangle pageSize = PDRectangle.A6;
        float pageWidth = pageSize.getWidth();
        float pageHeight = pageSize.getHeight();

        // Define the positions for the text lines
        float startX = 20;
        float startY = pageHeight - 20;
        float lineHeightPercentage = 1.2f; // 120% da altura da fonte
        float lineHeight = fontSize * lineHeightPercentage;

        int linesPerPage = 100;

        String testText = "Lorem ipsum dolor sit amet," +
                "consectetur adipiscing elit," +
                "Sed do eiusmod tempor incididunt," +
                "ut labore et dolore magna aliqua," +
                "Ut enim ad minim veniam," +
                "quis nostrud exercitation ullamco laboris," +
                "nisi ut aliquip ex ea commodo consequat," +
                "Duis aute irure dolor in reprehenderit," +
                "in voluptate velit esse cillum dolore," +
                "eu fugiat nulla pariatur," +
                "Excepteur sint occaecat cupidatat non proident," +
                "sunt in culpa qui officia deserunt mollit anim id est laborum," +
                "Lorem ipsum dolor sit amet," +
                "consectetur adipiscing elit," +
                "Sed do eiusmod tempor incididunt," +
                "ut labore et dolore magna aliqua," +
                "Ut enim ad minim veniam," +
                "quis nostrud exercitation ullamco laboris," +
                "nisi ut aliquip ex ea commodo consequat," +
                "Duis aute irure dolor in reprehenderit," +
                "in voluptate velit esse cillum dolore," +
                "eu fugiat nulla pariatur," +
                "Excepteur sint occaecat cupidatat non proident," +
                "sunt in culpa qui officia deserunt mollit anim id est laborum," +
                "Lorem ipsum dolor sit amet," +
                "consectetur adipiscing elit," +
                "Sed do eiusmod tempor incididunt," +
                "ut labore et dolore magna aliqua," +
                "Ut enim ad minim veniam," +
                "quis nostrud exercitation ullamco laboris," +
                "nisi ut aliquip ex ea commodo consequat," +
                "Duis aute irure dolor in reprehenderit," +
                "in voluptate velit esse cillum dolore.";

        // Define the text content
        List<String> textLines = Arrays.asList(testText.split(","));

        // Iterate through the text lines
        int currentLine = 0;

        PDPageContentStream contentStream = null;
        PDPage currentPage = null;

        // Define the maximum line width
        float maxLineWidth = pageWidth - startX * 2;

        for (String textLine : textLines) {
            // Split the text line if it exceeds the maximum line width
            List<String> wrappedLines = wrapTextLine(textLine, maxLineWidth);

            for (String wrappedLine : wrappedLines) {
                if (currentLine % linesPerPage == 0) {
                    // Create a new page if needed
                    if (contentStream != null) {
                        contentStream.close();
                    }
                    currentPage = new PDPage(pageSize);
                    document.addPage(currentPage);
                    contentStream = new PDPageContentStream(document, currentPage);
                }

                float lineY = startY - (currentLine % linesPerPage) * lineHeight;
                addTextLine(contentStream, wrappedLine, startX, lineY);
                currentLine++;
            }
        }

        // Close the content stream
        if (contentStream != null) {
            contentStream.close();
        }

        // Save the document to a byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.save(baos);
        document.close();

        // Convert the PDF bytes to Base64
        String base64 = java.util.Base64.getEncoder().encodeToString(baos.toByteArray());

        // Print the Base64 string
        System.out.println(base64);
    }

    private static void addTextLine(PDPageContentStream contentStream, String text, float x, float y) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private static List<String> wrapTextLine(String text, float maxLineWidth) throws IOException {
        List<String> wrappedLines = new ArrayList<>();

        StringBuilder currentLine = new StringBuilder();
        String[] words = text.split(" ");

        for (String word : words) {
            float wordWidth = font.getStringWidth(word) / 1000 * fontSize;

            if (currentLine.length() == 0 || font.getStringWidth(currentLine.toString() + " " + word) / 1000 * fontSize <= maxLineWidth) {
                currentLine.append(word).append(" ");
            } else {
                wrappedLines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word + " ");
            }
        }

        if (currentLine.length() > 0) {
            wrappedLines.add(currentLine.toString().trim());
        }

        return wrappedLines;
    }
}
