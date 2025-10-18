package org.pdfquill.paper;

import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class PaperUtilsTest {

    @Test
    void isThermalRecognisesConfiguredTypes() {
        assertThat(PaperUtils.isThermal(PaperType.THERMAL_58MM)).isTrue();
        assertThat(PaperUtils.isThermal(PaperType.A4)).isFalse();
        assertThat(PaperUtils.isNotThermal(PaperType.A4)).isTrue();
    }

    @Test
    void getThermalPaperTypesReturnsDefensiveCopy() {
        EnumSet<PaperType> snapshot = PaperUtils.getThermalPaperTypes();
        snapshot.add(PaperType.A4);

        EnumSet<PaperType> freshSnapshot = PaperUtils.getThermalPaperTypes();

        assertThat(freshSnapshot).doesNotContain(PaperType.A4);
    }
}
