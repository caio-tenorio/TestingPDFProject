package org.pdfquill.writer;

import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.util.Matrix;

import java.io.IOException;

/**
 * Tracks the current text position within a {@link PDPageContentStream}, taking care of the
 * transformation matrix and the text object lifecycle so callers do not need to juggle
 * repeated begin/end calls.
 */
public final class TextCursor {
    private PDPageContentStream contentStream;
    private Matrix textMatrix = Matrix.getTranslateInstance(0, 0);

    private float startX;
    private float startY;
    private float currentX;
    private float currentY;
    private float writtenHeight;

    private boolean textObjectOpen = false;

    /**
     * Binds the cursor to a new page content stream and resets the writing origin.
     */
    public void bindToContentStream(PDPageContentStream contentStream, float startX, float startY) throws IOException {
        closeTextObject();
        this.contentStream = contentStream;
        this.startX = startX;
        this.startY = startY;
        resetProgress();
        this.textMatrix = Matrix.getTranslateInstance(startX, startY);
        this.textObjectOpen = false;
    }

    /**
     * Advances the cursor by the provided height, returning the new baseline Y coordinate.
     */
    public float advance(float height) {
        this.writtenHeight += height;
        this.currentY = this.startY - this.writtenHeight;
        this.currentX = this.startX;
        return this.currentY;
    }

    /**
     * Resets vertical progress tracking for a newly bound page.
     */
    public void resetProgress() {
        this.writtenHeight = 0f;
        this.currentX = this.startX;
        this.currentY = this.startY;
    }

    /**
     * Returns the height already written on the current page.
     */
    public float getWrittenHeight() {
        return this.writtenHeight;
    }

    /**
     * Returns the current Y coordinate for the baseline.
     */
    public float getCurrentY() {
        return this.currentY;
    }

    private void ensureTextObject() throws IOException {
        if (this.contentStream == null) {
            throw new IllegalStateException("No content stream bound to cursor");
        }
        if (!this.textObjectOpen) {
            this.contentStream.beginText();
            this.textObjectOpen = true;
        }
    }

    /**
     * Moves the text matrix to an absolute coordinate.
     */
    public void moveTo(float x, float y) throws IOException {
        ensureTextObject();
        this.currentX = x;
        this.currentY = y;
        this.textMatrix = Matrix.getTranslateInstance(x, y);
        this.contentStream.setTextMatrix(this.textMatrix);
    }

    /**
     * Writes the supplied text using the supplied font at the cursor's current position.
     */
    public void showText(String text, PDType1Font font, int fontSize) throws IOException {
        if (text == null || text.isEmpty()) {
            return;
        }
        ensureTextObject();
        this.contentStream.setFont(font, fontSize);
        this.contentStream.showText(text);
    }

    /**
     * Convenience helper that moves the cursor then writes the text.
     */
    public void showTextAt(String text, float x, float y, PDType1Font font, int fontSize) throws IOException {
        moveTo(x, y);
        showText(text, font, fontSize);
    }

    /**
     * Ensures the current text object is closed so other drawing commands can run.
     */
    public void closeTextObject() throws IOException {
        if (this.textObjectOpen && this.contentStream != null) {
            this.contentStream.endText();
            this.textObjectOpen = false;
        }
    }

    /**
     * Releases the cursor from the bound content stream.
     */
    public void detach() throws IOException {
        closeTextObject();
        this.contentStream = null;
    }
}
