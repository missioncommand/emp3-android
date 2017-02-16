package mil.emp3.worldwind.layer;

import android.util.Log;

import org.cmapi.primitives.IGeoBounds;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.RenderedFeature;

/**
 * This class handles the basic shape features that get rendered with the MilStd renderer.
 */

public class RenderedFeatureLayer extends EmpLayer<IFeature> {
    final static private String TAG = RenderedFeatureLayer.class.getSimpleName();

    public RenderedFeatureLayer(MapInstance mapInstance) {
        super(TAG, mapInstance);
    }

    @Override
    protected void doRender(RenderContext rc) {
        if ((this.count() > 0) && (this.getRenderable(0) != null)) {
            double cameraAltitude = getMapInstance().getWW().getNavigator().getAltitude();
            RenderedFeature feature;
            IGeoBounds bounds = getMapInstance().getMapBounds();
            java.util.Iterator<Renderable> iterator = this.iterator();

            while (iterator.hasNext()) {
                feature = (RenderedFeature) iterator.next();
                try {
                    feature.render(rc, bounds, cameraAltitude);
                } catch (Exception var6) {
                    Log.e(TAG, "Exception while rendering shape \'" + feature.getDisplayName() + "\'", var6);
                }
            }
        }
    }

    @Override
    protected FeatureRenderableMapping createFeatureMapping(IFeature feature) {
        RenderedFeature renderedFeature = new RenderedFeature(feature, getMapInstance());

        // When we zoom in and out rendered features need to be re-rendered by the sec renderer.
        // For example: when map is zoomed out (camera at 2000000 m) and a rectangle of size 1000m x 500m is drawn all the
        // coordinates (lat/lon) are the same. As we zoom in we need to go back to the SEC mil std renderer to recalculate
        // the coordinates.
        if(null != renderedFeature) {
            getMapInstance().addToDirtyOnMapMove(feature.getGeoId());
        }
        return renderedFeature;
    }
}
