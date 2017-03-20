package mil.emp3.api.utils;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.global;

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

    public EmpGeoPosition() {

    }

    public boolean isValid() {
        return validate(getLatitude(), getLongitude(), getAltitude());
    }

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

    public static boolean validate(IGeoPosition position) {
        return validate(position.getLatitude(), position.getLongitude(), position.getAltitude());
    }
}
