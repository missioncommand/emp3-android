package mil.emp3.api.utils;

import android.util.Log;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.IGeoColor;

/**
 *
 */
public class ColorUtils {
    public static String colorToString(IGeoColor oColor) {
        byte[] colorBytes = new byte[4];
        StringBuffer sb = new StringBuffer();
        colorBytes[0] = (byte) (oColor.getAlpha() * 255);
        colorBytes[1] = (byte) oColor.getRed();
        colorBytes[2] = (byte) oColor.getGreen();
        colorBytes[3] = (byte) oColor.getBlue();
        for (byte b : colorBytes) {
            if (b == 0) {
                sb.append("00");
            } else {
                sb.append(Integer.toHexString((int) (b & 0xff)));
            }
        }
        return sb.toString();
    }

    public static String colorToHashString(IGeoColor oColor) {
        byte[] colorBytes = new byte[4];
        StringBuffer sb = new StringBuffer("#");
        colorBytes[0] = (byte) (oColor.getAlpha() * 255);
        colorBytes[1] = (byte) oColor.getRed();
        colorBytes[2] = (byte) oColor.getGreen();
        colorBytes[3] = (byte) oColor.getBlue();
        for (byte b : colorBytes) {
            if (b == 0) {
                sb.append("00");
            } else {
                sb.append(Integer.toHexString((int) (b & 0xff)));
            }
        }
        return sb.toString();
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
}
