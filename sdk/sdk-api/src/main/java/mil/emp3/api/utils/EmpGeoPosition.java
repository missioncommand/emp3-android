package mil.emp3.api.utils;

import org.cmapi.primitives.GeoPosition;

import mil.emp3.api.global;

/**
 * Convenience class for for creating a GeoPosition obkect.
 */

public class EmpGeoPosition extends GeoPosition {
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
    public EmpGeoPosition(GeoPosition geoPosition) {
        setLatitude(geoPosition.getLatitude());
        setLongitude(geoPosition.getLongitude());
        setAltitude(geoPosition.getAltitude());
    }

    public EmpGeoPosition() {

    }

    public boolean isValid() {
        if(getLatitude() < global.LATITUDE_MINIMUM || getLatitude() > global.LATITUDE_MAXIMUM) {
            return false;
        }
        if(getLongitude() < global.LONGITUDE_MINIMUM || getLongitude() > global.LONGITUDE_MAXIMUM) {
            return false;
        }
        return true;
    }
}
