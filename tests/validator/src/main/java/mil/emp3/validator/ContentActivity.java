package mil.emp3.validator;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import armyc2.c2sd.renderer.utilities.SymbolDefTable;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import mil.emp3.api.MapFragment;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.validator.R;
import mil.emp3.validator.dialogs.FeatureLocationDialog;
import mil.emp3.validator.dialogs.milstdtacticalgraphics.TacticalGraphicPropertiesDialog;
import mil.emp3.validator.dialogs.milstdunits.SymbolPropertiesDialog;
import mil.emp3.validator.features.FeatureDrawListener;
import mil.emp3.validator.features.FeatureInteractionEventListener;
import mil.emp3.validator.fragment.SideNavFragment;
import mil.emp3.validator.model.ManagedMapFragment;
import mil.emp3.validator.model.ManagedOverlay;

import static mil.emp3.validator.PlotModeEnum.DRAW_FEATURE;
import static mil.emp3.validator.PlotModeEnum.EDIT_FEATURE;
import static mil.emp3.validator.PlotModeEnum.EDIT_PROPERTIES;
import static mil.emp3.validator.PlotModeEnum.IDLE;
import static mil.emp3.validator.PlotModeEnum.NEW;

public class ContentActivity extends AppCompatActivity implements
        TacticalGraphicPropertiesDialog.SymbolPropertiesDialogListener,
        SymbolPropertiesDialog.SymbolPropertiesDialogListener{
    static final private String TAG = ContentActivity.class.getSimpleName();
    private IFeature oCurrentSelectedFeature;
    private ManagedMapFragment currentMap = null;
    private FloatingActionButton oEditorCompleteBtn;
    private FloatingActionButton oEditorCancelBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_content);

        if (savedInstanceState != null) {
            Log.d(TAG, "savedInstanceState != null");
            return;
        }

        SymbolDefTable symbolDefTable = SymbolDefTable.getInstance();
        symbolDefTable.init();
        TacticalGraphicPropertiesDialog.loadSymbolTables();
        UnitDefTable unitDefTable = UnitDefTable.getInstance();
        unitDefTable.init();
        SymbolPropertiesDialog.loadSymbolTables();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        oEditorCompleteBtn = (FloatingActionButton) this.findViewById(R.id.editorCompleteBtn);
        oEditorCancelBtn = (FloatingActionButton) super.findViewById(R.id.editorCancelBtn);
        this.oEditorCompleteBtn.hide();
        this.oEditorCancelBtn.hide();

        this.oEditorCompleteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    switch (getMap().getPlotMode()) {
                        case EDIT_FEATURE:
                            getMap().get().completeEdit();
                            break;
                        case DRAW_FEATURE:
                            getMap().get().completeDraw();
                            break;
                    }
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            }
        });

        this.oEditorCancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    //Log.d(TAG, "Edit/Draw Canceled.");
                    switch (currentMap.getPlotMode()) {
                        case EDIT_FEATURE:
                            getMap().get().cancelEdit();
                            break;
                        case DRAW_FEATURE:
                            getMap().get().cancelDraw();
                            break;
                    }
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            }
        });

        getFragmentManager().beginTransaction().replace(R.id.sideNav_fragment, new SideNavFragment()).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    public FloatingActionButton getCancel() {
        return oEditorCancelBtn;
    }

    public FloatingActionButton getComplete() {
        return oEditorCompleteBtn;
    }

    public ManagedMapFragment getMap() {
        if (currentMap == null) {
            currentMap = ValidatorStateManager.getInstance().getMap();
        }
        if (currentMap == null) {
            new AlertDialog.Builder(this)
                    .setTitle("ERROR: You must create a map first")
                    .setMessage("Exit?")
                    .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface arg0, int arg1) {
                            System.exit(13579);
                        }
                    }).create().show();

        }

        try {
            currentMap.get().addFeatureInteractionEventListener(new FeatureInteractionEventListener(
                    currentMap, getFragmentManager()));
        } catch (EMP_Exception e) {
            Log.e(TAG, "addFeatureInteractionEventListener failed. " + e.toString());
        }
        return currentMap;
    }

    @Override
    public void onBackPressed() {
        Log.d(TAG, "onBackPressed");
        if (getFragmentManager().getBackStackEntryCount() == 0) {
            new AlertDialog.Builder(this)
                .setTitle("Really Exit?")
                .setMessage("Are you sure you want to exit?")
                .setNegativeButton(android.R.string.no, null)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        ContentActivity.super.onBackPressed();
                    }
                }).create().show();

        } else {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onSaveClick(TacticalGraphicPropertiesDialog dialog) {
        getMap();
        switch (getMap().getPlotMode()) {
            case NEW:
                break;
            case EDIT_PROPERTIES:
                break;
            case DRAW_FEATURE: {
                MilStdSymbol oSymbol = null;
                try {
                    oSymbol = new MilStdSymbol(dialog.getMilStdVersion(), dialog.getSymbolCode());

                    oSymbol.setSymbolCode(dialog.getSymbolCode());
                    oSymbol.setName(dialog.getFeatureName());
                    oSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                    FeatureDrawListener featureDrawListener = new FeatureDrawListener(getMap(),
                            getFragmentManager());
                    this.getMap().get().drawFeature(oSymbol, featureDrawListener);
                } catch (EMP_Exception e) {
                    Log.d(TAG, "Cant draw " + dialog.getSymbolCode(), e);
                    getMap().setPlotMode(IDLE);
                }
                break;
            }
        }
    }

    @Override
    public void onCancelClick(TacticalGraphicPropertiesDialog dialog) {
        getMap().setPlotMode(IDLE);
    }

    private void openFeatureProperties() {
        SymbolPropertiesDialog featurePropertiesDlg;

        switch (getMap().getPlotMode()) {
            case NEW:
                featurePropertiesDlg = new SymbolPropertiesDialog();

                featurePropertiesDlg.show(this.getFragmentManager(), "Feature Properties");
                break;
            case EDIT_PROPERTIES:
                if (this.oCurrentSelectedFeature != null) {
                    if (this.oCurrentSelectedFeature instanceof MilStdSymbol) {
                        featurePropertiesDlg = new SymbolPropertiesDialog();

                        MilStdSymbol oSymbol = (MilStdSymbol) this.oCurrentSelectedFeature;

                        featurePropertiesDlg.setMilStdVersion(oSymbol.getSymbolStandard());
                        featurePropertiesDlg.setSymbolCode(oSymbol.getSymbolCode());
                        featurePropertiesDlg.setFeatureName(oSymbol.getName());
                        featurePropertiesDlg.show(this.getFragmentManager(), "Feature Properties");
                    } else {
                        getMap().setPlotMode(IDLE);
                    }
                }
                break;
        }
    }

    @Override
    public void onSymbolPropertiesSaveClick(SymbolPropertiesDialog oDialog) {
        armyc2.c2sd.renderer.utilities.UnitDef oUnitDef = oDialog.getCurrentUnitDef();
        getMap();
        try {
            //Log.d(TAG, "Symbol properties Save Btn");
            switch (getMap().getPlotMode()) {
                case NEW:
                    java.util.List<IGeoPosition> oPositionList = new java.util.ArrayList<>();
                    final MilStdSymbol oNewSymbol = new MilStdSymbol(oDialog.getMilStdVersion(), oDialog.getSymbolCode());
                    oNewSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                    oNewSymbol.setName(oDialog.getFeatureName());
                    oNewSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oUnitDef.getDescription());

                    this.getMap().get().drawFeature(oNewSymbol, new IDrawEventListener() {
                        @Override
                        public void onDrawStart(IMap map) {
                            Log.d(TAG, "Draw Start");
                            getMap().setPlotMode(PlotModeEnum.DRAW_FEATURE);
                            getMap().show();
                            FeatureLocationDialog oDialog = new FeatureLocationDialog();
                            oDialog.setFeature(oNewSymbol);
                            oDialog.show(ContentActivity.this.getFragmentManager(), null);
                            getMap().putDialog(oNewSymbol.getGeoId(), oDialog);
                        }

                        @Override
                        public void onDrawUpdate(IMap map, IFeature feature, List<IEditUpdateData> updateList) {
                            Log.d(TAG, "Draw Update");
                            FeatureLocationDialog oDialog = getMap().getDialog(feature.getGeoId());
                            if (oDialog != null) {
                                oDialog.updateDialog();
                            }
                        }

                        @Override
                        public void onDrawComplete(IMap map, IFeature feature) {
                            Log.d(TAG, "Draw Complete");
                            getMap().setPlotMode(IDLE);
                            getMap().hide();
                            try {
                                getMap().getRootOverlay().addFeature(oNewSymbol, true);
                                getMap().putFeature(oNewSymbol.getGeoId(), oNewSymbol);
                                FeatureLocationDialog oDialog = getMap().getDialog(feature.getGeoId());
                                if (oDialog != null) {
                                    oDialog.updateDialog();
                                }
                                getMap().get().selectFeature(oNewSymbol);
                                ContentActivity.this.oCurrentSelectedFeature = oNewSymbol;
                            } catch (EMP_Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onDrawCancel(IMap map, IFeature originalFeature) {
                            Log.d(TAG, "Draw Cancel");
                            getMap().setPlotMode(IDLE);
                            getMap().hide();
                            FeatureLocationDialog oDialog = getMap().getDialog(originalFeature.getGeoId());
                            if (oDialog != null) {
                                oDialog.updateDialog();
                            }
                        }

                        @Override
                        public void onDrawError(IMap map, String errorMessage) {
                            Log.d(TAG, "Draw Error");
                        }
                    });
                    break;
                case IDLE:
                    break;
                case EDIT_PROPERTIES:
                    if (this.oCurrentSelectedFeature != null) {
                        if (this.oCurrentSelectedFeature instanceof MilStdSymbol) {
                            MilStdSymbol oSymbol = (MilStdSymbol) this.oCurrentSelectedFeature;

                            oSymbol.setSymbolStandard(oDialog.getMilStdVersion());
                            oSymbol.setSymbolCode(oDialog.getSymbolCode());
                            //oSymbol.setAffiliation(oDialog.getAffiliation());
                            //oSymbol.setEchelonSymbolModifier(oDialog.getEchelon());
                            oSymbol.setName(oDialog.getFeatureName());
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oUnitDef.getDescription());
                            oSymbol.apply();
                            getMap().setPlotMode(IDLE);
                        }
                    }
                    break;
            }
        } catch (EMP_Exception ex) {
            Logger.getLogger(ContentActivity.class.getName()).log(Level.SEVERE, null, ex);
            getMap().setPlotMode(IDLE);
        }
    }

    @Override
    public void onSymbolPropertiesCancelClick(SymbolPropertiesDialog dialog) {
        Log.d(TAG, "Feature properties Cancel Btn");
        getMap().setPlotMode(IDLE);
    }
}
