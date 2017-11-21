package mil.emp3.api.utils;

import org.cmapi.primitives.GeoColor;

/**
 * This class extends the geo notation lib GeoColor class. It provides a constructors that take color
 * parameters. These make it cleaner to allocate a color and assign values in one declaration.
 */
public class EmpGeoColor extends GeoColor {
    /**
     * This constructor assigns all the color values required.
     * @param alpha - a double value between 0 - 1.0
     * @param red - an integer value between 0 - 255.
     * @param green - an integer value between 0 - 255.
     * @param blue - an integer value between 0 - 255.
     */
    public EmpGeoColor(double alpha, int red, int green, int blue) {
        super.setAlpha(alpha);
        super.setRed(red);
        super.setGreen(green);
        super.setBlue(blue);
    }

    /**
     * This constructor assings all the color values required. The alpha setting remains the default.
     * @param red - an integer value between 0 - 255.
     * @param green - an integer value between 0 - 255.
     * @param blue - an integer value between 0 - 255.
     */
    public EmpGeoColor(int red, int green, int blue) {
        super.setRed(red);
        super.setGreen(green);
        super.setBlue(blue);
    }
}
