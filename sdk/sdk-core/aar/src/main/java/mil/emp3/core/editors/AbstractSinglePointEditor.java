package mil.emp3.core.editors;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This abstract class handles features that rare single point type features.
 */

public abstract class AbstractSinglePointEditor<T extends IFeature> extends AbstractDrawEditEditor<T> {
    protected AbstractSinglePointEditor(IMapInstance map, T feature, IEditEventListener oEventListener, boolean bUsesCP) throws EMP_Exception {
        super(map, feature, oEventListener, bUsesCP);
    }

    protected AbstractSinglePointEditor(IMapInstance map, T feature, IDrawEventListener oEventListener, boolean bUsesCP, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, bUsesCP, newFeature);
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        if (!this.isNewFeature()) {
            // A feature that already exists should have all of its properties set already.
            return;
        }
        this.oFeature.getPositions().add(getCenter());
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
        // this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{0}); TODO causes duplicate events.

        return true;
    }
}
