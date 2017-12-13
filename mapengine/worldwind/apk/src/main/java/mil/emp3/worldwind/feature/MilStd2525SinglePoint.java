package mil.emp3.worldwind.feature;

import android.util.SparseArray;

import org.cmapi.primitives.IGeoPosition;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Placemark;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.support.MilStd2525LevelOfDetailSelector;

/**
 * This class implements the milstd 2525 single point icons in the world wind map engine.
 */
public class MilStd2525SinglePoint extends FeatureRenderableMapping<MilStdSymbol> {
    private static final String TAG = MilStd2525SinglePoint.class.getSimpleName();

    public class EMPPlacemark extends Placemark {
        public final MilStd2525SinglePoint featureMapper;

        protected EMPPlacemark(MilStd2525SinglePoint mapper, Position position) {
            super(position);
            this.featureMapper = mapper;
        }
    }
    private final IMilStdRenderer oRenderer;
    private String sSymbolCode;
    private SparseArray oModifiers = null;
    private SparseArray oAttributes = null;
    private int iLastLevelOfDetail = -1;
    private final Placemark placemark;

    public MilStd2525SinglePoint(MapInstance mapInstance, IMilStdRenderer iconRenderer, Position position, MilStdSymbol symbol) {
        super(symbol, mapInstance);

        this.placemark = new EMPPlacemark(this, position);
        this.sSymbolCode = symbol.getSymbolCode();
        this.oRenderer = iconRenderer;
        this.setSymbolAttributes();
        this.setSymbolModifiers();
        placemark.setPickDelegate(symbol);
        switch (symbol.getAltitudeMode()) {
            case RELATIVE_TO_GROUND:
                placemark.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                break;
            case ABSOLUTE:
                placemark.setAltitudeMode(WorldWind.ABSOLUTE);
                break;
            case CLAMP_TO_GROUND:
                placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                break;
        }
        placemark.setLevelOfDetailSelector(MilStd2525LevelOfDetailSelector.getInstance());

        this.setRenderable(placemark);
    }

    @Override
    public void render(RenderContext renderContext) {
        this.placemark.render(renderContext);
    }

    private void setSymbolModifiers() {
        this.oModifiers = this.oRenderer.getUnitModifiers(this.getMapInstance(), this.getFeature());
    }

    private void setSymbolAttributes() {
        this.oAttributes = this.oRenderer.getAttributes(this.getMapInstance(), this.getFeature(), this.isSelected());
        this.getSymbol().setSymbolAttributes(this.oAttributes);
    }

    public SparseArray getSymbolModifiers() {
        return this.oModifiers;
    }

    public SparseArray getSymbolAttributes() {
        return this.oAttributes;
    }

    public void resetDirty() {
        this.setDirty(false);
    }

    /**
     * This method updates the feature and set it dirty if it requires the renderer to be called.
     * @param symbol
     */
    public void updateSymbol(MilStdSymbol symbol) {
        if (this.getFeature() != symbol) {
            this.setFeature(symbol);
            this.setDirty(false);
        }

        // Without the following position updates via a call to Feature.apply will not work.
        // Should we comparing old position vs new position?
        IGeoPosition oPos = this.getFeature().getPosition();
        this.placemark.setPosition(Position.fromDegrees(oPos.getLatitude(), oPos.getLongitude(), oPos.getAltitude()));

        if (this.sSymbolCode != symbol.getSymbolCode()) {
            // if the symbol code has changed it is dirty.
            this.setDirty(true);
            this.sSymbolCode = symbol.getSymbolCode();
        }

        SparseArray oMod = this.oRenderer.getUnitModifiers(this.getMapInstance(), this.getFeature());
        SparseArray oAttr = this.oRenderer.getAttributes(this.getMapInstance(), this.getFeature(), isSelected());

        if (!this.oModifiers.toString().equals(oMod.toString())) {
            // The modifiers have changed so we mark it dirty.
            this.setDirty(true);
            this.oModifiers = oMod;
        }

        if (!this.oAttributes.toString().equals(oAttr.toString())) {
            // The attributes has changed, mark dirty.
            this.setDirty(true);
            this.oAttributes = oAttr;
        }
    }

    public String getSymbolCode() {
        return this.getFeature().getSymbolCode();
    }

    public MilStdSymbol getSymbol() {
        return this.getFeature();
    }

    public int getLastLevelOfDetail() {
        return this.iLastLevelOfDetail;
    }

    public void setLastLevelOfDetail(int iLevel) {
        this.iLastLevelOfDetail = iLevel;
    }

    public double getIconScale() {
        double scale = this.getFeature().getIconScale();

        if (this.isSelected()) {
            scale = scale * oRenderer.getSelectedIconScale(this.getMapInstance());
        }

        return scale;
    }

    /**
     * Update attributes based on selected flag. This will get picked up on the next render call.
     * @param selected
     */
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        setSymbolAttributes();
        setSymbolModifiers();
    }
}
