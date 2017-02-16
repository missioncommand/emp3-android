package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Types of supported key presses
 */
public enum UserInteractionKeyEnum implements IEventEnum {
    /**
     * Ctrl key interaction.
     */
    CTRL,
    /**
     * Alt key interaction.
     */
    ALT,
    /**
     * Shift key interaction.
     */
    SHIFT
}
