package mil.emp3.api.enums;

/**
 * Types of screen operations performed by the user when Map is in EDIT_MODE or DRAW_MODE {@link EditorMode}. The context for these operations is either
 * {@link mil.emp3.api.events.FeatureEditEvent} or {@link mil.emp3.api.events.FeatureEditEvent}
 */
public enum FeatureEditUpdateTypeEnum {
    /**
     * One or more coordinates were added to the feature. The event contains an array of the new coordinates.
     */
    COORDINATE_ADDED,
    /**
     * One or more coordinates were moved. The event contains an array of the coordinates moved.
     */
    COORDINATE_MOVED,
    /**
     * One or more coordinates were deleted from the feature. The event contains an array of the coordinates deleted.
     */
    COORDINATE_DELETED,
    /**
     * MilStd modifier was changed. The event contains the modifier affected. This applies to MilStd features only.
     */
    MILSTD_MODIFIER_UPDATED,
    /**
     * Air Control Measure attribute was changed. The event contains the attribute affected. This applies to ACM features only.
     */
    ACM_ATTRIBUTE_UPDATED,
     /**
     * Feature property was updated. It only applies to basic shapes such as Labels, Circles, Ellipse, Rectangle and Squares.
     */
    FEATURE_PROPERTY_UPDATED,
    /**
     * Position of the shape was updated.
     */
    POSITION_UPDATED
}
