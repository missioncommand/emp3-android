package mil.emp3.api.utils;

import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.global;

/**
 * This class defines a bounded box.
 */

public class EmpBoundingBox extends GeoBounds {
    private static String TAG = EmpBoundingBox.class.getSimpleName();
    public final static int REQUIRED_VERTICES = 4;
    private final IGeoPosition[] vertices;

    // We will calculate the bounds required for the GeoBounds class only when application tries to get them.
    private boolean boundsCalculated = false;

    public EmpBoundingBox(IGeoPosition v1, IGeoPosition v2, IGeoPosition v3, IGeoPosition v4) {

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
    }

    /**
     * Sets North boundary to the highest latitude
     * Sets South boundary to the smallest latitude.
     */
    private void setNorthAndSouth() {
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

        super.setNorth(tmpNorth);
        super.setSouth(tmpSouth);
    }

    /**
     * This methods finds APPROXIMATE east/west boundaries for the underlying GeoBounds object.
     *    - Get the center of the vertices.
     *    - Find the bearing of each vertex from center to the vertex.
     *    - Set East boundary to the longitude of the vertex with smallest bearing
     *    - Set West boundary to the longitude of the vertex with highest bearing.
     */
    private void setEastAndWest() {

        // Get the center
        List<IGeoPosition> positionList = new ArrayList<>();
        for(int ii = 0; ii < vertices.length; ii++) {
            positionList.add(vertices[ii]);
        }
        IGeoPosition center = GeoLibrary.getCenter(positionList);

        // Now compute bearing of each vertex and figure out vertex with smallest and highest bearing.
        double[] bearing = new double[vertices.length];
        double smallestBearing = -361.0;
        double highestBearing = 361.0;
        int smallestBearingIndex = -1;
        int highestBearingIndex = -1;
        for(int ii = 0; ii < vertices.length; ii++) {
            bearing[ii] = GeoLibrary.computeBearing(center, vertices[ii]);
            Log.d(TAG, "bearing " + bearing[ii] + " " + vertices[ii].getLatitude() + " " + vertices[ii].getLongitude());
            if(smallestBearing < -360.0) {
                smallestBearingIndex = ii;
                highestBearingIndex = ii;
                smallestBearing = bearing[ii];
                highestBearing = bearing[ii];
            } else {
                if(bearing[ii] < smallestBearing) {
                    smallestBearing = bearing[ii];
                    smallestBearingIndex = ii;
                } else if(bearing[ii] > highestBearing) {
                    highestBearing = bearing[ii];
                    highestBearingIndex = ii;
                }
            }
        }

        // Set east and west.
        if(smallestBearingIndex >= 0) {
            super.setEast(vertices[smallestBearingIndex].getLongitude());
        }
        if(highestBearingIndex >= 0) {
            super.setWest(vertices[highestBearingIndex].getLongitude());
        }
    }
    public String toString() {
        String value = "";
        return value;
    }

    public IGeoPosition[] getBounds() {
        IGeoPosition[] bounds = new IGeoPosition[REQUIRED_VERTICES];
        for(int ii = 0; ii < vertices.length; ii++) {
            bounds[ii] = new GeoPosition();
            bounds[ii].setLatitude(vertices[ii].getLatitude());
            bounds[ii].setLongitude(vertices[ii].getLongitude());
        }
        return bounds;
    }
    public void setWest(double west){
        throw new IllegalStateException("Cannot set on " + this.getClass().getSimpleName());
    }

    public void setEast(double east){
        throw new IllegalStateException("Cannot set on " + this.getClass().getSimpleName());
    }

    public void setNorth(double north){
        throw new IllegalStateException("Cannot set on " + this.getClass().getSimpleName());
    }

    public void setSouth(double south){
        throw new IllegalStateException("Cannot set on " + this.getClass().getSimpleName());
    }

    private void calculateBounds() {
        if(!boundsCalculated) {
            // Set approximate North, south, east and west.
            setNorthAndSouth();
            setEastAndWest();
            boundsCalculated = true;
        }
    }
    @Override
    public double getWest() {
        calculateBounds();
        return super.getWest();
    }

    @Override
    public double getEast() {
        calculateBounds();
        return super.getEast();
    }

    @Override
    public double getNorth() {
        calculateBounds();
        return super.getNorth();
    }

    @Override
    public double getSouth() {
        calculateBounds();
        return super.getSouth();
    }

    /**
     * This method returns the delta of the north and south latitudes.
     * @return Degrees
     */
    public double deltaLatitude() {
        return Math.abs(getNorth() - getSouth());
    }

    /**
     * This method returns the delta between the west and east logitudes.
     * @return degrees.
     */
    public double deltaLongitude() {
        double delta = getEast() - getWest();

        if (delta < 0) {
            delta = (delta + 360) % 360;
        }
        return delta;
    }

    /**
     * This method check if the international date line is within the bounding box.
     * @return True if the IDL is contained in the bounding box, false if not.
     */
    public boolean containsIDL() {
        return (getWest() > getEast());
    }
}
