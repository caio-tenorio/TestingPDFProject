package org.pdfquill;

import java.util.ArrayList;
import java.util.List;

public class TextLinePlan {
    private List<Text> textList = new ArrayList<>();
    private float y;

    public List<Text> getTextList() {
        return textList;
    }

    public void setTextList(List<Text> textList) {
        this.textList = textList;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }
}
