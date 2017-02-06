package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Type of events generated when map enters {@link EditorMode} DRAW_MODE and user DRAG gestures are processed. These events are triggered when application
 * issues drawFeature, cancelDraw, completeDraw on {@link mil.emp3.api.interfaces.IMap}
 */
public enum FeatureDrawEventEnum implements IEventEnum {
    /**
     * Draw mode has started
     */
    DRAW_START,
    /**
     * Draw status has changed e.g. DRAG gesture continues or is complete, see {@link FeatureEditUpdateTypeEnum}
     */
    DRAW_UPDATE,
    /**
     * Application issued Cancel action was processed and Map has exited the DRAW_MODE
     */
    DRAW_CANCELED,
    /**
     * Application issues Complete action was processed and Map has exited the DRAW_MODE
     */
    DRAW_COMPLETE
}
