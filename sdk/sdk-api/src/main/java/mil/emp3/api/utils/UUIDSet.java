package mil.emp3.api.utils;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import org.cmapi.primitives.IGeoBase;
import mil.emp3.api.interfaces.IUUIDSet;

/**
 * This class provides an easy method to create a list of unique UUID.
 * 
 */
public class UUIDSet extends java.util.HashSet<java.util.UUID> implements IUUIDSet {
    public void add(IGeoBase geoObject) {
        this.add(geoObject.getGeoId());
    }

    /**
     * This method adds the UUID of the feature to the set, eliminates duplicates
     * @param geoObject
     */
    public void addFeature(IGeoBase geoObject) {
        if (geoObject instanceof IFeature) {
            this.add(geoObject.getGeoId());
        }
    }

    /**
     * This method adds the UUID of the overlay to the set, eliminates duplicates
     * @param geoObject
     */
    public void addOverlay(IGeoBase geoObject) {
        if (geoObject instanceof IOverlay) {
            this.add(geoObject.getGeoId());
        }
    }

    /**
     * This method adds the UUID of the container to the set, eliminates duplicates
     * @param geoObject
     */
    public void addContainer(IGeoBase geoObject) {
        if (geoObject instanceof IContainer) {
            this.add(geoObject.getGeoId());
        }
    }
}
