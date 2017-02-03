package mil.emp3.worldwind.feature;

import android.util.Log;

import org.cmapi.primitives.IGeoBounds;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.worldwind.FeatureRenderableMapping;
import mil.emp3.worldwind.MapInstance;

/**
 * This class manages the relationship between the EMP MilStdSymbol and the WW basic shapes.
 */
public class MilStd2525TacticalGraphic extends FeatureRenderableMapping implements Renderable {
    final static private String TAG = MilStd2525TacticalGraphic.class.getSimpleName();
    private final MapInstance mapInstance;

    public MilStd2525TacticalGraphic(MilStdSymbol symbol, MapInstance instance) {
        super(symbol);
        this.mapInstance = instance;
        super.isDirty(true);
    }

    @Override
    public String getDisplayName() {
        return this.oFeature.getName();
    }

    @Override
    public void setDisplayName(String s) {
    }

    @Override
    public boolean isEnabled() {
        return this.isVisible();
    }

    @Override
    public void setEnabled(boolean b) {
        this.setVisible(b);
    }

    @Override
    public Object getPickDelegate() {
        return this.oFeature;
    }

    @Override
    public void setPickDelegate(Object o) {
    }

    @Override
    public Object getUserProperty(Object o) {
        return null;
    }

    @Override
    public Object putUserProperty(Object o, Object o1) {
        return null;
    }

    @Override
    public Object removeUserProperty(Object o) {
        return null;
    }

    @Override
    public boolean hasUserProperty(Object o) {
        return false;
    }

    @Override
    public void render(RenderContext renderContext) {
        Log.i(TAG, "TG render.");
    }

    public void render(RenderContext renderContext, IGeoBounds bounds, double cameraAltitude) {

        if (this.isVisible()) {
            // If its visible we must render it on the map.
            //Log.i(TAG, "TG render with bounds visible.");
            if (this.isDirty()) {
                // If it is dirty we must call the renderer again before we render it on the map.
                MilStdSymbol symbol = (MilStdSymbol) this.oFeature;

                this.mapInstance.renderMPTacticalGraphic(this, bounds);
                this.isDirty(false);
                //Log.i(TAG, "  render dirty.");
            }

            java.util.List<Renderable> renderableList = this.getRenderable();

            if ((renderableList != null) && !renderableList.isEmpty()) {
                //Log.i(TAG, "  render renderables.");
                // Render the polygons first, then the lines then the Text.
                for(Renderable renderable: renderableList) {
                    if (renderable instanceof gov.nasa.worldwind.shape.Polygon) {
                        renderable.render(renderContext);
                    }
                }
                for(Renderable renderable: renderableList) {
                    if (renderable instanceof gov.nasa.worldwind.shape.Path) {
                        renderable.render(renderContext);
                    }
                }

                if (cameraAltitude <= this.mapInstance.getFarDistanceThreshold()) {
                    for(Renderable renderable: renderableList) {
                        if (renderable instanceof gov.nasa.worldwind.shape.Label) {
                            renderable.render(renderContext);
                        }
                    }
                }
            }
        }
    }
}
