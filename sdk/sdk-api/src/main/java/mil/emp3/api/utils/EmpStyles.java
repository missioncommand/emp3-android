package mil.emp3.api.utils;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

public class EmpStyles {
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

    public static String toString(IGeoColor geoColor) {
        if(null == geoColor) {
            return "Color ";
        } else {
            return "Color R:" + geoColor.getRed() + " G:" + geoColor.getGreen() + " B:" + geoColor.getBlue() + " A:" + geoColor.getAlpha() + " ";
        }
    }

    public static String toString(IGeoStrokeStyle geoStrokeStyle) {
        if(null == geoStrokeStyle) {
            return "StrokeStyle ";
        }

        String strokeColor = "StrokeStyle " + toString(geoStrokeStyle.getStrokeColor());
        return strokeColor + "W:" + geoStrokeStyle.getStrokeWidth() + " SF:" + geoStrokeStyle.getStipplingFactor() +
                " SP:" + geoStrokeStyle.getStipplingPattern() + " ";
    }

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
}
