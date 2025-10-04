package org.example.barcode;

import com.google.zxing.BarcodeFormat;

public class BarcodeUtils {

    private BarcodeUtils() {
        // utility class
    }

    public static boolean isQrCode(BarcodeType barcodeType) {
        return BarcodeType.QRCODE.equals(barcodeType);
    }

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
