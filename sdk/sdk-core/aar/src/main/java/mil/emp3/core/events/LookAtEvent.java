package mil.emp3.core.events;

import mil.emp3.api.enums.LookAtEventEnum;
import mil.emp3.api.interfaces.ILookAt;

/**
 * This class its used by the core to create lookAt events.
 */
public class LookAtEvent extends mil.emp3.api.events.LookAtEvent {

    public LookAtEvent(LookAtEventEnum eventEnum, ILookAt lookAt, boolean animate) {
        super(eventEnum, lookAt, animate);
    }

    @Override
    public ILookAt getLookAt() {
        return this.getTarget();
    }

}
