package org.pdfquill.settings.font;

import org.apache.pdfbox.pdmodel.font.PDType1Font;

import java.util.HashMap;

/**
 * Mutable holder for the font family variations and size applied when drawing text.
 * Callers can reuse instances or obtain defensive copies via {@link #copy()} when
 * sharing settings across different {@code PageLayout}s.
 */
public class FontSettings {
    private int fontSize = 12;
    private PDType1Font defaultFont = PDType1Font.COURIER;
    private PDType1Font boldFont = PDType1Font.COURIER_BOLD;
    private PDType1Font italicFont = PDType1Font.COURIER_OBLIQUE;
    private PDType1Font boldItalicFont = PDType1Font.COURIER_BOLD_OBLIQUE;
    private HashMap<FontType, PDType1Font> fontMap = new HashMap<>();

    /**
     * Creates a configuration seeded with Courier fonts and size 12.
     */
    public FontSettings() {
        loadFontMap();
    }

    /**
     * Copy constructor used to clone another set of font settings.
     *
     * @param other source configuration; a {@code null} value leaves defaults intact
     */
    public FontSettings(FontSettings other) {
        if (other == null) {
            return;
        }
        this.fontSize = other.fontSize;
        this.defaultFont = other.defaultFont;
        this.boldFont = other.boldFont;
        this.italicFont = other.italicFont;
        this.boldItalicFont = other.boldItalicFont;
        loadFontMap();
    }

    public void loadFontMap() {
        fontMap.put(FontType.DEFAULT, defaultFont);
        fontMap.put(FontType.BOLD, boldFont);
        fontMap.put(FontType.ITALIC, italicFont);
        fontMap.put(FontType.ITALIC_BOLD, boldItalicFont);
    }

    public HashMap<FontType, PDType1Font> getFontMap() {
        return fontMap;
    }

    public PDType1Font getFontByFontType(FontType fontType) {
        return fontMap.get(fontType);
    }

    /**
     * Builds a deep copy of the current values.
     *
     * @return a new {@link FontSettings} with identical properties
     */
    public FontSettings copy() {
        return new FontSettings(this);
    }

    /**
     * @return configured font size in points
     */
    public int getFontSize() {
        return fontSize;
    }

    /**
     * Sets the default font size in points.
     *
     * @param fontSize size value to apply
     */
    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    /**
     * @return typeface used for normal weight text
     */
    public PDType1Font getDefaultFont() {
        return defaultFont;
    }

    /**
     * Defines the typeface used for normal weight text.
     *
     * @param defaultFont font to use for regular text
     */
    public void setDefaultFont(PDType1Font defaultFont) {
        this.defaultFont = defaultFont;
    }

    /**
     * @return typeface used for bold text
     */
    public PDType1Font getBoldFont() {
        return boldFont;
    }

    /**
     * Defines the typeface used for bold text.
     *
     * @param boldFont font to use for bold text
     */
    public void setBoldFont(PDType1Font boldFont) {
        this.boldFont = boldFont;
    }

    /**
     * @return typeface used for italic text
     */
    public PDType1Font getItalicFont() {
        return italicFont;
    }

    /**
     * Defines the typeface used for italic text.
     *
     * @param italicFont font to use for italic text
     */
    public void setItalicFont(PDType1Font italicFont) {
        this.italicFont = italicFont;
    }

    /**
     * @return typeface used for bold italic text
     */
    public PDType1Font getBoldItalicFont() {
        return boldItalicFont;
    }

    /**
     * Defines the typeface used for bold italic text.
     *
     * @param boldItalicFont font to use for bold italic text
     */
    public void setBoldItalicFont(PDType1Font boldItalicFont) {
        this.boldItalicFont = boldItalicFont;
    }
}
