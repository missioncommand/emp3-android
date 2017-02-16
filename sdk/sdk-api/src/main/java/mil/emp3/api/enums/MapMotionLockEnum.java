package mil.emp3.api.enums;

/**
 * Types of locks that can be placed on the map's motion. In a normal scenario map is in UNLOCKED state i.e. user can pan/zoom/rotate the map.
 */
public enum MapMotionLockEnum {

    /**
     * Map view is free to change i.e. pan, zoom, rotate, tilt
     * based on Gestures. Note that pan, zoom, rotate, tilt is not necessarily an exhaustive list of motions
     */
    UNLOCKED,
    /**
     * Map will not pan, zoom or rotate, tilt based on Gestures. The only
     * motion allowed is panning if the user is dragging an object at the edge of the viewing area.
     * This is currently used by the Freehand Editor. drawFeature and editFeature actions are not allowed when map
     * is in this lock mode.
     */
    SMART_LOCK,
    /**
     * Map will not pan, zoom, rotate, tilt based on Gestures.
     * Client application can still use Camera and LookAt to change the map view.
     *  drawFeature and editFeature actions are not allowed when map is in this lock mode.
     */
    LOCKED
}
