package mil.emp3.api;


import org.cmapi.primitives.GeoContainer;
import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoContainer;

import java.util.List;

import mil.emp3.api.abstracts.Container;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;

/**
 * This class handles all operations on a Geo Container object or Overlay.
 */
public class Overlay extends Container implements IOverlay {

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    /**
     * This default constructor creates an Overlay and its encapsulated GeoContainer.
     */
    public Overlay() {
        super(new GeoContainer());
    }

    /**
     * This constructor create the overlay object with the provided object encapsulated within.
     * @param geoContainer and object that implements the IGeoContainer interface.
     */
    public Overlay(IGeoContainer geoContainer) {
        super(geoContainer);
    }

    @Override
    public java.util.List<IOverlay> getOverlays() {
        return storageManager.getChildOverlays(this);
    }

    @Override
    public void addOverlay(IOverlay overlay, boolean visible)
            throws EMP_Exception {
        if (overlay == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addOverlay can not be null.");
        }
        
        java.util.ArrayList<IOverlay> oList = new java.util.ArrayList<>();
        oList.add(overlay);
        this.addOverlays(oList, visible);
    }

    @Override
    public void addOverlays(java.util.List<IOverlay> overlays, boolean visible)
            throws EMP_Exception {
        if (overlays == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addOverlays can not be null.");
        } else if (overlays.size() > 0) {
            storageManager.addOverlays(this, overlays, visible);
        }
    }

    @Override
    public void removeOverlay(IOverlay overlay)
            throws EMP_Exception {
        if(null == overlay) return;
        java.util.ArrayList<IOverlay> oList = new java.util.ArrayList<>();
        oList.add(overlay);
        this.removeOverlays(oList);
    }

    @Override
    public void removeOverlays(java.util.List<IOverlay> overlays)
            throws EMP_Exception {
        if((null == overlays) || (0 == overlays.size())) return;
        storageManager.removeOverlays(this, overlays);
    }

    @Override
    public java.util.List<IFeature> getFeatures() {
        return storageManager.getChildFeatures(this);
    }

    @Override
    public void addFeature(IFeature feature, boolean visible)
            throws EMP_Exception {
        if (feature == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addFeature can not be null.");
        }
        
        java.util.ArrayList<IFeature> oList = new java.util.ArrayList<>();
        oList.add(feature);
        this.addFeatures(oList, visible);
    }

    @Override
    public void addFeatures(java.util.List<IFeature> features, boolean visible)
            throws EMP_Exception {
        if (features == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addFeatures can not be null.");
        } else if (features.size() > 0) {
            storageManager.addFeatures(this, features, visible);
        }
    }

    @Override
    public void removeFeature(IFeature feature)
            throws EMP_Exception {
        if(null == feature) return;
        java.util.ArrayList<IFeature> oList = new java.util.ArrayList<>();
        oList.add(feature);
        this.removeFeatures(oList);
    }

    @Override
    public void removeFeatures(java.util.List<IFeature> features)
            throws EMP_Exception {
        if((null == features) || (0 == features.size())) return;
        storageManager.removeFeatures(this, features);
    }

    @Override
    public void apply() {

    }
}
