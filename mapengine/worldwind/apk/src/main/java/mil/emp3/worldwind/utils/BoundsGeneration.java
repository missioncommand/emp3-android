package mil.emp3.worldwind.utils;

import android.graphics.Point;
import android.os.Looper;
import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import gov.nasa.worldwind.geom.Position;
import mil.emp3.api.interfaces.IEmpBoundingArea;
import mil.emp3.api.utils.EmpBoundingArea;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.controller.PickNavigateController;

/**
 * Generates a bounding polygon for the visible map. Aim is to return four points defining a quadrilateral.
 *
 * Here is how this should work:
 *
 * When camera is moved (pan/zoom/rotate/tilt etc) the Emp3NavigationListener will generate a viewChangeEvent with 'bounds' parameter
 * set to null. The 'core' (MapStatus) will detect the null value in setBounds and post a request to the UI thread to calculate
 * bounds via MapInstance.getMapBounds().
 *
 * In the 'core' MilStdRenderer.java will fetch the bounds via the IMap API. There will be no recalculation of the bounds at that point.
 * We are presuming that when view is changed the bounds will get calculated before NASA code invokes the renderer to redraw the
 * symbols on the map. If that assumption is incorrect then we will have to devise a different method.
 */
public class BoundsGeneration {

    private static String TAG = BoundsGeneration.class.getSimpleName();

    private final static int CORNERS = 4;
    private final static int NW = 0;       // x = 0, y = 0
    private final static int SW = 1;       // x = 0, y = height
    private final static int SE = 2;       // x = width, y = height
    private final static int NE = 3;       // x = width, y = 0;

    private final static double lc = 0.05;  // Least Count on the grid. (5 percent of width/height

    private static List<GridPoints> gridPoints = new ArrayList<>();

    /**
     * Make creation of GridPoints thread safe. Is this really required, we allow getBounds on UI thread only?
     */
    private static ReentrantLock lock = new ReentrantLock();

    /**
     * If the view hodling the Map is being resized then we may end up with GridPoints for many different width and height, so
     * we must clean up if we have more than max allowed.
     */
    private static int MAX_GRID_POINTS = 10;

    /**
     * In the case where two corners of the view show sky, we may have to make an adjustment to the calculated polygon
     * based on the ration of the side that is on the edge to the two adjacent perpandicular sides.
     */
    private static int W2H_RATIO_TOLERANCE = 2;

    /**
     * if W2H_RATIO_TOLERANCE is not met then we try to make the adjustments to reach ratio of .8
     */
    private static double HEIGHT_ADJUSTMENT_FACTOR = .8;

    /**
     * We will store the previously calculated bounding area that can be returned with things like VIEW_IN_MOTION.
     * We should recalculate bounds only when VIEW_MOTION_STOPPED is generated. This Map is always accessed on UI thread so
     * no need to make it a ConcurrentHashMap.
     */
    private static Map<MapInstance, IEmpBoundingArea> currentBoundingArea = new HashMap<>();
    /**
     * Made this a method to allow for any adjustments in future.
     * @return
     */
    private static int getOriginX() {
        return 0;
    }

    /**
     * Made this a method to allow for any adjustments in future.
     * @return
     */
    private static int getOriginY() {
        return 0;
    }

    /**
     * Need to figure out why this is required ....
     * @return
     */
    private static int getOriginXForGrid() { return 1; }
    private static int getOriginYForGrid() { return 1; }
    /**
     * When stepping through the view space we need to select the size of the step. lc defines the percentage of
     * width or height as size of the step.
     * @return
     */
    private static double getLc() {
        return lc;
    }

    /**
     * Calculates the size of the step along X axis using width.
     * @param mapInstance
     * @return
     */
    private static int getDeltaX(MapInstance mapInstance) {
        return (int) (mapInstance.getWW().getWidth() * getLc());
    }

    /**
     * Calculates the size of the step along Y axis using height.
     * @param mapInstance
     * @return
     */
    private static int getDeltaY(MapInstance mapInstance) {
        return (int) (mapInstance.getWW().getHeight() * getLc());
    }

    /**
     * Map is in motion, stored Bounding Area is no longer valid, it will have to be recalculated when map motion is stopped.
     * @param mapInstance
     */
    public static void mapInMotion(MapInstance mapInstance) {
        currentBoundingArea.put(mapInstance, null);
    }

    /**
     * This is invoked when MAP is READY by the MapInstance object to force initial bounds generation. We could initialize
     * other dat here if required. If in future we need to create a BoundsGeneration object per MapEngine then this would be
     * a good place to do that, ofcourse then all those static would go away.
     * @param mapInstance
     * @return
     */
    public static void initialize(MapInstance mapInstance) {
        getBounds(mapInstance);
    }
    public static IEmpBoundingArea getCurrentBoundingArea(MapInstance mapInstance) {
        return currentBoundingArea.get(mapInstance);
    }

    public static IEmpBoundingArea getBounds(MapInstance mapInstance) {
        try {
            if(Looper.myLooper() == Looper.getMainLooper()) {
                int[] cornersTouched = new int[1];
                cornersTouched[0] = 0;
                IGeoPosition[] corners = getBoundingPolygon(mapInstance, cornersTouched);
                if ((null != corners) && (EmpBoundingArea.REQUIRED_VERTICES == corners.length)) {

                    Point cameraPoint = new Point();
                    boolean cameraOnScreen = false;
                    if(mapInstance.getMapController().groundPositionToScreenPoint(mapInstance.getCamera().getLatitude(), mapInstance.getCamera().getLongitude(),
                            cameraPoint)) {
                        if((cameraPoint.x > 0 && cameraPoint.x < mapInstance.getWW().getWidth() &&
                                cameraPoint.y > 0 && cameraPoint.y < mapInstance.getWW().getHeight())) {
                            cameraOnScreen = true;
                        }
                    }

                    // Find geometric center of the vertices and then the corresponding HeoPosition. Store this GeoPosition
                    // in the EmpBoundingArea as it will be required by the emp-core to support Draw capability.
                    IGeoPosition geometricCenter = getGeometricCenter(mapInstance, corners);

                    IGeoBounds geoBounds = new GeoBounds();
                    BoundingBoxGeneration.buildBoundingBox(mapInstance, corners, geoBounds, cameraOnScreen, cornersTouched[0], geometricCenter);

                    IEmpBoundingArea boundingArea = new EmpBoundingArea(mapInstance.getCamera(), cameraOnScreen,
                            corners[0], corners[1], corners[2], corners[3], geoBounds, geometricCenter);

                    currentBoundingArea.put(mapInstance, boundingArea);

                    return boundingArea;
                }
            } else {
                Log.e(TAG, "This method must be invoked on UI thread");
            }
        } catch (Exception e) {
            Log.e(TAG, "getBounds " + e.getMessage(), e);
        }

        Log.e(TAG, "a null value is being returned for bounds, this is NOT necessarily an error, tilt > 45 ?");
        return null;
    }

    /**
     * Finds the geometric center of the four vertices. Then finds the Geographic Position of that point.
     * @param mapInstance
     * @param vertices
     * @return
     */
    private static IGeoPosition getGeometricCenter(MapInstance mapInstance, IGeoPosition[] vertices) {

        PickNavigateController mapController = mapInstance.getMapController();
        Point centerPoint = new Point();
        IGeoPosition geometricCenter = new GeoPosition();

        int count = 0;
        for(int ii = 0; ii < vertices.length; ii++) {
            Point vPoint = new Point();
            if(mapController.groundPositionToScreenPoint(vertices[ii].getLatitude(), vertices[ii].getLongitude(), vPoint)) {
                centerPoint.x += vPoint.x;
                centerPoint.y += vPoint.y;
                count++;
                Log.v(TAG, "vPoint " + vPoint.x + " " + vPoint.y);
            }
        }

        if(count > 0) {
            centerPoint.x /= count;
            centerPoint.y /= count;
            Log.v(TAG, "centerPoint count " + centerPoint.x + " " + centerPoint.y + " " + count);
            Position positionAtCenterPoint = new Position();
            if(mapController.screenPointToGroundPosition(centerPoint.x, centerPoint.y, positionAtCenterPoint)) {
                geometricCenter.setLatitude(positionAtCenterPoint.latitude);
                geometricCenter.setLongitude(positionAtCenterPoint.longitude);
                Log.v(TAG, "center " + geometricCenter.getLatitude() + " " + geometricCenter.getLongitude());
            }
        }

        return geometricCenter;
    }

    /**
     * Figures out how many corners of the view show a sky. Based on that information invokes appropriate method to calculate the
     * polygon.
     * @param mapInstance
     * @return
     */
    private static IGeoPosition[] getBoundingPolygon(MapInstance mapInstance, int[] cornersTouched) {
        int cornersFound = 0;
        try {
            IGeoPosition corners[] = new IGeoPosition[CORNERS];
            boolean status = false;

            // Check how many corners we have a map rather than sky/space
            cornersFound = getCornersTouched(mapInstance, corners);
            cornersTouched[0] = cornersFound;
            Log.d(TAG, "getBoundingPolygon cornersFound " + cornersFound);
            switch(cornersFound) {
                case CORNERS:
                    // All corners have a map, so this is the most common and probably relatively easy scenario.
                    // corners are already populated with the bounding polygon.
                    // cornersToBound(corners);
                    status = true;
                    break;
                case 1:
                    status = processSingleCorner(mapInstance, corners);
                    break;
                case 2:
                    status = processTwoCorners(mapInstance, corners);
                    break;
                case 3:
                    status = processThreeCorners(mapInstance, corners);
                    break;
                case 0:
                    if (-1.0 < mapInstance.getCamera().getTilt() && mapInstance.getCamera().getTilt() < 1.0) {
                        // Globe is at the center of the view but roll and heading may not be zero.
                        status = processGlobeInCenter(mapInstance, corners);
                    } else {
                        status = processGrid(mapInstance, corners);
                    }
                    break;
                default:
                    Log.e(TAG, "getCornersTouched returned " + cornersFound);
            }

            if(status) {
                return corners;
            }
        } catch (Exception e) {
            Log.e(TAG, "cornersFound " + cornersFound + " " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * We are trying to figure out if a specific corner has a map or sky/space (map may be tilted/rotated). If we do find
     * such corners then we also get the corresponding latitude/longitude of the point.
     *
     * @param mapInstance
     * @param corners
     * @return
     */
    private static int getCornersTouched(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();
        int origin_x = getOriginX();   // This is the NW corner when heading is zero
        int origin_y = getOriginY();   // This is the NW corner when heading is zero
        int cornersFound = 0;

        android.graphics.Point[] vertices = new android.graphics.Point[CORNERS];

        vertices[NW] = new android.graphics.Point(origin_x, origin_y);
        vertices[SW] = new android.graphics.Point(origin_x, origin_y + height);
        vertices[SE] = new android.graphics.Point(origin_x + width, origin_y + height);
        vertices[NE] = new android.graphics.Point(origin_x + width , origin_y);

        // Now fetch the geographical points for the corners of the relevant rectangle.
        // There is an issue with underlying NASA methods where it reports a geographical point where there is actually
        // sky. This happens when camera is at an altitude less than (approximately) 100000 meters.
        // So as a work around we do a reverse check to verify the answer returned by screenPointToGroundPosition.
        // I have filed a report with NASA.

        Position pos = new Position();
        Point result = new Point();
        for (int ii = 0; ii < vertices.length; ii++) {
            if (mapController.screenPointToGroundPosition(vertices[ii].x, vertices[ii].y, pos)) {
                if(mapController.groundPositionToScreenPoint(pos.latitude, pos.longitude, result)) {
                    corners[ii] = new EmpGeoPosition(pos.latitude, pos.longitude);
                    Log.v(TAG, "x/y/l/n " + vertices[ii].x + " " + vertices[ii].y + " " + pos.latitude + " " + pos.longitude);
                    cornersFound++;
                }
            }
        }

        return cornersFound;
    }

    /**
     * Three corners of the view show sky and one corner shows the map. You will need to tilt and roll the camera to get this
     * scenario. Our job is to figure out the three geographical points that are not at the view corner.
     * @param mapInstance
     * @param corners
     * @return
     */
    private static boolean processSingleCorner(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();
        int origin_x = getOriginX();
        int origin_y = getOriginY();

        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        // Locate the view corner that doesn't show the sky. Then traverse from this corner towards the adjacent corner(s) (findEarth2SkyTransition)
        // looking for a place where we make a transition from map (earth) to sky and you have found two points.
        // Traverse from the opposite corner towards this corner (findSky2EarthTransition) looking for the place where there is a transition
        // from sky to map (earth) and this will give you the third point of the polygon.
        if(corners[NW] != null) {
            corners[NE] = findEarth2SkyTransition(mapController, origin_x, origin_y, delta_x, 0, width, height);
            corners[SE] = findSky2EarthTransition(mapController, width, height, -delta_x, -delta_y, width, height);
            corners[SW] = findEarth2SkyTransition(mapController, origin_x, origin_y, 0, delta_y, width, height);
        } else if(corners[SW] != null) {
            corners[NW] = findEarth2SkyTransition(mapController, origin_x, height, 0, -delta_y, width, height);
            corners[NE] = findSky2EarthTransition(mapController, width, origin_y, -delta_x, delta_y, width, height);
            corners[SE] = findEarth2SkyTransition(mapController, origin_x, height, delta_x, 0, width, height);
        } else if(corners[NE] != null) {
            corners[SE] = findEarth2SkyTransition(mapController, width, origin_y, 0, delta_y, width, height);
            corners[SW] = findSky2EarthTransition(mapController, origin_x, height, delta_x, -delta_y, width, height);
            corners[NW] = findEarth2SkyTransition(mapController, width, origin_y, -delta_x, 0, width, height);
        } else if(corners[SE] != null) {
            corners[SW] = findEarth2SkyTransition(mapController, width, height, -delta_x, 0, width, height);
            corners[NW] = findSky2EarthTransition(mapController, origin_x, origin_y, delta_x, delta_y, width, height);
            corners[NE] = findEarth2SkyTransition(mapController, width, height, 0, -delta_y, width, height);
        } else {
            Log.e(TAG, "NO CORNER FOUND");
            return false;
        }
        return true;
    }

    /**
     * Finds a point on the map where there is a transition from earth to sky. Values of delta_x & delta_y decide the
     * direction of search and size of the step.
     *
     * @param mapController
     * @param start_x - Starting position
     * @param start_y - Starting position
     * @param delta_x - Step size
     * @param delta_y - Step size
     * @param width - width of the view
     * @param height - height of the view.
     * @param positionPoint - x, y for returned GeoPosition
     * @return
     */
    private static IGeoPosition findEarth2SkyTransition(PickNavigateController mapController, int start_x, int start_y, int delta_x, int delta_y, int width, int height, Point positionPoint) {

        IGeoPosition corner = null;
        boolean found = false;
        Position pos = new Position();
        Position prevPos = null;
        Point result = new Point();

        // Following three block can actually be collapsed into one if we change the comparison to <= and =>, but for now
        // we are keeping them separate. Until then whatever changes you make in one block have to be applied to other two blocks.

        if((0 != delta_x) || (0 != delta_y)) {

            // Save the initial position where there is earth so that we can deal with the special case of first step transition.
            if (mapController.screenPointToGroundPosition(start_x, start_y, pos)) {
                if(null == prevPos) {
                    prevPos = new Position(pos.latitude, pos.longitude, pos.altitude);
                } else {
                    prevPos.latitude = pos.latitude;
                    prevPos.longitude = pos.longitude;
                    prevPos.altitude = pos.altitude;
                }
                positionPoint.set(start_x, start_y);
            }

            int current_x = start_x + delta_x;
            int current_y = start_y + delta_y;
            for (; current_x <= width && current_x >= 0 && current_y <= height && current_y >= 0 && !found;
                 current_x += delta_x, current_y += delta_y) {
                if (!mapController.screenPointToGroundPosition(current_x, current_y, pos)) {
                    if(prevPos != null) {
                        corner = new EmpGeoPosition(prevPos.latitude, prevPos.longitude);
                        Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
                        found = true;
                    }
                } else {
                    // This check is for NASA issue related to false positive.
                    if(mapController.groundPositionToScreenPoint(pos.latitude, pos.longitude, result) &&
                            result.x >= 0 && result.x <= width && result.y >= 0 && result.y <= height) {
                        if(null == prevPos) {
                            prevPos = new Position(pos.latitude, pos.longitude, pos.altitude);
                        } else {
                            prevPos.latitude = pos.latitude;
                            prevPos.longitude = pos.longitude;
                            prevPos.altitude = pos.altitude;
                        }
                        positionPoint.set(result.x, result.y);
                    } else {
                        if(prevPos != null) {
                            corner = new EmpGeoPosition(prevPos.latitude, prevPos.longitude);
                            Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
                            found = true;
                        }
                    }
                }
            }

            // Following takes care of the condition where transition occurs before the first increment. lower the value of
            // lc less the probability of that happening, but that will affect the performance.
            if((null == corner) && (prevPos != null)) {
                corner = new EmpGeoPosition(prevPos.latitude, prevPos.longitude);
                Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
            }

        } else {
            Log.e(TAG, "findEarth2SkyTransition was invoked with both delta_x and delta_y set to zero.");
        }
        return corner;
    }

    /**
     * Finds a point on the map where there is a transition from earth to sky. Values of delta_x & delta_y decide the
     * direction of search and size of the step.
     *
     *
     * @param mapController
     * @param start_x - Starting position
     * @param start_y - Starting position
     * @param delta_x - Step size
     * @param delta_y - Step size
     * @param width - width of the view
     * @param height - height of the view.
     * @return
     */
    private static IGeoPosition findEarth2SkyTransition(PickNavigateController mapController, int start_x, int start_y, int delta_x, int delta_y, int width, int height) {
        Point positionPoint = new Point();
        return findEarth2SkyTransition(mapController, start_x, start_y, delta_x, delta_y, width, height, positionPoint);
    }

    /**
     * If a side of the 'quadrilateral' is more than twice the length of two adjacent sides then make an adjustment.
     * This happens when the globe is on one of the edges of the view. In this case two corners are showing the earth and globe
     * intersects the two edges very close to the corners that are showing the earth.
     *
     * @param polygonBaseLength
     * @param polygonLeftLength
     * @param polygonRightLength
     * @return
     */
    private static boolean processTwoCornersAdjustmentRequired(int polygonBaseLength, int polygonLeftLength, int polygonRightLength) {
        if((polygonLeftLength > 0) && (polygonRightLength > 0)) {
            if (((polygonBaseLength / polygonLeftLength) > W2H_RATIO_TOLERANCE) && ((polygonBaseLength / polygonRightLength) > W2H_RATIO_TOLERANCE)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Tow view corners are showing sky and two are showing earth. Considering the geometry of the earth these have to be
     * adjacent corners.
     *
     * For each two corner case there is a special adjustment based on the height and width of the quadrilateral created. If the
     * width is more that two times the height then we adjust the coordinates to balance this out. Code contains the required
     * camera coordinates to reproduce this case.
     *
     * @param mapInstance
     * @param corners
     * @return
     */
    private static boolean processTwoCorners(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();
        int origin_x = getOriginX();
        int origin_y = getOriginY();
        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        Point positionPoint_1 = new Point();
        Point positionPoint_2 = new Point();

        // Locate the two adjacent corners that show the earth. Then traverse along the x or the y axis to find transition
        // from earth to sky.

        if((corners[NW] != null) && (corners[NE] != null)){
            corners[SE] = findEarth2SkyTransition(mapController, width, origin_y, 0, delta_y, width, height, positionPoint_1);
            corners[SW] = findEarth2SkyTransition(mapController, origin_x, origin_y, 0, delta_y, width, height, positionPoint_2);

            /*
             * This code is repeated for each of the four cases but no method was created because very specific actions are
             * required for each case.
             * To test set camera Camera 40.169, -114.001, 12558210, 10.791, 16.627, 179.9
             */

            if(processTwoCornersAdjustmentRequired(width, positionPoint_2.y, positionPoint_1.y)) {
                findEarth2SkyTransition(mapController, width/2 , 0, 0, delta_y, width, height, positionPoint_1);
                int newHeight = (int) (positionPoint_1.y * HEIGHT_ADJUSTMENT_FACTOR);
                corners[SE] = findEarth2SkyTransition(mapController, width/2, newHeight, delta_x, 0, width, height);
                corners[SW] = findEarth2SkyTransition(mapController, width/2, newHeight, -delta_x, 0, width, height);
            }
        } else if((corners[SW] != null) && (corners[SE] != null)) {
            corners[NE] = findEarth2SkyTransition(mapController, width, height, 0, -delta_y, width, height, positionPoint_1);
            corners[NW] = findEarth2SkyTransition(mapController, origin_x, height, 0, -delta_y, width, height, positionPoint_2);

            /*
             * Please see comments in the previous if block.
             * To test set camera Camera 40.169, -114.001, 12558210, 10.791, 16.627, 0
             */
            if(processTwoCornersAdjustmentRequired(width, height - positionPoint_2.y, height - positionPoint_1.y)) {
                findEarth2SkyTransition(mapController, width/2 , height, 0, -delta_y, width, height, positionPoint_1);
                int newHeight = (int) ((height - positionPoint_1.y) * HEIGHT_ADJUSTMENT_FACTOR);
                corners[NW] = findEarth2SkyTransition(mapController, width/2, height - newHeight, -delta_x, 0, width, height);
                corners[NE] = findEarth2SkyTransition(mapController, width/2, height - newHeight, delta_x, 0, width, height);
            }
        } else if((corners[NE] != null) && (corners[SE] != null)) {
            corners[NW] = findEarth2SkyTransition(mapController, width, origin_y, -delta_x, 0, width, height, positionPoint_1);
            corners[SW] = findEarth2SkyTransition(mapController, width, height, -delta_x, 0, width, height, positionPoint_2);

            /*
             * Please see comments in the first if block.
             * Camera 42.379, -112.363, 9835007, 11.944, 25.0 -90 set to test the following code.
             * Pass Right edge of the view, Bottom edge of the view, Top edge of the view
             */
            if(processTwoCornersAdjustmentRequired(height, width - positionPoint_2.x, width - positionPoint_1.x)) {
                findEarth2SkyTransition(mapController, width , height/2, -delta_x, 0, width, height, positionPoint_1);
                int newHeight = (int) ((width - positionPoint_1.x) * HEIGHT_ADJUSTMENT_FACTOR);
                corners[NW] = findEarth2SkyTransition(mapController, width - newHeight, height/2, 0, -delta_y, width, height);
                corners[SW] = findEarth2SkyTransition(mapController, width - newHeight, height/2, 0, delta_y, width, height);
            }
        } else if((corners[SW] != null) && (corners[NW] != null)) {
            corners[NE] = findEarth2SkyTransition(mapController, origin_x, origin_y, delta_x, 0, width, height, positionPoint_1);
            corners[SE] = findEarth2SkyTransition(mapController, origin_x, height, delta_x, 0, width, height, positionPoint_2);

            /*
             * Please see comments in the first if block.
             * Camera 42.379, -112.363, 9835007, 11.944, 25.0 90 set to test the following code.
             * Left edge of the view, Top edge of the view and Bottom edge of the view
             */
            if(processTwoCornersAdjustmentRequired(height, positionPoint_1.x, positionPoint_2.x)) {
                findEarth2SkyTransition(mapController, 0 , height/2, delta_x, 0, width, height, positionPoint_1);
                int newHeight = (int) (positionPoint_1.x * HEIGHT_ADJUSTMENT_FACTOR);
                corners[NE] = findEarth2SkyTransition(mapController, newHeight, height/2, 0, -delta_y, width, height);
                corners[SE] = findEarth2SkyTransition(mapController, newHeight, height/2, 0, delta_y, width, height);
            }
        } else {
            Log.e(TAG, "processTwoCorners TWO CORNERS NOT FOUND");
            return false;
        }

//        if(mapInstance.getCamera().getAltitude() < 1000) {
//            refineIt(mapInstance, corners);
//        }
        return true;
    }

    /**
     * This is currently unused.
     * @param mapInstance
     * @param corners
     */
    private static void refineIt(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();

        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();

        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);
        List<IGeoPosition> cornersFound = new ArrayList<>();

        for(int ii = 0; ii < corners.length; ii++) {
            if(null != corners[ii]) {
                cornersFound.add(corners[ii]);
            }
        }

        if(cornersFound.size() > 3) {
            IGeoPosition center = GeoLibrary.getCenter(cornersFound);

            Point result = new Point();
            if (mapController.groundPositionToScreenPoint(center.getLatitude(), center.getLongitude(), result) &&
                    result.x > 0 && result.y > 0 && result.x < width && result.y < height) {
                corners[NW] = findEarth2SkyTransition(mapController, result.x, result.y, -delta_x, -delta_y, width, height);
                corners[NE] = findEarth2SkyTransition(mapController, result.x, result.y, delta_x, -delta_y, width, height);
                corners[SE] = findEarth2SkyTransition(mapController, result.x, result.y, delta_x, delta_y, width, height);
                corners[SW] = findEarth2SkyTransition(mapController, result.x, result.y, -delta_x, delta_y, width, height);
            } else {
                Log.d(TAG, "Cannot refine further center is NOT on screen");
            }
        } else {
            Log.d(TAG, "Cannot refine further not enough points " + cornersFound.size());
        }
    }

    /**
     * This is very similar to findEarth2SkyTransition, except it traverses from a point that has sky towards an area that shows the earth and
     * stops at the transition.
     *
     * @param mapController
     * @param start_x
     * @param start_y
     * @param delta_x
     * @param delta_y
     * @param width
     * @param height
     * @return
     */
    private static IGeoPosition findSky2EarthTransition(PickNavigateController mapController, int start_x, int start_y, int delta_x, int delta_y, int width, int height) {
        IGeoPosition corner = null;
        boolean found = false;
        Position pos = new Position();
        Point result = new Point();

        if ((0 != delta_x) || (0 != delta_y)){
            int current_x = start_x + delta_x;
            int current_y = start_y + delta_y;
            for (; current_x < width && current_x > 0 && current_y < height && current_y > 0 && !found;
                 current_x += delta_x, current_y += delta_y) {

                // Note that we are checking the reverse to work around the NASA issue mentioned in getCornersTouched method.
                if (mapController.screenPointToGroundPosition(current_x, current_y, pos)) {
                    if(mapController.groundPositionToScreenPoint(pos.latitude, pos.longitude, result)) {
                        corner = new EmpGeoPosition(pos.latitude, pos.longitude);
                        Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
                        found = true;
                    }
                }
            }

        }
        return corner;
    }

    /**
     * One view corner is showing the sky and other three are showing the earth.
     *
     * @param mapInstance
     * @param corners
     * @return
     */
    private static boolean processThreeCorners(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();
        int origin_x = getOriginX();
        int origin_y = getOriginY();
        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        // Locate the corner that is showing the sky and traverse from that corner towards the opposite corner looking for a
        // transition from sky to earth.
        if(corners[NW] == null) {
            corners[NW] = findSky2EarthTransition(mapController, origin_x, origin_y, delta_x, delta_y, width, height);
        } else if(corners[SW] == null) {
            corners[SW] = findSky2EarthTransition(mapController, origin_x, height, delta_x, -delta_y, width, height);
        } else if(corners[NE] == null) {
            corners[NE] = findSky2EarthTransition(mapController, width, origin_y, -delta_x, delta_y, width, height);
        } else if(corners[SE] == null) {
            corners[SE] = findSky2EarthTransition(mapController, width, height, -delta_x, -delta_y, width, height);
        } else {
            Log.e(TAG, "processThreeCorners NULL CORNER NOT FOUND");
            return false;
        }

        return true;
    }

    /**
     * All four corners of the view are showing the sky and tilt is within range, so we are showing the entire globe in the view.
     *
     * @param mapInstance
     * @param corners
     */
    private static boolean processGlobeInCenter(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();
        int origin_x = getOriginX();
        int origin_y = getOriginY();
        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        List<IGeoPosition> cornersFound = new ArrayList<>();
        IGeoPosition corner;

        // Traverse from each corner towards the center of the view, find the transition from sky to earth and you have found one
        // point. Repeat this for all four corners.

        if(null != (corner = findSky2EarthTransition(mapController, width/2, origin_y, 0, delta_y, width, height))) {
            cornersFound.add(corner);
        }
        if(null != (corner = findSky2EarthTransition(mapController, width, height/2, -delta_x, 0, width, height)))  {
            cornersFound.add(corner);
        }
        if(null != (corner = findSky2EarthTransition(mapController, width/2, height, 0, -delta_y, width, height))) {
            cornersFound.add(corner);
        }
        if(null != (corner = corners[SE] = findSky2EarthTransition(mapController, origin_x, height/2, delta_x, 0, width, height))) {
            cornersFound.add(corner);
        }

        if(CORNERS != cornersFound.size()) {
            Log.e(TAG, "processGlobeInCenter couldn't find all four points");
        }

        if(0 != cornersFound.size()) {
            // Find the center of the four geographical points
            IGeoPosition center = GeoLibrary.getCenter(cornersFound);

            // Convert the center to geo point and traverse out diagonally to locate the desired polygon vertices.
            // We don't short circuit this by simply traversing from center of the sides initially as globe may not be
            // exactly at the center.

            Point result = new Point();
            if (mapController.groundPositionToScreenPoint(center.getLatitude(), center.getLongitude(), result)) {
                corners[NW] = findEarth2SkyTransition(mapController, result.x, result.y, -delta_x, -delta_y, width, height);
                corners[NE] = findEarth2SkyTransition(mapController, result.x, result.y, delta_x, -delta_y, width, height);
                corners[SE] = findEarth2SkyTransition(mapController, result.x, result.y, delta_x, delta_y, width, height);
                corners[SW] = findEarth2SkyTransition(mapController, result.x, result.y, -delta_x, delta_y, width, height);
                return true;
            }
        } else {
            Log.e(TAG, "processGlobeInCenter couldn't find ANY point");
        }
        return false;
    }

    /**
     * Check if specified point is on one of the view edges, this is part of Grid processing. Currently not used.
     * @param point
     * @param width
     * @param height
     * @return
     */
    private static boolean pointOnGridEdge(Point point, int width, int height) {
        if((getOriginXForGrid() == point.x) || (width == point.x) || (getOriginYForGrid() == point.y) || (height == point.y)) {
            return true;
        }
        return false;
    }

    /**
     * If globe is intersecting one of the edges then this method tries to move those points as far as possible. But it was discovered
     * that result actually reduces the Bounding Area as earth is tilted and latitude at the end is less than that in the middle.
     * So method is NOT used but kept here for future reference.
     *
     * processGrid will need to be updated if you want to make use of this method.
     * @param mapInstance
     * @param corners
     * @param cornerPoints
     * @param pointsOnEdge
     */
    private static void processGridPointOnEdge(MapInstance mapInstance, IGeoPosition corners[], Point cornerPoints[], List<Point> pointsOnEdge) {
        PickNavigateController mapController = mapInstance.getMapController();
        boolean pointsOnNWNEEdge = false;
        boolean pointsOnNESEEdge = false;
        boolean pointsOnSESWEdge = false;
        boolean pointsOnSWNWEdge = false;

        int origin_x = getOriginXForGrid();
        int origin_y = getOriginYForGrid();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();
        int delta_x = (int) (getDeltaX(mapInstance) * .25);
        int delta_y = (int) (getDeltaY(mapInstance) * .25);

        for(Point point: pointsOnEdge) {
            if(origin_x == point.x) pointsOnSWNWEdge = true;
            if(origin_y == point.y) pointsOnNWNEEdge = true;
            if(width == point.x)  pointsOnNESEEdge = true;
            if(height == point.y)  pointsOnSESWEdge = true;
            if(!(pointsOnNWNEEdge ^ pointsOnNESEEdge ^ pointsOnSESWEdge ^ pointsOnSWNWEdge)) {
                return;
            }
        }

        if(pointsOnSESWEdge) {
            corners[SE] = findEarth2SkyTransition(mapController, cornerPoints[SE].x, cornerPoints[SE].y, delta_x, 0, width, height);
            corners[SW] = findEarth2SkyTransition(mapController, cornerPoints[SW].x, cornerPoints[SW].y, -delta_x, 0, width, height);
        }
    }
    /**
     *
     * There is sky in the four corners and tilt is outside the range so now we will build a grid of points and figure out
     * points near the border of the earth/sky boundary. We will use these points to arrive at four points that best
     * represent the bounding polygon.
     *
     * @param mapInstance
     * @param corners
     * @return
     */
    private static boolean processGrid(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();

        // We will use a smaller deltaX and deltaY as we may be looking at a small portion of the globe and we need to get
        // distinct four points.
        int delta_x = (int) (getDeltaX(mapInstance) * .25);
        int delta_y = (int) (getDeltaY(mapInstance) * .25);

        List<IGeoPosition> cornersFound = new ArrayList<>();
        IGeoPosition corner;

        // Checking at (x, height) and (width, y) will NOT yield a geographical point so must subtract one.
        // This is NOT an issue when you are looking at the corners themselves.
        Set<Point> points = getGridPoints(width-1, height-1);

        Point result = new Point();
        Position pos = new Position();

        for(Point p: points) {
            Log.v(TAG, "x, y " + p.x + " " + p.y);
            if (mapController.screenPointToGroundPosition(p.x, p.y, pos)) {
                if(mapController.groundPositionToScreenPoint(pos.latitude, pos.longitude, result)
                        && 0 < result.x && 0 < result.y && result.x < width && result.y < height) {
                    if (null != (corner = findEarth2SkyTransition(mapController, p.x, p.y, 0, -delta_y, width, height))) {
                        cornersFound.add(corner);
                    }
                    if (null != (corner = findEarth2SkyTransition(mapController, p.x, p.y, delta_x, 0, width, height))) {
                        cornersFound.add(corner);
                    }
                    if (null != (corner = findEarth2SkyTransition(mapController, p.x, p.y, 0, delta_y, width, height))) {
                        cornersFound.add(corner);
                    }
                    if (null != (corner = findEarth2SkyTransition(mapController, p.x, p.y, -delta_x, 0, width, height))) {
                        cornersFound.add(corner);
                    }
                }
            }
        }

        if(cornersFound.size() > 0) {
            IGeoPosition center = GeoLibrary.getCenter(cornersFound);

            if (mapController.groundPositionToScreenPoint(center.getLatitude(), center.getLongitude(), result)) {
                corners[NW] = findEarth2SkyTransition(mapController, result.x, result.y, -delta_x, -delta_y, width, height);
                corners[NE] = findEarth2SkyTransition(mapController, result.x, result.y, delta_x, -delta_y, width, height);
                corners[SE] = findEarth2SkyTransition(mapController, result.x, result.y, delta_x, delta_y, width, height);
                corners[SW] = findEarth2SkyTransition(mapController, result.x, result.y, -delta_x, delta_y, width, height);
                return true;
            }
        }
        return false;
    }

    private static Set<Point> getGridPoints(int width, int height) {

        for(GridPoints gp: gridPoints) {
            if((height == gp.height) && (width == gp.width)) {
                return gp.points;
            }
        }

        lock.lock();
        try {
            for(GridPoints gp: gridPoints) {
                if((height == gp.height) && (width == gp.width)) {
                    return gp.points;
                }
            }

            if(gridPoints.size() > MAX_GRID_POINTS) {
                Log.e(TAG, "We need to cleanup the GridPoints, this can happen if view size is changed many times");
                gridPoints.clear();
            }

            GridPoints newGridPoints = new GridPoints(width, height);
            gridPoints.add(newGridPoints);
            return newGridPoints.points;
        } finally {
            lock.unlock();
        }
    }

    /**
     * Class holds generated Grid Points for a view of size width x height
     */
    static class GridPoints {
        final int width;
        final int height;
        final Set<Point> points;

        GridPoints(int width, int height) {
            this.width = width;
            this.height = height;
            this.points = new HashSet<>();

            // We are going to create a grid of 16 rectangles. We will then pick the four points from each rectangle
            // (four vertices). Yes there will be overlapping (identical) points and we will trim them
            // as part of optimization phase.
            // We can also generate these points ahead of time based on height and width.

            List<RectangleView> rcViews = new ArrayList<>();

            // Checking (0, y) and (x, 0) will not yield a geographical point hence start with 1 for outermost/level0
            // rectangle. This is NOT an issue when you are looking at the corners themselves.
            RectangleView level0 = new RectangleView(getOriginXForGrid(), getOriginYForGrid(), width, getOriginYForGrid(),
                    width, height, getOriginXForGrid(), height);
            // RectangleView level0 = new RectangleView(0, 0, width, 0, width, height, 0, height);
            Log.d(TAG, "level0 rectangle");
            level0.printRectangle();

            List<RectangleView> level1 = level0.getRectangleViews();

            for(RectangleView rv: level1) {
                Log.d(TAG, "level1 rectangle");
                rv.printRectangle();
                rcViews.addAll(rv.getRectangleViews());
            }

            for(RectangleView rv : rcViews) {
                this.points.addAll(rv.getPoints());
            }

            Log.d(TAG, "GRID POINTS w/h/count " + width + " " + height + " " + this.points.size());
        }
    }

    /**
     * Defines a rectangle with four screen points.
     */
    static class RectangleView {
        int nw_x;
        int nw_y;
        int ne_x;
        int ne_y;
        int se_x;
        int se_y;
        int sw_x;
        int sw_y;

        RectangleView(int nw_x, int nw_y, int ne_x, int ne_y, int se_x, int se_y, int sw_x, int sw_y) {
            this.nw_x = nw_x;
            this.nw_y = nw_y;
            this.ne_x = ne_x;
            this.ne_y = ne_y;
            this.se_x = se_x;
            this.se_y = se_y;
            this.sw_x = sw_x;
            this.sw_y = sw_y;
            printRectangle();
        }

        void printRectangle() {
            Log.d(TAG, "nw ne we sw " + nw_x + " " + nw_y + " " + ne_x + " " + ne_y + " " + se_x + " " + se_y + " " + sw_x + " " + sw_y);
        }

        /**
         * Returns list of vertices of the rectangle
         * @return
         */
        List<Point> getPoints() {
            List<Point> points = new ArrayList();
            points.add(new Point(nw_x, nw_y));
            points.add(new Point(ne_x, ne_y));
            points.add(new Point(se_x, se_y));
            points.add(new Point(sw_x, sw_y));
            points.add(new Point(nw_x + (ne_x - nw_x)/2, nw_y + (se_y - ne_y)/2));

            return points;
        }

        /**
         * Generates four rectangles from this rectangle.
         * @return
         */
        List<RectangleView> getRectangleViews() {
            List<RectangleView> rectangleViews = new ArrayList<>();
            int o_x = nw_x;
            int o_y = nw_y;
            rectangleViews.add(new RectangleView(nw_x, nw_y, o_x + (ne_x - nw_x)/2, nw_y, o_x + (ne_x - nw_x)/2, o_y + (se_y - ne_y)/2, nw_x, o_y + (sw_y - nw_y)/2));
            rectangleViews.add(new RectangleView(o_x + (ne_x - nw_x)/2, nw_y, ne_x, ne_y, ne_x, o_y + (se_y - ne_y)/2, o_x + (ne_x - nw_x)/2, o_y + (se_y - ne_y)/2));
            rectangleViews.add(new RectangleView(o_x + (ne_x - nw_x)/2, o_y + (se_y - ne_y)/2, ne_x, o_y + (se_y - ne_y)/2, ne_x, se_y, o_x + (se_x - sw_x)/2, se_y));
            rectangleViews.add(new RectangleView(nw_x, o_y + (sw_y - nw_y)/2, o_x + (ne_x - nw_x)/2, o_y + (se_y - ne_y)/2, o_x + (se_x - sw_x)/2, se_y, sw_x, sw_y));
            return rectangleViews;
        }
    }
}
