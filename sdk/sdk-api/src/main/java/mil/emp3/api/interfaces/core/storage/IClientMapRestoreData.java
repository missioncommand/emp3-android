package mil.emp3.api.interfaces.core.storage;

import java.util.HashMap;
import java.util.UUID;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;

/*
 * This is an internal interface class.  The app developer must not implement this interface.
 */
public interface IClientMapRestoreData {
    String getName();

    void setName(String name);

    UUID getUuid();

    void setUuid(UUID uuid);

    String getEngineApkName();

    void setEngineApkName(String engineApkName);

    String getEngineClassName();

    void setEngineClassName(String engineClassName);

    IStorageObjectWrapper getObjectWrapper();

    void setObjectWrapper(IStorageObjectWrapper objectWrapper);

    ICamera getCamera();

    void setCamera(ICamera camera);

    void setLookAt(ILookAt lookAt);

    ILookAt getLookAt();

    HashMap<UUID, IMapService> getMapServiceHash();

    void setMapServiceHash(HashMap<UUID, IMapService> mapServiceHash);

    // Call by StorageManager each time map service is added or removed. We will need this to set service on swapMapEngine
    void addMapService(IMapService mapService);

    boolean removeMapService(IMapService mapService);
}
