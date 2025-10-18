package org.pdfquill.measurements;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class MeasurementUtilsTest {

    @Test
    void mmToPtConvertsMillimetresToPoints() {
        assertThat(MeasurementUtils.mmToPt(25.4f)).isCloseTo(72f, within(1e-6f));
    }
}
