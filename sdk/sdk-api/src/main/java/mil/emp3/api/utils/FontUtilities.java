package mil.emp3.api.utils;

import android.content.res.Resources;

import org.cmapi.primitives.IGeoLabelStyle;

import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.global;

/**
 * This class provides method for various conversion used thru out the system.
 */

public class FontUtilities {
    public static final int DEFAULT_FONT_POINT_SIZE = 10;
    public static final double PIXEL_PER_POINT = Resources.getSystem().getDisplayMetrics().densityDpi / 72.0;

    /**
     * This method gives the font size in pixels
     * @param points
     * @return
     */
    public static int fontPointsToPixels(int points) {
        return (int) (PIXEL_PER_POINT * (double) points);
    }

    /**
     * This method gives the text size in pixels.
     * @param labelStyle
     * @param fontSizeModifier
     * @return
     */
    public static int getTextPixelSize(IGeoLabelStyle labelStyle, FontSizeModifierEnum fontSizeModifier) {
        float fontScale = fontSizeModifier.getScaleValue();
        int fontSize;
        int fontPoint;

        if (null != labelStyle) {
            fontPoint = (int) labelStyle.getSize();
        } else {
            fontPoint = FontUtilities.DEFAULT_FONT_POINT_SIZE;
        }

        fontSize = FontUtilities.fontPointsToPixels(fontPoint);
        fontSize *= fontScale;

        return fontSize;
    }
}
