package mil.emp3.api.enums;

/**
 * This class enumerates the different types of event listeners.
 */
public enum EventListenerTypeEnum {
    /**
     * This value identifies a container event listener.
     */
    CONTAINER_EVENT_LISTENER,
    /**
     * This value identifies a feature event listener.
     */
    FEATURE_EVENT_LISTENER,
    /**
     * This value identifies a feature interaction event listener.
     */
    FEATURE_INTERACTION_EVENT_LISTENER,
    /**
     * This value identifies a map state change event listener.
     */
    MAP_STATE_CHANGE_EVENT_LISTENER,
    /**
     * This value identifies a map interaction event listener.
     */
    MAP_INTERACTION_EVENT_LISTENER,
    /**
     * This value identifies a map view change event listener.
     */
    MAP_VIEW_CHANGE_EVENT_LISTENER,

    /**
     * This value identifies a map feature added event listener.
     */
    MAP_FEATURE_ADDED_EVENT_LISTENER,
    /**
     * This value identifies a map feature removed event listener.
     */
    MAP_FEATURE_REMOVED_EVENT_LISTENER,
    /**
     * This value identifies a visibility event listener.
     */
    VISIBILITY_EVENT_LISTENER,
    /**
     * This value identifies a camera event listener.
     */
    CAMERA_EVENT_LISTENER,
    /**
     * This value identifies a lookAt event listener.
     */
    LOOKAT_EVENT_LISTENER,
    /**
     * This value identifies a feature edit event listener.
     */
    FEATURE_EDIT_EVENT_LISTENER,
    /**
     * This value identifies a feature draw event listener.
     */
    FEATURE_DRAW_EVENT_LISTENER,
    /**
     * This value identifies a freehand draw event listener.
     */
    FREEHAND_DRAW_EVENT_LISTENER
}
