package mil.emp3.api.enums;

/**
 * Types of the basic shape feature properties that can be changed via an editor, {@link FeatureEditUpdateTypeEnum}
 */

public enum FeaturePropertyChangedEnum {
    /**
     * Azimuth of the feature was changed.
     */
    AZIMUTH_PROPERTY_CHANGED,
    /**
     * Radius of the feature was changed.
     */
    RADIUS_PROPERTY_CHANGED,
    /**
     * Semi major radius of the ellipse feature was changed.
     */
    SEMI_MAJOR_PROPERTY_CHANGED,
    /**
     * Semi minor radius of the ellipse feature was changed.
     */
    SEMI_MINOR_PROPERTY_CHANGED,
    /**
     * Width of the feature was changed.
     */
    WIDTH_PROPERTY_CHANGED,
    /**
     * Height of the feature was changed.
     */
    HEIGHT_PROPERTY_CHANGED
}
