package mil.emp3.api.enums;

/**
 * Type of {@link mil.emp3.api.listeners.IEventListener}.
 */
public enum EventListenerTypeEnum {
    /**
     * Listener for {@link ContainerEventEnum}
     */
    CONTAINER_EVENT_LISTENER,
    /**
     * Listener for {@link FeatureEditEventEnum}
     */
    FEATURE_EVENT_LISTENER,
    /**
     * Listener for {@link UserInteractionEventEnum}.
     */
    FEATURE_INTERACTION_EVENT_LISTENER,
    /**
     * Listener for {@link MapStateEnum}.
     */
    MAP_STATE_CHANGE_EVENT_LISTENER,
    /**
     * Listener for {@link UserInteractionEventEnum}
     */
    MAP_INTERACTION_EVENT_LISTENER,
    /**
     * Listener for {@link MapViewEventEnum}
     */
    MAP_VIEW_CHANGE_EVENT_LISTENER,

    /**
     * Listener for {@link MapFeatureEventEnum}
     */
    MAP_FEATURE_ADDED_EVENT_LISTENER,
    /**
     * Listener for {@link MapFeatureEventEnum}
     */
    MAP_FEATURE_REMOVED_EVENT_LISTENER,
    /**
     * Listener for {@link VisibilityActionEnum}
     */
    VISIBILITY_EVENT_LISTENER,
    /**
     * Listener for {@link CameraEventEnum}
     */
    CAMERA_EVENT_LISTENER,
    /**
     * Listener for {@link LookAtEventEnum}
     */
    LOOKAT_EVENT_LISTENER,
    /**
     * Listener for {@link FeatureEditEventEnum}
     */
    FEATURE_EDIT_EVENT_LISTENER,
    /**
     * Listener for {@link FeatureDrawEventEnum}
     */
    FEATURE_DRAW_EVENT_LISTENER,
    /**
     * Listener for {@link MapFreehandEventEnum}
     */
    FREEHAND_DRAW_EVENT_LISTENER
}
