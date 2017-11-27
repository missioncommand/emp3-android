package mil.emp3.worldwind.feature;

import android.util.Log;

import org.cmapi.primitives.IGeoBounds;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.Path;
import mil.emp3.api.Polygon;
import mil.emp3.api.Text;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.support.BufferGenerator;

/**
 * This class represents the features (Rectangle, Square, Circle, and Ellipse) that get rendered with the MilStd renderer.
 * These feature contain properties such as width, length, and azimuth, in the feature where a MilStd feature
 * provides them in the MilStd modifiers.
 */

public class RenderedFeature extends FeatureRenderableMapping<IFeature> {
    final static private String TAG = RenderedFeature.class.getSimpleName();

    // The renderable list in the base class contains the polygons.
    // This list contains the paths.
    private final List<Renderable> pathRenderableList;
    // This list contains the labels.
    private final List<Renderable> labelRenderableList;

    public RenderedFeature(IFeature feature, MapInstance instance) {
        super(feature, instance);
        pathRenderableList = new ArrayList<>();
        labelRenderableList = new ArrayList<>();
    }

    @Override
    public void render(RenderContext renderContext) {
    }

    public void render(RenderContext renderContext, IGeoBounds bounds, double cameraAltitude) {

        if (this.isVisible()) {
            if (this.isDirty()) {
                removeRenderables();
                // If it is dirty we must call the renderer again before we render it on the map.
                this.renderFeature(bounds);
                this.setDirty(false);
            }

            /*
                When features are rendered we need to render all background objects first. This is map services, ground images etc.. Then we need
                to render polygons because if they have fill they would cover lines, text and icons. After polygons, we render paths (lines) then icons,
                then text.

                However when the feature is rendered with a combination of basic shapes we need to plot them in the same order. Using KML as an example,
                when the KML layer is going to get rendered it renders each KML feature sequentially. For each KML it will render all ground images,
                then polygons, then lines, then icons, then text. This has the effect of rendering the KML as one object and it would be obscured by the
                next feature if they are at the same location.

                Render the polygons first, then the lines(Path) then the Text.
            */

            if (this.getRenderableList() != null){
                for(Renderable renderable: this.getRenderableList()) {
                    renderable.render(renderContext);
                }
            }

            if (this.pathRenderableList != null){
                for(Renderable renderable: this.pathRenderableList) {
                    renderable.render(renderContext);
                }
            }

            if (this.labelRenderableList != null){
                if (cameraAltitude <= this.getMapInstance().getFarDistanceThreshold()) {
                    for(Renderable renderable: this.labelRenderableList) {
                        renderable.render(renderContext);
                    }
                }
            }
        }
    }

    protected Renderable generateBuffer(double buffer) {
        try {
            Polygon bufferPolygon = BufferGenerator.generateBufferPolygon(getFeature(), getMapInstance(), buffer);
            if(null != bufferPolygon) {
                return (this.createWWPolygon(bufferPolygon, false));
            }
        } catch(Exception e) {
            Log.e(TAG, "generateBuffer buffer " + buffer, e);
        }
        return null;
    }

    private void renderFeature(IGeoBounds bounds) {
        Renderable tempRenderable;

        if (getFeature().getBuffer() > 0) {
            tempRenderable = generateBuffer(getFeature().getBuffer());

            if (tempRenderable != null) {
                tempRenderable.setPickDelegate(getFeature());
                addRenderable(tempRenderable);
            }
        }

        java.util.List<IFeature> featureList = getMapInstance().getMilStdRenderer().getFeatureRenderableShapes(getMapInstance(), this.getFeature(), false);

        for (IFeature feature: featureList) {
            if (feature instanceof Path) {
                // Create a WW path object.
                tempRenderable = this.createWWPath((Path) feature, false);

                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(this.getFeature());
                    this.pathRenderableList.add(tempRenderable);
                }
            } else if (feature instanceof Polygon) {
                // Create a WW polygon object.
                tempRenderable = this.createWWPolygon((Polygon) feature, false);

                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(this.getFeature());
                    this.addRenderable(tempRenderable);
                }
            } else if (feature instanceof Text) {
                // Create a WW text object.
                tempRenderable = this.createWWLabel((Text) feature, false);

                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(this.getFeature());
                    this.labelRenderableList.add(tempRenderable);
                }
            }
        }
    }

    @Override
    public void removeRenderables() {
        this.getRenderableList().clear();
        pathRenderableList.clear();
        labelRenderableList.clear();
    }
}
