package org.pdfquill.paper;

import java.util.EnumSet;

/**
 * Utility helpers related to {@link PaperType} categorisation.
 */
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

    /**
     * @return immutable copy of the thermal paper types supported by the library
     */
    public static EnumSet<PaperType> getThermalPaperTypes() {
        return EnumSet.copyOf(THERMAL_PAPER_TYPES);
    }

    /**
     * @return immutable copy of the non-thermal paper types
     */
    public static EnumSet<PaperType> getNonThermalPaperTypes() {
        return EnumSet.copyOf(NON_THERMAL_PAPER_TYPES);
    }

    /**
     * Checks whether the provided paper type is not thermal.
     *
     * @param paperType type to inspect
     * @return {@code true} when the type is non-thermal
     */
    public static boolean isNotThermal(PaperType paperType) {
        return paperType != null && NON_THERMAL_PAPER_TYPES.contains(paperType);
    }

    /**
     * Checks whether the provided paper type is thermal.
     *
     * @param paperType type to inspect
     * @return {@code true} when the type is thermal
     */
    public static boolean isThermal(PaperType paperType) {
        return paperType != null && THERMAL_PAPER_TYPES.contains(paperType);
    }
}
