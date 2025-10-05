package com.caio.settings.page;

import com.caio.settings.font.FontSettings;

public class PageLayout {
    // Margins
    private float marginLeft = 4f;
    private float marginRight = 4f;
    private float marginTop = 50f;
    private float marginBottom = 4f;

    private FontSettings fontSettings = new FontSettings();

    // Required
    private float pageWidth;
    private float pageHeight;

    //Defined by margins
    private float startX;
    private float startY;

    // Calculated
    private float lineHeight;
    private float maxLineWidth;
    private Integer linesPerPage;

    public PageLayout(float marginLeft, float marginRight, float marginTop, float marginBottom, float pageWidth,
                      float pageHeight, FontSettings fontSettings) {

        //TODO: Creio que devemos permitir mudança de page settings no meio do texto
        this.marginLeft = marginLeft;
        this.marginRight = marginRight;
        this.marginTop = marginTop;
        this.marginBottom = marginBottom;

        this.fontSettings = fontSettings;

        this.pageWidth = pageWidth;
        this.pageHeight = pageHeight;

        this.startX = this.marginLeft; // TODO: isso será definido pelas margens no futuro
        this.startY = pageHeight - this.marginTop; //TODO: revisar esse número arbitrário 8

        this.maxLineWidth = pageWidth - startX * 2;

        //TODO: definir essa porcentagem através do spacing depois
        float lineHeightPercentage = 1.20f; // 140% da altura da fonte

        this.lineHeight = this.fontSettings.getFontSize() * lineHeightPercentage;
        this.linesPerPage = (int) Math.floor(startY / lineHeight);
    }

    public PageLayout(float pageHeight, float pageWidth) {
        this.pageHeight = pageHeight;
        this.pageWidth = pageWidth;

        this.startX = this.marginLeft; // TODO: isso será definido pelas margens no futuro
        this.startY = pageHeight - this.marginTop; //TODO: revisar esse número arbitrário 8

        this.assignDependentAttrs();
    }

    private void assignDependentAttrs() {
        this.maxLineWidth = this.pageWidth - this.startX * 2;

        //TODO: definir essa porcentagem através do spacing depois
        float lineHeightPercentage = 1.20f; // 140% da altura da fonte

        this.lineHeight = this.fontSettings.getFontSize() * lineHeightPercentage;
        this.linesPerPage = (int) Math.floor(startY / lineHeight);
    }

    public float getPageWidth() {
        return pageWidth;
    }

    public float getPageHeight() {
        return pageHeight;
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
