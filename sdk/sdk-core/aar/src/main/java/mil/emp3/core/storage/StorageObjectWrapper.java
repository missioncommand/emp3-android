package mil.emp3.core.storage;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IContainerSet;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.interfaces.core.storage.IParentRelationship;
import mil.emp3.api.interfaces.core.storage.IStorageObjectWrapper;
import mil.emp3.api.utils.ContainerSet;
import mil.emp3.api.utils.UUIDSet;

/**
 *
 * This class is used by the storage manager to wrap all EMP object and their relationships.
 * @param <T> A Type that extends the Container class.
 */
public class StorageObjectWrapper<T extends IContainer> implements IStorageObjectWrapper<T> {
    private static String TAG = StorageObjectWrapper.class.getSimpleName();
    private T container;
    private final Map<UUID, IParentRelationship> parentList;
    private final Map<UUID, IStorageObjectWrapper> childrenList;
    
    protected StorageObjectWrapper(T oContainer) {
        this.container = oContainer;
        this.parentList = new HashMap<>();
        this.childrenList = new HashMap<>();
    }

    /**
     * This is used when we are restoring the ClientMap object after activity restart.
     * We definitely shouldn't touch the container. We definitely need to copy the childrenList.
     * Doesn't hurt to copy the parentList.
     * Newly create clientMap needs to be retrofitted with children of the old clientmap after activity
     * restart.
     * @param from
     */
    protected void copy(IStorageObjectWrapper from) {
        this.parentList.putAll(from.getParentList());
        this.childrenList.putAll(from.getChildrenList());
    }

    protected java.util.Set<java.util.UUID> getChildIdList() {
        return this.childrenList.keySet();
    }
    
    protected void setObject(T newContainer) {
        this.container = newContainer;
    }

    @Override
    public T getObject() {
        return this.container;
    }

    protected IContainerSet getParents() {
        IContainerSet oSet = new ContainerSet();
        
        for (IParentRelationship oParentRelationship: this.parentList.values()) {
            oSet.add(oParentRelationship.getParentWrapper().getObject());
        }
        return oSet;
    }
    
    protected int getChildrenCount() {
        return this.childrenList.size();
    }
    
    protected int getParentCount() {
        return this.parentList.size();
    }
    
    @Override
    public boolean hasParents() {
        return (this.parentList.size() > 0);
    }
    
    @Override
    public boolean hasChildren() {
        return (this.childrenList.size() > 0);
    }
    
    @Override
    public boolean hasParent(java.util.UUID id) {
        return this.parentList.containsKey(id);
    }

    @Override
    public void setChildrenVisibilityOnMap(java.util.UUID mapId, VisibilityStateEnum visibilityEnum) {
        java.util.UUID parentId = this.container.getGeoId();
        
        for (IStorageObjectWrapper oWrapper: this.childrenList.values()) {
            if (oWrapper.getVisibilityWithParentOnMap(mapId, parentId) == null) {
                oWrapper.setVisibilityWithParentOnMap(mapId, parentId, visibilityEnum);
                oWrapper.setChildrenVisibilityOnMap(mapId, visibilityEnum);
            }
        }
    }
    
    private void addParent(StorageObjectWrapper parent, VisibilityStateEnum visibilityEnum)
            throws EMP_Exception {

        if (!this.hasParent(parent.getObject().getGeoId())) {
            VisibilityStateEnum visibilityStateEnum;
            IUUIDSet parentMapIdList = parent.getMapList();
            IUUIDSet MapIdList = this.getMapList();
            ParentRelationship oParentRelationship = new ParentRelationship(parent);

            this.parentList.put(parent.getObject().getGeoId(), oParentRelationship);
            
            for (java.util.UUID uuId: parentMapIdList) {
                    visibilityStateEnum = VisibilityStateEnum.HIDDEN;
                    switch (visibilityEnum) {
                        case VISIBLE:
                            switch (parent.getVisibilityOnMap(uuId)) {
                                case VISIBLE:
                                    visibilityStateEnum = VisibilityStateEnum.VISIBLE;
                                    break;
                                case HIDDEN:
                                    visibilityStateEnum = VisibilityStateEnum.VISIBLE_ANCESTOR_HIDDEN;
                                    break;
                                case VISIBLE_ANCESTOR_HIDDEN:
                                    visibilityStateEnum = VisibilityStateEnum.VISIBLE_ANCESTOR_HIDDEN;
                                    break;
                            }
                            break;
                        case VISIBLE_ANCESTOR_HIDDEN:
                            visibilityStateEnum = VisibilityStateEnum.HIDDEN;
                            break;
                        case HIDDEN:
                            visibilityStateEnum = VisibilityStateEnum.HIDDEN;
                            break;
                    }
                    oParentRelationship.setVisibilityOnMap(uuId, visibilityStateEnum);
                    this.setChildrenVisibilityOnMap(uuId, visibilityEnum);
            }
        }
    }

    @Override
    public boolean hasChild(java.util.UUID id) {
        return this.childrenList.containsKey(id);
    }

    protected void addChild(StorageObjectWrapper newChild, VisibilityStateEnum visibilityEnum)
            throws EMP_Exception {
        if (!this.hasChild(newChild.getObject().getGeoId())) {
            if (this.childCreatesParadox(newChild)) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_CHILD, "The object can not be its own ancestor.");
            } else {
                this.childrenList.put(newChild.getObject().getGeoId(), newChild);
                newChild.addParent(this, visibilityEnum);
            }
        }
    }
    
    @Override
    public boolean childCreatesParadox(IStorageObjectWrapper newChild) {
        // Make sure its not trying to add an object as its own parent nor ancestor.
        if (this.container.getGeoId().compareTo(newChild.getObject().getGeoId()) != 0) {
            for (IParentRelationship oParentRelationship: this.parentList.values()) {
                if (!(oParentRelationship.getParentWrapper().getObject() instanceof IMap)) {
                    if (oParentRelationship.getParentWrapper().childCreatesParadox(newChild)) {
                        return true;
                    }
                }
            }
            
            return false;
        }
        return true;
    }

    @Override
    public void setVisibilityWithParentOnMap(java.util.UUID mapId, java.util.UUID parentId, VisibilityStateEnum visibilitystate) {
        IParentRelationship parentRelationship = this.parentList.get(parentId);
        
        if (parentRelationship != null) {
            parentRelationship.setVisibilityOnMap(mapId, visibilitystate);
        }
    }

    @Override
    public VisibilityStateEnum getVisibilityWithParentOnMap(java.util.UUID mapId, java.util.UUID parentId) {
        if (this.container instanceof IMap) {
            if (this.container.getGeoId().compareTo(mapId) == 0) {
                return VisibilityStateEnum.VISIBLE;
            }
        } else {
            IParentRelationship parentRelationship = this.parentList.get(parentId);
        
            if (parentRelationship != null) {
                return parentRelationship.getVisibilityOnMap(mapId);
            }
        }
        
        return null;
    }

    @Override
    public VisibilityStateEnum getVisibilityOnMap(java.util.UUID mapId) {
        if (this.container instanceof IMap) {
            if (this.container.getGeoId().compareTo(mapId) == 0) {
                return VisibilityStateEnum.VISIBLE;
            }
        } else for (IParentRelationship parentRelationship: this.parentList.values()) {
            if (parentRelationship.getVisibilityOnMap(mapId) == VisibilityStateEnum.VISIBLE) {
                return VisibilityStateEnum.VISIBLE;
            }
        }
        
        return VisibilityStateEnum.HIDDEN;
    }
    
    protected IUUIDSet getMapList() {
        IUUIDSet oList = new UUIDSet();
        
        if (this.container instanceof IMap) {
            oList.add(this.container.getGeoId());
        } else for (IParentRelationship parentRelationship: this.parentList.values()) {
            parentRelationship.getMapList(oList);
        }
        return oList;
    }
    
    @Override
    public java.util.UUID getGeoId() {
        return this.container.getGeoId();
    }
    
    protected void removeChild(java.util.UUID childId) {
        if (this.childrenList.containsKey(childId)) {
            IStorageObjectWrapper childWrapper = this.childrenList.get(childId);
            
            this.childrenList.remove(childId);
            childWrapper.removeParent(this);
        }
    }

    @Override
    public void removeParent(IStorageObjectWrapper parentWrapper) {
        if (this.parentList.containsKey(parentWrapper.getGeoId())) {
            this.parentList.remove(parentWrapper.getGeoId());
        }
    }
    
    protected boolean isOnMap(java.util.UUID mapId) {
        boolean bRet = false;
        
        for (IParentRelationship parentRelationship: this.parentList.values()) {
            bRet = parentRelationship.isOnMap(mapId);
            if (bRet) {
                Log.d(TAG, "isOnMap parentRelationship " + parentRelationship.getParentWrapper().getGeoId());
                break;
            }
        }
        return bRet;
    }

    protected void clearMapVisibility(StorageObjectWrapper parentWrapper, UUID mapId) {
        Log.d(TAG, "clearMapVisibility " + parentWrapper.getGeoId());
        for (IParentRelationship parentRelationship: this.parentList.values()) {
            if(parentWrapper.getGeoId().equals(parentRelationship.getParentWrapper().getGeoId())) {
                parentRelationship.resetVisibilityOnMap(mapId);
            }
        }
    }

    @Override
    public Map<UUID, IStorageObjectWrapper> getChildrenList() {
        return this.childrenList;
    }

    @Override
    public Map<UUID, IParentRelationship> getParentList() {
        return this.parentList;
    }
}
