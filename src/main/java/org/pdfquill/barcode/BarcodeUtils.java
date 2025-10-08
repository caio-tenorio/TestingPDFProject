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

        return switch (barcodeType) {
            case UPCA -> BarcodeFormat.UPC_A;
            case UPCE -> BarcodeFormat.UPC_E;
            case EAN8 -> BarcodeFormat.EAN_8;
            case EAN13 -> BarcodeFormat.EAN_13;
            case INTERLEAVED2OF5 -> BarcodeFormat.ITF;
            case CODE128 -> BarcodeFormat.CODE_128;
            case CODABAR -> BarcodeFormat.CODABAR;
            case CODE39 -> BarcodeFormat.CODE_39;
            case QRCODE -> BarcodeFormat.QR_CODE;
        };
    }
}
