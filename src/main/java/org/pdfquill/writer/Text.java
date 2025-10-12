package org.pdfquill.writer;

import org.pdfquill.settings.font.FontSettings;

public class Text {
    private final FontSettings fontSetting;
    private final String text;
    private float x;

    public Text(String text, FontSettings fontSetting) {
        this.text = text;
        this.fontSetting = fontSetting;
    }

    public FontSettings getFontSetting() {
        return fontSetting;
    }
    public String getText() { return  text; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
}
