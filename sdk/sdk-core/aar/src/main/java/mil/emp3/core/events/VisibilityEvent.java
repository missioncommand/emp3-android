package mil.emp3.core.events;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.VisibilityActionEnum;

/**
 * This class is used by the core to generate visibility events.
 */
public class VisibilityEvent extends mil.emp3.api.events.VisibilityEvent {
    private final IMap oEventOnMap;
    private final IContainer parentContainer;

    public VisibilityEvent(VisibilityActionEnum eEvent, IContainer target, IContainer parent, IMap oOnMap) {
        super(eEvent, target);
        this.oEventOnMap = oOnMap;
        this.parentContainer = parent;
    }

    @Override
    public IContainer getParent() {
        return this.parentContainer;
    }

    @Override
    public IMap getMapEventOccurredOn() {
        return this.oEventOnMap;
    }
}
