package mil.emp3.worldwind.layer;

import mil.emp3.api.interfaces.IGeoJSON;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.feature.GeoJSONFeature;

public class GeoJSONLayer extends EmpLayer<IGeoJSON>{
    final static private String TAG = KMLLayer.class.getSimpleName();

    public GeoJSONLayer(MapInstance mapInstance) {
        super(TAG, mapInstance);
    }

    @Override
    protected FeatureRenderableMapping createFeatureMapping(IGeoJSON feature) {
        return new GeoJSONFeature(feature, getMapInstance());
    }
}
