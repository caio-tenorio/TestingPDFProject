package com.caio.measurements;

public class MeasurementUtils {
    /**
     *
     * @param mm
     * @return
     */
    public static float mmToPt(float mm) {
        return (72f * mm) / 25.4f;
    }
}
