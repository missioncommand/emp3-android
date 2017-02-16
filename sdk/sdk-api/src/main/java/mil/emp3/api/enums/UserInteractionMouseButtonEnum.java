package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Types of supported mouse buttons
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
