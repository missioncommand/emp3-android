package mil.emp3.api.enums;

/**
 * This class enumerate the icon sizes suported by the system. Actual pixel size is not used do
 * to the difference in pixel density of android devices.
 */
public enum IconSizeEnum {
    /**
     * This enumerated value indicates the smallest size possible. It should match 0.6 the size of a SMALL icon.
     */
    TINY,
    /**
     * This enumerated value indicates a size equivalent to a 1/3 in X 1/3 in icon. Which on a standard 96dpi screen is ~32x32.
     */
    SMALL,
    /**
     * This enumerated value indicates a size equivalent to 1.4 the size of a SMALL icon.
     */
    MEDIUM,
    /**
     * This enumerated value indicates a size equivalent to 1.8 the size of a SMALL icon.
     */
    LARGE;

    /**
     * This method return the scaling factor associated with the enumerated value.
     * @return scale factor.
     */
    public double getScaleFactor() {
        double dRet = 1.0;

        switch (this) {
            case TINY:
                dRet = 0.7; // 70% of the original size.
                break;
            case SMALL:
                dRet = 1.0; // 100% of the original size.
                break;
            case MEDIUM:
                dRet = 1.4; // 140% of the original size.
                break;
            case LARGE:
                dRet = 1.8; // 180% of the original size.
                break;
        }

        return dRet;
    }
}
