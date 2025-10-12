package org.pdfquill;

import org.pdfquill.settings.font.FontSettings;

public class Text {
    private final FontSettings fontSetting;
    private final String text;
    private float x;
    private float y;

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
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
}
