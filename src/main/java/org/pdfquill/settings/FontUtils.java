package org.pdfquill.settings;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.pdfquill.settings.font.FontType;

import java.io.IOException;

public class FontUtils {
    public static float getTextWidth(String text, PDType1Font font, int fontSize) throws IOException {
        return font.getStringWidth(text) * fontSize / 1000f;
    }

    public static int lastWhitespaceBetween(String s, int from, int toInclusive) {
        for (int i = toInclusive; i >= from; i--) {
            if (Character.isWhitespace(s.charAt(i))) return i;
        }
        return -1;
    }
}
