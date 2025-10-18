package org.pdfquill.settings.font;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FontSettingsTest {

    @Test
    void copyProducesIndependentInstance() {
        FontSettings original = new FontSettings();
        original.setFontSize(14);
        original.setDefaultFont(PDType1Font.HELVETICA);
        original.loadFontMap();

        FontSettings copy = original.copy();

        assertThat(copy).isNotSameAs(original);
        assertThat(copy.getFontSize()).isEqualTo(14);
        assertThat(copy.getFontByFontType(FontType.DEFAULT)).isEqualTo(PDType1Font.HELVETICA);

        original.setFontSize(9);
        original.setDefaultFont(PDType1Font.COURIER);
        original.loadFontMap();

        assertThat(copy.getFontSize()).isEqualTo(14);
        assertThat(copy.getFontByFontType(FontType.DEFAULT)).isEqualTo(PDType1Font.HELVETICA);
    }
}
