package mil.emp3.worldwind.utils;

import android.graphics.Point;
import android.util.Log;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.mapengine.interfaces.IMapInstance;
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
    private final IMapInstance mapInstance;
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
    private final static double USE_GEOMETRIC_CENTER_FOR_TILT_GT = 45.0;
    private final static double POLE_OFFEST = 1.0; // setting north/south as 90/-90 causes calculation breakdown in places like MGRS.

    private BoundingBoxGeneration(MapInstance mapInstance, boolean cameraOnScreen, int cornersTouched) {
        this.mapInstance = mapInstance;
        mapController = mapInstance.getMapController();
        width = mapInstance.getWW().getWidth();
        height = mapInstance.getWW().getHeight();
        this.cameraOnScreen = cameraOnScreen;
        camera = mapInstance.getCamera();
        this.cornersTouched = cornersTouched;

        if(camera.getAltitude() < 1000000) {
            lr_margin = (int) (.005 * width);
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
        Log.i(TAG, "buildBoundingBox NEWS " + geoBounds.getNorth() + " " + geoBounds.getEast() + " " + geoBounds.getWest() + " " + geoBounds.getSouth());
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

    private boolean checkResultNoMargin(Point result) {
        return result.x < 0 || result.x > width || result.y < 0 || result.y > height;
    }

    /**
     * If complete Globe is visible then we build the bounding box using the geometric center and moving 60 degrees out in each direction.
     *
     * @param geoBounds
     * @param geometricCenter
     * @return
     */
    private boolean boundsForEntireGlobeVisible(IGeoBounds geoBounds, IGeoPosition geometricCenter) {
        if(0 == cornersTouched && cameraOnScreen && -BoundsGeneration.TILT_TOLERANCE < camera.getTilt() && camera.getTilt() < BoundsGeneration.TILT_TOLERANCE) {
            geoBounds.setNorth(geometricCenter.getLatitude() + MAX_LATITUDE_SPAN/2);
            if(geoBounds.getNorth() > global.LATITUDE_MAXIMUM) {
                geoBounds.setNorth(global.LATITUDE_MAXIMUM - POLE_OFFEST);
            }
            geoBounds.setEast(geometricCenter.getLongitude() + MAX_LONGITUDE_SPAN/2);
            if(geoBounds.getEast() > global.LONGITUDE_MAXIMUM) {
                geoBounds.setEast(global.LONGITUDE_MINIMUM + (geoBounds.getEast() % global.LONGITUDE_MAXIMUM));
            }
            geoBounds.setWest(geometricCenter.getLongitude() - MAX_LONGITUDE_SPAN/2);
            if(geoBounds.getWest() < global.LONGITUDE_MINIMUM) {
                geoBounds.setWest(geoBounds.getWest() + (2 * global.LONGITUDE_MAXIMUM));
            }
            geoBounds.setSouth(geometricCenter.getLatitude() - MAX_LATITUDE_SPAN/2);
            if(geoBounds.getSouth() < global.LATITUDE_MINIMUM) {
                geoBounds.setSouth(global.LATITUDE_MINIMUM + POLE_OFFEST);
            }
            return true;
        }
        return false;
    }

    private boolean boundsForSmallCameraAngles(IGeoBounds geoBounds) {
        if (null == camera) {
            return false;
        }

        boolean ret = false;

        if (((Math.abs(camera.getHeading()) <= 5.0) || (Math.abs(camera.getHeading()) >= 355.0)) &&
                (Math.abs(camera.getTilt()) <= 5.0) &&
                (Math.abs(camera.getRoll()) <= 5.0)) {

            gov.nasa.worldwind.geom.Position centerNorth = new gov.nasa.worldwind.geom.Position();
            if (mapController.screenPointToGroundPosition(width / 2, 0, centerNorth)) {
                gov.nasa.worldwind.geom.Position centerSouth = new gov.nasa.worldwind.geom.Position();
                if (mapController.screenPointToGroundPosition(width / 2, height, centerSouth)) {
                    geoBounds.setNorth(centerNorth.latitude);
                    geoBounds.setSouth(centerSouth.latitude);
                    ret = true;
                }
            }

            gov.nasa.worldwind.geom.Position centerWest = new gov.nasa.worldwind.geom.Position();
            if (mapController.screenPointToGroundPosition(0, height / 2, centerWest)) {
                gov.nasa.worldwind.geom.Position centerEast = new gov.nasa.worldwind.geom.Position();
                if (mapController.screenPointToGroundPosition(width, height / 2, centerEast)) {
                    geoBounds.setWest(centerWest.longitude);
                    geoBounds.setEast(centerEast.longitude);
                    ret = true;
                }
            }
        }

        return ret;
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
        Log.i(TAG, "start buildBoundingBox_ ");

        // Consider the simple case where the entire globe is visible.
        if (boundsForSmallCameraAngles(geoBounds)) {
            return;
        } else if(boundsForEntireGlobeVisible(geoBounds, geometricCenter)) {
            return;
        }

        setNorthAndSouth(vertices, geoBounds);  // Simply to figure out value for increment based on span.
        IGeoPosition center;

        // Find the point that will be used as center.
        // Need to investigate if we can always use the geometric Center
        if (camera.getTilt() > USE_GEOMETRIC_CENTER_FOR_TILT_GT) {
            center = geometricCenter;
        } else {
            List<IGeoPosition> corners = new ArrayList<>();
            int ii;
            for (ii = 0; ii < vertices.length; ii++) {
                corners.add(vertices[ii]);
            }
            center = GeographicLib.getCenter(corners);

            if (cameraOnScreen) { // then take the center of the vertices and camera.
                corners.clear();
                corners.add(center);
                corners.add(new EmpGeoPosition(camera.getLatitude(), camera.getLongitude()));
                center = GeographicLib.getCenter(corners);
            }
        }

        double increment = (geoBounds.getNorth() - geoBounds.getSouth()) * INCREMENT_FACTOR;

        // Depending on height, width and number of corners that show earth adjust the increment factor.
        // This logic really needs to be vetted as it is based purely on experimentation. I am working on
        // a 9 inch tablet.
        double increment_lon_f = 1.0;
        if(width > height) {
            increment_lon_f = (double) width / (double) height;
            Log.v(TAG, "increment_lon_f " + increment_lon_f);
        }
        if((cornersTouched > 0) && (cornersTouched <= 2)) {
            increment_lon_f *= 2;
            Log.v(TAG, "increment_lon_f " + increment_lon_f);
        }

        double increment_lat_f = 1.0;
        if(height > width) {
            increment_lat_f = (double) height / (double) width;
            Log.v(TAG, "increment_lat_f " + increment_lat_f);
        }

        // Stretch along all four corners
        stretch(center, geoBounds, increment * increment_lat_f, increment * increment_lon_f);

        // Following is the refinement where we try to stretch each edge as much as possible to make the bounding box
        // as large as possible within the provided constraints.

        if(getLongitudeSpan(geoBounds.getEast(), geoBounds.getWest()) < MAX_LONGITUDE_SPAN) {
            geoBounds.setWest(adjustLongitude(geoBounds.getNorth(), geoBounds.getSouth(), geoBounds.getWest(), increment, true, geoBounds.getEast()));
            geoBounds.setEast(adjustLongitude(geoBounds.getNorth(), geoBounds.getSouth(), geoBounds.getEast(), increment, false, geoBounds.getWest()));
        }

        if(geoBounds.getNorth() - geoBounds.getSouth() < MAX_LATITUDE_SPAN) {
            geoBounds.setSouth(adjustLatitude(geoBounds.getEast(), geoBounds.getWest(), geoBounds.getNorth(), geoBounds.getSouth(), increment, true));
            geoBounds.setNorth(adjustLatitude(geoBounds.getEast(), geoBounds.getWest(), geoBounds.getSouth(), geoBounds.getNorth(), increment, false));
        }
    }

    /**
     * Stretched all four corners out equally until visibility criteria is met.
     * @param center
     * @param geoBounds
     * @param lat_increment
     * @param lon_increment
     */
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
    private double adjustLongitude(double north, double south, double longitude, double increment, boolean isWest, double otherLongitude) {

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

            if(isWest) {
                if (getLongitudeSpan(otherLongitude, longitude) > MAX_LONGITUDE_SPAN) {
                    break;
                }
            } else {
                if (getLongitudeSpan(longitude, otherLongitude) > MAX_LONGITUDE_SPAN) {
                    break;
                }
            }
            if (!mapController.groundPositionToScreenPoint(middleLatitude, longitude, result) || checkResult(result)) {
                break;
            }

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

            if(isSouth) {
                if (!mapController.groundPositionToScreenPoint(latitude, middleLongitude, result) || checkResultNoMargin(result)) {
                    if (!mapController.groundPositionToScreenPoint(latitude, east, result) || checkResultNoMargin(result)) {
                        break;
                    }

                    if (!mapController.groundPositionToScreenPoint(latitude, west, result) || checkResultNoMargin(result)) {
                        break;
                    }
                }
            }
            else if (!mapController.groundPositionToScreenPoint(latitude, middleLongitude, result) || checkResult(result)) {
                break;
            }
            updatedLatitude = latitude;
        }
        return updatedLatitude;
    }

}
