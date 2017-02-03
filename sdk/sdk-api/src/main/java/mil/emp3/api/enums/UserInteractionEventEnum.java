package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;
/**
 * This class enumerates the user interaction events that can be generated by a map.
 */
public enum UserInteractionEventEnum implements IEventEnum {
    /**
     * An event indicating that an object has been clicked.
     */
    CLICKED,

    /**
     * An event indicating that an object has been double clicked.
     */
    DOUBLE_CLICKED,

    /**
     * An event indicating that an object has been long pressed.
     */
    LONG_PRESS,
    //PRESS_RELEASE,

    /**
     * This value indicates that a feature or map has been dragged.
     */
    DRAG,
    /**
     * This enumerated value indicates that the drag operation has completed.
     */
    DRAG_COMPLETE
}
