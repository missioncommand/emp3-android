package mil.emp3.api.interfaces.core.storage;

import java.util.Map;
import java.util.UUID;

import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.interfaces.IContainer;

public interface IStorageObjectWrapper<T extends IContainer> {

    T getObject();

    UUID getGeoId();

    boolean childCreatesParodox(IStorageObjectWrapper newChild);

    boolean hasParents();
    boolean hasParent(java.util.UUID id);
    Map<UUID, IParentRelationship> getParentList();

    boolean hasChildren();
    boolean hasChild(java.util.UUID id);
    Map<UUID, IStorageObjectWrapper> getChildrenList();

    VisibilityStateEnum getVisibilityWithParentOnMap(UUID mapId, UUID parentId);
    void setVisibilityWithParentOnMap(UUID mapId, UUID parentId, VisibilityStateEnum visibilitystate);
    void setChildrenVisibilityOnMap(UUID mapId, VisibilityStateEnum visibilityEnum);

    VisibilityStateEnum getVisibilityOnMap(java.util.UUID mapId);

    void removeParent(IStorageObjectWrapper parentWrapper);
}
