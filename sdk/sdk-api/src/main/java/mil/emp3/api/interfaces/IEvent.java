package mil.emp3.api.interfaces;

/**
 * This is a generic event structure that is presented to the
 * application developer in the asynchronous event callback.
 *
 * It handles any mapping event in a consistent manner.
 *
 * These interfaces are designed to only allow specific map event
 * types for the object they are applied. For instance, onEvent()
 * will provide the mapping event on the form of a CameraEvent
 * getEvents() when the event listener was added via addCameraListener().
 * @param <T> An enumeration class that implements IEventEnum.
 * @param <TargetType> Any java class.
 */
public interface IEvent<T extends IEventEnum, TargetType extends Object> {

    /**
     * This method retrieves the enumeration value that identifies the type of event.
     * @return An enumerated value from a class derived from {@link IEventEnum}
     */
    public T getEvent();
    
    /**
     * This method return the object that the event occurred on.
     * @return An object derived from {@link org.cmapi.primitives.IGeoBase}
     */
    public TargetType getTarget();
}
