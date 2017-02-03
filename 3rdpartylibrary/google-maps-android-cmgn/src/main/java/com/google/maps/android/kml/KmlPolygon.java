package com.google.maps.android.kml;

import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a KML Polygon. Contains a single array of outer boundary coordinates and an array of
 * arrays for the inner boundary coordinates.
 *
 * NOTE: This file has been modified from its initial content to account for Common Map Geospatial Notation.
 */
public class KmlPolygon implements KmlGeometry<List<List<IGeoPosition>>> {

    public static final String GEOMETRY_TYPE = "Polygon";

    private final List<IGeoPosition> mOuterBoundaryCoordinates;

    private final List<List<IGeoPosition>> mInnerBoundaryCoordinates;

    /**
     * Creates a new KmlPolygon object
     *
     * @param outerBoundaryCoordinates single array of outer boundary coordinates
     * @param innerBoundaryCoordinates multiple arrays of inner boundary coordinates
     */
    public KmlPolygon(List<IGeoPosition> outerBoundaryCoordinates,
            List<List<IGeoPosition>> innerBoundaryCoordinates) {
        if (outerBoundaryCoordinates == null) {
            throw new IllegalArgumentException("Outer boundary coordinates cannot be null");
        } else {
            mOuterBoundaryCoordinates = outerBoundaryCoordinates;
            mInnerBoundaryCoordinates = innerBoundaryCoordinates;
        }
    }

    /**
     * Gets the type of geometry
     *
     * @return type of geometry
     */
    @Override
    public String getGeometryType() {
        return GEOMETRY_TYPE;
    }

    /**
     * Gets an array of outer boundary coordinates
     *
     * @return array of outer boundary coordinates
     */
    public List<IGeoPosition> getOuterBoundaryCoordinates() {
        return mOuterBoundaryCoordinates;
    }

    /**
     * Gets an array of arrays of inner boundary coordinates
     *
     * @return array of arrays of inner boundary coordinates
     */
    public List<List<IGeoPosition>> getInnerBoundaryCoordinates() {
        return mInnerBoundaryCoordinates;
    }

    /**
     * Gets the coordinates
     *
     * @return ArrayList of an ArrayList of LatLng points
     */
    public List<List<IGeoPosition>> getGeometryObject() {
        List<List<IGeoPosition>> coordinates = new ArrayList<>();
        coordinates.add(mOuterBoundaryCoordinates);
        //Polygon objects do not have to have inner holes
        if (mInnerBoundaryCoordinates != null) {
            coordinates.addAll(mInnerBoundaryCoordinates);
        }
        return coordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n outer coordinates=").append(mOuterBoundaryCoordinates);
        sb.append(",\n inner coordinates=").append(mInnerBoundaryCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}
