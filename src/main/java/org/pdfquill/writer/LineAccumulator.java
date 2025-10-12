package org.pdfquill.writer;

import java.util.ArrayList;
import java.util.List;

public final class LineAccumulator {
    private final float startX;
    private final List<Text> chunks = new ArrayList<>();
    private float width = 0f;
    private int maxFontSize = 0;

    public LineAccumulator(float startX) {
        this.startX = startX;
    }

    public float getWidth() {
        return width;
    }

    public boolean isEmpty() {
        return chunks.isEmpty();
    }

    public void addChunk(Text text, float textWidth, int fontSize) {
        text.setX(startX + width);
        chunks.add(text);
        width += textWidth;
        if (fontSize > maxFontSize) {
            maxFontSize = fontSize;
        }
    }

    public void flushInto(List<TextLinePlan> target) {
        if (chunks.isEmpty()) {
            return;
        }

        TextLinePlan plan = new TextLinePlan();
        plan.setMaxFontSize(maxFontSize);
        plan.setTextList(new ArrayList<>(chunks));
        target.add(plan);

        chunks.clear();
        width = 0f;
        maxFontSize = 0;
    }
}