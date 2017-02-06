package mil.emp3.worldwind.layer;

import mil.emp3.api.interfaces.IKML;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.KMLFeature;

/**
 * This class implements the NASA WW layer that shall contain all KML objects.
 */

public class KMLLayer extends EmpLayer<IKML> {
    final static private String TAG = KMLLayer.class.getSimpleName();

    public KMLLayer(MapInstance mapInstance) {
        super(TAG, mapInstance);
    }

    @Override
    protected FeatureRenderableMapping createFeatureMapping(IKML feature) {
        return new KMLFeature(feature, getMapInstance());
    }
}
