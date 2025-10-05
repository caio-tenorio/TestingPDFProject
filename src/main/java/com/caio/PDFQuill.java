package com.caio;

import com.caio.barcode.BarcodeType;
import com.caio.paper.PaperType;
import com.caio.settings.font.FontSettings;
import com.caio.settings.page.PageLayout;
import com.caio.settings.permissions.PermissionSettings;

import javax.xml.bind.DatatypeConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Consumer;

public class PDFQuill {
    private final PageLayout pageLayout;
    private final PermissionSettings permissionSettings;
    private final DocumentManager documentManager;
    private final ContentDrawer contentDrawer;
    private byte[] pdf;

    public PDFQuill() {
        this(new Builder());
    }

    private PDFQuill(Builder builder) {
        PermissionSettings basePermissionSettings = builder.permissionSettings != null ? builder.permissionSettings : new PermissionSettings();
        this.permissionSettings = copyPermissionSettings(basePermissionSettings);
        PageLayout layout = builder.pageLayout != null ? builder.pageLayout : createDefaultPageLayout(builder.paperType);

        if (builder.pageLayout != null && builder.paperType != null) {
            layout.setPaperType(builder.paperType);
        }

        if (builder.fontSettings != null) {
            layout.setFontSettings(copyFontSettings(builder.fontSettings));
        }
        if (builder.fontSettingsCustomizer != null) {
            builder.fontSettingsCustomizer.accept(layout.getFontSettings());
            layout.recalculate();
        }

        this.pageLayout = layout;

        if (builder.permissionSettingsCustomizer != null) {
            builder.permissionSettingsCustomizer.accept(this.permissionSettings);
        }

        this.documentManager = new DocumentManager(this.pageLayout);
        this.contentDrawer = new ContentDrawer(this.documentManager, this.pageLayout, builder.preserveSpaces);
    }

    public static Builder builder() {
        return new Builder();
    }

    private static PageLayout createDefaultPageLayout(PaperType paperType) {
        PaperType resolvedPaperType = paperType != null ? paperType : PaperType.A4;
        return new PageLayout(resolvedPaperType);
    }

    public String getBase64PDFBytes() throws PrinterException {
        if (this.documentManager.isClosed()) {
            return DatatypeConverter.printBase64Binary(this.pdf);
        }

        try {
            this.pdf = this.documentManager.saveAndGetBytes();
        } catch (IOException e) {
            throw new PrinterException("Erro ao criar PDF", e);
        }
        return DatatypeConverter.printBase64Binary(this.pdf);
    }

    public void close() throws Exception {
        this.getBase64PDFBytes();
    }

    public void print(String text) throws PrinterException {
        this.contentDrawer.print(text);
    }

    public PDFQuill printImage(ByteArrayInputStream imgBytes) {
        this.contentDrawer.printImage(imgBytes);
        return this;
    }

    public PDFQuill printBarcode(String code, BarcodeType barcodeType) throws PrinterException {
        return this.printBarcode(code, barcodeType, 0, 0);
    }

    public PDFQuill printBarcode(String code, BarcodeType barcodeType, int height, int width) throws PrinterException {
        this.contentDrawer.printBarcode(code, barcodeType, height, width);
        return this;
    }

    public PDFQuill cutSignal() throws PrinterException {
        this.contentDrawer.cutSignal();
        return this;
    }

    private static PermissionSettings copyPermissionSettings(PermissionSettings source) {
        PermissionSettings copy = new PermissionSettings();
        copy.setCanPrint(source.isCanPrint());
        copy.setCanModify(source.isCanModify());
        copy.setCanExtractContent(source.isCanExtractContent());
        return copy;
    }

    private static FontSettings copyFontSettings(FontSettings source) {
        FontSettings copy = new FontSettings();
        copy.setFontSize(source.getFontSize());
        copy.setDefaultFont(source.getDefaultFont());
        copy.setBoldFont(source.getBoldFont());
        copy.setItalicFont(source.getItalicFont());
        copy.setBoldItalicFont(source.getBoldItalicFont());
        return copy;
    }

    public static final class Builder {
        private PaperType paperType;
        private boolean preserveSpaces;
        private PermissionSettings permissionSettings;
        private Consumer<PermissionSettings> permissionSettingsCustomizer;
        private PageLayout pageLayout;
        private FontSettings fontSettings;
        private Consumer<FontSettings> fontSettingsCustomizer;

        public Builder withPaperType(PaperType paperType) {
            if (paperType == null) {
                throw new IllegalArgumentException("paperType cannot be null");
            }
            this.paperType = paperType;
            return this;
        }

        public Builder preserveSpaces(boolean preserveSpaces) {
            this.preserveSpaces = preserveSpaces;
            return this;
        }

        public Builder withPermissionSettings(PermissionSettings permissionSettings) {
            this.permissionSettings = permissionSettings;
            return this;
        }

        public Builder configurePermissionSettings(Consumer<PermissionSettings> permissionSettingsCustomizer) {
            this.permissionSettingsCustomizer = permissionSettingsCustomizer;
            return this;
        }

        public Builder withPageLayout(PageLayout pageLayout) {
            this.pageLayout = pageLayout;
            return this;
        }

        public Builder withFontSettings(FontSettings fontSettings) {
            this.fontSettings = fontSettings;
            return this;
        }

        public Builder configureFontSettings(Consumer<FontSettings> fontSettingsCustomizer) {
            this.fontSettingsCustomizer = fontSettingsCustomizer;
            return this;
        }

        public PDFQuill build() {
            return new PDFQuill(this);
        }
    }
}
