package mil.emp3.api.interfaces.core.storage;

import java.util.Collection;
import java.util.Map;
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

    Map<UUID, IMapService> getMapServiceHash();

    // Called by StorageManager each time map service is added or removed. We will need this to set service on swapMapEngine
    void addMapService(IMapService mapService);

    boolean removeMapService(IMapService mapService);

    // used to restore KML Service on activity restart.
    void addKmlRequest(IKMLSRequest kmlRequest);
    void removeKmlRequest(IKMLSRequest kmlRequest);
    IKMLSRequest getKmlRequest(UUID id);
}
