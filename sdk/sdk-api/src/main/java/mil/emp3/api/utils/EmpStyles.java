package mil.emp3.api.utils;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

/**
 * Underlying CMAPI supports multiple style objects that controle the appearance of the features rendered on the map,
 *     GeoStrokeStyle
 *     GeoFillStyle
 *     GeiLabelStyle
 *
 * EmpStyles is a utility class that provided convenience methods for building these objects and providing a String representation
 * of their members.
 */
public class EmpStyles {

    /**
     * Convert GeoFillStyle to String
     * @param geoFillStyle
     * @return
     */
    public static String toString(IGeoFillStyle geoFillStyle) {
        if(null == geoFillStyle) {
            return "FillStyle ";
        }
        String fillColor = "FillStyle " + toString(geoFillStyle.getFillColor());
        if(null != geoFillStyle.getFillPattern()) {
            return fillColor + " Pattern " + geoFillStyle.getFillPattern() + " ";
        }
        return fillColor + " Pattern ";
    }

    /**
     * Convert GeoColor to String
     * @param geoColor
     * @return
     */
    public static String toString(IGeoColor geoColor) {
        if(null == geoColor) {
            return "Color ";
        } else {
            return "Color R:" + geoColor.getRed() + " G:" + geoColor.getGreen() + " B:" + geoColor.getBlue() + " A:" + geoColor.getAlpha() + " ";
        }
    }

    /**
     * Convert Stroke Style to String
     * @param geoStrokeStyle
     * @return
     */
    public static String toString(IGeoStrokeStyle geoStrokeStyle) {
        if(null == geoStrokeStyle) {
            return "StrokeStyle ";
        }

        String strokeColor = "StrokeStyle " + toString(geoStrokeStyle.getStrokeColor());
        return strokeColor + "W:" + geoStrokeStyle.getStrokeWidth() + " SF:" + geoStrokeStyle.getStipplingFactor() +
                " SP:" + Integer.toBinaryString(geoStrokeStyle.getStipplingPattern()) + " ";
        // When interpreting the stippling pattern output consider negative numbers and 2s complement.
    }

    /**
     * Build a Stroke Style object
     * @param red
     * @param green
     * @param blue
     * @param alpha
     * @param width
     * @param stipplingFactor
     * @param stipplingPattern
     * @return
     */
    public static IGeoStrokeStyle buildStrokeStyle(int red, int green, int blue, double alpha, double width, int stipplingFactor, short stipplingPattern) {
        IGeoStrokeStyle geoStrokeStyle = new GeoStrokeStyle();
        IGeoColor geoColor = new GeoColor();
        geoColor.setRed(red);
        geoColor.setGreen(green);
        geoColor.setBlue(blue);
        geoStrokeStyle.setStrokeColor(geoColor);

        geoStrokeStyle.setStrokeWidth(width);
        geoStrokeStyle.setStipplingFactor(stipplingFactor);
        geoStrokeStyle.setStipplingPattern(stipplingPattern);
        return geoStrokeStyle;
    }

    /**
     * This method copies a renderer color to a geo color.
     * @param toColor       The geo color to copy to.
     * @param fromColor     the renderer color to copy from.
     */
    public static void copyColor(IGeoColor toColor, armyc2.c2sd.renderer.utilities.Color fromColor) {
        if ((null == toColor) || (null == fromColor)) {
            throw new IllegalArgumentException("toColor and fromColor can not be null.");
        }

        toColor.setAlpha((double) fromColor.getAlpha() / 255.0);
        toColor.setRed(fromColor.getRed());
        toColor.setGreen(fromColor.getGreen());
        toColor.setBlue(fromColor.getBlue());
    }

    /**
     * This method copies a geo color to another geo color.
     * @param toColor       The geo color to copy to.
     * @param fromColor     the geo color to copy from.
     */
    public static void copyColor(IGeoColor toColor, IGeoColor fromColor) {
        if ((null == toColor) || (null == fromColor)) {
            throw new IllegalArgumentException("toColor and fromColor can not be null.");
        }

        toColor.setAlpha(fromColor.getAlpha());
        toColor.setRed(fromColor.getRed());
        toColor.setGreen(fromColor.getGreen());
        toColor.setBlue(fromColor.getBlue());
    }
}
