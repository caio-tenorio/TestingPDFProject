package org.pdfquill.barcode;

/**
 * Available barcode encodings supported by the rendering utilities.
 */
public enum BarcodeType {
    UPCA(1),
    UPCE(2),
    EAN8(3),
    EAN13(4),
    INTERLEAVED2OF5(5),
    CODE128(6),
    CODABAR(7),
    CODE39(8),
    QRCODE(9);

    private final int value;

    BarcodeType(int value) {
        this.value = value;
    }

    /**
     * @return numeric identifier associated with the enum constant
     */
    public int getValue() {
        return value;
    }

    /**
     * Resolves the enum constant for the provided numeric identifier.
     *
     * @param value numeric representation
     * @return matching {@link BarcodeType} or {@code null} when not found
     */
    public static BarcodeType valueOf(int value) {
        for (BarcodeType type : values()) {
            if (type.getValue() == value) {
                return type;
            }
        }
        return null;
    }
}
