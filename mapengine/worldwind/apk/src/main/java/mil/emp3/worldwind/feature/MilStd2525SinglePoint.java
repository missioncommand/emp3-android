package mil.emp3.worldwind.feature;

import android.util.Log;
import android.util.SparseArray;

import org.cmapi.primitives.IGeoPosition;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.RenderContext;
import gov.nasa.worldwind.shape.Placemark;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.worldwind.MapInstance;
import mil.emp3.worldwind.feature.support.MilStd2525LevelOfDetailSelector;

/**
 * This class implements the milstd 2525 single point icons in the world wind map engine.
 */
public class MilStd2525SinglePoint extends Placemark {
    private static final String TAG = MilStd2525SinglePoint.class.getSimpleName();

    private MilStdSymbol oSymbol;
    private final IMilStdRenderer oRenderer;
    private final MapInstance oMapInstance;
    private String sSymbolCode;
    private SparseArray oModifiers = null;
    private SparseArray oAttributes = null;
    private boolean bIsDirty = true;
    private int iLastLevelOfDetail = -1;
    private boolean selected = false;

    public MilStd2525SinglePoint(MapInstance mapInstance, IMilStdRenderer iconRenderer, Position position, MilStdSymbol symbol, boolean selected) {
        super(position);
        this.oSymbol = symbol;
        this.sSymbolCode = symbol.getSymbolCode();
        this.oRenderer = iconRenderer;
        this.oMapInstance = mapInstance;
        this.selected = selected;
        this.setSymbolAttributes();
        this.setSymbolModifiers();
        this.setPickDelegate(this.oSymbol);
        switch (symbol.getAltitudeMode()) {
            case RELATIVE_TO_GROUND:
                this.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                break;
            case ABSOLUTE:
                this.setAltitudeMode(WorldWind.ABSOLUTE);
                break;
            case CLAMP_TO_GROUND:
                this.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                break;
        }
        this.setLevelOfDetailSelector(MilStd2525LevelOfDetailSelector.getInstance());
    }

    public void setSelected(boolean value) {
        this.selected = value;
    }

    private void setSymbolModifiers() {
        this.oModifiers = this.oRenderer.getUnitModifiers(this.oMapInstance, this.oSymbol);
    }

    private void setSymbolAttributes() {
        this.oAttributes = this.oRenderer.getAttributes(this.oMapInstance, this.oSymbol, this.selected);
    }

    public SparseArray getSymbolModifiers() {
        return this.oModifiers;
    }

    public SparseArray getSymbolAttributes() {
        return this.oAttributes;
    }

    public boolean isDirty() {
        return this.bIsDirty;
    }

    public void resetDirty() {
        this.bIsDirty = false;
    }

    /**
     * This method updates the feature and set it dirty if it requires the renderer to be called.
     * @param symbol
     */
    public void updateSymbol(MilStdSymbol symbol) {
        if (this.oSymbol != symbol) {
            this.oSymbol = symbol;
        }
        IGeoPosition oPos = this.oSymbol.getPosition();
        //Log.d(TAG, "Lat/Lon: " + oPos.getLatitude() + " / " + oPos.getLongitude());
        this.setPosition(Position.fromDegrees(oPos.getLatitude(), oPos.getLongitude(), oPos.getAltitude()));

        if (this.sSymbolCode != symbol.getSymbolCode()) {
            // if the symbol code has changed it is dirty.
            this.bIsDirty = true;
            this.sSymbolCode = symbol.getSymbolCode();
        }

        SparseArray oMod = this.oRenderer.getUnitModifiers(this.oMapInstance, this.oSymbol);
        SparseArray oAttr = this.oRenderer.getAttributes(this.oMapInstance, this.oSymbol, selected);

        if (!this.oModifiers.toString().equals(oMod.toString())) {
            // The modifiers have changed so we mark it dirty.
            this.bIsDirty = true;
            this.oModifiers = oMod;
        }

        if (!this.oAttributes.toString().equals(oAttr.toString())) {
            // The attributes has changed, mark dirty.
            this.bIsDirty = true;
            this.oAttributes = oAttr;
        }
    }

    public String getSymbolCode() {
        return this.oSymbol.getSymbolCode();
    }

    public MilStdSymbol getSymbol() {
        return this.oSymbol;
    }

    public int getLastLevelOfDetail() {
        return this.iLastLevelOfDetail;
    }

    public void setLastLevelOfDetail(int iLevel) {
        this.iLastLevelOfDetail = iLevel;
    }

    public double getIconScale() {
        double scale = this.oSymbol.getIconScale();

        if (this.selected) {
            scale = scale * oRenderer.getSelectedIconScale(this.oMapInstance);
        }

        return scale;
    }
}
