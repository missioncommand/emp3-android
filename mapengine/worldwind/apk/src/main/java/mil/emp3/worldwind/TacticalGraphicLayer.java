package mil.emp3.worldwind;

import android.util.Log;

import org.cmapi.primitives.IGeoBounds;

import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.worldwind.feature.MilStd2525TacticalGraphic;

/**
 * This class implements the NASA WW layer that shall contain all tactical graphics.
 */
public class TacticalGraphicLayer extends RenderableLayer {
    final static private String TAG = TacticalGraphicLayer.class.getSimpleName();
    private final MapInstance mapInstance;

    public TacticalGraphicLayer(MapInstance instance) {
        this.mapInstance = instance;
    }

    @Override
    public void render(RenderContext rc) {
        super.render(rc);
    }

    @Override
    protected void doRender(RenderContext rc) {
        if ((this.count() > 0) && (this.getRenderable(0) != null)) {
            double cameraAltitude = this.mapInstance.getWW().getNavigator().getAltitude();
            MilStd2525TacticalGraphic tg;
            IGeoBounds bounds = this.mapInstance.getMapBounds();
            java.util.Iterator<Renderable> iterator = this.iterator();

            while (iterator.hasNext()) {
                tg = (MilStd2525TacticalGraphic) iterator.next();
                try {
                    tg.render(rc, bounds, cameraAltitude);
                } catch (Exception var6) {
                    Log.e(TAG, "Exception while rendering shape \'" + tg.getDisplayName() + "\'", var6);
                }
            }
        }
    }
}
