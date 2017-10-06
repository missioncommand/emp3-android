package mil.emp3.api.utils;

import android.util.Log;

import net.sf.geographiclib.*;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;

public class GeographicLib {

    private static String TAG = GeographicLib.class.getSimpleName();

    /* Conversions from azimuth to/from bearing would normally be
       needed. However, calculations within EMP use a bearing
       value of 0-360, like azimuth, rather than 0-90 as elsewhere.

       One unresolved wrinkle is that GeographicLib returns an azimuth
       of 180, rather than 0, between two identical points.
     */
    private static double azimuthToBearing(double azimuth) {
        double bearing = azimuth % 360.0;
        if (bearing >= 270.0) {
            bearing = 360.0 - bearing;
        } else if (bearing >= 180.0) {
            bearing = bearing - 180.0;
        } else if (bearing > 90.0) {
            bearing = 180.0 - bearing;
        }
        Log.d(TAG, "Azimuth " + azimuth + " Bearing " + bearing);
        return bearing;
    }

    private static double bearingToAzimuth(double bearing, double lat, double lon) {
        if (Math.abs(lat) > 90.0) {
            throw new IllegalArgumentException("Invalid latitude value " + lat);
        }
        if (Math.abs(lon) > 180.0) {
            throw new IllegalArgumentException("Invalid longitude value " + lon);
        }
        double azimuth = lat >= 0.0 ?
                lon >= 0.0 ? bearing : 360.0 - bearing :
                lon >= 0.0 ? 180.0 - bearing : 180.0 + bearing;
        Log.d(TAG,"Latitude " + lat + " Longitude " + lon);
        Log.d(TAG, "Bearing " + bearing + " Azimuth " + azimuth);
        return azimuth;
    }

    private static GeodesicData getInverseGeodesicData(IGeoPosition p1, IGeoPosition p2, int mask) {

        if (p1 == null) {
            throw new IllegalArgumentException("GeoPosition cannot be null.");
        }

        if (p2 == null) {
            throw new IllegalArgumentException("GeoPosition cannot be null.");
        }

        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();
        double lat2 = p2.getLatitude();
        double lon2 = p2.getLongitude();
        return Geodesic.WGS84.Inverse(lat1, lon1, lat2, lon2, mask);
    }

    private static GeodesicData getDirectGeodesicData(IGeoPosition p1, double azimuth, double distance) {

        if (p1 == null) {
            throw new IllegalArgumentException("GeoPosition cannot be null.");
        }

        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();
        return Geodesic.WGS84.Direct(lat1, lon1, azimuth, distance,
                GeodesicMask.LATITUDE + GeodesicMask.LONGITUDE);
    }

    /**
     * This method computes the ground (arc) distance between the two locations provided.
     * @param p1
     * @param p2
     * @return The distance in meters.
     * @throws IllegalArgumentException
     */
    public static double computeDistanceBetween(IGeoPosition p1, IGeoPosition p2) {
        GeodesicData g = getInverseGeodesicData(p1, p2, GeodesicMask.DISTANCE);
        return g.s12;
    }

    /**
     * This method return the azimuth of a line from p1 to p2.
     * @param p1
     * @param p2
     * @return the bearing is return in degrees.
     * @throws IllegalArgumentException
     */
    public static double computeBearing(IGeoPosition p1, IGeoPosition p2) {
        // keep compatibility with GeoLibrary
        if (p1.getLatitude() == p2.getLatitude()
                && p1.getLongitude() == p2.getLongitude()) {
            return 0.0;
        }
        GeodesicData g = getInverseGeodesicData(p1, p2, GeodesicMask.AZIMUTH);
        // keep compatibility with GeoLibrary
        return (g.azi1 + 360.0) % 360.0;
    }

    /**
     * This method computes a location which is at the specified bearing at the specified distance from the
     * provided from location.
     * @param bearing The bearing in degrees
     * @param distance The distance in meters.
     * @param from The starting position
     * @return position
     * @throws IllegalArgumentException
     */
    public static IGeoPosition computePositionAt(double bearing, double distance, IGeoPosition from) {
        IGeoPosition position = new GeoPosition();
        if (distance == 0.0) {
            position.setLatitude(from.getLatitude());
            position.setLongitude(from.getLongitude());
        } else {
            computePositionAt(bearing, distance, from, position);
        }
        return position;
    }

    /**
     * This method computes a location which is at the specified bearing at the specified distance from the
     * provided from location.
     * @param bearing The bearing in degrees
     * @param distance The distance in meters.
     * @param from The starting position
     * @param result the object to place the resulting position.
     * @throws IllegalArgumentException
     */
    public static void computePositionAt(double bearing, double distance, IGeoPosition from, IGeoPosition result) {
        if (result == null) {
            throw new IllegalArgumentException("Null argument provided for result");
        }
        GeodesicData g = getDirectGeodesicData(from, bearing, distance);
        result.setLatitude(g.lat2);
        result.setLongitude(g.lon2);
    }

    /**
     * This method computes the midpoint between the two locations provided.
     * @param p1
     * @param p2
     * @return The distance in meters.
     * @throws IllegalArgumentException
     */
    public static IGeoPosition midPointBetween(IGeoPosition p1, IGeoPosition p2) {
        GeodesicData g = getInverseGeodesicData(p1, p2,
                GeodesicMask.AZIMUTH + GeodesicMask.DISTANCE);
        double midpointDistance = g.s12 * 0.5;
        return computePositionAt(g.azi1, midpointDistance, p1);
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
        double lat1 = p1.getLatitude();
        double lon1 = p1.getLongitude();

        double lat2 = p2.getLatitude();
        double lon2 = p2.getLongitude();

        double lat3 = p3.getLatitude();
        double lon3 = p3.getLongitude();

        return ((lat2 - lat1) * (lon3 - lon1) - (lon2 - lon1) * (lat3 - lat1)) > 0;
    }

    /**
     * This method ensures that a longitude is between -180 an 180
     * @param longitude
     * @return
     */
    public static double wrapLongitude(double longitude) {
        return (((longitude + 540.0) % 360) - 180.0);
    }

    /**
     * https://stackoverflow.com/questions/6671183/calculate-the-center-point-of-multiple-latitude-longitude-coordinate-pairs
     * Method taken from GeoLibrary, may not be accurate
     * However the places it is used may not need the accuracy
     * Procedure:
     *     Convert each lat/long pair into a unit-length 3D vector.
     *     Sum each of those vectors
     *     Normalize the resulting vector
     *     Convert back to spherical coordinates
     * @param positionList
     * @return
     */
    public static IGeoPosition getCenter(List<IGeoPosition> positionList) {

        IGeoPosition center = null;
        if((null != positionList) && (0 != positionList.size())) {
            center = new GeoPosition();
            if(1 == positionList.size()) {
                center.setLongitude(positionList.get(0).getLongitude());
                center.setLatitude(positionList.get(0).getLatitude());
            } else {
                double x = 0;
                double y = 0;
                double z = 0;

                for(IGeoPosition position: positionList)
                {
                    double latitude = position.getLatitude() * Math.PI / 180;
                    double longitude = position.getLongitude() * Math.PI / 180;


                    x += Math.cos(latitude) * Math.cos(longitude);
                    y += Math.cos(latitude) * Math.sin(longitude);
                    z += Math.sin(latitude);
                }

                x = x / positionList.size();
                y = y / positionList.size();
                z = z / positionList.size();

                double centralLongitude = Math.atan2(y, x);
                double centralSquareRoot = Math.sqrt(x * x + y * y);
                double centralLatitude = Math.atan2(z, centralSquareRoot);

                center.setLongitude(centralLongitude * 180 / Math.PI);
                center.setLatitude(centralLatitude * 180 / Math.PI);

                Log.d(TAG, "Center Lat/Lon " + center.getLatitude() + "/" + center.getLongitude());
            }
        }

        return center;
    }

    /**
     * The two positions here could be diagonal or adjacent corners of a rectangle
     * The intersect method we have below needs four points. We have two points and
     * two bearings. We calculate the other two points using a length of 2x distance
     * between two points, because that will always be longer than either side.
     *
     * @param p1
     * @param bearing1
     * @param p2
     * @param bearing2
     * @return
     */
    public static IGeoPosition intersection(IGeoPosition p1, double bearing1, IGeoPosition p2, double bearing2) {
        IGeoPosition cross = new GeoPosition();
        double distance = computeDistanceBetween(p1, p2);
        IGeoPosition p3 = computePositionAt(bearing1, 2.0*distance, p1);
        IGeoPosition p4 = computePositionAt(bearing2, 2.0*distance, p2);
        GeodesicData g = intersect(p1.getLatitude(), p1.getLongitude(), p3.getLatitude(), p3.getLongitude(),
                p2.getLatitude(), p2.getLongitude(), p4.getLatitude(), p4.getLongitude());
        cross.setLatitude(g.lat2);
        cross.setLongitude(g.lon2);
        return cross;
    }

    /* Intersect method taken from BMW Car IT GMBH

    https://github.com/bmwcarit/barefoot/blob/master/src/main/java/com/bmwcarit/barefoot/spatial/Intersect.java

     */

    private static final double eps = 0.01 * Math.sqrt(GeoMath.epsilon);
    /**
     * Maximum number of iterations for calculation of interception point. (The
     * solution should usually converge before reaching the maximum number of
     * iterations. The default is 10.)
     */
    public static int maxit = 10;
    private static Geodesic earth = Geodesic.WGS84;
    private static Gnomonic gnom = new Gnomonic(earth);

    /**
     * Intersection of geodesics.
     * <p>
     * @param lata1
     * latitude of point <i>1</i> of geodesic <i>a</i> (degrees).
     * @param lona1
     * longitude of point <i>1</i> of geodesic <i>a</i> (degrees).
     * @param lata2
     * latitude of point <i>2</i> of geodesic <i>a</i> (degrees).
     * @param lona2
     * longitude of point <i>2</i> of geodesic <i>a</i> (degrees).
     * @param latb1
     * latitude of point <i>1</i> of geodesic <i>b</i> (degrees).
     * @param lonb1
     * longitude of point <i>1</i> of geodesic <i>b</i> (degrees).
     * @param latb2
     * latitude of point <i>2</i> of geodesic <i>b</i> (degrees).
     * @param lonb2
     * longitude of point <i>2</i> of geodesic <i>b</i> (degrees).
     * @return a {@link GeodesicData} object, defining a geodesic from point
     * <i>1</i> of geodesic <i>a</i> to the intersection point, with the
     * following fields: <i>lat1</i>, <i>lon1</i>, <i>azi1</i>,
     * <i>lat2</i>, <i>lon2</i>, <i>azi2</i>, <i>s12</i>, <i>a12</i>.
     * <p>
     * <i>lat1</i> should be in the range [&minus;90&deg;, 90&deg;];
     * <i>lon1</i> and <i>azi1</i> should be in the range
     * [&minus;540&deg;, 540&deg;). The values of <i>lon2</i> and
     * <i>azi2</i> returned are in the range [&minus;180&deg;, 180&deg;).
     */
    public static GeodesicData intersect(double lata1, double lona1, double lata2,
                                  double lona2, double latb1, double lonb1, double latb2, double lonb2) {
        double latp0 = (lata1 + lata2 + latb1 + latb2) / 4, latp0_ = Double.NaN, lonp0_ =
                Double.NaN;
        double lonp0 =
                ((lona1 >= 0 ? lona1 % 360 : (lona1 % 360) + 360)
                        + (lona2 >= 0 ? lona2 % 360 : (lona2 % 360) + 360)
                        + (lonb1 >= 0 ? lonb1 % 360 : (lonb1 % 360) + 360) + (lonb2 >= 0
                        ? lonb2 % 360 : (lonb2 % 360) + 360)) / 4;
        lonp0 = (lonp0 > 180 ? lonp0 - 360 : lonp0);
        for (int i = 0; i < maxit; ++i) {
            GnomonicData xa1 = gnom.Forward(latp0, lonp0, lata1, lona1);
            GnomonicData xa2 = gnom.Forward(latp0, lonp0, lata2, lona2);
            GnomonicData xb1 = gnom.Forward(latp0, lonp0, latb1, lonb1);
            GnomonicData xb2 = gnom.Forward(latp0, lonp0, latb2, lonb2);
            Vector va1 = new Vector(xa1.x, xa1.y, 1);
            Vector va2 = new Vector(xa2.x, xa2.y, 1);
            Vector vb1 = new Vector(xb1.x, xb1.y, 1);
            Vector vb2 = new Vector(xb2.x, xb2.y, 1);
            Vector la = va1.cross(va2);
            Vector lb = vb1.cross(vb2);
            Vector p0 = la.cross(lb);
            p0 = p0.multiply(1d / p0.z);
            latp0_ = latp0;
            lonp0_ = lonp0;
            GnomonicData rev = gnom.Reverse(latp0, lonp0, p0.x, p0.y);
            latp0 = rev.lat;
            lonp0 = rev.lon;
            if (Math.abs(lonp0_ - lonp0) < eps
                    && Math.abs(latp0_ - latp0) < eps) {
                break;
            }
        }
        return earth.Inverse(lata1, lona1, latp0, lonp0);
    }

    private static class Vector {
        public double x = Double.NaN;
        /**
         * Component in y dimension.
         */
        public double y = Double.NaN;
        /**
         * Component in z dimension.
         */
        public double z = Double.NaN;

        /**
         * Vector constructor.
         * <p>
         *
         * @param x Component in x dimension.
         * @param y Component in y dimension.
         * @param z Component in z dimension.
         */
        public Vector(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        /**
         * Addition with a vector.
         * <p>
         *
         * @param other Vector for addition.
         * @return Added {@link Vector} object.
         */
        public Vector add(Vector other) {
            return new Vector(this.x + other.x, this.y + other.y, this.z + other.z);
        }

        /**
         * Multiplication with a scalar.
         * <p>
         *
         * @param a Scalar multiplicator.
         * @return Scaled {@link Vector} object.
         */
        public Vector multiply(double a) {
            return new Vector(this.x * a, this.y * a, this.z * a);
        }

        /**
         * Cross product with a vector.
         * <p>
         *
         * @param other Vector for cross product.
         * @return {@link Vector} object as result of cross product.
         */
        public Vector cross(Vector other) {
            return new Vector((y * other.z) - (z * other.y), (z * other.x) - (x * other.z),
                    (x * other.y) - (y * other.x));
        }

        /**
         * Dot product with a vector.
         * <p>
         *
         * @param other Vector for dot product.
         * @return Scalar as result of dot product.
         */
        public double dot(Vector other) {
            return this.x * other.x + this.y * other.y + this.z * other.z;
        }
    }

}
