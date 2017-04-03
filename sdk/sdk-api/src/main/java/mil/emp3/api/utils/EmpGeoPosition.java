package mil.emp3.api.utils;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

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
}
