package mil.emp3.validator.model;

import android.support.design.widget.FloatingActionButton;

import java.util.HashMap;
import java.util.UUID;

import mil.emp3.api.MapFragment;
import mil.emp3.api.Overlay;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.validator.PlotModeEnum;
import mil.emp3.validator.dialogs.FeatureLocationDialog;

public class ManagedMapFragment extends ManagedObject<MapFragment> {

    private PlotModeEnum plotMode = PlotModeEnum.IDLE;
    private Overlay oRootOverlay;
    private IFeature oCurrentSelectedFeature;
    private HashMap<UUID, FeatureLocationDialog> oSelectedDialogHash = new HashMap<>();;
    protected HashMap<java.util.UUID, IFeature> oFeatureHash = new HashMap<>();;
    private FloatingActionButton oEditorCompleteBtn;
    private FloatingActionButton oEditorCancelBtn;

    public ManagedMapFragment(MapFragment mapFragment) {
        super(mapFragment);
    }

    public void setPlotMode(PlotModeEnum mode) {
        plotMode = mode;
    }

    public PlotModeEnum getPlotMode() {
        return plotMode;
    }

    public void setRootOverlay() {
        oRootOverlay = new Overlay();
        try {
            this.get().addOverlay(oRootOverlay, true);
        } catch (EMP_Exception empe) {
            empe.printStackTrace();
        }
    }

    public Overlay getRootOverlay() {
        return oRootOverlay;
    }

    public void setSelectedFeature (IFeature feature) {
        oCurrentSelectedFeature = feature;
    }

    public IFeature getSelectedFeature() {
        return oCurrentSelectedFeature;
    }

    public void setDialog(HashMap dialog) {
        oSelectedDialogHash = dialog;
    }

    public void removeDialog(UUID id) { oSelectedDialogHash.remove(id); }

    public void putDialog(UUID id, FeatureLocationDialog fld) {
        oSelectedDialogHash.put(id, fld);
    }

    public FeatureLocationDialog getDialog(UUID id) {
        return oSelectedDialogHash.get(id);
    }

    public void setFeatureHash(HashMap featureHash) {
        oFeatureHash = featureHash;
    }

    public void putFeature(UUID id, IFeature feature) {
        oFeatureHash.put(id, feature);
    }

    public IFeature getFeature(UUID id) {
        return oFeatureHash.get(id);
    }

    public HashMap getFeatureHash() { return oFeatureHash; }

    public void setCancel(FloatingActionButton f) {
        oEditorCancelBtn = f;
    }

    public FloatingActionButton getCancel() {
        return oEditorCancelBtn;
    }

    public void setComplete(FloatingActionButton f) {
        oEditorCompleteBtn = f;
    }

    public FloatingActionButton getComplete() {
        return oEditorCompleteBtn;
    }

    public void show() {
        oEditorCompleteBtn.show();
        oEditorCancelBtn.show();
    }

    public void hide() {
        oEditorCompleteBtn.hide();
        oEditorCancelBtn.hide();
    }

}
