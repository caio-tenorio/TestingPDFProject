package org.pdfquill.barcode;

import com.google.zxing.BarcodeFormat;

/**
 * Helper methods for converting {@link BarcodeType} values into ZXing artefacts.
 */
public final class BarcodeUtils {

    private BarcodeUtils() {
        // utility class
    }

    /**
     * @param barcodeType type to inspect
     * @return {@code true} when the supplied type is a QR Code
     */
    public static boolean isQrCode(BarcodeType barcodeType) {
        return BarcodeType.QRCODE.equals(barcodeType);
    }

    /**
     * Maps the domain-specific {@link BarcodeType} enumeration to ZXing's {@link BarcodeFormat}.
     * Defaults to CODE_128 when {@code null} is supplied.
     *
     * @param barcodeType desired barcode type
     * @return ZXing format matching the supplied type
     */
    public static BarcodeFormat getBarcodeFormat(BarcodeType barcodeType) {
        if (barcodeType == null) {
            return BarcodeFormat.CODE_128;
        }

        switch (barcodeType) {
            case UPCA:
                return BarcodeFormat.UPC_A;
            case UPCE:
                return BarcodeFormat.UPC_E;
            case EAN8:
                return BarcodeFormat.EAN_8;
            case EAN13:
                return BarcodeFormat.EAN_13;
            case INTERLEAVED2OF5:
                return BarcodeFormat.ITF;
            case CODE128:
                return BarcodeFormat.CODE_128;
            case CODABAR:
                return BarcodeFormat.CODABAR;
            case CODE39:
                return BarcodeFormat.CODE_39;
            case QRCODE:
                return BarcodeFormat.QR_CODE;
            default:
                throw new IllegalStateException("Unsupported barcode type: " + barcodeType);
        }
    }
}
