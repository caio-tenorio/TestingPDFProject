package org.example.font;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class FontSettings {
    private int fontSize = 12;
    private PDType1Font defaultFont = PDType1Font.COURIER;
    private PDType1Font boldFont = PDType1Font.COURIER_BOLD;
    private PDType1Font italicFont = PDType1Font.COURIER_OBLIQUE;
    private PDType1Font boldItalicFont = PDType1Font.COURIER_BOLD_OBLIQUE;

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public PDType1Font getDefaultFont() {
        return defaultFont;
    }

    public void setDefaultFont(PDType1Font defaultFont) {
        this.defaultFont = defaultFont;
    }

    public PDType1Font getBoldFont() {
        return boldFont;
    }

    public void setBoldFont(PDType1Font boldFont) {
        this.boldFont = boldFont;
    }

    public PDType1Font getItalicFont() {
        return italicFont;
    }

    public void setItalicFont(PDType1Font italicFont) {
        this.italicFont = italicFont;
    }

    public PDType1Font getBoldItalicFont() {
        return boldItalicFont;
    }

    public void setBoldItalicFont(PDType1Font boldItalicFont) {
        this.boldItalicFont = boldItalicFont;
    }
}
