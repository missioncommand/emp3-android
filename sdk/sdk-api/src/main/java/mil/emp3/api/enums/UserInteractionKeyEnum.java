package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Created by raju on 9/2/2016.
 * Enumerates keys which are pressed during mouse interaction
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
