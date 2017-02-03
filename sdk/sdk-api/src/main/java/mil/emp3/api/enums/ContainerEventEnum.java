package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * This class enumerates the container events that can be generated.
 */
public enum ContainerEventEnum implements IEventEnum {
    /**
     * This event indicates that one or more objects has been added to the container.
     */
    OBJECT_ADDED,
    /**
     * This event indicates that one or more objects has been removed from the container.
     */
    OBJECT_REMOVED
}
