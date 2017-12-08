package mil.emp3.api.abstracts;


import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoContainer;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IContainerEventListener;
import mil.emp3.api.listeners.IFeatureEditEventListener;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IVisibilityEventListener;
import mil.emp3.api.utils.ManagerFactory;

/**
 * Container class serves as a base class for the Map objects including Map, Overlay and Feature. It implements some basic capabilities
 * related to children, listeners and properties.
 *
 * A note on memory management: When an application passes a reference to a Container to the EMP via any of interface methods, the EMP Core doesn't
 * clone the object, it uses and stores the reference to the object in its internal structures. Applications need to be careful when making changes
 * to these objects. In many cases 'apply' methods are provided in the interface to allow the applications to control when a change is reflected
 * on the map.
 */
public class Container implements IContainer{

    final protected IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final protected IEventManager eventManager = ManagerFactory.getInstance().getEventManager();
    final private IGeoContainer geoContainer;

    protected IGeoContainer getGeoContainer() { return geoContainer; }

    protected Container(IGeoContainer geoContainer) {
        this.geoContainer = geoContainer;
    }

    /**
     * Set or reset the flag that indicates if Container should be treated as read only.
     * Application or EMP API may choose to mark this Container as read only. There is no mechanism for enforcing this read only restriction,
     * it is just a hint.
     * @param var1
     */
    @Override
    public void setReadOnly(boolean var1) {
        this.geoContainer.setReadOnly(var1);
    }

    /**
     * Returns current value of read/write flag for the container.
     * @return
     */
    @Override
    public boolean getReadOnly() {
        return this.geoContainer.getReadOnly();
    }

    @Override
    public List<IContainer> getParents() {
        return storageManager.getParents(this);
    }

    @Override
    public void clearContainer(Object userContext)
            throws EMP_Exception {
        storageManager.removeChildren(this, userContext);
    }

    @Override
    public void clearContainer()
            throws EMP_Exception {
        storageManager.removeChildren(this, null);
    }

    @Override
    public boolean hasChildren() {
        List<IGeoBase> oChildren = getChildren();
        return !((oChildren == null) || (oChildren.size() == 0));
    }

    /**
     * Returns immediate children of the Container.
     * @return
     */
    @Override
    public List<IGeoBase> getChildren() {
        return storageManager.getImmediateChildren(this);
    }

    @Override
    public EventListenerHandle addContainerEventListener(IContainerEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addVisibilityEventListener(IVisibilityEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addFeatureEventListener(IFeatureEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.FEATURE_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addFeatureInteractionEventListener(IFeatureInteractionEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.FEATURE_INTERACTION_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addFeatureEditEventListener(IFeatureEditEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.FEATURE_EDIT_EVENT_LISTENER, this, listener);
    }

    @Override
    public void removeEventListener(EventListenerHandle hListener) {
        eventManager.removeEventHandler(hListener);
    }

    /**
     * Each Container has user specified name. There is no check in the EMP Core for duplicate names or any sort of validation.
     *
     * For icon based MIL-STD-2525 point features the name will display if populated.  If the uniqueDesignation modifier is also populated
     * and labels are turned on both will display with the name to the right of the uniqueDesignation.
     * This is based on behavior we had to be compliant with legacy products.
     * @param s
     */
    @Override
    public void setName(String s) {
        this.geoContainer.setName(s);
    }

    /**
     * Returns previously assigned name of the container.
     * @return
     */
    @Override
    public String getName() {
        return this.geoContainer.getName();
    }

    /**
     * Sets GeoId of the Container. This will be removed once a constructor is created in GeoBase class. EMP-2995
     * @param uuid
     */
    @Override
    public void setGeoId(java.util.UUID uuid) {
        this.geoContainer.setGeoId(uuid);
        // throw new IllegalStateException("Application is not allowed to change the GeoId");
    }

    /**
     * Returns GeoId of a container. GeoId is cloned before handing it off to the application.
     * @return
     */
    @Override
    public UUID getGeoId() {
        UUID geoId = new UUID(this.geoContainer.getGeoId().getMostSignificantBits(), this.geoContainer.getGeoId().getLeastSignificantBits());
        return geoId;
    }

    /**
     * Application can set the data provider Id for its own use. This attribute is not processed by EMP
     * @param s
     */
    @Override
    public void setDataProviderId(String s) {
        this.geoContainer.setDataProviderId(s);
    }

    /**
     * Get the data provider id.
     * @return
     */
    @Override
    public String getDataProviderId() {
        return this.geoContainer.getDataProviderId();
    }

    /**
     * Sets the description of the Container.
     * Description is typically used for things like the pop up window that may display when a feature is clicked on by user.
     * This behavior is completely up to app developer though.  Description may contain numerous lines of text.
     * @param s
     */
    @Override
    public void setDescription(String s) {
        this.geoContainer.setDescription(s);
    }

    /**
     * Gets the description of the Container.
     * @return
     */
    @Override
    public String getDescription() {
        return this.geoContainer.getDescription();
    }

    /**
     * Returns container properties.
     * @return
     */
    @Override
    public HashMap<String, String> getProperties() {
        return this.geoContainer.getProperties();
    }

    @Override
    public boolean containsProperty(String propertyName) {
        return (this.getProperties().containsKey(propertyName));
    }

    @Override
    public String getProperty(String propertyName) {
        if (!this.containsProperty(propertyName)) {
            return null;
        }

        return this.getProperties().get(propertyName);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        this.getProperties().put(propertyName, propertyValue);
    }

    @Override
    public void removeProperty(String propertyName) {
        this.getProperties().remove(propertyName);
    }

    @Override
    public boolean getBooleanProperty(String propertyName) {
        if (!this.containsProperty(propertyName)) {
            throw new IllegalArgumentException("Property does not exist.");
        }

        String strValue = this.getProperty(propertyName);

        if ((null == strValue) || strValue.isEmpty() || !(strValue.equalsIgnoreCase("true") || strValue.equalsIgnoreCase("false"))) {
            throw new IllegalArgumentException("Property value is not a boolean.");
        }

        return Boolean.parseBoolean(strValue);
    }

    @Override
    public int getIntegerProperty(String propertyName) {
        int retValue;

        if (!this.containsProperty(propertyName)) {
            throw new IllegalArgumentException("Property does not exist.");
        }

        String strValue = this.getProperty(propertyName);

        if ((null == strValue) || strValue.isEmpty()) {
            throw new IllegalArgumentException("Property value is not an integer.");
        }

        try {
            retValue = Integer.parseInt(strValue);
        } catch (NumberFormatException Ex) {
            throw new IllegalArgumentException("Property value is not an integer.");
        }

        return retValue;
    }

    @Override
    public float getFloatProperty(String propertyName) {
        float retValue;

        if (!this.containsProperty(propertyName)) {
            throw new IllegalArgumentException("Property does not exist.");
        }

        String strValue = this.getProperty(propertyName);

        if ((null == strValue) || strValue.isEmpty()) {
            throw new IllegalArgumentException("Property value is not a float.");
        }

        try {
            retValue = Float.parseFloat(strValue);
        } catch (NumberFormatException Ex) {
            throw new IllegalArgumentException("Property value is not an float.");
        }

        return retValue;
    }

    @Override
    public double getDoubleProperty(String propertyName) {
        double retValue;

        if (!this.containsProperty(propertyName)) {
            throw new IllegalArgumentException("Property does not exist.");
        }

        String strValue = this.getProperty(propertyName);

        if ((null == strValue) || strValue.isEmpty()) {
            throw new IllegalArgumentException("Property value is not a double.");
        }

        try {
            retValue = Double.parseDouble(strValue);
        } catch (NumberFormatException Ex) {
            throw new IllegalArgumentException("Property value is not an double.");
        }

        return retValue;
    }
}
