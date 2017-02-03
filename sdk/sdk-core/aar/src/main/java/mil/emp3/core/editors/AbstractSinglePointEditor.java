package mil.emp3.core.editors;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This abstract class handles features that rare single point type features.
 */

public abstract class AbstractSinglePointEditor extends AbstractDrawEditEditor {
    protected AbstractSinglePointEditor(IMapInstance map, IFeature feature, IEditEventListener oEventListener, boolean bUsesCP) throws EMP_Exception {
        super(map, feature, oEventListener, bUsesCP);
    }

    protected AbstractSinglePointEditor(IMapInstance map, IFeature feature, IDrawEventListener oEventListener, boolean bUsesCP) throws EMP_Exception {
        super(map, feature, oEventListener, bUsesCP);
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        IGeoPosition oCenterPos = this.getMapCameraPosition();
        IGeoPosition pos = new GeoPosition();
        java.util.List<IGeoPosition> posList = new java.util.ArrayList<>();

        pos.setLatitude(oCenterPos.getLatitude());
        pos.setLongitude(oCenterPos.getLongitude());
        pos.setAltitude(0.0);

        posList.add(pos);

        this.oFeature.setPositions(posList);
    }

    @Override
    protected boolean moveIconTo(IGeoPosition oLatLng) {
        IGeoPosition oPos = this.getFeature().getPositions().get(0);
        oPos.setLatitude(oLatLng.getLatitude());
        oPos.setLongitude(oLatLng.getLongitude());
        return true;
    }

    @Override
    protected boolean doFeatureMove(double dBearing, double dDistance) {
        IFeature oFeature = this.getFeature();
        IGeoPosition oPos = oFeature.getPositions().get(0);

        GeoLibrary.computePositionAt(dBearing, dDistance, oPos, oPos);

        return true;
    }
}
