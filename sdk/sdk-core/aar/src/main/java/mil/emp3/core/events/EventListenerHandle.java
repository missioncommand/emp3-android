package mil.emp3.core.events;

import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.listeners.IEventListener;
import org.cmapi.primitives.IGeoBase;

/**
 *
 */
public class EventListenerHandle extends mil.emp3.api.listeners.EventListenerHandle {
    private final EventListenerTypeEnum eEventListenerType;
    private final IEventListener listener;
    private final IGeoBase oObjectRegisteredOn;

    public EventListenerHandle(EventListenerTypeEnum eType, IGeoBase oRegistrationObject, IEventListener listener) {
        this.eEventListenerType = eType;
        this.listener = listener;
        this.oObjectRegisteredOn = oRegistrationObject;
    }

    @Override
    public EventListenerTypeEnum getListenerType() {
        return this.eEventListenerType;
    }

    @Override
    public IEventListener getListener() {
        return listener;
    }

    public IGeoBase getRegistrationObject() {
        return this.oObjectRegisteredOn;
    }
}
