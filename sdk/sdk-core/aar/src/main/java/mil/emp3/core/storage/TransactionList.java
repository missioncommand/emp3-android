package mil.emp3.core.storage;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.utils.UUIDSet;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;

/**
 *
 */
public class TransactionList {
    private final java.util.HashMap<java.util.UUID, FeatureVisibilityList> featureAdds = new java.util.HashMap<>();
    private final java.util.HashMap<java.util.UUID, IUUIDSet> featureRemoves = new java.util.HashMap<>();
    private final java.util.HashMap<java.util.UUID, List<IOverlay>> overlayAdds = new java.util.HashMap<>();
    private final java.util.HashMap<java.util.UUID, IUUIDSet> overlayRemoves = new java.util.HashMap<>();

    public TransactionList() {
        
    }
    
    public java.util.HashMap<java.util.UUID, FeatureVisibilityList> getFeatureAdds() {
        return this.featureAdds;
    }

    public java.util.HashMap<java.util.UUID, IUUIDSet> getFeatureRemoves() {
        return this.featureRemoves;
    }

    public java.util.HashMap<java.util.UUID, List<IOverlay>> getOverlayAdds() {
        return this.overlayAdds;
    }

    public java.util.HashMap<java.util.UUID, IUUIDSet> getOverlayRemoves() {
        return this.overlayRemoves;
    }

    public void addContainerToMap(java.util.UUID mapId, StorageObjectWrapper wrapper) {
        IContainer container = wrapper.getObject();
        
        if (container instanceof IFeature) {
            FeatureVisibilityList oList;
            FeatureVisibility addRecord;
            IFeature feature = (IFeature) container;

            if (!this.featureAdds.containsKey(mapId)) {
                oList = new FeatureVisibilityList();
                this.featureAdds.put(mapId, oList);
            } else {
                oList = this.featureAdds.get(mapId);
            }

            if (!oList.contains(feature.getGeoId())) {
                addRecord = new FeatureVisibility(feature, (wrapper.getVisibilityOnMap(mapId) == VisibilityStateEnum.VISIBLE));
                oList.add(addRecord);
            }
        } else if (container instanceof IOverlay) {
            List<IOverlay> oList;
            IOverlay overlay = (IOverlay) container;

            if (!this.overlayAdds.containsKey(mapId)) {
                oList = new ArrayList<>();
                this.overlayAdds.put(mapId, oList);
            } else {
                oList = this.overlayAdds.get(mapId);
            }

            if (!oList.contains(overlay)) {
                oList.add(overlay);
            }
        }
    }

    public void removeContainerFromMap(java.util.UUID mapId, StorageObjectWrapper wrapper) {
        IContainer container = wrapper.getObject();
        java.util.UUID wrapperId = wrapper.getGeoId();
        IUUIDSet oList;
        
        if (container instanceof IFeature) {
            if (!this.featureRemoves.containsKey(mapId)) {
                oList = new UUIDSet();
                this.featureRemoves.put(mapId, oList);
            } else {
                oList = this.featureRemoves.get(mapId);
            }

            if (!oList.contains(wrapperId)) {
                oList.add(wrapperId);
            }
        } else if (container instanceof IOverlay) {
            if (!this.overlayRemoves.containsKey(mapId)) {
                oList = new UUIDSet();
                this.overlayRemoves.put(mapId, oList);
            } else {
                oList = this.overlayRemoves.get(mapId);
            }

            if (!oList.contains(wrapperId)) {
                oList.add(wrapperId);
            }
        }
    }
}
