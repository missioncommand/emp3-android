package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;
/**
 * This class enumerates the type of feature edit events
 */
public enum FeatureEditEventEnum implements IEventEnum {
    /**
     * This value indicates that the edit has started. The event target provides the feature being edited.
     */
    EDIT_START,
    /**
     * This value indicates that the feature has been changed. The event target is a reference to the updated feature.
     */
    EDIT_UPDATE,
    /**
     * This value indicates that the edit has been canceled. The event target is a reference of the feature prior to the edit.
     */
    EDIT_CANCELED,
    /**
     * This value indicates that the edit has completed. The event target is a reference of the edited feature.
     */
    EDIT_COMPLETE
}
