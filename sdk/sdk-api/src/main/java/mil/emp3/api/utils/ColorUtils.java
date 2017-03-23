package mil.emp3.api.utils;

import org.cmapi.primitives.IGeoColor;

/**
 *
 */
public class ColorUtils {
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
}
