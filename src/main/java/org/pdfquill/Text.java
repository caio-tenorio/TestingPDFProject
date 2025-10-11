package org.pdfquill;

import org.pdfquill.settings.font.FontSettings;

public class Text {
    private final FontSettings fontSetting;
    private final String text;

    public Text(FontSettings fontSetting, String text) {
        this.fontSetting = fontSetting;
        this.text = text;
    }

    public FontSettings getFontSetting() {
        return fontSetting;
    }
    public String getText() { return  text; }
}
