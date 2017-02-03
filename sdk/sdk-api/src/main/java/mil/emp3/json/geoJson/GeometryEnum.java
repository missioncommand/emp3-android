package mil.emp3.json.geoJson;

/**
 * Enumerates GeoJSON geometry types.
 */

public enum GeometryEnum {
    POINT ("Point"),
    LINE ("LineString"),
    POLYGON ("Polygon"),
    MULTI_POINT("MultiPoint"),
    MULTI_LINE("MultiLineString"),
    MULTI_POLYGON("MultiPolygon");

    private String geometryEnum;

    GeometryEnum(String g) {
        geometryEnum = g;
    }

    public static GeometryEnum fromString(String geometry) {
        switch (geometry) {
            case "Point":
                return POINT;
            case "LineString":
                return LINE;
            case "Polygon":
                return POLYGON;
        }
        return null;
    }

    @Override
    public String toString(){
        return geometryEnum;
    }
}
