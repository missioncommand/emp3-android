package mil.emp3.worldwind.utils;

import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import gov.nasa.worldwind.geom.Position;
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

    public static IGeoBounds getBounds(MapInstance mapInstance) {
        IGeoBounds bounds = null;
        IGeoPosition corners[] = new IGeoPosition[CORNERS];

        // Check how many corners we have a map rather than sky/space
        int cornersFound = getCornersTouched(mapInstance, corners);
        if(CORNERS == cornersFound) {
            // All corners have a map, so this is the most common and probably relatively easy scenario.
            return cornersToBound(corners);
        }
        return bounds;
    }

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

        //
        // If heading is not zero then we can't use the physical corners of the rectangular view. We have to rotate the rectangle
        // so that it aligns with the heading. We then figure out the points that are the vertices of the rectangle that is perpandicular
        // to the heading. These points should be as close to the edge of the view as possible.
        //

        if((mapInstance.getCamera().getHeading() > 0) || (mapInstance.getCamera().getHeading() < 0)) {

            // We have to first translate the rectangle to the center of the view from top left corner.
            vertices[NW] = new android.graphics.Point(-(width)/2, (height)/2);
            vertices[SW] = new android.graphics.Point(-(width)/2, -(height)/2);
            vertices[SE] = new android.graphics.Point((width)/2, -(height)/2);
            vertices[NE] = new android.graphics.Point((width)/2, (height)/2);

            // We then rotate the rectangle to aligned the heading.
            rotate(vertices, mapInstance.getCamera().getHeading());

            // We now translate the aligned rectangle's points to the original coordinate syetem rooted at top left of the view.
            for(int ii = 0; ii < vertices.length; ii++) {
                Log.d(TAG, "Rotation B4 Translate" + vertices[ii].x + " " + vertices[ii].y);
                vertices[ii].x = vertices[ii].x + (width) / 2;
                if(vertices[ii].x > width) {
                    vertices[ii].x = width;
                } else if(vertices[ii].x < 0) {
                    vertices[ii].x = 0;
                }
                vertices[ii].y *= -1;
                vertices[ii].y = vertices[ii].y + (height) / 2;
                if(vertices[ii].y > height) {
                    vertices[ii].y = height;
                } else if(vertices[ii].y < 0) {
                    vertices[ii].y = 0;
                }
                Log.d(TAG, "Rotation AFT Translate" + vertices[ii].x + " " + vertices[ii].y);
            }
        } else {

            // No rotations is required.
            vertices[NW] = new android.graphics.Point(origin_x, origin_y);
            vertices[SW] = new android.graphics.Point(origin_x, origin_y + height);
            vertices[SE] = new android.graphics.Point(origin_x + width, origin_y + height);
            vertices[NE] = new android.graphics.Point(origin_x + width, origin_y);
        }

        // Now fetch the geographical points for the corners of the relavent rectangle.
        Position pos = new Position();
        for (int ii = 0; ii < vertices.length; ii++) {
            if (mapController.screenPointToGroundPosition(vertices[ii].x, vertices[ii].y, pos)) {
                corners[ii] = new MyGeoPosition(pos.latitude, pos.longitude);
                Log.d(TAG, "x/y/l/n " + vertices[ii].x + " " +  vertices[ii].y + " " + pos.latitude + " " + pos.longitude);
                cornersFound++;
            }
        }
//        Position pos = new Position();
//        if (mapController.screenPointToGroundPosition(origin_x, origin_y, pos)) {
//            corners[NW] = new MyGeoPosition(pos.latitude, pos.longitude);
//            Log.d(TAG, "x/y/l/n " + origin_x + " " + origin_y + " " + pos.latitude + " " + pos.longitude);
//            cornersFound++;
//        }
//
//        if (mapController.screenPointToGroundPosition(origin_x + width, origin_y, pos)) {
//            corners[NE] = new MyGeoPosition(pos.latitude, pos.longitude);
//            Log.d(TAG, "x/y/l/n " + (origin_x + width) + " " + origin_y + " " + pos.latitude + " " + pos.longitude);
//            cornersFound++;
//        }
//
//        if (mapController.screenPointToGroundPosition(origin_x + width, origin_y + height, pos)) {
//            corners[SE] = new MyGeoPosition(pos.latitude, pos.longitude);
//            Log.d(TAG, "x/y/l/n " + (origin_x + width) + " " + (origin_y + height) + " " + pos.latitude + " " + pos.longitude);
//            cornersFound++;
//        }
//
//        if (mapController.screenPointToGroundPosition(origin_x, origin_y + height, pos)) {
//            corners[SW] = new MyGeoPosition(pos.latitude, pos.longitude);
//            Log.d(TAG, "x/y/l/n " + (origin_x) + " " + (origin_y + height) + " " + pos.latitude + " " + pos.longitude);
//            cornersFound++;
//        }
        return cornersFound;
    }

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

    /**
     * Convinience class.
     */
    private static class MyGeoPosition extends GeoPosition {
        MyGeoPosition(double latitude, double longitude) {
            setLatitude(latitude);
            setLongitude(longitude);
        }
    }
}
