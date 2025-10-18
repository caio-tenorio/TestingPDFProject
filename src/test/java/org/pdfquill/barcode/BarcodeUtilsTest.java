package org.pdfquill.barcode;

import com.google.zxing.BarcodeFormat;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BarcodeUtilsTest {

    @Test
    void getBarcodeFormatDefaultsToCode128WhenNull() {
        assertThat(BarcodeUtils.getBarcodeFormat(null)).isEqualTo(BarcodeFormat.CODE_128);
    }

    @Test
    void isQrCodeMatchesExpectedType() {
        assertThat(BarcodeUtils.isQrCode(BarcodeType.QRCODE)).isTrue();
        assertThat(BarcodeUtils.isQrCode(BarcodeType.CODE128)).isFalse();
    }
}
