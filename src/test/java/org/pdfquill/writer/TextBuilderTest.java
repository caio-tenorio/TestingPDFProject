package org.pdfquill.writer;

import org.junit.jupiter.api.Test;
import org.pdfquill.settings.font.FontSettings;

import static org.assertj.core.api.Assertions.assertThat;

class TextBuilderTest {

    @Test
    void addTextTracksMaxFontSize() {
        FontSettings small = new FontSettings();
        small.setFontSize(10);

        FontSettings large = new FontSettings();
        large.setFontSize(18);

        TextBuilder builder = new TextBuilder()
                .addText(new Text("small", small))
                .addText(new Text("large", large));

        assertThat(builder.getTextList()).hasSize(2);
        assertThat(builder.getMaxFontSize()).isEqualTo(18);
    }
}
