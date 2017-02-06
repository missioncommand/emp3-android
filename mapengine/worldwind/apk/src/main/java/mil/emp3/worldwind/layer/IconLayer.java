package mil.emp3.worldwind.layer;

import mil.emp3.api.Point;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.PointFeature;

public class IconLayer extends EmpLayer<Point> {
    static final private String TAG = IconLayer.class.getSimpleName();

    public IconLayer(MapInstance mapInstance) {
        super(TAG, mapInstance);
    }

    @Override
    protected FeatureRenderableMapping createFeatureMapping(Point feature) {
        return new PointFeature(feature, getMapInstance());
    }
}
