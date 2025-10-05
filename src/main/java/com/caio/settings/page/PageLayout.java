package com.caio.settings.page;

import com.caio.paper.PaperType;
import com.caio.paper.PaperUtils;
import com.caio.settings.font.FontSettings;

public class PageLayout {
    // Margins
    private float marginLeft = 4f;
    private float marginRight = 4f;
    private float marginTop = 8f;
    private float marginBottom = 4f;

    private FontSettings fontSettings = new FontSettings();

    private PaperType paperType = PaperType.A4;

    //Defined by margins
    private float startX;
    private float startY;

    // Calculated
    private float lineHeight;
    private float maxLineWidth;
    private Integer linesPerPage;

    public PageLayout(PaperType paperType) {
        this.paperType = paperType != null ? paperType : PaperType.A4;
        this.assignDependentAttrs();
    }

    public void setFontSettings(FontSettings fontSettings) {
        this.fontSettings = fontSettings;
        this.assignDependentAttrs();
    }

    public void recalculate() {
        this.assignDependentAttrs();
    }

    private void assignDependentAttrs() {
        this.startX = this.marginLeft; // TODO: isso será definido pelas margens no futuro
        this.startY = getPageHeight() - this.marginTop; //TODO: revisar esse número arbitrário 8

        this.maxLineWidth = getPageWidth() - this.marginLeft - this.marginRight;

        //TODO: definir essa porcentagem através do spacing depois
        float lineHeightPercentage = 1.20f; // 140% da altura da fonte

        this.lineHeight = this.fontSettings.getFontSize() * lineHeightPercentage;
        this.linesPerPage = (int) Math.floor(startY / lineHeight);
    }

    public PaperType getPaperType() {
        return this.paperType;
    }

    public void setPaperType(PaperType paperType) {
        this.paperType = paperType != null ? paperType : PaperType.A4;
        this.assignDependentAttrs();
    }

    public boolean isThermalPaper() {
        return PaperUtils.isThermal(this.paperType);
    }

    public boolean isNonThermalPaper() {
        return PaperUtils.isNotThermal(this.paperType);
    }

    public float getPageWidth() {
        return this.paperType.getWidth();
    }

    public float getPageHeight() {
        return this.paperType.getHeight();
    }

    public float getStartX() {
        return startX;
    }

    public float getStartY() {
        return startY;
    }

    public float getLineHeight() {
        return lineHeight;
    }

    public float getMaxLineWidth() {
        return maxLineWidth;
    }

    public void setMaxLineWidth(float maxLineWidth) {
        this.maxLineWidth = maxLineWidth;
    }

    public int getLinesPerPage() {
        return linesPerPage;
    }

    public FontSettings getFontSettings() {
        return this.fontSettings;
    }
}
