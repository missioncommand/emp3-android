package mil.emp3.api.enums;

/**
 * This class enumerates the type of operation performed during a feature edit update event.
 */
public enum FeatureEditUpdateTypeEnum {
    /**
     * This value indicates that one or more coordinates have been added to the feature. The event contains an array of indexes of the new coordinates.
     */
    COORDINATE_ADDED,
    /**
     * This value indicates that one or more coordinates have been moved. The event contains an array of indexes of the coordinates moved.
     */
    COORDINATE_MOVED,
    /**
     * This value indicates that one or more coordinates have been deleted from the feature. The event contains an array of indexes of the coordinates deleted.
     */
    COORDINATE_DELETED,
    /**
     * This value indicates that a MilStd modifier has been changed. The event contains the modifier affected. This only applies to MilStd features.
     */
    MILSTD_MODIFIER_UPDATED,
    /**
     * This value indicates that an Air Control Measure attribute has been changed. The event contains the attribute affected. This only applies to ACM features.
     */
    ACM_ATTRIBUTE_UPDATED
}
