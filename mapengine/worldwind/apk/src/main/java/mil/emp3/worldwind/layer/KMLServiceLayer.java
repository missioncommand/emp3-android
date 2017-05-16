package mil.emp3.worldwind.layer;

import mil.emp3.api.interfaces.IKML;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.feature.KMLFeature;

/**
 * KMLService creates KMLFeature(s) and they will get added to this layer.
 */
public class KMLServiceLayer extends EmpLayer<IKML> {
    final static private String TAG = KMLServiceLayer.class.getSimpleName();

    public KMLServiceLayer(MapInstance mapInstance) {
        super(TAG, mapInstance);
    }

    @Override
    protected FeatureRenderableMapping createFeatureMapping(IKML feature) {
        return new KMLFeature(feature, getMapInstance());
    }
}
