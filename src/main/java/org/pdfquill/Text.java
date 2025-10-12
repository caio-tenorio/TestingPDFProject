package org.pdfquill;

import org.pdfquill.settings.font.FontSettings;

public class Text {
    private final FontSettings fontSetting;
    private final String text;

    public Text(String text, FontSettings fontSetting) {
        this.text = text;
        this.fontSetting = fontSetting;
    }

    public FontSettings getFontSetting() {
        return fontSetting;
    }
    public String getText() { return  text; }
}
