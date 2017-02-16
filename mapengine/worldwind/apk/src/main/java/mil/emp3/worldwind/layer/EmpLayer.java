package mil.emp3.worldwind.layer;

import java.util.UUID;

import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.MapInstance;

/**
 * This abstract class is the base class for all layers that contain EMP feature. It allows the implementation
 * to ensure that the feature types are rendered in a specific order.
 * @param <T> A class the implements the IFeature interface.
 */
abstract public class EmpLayer<T extends IFeature> extends RenderableLayer {
    final static private String TAG = EmpLayer.class.getSimpleName();

    final private MapInstance mapInstance;

    public EmpLayer(String displayName, MapInstance mapInstance) {
        super(displayName);

        this.mapInstance = mapInstance;
    }

    protected MapInstance getMapInstance() {
        return mapInstance;
    }

    /**
     * Make sure you don't touch the View in this method.
     * @param feature
     * @param isVisible
     */
    public void plot(T feature, boolean isVisible) {
        FeatureRenderableMapping oMapping = this.getFeatureMapping(feature);

        oMapping.setVisible(isVisible);
    }

    /**
     * This method removes all the renderables generated from the feature from the mapping and from the layer.
     * @param geoId This geo Id of the feature to remove.
     */
    public void removeFeatureRenderables(UUID geoId) {
        if (getMapInstance().getFeatureHash().containsKey(geoId)) {
            FeatureRenderableMapping oMapping;
            oMapping = getMapInstance().getFeatureHash().get(geoId);

            oMapping.removeRenderables();
            this.removeRenderable(oMapping);
        }
    }

    /**
     * This method is called to create a feature mapping of the proper type. It must be implemented
     * by the each derived class.
     * @param feature
     * @return
     */
    abstract protected FeatureRenderableMapping createFeatureMapping(T feature);

    /**
     * This method retrieves the features mapping if it exists or creates one for it if it does not exists.
     * @param feature
     * @return The feature mapping object.
     */
    protected FeatureRenderableMapping getFeatureMapping(T feature) {
        FeatureRenderableMapping oMapping;

        if (getMapInstance().getFeatureHash().containsKey(feature.getGeoId())) {
            oMapping = getMapInstance().getFeatureHash().get(feature.getGeoId());
            oMapping.setFeature(feature);
        } else {
            oMapping = this.createFeatureMapping(feature);
            this.addRenderable(oMapping);
            getMapInstance().getFeatureHash().put(feature.getGeoId(), oMapping);
        }

        return oMapping;
    }
}
