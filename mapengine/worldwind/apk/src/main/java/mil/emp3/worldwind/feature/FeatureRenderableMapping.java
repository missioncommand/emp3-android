package mil.emp3.worldwind.feature;

import java.util.ArrayList;
import java.util.List;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.Path;
import mil.emp3.api.Polygon;
import mil.emp3.api.Text;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.worldwind.MapInstance;

/**
 * This is the base class for all WW to EMP feature mapping. It implements the Renderable interface
 * so it can be added to a RenderableLayer. This allows it to forward the render request to the actual
 * renderables on the renderable list.
 */
public abstract class FeatureRenderableMapping<T extends IFeature> extends EMPtoWWFeatureConverter implements Renderable {
    final static private String TAG = FeatureRenderableMapping.class.getSimpleName();

    protected T oFeature;
    private boolean isVisible = true;
    private boolean isDirty = true;
    // This list contains all the WW renderables the EMP feature translates into.
    private final List<Renderable> renderableList;
    private boolean isSelected = false;

    public FeatureRenderableMapping(T feature, MapInstance instance) {
        super(instance);
        this.oFeature = feature;
        this.renderableList = new ArrayList<>();
    }

    @Override
    public String getDisplayName() {
        return this.oFeature.getName();
    }

    @Override
    public void setDisplayName(String s) {
        // This method is defined by the Renderable interface but it is never called.
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
        // This method is defined by the Renderable interface but it is never called.
    }

    @Override
    public Object getUserProperty(Object key) {
        // This method is defined by the Renderable interface but it is never called.
        return null;
    }

    @Override
    public Object putUserProperty(Object key, Object value) {
        // This method is defined by the Renderable interface but it is never called.
        return null;
    }

    @Override
    public Object removeUserProperty(Object key) {
        // This method is defined by the Renderable interface but it is never called.
        return null;
    }

    @Override
    public boolean hasUserProperty(Object key) {
        // This method is defined by the Renderable interface but it is never called.
        return false;
    }

    @Override
    public void render(RenderContext renderContext) {
        if (isVisible()) {
            if (isDirty()) {
                this.getRenderableList().clear();
                if (this.getFeature().getBuffer() > 0) {
                    // This allows the subclass to create the buffer renderable.
                    Renderable tempRenderable = generateBuffer(this.getFeature().getBuffer());

                    if (tempRenderable != null) {
                        tempRenderable.setPickDelegate(this.getFeature());
                        this.addRenderable(tempRenderable);
                    }
                }
                generateRenderables();
                setDirty(false);
            }
            for (Renderable renderable : this.renderableList) {
                renderable.render(renderContext);
            }
        }
    }

    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public boolean isVisible() {
        return this.isVisible;
    }

    public void setVisible(boolean value) {
        this.isVisible = value;
    }

    public T getFeature() {
        return this.oFeature;
    }

    public List<Renderable> getRenderableList() {
        return renderableList;
    }

    public void setFeature(T feature) {
        this.oFeature = feature;
        this.isDirty = true;
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    public void setDirty(boolean value) {
        this.isDirty = value;
    }

    public void setRenderable(Renderable renderable) {
        this.renderableList.clear();
        this.renderableList.add(renderable);
    }

    public void setRenderables(java.util.List<Renderable> renderables) {
        this.renderableList.clear();
        this.renderableList.addAll(renderables);
    }

    public void addRenderable(Renderable renderable) {
        if (!this.renderableList.contains(renderable)) {
            this.renderableList.add(renderable);
        }
    }

    /**
     * This method handles the creation of basic shape renderable for points, line, polygons and text.
     * Complex features should override this method to create the renderables needed to plot the feature.
     */
    protected void generateRenderables() {
        Renderable tempRenderable = null;

        if (this.getFeature() instanceof mil.emp3.api.Point) {
            // Create a WW placemark.
            tempRenderable = this.createPlacemark((mil.emp3.api.Point) this.getFeature(), isSelected());
        }
        else if (this.getFeature() instanceof Circle) {
            tempRenderable = this.createWWCircle((Circle) this.getFeature(), isSelected());
        }
        else if (this.getFeature() instanceof Ellipse) {
            tempRenderable = this.createWWEllipse((Ellipse) this.getFeature(), isSelected());
        }
        else if (this.getFeature() instanceof Path) {
            // Create a WW path object.
            tempRenderable = this.createWWPath((Path) this.getFeature(), isSelected());
        } else if (this.getFeature() instanceof Polygon) {
            // Create a WW polygon object.
            tempRenderable = this.createWWPolygon((Polygon) this.getFeature(), isSelected());
        } else if (this.getFeature() instanceof Text) {
            // Create a WW text object.
            tempRenderable = this.createWWLabel((Text) this.getFeature(), isSelected());
        }

        if (tempRenderable != null) {
            tempRenderable.setPickDelegate(this.getFeature());
            this.addRenderable(tempRenderable);
        }
    }

    public void removeRenderables() {
        this.getRenderableList().clear();
    }
}
