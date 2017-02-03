package mil.emp3.api.utils;

import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.GeoPosition;

import java.security.InvalidParameterException;

/**
 * This class provide utility function for geospatial computations.
 */
public class GeoLibrary {
    public static double DEFAULT_EARTH_RADIUS = 6371000.0; // meters

    //
    // The following is an array of earth radius corresponding to the array element as the latitude.
    // Element 0 contains the earth radius at the equator.
    // Element 1 contains the earth radius at 1 degree latitude.
    // :
    // Element 10 contains the earth radius at 10 degrees latitude.
    // :
    // :
    // Element 90 contains the earth radius at the pole.
    private static double[] earthRadius = {6378137, 6378131, 6378111, 6378079, 6378034, 6377976, 6377905, 6377822, 6377726, 6377618, 6377497, 6377365, 6377220, 6377063, 6376895, 6376716, 6376525, 6376323, 6376110, 6375887, 6375654, 6375411, 6375158, 6374895, 6374624, 6374344, 6374055, 6373759, 6373455, 6373143, 6372824, 6372499, 6372168, 6371831, 6371489, 6371141, 6370789, 6370433, 6370074, 6369711, 6369345, 6368977, 6368607, 6368235, 6367863, 6367490, 6367116, 6366743, 6366371, 6366001, 6365632, 6365265, 6364900, 6364539, 6364181, 6363827, 6363478, 6363133, 6362794, 6362460, 6362132, 6361811, 6361497, 6361189, 6360890, 6360598, 6360315, 6360040, 6359775, 6359519, 6359272, 6359036, 6358810, 6358594, 6358390, 6358196, 6358014, 6357843, 6357684, 6357537, 6357402, 6357280, 6357170, 6357073, 6356988, 6356916, 6356857, 6356811, 6356779, 6356759, 6356752};

    public static double getEarthRadiusAt(double Latitude) {
        int newLat = (int) (Math.round(Math.abs(Latitude)) % 90);

        return GeoLibrary.earthRadius[newLat];
    }

    /**
     * This method computes the ground (arc) distance between the two locations provided.
     * @param oLatLon1
     * @param oLatLon2
     * @return The distance in meters.
     * @throws InvalidParameterException
     */
    public static double computeDistanceBetween(IGeoPosition oLatLon1, IGeoPosition oLatLon2) throws InvalidParameterException {

        if (oLatLon1 == null) {
            throw new InvalidParameterException("oLatLon1 can not be null.");
        }

        if (oLatLon2 == null) {
            throw new InvalidParameterException("oLatLon2 can not be null.");
        }

        double dLat1Rad = Math.toRadians(oLatLon1.getLatitude());
        double dLon1Rad = Math.toRadians(oLatLon1.getLongitude());
        double dLat2Rad = Math.toRadians(oLatLon2.getLatitude());
        double dLon2Rad = Math.toRadians(oLatLon2.getLongitude());
        double dDeltaLat = dLat2Rad - dLat1Rad;
        double dDeltaLon = dLon2Rad - dLon1Rad;

        double a = Math.sin(dDeltaLat / 2.0) * Math.sin(dDeltaLat / 2.0) +
                Math.cos(dLat1Rad) * Math.cos(dLat2Rad) *
                        Math.sin(dDeltaLon / 2.0) * Math.sin(dDeltaLon / 2.0);
        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return c * GeoLibrary.getEarthRadiusAt(oLatLon1.getLatitude());
    }

    /**
     * This method return the bearing (from north) of a line from oFrom to oTo.
     * @param oFrom
     * @param oTo
     * @return the bearing is return in degrees.
     * @throws InvalidParameterException
     */
    public static double computeBearing(IGeoPosition oFrom, IGeoPosition oTo) throws InvalidParameterException {

        if (oFrom == null) {
            throw new InvalidParameterException("oFrom can not be null.");
        }

        if (oTo == null) {
            throw new InvalidParameterException("oTo can not be null.");
        }

        double dLat1Rad = Math.toRadians(oFrom.getLatitude());
        double dLat2Rad = Math.toRadians(oTo.getLatitude());
        double dDeltaLon = Math.toRadians(oTo.getLongitude() - oFrom.getLongitude());
        double y = Math.sin(dDeltaLon) * Math.cos(dLat2Rad);
        double x = Math.cos(dLat1Rad) * Math.sin(dLat2Rad) - Math.sin(dLat1Rad) * Math.cos(dLat2Rad) * Math.cos(dDeltaLon);
        double brng = Math.toDegrees(Math.atan2(y, x));

        return (brng + 360.0) % 360.0;
    }

    /**
     * This method computes a location which is at the specified bearing at the specified distance from the
     * provided oFrom location.
     * @param dBearing The bearing in degrees
     * @param dDistance The distance in meters.
     * @param oFrom The starting position
     * @return position
     * @throws InvalidParameterException
     */
    public static IGeoPosition computePositionAt(double dBearing, double dDistance, IGeoPosition oFrom) throws InvalidParameterException {
        IGeoPosition oNewPos = new GeoPosition();

        GeoLibrary.computePositionAt(dBearing, dDistance, oFrom, oNewPos);
        return oNewPos;
    }

    /**
     * This method computes a location which is at the specified bearing at the specified distance from the
     * provided oFrom location.
     * @param dBearing The bearing in degrees
     * @param dDistance The distance in meters.
     * @param oFrom The starting position
     * @param result the object to place the resulting position.
     * @throws InvalidParameterException
     */
    public static void computePositionAt(double dBearing, double dDistance, IGeoPosition oFrom, IGeoPosition result) throws InvalidParameterException {
        if (oFrom == null) {
            throw new InvalidParameterException("oFrom can not be null.");
        }
        if (result == null) {
            throw new InvalidParameterException("result must be provided.");
        }

        double dAngularDistance = dDistance / GeoLibrary.getEarthRadiusAt(oFrom.getLatitude()); //DEFAULT_EARTH_RADIUS;
        double dBearingRad = Math.toRadians(dBearing);
        double dLat1Rad = Math.toRadians(oFrom.getLatitude());
        double dLon1Rad = Math.toRadians(oFrom.getLongitude());
        double dLat2Rad = Math.asin(Math.sin(dLat1Rad) * Math.cos(dAngularDistance) +
                Math.cos(dLat1Rad) * Math.sin(dAngularDistance) * Math.cos(dBearingRad));
        double dLon2Rad = dLon1Rad + Math.atan2(Math.sin(dBearingRad) * Math.sin(dAngularDistance) * Math.cos(dLat1Rad),
                Math.cos(dAngularDistance) - Math.sin(dLat1Rad) * Math.sin(dLat2Rad));

        dLon2Rad = (dLon2Rad + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
        result.setLatitude(Math.toDegrees(dLat2Rad));
        result.setLongitude(Math.toDegrees(dLon2Rad));
    }

    public static IGeoPosition midPointBetween(IGeoPosition oLocation1, IGeoPosition oLocation2) throws InvalidParameterException {
        if (oLocation1 == null) {
            throw new InvalidParameterException("oLocation1 can not be null.");
        }

        if (oLocation2 == null) {
            throw new InvalidParameterException("oLocation2 can not be null.");
        }
        IGeoPosition oNewPos = new GeoPosition();

        double dLat1Rad = Math.toRadians(oLocation1.getLatitude());
        double dLon1Rad = Math.toRadians(oLocation1.getLongitude());
        double dLat2Rad = Math.toRadians(oLocation2.getLatitude());
        double dDeltaLon = Math.toRadians(oLocation2.getLongitude() - oLocation1.getLongitude());

        double Bx = Math.cos(dLat2Rad) * Math.cos(dDeltaLon);
        double By = Math.cos(dLat2Rad) * Math.sin(dDeltaLon);

        double dLat3Rad = Math.atan2(Math.sin(dLat1Rad) + Math.sin(dLat2Rad),
                Math.sqrt((Math.cos(dLat1Rad) + Bx) * (Math.cos(dLat1Rad) + Bx) + By * By));
        double dLon3Rad = dLon1Rad + Math.atan2(By, Math.cos(dLat1Rad) + Bx);
        dLon3Rad = (dLon3Rad + 3 * Math.PI) % (2 * Math.PI) - Math.PI;

        oNewPos.setLatitude(Math.toDegrees(dLat3Rad));
        oNewPos.setLongitude(Math.toDegrees(dLon3Rad));

        return oNewPos;
    }

    /**
     * This method compute the rhumb distance between to points.
     * @param oFrom
     * @param oTo
     * @return The distance in meters.
     * @throws InvalidParameterException
     */
    public static double computeRhumbDistance(IGeoPosition oFrom, IGeoPosition oTo) throws InvalidParameterException {
        if (oFrom == null) {
            throw new InvalidParameterException("oFrom can not be null.");
        }

        if (oTo == null) {
            throw new InvalidParameterException("oTo can not be null.");
        }
        double lat1 = Math.toRadians(oFrom.getLatitude()),
               lat2 = Math.toRadians(oTo.getLatitude());
        double dLat = Math.toRadians(oTo.getLatitude() - oFrom.getLatitude());
        double dLon = Math.toRadians(Math.abs(oTo.getLongitude() - oFrom.getLongitude()));

        double dPhi = Math.log(Math.tan(lat2 / 2 + Math.PI / 4) / Math.tan(lat1 / 2 + Math.PI / 4));
        double q = (!Double.isNaN(dLat / dPhi) && (dPhi != 0)) ? dLat / dPhi : Math.cos(lat1); // E-W line gives dPhi=0
        // if dLon over 180° take shorter rhumb across 180° meridian:
        if (dLon > Math.PI) {
            dLon = 2 * Math.PI - dLon;
        }
        double dist = Math.sqrt(dLat * dLat + q * q * dLon * dLon) * GeoLibrary.getEarthRadiusAt(oFrom.getLatitude()); //DEFAULT_EARTH_RADIUS;

        return dist;
    }

    /**
     * This method calculates the rhumb bearing of the line starting at oFrom and ending at oTo.
     * @param oFrom
     * @param oTo
     * @return The bearing is return in degrees.
     * @throws InvalidParameterException
     */
    public static double computeRhumbBearing(IGeoPosition oFrom, IGeoPosition oTo) throws InvalidParameterException {
        if (oFrom == null) {
            throw new InvalidParameterException("oFrom can not be null.");
        }

        if (oTo == null) {
            throw new InvalidParameterException("oTo can not be null.");
        }
        double lat1 = Math.toRadians(oFrom.getLatitude()),
               lat2 = Math.toRadians(oTo.getLatitude());
        double dLon = Math.toRadians(oTo.getLongitude() - oFrom.getLongitude());

        double dPhi = Math.log(Math.tan(lat2 / 2 + Math.PI / 4) / Math.tan(lat1 / 2 + Math.PI / 4));
        if (Math.abs(dLon) > Math.PI) {
            dLon = dLon > 0 ? -(2 * Math.PI - dLon) : (2 * Math.PI + dLon);
        }
        double brng = Math.atan2(dLon, dPhi);

        return (Math.toDegrees(brng) + 360) % 360;
    }

    /**
     * This method calculates a point at a specified distance at a specified angle from the specified position.
     * @param dBearing The bearing in degrees
     * @param dDistance The distance in meters.
     * @param oFrom
     * @return Geo position
     * @throws InvalidParameterException
     */
    public static IGeoPosition calculateRhumbPositionAt(double dBearing, double dDistance, IGeoPosition oFrom) throws InvalidParameterException {
        if (oFrom == null) {
            throw new InvalidParameterException("oFrom can not be null.");
        }
        double d = dDistance / GeoLibrary.getEarthRadiusAt(oFrom.getLatitude()); // DEFAULT_EARTH_RADIUS; // d = angular distance covered on earth's surface
        double lat1 = Math.toRadians(oFrom.getLatitude()),
               lon1 = Math.toRadians(oFrom.getLongitude()),
               lon2;
        double brng = Math.toRadians(dBearing);

        double lat2 = lat1 + d * Math.cos(brng);
        double dLat = lat2 - lat1;
        double dPhi = Math.log(Math.tan(lat2 / 2 + Math.PI / 4) / Math.tan(lat1 / 2 + Math.PI / 4));
        double q = (!Double.isNaN(dLat / dPhi) && (dPhi != 0)) ? dLat / dPhi : Math.cos(lat1); // E-W line gives dPhi=0
        double dLon = d * Math.sin(brng) / q;
        // check for some daft bugger going past the pole
        if (Math.abs(lat2) > Math.PI / 2) {
            lat2 = lat2 > 0 ? Math.PI - lat2 : -(Math.PI - lat2);
        }
        lon2 = (lon1 + dLon + 3 * Math.PI) % (2 * Math.PI) - Math.PI;
        IGeoPosition oNewPos = new GeoPosition();

        oNewPos.setLatitude(Math.toDegrees(lat2));
        oNewPos.setLongitude(Math.toDegrees(lon2));

        return oNewPos;
    }

    /**
     * This method ensures that a longitude is between -180 an 180
     * @param dLongitude
     * @return
     */
    public static double wrapLongitude(double dLongitude) {
        return (((dLongitude + 540.0) % 360) - 180.0);
    }

    /**
     * This method calculates the arc length on the earth surface from degrees
     * @param dDegrees
     * @return The arc length in meters.
     */
    public static double getArcLengthFromDegrees(double dDegrees) {
        return 2.0 * Math.PI * GeoLibrary.DEFAULT_EARTH_RADIUS * dDegrees / 360.0;
    }

    /**
     * This method calculates the degrees from the arc length.
     * @param dArcLength The length in meters.
     * @return the angle in degrees.
     */
    public static double getDegreesFromArcLength(double dArcLength) {
        return dArcLength * 180.0 / (Math.PI * GeoLibrary.DEFAULT_EARTH_RADIUS);
    }

    /**
     * Uses the cross product to determine if a point is on the left side of
     * a line or not.
     *
     * @param p1 the first point of the line
     * @param p2 the second point of the line
     * @param p3 the point to test
     *
     * @return true if p3 is on the left side of the line, false otherwise
     */
    public static boolean isLeft(IGeoPosition p1, IGeoPosition p2, IGeoPosition p3) {
        double lat1 = Math.toRadians(p1.getLatitude());
        double lon1 = Math.toRadians(p1.getLongitude());

        double lat2 = Math.toRadians(p2.getLatitude());
        double lon2 = Math.toRadians(p2.getLongitude());

        double lat3 = Math.toRadians(p3.getLatitude());
        double lon3 = Math.toRadians(p3.getLongitude());

        return ((lat2 - lat1) * (lon3 - lon1) - (lon2 - lon1) * (lat3 - lat1)) > 0;
    }
}
