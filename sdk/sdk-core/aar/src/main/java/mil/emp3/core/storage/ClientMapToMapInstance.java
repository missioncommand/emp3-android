/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mil.emp3.core.storage;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.storage.IClientMapToMapInstance;
import mil.emp3.core.events.MapInstanceEventHandler;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 *
 * This class maintains the relationship between a client map and a map instance. It also handles
 * the feature selection.
 */
public class ClientMapToMapInstance extends MapInstanceEventHandler implements IClientMapToMapInstance {

    private final IMap clientMap;
    private IMapInstance mapInstance;

    // This hash contains the features that are marked selected on the map keyed by feature id.
    private java.util.HashMap<java.util.UUID, IFeature> selectedFeatures = new java.util.HashMap<>();

    public ClientMapToMapInstance(IMap clientMap, IMapInstance mapInstance) {
        this.clientMap = clientMap;
        this.mapInstance = mapInstance;
        this.oMapCapabilities = this.mapInstance.getCapabilities();

        // On initialization MapInstance and core should refer to the same LookAt object else apply logic will not work
        //  before a set. Corresponding change in Emp3NavigationListener was also made to create an instance of LookAt on construction
        this.initializeLookAt(mapInstance.getLookAt());
    }

    /**
     * This is a selective copy. Camera and mapServices will be set by CoreManager when MAP_READY event
     * is received. It will invoke the API that will set those values.
     * @param from
     */
    @Override
    public void copy(IClientMapToMapInstance from) {
        super.copy(from);

        for(IFeature feature: from.getSelected()) {
            this.selectedFeatures.put(feature.getGeoId(), feature);
        }
    }

    @Override
    public IMap getClientMap() {
        return this.clientMap;
    }
    
    @Override
    public void setMapInstance(IMapInstance mapInstance) {
        this.mapInstance = mapInstance;
        this.oMapCapabilities = this.mapInstance.getCapabilities();
    }

    @Override
    public IMapInstance getMapInstance() {
        return this.mapInstance;
    }

    @Override
    public boolean selectFeature(IFeature feature) {
        if (!this.selectedFeatures.containsKey(feature.getGeoId())) {
            this.selectedFeatures.put(feature.getGeoId(), feature);
            return true;
        }

        return false;
    }

    @Override
    public boolean deselectFeature(java.util.UUID featureId) {
        if (this.selectedFeatures.containsKey(featureId)) {
            this.selectedFeatures.remove(featureId);
            return true;
        }
        return false;
    }

    @Override
    public List<IFeature> getSelected() {
        List<IFeature> list = new ArrayList<>();

        for (IFeature feature: this.selectedFeatures.values()) {
            list.add(feature);
        }

        return list;
    }

    @Override
    public void clearSelected() {
        this.selectedFeatures.clear();
    }

    @Override
    public boolean isSelected(IFeature feature) {
        return this.selectedFeatures.containsKey(feature.getGeoId());
    }

    @Override
    public int getMapViewWidth() {
        if(null != mapInstance) {
            return mapInstance.getViewWidth();
        } else {
            return super.getMapViewWidth();
        }
    }

    @Override
    public int getMapViewHeight() {
        if(null != mapInstance) {
            return mapInstance.getViewHeight();
        } else {
            return super.getMapViewHeight();
        }
    }
}
