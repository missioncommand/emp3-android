package mil.emp3.api.utils;

import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Camera;
import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpBoundingArea;

/**
 * When map view changes i.e. pan/zoom/tilt/rotate/heading we recalculate the quadrilateral that approximates visible geographical area
 * of the map view. When the entire view is occupied by the map we can get the exact geographical coordinates of the four corners. When the map
 * shows sky/space then we provide the best possible geographical coordinates. SEC Military Symbol render-er is one know customer of this
 * capability.
 *
 * The vertices represent a polygon/quadrilateral and they are stored in clockwise order. You cannot associate any specific vertex to any specific
 * corner of the view. The class guarentees that all four vertices are non-null and have valid latitude/longitude values. Altitude is always set to zero.
 *
 * Class also provides a capability to to convert (approximately) these four vertices to GeoBounds object i.e. North, East, West and South
 * boundaries.
 *
 * If you are interested in the logic used to generate this object then please refer to BoundsGeneration class in mapengine-worldwind-apk project.
 */

public class EmpBoundingArea extends GeoBounds implements IEmpBoundingArea {
    private static String TAG = EmpBoundingArea.class.getSimpleName();
    public final static int REQUIRED_VERTICES = 4;
    private final IGeoPosition[] vertices;

    private final ICamera camera;              // Camera when vertices were calculated, we will need this for
                                               // adjusting the distance.
    private final boolean cameraOnScreen;      // True if camera position is on screen.

    private final IGeoPosition geometricCenter; // Position corresponding to the geometric center of the vertices.

    // We will adjust vertices to restrict the distance from the camera. Purpose is to reduce the size of resources used
    // by SEC Military Symbol renderer. We will do the adjustment only when user tries to retrieve anything related
    // to the vertices.

    private final double MAXIMUM_LONGITUDE_SPAN = 180.0;

    // NOTE: We are no longer adjusting the vertices. If SPAN of the bounding area is more that 180 deg then we simply
    // return the bounding box that was calculated when bounding area was calculated. We are still populating
    // the adjusted vertices and span calculation is done on the adjustedVertices member. Since this is a work in
    // progress we have kept the member variable and keep using it.

    private IGeoPosition[] adjustedVertices = null;   // Adjust the vertices to keep longitude range to 180 degrees.
    boolean adjustVerticesStatus = false;             // This is set to true if vertices could be adjusted to meet the 180 span requirement.

    private String boundingAreaString = null;         // Stores bounding area String that was created in toString operation.

    /**
     * All parameters must be non-null and have valid values.
     * @param currentCamera - Camera object when the vertices were calculated.
     * @param v1
     * @param v2
     * @param v3
     * @param v4
     */
    public EmpBoundingArea(ICamera currentCamera, boolean cameraOnScreen, IGeoPosition v1, IGeoPosition v2, IGeoPosition v3, IGeoPosition v4,
                           IGeoBounds geoBounds, IGeoPosition geometricCenter) {

        if((null == v1) || (null == v2) || (null == v3) || (null == v4)) {
            throw new IllegalArgumentException("All vertices must be non-null");
        }
        vertices = new IGeoPosition[REQUIRED_VERTICES];
        vertices[0] = v1;
        vertices[1] = v2;
        vertices[2] = v3;
        vertices[3] = v4;

        for(int ii = 0; ii < vertices.length; ii++) {
            if (Double.isNaN(vertices[ii].getLatitude()) || (vertices[ii].getLatitude() < global.LATITUDE_MINIMUM) || (vertices[ii].getLatitude() > global.LATITUDE_MAXIMUM)) {
                throw new InvalidParameterException("Latitude is Out Of Range for vertex " + (ii+1));
            }

            if (Double.isNaN(vertices[ii].getLongitude()) || (vertices[ii].getLongitude() < global.LONGITUDE_MINIMUM) || (vertices[ii].getLongitude() > global.LONGITUDE_MAXIMUM)) {
                throw new InvalidParameterException("Longitude is Out Of Range for vertex " + (ii+1));
            }

            vertices[ii].setAltitude(0);
        }

        camera = new Camera();
        camera.copySettingsFrom(currentCamera);
        this.cameraOnScreen = cameraOnScreen;
        this.geometricCenter = geometricCenter;

        super.setEast(geoBounds.getEast());
        super.setWest(geoBounds.getWest());
        super.setNorth(geoBounds.getNorth());
        super.setSouth(geoBounds.getSouth());
    }

    /**
     * Get Longitude Span
     *    - If all longitudes are either positive or negative then span is simply the difference between highest and lowest
     *    - Otherwise we have to check the span over the 0 longitude or 180 degree longitude. Whichever is the lowers we will return.
     *    - We are presuming that vertices were copied to adjusted vertices in case we need to reintroduce the adjustment.
     * @return
     */
    private double getLongitudeSpan() {
        // If all vertices are either positive or negative then span is less tha or equal to 180
        if ((adjustedVertices[0].getLongitude() >= 0 && adjustedVertices[1].getLongitude() >= 0 &&
                adjustedVertices[2].getLongitude() >= 0 && adjustedVertices[3].getLongitude() >= 0) ||
                (adjustedVertices[0].getLongitude() <= 0 && adjustedVertices[1].getLongitude() <= 0 &&
                        adjustedVertices[2].getLongitude() <= 0 && adjustedVertices[3].getLongitude() <= 0)) {
            double lowest = global.LONGITUDE_MAXIMUM + 1.0;
            double highest = global.LONGITUDE_MINIMUM - 1.0;
            for (int ii = 0; ii < adjustedVertices.length; ii++) {
                if (adjustedVertices[ii].getLongitude() < lowest) {
                    lowest = adjustedVertices[ii].getLongitude();
                }
                if (adjustedVertices[ii].getLongitude() > highest) {
                    highest = adjustedVertices[ii].getLongitude();
                }
            }

            return Math.abs(highest - lowest);
        } else {
            // Find the lowest longitude and highest longitude
            double lowest = global.LONGITUDE_MAXIMUM + 1.0;
            double highest = global.LONGITUDE_MINIMUM  - 1.0;
            for (int ii = 0; ii < adjustedVertices.length; ii++) {
                if (adjustedVertices[ii].getLongitude() < lowest) {
                    lowest = adjustedVertices[ii].getLongitude();
                }
                if (adjustedVertices[ii].getLongitude() > highest) {
                    highest = adjustedVertices[ii].getLongitude();
                }
            }

            // check around longitude 0 if span is less than 180
            double span1 = Math.abs(lowest) + highest;
            if (span1 < MAXIMUM_LONGITUDE_SPAN) {
                return span1;
            }


            // Find the lowest positive and highest negative longitude
            double lowestPositive = global.LONGITUDE_MAXIMUM + 1.0;
            double highestNegative = global.LONGITUDE_MINIMUM  - 1.0;
            for (int ii = 0; ii < adjustedVertices.length; ii++) {
                if (adjustedVertices[ii].getLongitude() >= 0 && adjustedVertices[ii].getLongitude() < lowestPositive) {
                    lowestPositive = adjustedVertices[ii].getLongitude();
                }
                if (adjustedVertices[ii].getLongitude() <= 0 && adjustedVertices[ii].getLongitude() > highestNegative) {
                    highestNegative = adjustedVertices[ii].getLongitude();
                }
            }

            // check around longitude 180 if span is less than 180
            double span2 = (global.LONGITUDE_MAXIMUM - lowestPositive) + (global.LONGITUDE_MAXIMUM - Math.abs(highestNegative));
            if (span2 < MAXIMUM_LONGITUDE_SPAN) {
                return span2;
            }

            if(span1 < span2) {
                return span1;
            } else {
                return span2;
            }
        }
    }

    /**
     * Get the current span for the bounded area. If span is more than 180 deg then return false else return true. If the logic to
     * adjust vertices for span is to be reintroduced then we should do that here. Currently there is no adjustment.
     * @return
     */
    private boolean adjustVerticesByDistance() {
        if(null != adjustedVertices) {
            return adjustVerticesStatus;
        }

        adjustVerticesStatus = true;
        adjustedVertices = new IGeoPosition[vertices.length];
        for (int ii = 0; ii < vertices.length; ii++) {
            adjustedVertices[ii] = new EmpGeoPosition(vertices[ii]);
        }
        double span = getLongitudeSpan();
        Log.i(TAG, "Initial Span " + span);
        if(span < MAXIMUM_LONGITUDE_SPAN) {
            return true;
        } else {
            return false; // We are not adjusting vertices. Instead caller should simply use the bounding box.
        }
    }


    /**
     * Converts vertices to a String to a format required by Military Symbol render-er. It returns a string based on adjustedVertices that
     * meets the SEC Render-er requirement of 180 degrees max span of longitude. This calculation is very much approximate and may need
     * more work in future.
     *
     * If quadrilateral vertices can be adjusted to meet the 180 span requirement then that is the output. Otherwise use calculated bounds to
     * generate a string (this has a different format). This will usually be the case when camera is at or near the pole.
     * @return
     */

    @Override
    public String toString() {
        if(null != boundingAreaString) {
            return boundingAreaString;
        }
        if(adjustVerticesByDistance()) {

            StringBuilder builder = new StringBuilder();
            for (int ii = adjustedVertices.length - 1; ii >= 0; ii--) {
                builder.append(adjustedVertices[ii].getLongitude() + "," + adjustedVertices[ii].getLatitude() + " ");
            }
            builder.append(adjustedVertices[adjustedVertices.length - 1].getLongitude() + "," + adjustedVertices[adjustedVertices.length - 1].getLatitude());
            boundingAreaString = builder.toString();
        } else {
            // Span of the area is larger than 180 deg, so simply sue the bounding box which is guarenteed to have a span less than 120.
            // This usually happens when complete globe is visible of we are at or near the pole.
            boundingAreaString = getWest() + "," + getSouth() + "," + getEast() + "," + getNorth();
        }
        return boundingAreaString;
    }

    /**
     * Fetch the four vertices. We are cloning the vertices and returning. Application will not be allowed to change this object.
     * @return
     */
    public IGeoPosition[] getBoundingVertices() {
        IGeoPosition[] bounds = new IGeoPosition[REQUIRED_VERTICES];
        for(int ii = 0; ii < vertices.length; ii++) {
            bounds[ii] = new GeoPosition();
            bounds[ii].setLatitude(vertices[ii].getLatitude());
            bounds[ii].setLongitude(vertices[ii].getLongitude());
        }
        return bounds;
    }

    /**
     * Application is not allowed to change the boundaries of the underlying GeoBounds object as they are calculated from
     * the vertices (approximation). Application can use getGeoBounds or getEmpBoundingBox method to fetch an object it can change.
     * @param west
     */
    @Override
    public void setWest(double west){
        throw new IllegalStateException("Cannot set on " + this.getClass().getSimpleName());
    }

    /**
     * Application is not allowed to change the boundaries of the underlying GeoBounds object as they are calculated from
     * the vertices (approximation). Application can use getGeoBounds or getEmpBoundingBox method to fetch an object it can change.
     * @param east
     */
    @Override
    public void setEast(double east){
        throw new IllegalStateException("Cannot set on " + this.getClass().getSimpleName());
    }

    /**
     * Application is not allowed to change the boundaries of the underlying GeoBounds object as they are calculated from
     * the vertices (approximation). Application can use getGeoBounds or getEmpBoundingBox method to fetch an object it can change.
     * @param north
     */
    @Override
    public void setNorth(double north){
        throw new IllegalStateException("Cannot set on " + this.getClass().getSimpleName());
    }

    /**
     * Application is not allowed to change the boundaries of the underlying GeoBounds object as they are calculated from
     * the vertices (approximation). Application can use getGeoBounds or getEmpBoundingBox method to fetch an object it can change.
     * @param south
     */
    @Override
    public void setSouth(double south){
        throw new IllegalStateException("Cannot set on " + this.getClass().getSimpleName());
    }

    /**
     * Builds a GeoBounds object using the calculated boundaries.
     * @return
     */
    @Override
    public IGeoBounds getGeoBounds() {
        IGeoBounds geoBounds = new GeoBounds();
        geoBounds.setNorth(getNorth());
        geoBounds.setEast(getEast());
        geoBounds.setWest(getWest());
        geoBounds.setSouth(getSouth());
        return geoBounds;
    }

    /**
     * Builds a EmpBoundingBox using the calculated boundaries.
     * @return
     */
    @Override
    public EmpBoundingBox getEmpBoundingBox() {
        EmpBoundingBox empBoundingBox = new EmpBoundingBox(getNorth(), getSouth(), getEast(), getWest());
        return empBoundingBox;
    }

    @Override
    public IGeoPosition getCenter() {
        List<IGeoPosition> cornersFound = new ArrayList<>();

        IGeoPosition nw = new GeoPosition();
        nw.setLatitude(getNorth());
        nw.setLongitude(getWest());
        cornersFound.add(nw);

        IGeoPosition ne = new GeoPosition();
        ne.setLatitude(getNorth());
        ne.setLongitude(getEast());
        cornersFound.add(ne);

        IGeoPosition se = new GeoPosition();
        se.setLatitude(getSouth());
        se.setLongitude(getEast());
        cornersFound.add(se);

        IGeoPosition sw = new GeoPosition();
        sw.setLatitude(getSouth());
        sw.setLongitude(getWest());
        cornersFound.add(sw);

        return GeoLibrary.getCenter(cornersFound);
    }

    @Override
    public boolean cameraPositionIsVisible() {
        return cameraOnScreen;
    }

    @Override
    public IGeoPosition getGeometricCenter() { return new EmpGeoPosition(geometricCenter);}
}
