package mil.emp3.api.enums;

/**
 * {@link mil.emp3.api.abstracts.Map} is normally in INACTIVE mode but will transition to one of the Editor Modes as described below.
 * Mode transitions are controlled by following Map methods
 * <ul>
 *     <li>editFeature, cancelEdit, completeEdit - EDIT_MODE</li>
 *     <li>drawFeature, cancelDraw, completeDraw - DRAW_MODE</li>
 *     <li>drawFreehand, drawFreehandExit - FREEHAND_MODE</li>
 * </ul>
 */
public enum EditorMode {
    /**
     * Map is in normal mode and can transition to any other based on application actions and current value of {@link MapMotionLockEnum}
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
