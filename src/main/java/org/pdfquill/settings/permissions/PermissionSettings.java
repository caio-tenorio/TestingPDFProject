package org.pdfquill.settings.permissions;

/**
 * Simple bean that mirrors the PDF permission flags configured when exporting a document.
 */
public class PermissionSettings {
    private boolean canPrint = true;
    private boolean canModify = true;
    private boolean canExtractContent = true;

    /**
     * @return whether document printing is allowed
     */
    public boolean isCanPrint() {
        return canPrint;
    }

    /**
     * Enables or disables print permissions.
     *
     * @param canPrint desired print flag
     */
    public void setCanPrint(boolean canPrint) {
        this.canPrint = canPrint;
    }

    /**
     * @return whether document modifications are allowed
     */
    public boolean isCanModify() {
        return canModify;
    }

    /**
     * Enables or disables document modification permissions.
     *
     * @param canModify desired modification flag
     */
    public void setCanModify(boolean canModify) {
        this.canModify = canModify;
    }

    /**
     * @return whether text extraction is allowed
     */
    public boolean isCanExtractContent() {
        return canExtractContent;
    }

    /**
     * Enables or disables text extraction permissions.
     *
     * @param canExtractContent desired extraction flag
     */
    public void setCanExtractContent(boolean canExtractContent) {
        this.canExtractContent = canExtractContent;
    }
}
