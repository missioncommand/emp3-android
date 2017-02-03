package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Created by raju on 7/22/2016.
 */
public enum LookAtEventEnum implements IEventEnum {
    /**
     * This enumerated value indicates that the target lookAt is currently in motion. One or more camera settings have been changed.
     */
    LOOKAT_IN_MOTION,

    /**
     * This enumerated value indicates that the target lookAt has stopped moving. The event contains the cameras final settings.
     */
    LOOKAT_MOTION_STOPPED

}
