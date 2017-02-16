package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * Type of events generated when map enters {@link EditorMode} EDIT_MODE and user DRAG gestures are processed. These events are triggered when application
 * issues editFeature, cancelEdit, completeEdit on {@link mil.emp3.api.interfaces.IMap}
 */
public enum FeatureEditEventEnum implements IEventEnum {
    /**
     * EDIT_MODE has started.
     */
    EDIT_START,
    /**
     * Edit status has changed e.g. DRAG gesture continues or is complete, see {@link FeatureEditUpdateTypeEnum}
     */
    EDIT_UPDATE,
    /**
     * Application issued Cancel action was processed and Map has exited the EDIT_MODE
     */
    EDIT_CANCELED,
    /**
     * Application issues Complete action was processed and Map has exited the EDIT_MODE
     */
    EDIT_COMPLETE
}
