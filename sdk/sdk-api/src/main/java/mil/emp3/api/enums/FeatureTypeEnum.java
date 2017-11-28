package mil.emp3.api.enums;

/**
 * Types of features supported by EMP, there is a concrete class for each of the types.
 */
public enum FeatureTypeEnum {
    /**
     * Circular feature. {@link mil.emp3.api.Circle}
     */
    GEO_CIRCLE,
    /**
     * Ellipse feature {@link mil.emp3.api.Ellipse}
     */
    GEO_ELLIPSE,
    /**
     * Path (aka. multi segment line) feature. {@link mil.emp3.api.Path}
     */
    GEO_PATH,
    /**
     * Single point icon feature. {@link mil.emp3.api.Point}
     */
    GEO_POINT,
    /**
     * Polygon feature.{@link mil.emp3.api.Polygon}
     */
    GEO_POLYGON,
    /**
     * Rectangular feature. {@link mil.emp3.api.Rectangle}
     */
    GEO_RECTANGLE,
    /**
     * Square feature. {@link mil.emp3.api.Square}
     */
    GEO_SQUARE,
    /**
     * MilStd feature. {@link mil.emp3.api.MilStdSymbol
     */
    GEO_MIL_SYMBOL,
    /**
     * Air control measure (aka 3D airspace) feature. {@link mil.emp3.api.AirControlMeasure}
     */
    GEO_ACM,
    /**
     * Geo positioned text.{@link mil.emp3.api.Text}
     */
    GEO_TEXT,
    /**
     * KML
     */
    KML,
    /**
     * GeoJSON
     */
    GEOJSON
}
