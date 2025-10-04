package org.example.paper;

public enum PaperType {
    A0(2384, 3370),
    A1(1684, 2384),
    A2(1191, 1684),
    A3(842, 1191),
    A4(595, 842),
    A5(420, 595),
    A6(297, 420),
    LETTER(612, 792),
    LEGAL(612, 1008),
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