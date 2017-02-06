package com.google.maps.android.kml;

import org.cmapi.primitives.IGeoPosition;

import java.util.List;

/**
 * Represents a KML LineString. Contains a single array of coordinates.
 *
 * NOTE: This file has been modified from its initial content to account for Common Map Geospatial Notation.
 */
public class KmlLineString implements KmlGeometry<List<IGeoPosition>> {

    public static final String GEOMETRY_TYPE = "LineString";

    final List<IGeoPosition> mCoordinates;

    /**
     * Creates a new KmlLineString object
     *
     * @param coordinates array of coordinates
     */
    public KmlLineString(List<IGeoPosition> coordinates) {
        if (coordinates == null) {
            throw new IllegalArgumentException("Coordinates cannot be null");
        }
        mCoordinates = coordinates;
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
     * Gets the coordinates
     *
     * @return ArrayList of LatLng
     */
    public List<IGeoPosition> getGeometryObject() {
        return mCoordinates;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(GEOMETRY_TYPE).append("{");
        sb.append("\n coordinates=").append(mCoordinates);
        sb.append("\n}\n");
        return sb.toString();
    }
}
