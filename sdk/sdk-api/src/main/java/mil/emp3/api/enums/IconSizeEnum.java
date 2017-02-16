package mil.emp3.api.enums;

/**
 * These are the icon sizes supported by the system. Actual pixel size is not used due
 * to the difference in pixel density of android devices.
 */
public enum IconSizeEnum {
    /**
     * Smallest size possible. It should match 0.6 the size of a SMALL icon.
     */
    TINY,
    /**
     * Size equivalent to a 1/3 in X 1/3 in icon. Which on a standard 96dpi screen is approximately 32x32.
     */
    SMALL,
    /**
     * Size equivalent to 1.4 times the size of a SMALL icon.
     */
    MEDIUM,
    /**
     * Size equivalent to 1.8 times the size of a SMALL icon.
     */
    LARGE;

    /**
     * This method returns the scaling factor associated with the enumerated value.
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
