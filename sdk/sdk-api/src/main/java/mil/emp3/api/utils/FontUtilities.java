package mil.emp3.api.utils;

import android.content.res.Resources;

import org.cmapi.primitives.IGeoLabelStyle;

import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.global;

/**
 * This class provides method for various conversion used thru out the system.
 */

public class FontUtilities {
    public static final float DEFAULT_FONT_POINT_SIZE = 10.0f;
    public static final double PIXEL_PER_POINT = Resources.getSystem().getDisplayMetrics().densityDpi / 72.0;

    /**
     * This method gives the font size in pixels
     * @param pointSize
     * @return
     */
    public static int fontPointsToPixels(float pointSize) {
        return (int) (PIXEL_PER_POINT * pointSize);
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
        float fontPoint;

        if ((null != labelStyle) && (labelStyle.getSize() > 0.0)) {
            fontPoint = (float) labelStyle.getSize();
        } else {
            fontPoint = FontUtilities.DEFAULT_FONT_POINT_SIZE;
        }

        fontSize = FontUtilities.fontPointsToPixels(fontPoint);
        fontSize *= fontScale;

        return fontSize;
    }

    /**
     * This method gives the text size in pixels.
     * @param pointSize
     * @param fontSizeModifier
     * @return
     */
    public static int getTextPixelSize(float pointSize, FontSizeModifierEnum fontSizeModifier) {
        float fontScale = fontSizeModifier.getScaleValue();
        int fontSize;
        float fontPoint;

        if (pointSize > 0) {
            fontPoint = pointSize;
        } else {
            fontPoint = FontUtilities.DEFAULT_FONT_POINT_SIZE;
        }

        fontSize = FontUtilities.fontPointsToPixels(fontPoint);
        fontSize *= fontScale;

        return fontSize;
    }
}
