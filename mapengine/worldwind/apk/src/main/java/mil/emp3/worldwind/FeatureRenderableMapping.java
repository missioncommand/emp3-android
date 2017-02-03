package mil.emp3.worldwind;

import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.render.Renderable;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;

/**
 *
 */
public class FeatureRenderableMapping {
    protected IFeature oFeature;
    private boolean isVisible = true;
    private boolean isDirty = true;
    private final java.util.List<Renderable> renderableList;
    private boolean isSelected = false;

    public FeatureRenderableMapping(IFeature feature) {
        this.oFeature = feature;
        this.renderableList = new java.util.ArrayList<>();
    }

    public FeatureRenderableMapping(IFeature feature, Renderable renderable) {
        this.oFeature = feature;
        this.renderableList = new java.util.ArrayList<>();
        this.renderableList.add(renderable);
    }

    public FeatureRenderableMapping(IFeature feature, java.util.List<Renderable> list) {
        this.oFeature = feature;
        this.renderableList = list;
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

    public IFeature getFeature() {
        return this.oFeature;
    }

    public java.util.List<Renderable> getRenderable() {
        return renderableList;
    }

    public void setFeature(IFeature feature) {
        this.oFeature = feature;
        this.isDirty = true;
    }

    public boolean isDirty() {
        return this.isDirty;
    }

    protected void isDirty(boolean value) {
        this.isDirty = value;
    }

    public void setRenderable(Renderable renderable) {
        this.renderableList.clear();
        this.renderableList.add(renderable);
    }
}
