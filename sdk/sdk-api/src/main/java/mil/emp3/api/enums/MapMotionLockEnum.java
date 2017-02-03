package mil.emp3.api.enums;

/**
 * This class enumerates the different locks that can be placed on the map's motion.
 */
public enum MapMotionLockEnum {

    /**
     * This enumerated value indicates that the map view is free to change i.e. pan, zoom, rotate, tilt
     * based on Gestures. Note that pan, zoom, rotate, tilt is not necessarily an exhaustive list of motions
     */
    UNLOCKED,
    /**
     * This enumerated value indicates that the map will not pan, zoom or rotate, tilt based on Gestures. The only
     * motion allowed is panning if the user is dragging an object at the edge of the viewing area.
     * This is currently used by the Freehand Editor
     */
    SMART_LOCK,
    /**
     * This enumerated value indicates that the map will not pan, zoom, rotate, tilt based on Gestures.
     * Client application can still use Camera and LookAt to change the map view.
     */
    LOCKED
}
