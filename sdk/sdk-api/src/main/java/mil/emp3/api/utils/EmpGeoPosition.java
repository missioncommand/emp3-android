package mil.emp3.api.utils;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;
import java.util.Locale;

import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;

/**
 * Convenience class for for creating a GeoPosition obkect.
 */

public class EmpGeoPosition extends GeoPosition {
    private final static String TAG = EmpGeoPosition.class.getSimpleName();
    /**
     * Creates EmpGeoPosition object from latitude and longitude. Sets altitude to 0.
     * @param latitude
     * @param longitude
     */
    public EmpGeoPosition(double latitude, double longitude) {
        setLatitude(latitude);
        setLongitude(longitude);
        setAltitude(0);
    }

    /**
     * Creates EmpGeoPosition object from latitude, longitude, and altitude.
     * @param latitude
     * @param longitude
     * @param altitude
     */
    public EmpGeoPosition(double latitude, double longitude, double altitude) {
        setLatitude(latitude);
        setLongitude(longitude);
        setAltitude(altitude);
    }

    /**
     * Copies latitude, longitude and altitude.
     * @param geoPosition
     */
    public EmpGeoPosition(IGeoPosition geoPosition) {
        setLatitude(geoPosition.getLatitude());
        setLongitude(geoPosition.getLongitude());
        setAltitude(geoPosition.getAltitude());
    }

    /**
     * Sets position form camera
     * @param camera
     */
    public EmpGeoPosition(ICamera camera) {
        setLatitude(camera.getLatitude());
        setLongitude(camera.getLongitude());
        setAltitude(camera.getAltitude());
    }

    public EmpGeoPosition() {

    }

    /**
     * Returns true if latitude, longitude and altitude are valid
     * @return
     */
    public boolean isValid() {
        return validate(getLatitude(), getLongitude(), getAltitude());
    }

    /**
     * Returns true if latitude, longitude and altitude are valid
     * @param latitude
     * @param longitude
     * @param altitude
     * @return
     */
    public static boolean validate(double latitude, double longitude, double altitude) {
        if(Double.isNaN(latitude) || latitude < global.LATITUDE_MINIMUM || latitude > global.LATITUDE_MAXIMUM) {
            Log.e(TAG, "Invalid Latitude " + latitude);
            return false;
        }
        if(Double.isNaN(longitude) || longitude < global.LONGITUDE_MINIMUM || longitude > global.LONGITUDE_MAXIMUM) {
            Log.e(TAG, "Invalid Longitude " + longitude);
            return false;
        }
        if(Double.isNaN(altitude)) {
            Log.e(TAG, "Invalid altitude " + altitude);
            return false;
        }
        return true;
    }

    /**
     * Returns true if all components of the position are valid
     * @param position
     * @return
     */
    public static boolean validate(IGeoPosition position) {
        return validate(position.getLatitude(), position.getLongitude(), position.getAltitude());
    }

    /**
     * Converts position to String consisting of latitude longitude and altitude
     * @param position
     * @return
     */
    public static String toString(IGeoPosition position) {
        if(null != position) {
            return(String.format(Locale.US, "L %1$6.3f N %2$6.3f A %3$6.0f",
                    position.getLatitude(), position.getLongitude(), position.getAltitude()));
        } else {
            return "null position";
        }
    }

    // Following methods were copied from NASA World Wind Location.java class. These methods were minimally adjusted to use EMP classes.

    /**
     * Restricts an angle to the range [-90, +90] degrees, wrapping angles outside the range. Wrapping takes place along
     * a line of constant longitude which may pass through the poles. In which case, 135 degrees normalizes to 45
     * degrees; 181 degrees normalizes to -1 degree.
     *
     * @param degrees the angle to wrap in degrees
     *
     * @return the specified angle wrapped to the range [-90, +90] degrees
     */
    public static double normalizeLatitude(double degrees) {
        double lat = degrees % 180;
        double normalizedLat = lat > 90 ? 180 - lat : (lat < -90 ? -180 - lat : lat);
        // Determine whether whether the latitude is in the north or south hemisphere
        int numEquatorCrosses = (int) (degrees / 180);
        return (numEquatorCrosses % 2 == 0) ? normalizedLat : -normalizedLat;
    }

    /**
     * Restricts an angle to the range [-180, +180] degrees, wrapping angles outside the range. Wrapping takes place as
     * though traversing a line of constant latitude which may pass through the antimeridian; angles less than -180 wrap
     * back to +180, while angles greater than +180 wrap back to -180.
     *
     * @param degrees the angle to wrap in degrees
     *
     * @return the specified angle wrapped to the range [-180, +180] degrees
     */
    public static double normalizeLongitude(double degrees) {
        double lon = degrees % 360;
        return lon > 180 ? lon - 360 : (lon < -180 ? 360 + lon : lon);
    }

    /**
     * Restricts an angle to the range [-90, +90] degrees, clamping angles outside the range. Angles less than -90 are
     * returned as -90, and angles greater than +90 are returned as +90. Angles within the range are returned
     * unmodified.
     *
     * @param degrees the angle to clamp in degrees
     *
     * @return the specified angle clamped to the range [-90, +90] degrees
     */
    public static double clampLatitude(double degrees) {
        return degrees > 90 ? 90 : (degrees < -90 ? -90 : degrees);
    }

    /**
     * Restricts an angle to the range [-180, +180] degrees, clamping angles outside the range. Angles less than -180
     * are returned as 0, and angles greater than +180 are returned as +180. Angles within the range are returned
     * unmodified.
     *
     * @param degrees the angle to clamp in degrees
     *
     * @return the specified angle clamped to the range [-180, +180] degrees
     */
    public static double clampLongitude(double degrees) {
        return degrees > 180 ? 180 : (degrees < -180 ? -180 : degrees);
    }

    /**
     * Determines whether a list of locations crosses the antimeridian.
     *
     * @param locations the locations to test
     *
     * @return true if the antimeridian is crossed, false otherwise
     *
     * @throws IllegalArgumentException If the locations list is null
     */
    public static boolean locationsCrossAntimeridian(List<? extends IGeoPosition> locations) {
        if (locations == null) {
            throw new IllegalArgumentException("EmpGeoPosition-locationsCrossAntimeridian-missingList");
        }

        // Check the list's length. A list with fewer than two locations does not cross the antimeridan.
        int len = locations.size();
        if (len < 2) {
            return false;
        }

        // Compute the longitude attributes associated with the first location.
        double lon1 = normalizeLongitude(locations.get(0).getLongitude());
        double sig1 = Math.signum(lon1);

        // Iterate over the segments in the list. A segment crosses the antimeridian if its endpoint longitudes have
        // different signs and are more than 180 degrees apart (but not 360, which indicates the longitudes are the same).
        for (int idx = 1; idx < len; idx++) {
            double lon2 = normalizeLongitude(locations.get(idx).getLongitude());
            double sig2 = Math.signum(lon2);

            if (sig1 != sig2) {
                double delta = Math.abs(lon1 - lon2);
                if (delta > 180 && delta < 360) {
                    return true;
                }
            }

            lon1 = lon2;
            sig1 = sig2;
        }

        return false;
    }
}
