package com.caio.paper;

import java.util.EnumSet;

public class PaperUtils {

    private static final EnumSet<PaperType> THERMAL_PAPER_TYPES = EnumSet.of(
            PaperType.THERMAL_80MM,
            PaperType.THERMAL_64MM,
            PaperType.THERMAL_58MM,
            PaperType.THERMAL_56MM,
            PaperType.THERMAL_42MM
    );

    private static final EnumSet<PaperType> NON_THERMAL_PAPER_TYPES = EnumSet.complementOf(THERMAL_PAPER_TYPES);

    private PaperUtils() {
        // utility class
    }

    public static EnumSet<PaperType> getThermalPaperTypes() {
        return EnumSet.copyOf(THERMAL_PAPER_TYPES);
    }

    public static EnumSet<PaperType> getNonThermalPaperTypes() {
        return EnumSet.copyOf(NON_THERMAL_PAPER_TYPES);
    }

    public static boolean isNotThermal(PaperType paperType) {
        return paperType != null && NON_THERMAL_PAPER_TYPES.contains(paperType);
    }

    public static boolean isThermal(PaperType paperType) {
        return paperType != null && THERMAL_PAPER_TYPES.contains(paperType);
    }
}
