package mil.emp3.worldwind.feature;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.SurfaceImage;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IGeoJSON;
import mil.emp3.api.interfaces.IImageLayer;
import mil.emp3.worldwind.MapInstance;

/**
 * This class implements the mapping between an EMP GeoJSON feature and the WW renderables.
 * In the current implementation the parsing of GeoJSON is done in the EMP API when the GeoJSON feature is
 * instantiated. When the GeoJSON feature arrives at the map instance it contains a list of EMP features
 * and a list of Images that correspond to the objects defined in the KML document.
 *
 * This class separates the renderables by type (Polygon, Line, Point) and renders them
 * in that order. This will ensure that a GeoJSON feature is rendered properly yet in the correct order
 * in respect to all other features.
 */

public class GeoJSONFeature extends FeatureRenderableMapping<IGeoJSON> {

    private final List<Renderable> pointRenderableList;
    private final List<Renderable> lineRenderableList;
    private final List<Renderable> polygonRenderableList;

    public GeoJSONFeature(IGeoJSON feature, MapInstance instance) {
        super(feature, instance);

        pointRenderableList = new ArrayList<>();
        lineRenderableList = new ArrayList<>();
        polygonRenderableList = new ArrayList<>();
    }

    @Override
    protected void generateRenderables() {
        Renderable tempRenderable;
        List<IFeature> featureList = getFeature().getFeatureList();
        List<IImageLayer> imageLayerList = getFeature().getImageLayerList();

        this.getRenderableList().clear();
        pointRenderableList.clear();
        lineRenderableList.clear();
        polygonRenderableList.clear();

        for (IImageLayer imageLayer: imageLayerList) {
            SurfaceImage surfaceImage = this.createWWSurfaceImage(imageLayer);

            surfaceImage.setPickDelegate(getFeature());
            this.addRenderable(surfaceImage);

            if (isSelected()) {
                tempRenderable = this.createWWSurfaceImageSelect(imageLayer);
                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(getFeature());
                    this.polygonRenderableList.add(tempRenderable);
                }
            }
        }

        for (IFeature subFeature: featureList) {
            if (subFeature instanceof Point) {
                tempRenderable = this.createPlacemark((Point) subFeature, isSelected());

                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(getFeature());
                    pointRenderableList.add(tempRenderable);
                }
            } else if (subFeature instanceof Path) {
                // Create a WW path object.
                tempRenderable = this.createWWPath((Path) subFeature, isSelected());

                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(getFeature());
                    this.lineRenderableList.add(tempRenderable);
                }
            } else if (subFeature instanceof Polygon) {
                // Create a WW polygon object.
                tempRenderable = this.createWWPolygon((Polygon) subFeature, isSelected());

                if (tempRenderable != null) {
                    tempRenderable.setPickDelegate(getFeature());
                    this.polygonRenderableList.add(tempRenderable);
                }
            }
        }
    }

    @Override
    public void render(RenderContext renderContext) {
        if (isVisible()) {
            if (isDirty()) {
                generateRenderables();
                setDirty(false);
            }
            // Place the surface images first followed by the polygon, then the paths, then the points, then the labels.
            for (Renderable renderable : this.getRenderableList()) {
                renderable.render(renderContext);
            }
            for (Renderable renderable : this.polygonRenderableList) {
                renderable.render(renderContext);
            }
            for (Renderable renderable : this.lineRenderableList) {
                renderable.render(renderContext);
            }
            for (Renderable renderable : this.pointRenderableList) {
                renderable.render(renderContext);
            }
        }
    }

    @Override
    public void removeRenderables() {
        this.getRenderableList().clear();
        pointRenderableList.clear();
        lineRenderableList.clear();
        polygonRenderableList.clear();
    }
}

