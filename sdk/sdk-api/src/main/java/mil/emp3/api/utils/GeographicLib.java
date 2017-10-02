package mil.emp3.api.utils;

import net.sf.geographiclib.*;

import org.cmapi.primitives.IGeoPosition;

public class GeographicLib {

    /**
     * This method computes the ground (arc) distance between the two locations provided.
     * @param oLatLon1
     * @param oLatLon2
     * @return The distance in meters.
     * @throws IllegalArgumentException
     */
    public static double computeDistanceBetween(IGeoPosition oLatLon1, IGeoPosition oLatLon2) {

        if (oLatLon1 == null) {
            throw new IllegalArgumentException("oLatLon1 can not be null.");
        }

        if (oLatLon2 == null) {
            throw new IllegalArgumentException("oLatLon2 can not be null.");
        }

        double dLat1 = oLatLon1.getLatitude();
        double dLon1 = oLatLon1.getLongitude();
        double dLat2 = oLatLon2.getLatitude();
        double dLon2 = oLatLon2.getLongitude();
        GeodesicData g = Geodesic.WGS84.Inverse(dLat1, dLon1, dLat2, dLon2,
                GeodesicMask.DISTANCE);
        return g.s12;
    }

}
