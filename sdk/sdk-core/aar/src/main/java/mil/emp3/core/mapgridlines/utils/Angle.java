package mil.emp3.core.mapgridlines.utils;

import mil.emp3.api.global;

/**
 * This class provide a series of convenience methos for angles.
 */

public class Angle {

    /**
     * This static method converts radians to degrees
     * @param radians The angle in radians
     * @return The angle in degrees.
     * @throws IllegalArgumentException if the argument is NaN or Infinity.
     */
    public static double fromRadians(double radians) {
        if (Double.isNaN(radians) || Double.isInfinite(radians)) {
            throw new IllegalArgumentException("Argument can not be NaN or Infinity.");
        }
        return Math.toDegrees(radians);
    }

    /**
     * This static method normalizes the angle to a value between -90 and 90.
     * @param degrees The angle in degrees
     * @return A normalized latitude in degrees.
     * @throws IllegalArgumentException if the argument is NaN or Infinity.
     */
    public static double normalizeLatitude(double degrees) {
        if (Double.isNaN(degrees) || Double.isInfinite(degrees)) {
            throw new IllegalArgumentException("Argument can not be NaN or Infinity.");
        }
        return global.modulus(degrees + 90.0, 180.0) - 90.0;
    }

    /**
     * This static method normalizes the angle to a value between -180 and 180.
     * @param degrees The angle in degrees
     * @return A normalized longitude in degrees.
     * @throws IllegalArgumentException if the argument is NaN or Infinity.
     */
    public static double normalizeLongitude(double degrees) {
        if (Double.isNaN(degrees) || Double.isInfinite(degrees)) {
            throw new IllegalArgumentException("Argument can not be NaN or Infinity.");
        }
        return global.modulus(degrees + 180.0, 360.0) - 180.0;
    }
}
