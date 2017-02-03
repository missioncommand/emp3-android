package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * This class enumerates the possible events that are generated during a freehand draw operation.
 */
public enum MapFreehandEventEnum implements IEventEnum{
    /**
     * This enumerated value indicates that the map has placed itself
     * into freehand draw mode.
     */
    MAP_ENTERED_FREEHAND_DRAW_MODE,
    /**
     * This enumerated value indicates that the user started to draw on the map.
     */
    MAP_FREEHAND_LINE_DRAW_START,
    /**
     * This enumerated value indicates that the that the line has been updated.
     */
    MAP_FREEHAND_LINE_DRAW_UPDATE,
    /**
     * This enumerated value indicates that the user finished drawing a line (lifted its finger).
     */
    MAP_FREEHAND_LINE_DRAW_END,
    /**
     * This enumerated value indicates that the map has exited freehand draw mode.
     */
    MAP_EXIT_FREEHAND_DRAW_MODE
}
