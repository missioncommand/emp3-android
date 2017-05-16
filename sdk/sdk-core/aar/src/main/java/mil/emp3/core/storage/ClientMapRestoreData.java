package mil.emp3.core.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import mil.emp3.api.LookAt;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.core.storage.IClientMapRestoreData;
import mil.emp3.api.interfaces.core.storage.IKMLSRequest;
import mil.emp3.api.interfaces.core.storage.IStorageObjectWrapper;
import mil.emp3.core.services.kml.KMLSRequest;

/**
 * This class stores client map data that will be required to restore the map state after activity restart.
 * Please check Emp3DataManager for further details on activity restart.
 */
public class ClientMapRestoreData implements IClientMapRestoreData {
    private String name;                           // map name, user is require to set name on map for restore capability
    private UUID uuid;                             // uuid of the clientMap when it is first created (name - uuid) pairing
    private String engineApkName;                  // map engine used
    private String engineClassName;
    private IStorageObjectWrapper objectWrapper;    // Helps restore children
    private ICamera camera;                         // We will restore camera as well
    private ILookAt lookAt;                         // restore lookAt
    final private Map<UUID, IMapService> mapServiceHash = new HashMap<>();;
    final private Map<UUID, IKMLSRequest> kmlsRequestMap = new HashMap<>();

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public UUID getUuid() {
        return uuid;
    }

    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getEngineApkName() {
        return engineApkName;
    }

    @Override
    public void setEngineApkName(String engineApkName) {
        this.engineApkName = engineApkName;
    }

    @Override
    public String getEngineClassName() {
        return engineClassName;
    }

    @Override
    public void setEngineClassName(String engineClassName) {
        this.engineClassName = engineClassName;
    }

    @Override
    public IStorageObjectWrapper getObjectWrapper() {
        return objectWrapper;
    }

    @Override
    public void setObjectWrapper(IStorageObjectWrapper objectWrapper) {
        this.objectWrapper = objectWrapper;
    }

    @Override
    public ICamera getCamera() {
        return camera;
    }

    @Override
    public void setCamera(ICamera camera) {
        this.camera = camera;
    }

    @Override
    public void setLookAt(ILookAt lookAt) {
        this.lookAt = lookAt;
    }

    @Override
    public ILookAt getLookAt() {
        return this.lookAt;
    }

    @Override
    public Map<UUID, IMapService> getMapServiceHash() {
        return mapServiceHash;
    }

    // Call by StorageManager each time map service is added or removed. We will need this to set service on swapMapEngine
    @Override
    public void addMapService(IMapService mapService) {
        this.mapServiceHash.put(mapService.getGeoId(), mapService);
    }

    @Override
    public boolean removeMapService(IMapService mapService) {
        if(null == this.mapServiceHash) {
            return false;
        }
        if (this.mapServiceHash.containsKey(mapService.getGeoId())) {
            this.mapServiceHash.remove(mapService.getGeoId());
            return true;
        }

        return false;
    }

    @Override
    public void addKmlRequest(IKMLSRequest kmlRequest) {
        kmlsRequestMap.put(kmlRequest.getId(), kmlRequest);
    }

    @Override
    public void removeKmlRequest(IKMLSRequest kmlRequest) {
        kmlsRequestMap.remove(kmlRequest.getId());
    }

    @Override
    public IKMLSRequest getKmlRequest(UUID id) {
        return kmlsRequestMap.get(id);
    }
}
