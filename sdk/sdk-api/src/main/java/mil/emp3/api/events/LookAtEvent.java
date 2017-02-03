package mil.emp3.api.events;

import mil.emp3.api.enums.LookAtEventEnum;
import mil.emp3.api.interfaces.ILookAt;

/**
 * This abstract class implements a lookAT event. It is generated when a lookAt is associated with a map and the map's view changes.
 */
public abstract class LookAtEvent extends Event<LookAtEventEnum, ILookAt>{
    // When apply interface is invoked on the lookAt an event is generated that is handled by map engine to move the camera.
    // map engine needs to know if lookAt move should be animated. This parameter is of no use to applications.

    private boolean animate;

    /**
     * This protected constructor is for internal use only.
     * @param eEvent
     * @param oLookAt
     */
    protected LookAtEvent(LookAtEventEnum eEvent, ILookAt oLookAt, boolean animate) {
        super(eEvent, oLookAt);
        this.animate = animate;
    }

    /**
     * This method retrieves the target LookAt on which the event was generated.
     * @return An object that implements the {@link ILookAt} interface.
     */
    public abstract ILookAt getLookAt();


    /**
     * Returns the setting of animation. True means LookAt move will be animated.
     * @return boolean value of animate.
     */
    public boolean isAnimate() {
        return animate;
    }
}
