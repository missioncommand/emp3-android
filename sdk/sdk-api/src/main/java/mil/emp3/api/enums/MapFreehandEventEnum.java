package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Types of events generated during a freehand draw operation. Map enters FREEHAND_DRAW {@link EditorMode} when application issues drawFreehand action.
 */
public enum MapFreehandEventEnum implements IEventEnum{
    /**
     * Map entered freehand draw mode as a result of drawFreehand issued by the application
     */
    MAP_ENTERED_FREEHAND_DRAW_MODE,
    /**
     * User started to draw on the map.
     */
    MAP_FREEHAND_LINE_DRAW_START,
    /**
     * Line was been updated by the user, i.e. user continued with the DRAG gesture
     */
    MAP_FREEHAND_LINE_DRAW_UPDATE,
    /**
     * User finished drawing a line (lifted its finger) i.e DRAG is complete.
     */
    MAP_FREEHAND_LINE_DRAW_END,
    /**
     * Map exited freehand draw mode as a result of drawFreehandExit issues by the  application
     */
    MAP_EXIT_FREEHAND_DRAW_MODE
}
