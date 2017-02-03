package mil.emp3.api.enums;

/**
 * This class enumerates various editing states Map can enter as a result of application invoking following
 * methods of IMap:
 *     editFeature, cancelEdit, completeEdit
 *     drawFeature, cancelDraw, completeDraw
 *     drawFreehand, drawFreehandExit
 */
public enum EditorMode {
    /**
     * Map is NOT within editFeature, drawFeature or drawFreehand operation
     */
    INACTIVE,

    /**
     * Map is in editFeature operation and will return to INACTIVE on cancelEdit or completeEdit
     */
    EDIT_MODE,

    /**
     * Map is in drawFeature operation and will return to INACTIVE on cancelDraw or completeDraw
     */
    DRAW_MODE,

    /**
     * Map is within drawFreehand operation and will return to INACTIVE on drawFreehandExit
     */
    FREEHAND_MODE
}
