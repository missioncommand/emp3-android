package mil.emp3.worldwind.utils;

import android.graphics.Point;
import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;

import java.util.List;

import gov.nasa.worldwind.geom.Position;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.controller.PickNavigateController;

/**
 * Generates bounds for the map. Work in progress.
 */
public class BoundsGeneration {

    private static String TAG = BoundsGeneration.class.getSimpleName();

    private final static int CORNERS = 4;
    private final static int NW = 0;       // x = 0, y = 0
    private final static int SW = 1;       // x = 0, y = height
    private final static int SE = 2;       // x = width, y = height
    private final static int NE = 3;       // x = width, y = 0;

    private final static int cc = 3;       // Corner correction.
    private final static double lc = 0.05;  // Least Count on the grid. (5 percent of width/height

    private static double getLc() {
        return lc;
    }

    private static int getDeltaX(MapInstance mapInstance) {
        return (int) (mapInstance.getWW().getWidth() * getLc());
    }

    private static int getDeltaY(MapInstance mapInstance) {
        return (int) (mapInstance.getWW().getHeight() * getLc());
    }
    public static List<IGeoPosition> getBoundingPolygon(MapInstance mapInstance) {
        // IGeoBounds bounds = null;
        IGeoPosition corners[] = new IGeoPosition[CORNERS];

        // Check how many corners we have a map rather than sky/space
        int cornersFound = getCornersTouched(mapInstance, corners);
        if(CORNERS == cornersFound) {
            // All corners have a map, so this is the most common and probably relatively easy scenario.
            // cornersToBound(corners);
        } else if(1 == cornersFound) {
            processSingleCorner(mapInstance, corners);
        } else if(2 == cornersFound) {
            processTwoCorners(mapInstance, corners);
        } else if(3 == cornersFound) {
            processThreeCorners(mapInstance, corners);
        } else if(0 == cornersFound) {
            Log.d(TAG, "camera Tils " + mapInstance.getCamera().getTilt());
            if(-1.0 < mapInstance.getCamera().getTilt() && mapInstance.getCamera().getTilt() < 1.0) {
                // Globe is at the center of the view but roll and heading may not be zero.
                processGlobeInCenter(mapInstance, corners);
            }
            else {
                processGrid(mapInstance, corners);
            }
        }

        List<IGeoPosition> boundingPolygon = new ArrayList<>();
        for(int ii = 0; ii < corners.length; ii++) {
            if(null != corners[ii]) {
                boundingPolygon.add(corners[ii]);
            }
        }
        return boundingPolygon;
    }

//    public static IGeoBounds getBounds(MapInstance mapInstance) {
//        IGeoBounds bounds = null;
//        IGeoPosition corners[] = new IGeoPosition[CORNERS];
//
//        // Check how many corners we have a map rather than sky/space
//        int cornersFound = getCornersTouched(mapInstance, corners);
//        if(CORNERS == cornersFound) {
//            // All corners have a map, so this is the most common and probably relatively easy scenario.
//            return cornersToBound(corners);
//        } else if(1 == cornersFound) {
////            return(processSingleCorner(mapInstance, corners));
//            processSingleCorner(mapInstance, corners);
//        }
//        return bounds;
//    }

    /**
     * We have calculated four corners of the bounding box. Now convert that to bounds, i.e.
     * West, East, North and South. We want entire bounding box to be visible so we will pick the coordinates accordingly.
     *
     * Example, We have to pick the South latitude from either SE corner or SW corner. We will pick the one that is lesser of the
     * two. Similar logic is applied to all four sides as for each side we have a choice of two coordinates.
     * @param corners
     * @return
     */
    private static IGeoBounds cornersToBound(IGeoPosition corners[]) {
        IGeoBounds bounds = new GeoBounds();
        if(corners[SW].getLatitude() < corners[SE].getLatitude()) {
            bounds.setSouth(corners[SE].getLatitude());
        } else {
            bounds.setSouth(corners[SW].getLatitude());
        }

        if(corners[NW].getLatitude() < corners[NE].getLatitude()) {
            bounds.setNorth(corners[NW].getLatitude());
        } else {
            bounds.setNorth(corners[NE].getLatitude());
        }

        if(((corners[SW].getLongitude() + 540) % 360) < ((corners[NW].getLongitude() + 540) % 360))
        {
            bounds.setWest(corners[NW].getLongitude());
        } else {
            bounds.setWest(corners[SW].getLongitude());
        }

        if(((corners[SE].getLongitude() + 540) % 360) < ((corners[NE].getLongitude() + 540) % 360))
        {
            bounds.setEast(corners[SE].getLongitude());
        } else {
            bounds.setEast(corners[NE].getLongitude());
        }

        return bounds;
    }

    /**
     * We are trying to figure out if a specific corner has a map of sky/space (mape may be tilted/rotated). If we do find
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

        //
        // If heading is not zero then we can't use the physical corners of the rectangular view. We have to rotate the rectangle
        // so that it aligns with the heading. We then figure out the points that are the vertices of the rectangle that is perpandicular
        // to the heading. These points should be as close to the edge of the view as possible.
        //

//        if((mapInstance.getCamera().getRoll() > 0) || (mapInstance.getCamera().getRoll() < 0)) {
//            vertices[NW] = new android.graphics.Point(origin_x, origin_y);
//            vertices[SW] = new android.graphics.Point(origin_x, origin_y + height);
//            vertices[SE] = new android.graphics.Point(origin_x + width, origin_y + height);
//            vertices[NE] = new android.graphics.Point(origin_x + width, origin_y);
//        }
//        else if((mapInstance.getCamera().getHeading() > 0) || (mapInstance.getCamera().getHeading() < 0)) {
//
//            // We have to first translate the rectangle to the center of the view from top left corner.
//            vertices[NW] = new android.graphics.Point(-(width)/2, (height)/2);
//            vertices[SW] = new android.graphics.Point(-(width)/2, -(height)/2);
//            vertices[SE] = new android.graphics.Point((width)/2, -(height)/2);
//            vertices[NE] = new android.graphics.Point((width)/2, (height)/2);
//
//            // We then rotate the rectangle to aligned the heading.
//            rotate(vertices, mapInstance.getCamera().getHeading());
//
//            // We now translate the aligned rectangle's points to the original coordinate syetem rooted at top left of the view.
//            for(int ii = 0; ii < vertices.length; ii++) {
//                Log.d(TAG, "Rotation B4 Translate" + vertices[ii].x + " " + vertices[ii].y);
//                vertices[ii].x = vertices[ii].x + (width) / 2;
//                if(vertices[ii].x > width) {
//                    vertices[ii].x = width;
//                } else if(vertices[ii].x < 0) {
//                    vertices[ii].x = 0;
//                }
//                vertices[ii].y *= -1;
//                vertices[ii].y = vertices[ii].y + (height) / 2;
//                if(vertices[ii].y > height) {
//                    vertices[ii].y = height;
//                } else if(vertices[ii].y < 0) {
//                    vertices[ii].y = 0;
//                }
//                Log.d(TAG, "Rotation AFT Translate" + vertices[ii].x + " " + vertices[ii].y);
//            }
//        } else {
//
//            // No rotations is required.
//            vertices[NW] = new android.graphics.Point(origin_x, origin_y);
//            vertices[SW] = new android.graphics.Point(origin_x, origin_y + height);
//            vertices[SE] = new android.graphics.Point(origin_x + width, origin_y + height);
//            vertices[NE] = new android.graphics.Point(origin_x + width, origin_y);
//        }

        // Now fetch the geographical points for the corners of the relevant rectangle.
        Position pos = new Position();
        for (int ii = 0; ii < vertices.length; ii++) {
            if (mapController.screenPointToGroundPosition(vertices[ii].x, vertices[ii].y, pos)) {
                corners[ii] = new MyGeoPosition(pos.latitude, pos.longitude);
                Log.d(TAG, "x/y/l/n " + vertices[ii].x + " " +  vertices[ii].y + " " + pos.latitude + " " + pos.longitude);
                cornersFound++;
            }
        }

//        if(CORNERS == cornersFound) {
//            if ((mapInstance.getCamera().getRoll() > 0) || (mapInstance.getCamera().getRoll() < 0)) {
//                processForRole(mapInstance, corners);
//            }
//        }

        return cornersFound;
    }

//    private static void processForRole(MapInstance mapInstance, IGeoPosition corners[]) {
//        PickNavigateController mapController = mapInstance.getMapController();
//        int width = mapInstance.getWW().getWidth();
//        int height = mapInstance.getWW().getHeight();
//
//        Double northMost = -91.0;
//        Double southMost = -91.0;
//        int northMostCorner = -1;
//        int southMostCorner = -1;
//
//        for (int ii = 0; ii < corners.length; ii++) {
//            if (northMost < -90.0) {
//                northMost = corners[ii].getLatitude();
//                southMost = corners[ii].getLatitude();
//                northMostCorner = ii;
//                southMostCorner = ii;
//                continue;
//            }
//
//            if (corners[ii].getLatitude() > northMost) {
//                northMost = corners[ii].getLatitude();
//                northMostCorner = ii;
//            } else if (corners[ii].getLatitude() < southMost) {
//                southMost = corners[ii].getLatitude();
//                southMostCorner = ii;
//            }
//        }
//        Log.d(TAG, "NEWS Roll " + northMost + " " + southMost + " " + northMostCorner + " " + southMostCorner);
//
//        // Now pick the starting longitudes from the other two corners
//        int westMostCorner = (northMostCorner + 1) % 4;
//        int eastMostCorner = (northMostCorner + 3) % 4;
//
//        Double eastMost = corners[eastMostCorner].getLongitude();
//        Double westMost = corners[westMostCorner].getLongitude();
//
//        Log.d(TAG, "NEWS Roll " + eastMost + " " + westMost + " " + eastMostCorner + " " + westMostCorner);
//
//        double nsDiff = abs(northMost - southMost);
//        double ewDiff;
//
//        if((eastMost >= 0) && (westMost >= 0)) {
//            ewDiff = abs(eastMost - westMost);
//        } else if((eastMost <= 0) && (westMost <= 0)) {
//            ewDiff = abs(eastMost - westMost);
//        } else {
//            ewDiff = abs(eastMost) + abs(westMost);
//            if(ewDiff > 180.0) {
//                ewDiff = (180.0 - abs(eastMost) + 180.0 - abs(westMost));
//            }
//        }
//
//        Log.d(TAG, "NEWS Roll diff " + nsDiff + " " + ewDiff);
//
//        double nsDelta = nsDiff * .05;
//        double ewDelta = ewDiff * .05;
//
//        android.graphics.Point result = new Point();
//        boolean adjusted = true;
//        for (int ii = 0; (ii < 100) && adjusted; ii++) {
//            adjusted = false;
//            if (!mapController.groundPositionToScreenPoint(northMost, eastMost, result) || (result.x > width) || (result.y > height)
//                    || (result.x < 0) || (result.y < 0)) {
//                adjusted = true;
//                northMost -= nsDelta;
//                eastMost -= ewDelta;
//            } else {
//                Log.d(TAG, " northMost, eastMost  + " + result.x + " " + result.y);
//            }
//
//            if (!mapController.groundPositionToScreenPoint(northMost, westMost, result) || (result.x > width) || (result.y > height)
//                    || (result.x < 0) || (result.y < 0)) {
//                adjusted = true;
//                northMost -= nsDelta;
//                westMost += ewDelta;
//            } else {
//                Log.d(TAG, " northMost, westMost  + " + result.x + " " + result.y);
//            }
//
//            if (!mapController.groundPositionToScreenPoint(southMost, westMost, result) || (result.x > width) || (result.y > height) ||
//                    (result.x < 0) || (result.y < 0)) {
//                adjusted = true;
//                southMost += nsDelta;
//                westMost += ewDelta;
//            } else {
//                Log.d(TAG, " southMost, westMost  + " + result.x + " " + result.y);
//            }
//
//            if (!mapController.groundPositionToScreenPoint(southMost, eastMost, result) || (result.x > width) || (result.y > height)
//                    || (result.x < 0) || (result.y < 0)) {
//                adjusted = true;
//                southMost += nsDelta;
//                eastMost -= ewDelta;
//            } else {
//                Log.d(TAG, " southMost, eastMost  + " + result.x + " " + result.y);
//            }
//
//            Log.d(TAG, "NEWS Roll " + northMost + " " + eastMost + " " + westMost + " " + southMost);
//        }
//
//        corners[NE].setLatitude(northMost);
//        corners[NE].setLongitude(eastMost);
//
//        corners[SE].setLatitude(southMost);
//        corners[SE].setLongitude(eastMost);
//
//        corners[NW].setLatitude(northMost);
//        corners[NW].setLongitude(westMost);
//
//        corners[SW].setLatitude(southMost);
//        corners[SW].setLongitude(westMost);
//    }
    /**
     * refer to https://en.wikipedia.org/wiki/Rotation_matrix for the theory behind the following code.
     * @param vertices
     * @param heading
     */
    private static void rotate(android.graphics.Point[] vertices, double heading) {

        double cosHeading = Math.cos(Math.toRadians(heading));
        double sinHeading = Math.sin(Math.toRadians(heading));

        for(int ii = 0; ii < vertices.length; ii++) {
            int new_x = (int) ((vertices[ii].x * cosHeading) - (vertices[ii].y * sinHeading));
            int new_y = (int) ((vertices[ii].x * sinHeading) + (vertices[ii].y * cosHeading));
            vertices[ii].x = new_x;
            vertices[ii].y = new_y;
        }
    }

    private static int processSingleCorner(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();

        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        int cornersFound = 0;

        if(corners[NW] != null) {
            corners[NE] = findCorner(mapController, 0, 0, delta_x, 0, width, height);
            corners[SE] = findInnerCorner(mapController, width, height, -delta_x, -delta_y, width, height);
            corners[SW] = findCorner(mapController, 0, 0, 0, delta_y, width, height);
        } else if(corners[SW] != null) {
            corners[NW] = findCorner(mapController, 0, height, 0, -delta_y, width, height);
            corners[NE] = findInnerCorner(mapController, width, 0, -delta_x, delta_y, width, height);
            corners[SE] = findCorner(mapController, 0, height, delta_x, 0, width, height);
        } else if(corners[NE] != null) {
            corners[SE] = findCorner(mapController, width, 0, 0, delta_y, width, height);
            corners[SW] = findInnerCorner(mapController, 0, height, delta_x, -delta_y, width, height);
            corners[NW] = findCorner(mapController, width, 0, -delta_x, 0, width, height);
        } else if(corners[SE] != null) {
            corners[SW] = findCorner(mapController, width, height, -delta_x, 0, width, height);
            corners[NW] = findInnerCorner(mapController, 0, 0, delta_x, delta_y, width, height);
            corners[NE] = findCorner(mapController, width, height, 0, -delta_y, width, height);
        } else {
            Log.e(TAG, "NO CORNER FOUND");
            return cornersFound;
        }
        return cornersFound;
    }

    private static IGeoPosition findCorner(PickNavigateController mapController, int start_x, int start_y, int delta_x, int delta_y, int width, int height) {

        IGeoPosition corner = null;
        boolean found = false;
        Position pos = new Position();
        Position prevPos = null;

        if((0 != delta_x) && (0 != delta_y)) {
            if (mapController.screenPointToGroundPosition(start_x, start_y, pos)) {
                prevPos = pos;
            }

            int current_x = start_x + delta_x;
            int current_y = start_y + delta_y;
            for (; current_x < width && current_x > 0 && current_y < height && current_y > 0 && !found;
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

            if((null == corner) && (prevPos != null)) {
                corner = new MyGeoPosition(prevPos.latitude, prevPos.longitude);
                Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
            }

        } else if(0 != delta_x) {
            if (mapController.screenPointToGroundPosition(start_x, start_y, pos)) {
                prevPos = pos;
            }

            int current_x = start_x + delta_x;
            for(; current_x < width && current_x > 0 && !found; current_x += delta_x) {
                if (!mapController.screenPointToGroundPosition(current_x, start_y, pos)) {
                    if(prevPos != null) {
                        corner = new MyGeoPosition(prevPos.latitude, prevPos.longitude);
                        Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
                        found = true;
                    }
                } else {
                    prevPos = pos;
                }
            }

            if((null == corner) && (prevPos != null)) {
                corner = new MyGeoPosition(prevPos.latitude, prevPos.longitude);
                Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
            }

        } else if(0 != delta_y) {
            if(mapController.screenPointToGroundPosition(start_x, start_y, pos)) {
                prevPos = pos;
            }
            int current_y = start_y + delta_y;
            for(; current_y < height && current_y > 0 && !found; current_y += delta_y) {
                if (!mapController.screenPointToGroundPosition(start_x, current_y, pos)) {
                    if(prevPos != null) {
                        corner = new MyGeoPosition(prevPos.latitude, prevPos.longitude);
                        Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
                        found = true;
                    }
                } else {
                    prevPos = pos;
                }
            }

            if((null == corner) && (prevPos != null)) {
                corner = new MyGeoPosition(prevPos.latitude, prevPos.longitude);
                Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
            }
        }
        return corner;
    }

    private static boolean processTwoCorners(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();

        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        if((corners[NW] != null) && (corners[NE] != null)){
            corners[SE] = findCorner(mapController, width, 0, 0, delta_y, width, height);
            corners[SW] = findCorner(mapController, 0, 0, 0, delta_y, width, height);
        } else if((corners[SW] != null) && (corners[SE] != null)) {
            corners[NE] = findCorner(mapController, width, height, 0, -delta_y, width, height);
            corners[NW] = findCorner(mapController, 0, height, 0, -delta_y, width, height);
        } else if((corners[NE] != null) && (corners[SE] != null)) {
            corners[NW] = findCorner(mapController, width, 0, -delta_x, 0, width, height);
            corners[SW] = findCorner(mapController, width, height, -delta_x, 0, width, height);
        } else if((corners[SW] != null) && (corners[NW] != null)) {
            corners[NE] = findCorner(mapController, 0, 0, delta_x, 0, width, height);
            corners[SE] = findCorner(mapController, 0, height, delta_x, 0, width, height);
        } else {
            Log.e(TAG, "TWO CORNERS NOT FOUND");
            return false;
        }

        refineIt(mapInstance, corners);
        return true;
    }

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
                corners[NW] = findCorner(mapController, result.x, result.y, -delta_x, -delta_y, width, height);
                corners[NE] = findCorner(mapController, result.x, result.y, delta_x, -delta_y, width, height);
                corners[SE] = findCorner(mapController, result.x, result.y, delta_x, delta_y, width, height);
                corners[SW] = findCorner(mapController, result.x, result.y, -delta_x, delta_y, width, height);
            } else {
                Log.d(TAG, "Cannot refine further center is NOT on screen");
            }
        } else {
            Log.d(TAG, "Cannot refine further not enough points " + cornersFound.size());
        }
    }
    private static IGeoPosition findInnerCorner(PickNavigateController mapController, int start_x, int start_y, int delta_x, int delta_y, int width, int height) {
        IGeoPosition corner = null;
        boolean found = false;
        Position pos = new Position();

        if ((0 != delta_x) || (0 != delta_y)){
            int current_x = start_x + delta_x;
            int current_y = start_y + delta_y;
            for (; current_x < width && current_x > 0 && current_y < height && current_y > 0 && !found;
                 current_x += delta_x, current_y += delta_y) {
                if (mapController.screenPointToGroundPosition(current_x, current_y, pos)) {
                    corner = new MyGeoPosition(pos.latitude, pos.longitude);
                    Log.d(TAG, "corner " + corner.getLatitude() + " " + corner.getLongitude());
                    found = true;
                }
            }

        }
        return corner;
    }

    private static boolean processThreeCorners(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();
        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        if(corners[NW] == null) {
            corners[NW] = findInnerCorner(mapController, 0, 0, delta_x, delta_y, width, height);
        } else if(corners[SW] == null) {
            corners[SW] = findInnerCorner(mapController, 0, height, delta_x, -delta_y, width, height);
        } else if(corners[NE] == null) {
            Log.d(TAG, "Looking for NE corner");
            corners[NE] = findInnerCorner(mapController, width, 0, -delta_x, delta_y, width, height);
        } else if(corners[SE] == null) {
            corners[SE] = findInnerCorner(mapController, width, height, -delta_x, -delta_y, width, height);
        } else {
            Log.e(TAG, "NULL CORNER NOT FOUND");
            return false;
        }

        return true;
    }

    private static void processGlobeInCenter(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();
        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        List<IGeoPosition> cornersFound = new ArrayList<>();
        IGeoPosition corner;

        if(null != (corner = findInnerCorner(mapController, width/2, 0, 0, delta_y, width, height))) {
            cornersFound.add(corner);
        }
        if(null != (corner = findInnerCorner(mapController, width, height/2, -delta_x, 0, width, height)))  {
            cornersFound.add(corner);
        }
        if(null != (corner = findInnerCorner(mapController, width/2, height, 0, -delta_y, width, height))) {
            cornersFound.add(corner);
        }
        if(null != (corner = corners[SE] = findInnerCorner(mapController, 0, height/2, delta_x, 0, width, height))) {
            cornersFound.add(corner);
        }

        IGeoPosition center = GeoLibrary.getCenter(cornersFound);

        Point result = new Point();
        if (mapController.groundPositionToScreenPoint(center.getLatitude(), center.getLongitude(), result)) {
            corners[NW] = findCorner(mapController, result.x, result.y, -delta_x, -delta_y, width, height);
            corners[NE] = findCorner(mapController, result.x, result.y, delta_x, -delta_y, width, height);
            corners[SE] = findCorner(mapController, result.x, result.y, delta_x, delta_y, width, height);
            corners[SW] = findCorner(mapController, result.x, result.y, -delta_x, delta_y, width, height);
        }
//        corners[NW] = findInnerCorner(mapController, width/2, 0, 0, delta_y, width, height);
//        corners[NE] = findInnerCorner(mapController, width, height/2, -delta_x, 0, width, height);
//        corners[SW] = findInnerCorner(mapController, width/2, height, 0, -delta_y, width, height);
//        corners[SE] = findInnerCorner(mapController, 0, height/2, delta_x, 0, width, height);
    }

    private static void processGrid(MapInstance mapInstance, IGeoPosition corners[]) {
        PickNavigateController mapController = mapInstance.getMapController();
        int width = mapInstance.getWW().getWidth();
        int height = mapInstance.getWW().getHeight();

        int delta_x = getDeltaX(mapInstance);
        int delta_y = getDeltaY(mapInstance);

        List<IGeoPosition> cornersFound = new ArrayList<>();
        IGeoPosition corner;
        List<RectangleView> rcViews = new ArrayList<>();
        List<Point> points = new ArrayList<>();

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
            points.addAll(rv.getPoints());
        }

        for(Point p: points) {
            Log.d(TAG, "x, y " + p.x + " " + p.y);
            Position pos = new Position();
            if (mapController.screenPointToGroundPosition(p.x, p.y, pos)) {
                if(null != (corner = findCorner(mapController, p.x, p.y, 0, -delta_y, width, height))) {
                    cornersFound.add(corner);
                }
                if(null != (corner = findCorner(mapController, p.x, p.y, delta_x, 0, width, height))) {
                    cornersFound.add(corner);
                }
                if (null != (corner = findCorner(mapController, p.x, p.y, 0, delta_y, width, height))) {
                    cornersFound.add(corner);
                }
                if (null != (corner = findCorner(mapController, p.x, p.y, -delta_x, 0, width, height))) {
                    cornersFound.add(corner);
                }

//                corners[NW] = findCorner(mapController, p.x, p.y, 0, -delta_y, width, height);
//                corners[NE] = findCorner(mapController, p.x, p.y, delta_x, 0, width, height);
//                corners[SE] = findCorner(mapController, p.x, p.y, 0, delta_y, width, height);
//                corners[SW] = findCorner(mapController, p.x, p.y, -delta_x, 0, width, height);
//                break;
            }
        }

        IGeoPosition center = GeoLibrary.getCenter(cornersFound);

        Point result = new Point();
        if (mapController.groundPositionToScreenPoint(center.getLatitude(), center.getLongitude(), result)) {
            corners[NW] = findCorner(mapController, result.x, result.y, -delta_x, -delta_y, width, height);
            corners[NE] = findCorner(mapController, result.x, result.y, delta_x, -delta_y, width, height);
            corners[SE] = findCorner(mapController, result.x, result.y, delta_x, delta_y, width, height);
            corners[SW] = findCorner(mapController, result.x, result.y, -delta_x, delta_y, width, height);
        }

//        List<RectangleView> level1 = level0.getRectangleViews();
//        for(RectangleView rv: level1) {
//            Log.d(TAG, "nw, ne, se, sw " + rv.nw_x + " " + rv.nw_y + " " + rv.ne_x + " " + rv.ne_y + " "
//                + rv.se_x + " " + rv.se_y + " " + rv.sw_x + " " + rv.sw_y);
//        }
    }

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
        List<Point> getPoints() {
            List<Point> points = new ArrayList();
            points.add(new Point(nw_x, nw_y));
            points.add(new Point(ne_x, ne_y));
            points.add(new Point(se_x, se_y));
            points.add(new Point(sw_x, sw_y));
//            points.add(new Point((se_x - sw_x)/2, (se_y - ne_y)/2));
//            points.add(new Point((ne_x - nw_x)/2, ne_y));
//            points.add(new Point(ne_x, (se_y - ne_y)/2));
//            points.add(new Point((se_x - sw_x)/2, se_y));
//            points.add(new Point(nw_x, (sw_y - nw_y)/2));
            return points;
        }

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
