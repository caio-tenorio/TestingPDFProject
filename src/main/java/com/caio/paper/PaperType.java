package com.caio.paper;

public enum PaperType {
    A0(2383.937F, 3370.3938F),
    A1(1683.7795F, 2383.937F),
    A2(1190.5513F, 1683.7795F),
    A3(841.8898F, 1190.5513F),
    A4(595.27563F, 841.8898F),
    A5(419.52756F, 595.27563F),
    A6(297.63782F, 419.52756F),
    LETTER(612.0F, 792.0F),
    LEGAL(612.0F, 1008.0F),
    TABLOID(792, 1224),
    THERMAL_80MM(227, 5669),
    THERMAL_58MM(164, 5669),
    THERMAL_64MM(181, 5669),
    THERMAL_56MM(159, 5669),
    THERMAL_42MM(119, 5669);

    private final float width;
    private final float height;

    PaperType(float width, float height) {
        this.width = width;
        this.height = height;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}