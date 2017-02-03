package mil.emp3.worldwind.layer;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.MilStd2525SinglePoint;
import mil.emp3.worldwind.feature.MilStd2525TacticalGraphic;

/**
 * This class implements the NASA WW layer that shall contain all milstd symbols.
 */
public class MilStdSymbolLayer extends EmpLayer<MilStdSymbol> {
    final static private String TAG = MilStdSymbolLayer.class.getSimpleName();

    final private List<Renderable> singlePointList;

    public MilStdSymbolLayer(MapInstance mapInstance) {
        super(TAG, mapInstance);
        singlePointList = new ArrayList<>();
    }

    /**
     * It is important that multi-point symbols are rendered before single-point symbols.
     * @param rc
     */
    @Override
    protected void doRender(RenderContext rc) {
        Renderable renderable;
        if ((this.count() > 0) && (this.getRenderable(0) != null)) {
            double cameraAltitude = getMapInstance().getWW().getNavigator().getAltitude();
            IGeoBounds bounds = getMapInstance().getMapBounds();

            for (int iIndex = 0; iIndex < this.count() ; iIndex++) {
                renderable = this.getRenderable(iIndex);
                try {
                    if (renderable instanceof MilStd2525TacticalGraphic) {
                        ((MilStd2525TacticalGraphic) renderable).render(rc, bounds, cameraAltitude);
                    }
                } catch (Exception var6) {
                    Log.e(TAG, "Exception while rendering shape \'" + renderable.getDisplayName() + "\'", var6);
                }
            }

        }
        for (int iIndex = 0; iIndex < this.singlePointList.size(); iIndex++) {
            renderable = this.singlePointList.get(iIndex);
            renderable.render(rc);
        }
    }

    @Override
    public boolean removeRenderable(Renderable renderable) {
        if (super.removeRenderable(renderable)) {
            return true;
        }
        return this.singlePointList.remove(renderable);
    }

    @Override
    protected FeatureRenderableMapping createFeatureMapping(MilStdSymbol feature) {
        FeatureRenderableMapping mapping;

        if (feature.isSinglePoint()) {
            IGeoPosition oPos;
            if (null != feature.getPosition()) {
                oPos = feature.getPositions().get(0);
            } else {
                oPos = new GeoPosition();
            }
            mapping = new MilStd2525SinglePoint(getMapInstance(), getMapInstance().getMilStdRenderer(), Position.fromDegrees(oPos.getLatitude(), oPos.getLongitude(), oPos.getAltitude()), feature);
            singlePointList.add(mapping);
        } else {
            mapping = new MilStd2525TacticalGraphic(feature, getMapInstance());
            this.addRenderable(mapping);
        }

        return mapping;
    }

    @Override
    protected FeatureRenderableMapping getFeatureMapping(MilStdSymbol feature) {
        FeatureRenderableMapping oMapping;

        if (getMapInstance().getFeatureHash().containsKey(feature.getGeoId())) {
            oMapping = getMapInstance().getFeatureHash().get(feature.getGeoId());
            if (feature.isSinglePoint()) {
                ((MilStd2525SinglePoint) oMapping).updateSymbol(feature);
            } else {
                oMapping.setFeature(feature);
            }
        } else {
            oMapping = this.createFeatureMapping(feature);
            getMapInstance().getFeatureHash().put(feature.getGeoId(), oMapping);
            if (!feature.isSinglePoint()) {
                getMapInstance().addToDirtyOnMapMove(feature.getGeoId());
            }
        }

        return oMapping;
    }
}
