package mil.emp3.worldwind.utils;

import android.graphics.Point;
import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.controller.PickNavigateController;

/**
 * Generates a bounding box from the vertices of Bounding Area. It is presumed that there are four vertices and they have
 * valid data.
 *
 * Basic algorithm goes like this:
 *    - Get a center location to begin with (some combination of Camera and four vertices)
 *    - Start at that center position and stretch in all four directions as much as possible avoid going beyond visible portion of the View
 *    - Now stretch on each edge with same constraint
 *    - In reality once the bounds are calculated one or more corner may be of the screen, this is desired to cover as much area as
 *        possible in the center.
 *    - This was tuned for MGRS lines.
 *    - If there is a Mathematical way of figuring this out with help of some utilities from the underlying engine thaen that should be
 *        investigated.
 *    - We must also consider the performance implication as every time map movement stops this algorithm is executed.
 *
 */
public class BoundingBoxGeneration {

    private static String TAG = BoundingBoxGeneration.class.getSimpleName();
    private final PickNavigateController mapController;
    private final int width;
    private final int height;
    private final int lr_margin;
    private final int tb_margin;
    private final boolean cameraOnScreen;
    private final ICamera camera;
    private final int cornersTouched;
    private final static int MAX_LAT_LONG_ITERATIONS = 40;
    private final static int MAX_CORNER_ITERATIONS = 20;
    private final static double INCREMENT_FACTOR = 0.05;
    private final static double MAX_LATITUDE_SPAN = 120.0;
    private final static double MAX_LONGITUDE_SPAN = 120.0;

    private BoundingBoxGeneration(MapInstance mapInstance, boolean cameraOnScreen, int cornersTouched) {
        mapController = mapInstance.getMapController();
        width = mapInstance.getWW().getWidth();
        height = mapInstance.getWW().getHeight();
        this.cameraOnScreen = cameraOnScreen;
        camera = mapInstance.getCamera();
        this.cornersTouched = cornersTouched;

        if(camera.getAltitude() < 1000000) {
            lr_margin = (int) (.025 * width);
            tb_margin = (int) (.025 * height);
        } else if(camera.getAltitude() < 4000000) {
            lr_margin = (int) (.05 * width);
            tb_margin = (int) (.05 * height);
        } else {
            lr_margin = (int) (.1 * width);
            tb_margin = (int) (.1 * height);
        }
    }

    public static void buildBoundingBox(MapInstance mapInstance, IGeoPosition[] vertices, IGeoBounds geoBounds,
                                        boolean cameraOnScreen, int cornersTouched, IGeoPosition geometricCenter) {
        BoundingBoxGeneration bbg = new BoundingBoxGeneration(mapInstance, cameraOnScreen, cornersTouched);
        bbg.buildBoundingBox_(vertices, geoBounds, geometricCenter);
    }
    /**
     * Sets North boundary to the highest latitude
     * Sets South boundary to the smallest latitude.
     */
    private void setNorthAndSouth(IGeoPosition[] vertices, IGeoBounds geoBounds) {
        double tmpNorth = 91.0;
        double tmpSouth = -91.0;
        for(int ii = 0; ii < vertices.length; ii++) {
            if(tmpNorth > 90.0) {
                tmpNorth = vertices[ii].getLatitude();
                tmpSouth = vertices[ii].getLatitude();
            } else {
                if(vertices[ii].getLatitude() > tmpNorth) {
                    tmpNorth = vertices[ii].getLatitude();
                } else if(vertices[ii].getLatitude() < tmpSouth) {
                    tmpSouth = vertices[ii].getLatitude();
                }
            }
        }
        geoBounds.setNorth(tmpNorth);
        geoBounds.setSouth(tmpSouth);
    }

    private double getLongitudeSpan(double east, double west) {
        if((east >= 0 && west >= 0) || (east <= 0 && west <= 0)) {
            return east - west;
        } else if(east >= 0) {
            return east + Math.abs(west);
        } else /* (west >= 0) */ {
            return (global.LONGITUDE_MAXIMUM - west) + (global.LONGITUDE_MAXIMUM - Math.abs(east));
        }
    }

    private double getMidLongitude(double east, double west) {
        if((east >= 0 && west >= 0) || (east <= 0 && west <= 0)) {
            return west + (east - west)/2;
        } else if(east >= 0) {
            return west + (east + Math.abs(west))/2;
        } else /* (west >= 0) */ {
            double toAdd = (global.LONGITUDE_MAXIMUM - west) + (global.LONGITUDE_MAXIMUM - Math.abs(east))/2;
            double tmp = west + toAdd;
            if(tmp > global.LONGITUDE_MAXIMUM) {
                return (global.LONGITUDE_MINIMUM + (tmp - global.LONGITUDE_MAXIMUM));
            } else {
                return tmp;
            }
        }
    }

    private boolean checkResult(Point result) {
        return result.x < (0 + lr_margin) || result.x > (width - lr_margin) || result.y < (0 + tb_margin) || result.y > (height - tb_margin);
    }
    /**
     * Calculates a bounding box using the previously calculated vertices of the bounding area. There are multiple steps involved.
     *
     * 1. Find the vertex that will be used as center.
     * 2. Stretch in all four directions from this center and strop the stretching when next increment will take any of the corners
     *      off the bounding bix.
     * 3. Now stretch east, west, north and south edges as much as possible with same constraint.
     * @param vertices of the bounding area (4)
     * @param geoBounds is the output
     */
    private void buildBoundingBox_(IGeoPosition[] vertices, IGeoBounds geoBounds, IGeoPosition geometricCenter) {

        setNorthAndSouth(vertices, geoBounds);  // Simply to figure out value for increment based on span.
        IGeoPosition center;

        // Find the point that will be used as center.
        List<IGeoPosition> corners = new ArrayList<>();
        int ii;
        for(ii = 0; ii < vertices.length; ii++) {
            corners.add(vertices[ii]);
        }

        center = GeoLibrary.getCenter(corners);

        if(cameraOnScreen) {
            corners.clear();
            corners.add(center);
            corners.add(new MyGeoPosition(camera.getLatitude(), camera.getLongitude()));
            center = GeoLibrary.getCenter(corners);
        }

        Log.d(TAG, "adjustEastAndWest In NEWS " + geoBounds.getNorth() + " " + geoBounds.getEast() + " " + geoBounds.getWest() + " " + geoBounds.getSouth());
        Point result = new Point();

        double increment = (geoBounds.getNorth() - geoBounds.getSouth()) * INCREMENT_FACTOR;
        double increment_lon_f = 1.0;
        if(width > height) {
            increment_lon_f = (double) width / (double) height;
            Log.d(TAG, "increment_lon_f " + increment_lon_f);
        }
        if((cornersTouched > 0) && (cornersTouched <= 2)) {
            increment_lon_f *= 2;
            Log.d(TAG, "increment_lon_f " + increment_lon_f);
        }

        double increment_lat_f = 1.0;
        if(height > width) {
            increment_lat_f = (double) height / (double) width;
            Log.d(TAG, "increment_lat_f " + increment_lat_f);
        }

        // Need to investigate if we can always use the geometric Center
        if(camera.getTilt() > 45.0) {
            center = geometricCenter;
        }
        stretch(center, geoBounds, increment * increment_lat_f, increment * increment_lon_f);

        // Following is the refinement where we try to stretch each edge as much as possible to make the bounding box
        // as large as possible within the provided constraints.

        if(getLongitudeSpan(geoBounds.getEast(), geoBounds.getWest()) < MAX_LONGITUDE_SPAN) {
            geoBounds.setWest(adjustLongitude(geoBounds.getNorth(), geoBounds.getSouth(), geoBounds.getWest(), increment, true));
            geoBounds.setEast(adjustLongitude(geoBounds.getNorth(), geoBounds.getSouth(), geoBounds.getEast(), increment, false));
        }

        if(geoBounds.getNorth() - geoBounds.getSouth() < MAX_LATITUDE_SPAN) {
            geoBounds.setSouth(adjustLatitude(geoBounds.getEast(), geoBounds.getWest(), geoBounds.getNorth(), geoBounds.getSouth(), increment, true));
            geoBounds.setNorth(adjustLatitude(geoBounds.getEast(), geoBounds.getWest(), geoBounds.getSouth(), geoBounds.getNorth(), increment, false));
        }

        if((geoBounds.getNorth() - geoBounds.getSouth()) >= 15.0) {
            geoBounds.setSouth(geoBounds.getSouth() + ((geoBounds.getNorth() - geoBounds.getSouth()) * .05));
        } else {
            geoBounds.setSouth(geoBounds.getSouth() + ((geoBounds.getNorth() - geoBounds.getSouth()) * .1));
        }
        Log.d(TAG, "buildBoundingBox_ Out NEWS " + ii + " " + geoBounds.getNorth() + " " + geoBounds.getEast() + " " + geoBounds.getWest() + " " + geoBounds.getSouth());
    }

    private void stretch(IGeoPosition center, IGeoBounds geoBounds, double lat_increment, double lon_increment) {

        geoBounds.setNorth(center.getLatitude());
        geoBounds.setSouth(center.getLatitude());
        geoBounds.setWest(center.getLongitude());
        geoBounds.setEast(center.getLongitude());

        Point result = new Point();

        // Stretch all the corners making sure none goes off the screen.
        for(int ii = 0; ii < MAX_CORNER_ITERATIONS; ii++) {
            double north = geoBounds.getNorth() + (lat_increment);
            double south = geoBounds.getSouth() - (lat_increment);
            double west = geoBounds.getWest() - (lon_increment);
            double east = geoBounds.getEast() + (lon_increment);

            // Check lat/long constrains and span constraints. Any violation strops the stretching process.
            if(north >= global.LATITUDE_MAXIMUM || south <= global.LATITUDE_MINIMUM) {
                break;
            }

            if((north - south >= MAX_LATITUDE_SPAN) || (getLongitudeSpan(east, west) > MAX_LONGITUDE_SPAN)) {
                break;
            }
            if(west < global.LONGITUDE_MINIMUM) { west += 360.0; }
            if(east > global.LONGITUDE_MAXIMUM) { east -= 360.0; }

            // Check each corner. Any single violation stops the corner stretching process.
            if (!mapController.groundPositionToScreenPoint(north, west, result) || checkResult(result)) {
                break;
            }

            if (!mapController.groundPositionToScreenPoint(north, east, result) || checkResult(result)) {
                break;
            }

            if (!mapController.groundPositionToScreenPoint(south, east, result) || checkResult(result)) {
                break;
            }

            if (!mapController.groundPositionToScreenPoint(south, west, result) || checkResult(result)) {
                break;
            }

            // We are purposely doing the updates before the screen checks are made as we want to maximize the
            // Bounding Box for features like MGRS grid lines. If you see any issues you can move this update after the
            // following four checks.
            geoBounds.setNorth(north);
            geoBounds.setSouth(south);
            geoBounds.setEast(east);
            geoBounds.setWest(west);
        }
    }

    /**
     * Tries to stretch either the left or right edge of the bounding box as much as possible without letting the adjacent corners
     * go off screen
     * @param north edge of the bounding box, this will be held constant
     * @param south edge of the bounding bix, this will be held constant
     * @param longitude to be moved
     * @param increment
     * @param isWest is true if we are adjusting west edge of the box.
     * @return
     */
    private double adjustLongitude(double north, double south, double longitude, double increment, boolean isWest) {

        Point result = new Point();
        double updatedLongitude = longitude;
        double middleLatitude = (south + (north - south)/2);
        for(int ii = 0; ii < MAX_LAT_LONG_ITERATIONS; ii++) {

            if(isWest) {
                longitude = longitude - increment;
                if (longitude < global.LONGITUDE_MINIMUM) {
                    longitude += 360.0;
                }
            } else {
                longitude = longitude + increment;
                if(longitude > global.LONGITUDE_MAXIMUM) { longitude -= 360.0; }
            }

            if (!mapController.groundPositionToScreenPoint(middleLatitude, longitude, result) || checkResult(result)) {
                break;
            }

            // We are purposely doing the update before the screen checks are made as we want to maximize the
            // Bounding Box for features like MGRS grid lines. If you see any issues you can move this update after the
            // following two checks.
            updatedLongitude = longitude;
        }
        return updatedLongitude;
    }

    /**
     * Adjusts the top or bottom edge of the bounding box, basically trying to stretch as much as possible without letting the
     * corresponding adjacent corners off the screen.
     * @param east longitude, it will be held constant
     * @param west longitude, it will be held constant
     * @param northOrSouth top or bottom edge
     * @param latitude that is being adjusted
     * @param increment
     * @param isSouth set to true if adjusting south edge of the bounding box.
     * @return
     */
    private double adjustLatitude(double east, double west, double northOrSouth, double latitude, double increment, boolean isSouth) {
        Point result = new Point();
        double updatedLatitude = latitude;

        double middleLongitude = getMidLongitude(east, west);
        for(int ii = 0; ii < MAX_LAT_LONG_ITERATIONS; ii++) {

            if(isSouth) {
                latitude = latitude - increment;
                if (latitude <= global.LATITUDE_MINIMUM) {
                    break;
                }
            } else {
                latitude = latitude + increment;
                if(latitude >= global.LATITUDE_MAXIMUM) {
                    break;
                }
            }

            if(latitude - northOrSouth >= MAX_LATITUDE_SPAN) {
                break;
            }

            if (!mapController.groundPositionToScreenPoint(latitude, middleLongitude, result) || checkResult(result)) {
                break;
            }
//            if (!mapController.groundPositionToScreenPoint(latitude, east, result) || checkResult(result)) {
//                break;
//            }
//
//            if (!mapController.groundPositionToScreenPoint(latitude, west, result) || checkResult(result)) {
//                break;
//            }
            // We are purposely doing the update before the screen checks are made as we want to maximize the
            // Bounding Box for features like MGRS grid lines. If you see any issues you can move this update after the
            // following two checks.
            updatedLatitude = latitude;
        }
        return updatedLatitude;
    }
    /**
     * Convenience class.
     */
    class MyGeoPosition extends GeoPosition {
        MyGeoPosition(double latitude, double longitude) {
            setLatitude(latitude);
            setLongitude(longitude);
        }
    }
}
