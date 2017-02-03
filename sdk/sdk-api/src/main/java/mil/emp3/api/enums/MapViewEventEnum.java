package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Types of events generated by the map when the its view changes. {@link mil.emp3.api.events.MapViewChangeEvent}
 */
public enum MapViewEventEnum implements IEventEnum {
    /**
     * Map's view is in motion.
     */
    VIEW_IN_MOTION,
    /**
     * Map's view has stopped moving. The event contains the final camera settings.
     */
    VIEW_MOTION_STOPPED
}
