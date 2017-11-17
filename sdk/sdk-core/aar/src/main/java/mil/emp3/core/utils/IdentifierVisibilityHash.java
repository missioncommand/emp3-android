package mil.emp3.core.utils;

import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.mapengine.interfaces.ISetVisibilityList;

/**
 *
 * This class defines a java.util.HashMap of java.util.UUID, boolean
 */
public class IdentifierVisibilityHash extends java.util.HashMap<java.util.UUID, Boolean> implements ISetVisibilityList {
    public void putFeature(IContainer container, boolean bVisible) {
        if (container instanceof IFeature) {
            this.put(container.getGeoId(), bVisible);
        }
    }

    public void putOverlay(IContainer container, boolean bVisible) {
        if (container instanceof IOverlay) {
            this.put(container.getGeoId(), bVisible);
        }
    }

    public void put(IContainer container, boolean bVisible) {
        this.put(container.getGeoId(), bVisible);
    }
}
