package org.pdfquill;

import java.util.ArrayList;
import java.util.List;

public class TextBuilder {
    private List<Text> textList = new ArrayList<>();
    private int maxFontSize;

    public TextBuilder() {}

    public List<Text> getTextList() {
        return textList;
    }

    public int getMaxFontSize() {
        return maxFontSize;
    }

    public TextBuilder addText(Text text) {
        textList.add(text);
        if (text.getFontSetting().getFontSize() > maxFontSize) {
            maxFontSize = text.getFontSetting().getFontSize();
        }
        return this;
    }

    private void calculateMaxFontSize() {
        this.maxFontSize = textList.stream()
                .mapToInt(text -> text.getFontSetting().getFontSize())
                .max()
                .orElse(0);
    }
}
