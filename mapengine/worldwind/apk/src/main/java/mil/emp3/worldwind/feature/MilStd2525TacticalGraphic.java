package mil.emp3.worldwind.feature;

import org.cmapi.primitives.IGeoBounds;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Path;
import mil.emp3.api.Polygon;
import mil.emp3.api.Text;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.worldwind.MapInstance;

/**
 * This class manages the relationship between the EMP MilStdSymbol and the WW basic shapes.
 */
public class MilStd2525TacticalGraphic extends FeatureRenderableMapping<MilStdSymbol> {
    final static private String TAG = MilStd2525TacticalGraphic.class.getSimpleName();

    // The renderable list in the parent class will contain the polygon renderables.
    // This list will contain the path renderables.
    private final List<Renderable> renderablePathList;
    // This list cintains the label renderables.
    private final List<Renderable> renderableLabelList;

    public MilStd2525TacticalGraphic(MilStdSymbol symbol, MapInstance instance) {
        super(symbol, instance);
        renderablePathList = new ArrayList<>();
        renderableLabelList = new ArrayList<>();
    }

    public void render(RenderContext renderContext, IGeoBounds bounds, double cameraAltitude) {

        if (this.isVisible()) {
            // If its visible we must render it on the map.
            //Log.i(TAG, "TG render with bounds visible.");
            if (this.isDirty()) {
                // If it is dirty we must call the renderer again before we render it on the map.
                renderMPTacticalGraphic(bounds);
                this.setDirty(false);
            }

            java.util.List<Renderable> renderableList = this.getRenderableList();

            //Log.i(TAG, "  render renderables.");
            // Render the polygons first, then the lines then the Text.
            for(Renderable renderable: renderableList) {
                renderable.render(renderContext);
            }
            for(Renderable renderable: renderablePathList) {
                renderable.render(renderContext);
            }

            if (cameraAltitude <= this.getMapInstance().getFarDistanceThreshold()) {
                for(Renderable renderable: renderableLabelList) {
                    renderable.render(renderContext);
                }
            }
        }
    }

    private void renderMPTacticalGraphic(IGeoBounds bounds) {
        Renderable tempRenderable;
        MilStdSymbol oSymbol = this.getFeature();

        this.getRenderableList().clear();
        this.renderablePathList.clear();
        this.renderableLabelList.clear();

        java.util.List<IFeature> featureList = getMapInstance().getMilStdRenderer().getTGRenderableShapes(getMapInstance(), oSymbol, isSelected());

        for (IFeature feature: featureList) {
            if (feature instanceof Path) {
                // Create a WW path object.
                tempRenderable = this.createWWPath((Path) feature, isSelected());

                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(oSymbol);
                    this.renderablePathList.add(tempRenderable);
                }
            } else if (feature instanceof Polygon) {
                // Create a WW polygon object.
                tempRenderable = this.createWWPolygon((Polygon) feature, isSelected());

                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(oSymbol);
                    this.addRenderable(tempRenderable);
                }
            } else if (feature instanceof Text) {
                // Create a WW text object.
                tempRenderable = this.createWWLabel((Text) feature, isSelected());

                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(oSymbol);
                    this.renderableLabelList.add(tempRenderable);
                }
            }
        }
    }

    @Override
    public void removeRenderables() {
        this.getRenderableList().clear();
        this.renderablePathList.clear();
        this.renderableLabelList.clear();
    }
}
