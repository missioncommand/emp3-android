package mil.emp3.api.enums;

/**
 * This class enumerates the different type of features.
 */
public enum FeatureTypeEnum {
    /**
     * This value indicates a circular feature.
     */
    GEO_CIRCLE,
    /**
     * This value indicates an ellipse feature
     */
    GEO_ELLIPSE,
    /**
     * This value indicates a path (aka. multi segment line) feature.
     */
    GEO_PATH,
    /**
     * This value indicates a single point icon feature.
     */
    GEO_POINT,
    /**
     * This value indicates a polygon feature.
     */
    GEO_POLYGON,
    /**
     * This value indicates a rectangular feature.
     */
    GEO_RECTANGLE,
    /**
     * This value indicates a square feature.
     */
    GEO_SQUARE,
    /**
     * This value indicates a MilStd feature.
     */
    GEO_MIL_SYMBOL,
    /**
     * This value indicates an air control measure (aka 3D airspace) feature.
     */
    GEO_ACM,
    /**
     * This enumerated value indicates a geo positioned text.
     */
    GEO_TEXT
}
