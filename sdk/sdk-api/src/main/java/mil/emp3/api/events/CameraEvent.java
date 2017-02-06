package mil.emp3.api.events;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.enums.CameraEventEnum;

/**
 * This abstract class implements a camera event. It is generated when a camera is associated with a map and the map's view changes.
 */
public abstract class CameraEvent extends Event<CameraEventEnum, ICamera> {

    // When apply interface is invoked on the camera an event is generated that is handled by map engine to move the camera.
    // map engine needs to know if camera move should be animated. This parameter is of no use to applications.

    private boolean animate;
    /**
     * This protected constructor is for internal use only.
     * @param eEvent
     * @param oCamera 
     */
    protected CameraEvent(CameraEventEnum eEvent, ICamera oCamera, boolean animate) {
        super(eEvent, oCamera);
        this.animate = animate;
    }

    /**
     * This method retrieves the target camera on which the event was generated.
     * @return An object that implements the {@link ICamera} interface.
     */
    public abstract ICamera getCamera();

    /**
     * Returns the setting of animation. True means camera move will be animated.
     * @return boolean value of animate.
     */

    public boolean isAnimate() {
        return animate;
    }
}
