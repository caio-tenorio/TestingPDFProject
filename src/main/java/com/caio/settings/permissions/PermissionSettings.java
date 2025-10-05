package com.caio.settings.permissions;

public class PermissionSettings {
    private boolean canPrint = true;
    private boolean canModify = true;
    private boolean canExtractContent = true;

    public boolean isCanPrint() {
        return canPrint;
    }

    public void setCanPrint(boolean canPrint) {
        this.canPrint = canPrint;
    }

    public boolean isCanModify() {
        return canModify;
    }

    public void setCanModify(boolean canModify) {
        this.canModify = canModify;
    }

    public boolean isCanExtractContent() {
        return canExtractContent;
    }

    public void setCanExtractContent(boolean canExtractContent) {
        this.canExtractContent = canExtractContent;
    }
}
