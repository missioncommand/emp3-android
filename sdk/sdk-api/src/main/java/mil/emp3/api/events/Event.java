package mil.emp3.api.events;

import mil.emp3.api.interfaces.IEvent;
import mil.emp3.api.interfaces.IEventEnum;

/**
 * This class is the base class for all event classes.
 * layers of the EMP3 java map
 * @param <T> A enumerated class derived from {@link IEventEnum}.
 * @param <TargetType> Any java class.
 */
public class Event<T extends IEventEnum, TargetType extends Object> implements IEvent<T, TargetType> {

    private final T eEvent; // if we change bitmap to enum this will become a searchable list of long, Easier to implement but way less performant
    private final TargetType oTarget;
    private Object userObject = null;// allow user to track/filter own events

    protected Event(T eEvent, TargetType oTarget) {
        this.eEvent = eEvent;
        this.oTarget = oTarget;
    }

    /**
     *
     * @return returns the enumerated value of the event generated.
     */
    @Override
    public T getEvent() {
        return this.eEvent;
    }

    /**
     *
     * @return returns the events target container.
     */
    @Override
    public TargetType getTarget() {
        return this.oTarget;
    }

    @Override
    public void setObject(Object object) {
        userObject = object;
    }

    @Override
    public Object getObject() {
        return userObject;
    }
}

