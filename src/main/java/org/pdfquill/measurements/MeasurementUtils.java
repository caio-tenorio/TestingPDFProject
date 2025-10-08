package org.pdfquill.measurements;

/**
 * Collection of conversion helpers between common measurement units.
 */
public final class MeasurementUtils {
    private MeasurementUtils() {
        // utility class
    }

    /**
     * Converts millimetres to PDF points.
     *
     * @param mm measurement in millimetres
     * @return value converted to points
     */
    public static float mmToPt(float mm) {
        return (72f * mm) / 25.4f;
    }
}
