package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 *
 * This class enumerate the events generated by the map when the its view changes.
 */
public enum MapViewEventEnum implements IEventEnum {
    /**
     * This enumerated value indicates that the map's view is in motion. The event contains the current camera settings.
     */
    VIEW_IN_MOTION,
    /**
     * This enumerated value indicates that the map's view has stopped moving. The event contains the final camera settings.
     */
    VIEW_MOTION_STOPPED
}
