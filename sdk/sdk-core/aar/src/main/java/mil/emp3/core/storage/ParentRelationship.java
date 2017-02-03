package mil.emp3.core.storage;

import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.interfaces.core.storage.IParentRelationship;
import mil.emp3.api.interfaces.core.storage.IStorageObjectWrapper;

/**
 *
 * 
 */
public class ParentRelationship implements IParentRelationship {
    private final IStorageObjectWrapper parentWrapper;
    private final java.util.HashMap<java.util.UUID, VisibilityStateEnum> visibilityOnMap = new java.util.HashMap<>();

    public ParentRelationship(IStorageObjectWrapper wrapper) {
        this.parentWrapper = wrapper;
    }

    @Override
    public IStorageObjectWrapper getParentWrapper() {
        return this.parentWrapper;
    }

    @Override
    public VisibilityStateEnum getVisibilityOnMap(java.util.UUID mapId) {
        if (this.visibilityOnMap.containsKey(mapId)) {
            return this.visibilityOnMap.get(mapId);
        }
        return null;
    }
    
    @Override
    public void setVisibilityOnMap(java.util.UUID mapId, VisibilityStateEnum stateEnum) {
        this.visibilityOnMap.put(mapId, stateEnum);
    }

    @Override
    public void resetVisibilityOnMap(java.util.UUID mapId) {
        this.visibilityOnMap.remove(mapId);
    }

    @Override
    public void getMapList(IUUIDSet oList) {
        oList.addAll(this.visibilityOnMap.keySet());
    }
    
    @Override
    public boolean isOnMap(java.util.UUID mapId) {
        return this.visibilityOnMap.containsKey(mapId);
    }
}
