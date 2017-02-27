package mil.emp3.worldwind.utils;

import android.graphics.Point;
import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nasa.worldwind.geom.Position;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.controller.PickNavigateController;

/**
 * Generates a bounding polygon for the visible map. Aim is to return four points defining a quadrilateral.
 */
public class BoundsGeneration {

    private static String TAG = BoundsGeneration.class.getSimpleName();

    private final static int CORNERS = 4;
    private final static int NW = 0;       // x = 0, y = 0
    private final static int SW = 1;       // x = 0, y = height
    private final static int SE = 2;       // x = width, y = height
    private final static int NE = 3;       // x = width, y = 0;

    private final static int cc = 0;       // Corner correction. This is no longer an issue
    private final static double lc = 0.05;  // Least Count on the grid. (5 percent of width/height

    private static List<GridPoints> gridPoints = new ArrayList<>();

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

    public static EmpBoundingBox getBounds(MapInstance mapInstance) {
        try {
            List<IGeoPosition> list = getBoundingPolygon(mapInstance);
            if ((null != list) && (EmpBoundingBox.REQUIRED_VERTICES == list.size())) {
                EmpBoundingBox boundingBox = new EmpBoundingBox(list.get(0), list.get(1), list.get(2), list.get(3));
                return boundingBox;
            }
        } catch (Exception e) {
            Log.e(TAG, "getBoundingBox " + e.getMessage(), e);
        }
        return null;
    }
    /**
     * Figures out how many corners of the view show a sky. Based on that information invokes appropriate method to calculate the
     * polygon.
     * @param mapInstance
     * @return
     */
    public static List<IGeoPosition> getBoundingPolygon(MapInstance mapInstance) {
        int cornersFound = 0;
        try {
            IGeoPosition corners[] = new IGeoPosition[CORNERS];
            boolean status = false;

            // Check how many corners we have a map rather than sky/space
            cornersFound = getCornersTouched(mapInstance, corners);
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
                List<IGeoPosition> boundingPolygon = new ArrayList<>();
                for (int ii = 0; ii < corners.length; ii++) {
                    if (null != corners[ii]) {
                        boundingPolygon.add(corners[ii]);
                    }
                }

                if (-1.0 > mapInstance.getCamera().getTilt() || mapInstance.getCamera().getTilt() > 1.0) {
                    // Globe is at the center of the view but roll and heading may not be zero.
                    adjustPolygon(boundingPolygon);
                }

                if (CORNERS != boundingPolygon.size()) {
                    Log.e(TAG, "Return bounding polygon with size " + boundingPolygon.size());
                }
                return boundingPolygon;
            }
        } catch (Exception e) {
            Log.e(TAG, "cornersFound " + cornersFound + " " + e.getMessage(), e);
        }
        return null;
    }

    private static void adjustPolygon(List<IGeoPosition> boundingPolygon) {

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
        int origin_x = 0;   // This is the NW corner when heading is zero
        int origin_y = 0;   // This is the NW corner when heading is zero
        int cornersFound = 0;

        android.graphics.Point[] vertices = new android.graphics.Point[CORNERS];

        vertices[NW] = new android.graphics.Point(origin_x+cc, origin_y+cc);
        vertices[SW] = new android.graphics.Point(origin_x+cc, origin_y + height - cc);
        vertices[SE] = new android.graphics.Point(origin_x + width - cc, origin_y + height - cc);
        vertices[NE] = new android.graphics.Point(origin_x + width - cc, origin_y + cc);

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
                    corners[ii] = new MyGeoPosition(pos.latitude, pos.longitude);
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

        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        // Locate the view corner that doesn't show the sky. Then traverse from this corner towards the adjacent corner(s) (findEarth2SkyTransition)
        // looking for a place where we make a transition from map (earth) to sky and you have found two points.
        // Traverse from the opposite corner towards this corner (findSky2EarthTransition) looking for the place where there is a transition
        // from sky to map (earth) and this will give you the third point of the polygon.
        if(corners[NW] != null) {
            corners[NE] = findEarth2SkyTransition(mapController, 0, 0, delta_x, 0, width, height);
            corners[SE] = findSky2EarthTransition(mapController, width, height, -delta_x, -delta_y, width, height);
            corners[SW] = findEarth2SkyTransition(mapController, 0, 0, 0, delta_y, width, height);
        } else if(corners[SW] != null) {
            corners[NW] = findEarth2SkyTransition(mapController, 0, height, 0, -delta_y, width, height);
            corners[NE] = findSky2EarthTransition(mapController, width, 0, -delta_x, delta_y, width, height);
            corners[SE] = findEarth2SkyTransition(mapController, 0, height, delta_x, 0, width, height);
        } else if(corners[NE] != null) {
            corners[SE] = findEarth2SkyTransition(mapController, width, 0, 0, delta_y, width, height);
            corners[SW] = findSky2EarthTransition(mapController, 0, height, delta_x, -delta_y, width, height);
            corners[NW] = findEarth2SkyTransition(mapController, width, 0, -delta_x, 0, width, height);
        } else if(corners[SE] != null) {
            corners[SW] = findEarth2SkyTransition(mapController, width, height, -delta_x, 0, width, height);
            corners[NW] = findSky2EarthTransition(mapController, 0, 0, delta_x, delta_y, width, height);
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
     * @return
     */
    private static IGeoPosition findEarth2SkyTransition(PickNavigateController mapController, int start_x, int start_y, int delta_x, int delta_y, int width, int height) {

        IGeoPosition corner = null;
        boolean found = false;
        Position pos = new Position();
        Position prevPos = null;

        // Following three block can actually be collapsed into one if we change the comparison to <= and =>, but for now
        // we are keeping them separate. Until then whatever changes you make in one block have to be applied to other two blocks.

        if((0 != delta_x) || (0 != delta_y)) {

            // Save the initial position where there is earth so that we can deal with the special case of first step transition.
            if (mapController.screenPointToGroundPosition(start_x, start_y, pos)) {
                prevPos = pos;
            }

            int current_x = start_x + delta_x;
            int current_y = start_y + delta_y;
            for (; current_x <= width && current_x >= 0 && current_y <= height && current_y >= 0 && !found;
                 current_x += delta_x, current_y += delta_y) {
                if (!mapController.screenPointToGroundPosition(current_x, current_y, pos)) {
                    if(prevPos != null) {
                        corner = new MyGeoPosition(prevPos.latitude, prevPos.longitude);
                        Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
                        found = true;
                    }
                } else {
                    prevPos = pos;
                }
            }

            // Following takes care of the condition where transition occurs before the first increment. lower the value of
            // lc less the probability of that happening, but that will affect the performance.
            if((null == corner) && (prevPos != null)) {
                corner = new MyGeoPosition(prevPos.latitude, prevPos.longitude);
                Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
            }

        } else {
            Log.e(TAG, "findEarth2SkyTransition was invoked with both delta_x and delta_y set to zero.");
        }
        return corner;
    }

    /**
     * Tow view corners are showing sky and two are showing earth. Considering the geometry of the earth these have to be
     * adjacent corners.
     * @param mapInstance
     * @param corners
     * @return
     */
    private static boolean processTwoCorners(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();

        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        // Locate the two adjacent corners that show the earth. Then traverse along the x or the y axis to find transition
        // from earth to sky.

        if((corners[NW] != null) && (corners[NE] != null)){
            corners[SE] = findEarth2SkyTransition(mapController, width, 0, 0, delta_y, width, height);
            corners[SW] = findEarth2SkyTransition(mapController, 0, 0, 0, delta_y, width, height);
        } else if((corners[SW] != null) && (corners[SE] != null)) {
            corners[NE] = findEarth2SkyTransition(mapController, width, height, 0, -delta_y, width, height);
            corners[NW] = findEarth2SkyTransition(mapController, 0, height, 0, -delta_y, width, height);
        } else if((corners[NE] != null) && (corners[SE] != null)) {
            corners[NW] = findEarth2SkyTransition(mapController, width, 0, -delta_x, 0, width, height);
            corners[SW] = findEarth2SkyTransition(mapController, width, height, -delta_x, 0, width, height);
        } else if((corners[SW] != null) && (corners[NW] != null)) {
            corners[NE] = findEarth2SkyTransition(mapController, 0, 0, delta_x, 0, width, height);
            corners[SE] = findEarth2SkyTransition(mapController, 0, height, delta_x, 0, width, height);
        } else {
            Log.e(TAG, "processTwoCorners TWO CORNERS NOT FOUND");
            return false;
        }

        // refineIt(mapInstance, corners);
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
                        corner = new MyGeoPosition(pos.latitude, pos.longitude);
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
        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        // Locate the corner that is showing the sky and traverse from that corner towards the opposite corner looking for a
        // transition from sky to earth.
        if(corners[NW] == null) {
            corners[NW] = findSky2EarthTransition(mapController, 0, 0, delta_x, delta_y, width, height);
        } else if(corners[SW] == null) {
            corners[SW] = findSky2EarthTransition(mapController, 0, height, delta_x, -delta_y, width, height);
        } else if(corners[NE] == null) {
            corners[NE] = findSky2EarthTransition(mapController, width, 0, -delta_x, delta_y, width, height);
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
        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        List<IGeoPosition> cornersFound = new ArrayList<>();
        IGeoPosition corner;

        // Traverse from each corner towards the center of the view, find the transition from sky to earth and you have found one
        // point. Repeat this for all four corners.

        if(null != (corner = findSky2EarthTransition(mapController, width/2, 0, 0, delta_y, width, height))) {
            cornersFound.add(corner);
        }
        if(null != (corner = findSky2EarthTransition(mapController, width, height/2, -delta_x, 0, width, height)))  {
            cornersFound.add(corner);
        }
        if(null != (corner = findSky2EarthTransition(mapController, width/2, height, 0, -delta_y, width, height))) {
            cornersFound.add(corner);
        }
        if(null != (corner = corners[SE] = findSky2EarthTransition(mapController, 0, height/2, delta_x, 0, width, height))) {
            cornersFound.add(corner);
        }

        if(CORNERS != cornersFound.size()) {
            Log.e(TAG, "processGlobeInCenter couldn't find all four points");
        }

        if(0 != cornersFound.size()) {
            // Find the center of the four geographical points
            IGeoPosition center = GeoLibrary.getCenter(cornersFound);

            // Convert the center to geo point and traverse out diagonally to locate the desired polygon vertices.
            // We don't short circuit this by simply traversiong from center of the sides initially as globe may not be
            // exactly at the center.

            Point result = new Point();
            if (mapController.groundPositionToScreenPoint(center.getLatitude(), center.getLongitude(), result)) {
                corners[NW] = findEarth2SkyTransition(mapController, result.x, result.y, -delta_x, -delta_y, width, height);
                corners[NE] = findEarth2SkyTransition(mapController, result.x, result.y, delta_x, -delta_y, width, height);
                corners[SE] = findEarth2SkyTransition(mapController, result.x, result.y, delta_x, delta_y, width, height);
                corners[SW] = findEarth2SkyTransition(mapController, result.x, result.y, -delta_x, delta_y, width, height);
            }
        } else {
            Log.e(TAG, "processGlobeInCenter couldn't find ANY point");
            return false;
        }
        return true;
    }

    /**
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

        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        List<IGeoPosition> cornersFound = new ArrayList<>();
        IGeoPosition corner;
        Set<Point> points = getGridPoints(width, height);

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

    private static Set<Point> getGridPoints(int height, int width) {
        for(GridPoints gp: gridPoints) {
            if((height == gp.height) && (width == gp.width)) {
                return gp.points;
            }
        }

        GridPoints newGridPoints = new GridPoints(width, height);
        gridPoints.add(newGridPoints);
        return newGridPoints.points;
    }

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

            RectangleView level0 = new RectangleView(0, 0, width, 0, width, height, 0, height);
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
    /**
     * Convenience class.
     */
    private static class MyGeoPosition extends GeoPosition {
        MyGeoPosition(double latitude, double longitude) {
            setLatitude(latitude);
            setLongitude(longitude);
        }
    }
}
