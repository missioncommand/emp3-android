package mil.emp3.api.abstracts;


import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoContainer;

import java.util.HashMap;
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
 * This abstract class implements the container functionality
 */
public class Container implements IContainer {

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final private IEventManager eventManager = ManagerFactory.getInstance().getEventManager();
    final protected IGeoContainer geoContainer;

    protected Container(IGeoContainer geoContainer) {
        this.geoContainer = geoContainer;
    }

    @Override
    public void setReadOnly(boolean var1) {
        this.geoContainer.setReadOnly(var1);
    }

    @Override
    public boolean getReadOnly() {
        return this.geoContainer.getReadOnly();
    }

    @Override
    public java.util.List<IContainer> getParents() {
        return storageManager.getParents(this);
    }

    @Override
    public void clearContainer()
            throws EMP_Exception {
        storageManager.removeChildren(this);
    }

    @Override
    public boolean hasChildren() {
        java.util.List<IGeoBase> oChildren = getChildren();
        return !((oChildren == null) || (oChildren.size() == 0));
    }

    @Override
    public java.util.List<IGeoBase> getChildren() {
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

    @Override
    public void setName(String s) {
        this.geoContainer.setName(s);
    }

    @Override
    public String getName() {
        return this.geoContainer.getName();
    }

    @Override
    public void setGeoId(java.util.UUID uuid) {
        this.geoContainer.setGeoId(uuid);
    }

    @Override
    public UUID getGeoId() {
        return this.geoContainer.getGeoId();
    }

    @Override
    public void setDataProviderId(String s) {
        this.geoContainer.setDataProviderId(s);
    }

    @Override
    public String getDataProviderId() {
        return this.geoContainer.getDataProviderId();
    }

    @Override
    public void setDescription(String s) {
        this.geoContainer.setDescription(s);
    }

    @Override
    public String getDescription() {
        return this.geoContainer.getDescription();
    }

    @Override
    public HashMap<String, Object> getProperties() {
        return this.geoContainer.getProperties();
    }
}
