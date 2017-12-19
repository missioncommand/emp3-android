package mil.emp3.api.utils;

import android.util.Log;
import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.IGeoColor;

import armyc2.c2sd.renderer.utilities.Color;

/**
 *
 */
public class ColorUtils {

    private static int COLOR_CONVERTER_INT_TO_DOUBLE = 255;

    public static String colorToString(IGeoColor oColor) {
        int iTemp;
        String sTemp1;
        String sStr = "";
        
        iTemp = (int) (oColor.getAlpha() * 255);
        sTemp1 = Integer.toHexString(iTemp);
        if (sTemp1.length() == 1) {
            sTemp1 = "0" + sTemp1;
        }
        sStr += sTemp1;
        
        iTemp = (int) oColor.getRed();
        sTemp1 = Integer.toHexString(iTemp);
        if (sTemp1.length() == 1) {
            sTemp1 = "0" + sTemp1;
        }
        sStr += sTemp1;
        
        iTemp = (int) oColor.getGreen();
        sTemp1 = Integer.toHexString(iTemp);
        if (sTemp1.length() == 1) {
            sTemp1 = "0" + sTemp1;
        }
        sStr += sTemp1;
        
        iTemp = (int) oColor.getBlue();
        sTemp1 = Integer.toHexString(iTemp);
        if (sTemp1.length() == 1) {
            sTemp1 = "0" + sTemp1;
        }
        sStr += sTemp1;
        
        return sStr;
    }

    /**
     * Returns the IGeoColor of the given hex string
     * @param hexColorValue - Hex color in String form. Assuming the form of #RRGGBB
     * @return - IGeoColor object representing the given string.
     */
    public static IGeoColor stringToColor(final String hexColorValue) {
        final int radix = 16;
        if (hexColorValue.length() == 7) {
            try {
                int red = Integer.valueOf(hexColorValue.substring(1, 3), radix);
                int green = Integer.valueOf(hexColorValue.substring(3, 5), radix);
                int blue = Integer.valueOf(hexColorValue.substring(5, 7), radix);
                final IGeoColor geoColor = new GeoColor();
                geoColor.setRed(red);
                geoColor.setGreen(green);
                geoColor.setBlue(blue);
                return geoColor;
            } catch (NumberFormatException numberFormatException) {
                Log.e("Error", "RGB value string did not map to a color. Returning black.");
            }
        }
        else {
            Log.e("Error", "RGB value string was not formatted correctly. Returning black.");
        }
        // Default return black.
        return new EmpGeoColor(0, 0 , 0);
    }

    /**
     * Returns an SEC renderer {@link Color} from a cmapi {@link IGeoColor}
     * @param iGeoColor {@link IGeoColor} to convert
     * @return {@link Color} converted cmapi color.
     */
    public static Color CmapiColorToRendererColor(final IGeoColor iGeoColor) {
        final Double alphaChannel = iGeoColor.getAlpha() * 255.0;
        return new Color(iGeoColor.getRed(), iGeoColor.getGreen(), iGeoColor.getBlue(), alphaChannel.intValue());
    }

    /**
     * Returns a cmapi {@link IGeoColor} from the SEC renderer {@link Color}
     * @param color {@link Color} to convert.
     * @return {@link IGeoColor} converted SEC renderer color.
     */
    public static IGeoColor RendererColorToCmapiColor(final Color color) {
        final double alphaChannel  = ((double)color.getAlpha() / COLOR_CONVERTER_INT_TO_DOUBLE);
        return new EmpGeoColor(alphaChannel, color.getRed(), color.getGreen(), color.getBlue());
    }
}
