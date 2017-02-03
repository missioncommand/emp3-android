package mil.emp3.arcgis;
import java.util.UUID;

import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.arcgis.util.FeatureGraphicMapper;

/**
 * Created by deepakkarmarkar on 5/13/2016.
 */
public class GraphicsManager {

    private static GraphicsManager instance;
    private GraphicsManager() { }
    public static GraphicsManager getInstance() {
        if(null == instance) {
            synchronized(GraphicsManager.class) {
                if(null == instance) {
                    instance = new GraphicsManager();
                }
            }
        }
        return instance;
    }
    /**
     * This is inefficient but for this version of ArcGIS Individual update is not working.
     */
    public void updateGraphicsLayer(java.util.HashMap<java.util.UUID, FeatureGraphicMapper> featureHash) {
        StateManager.getInstance().getGraphicsLayer().removeAll();
        for(FeatureGraphicMapper graphic: featureHash.values()) {
            StateManager.getInstance().getGraphicsLayer().addGraphic(graphic.oGraphic);
        }
        StateManager.getInstance().getMapView().invalidate();
    }

    public void remove(java.util.HashMap<java.util.UUID, FeatureGraphicMapper> featureHash, IUUIDSet uuids) {
        boolean needToUpdateMap = false;
        for (java.util.UUID uniqueId : uuids) {
            if(null != featureHash.remove(uniqueId)) {
                needToUpdateMap = true;
            }
        }
        if(needToUpdateMap) {
            updateGraphicsLayer(featureHash);
        }
    }

    public void remove(java.util.HashMap<java.util.UUID, FeatureGraphicMapper> featureHash, UUID uuid) {
        FeatureGraphicMapper oMapper = featureHash.remove(uuid);
        if(null != oMapper) {
            updateGraphicsLayer(featureHash);
        }
    }

    public void add(java.util.HashMap<java.util.UUID, FeatureGraphicMapper> featureHash, FeatureGraphicMapper featureGraphicMapper) {
        featureHash.remove(featureGraphicMapper.oFeature.getGeoId());
        featureHash.put(featureGraphicMapper.oFeature.getGeoId(), featureGraphicMapper);
        updateGraphicsLayer(featureHash);
    }
}
