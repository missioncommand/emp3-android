package mil.emp3.api;


import org.cmapi.primitives.GeoContainer;
import org.cmapi.primitives.GeoMilSymbol;
import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoContainer;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.abstracts.Container;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IKMLExportable;
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
    public void addOverlay(IOverlay overlay, boolean visible)
            throws EMP_Exception {
        if (overlay == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addOverlay can not be null.");
        }
        
        ArrayList<IOverlay> oList = new ArrayList<>();
        oList.add(overlay);
        this.addOverlays(oList, visible);
    }

    @Override
    public void addOverlays(List<IOverlay> overlays, boolean visible)
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
        ArrayList<IOverlay> oList = new ArrayList<>();
        oList.add(overlay);
        this.removeOverlays(oList);
    }

    @Override
    public void removeOverlays(List<IOverlay> overlays)
            throws EMP_Exception {
        if((null == overlays) || (0 == overlays.size())) return;
        storageManager.removeOverlays(this, overlays);
    }

    @Override
    public List<IFeature> getFeatures() {
        return storageManager.getChildFeatures(this);
    }

    @Override
    public void addFeature(IFeature feature, boolean visible)
            throws EMP_Exception {
        if (feature == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addFeature can not be null.");
        }

        ArrayList<IFeature> oList = new ArrayList<>();
        oList.add(feature);
        this.addFeatures(oList, visible);
    }

    @Override
    public void addFeatures(List<IFeature> features, boolean visible)
            throws EMP_Exception {
        if (features == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Overlay.addFeatures can not be null.");
        } else if (features.size() > 0) {
            for (IFeature feature : features) {
                if (feature instanceof MilStdSymbol && ((MilStdSymbol) feature).getSymbolCode() == null) {
                    throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Can't add feature with null symbol code");
                }
            }
            storageManager.addFeatures(this, features, visible);
        }
    }

    @Override
    public void removeFeature(IFeature feature)
            throws EMP_Exception {
        if(null == feature) return;
        ArrayList<IFeature> oList = new ArrayList<>();
        oList.add(feature);
        this.removeFeatures(oList);
    }

    @Override
    public void removeFeatures(List<IFeature> features)
            throws EMP_Exception {
        if((null == features) || (0 == features.size())) return;
        storageManager.removeFeatures(this, features);
    }

    @Override
    public void apply() {

    }
}
