package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoContainer;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IContainerEventListener;
import mil.emp3.api.listeners.IFeatureEditEventListener;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IVisibilityEventListener;

/**
 * This interface defines the method for all containers.
 */
public interface IContainer extends IGeoContainer {
    /**
     * This method check if the container has children.
     * @return True if it has one or more children, false otherwise.
     */
    public boolean hasChildren();
    
    /**
     * This method returns a list of parents of this container.
     * @return java.util.List of zero or more IContainers
     */
    public java.util.List<IContainer> getParents();

    /**
     * This method removes all children from this container.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void clearContainer()
            throws EMP_Exception;

    /**
     * This method registers a listener for all container events.
     * The listener is called when any container event occurs on this container.
     * @param listener The object that implements the proper listener for the container event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     */
    public EventListenerHandle addContainerEventListener(IContainerEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for all visibility events.
     * The listener is called when the visibility changes for any object in the container.
     * @param listener The object that implements the proper listener for the visibility event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     */
    public EventListenerHandle addVisibilityEventListener(IVisibilityEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for all feature events.
     * The listener is called when any feature event occurs on this map.
     * @param listener The object that implements the proper listener for the feature event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     */
    public EventListenerHandle addFeatureEventListener(IFeatureEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for all user interaction events on all features in the container.
     * The listener is called when any user interaction event occurs on any feature in this container.
     * @param listener The object that implements the proper listener for the feature user interaction event.
     * @return A handle to the registration which is needed to unregister the listener. See {@link EventListenerHandle}
     */
    public EventListenerHandle addFeatureInteractionEventListener(IFeatureInteractionEventListener listener) throws EMP_Exception;

    /**
     * This method registers a listener for all feature edit events for all features in the container.
     * @param listener The object that implements the proper listener for the feature edit events.
     * @return A handle to the registration which is needed to unregister the listener. See {@link EventListenerHandle}
     */
    public EventListenerHandle addFeatureEditEventListener(IFeatureEditEventListener listener) throws EMP_Exception;

    /**
     * This method removes the listener registration identified by the handle specified. The handle
     * returned by the registration must be used to remove the registration to stop receiving events.
     * all listeners must be removed before discarding the container.
     * @param hListener The handle to the registration to remove.
     */
    public void removeEventListener(EventListenerHandle hListener);
}
