# PDF Quill

Java library focused on generating print-ready PDFs for receipts, tickets, and other compact documents with fine-grained control over layout, typography, barcodes, and cut marks for both thermal and standard printers.

## Key Features
- PDF generation powered by Apache PDFBox with declarative layout configuration (margins, printable area, line height, lines per page)
- Support for multiple paper formats (`A4`, `A5`, `THERMAL_56MM`, and more) with thermal paper detection for smart cropping
- Text printing with automatic word wrapping, mixed font styles per line through `TextBuilder`, optional whitespace preservation, and cut signals via `cutSignal`
- Image and barcode/QR Code rendering using ZXing through `printImage` and `printBarcode`
- Font customization (`FontSettings`) and basic PDF permission control (`PermissionSettings`)
- Quick Base64 export (`getBase64PDFBytes`) for easy transport or storage

## Requirements
- JDK 21+
- Maven 3.9+

## Build
```bash
mvn clean package
```
The compiled artifact will be available at `target/pdf-quill-1.0-SNAPSHOT.jar`. Run `mvn install` to publish it into the local Maven cache and consume it from other Maven or Gradle projects.

## Quick Start

```java
import java.io.IOException;

import org.pdfquill.PDFQuill;
import org.pdfquill.exceptions.PrinterException;
import org.pdfquill.barcode.BarcodeType;
import org.pdfquill.paper.PaperType;
import org.pdfquill.settings.font.FontSettings;

FontSettings fontSettings = new FontSettings();
fontSettings.setFontSize(10);

PDFQuill quill = PDFQuill.builder()
        .withPaperType(PaperType.THERMAL_56MM)
        .withFontSettings(fontSettings)
        .preserveSpaces(true)
        .build();

try {
    quill.printLine("Sample Store");
    quill.printLine("Full address line");
    quill.printBarcode("123456789012", BarcodeType.CODE128);
    quill.cutSignal();

    String pdfBase64 = quill.getBase64PDFBytes();
    // send pdfBase64 to printer, API, etc.
} catch (PrinterException | IOException e) {
    // handle failure (retry, log, etc.)
}
```

## Rich Text Blocks

For multi-style lines (bold headers, different sizes, mixed fragments), build a `TextBuilder` and send it to `writeFromTextBuilder`. The writer now handles wrapping per fragment without mutating the original list.

```java
import java.io.IOException;

import org.pdfquill.Text;
import org.pdfquill.TextBuilder;
import org.pdfquill.settings.font.FontSettings;
import org.pdfquill.settings.font.FontType;

FontSettings titleFont = new FontSettings();
titleFont.setSelectedFont(titleFont.getFontByFontType(FontType.BOLD));
titleFont.setFontSize(16);

TextBuilder builder = new TextBuilder()
        .addText(new Text("Subtotal: ", new FontSettings()))
        .addText(new Text("R$ 29,90", titleFont))
        .addText(new Text(" (promo)", new FontSettings()));

try {
    quill.writeFromTextBuilder(builder);
} catch (IOException e) {
    // handle layout or rendering failure
}
```

## Configuration Tips
- **Fonts**: tweak default/bold/italic fonts via `FontSettings` or rely on `configureFontSettings` for inline customization in the builder.
- **Layout**: instantiate `PageLayout` manually or combine `withPaperType` with `withPageLayout` to customize margins, line height, and maximum line width.
- **Permissions**: enable or disable printing, editing, and content extraction with `withPermissionSettings` or `configurePermissionSettings`.
- **Whitespace**: call `preserveSpaces(true)` to keep leading spaces, which is handy for manual alignment in receipts.
- **Images**: `printImage` accepts a `ByteArrayInputStream`; convert files using `Files.readAllBytes(path)`.

## Dependencies
- [Apache PDFBox](https://pdfbox.apache.org/) for PDF rendering
- [ZXing](https://github.com/zxing/zxing) for barcode and QR Code generation
- `javax.xml.bind` for Base64 encoding
