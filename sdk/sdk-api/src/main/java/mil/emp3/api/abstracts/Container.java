package mil.emp3.api.abstracts;


import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoContainer;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.Overlay;
import mil.emp3.api.Point;
import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IKMLExportable;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IContainerEventListener;
import mil.emp3.api.listeners.IFeatureEditEventListener;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IVisibilityEventListener;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.api.utils.kml.EmpKMLExporter;

/**
 * Container class serves as a base class for the Map objects including Map, Overlay and Feature. It implements some basic capabilities
 * related to children, listeners and properties.
 *
 * A note on memory management: When an application passes a reference to a Container to the EMP via any of interface methods, the EMP Core doesn't
 * clone the object, it uses and stores the reference to the object in its internal structures. Applications need to be careful when making changes
 * to these objects. In many cases 'apply' methods are provided in the interface to allow the applications to control when a change is reflected
 * on the map.
 */
public class Container implements IContainer, IKMLExportable {

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
    public void clearContainer()
            throws EMP_Exception {
        storageManager.removeChildren(this);
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
        return (this.getProperties().containsKey(propertyName.toUpperCase()));
    }

    @Override
    public String getProperty(String propertyName) {
        if (!this.containsProperty(propertyName)) {
            return null;
        }

        return this.getProperties().get(propertyName.toUpperCase());
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        this.getProperties().put(propertyName.toUpperCase(), propertyValue);
    }

    @Override
    public void removeProperty(String propertyName) {
        if (this.containsProperty(propertyName.toUpperCase())) {
            this.getProperties().remove(propertyName.toUpperCase());
        }
    }

    @Override
    public boolean getBooleanProperty(String propertyName) {
        if (!this.containsProperty(propertyName)) {
            throw new InvalidParameterException("Property does not exists.");
        }

        String strValue = this.getProperty(propertyName);

        if ((null == strValue) || strValue.isEmpty() || !(strValue.equalsIgnoreCase("true") || strValue.equalsIgnoreCase("false"))) {
            throw new InvalidParameterException("Property value is not a boolean.");
        }

        return Boolean.parseBoolean(strValue);
    }

    @Override
    public int getIntegerProperty(String propertyName) {
        int retValue;

        if (!this.containsProperty(propertyName)) {
            throw new InvalidParameterException("Property does not exists.");
        }

        String strValue = this.getProperty(propertyName);

        if ((null == strValue) || strValue.isEmpty()) {
            throw new InvalidParameterException("Property value is not an integer.");
        }

        try {
            retValue = Integer.parseInt(strValue);
        } catch (NumberFormatException Ex) {
            throw new InvalidParameterException("Property value is not an integer.");
        }

        return retValue;
    }

    @Override
    public float getFloatProperty(String propertyName) {
        float retValue;

        if (!this.containsProperty(propertyName)) {
            throw new InvalidParameterException("Property does not exists.");
        }

        String strValue = this.getProperty(propertyName);

        if ((null == strValue) || strValue.isEmpty()) {
            throw new InvalidParameterException("Property value is not a float.");
        }

        try {
            retValue = Float.parseFloat(strValue);
        } catch (NumberFormatException Ex) {
            throw new InvalidParameterException("Property value is not an float.");
        }

        return retValue;
    }

    @Override
    public double getDoubleProperty(String propertyName) {
        double retValue;

        if (!this.containsProperty(propertyName)) {
            throw new InvalidParameterException("Property does not exists.");
        }

        String strValue = this.getProperty(propertyName);

        if ((null == strValue) || strValue.isEmpty()) {
            throw new InvalidParameterException("Property value is not a double.");
        }

        try {
            retValue = Double.parseDouble(strValue);
        } catch (NumberFormatException Ex) {
            throw new InvalidParameterException("Property value is not an double.");
        }

        return retValue;
    }

    protected String callKMLExporter() throws IOException {
        return EmpKMLExporter.export(this);
    }

    @Override
    public String exportToKML() throws IOException {
        return "";
    }

    @Override
    public void exportStylesToKML(XmlSerializer xmlSerializer) throws IOException {
        for (IGeoBase geoObject : this.getChildren()) {
            if (geoObject instanceof IKMLExportable) {
                ((IKMLExportable) geoObject).exportStylesToKML(xmlSerializer);
            }
        }
    }

    @Override
    public void exportEmpObjectToKML(XmlSerializer xmlSerializer) throws IOException {
        if (this.getChildren().size() > 0) {
            xmlSerializer.startTag(null, "Folder");

            if (this instanceof Feature) {
                // If we are exporting a feature, this folder contains the features children.
                // We set the targetId to the id of the parent feature.
                if ((null != this.getDataProviderId()) && !this.getDataProviderId().isEmpty()) {
                    xmlSerializer.attribute(null, "targetId", this.getDataProviderId());
                } else {
                    xmlSerializer.attribute(null, "targetId", this.getGeoId().toString());
                }

                xmlSerializer.startTag(null, "name");
                if ((null != this.getName()) && !this.getName().isEmpty()) {
                    xmlSerializer.text(this.getName() + " - Children");
                } else {
                    xmlSerializer.text("EMP feature Children");
                }
                xmlSerializer.endTag(null, "name");
            } else {
                if ((null != this.getDataProviderId()) && !this.getDataProviderId().isEmpty()) {
                    xmlSerializer.attribute(null, "id", this.getDataProviderId());
                } else {
                    xmlSerializer.attribute(null, "id", this.getGeoId().toString());
                }

                xmlSerializer.startTag(null, "name");
                if ((null != this.getName()) && !this.getName().isEmpty()) {
                    xmlSerializer.text(this.getName());
                } else {
                    xmlSerializer.text("EMP Untitled Overlay");
                }
                xmlSerializer.endTag(null, "name");

                if ((null != this.getDescription()) && !this.getDescription().isEmpty()) {
                    xmlSerializer.startTag(null, "description");
                    xmlSerializer.text(this.getDescription());
                    xmlSerializer.endTag(null, "description");
                }
            }

            for (IGeoBase geoObject : this.getChildren()) {
                if (geoObject instanceof IKMLExportable) {
                    ((IKMLExportable) geoObject).exportEmpObjectToKML(xmlSerializer);
                }
            }

            xmlSerializer.endTag(null, "Folder");
        }
    }
}
