package mil.emp3.api.interfaces.core.storage;

import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.interfaces.IUUIDSet;

/*
 * This is an internal interface class.  The app developer must not implement this interface.
 */
public interface IParentRelationship {
    IStorageObjectWrapper getParentWrapper();

    VisibilityStateEnum getVisibilityOnMap(java.util.UUID mapId);

    void setVisibilityOnMap(java.util.UUID mapId, VisibilityStateEnum stateEnum);

    void resetVisibilityOnMap(java.util.UUID mapId);

    void getMapList(IUUIDSet oList);

    boolean isOnMap(java.util.UUID mapId);
}
