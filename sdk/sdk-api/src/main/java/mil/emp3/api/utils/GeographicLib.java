package mil.emp3.api.utils;

import net.sf.geographiclib.*;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

public class GeographicLib {


    private static GeodesicData getInverseGeodesicData(IGeoPosition p1, IGeoPosition p2) {

        if (p1 == null) {
            throw new IllegalArgumentException("oLatLon1 can not be null.");
        }

        if (p2 == null) {
            throw new IllegalArgumentException("oLatLon2 can not be null.");
        }

        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();
        double lat2 = p2.getLatitude();
        double lon2 = p2.getLongitude();
        return Geodesic.WGS84.Inverse(lat1, lon1, lat2, lon2);
    }

    private static GeodesicData getDirectGeodesicData(IGeoPosition p1, double azimuth, double distance) {

        if (p1 == null) {
            throw new IllegalArgumentException("oLatLon1 can not be null.");
        }

        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();
        return Geodesic.WGS84.Direct(lat1, lon1, azimuth, distance);
    }

    /**
     * This method computes the ground (arc) distance between the two locations provided.
     * @param p1
     * @param p2
     * @return The distance in meters.
     * @throws IllegalArgumentException
     */
    public static double computeDistanceBetween(IGeoPosition p1, IGeoPosition p2) {
        GeodesicData g = getInverseGeodesicData(p1, p2);
        return g.s12;
    }

    /**
     * This method return the azimuth of a line from from to oTo.
     * @param p1
     * @param p2
     * @return the bearing is return in degrees.
     * @throws IllegalArgumentException
     */
    public static double computeBearing(IGeoPosition p1, IGeoPosition p2) {
        GeodesicData g = getInverseGeodesicData(p1, p2);
        return g.azi1;
    }

    /**
     * This method computes a location which is at the specified bearing at the specified distance from the
     * provided from location.
     * @param azimuth The bearing in degrees
     * @param distance The distance in meters.
     * @param from The starting position
     * @return position
     * @throws IllegalArgumentException
     */
    public static IGeoPosition computePositionAt(double azimuth, double distance, IGeoPosition from) {
        IGeoPosition position = new GeoPosition();

        computePositionAt(azimuth, distance, from, position);
        return position;
    }

    /**
     * This method computes a location which is at the specified bearing at the specified distance from the
     * provided from location.
     * @param azimuth The bearing in degrees
     * @param distance The distance in meters.
     * @param from The starting position
     * @param result the object to place the resulting position.
     * @throws IllegalArgumentException
     */
    public static void computePositionAt(double azimuth, double distance, IGeoPosition from, IGeoPosition result) {
        if (result == null) {
            throw new IllegalArgumentException("result must be provided.");
        }

        GeodesicData g = getDirectGeodesicData(from, azimuth, distance);
        result.setLatitude(g.lat2);
        result.setLongitude(g.lon2);
    }



}
