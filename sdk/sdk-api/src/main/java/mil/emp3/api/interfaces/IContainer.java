package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoContainer;

import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IContainerEventListener;
import mil.emp3.api.listeners.IFeatureEditEventListener;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IVisibilityEventListener;

/**
 * Container class serves as a base class for the Map objects including Map, Overlay and Feature. It implements some basic capabilities
 * related to children, listeners and properties.
 *
 * A note on memory management: When an application passes a reference to a Container to the EMP via any of interface methods, the EMP Core doesn't
 * clone the object, it uses and stores the reference to the object in its internal structures. Applications need to be careful when making changes
 * to these objects. In many cases 'apply' methods are provided in the interface to allow the applications to control when a change is reflected
 * on the map.
 */
public interface IContainer extends IGeoContainer {
    /**
     * Returns true if Container has children.
     * @return True if it has one or more children, false otherwise.
     */
    boolean hasChildren();
    
    /**
     * This method returns a list of parents of this container.
     * @return java.util.List of zero or more IContainers
     */
    List<IContainer> getParents();

    /**
     * This method removes all children from this container.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void clearContainer()
            throws EMP_Exception;

    /**
     * Adds a Container event listener, which will be invoked by the EMP Core when changes are made to the container,
     * {@link mil.emp3.api.events.CameraEvent} and {@link mil.emp3.api.enums.ContainerEventEnum}
     * @param listener The object that implements the proper listener for the container event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     */
    EventListenerHandle addContainerEventListener(IContainerEventListener listener) throws EMP_Exception;

    /**
     * Adds a Visibility Event listener which will be invoked by the EMP Core when changes to visibility attribute of the Container are made,
     * {@link mil.emp3.api.events.VisibilityEvent} and {@link mil.emp3.api.enums.VisibilityStateEnum}
     * @param listener The object that implements the proper listener for the visibility event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     */
    EventListenerHandle addVisibilityEventListener(IVisibilityEventListener listener) throws EMP_Exception;

    /**
     * Adds a listener which will be invoked by the EMP Core when a Feature on the Map is selected or de-selected using the IMap methods,
     * {@link mil.emp3.api.events.FeatureEvent} and {@link mil.emp3.api.enums.FeatureEventEnum}
     * @param listener The object that implements the proper listener for the feature event.
     * @return EventListenerHandle A handle to the registration which is needed to unregister the listener.
     */
    EventListenerHandle addFeatureEventListener(IFeatureEventListener listener) throws EMP_Exception;

    /**
     * Adds a listener which will be invoked by the EMP Core when user interacts with a Feature icon on the screen using various supported
     * gestures like tap, double tap, long press etc, {@link mil.emp3.api.events.FeatureUserInteractionEvent}
     * and {@link mil.emp3.api.enums.UserInteractionEventEnum}
     * @param listener The object that implements the proper listener for the feature user interaction event.
     * @return A handle to the registration which is needed to unregister the listener. See {@link EventListenerHandle}
     */
    EventListenerHandle addFeatureInteractionEventListener(IFeatureInteractionEventListener listener) throws EMP_Exception;

    /**
     * Adds a listener which will be invoked by the EMP Core as state of the feature in edit mode is changed as a result user actions
     * on the screen, {@link mil.emp3.api.events.FeatureEditEvent} and {@link mil.emp3.api.enums.FeatureEditEventEnum}
     * @param listener The object that implements the proper listener for the feature edit events.
     * @return A handle to the registration which is needed to unregister the listener. See {@link EventListenerHandle}
     */
    EventListenerHandle addFeatureEditEventListener(IFeatureEditEventListener listener) throws EMP_Exception;

    /**
     * This method removes the listener registration identified by the handle specified. The handle
     * returned by the registration must be used to remove the registration to stop receiving events.
     * all listeners must be removed before discarding the container.
     * @param hListener The handle to the registration to remove.
     */
    void removeEventListener(EventListenerHandle hListener);

    /**
     * If specified property is already set then returns true else returns false.
     * @param propertyName The name of the property. The property name is case insensitive.
     * @return Returns true if the list contains an entry for the indicated property, false if not.
     */
    boolean containsProperty(String propertyName);

    /**
     * This method retrieves the property value from the property list.
     * @param propertyName The name of the property. The property name is case insensitive.
     * @return The property value of null if the property is not on the property list.
     */
    String getProperty(String propertyName);

    /**
     * This method adds or replaces a property value of the specified property name.
     * @param propertyName The name of the property. The property name is case insensitive.
     * @param propertyValue The property value to set.
     */
    void setProperty(String propertyName, String propertyValue);

    /**
     * This method removes a property value from the property list.
     * @param propertyName The name of the property. The property name is case insensitive.
     */
    void removeProperty(String propertyName);

    /**
     * This method retrieves a boolean property value from the property list.
     * @param propertyName The name of the property. The property name is case insensitive.
     * @return The boolean property value. An exception is raised if the property does not exist or if its not a boolean value.
     */
    boolean getBooleanProperty(String propertyName);

    /**
     * This method retrieves an integer property value from the property list.
     * @param propertyName The name of the property. The property name is case insensitive.
     * @return The integer property value. An exception is raised if the property does not exist or if its not a integer value.
     */
    int getIntegerProperty(String propertyName);

    /**
     * This method retrieves a float property value from the property list.
     * @param propertyName The name of the property. The property name is case insensitive.
     * @return The float property value. An exception is raised if the property does not exist or if its not a float value.
     */
    float getFloatProperty(String propertyName);

    /**
     * This method retrieves a double property value from the property list.
     * @param propertyName The name of the property. The property name is case insensitive.
     * @return The double property value. An exception is raised if the property does not exist or if its not a double value.
     */
    double getDoubleProperty(String propertyName);
}
