package mil.emp3.api;


import org.cmapi.primitives.GeoContainer;
import org.cmapi.primitives.IGeoContainer;

import java.util.ArrayList;
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
    public List<IOverlay> getOverlays() {
        return storageManager.getChildOverlays(this);
    }

    @Override
    public void addOverlay(IOverlay overlay, boolean visible, Object object)
            throws EMP_Exception {
        if (overlay == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addOverlay can not be null.");
        }
        
        ArrayList<IOverlay> oList = new ArrayList<>();
        oList.add(overlay);
        this.addOverlays(oList, visible, object);
    }

    @Override
    public void addOverlays(List<IOverlay> overlays, boolean visible, Object object)
            throws EMP_Exception {
        if (overlays == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addOverlays can not be null.");
        } else if (overlays.size() > 0) {
            storageManager.addOverlays(this, overlays, visible, object);
        }
    }

    @Override
    public void removeOverlay(IOverlay overlay, Object object)
            throws EMP_Exception {
        if(null == overlay) return;
        ArrayList<IOverlay> oList = new ArrayList<>();
        oList.add(overlay);
        this.removeOverlays(oList, object);
    }

    @Override
    public void removeOverlays(List<IOverlay> overlays, Object object)
            throws EMP_Exception {
        if((null == overlays) || (0 == overlays.size())) return;
        storageManager.removeOverlays(this, overlays, object);
    }

    @Override
    public void addOverlay(IOverlay overlay, boolean visible)
            throws EMP_Exception {
        this.addOverlay(overlay, visible, null);
    }

    @Override
    public void addOverlays(List<IOverlay> overlays, boolean visible)
            throws EMP_Exception {
        this.addOverlays(overlays, visible, null);
    }

    @Override
    public void removeOverlay(IOverlay overlay)
            throws EMP_Exception {
        this.removeOverlay(overlay, null);
    }

    @Override
    public void removeOverlays(List<IOverlay> overlays)
            throws EMP_Exception {
        this.removeOverlays(overlays, null);
    }

    @Override
    public List<IFeature> getFeatures() {
        return storageManager.getChildFeatures(this);
    }

    @Override
    public void addFeature(IFeature feature, boolean visible, Object object)
            throws EMP_Exception {
        if (feature == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addFeature can not be null.");
        }

        ArrayList<IFeature> oList = new ArrayList<>();
        oList.add(feature);
        this.addFeatures(oList, visible, object);
    }

    @Override
    public void addFeatures(List<IFeature> features, boolean visible, Object object)
            throws EMP_Exception {
        if (features == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addFeatures can not be null.");
        } else if (features.size() > 0) {
            for (IFeature feature : features) {
                feature.validate();
            }
            storageManager.addFeatures(this, features, visible, object);
        }
    }

    @Override
    public void removeFeature(IFeature feature, Object object)
            throws EMP_Exception {
        if(null == feature) return;
        ArrayList<IFeature> oList = new ArrayList<>();
        oList.add(feature);
        this.removeFeatures(oList, object);
    }

    @Override
    public void removeFeatures(List<IFeature> features, Object object)
            throws EMP_Exception {
        if((null == features) || (0 == features.size())) return;
        storageManager.removeFeatures(this, features, object);
    }


    @Override
    public void addFeature(IFeature feature, boolean visible)
            throws EMP_Exception {
        this.addFeature(feature, visible, null);
    }

    @Override
    public void addFeatures(List<IFeature> features, boolean visible)
            throws EMP_Exception {
        this.addFeatures(features, visible, null);
    }

    @Override
    public void removeFeature(IFeature feature)
            throws EMP_Exception {
        this.removeFeature(feature, null);
    }

    @Override
    public void removeFeatures(List<IFeature> features)
            throws EMP_Exception {
        this.removeFeatures(features, null);
    }

    @Override
    public void apply() {

    }
}
