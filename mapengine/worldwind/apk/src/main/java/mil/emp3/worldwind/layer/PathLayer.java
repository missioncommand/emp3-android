package mil.emp3.worldwind.layer;

import mil.emp3.api.Path;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.PathFeature;

public class PathLayer extends EmpLayer<Path> {
    static final private String TAG = PathLayer.class.getSimpleName();

    public PathLayer(MapInstance mapInstance) {
        super(TAG, mapInstance);
    }

    @Override
    protected FeatureRenderableMapping createFeatureMapping(Path feature) {
        return new PathFeature(feature, getMapInstance());
    }
}
