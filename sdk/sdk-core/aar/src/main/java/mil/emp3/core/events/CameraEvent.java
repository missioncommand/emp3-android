package mil.emp3.core.events;

import mil.emp3.api.enums.CameraEventEnum;
import mil.emp3.api.interfaces.ICamera;

/**
 *
 * This class its used by the core to create camera events.
 */
public class CameraEvent extends mil.emp3.api.events.CameraEvent {
    
    public CameraEvent(CameraEventEnum eventEnum, ICamera camera, boolean animate) {
        super(eventEnum, camera, animate);
    }

    @Override
    public ICamera getCamera() {
        return this.getTarget();
    }
    
}
