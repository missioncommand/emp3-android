package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Types of events raised when application issues either selectFeature(s), deselectFeature(s) or clearSelected on the Map to highlight displayed features. When those actions are
 */
public enum FeatureEventEnum implements IEventEnum {
    /**
     * Feature was selected(highlighted)
     */
    FEATURE_SELECTED,

    /**
     * Feature has been unselected i.e reverted to its normal appearance.
     */
    FEATURE_DESELECTED
}

