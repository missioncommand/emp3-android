package mil.emp3.api.utils;

import net.sf.geographiclib.GeoMath;
import net.sf.geographiclib.Geodesic;
import net.sf.geographiclib.GeodesicData;
import net.sf.geographiclib.GeodesicMask;
import net.sf.geographiclib.Gnomonic;
import net.sf.geographiclib.GnomonicData;
import net.sf.geographiclib.Pair;

import java.util.List;

public class GeodesicWrapper {

    final private static Geodesic earth = Geodesic.WGS84;
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
        return azimuth;
    }

    private static GeodesicData getInverseGeodesicData(double lat1, double lon1,
                                                       double lat2, double lon2, int mask) {
        if (Math.abs(lat1) > 90.0) {
            throw new IllegalArgumentException("Invalid latitude value " + lat1);
        }
        if (Math.abs(lon1) > 180.0) {
            throw new IllegalArgumentException("Invalid longitude value " + lon1);
        }

        if (Math.abs(lat2) > 90.0) {
            throw new IllegalArgumentException("Invalid latitude value " + lat2);
        }
        if (Math.abs(lon2) > 180.0) {
            throw new IllegalArgumentException("Invalid longitude value " + lon2);
        }

        return earth.Inverse(lat1, lon1, lat2, lon2, mask);
    }

    private static GeodesicData getDirectGeodesicData(double lat, double lon,
                                                      double azimuth, double distance) {
        if (Math.abs(lat) > 90.0) {
            throw new IllegalArgumentException("Invalid latitude value " + lat);
        }
        if (Math.abs(lon) > 180.0) {
            throw new IllegalArgumentException("Invalid longitude value " + lon);
        }

        return earth.Direct(lat, lon, azimuth, distance,
                GeodesicMask.LATITUDE + GeodesicMask.LONGITUDE);
    }

    private static GeodesicData getInverseRhumbData(double lat1, double lon1,
                                                    double lat2, double lon2, int mask) {
        if (Math.abs(lat1) > 90.0) {
            throw new IllegalArgumentException("Invalid latitude value " + lat1);
        }
        if (Math.abs(lon1) > 180.0) {
            throw new IllegalArgumentException("Invalid longitude value " + lon1);
        }

        if (Math.abs(lat2) > 90.0) {
            throw new IllegalArgumentException("Invalid latitude value " + lat2);
        }
        if (Math.abs(lon2) > 180.0) {
            throw new IllegalArgumentException("Invalid longitude value " + lon2);
        }

        return Rhumb.WGS84.Inverse(lat1, lon1, lat2, lon2, mask);
    }

    private static GeodesicData getDirectRhumbData(double lat, double lon,
                                                   double azimuth, double distance) {

        if (Math.abs(lat) > 90.0) {
            throw new IllegalArgumentException("Invalid latitude value " + lat);
        }
        if (Math.abs(lon) > 180.0) {
            throw new IllegalArgumentException("Invalid longitude value " + lon);
        }

        return Rhumb.WGS84.Direct(lat, lon, azimuth, distance,
                GeodesicMask.LATITUDE + GeodesicMask.LONGITUDE);
    }

    /**
     * This method computes the ground (arc) distance between the two locations provided.
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return The distance in meters.
     * @throws IllegalArgumentException
     */
    public static double computeDistanceBetween(double lat1, double lon1,
                                                double lat2, double lon2) {
        GeodesicData g = getInverseGeodesicData(lat1, lon1, lat2, lon2, GeodesicMask.DISTANCE);
        return g.s12;
    }

    /**
     * This method return the azimuth of a line from p1 to p2.
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return the bearing is return in degrees.
     * @throws IllegalArgumentException
     */
    public static double computeBearing(double lat1, double lon1,
                                        double lat2, double lon2) {
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }
        GeodesicData g = getInverseGeodesicData(lat1, lon1, lat2, lon2, GeodesicMask.AZIMUTH);
        return (g.azi1 + 360.0) % 360.0;
    }

    /**
     * This method computes a location which is at the specified bearing at the specified distance from the
     * provided from location.
     * @param bearing The bearing in degrees
     * @param distance The distance in meters.
     * @param lat latitude of starting position
     * @param lon longitude of starting position
     * @return position
     * @throws IllegalArgumentException
     */
    public static Pair computePositionAt(double bearing, double distance,
                                         double lat, double lon) {
        if (distance == 0.0) {
            return new Pair (lat, lon);
        } else {
            GeodesicData g = getDirectGeodesicData(lat, lon, bearing, distance);
            return new Pair(g.lat2, g.lon2);
        }
    }

    /**
     * This method compute the rhumb distance between to points.
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return The distance in meters.
     * @throws IllegalArgumentException
     */
    public static double computeRhumbDistance(double lat1, double lon1,
                                              double lat2, double lon2) {
        GeodesicData g = getInverseRhumbData(lat1, lon1, lat2, lon2, GeodesicMask.DISTANCE);
        return g.s12;
    }

    /**
     * This method calculates the rhumb bearing of the line starting at oFrom and ending at oTo.
     * @param lat1 latitude of first point
     * @param lon1 longitude of first point
     * @param lat2 latitude of second point
     * @param lon2 longitude of second point
     * @return The bearing is return in degrees.
     * @throws IllegalArgumentException
     */
    public static double computeRhumbBearing(double lat1, double lon1,
                                             double lat2, double lon2) {
        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0;
        }
        GeodesicData g = getInverseRhumbData(lat1, lon1, lat2, lon2, GeodesicMask.AZIMUTH);
        return (g.azi2 + 360.0) % 360.0;
    }

    /**
     * This method calculates a point at a specified distance at a specified angle from the specified position.
     * @param bearing The bearing in degrees
     * @param distance The distance in meters.
     * @param lat latitude of starting position
     * @param lon longitude of starting position
     * @return Pair of latitude, longitude (first, second)
     * @throws IllegalArgumentException
     */
    public static Pair calculateRhumbPositionAt(double bearing, double distance, double lat, double lon) {
        GeodesicData g = getDirectRhumbData(lat, lon, bearing, distance);
        return new Pair(g.lat2, g.lon2);
    }

    /**
     * https://stackoverflow.com/questions/6671183/calculate-the-center-point-of-multiple-latitude-longitude-coordinate-pairs
     * Spherical calculation, not ellipsoidal
     * However the places it is used may not need the extra accuracy
     * Procedure:
     *     Convert each lat/long pair into a unit-length 3D vector.
     *     Sum each of those vectors
     *     Normalize the resulting vector
     *     Convert back to spherical coordinates
     * @param positionList
     * @return Pair of latitude and longitude
     */
    public static Pair getCenter(List<Pair> positionList) {

        if((null != positionList) && (0 != positionList.size())) {
            if(1 == positionList.size()) {
                return new Pair(positionList.get(0).first,
                    positionList.get(0).second);
            } else {
                double x = 0;
                double y = 0;
                double z = 0;

                for(Pair position: positionList)
                {
                    double latitude = position.first * Math.PI / 180;
                    double longitude = position.second * Math.PI / 180;


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

                return new Pair(centralLatitude * 180 / Math.PI,
                centralLongitude * 180 / Math.PI);
            }
        }
        return null;
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
        double latp0 = (lata1 + lata2 + latb1 + latb2) / 4,
                latp0_ = Double.NaN,
                lonp0_ = Double.NaN;
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

