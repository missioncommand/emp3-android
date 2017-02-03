package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * This class enumerates the type of feature draw events
 */
public enum FeatureDrawEventEnum implements IEventEnum {
    /**
     * This value indicates that the draw has started. The event target provides the feature being drawn.
     */
    DRAW_START,
    /**
     * This value indicates that the feature has been changed. The event target is a reference to the updated feature.
     */
    DRAW_UPDATE,
    /**
     * This value indicates that the draw has been canceled.
     */
    DRAW_CANCELED,
    /**
     * This value indicates that the draw has completed. The event target is a reference of the drawn feature.
     */
    DRAW_COMPLETE
}
