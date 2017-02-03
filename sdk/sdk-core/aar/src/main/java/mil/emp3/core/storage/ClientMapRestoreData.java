package mil.emp3.core.storage;

import java.util.HashMap;
import java.util.UUID;

import mil.emp3.api.LookAt;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.core.storage.IClientMapRestoreData;
import mil.emp3.api.interfaces.core.storage.IStorageObjectWrapper;

/**
 * This class stores client map data that will be required to restore the map state after activity restart.
 * Please check Emp3DataManager for further details on activity restart.
 */
public class ClientMapRestoreData implements IClientMapRestoreData {
    String name;                           // map name, user is require to set name on map for restore capability
    UUID uuid;                             // uuid of the clientMap when it is first created (name - uuid) pairing
    String engineApkName;                  // map engine used
    String engineClassName;
    IStorageObjectWrapper objectWrapper;    // Helps restore children
    ICamera camera;                         // We will restore camera as well
    ILookAt lookAt;                         // restore lookAt
    java.util.HashMap<java.util.UUID, IMapService> mapServiceHash;

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
    public HashMap<UUID, IMapService> getMapServiceHash() {
        return mapServiceHash;
    }

    @Override
    public void setMapServiceHash(HashMap<UUID, IMapService> mapServiceHash) {
        this.mapServiceHash = mapServiceHash;
    }

    // Call by StorageManager each time map service is added or removed. We will need this to set service on swapMapEngine
    @Override
    public void addMapService(IMapService mapService) {
        if(null == this.mapServiceHash) {
            this.mapServiceHash = new HashMap<>();
        }
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
}
