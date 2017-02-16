package mil.emp3.worldwind.layer;

import mil.emp3.api.Polygon;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.PolygonFeature;

public class PolygonLayer extends EmpLayer<Polygon> {
    static final private String TAG = PolygonLayer.class.getSimpleName();

    public PolygonLayer(MapInstance mapInstance) {
        super(TAG, mapInstance);
    }

    @Override
    protected FeatureRenderableMapping createFeatureMapping(Polygon feature) {
        return new PolygonFeature(feature, getMapInstance());
    }
}
