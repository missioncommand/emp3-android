package mil.emp3.arcgis.plotters;

import mil.emp3.api.interfaces.IFeature;

/**
 * Created by deepakkarmarkar on 3/30/2016.
 */
public class RendererFactory {

    private static RendererFactory instance;
    public static RendererFactory getInstance() {
        if(null == instance) {
            synchronized(RendererFactory.class) {
                if(null == instance) {
                    instance = new RendererFactory();
                }
            }
        }
        return instance;
    }
    private RendererFactory() { }

    public IRenderer getRenderer(IFeature feature) {
        switch(feature.getFeatureType()) {
            case GEO_POINT: return PointRenderer.getInstance();
            //case GEO_CIRCLE: return CircleRenderer.getInstance();
        }
        return null;
    }
}
