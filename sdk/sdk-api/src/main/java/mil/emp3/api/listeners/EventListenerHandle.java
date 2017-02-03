package mil.emp3.api.listeners;

import mil.emp3.api.enums.EventListenerTypeEnum;

/**
 * This abstract class is the handle return by the add event listener method. It
 * must be used to remove the listeners from the system.
 */
public abstract class EventListenerHandle {
    /**
     * This method return the type of listener the handle refers to.
     * @return {@link EventListenerTypeEnum} The type of event listener.
     */
    public abstract EventListenerTypeEnum getListenerType();

    /**
     * This method return the event listener object the handle refers to.
     * @return {@link IEventListener} An event listener derived from IEventListener.
     */
    public abstract IEventListener getListener();
}
