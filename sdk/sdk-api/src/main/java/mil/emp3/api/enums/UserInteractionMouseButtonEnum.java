package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Created by raju on 9/2/2016.
 * Enumerates mouse buttons
 */
public enum UserInteractionMouseButtonEnum implements IEventEnum {
    /**
     *  No button, value for initialization.
     */
    NONE,
    /**
     * Left mouse button interaction.
     */
    LEFT,
    /**
     * Middle mouse button interaction.
     */
    MIDDLE,
    /**
     * Right mouse button interaction.
     */
    RIGHT
}
