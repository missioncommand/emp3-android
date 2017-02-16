package mil.emp3.dev_test_sdk;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.UnitDef;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import mil.emp3.api.Emp3LifeCycleManager;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.enums.Property;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.FeatureUserInteractionEvent;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.events.MapViewChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.core.utils.CoreMilStdUtilities;
import mil.emp3.dev_test_sdk.dialogs.FeatureLocationDialog;
import mil.emp3.dev_test_sdk.dialogs.milstdtacticalgraphics.TacticalGraphicPropertiesDialog;
import mil.emp3.dev_test_sdk.dialogs.milstdunits.SymbolPropertiesDialog;


public class MainActivity extends AppCompatActivity
        implements SymbolPropertiesDialog.SymbolPropertiesDialogListener,
        TacticalGraphicPropertiesDialog.SymbolPropertiesDialogListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    private IMap map;
    protected Overlay oRootOverlay;
    private ICamera oCamera;
    private ILookAt oLookAt;
    private mil.emp3.api.WMS wmsService;
    protected java.util.HashMap<java.util.UUID, IFeature> oFeatureHash = new java.util.HashMap<>();
    private IFeature oCurrentSelectedFeature;
    private final org.cmapi.primitives.IGeoStrokeStyle oSelectedStrokeStyle = new org.cmapi.primitives.GeoStrokeStyle();
    private java.util.HashMap<java.util.UUID, FeatureLocationDialog> oSelectedDialogHash = new java.util.HashMap<>();
    private boolean bFeatureUIEventProcessed = false;
    private FloatingActionButton oEditorCompleteBtn;
    private FloatingActionButton oEditorCancelBtn;
    private Menu oMenu = null;
    private RelativeLayout oPerformanceDlg;
    private Button oStartBtn;
    private Button oStopBtn;
    private Button oCloseBtn;
    private EditText oCountTb;
    private CheckBox oAffiliationCkb;
    private CheckBox oBatchUpdateCkb;
    protected TextView oResults;
    private PerformanceTestThread oPerformanceTestThread;
    protected TextView oResultTextView;
    protected LinearLayout oResultPanel;
    private EventListenerHandle stateHandler = null;
    private EventListenerHandle userInteraction = null;
    private EventListenerHandle featureInteraction = null;
    private EventListenerHandle viewChange = null;
    private EventListenerHandle cameraHandler = null;

    private boolean swapMapFlag;

    private boolean bCrossHairsOn = false;
    private Toast CoordToast = null;

    private IGeoFillStyle iconStyleFill     = new GeoFillStyle();
    private IGeoStrokeStyle iconStyleStroke = new GeoStrokeStyle();
    private IGeoLabelStyle iconStyleLabel   = new GeoLabelStyle();

    public enum PlotModeEnum {
        IDLE,
        NEW,
        EDIT_PROPERTIES,
        EDIT_FEATURE,
        DRAW_FEATURE
    }

    private MainActivity.PlotModeEnum ePlotMode = MainActivity.PlotModeEnum.IDLE;

    class MyComponentCallbacks implements ComponentCallbacks2 {
        @Override
        public void onTrimMemory(int level) {
            Log.e(TAG, "onTrimMemory " + level);
        }

        @Override
        public void onConfigurationChanged(Configuration newConfig) {
            Log.e(TAG, "onConfigurationChanged ");
        }

        @Override
        public void onLowMemory() {
            Log.e(TAG, "onLowMemory ");
        }
    }

    public class MapInteractionEventListener implements IMapInteractionEventListener {

        @Override
        public void onEvent(MapUserInteractionEvent event) {
            bFeatureUIEventProcessed = false;
            if (event.getCoordinate() == null) {
                Log.d(TAG, "Map User Interactive Event: " + event.getEvent().name() + " X/Y: " + event.getPoint().x + " / " +
                        event.getPoint().y + "  Lat/Lon: null");
            } else {
                Log.d(TAG, "Map User Interactive Event: " + event.getEvent().name() + " X/Y: " + event.getPoint().x + " / " +
                        event.getPoint().y + "  Lat: " + event.getCoordinate().getLatitude() + " Lon: " + event.getCoordinate().getLongitude());
            }

            switch (event.getEvent()) {
                case CLICKED:
                    try {
                        IGeoPosition pos = MainActivity.this.map.containerToGeo(event.getPoint());
                        Log.d(TAG, "Map User container to geo: Lat/Lon: " + pos.getLatitude() + " / " +
                                pos.getLongitude());
                        android.graphics.Point point = MainActivity.this.map.geoToContainer(event.getCoordinate());
                        Log.d(TAG, "Map User geo to container: X/Y: " + point.x + " / " +
                                point.y);
                        Toast.makeText(MainActivity.this, "Container: X/Y: " + point.x + " / " +
                                point.y + " Lat/Lon: " + pos.getLatitude() + " / " +
                                pos.getLongitude(), Toast.LENGTH_LONG).show();
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                        bFeatureUIEventProcessed = true;
                    }
                    break;
                case DOUBLE_CLICKED:
                    break;
                case LONG_PRESS:
                    try {
                        android.graphics.Point point = MainActivity.this.map.geoToScreen(event.getCoordinate());
                        Log.d(TAG, "Map User geo to screen: X/Y: " + point.x + " / " +
                                point.y);
                        IGeoPosition pos = MainActivity.this.map.screenToGeo(point);
                        Log.d(TAG, "Map User screen to geo: Lat/Lon: " + pos.getLatitude() + " / " +
                                pos.getLongitude());
                        Toast.makeText(MainActivity.this, "Screen: X/Y: " + point.x + " / " +
                                point.y + " Lat/Lon: " + pos.getLatitude() + " / " +
                                pos.getLongitude(), Toast.LENGTH_LONG).show();
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                        bFeatureUIEventProcessed = true;
                    }
                    break;
                case DRAG:
                    break;
            }
        }
    }

    public class FeatureInteractionEventListener implements IFeatureInteractionEventListener {

        @Override
        public void onEvent(FeatureUserInteractionEvent event) {
            IFeature oFeature = event.getTarget().get(0);
            //SelectedFeature oSelectedItem;

            bFeatureUIEventProcessed = true;
            if (event.getCoordinate() == null) {
                Log.d(TAG, "Feature User Interactive Event: " + event.getEvent().name() + " Feature Count: " + event.getTarget().size() + " X/Y: " + event.getPoint().x + " / " +
                        event.getPoint().y + "  Lat/Lon: null");
            } else {
                Log.d(TAG, "Feature User Interactive Event: " + event.getEvent().name() + " Feature Count: " + event.getTarget().size() + " X/Y: " + event.getPoint().x + " / " +
                        event.getPoint().y + "  Lat: " + event.getCoordinate().getLatitude() + " Lon: " + event.getCoordinate().getLongitude());
            }
            switch (event.getEvent()) {
                case CLICKED:
                    if (MainActivity.this.ePlotMode == PlotModeEnum.IDLE) {
                        if (MainActivity.this.map.isSelected(oFeature)) {
                            // Its being deselected.
                            MainActivity.this.map.deselectFeature(oFeature);
                            if (MainActivity.this.oSelectedDialogHash.containsKey(oFeature.getGeoId())) {
                                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(oFeature.getGeoId());
                                oDialog.dismiss();
                                MainActivity.this.oSelectedDialogHash.remove(oFeature.getGeoId());
                            }
                        } else {
                            // Its being selected.
                            MainActivity.this.map.selectFeature(oFeature);
                            MainActivity.this.oCurrentSelectedFeature = oFeature;
                            FeatureLocationDialog oDialog = new FeatureLocationDialog();
                            oDialog.show(MainActivity.this.getFragmentManager(), null);
                            oDialog.setFeature(oFeature);
                            MainActivity.this.oSelectedDialogHash.put(oFeature.getGeoId(), oDialog);
                        }
                    }
                    break;
                case DOUBLE_CLICKED:
                    // On a tablet this is the double tap.
                    if (oFeature != null) {
                        // If there is a feature and its selected, we want to place it in edit mode.
                        try {
                            if ((MainActivity.this.ePlotMode == PlotModeEnum.IDLE) &&
                                    MainActivity.this.map.isSelected(oFeature)) {
                                Log.d(TAG, "FeatureUserInteractionEvent  Entering edit mode.");
                                MainActivity.this.oCurrentSelectedFeature = oFeature;
                                MainActivity.this.map.editFeature(oFeature, new FeatureEditorListener(oFeature));
                            }
                        } catch (EMP_Exception Ex) {
                            Log.d(TAG, "editFeature failed.", Ex);
                        }
                    }
                    break;
                case LONG_PRESS:
                    if (event.getTarget().get(0) != null) {
                        // If there is a feature on the list, we want to place it in edit mode.
/*
                        if (MainActivity.this.ePlotMode == PlotModeEnum.IDLE) {
                            Log.d(TAG, "FeatureUserInteractionEvent  Entering edit properties mode.");
                            MainActivity.this.oCurrentSelectedFeature = event.getTarget().get(0);
                            MainActivity.this.ePlotMode = PlotModeEnum.EDIT_PROPERTIES;
                            MainActivity.this.openFeatureProperties();
                        }
*/
                    }
                    break;
                case DRAG:
                    break;
            }
        }
    }

    public class FeatureEditorListener implements IEditEventListener {

        public FeatureEditorListener(IFeature feature) {
        }

        @Override
        public void onEditStart(IMap map) {
            Log.d(TAG, "Edit Start.");
            MainActivity.this.ePlotMode = PlotModeEnum.EDIT_FEATURE;
            MainActivity.this.oEditorCompleteBtn.show();
            MainActivity.this.oEditorCancelBtn.show();
        }

        @Override
        public void onEditUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "Edit Update.");
            if (MainActivity.this.oSelectedDialogHash.containsKey(oFeature.getGeoId())) {
                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(oFeature.getGeoId());
                oDialog.updateDialog();
            }
        }

        @Override
        public void onEditComplete(IMap map, IFeature feature) {
            Log.d(TAG, "Edit Complete.");
            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
            MainActivity.this.oEditorCompleteBtn.hide();
            MainActivity.this.oEditorCancelBtn.hide();
            if (MainActivity.this.oSelectedDialogHash.containsKey(feature.getGeoId())) {
                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(feature.getGeoId());
                oDialog.updateDialog();
            }
        }

        @Override
        public void onEditCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "Edit Canceled.");
            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
            MainActivity.this.oEditorCompleteBtn.hide();
            MainActivity.this.oEditorCancelBtn.hide();
            if (MainActivity.this.oSelectedDialogHash.containsKey(originalFeature.getGeoId())) {
                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(originalFeature.getGeoId());
                oDialog.updateDialog();
            }
        }

        @Override
        public void onEditError(IMap map, String errorMessage) {
            Log.d(TAG, "Edit Error.");
        }
    }

    public class FeatureDrawListener implements IDrawEventListener {

        public FeatureDrawListener(IFeature feature) {
        }

        @Override
        public void onDrawStart(IMap map) {
            Log.d(TAG, "Draw Start.");
            MainActivity.this.ePlotMode = PlotModeEnum.DRAW_FEATURE;
            MainActivity.this.oEditorCompleteBtn.show();
            MainActivity.this.oEditorCancelBtn.show();
        }

        @Override
        public void onDrawUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "Draw Update.");
            int[] aIndexes;
            String temp;

            for (IEditUpdateData updateData: updateList) {
                switch (updateData.getUpdateType()) {
                    case COORDINATE_ADDED:
                    case COORDINATE_MOVED:
                    case COORDINATE_DELETED:
                        aIndexes = updateData.getCoordinateIndexes();
                        temp = "";
                        for (int index = 0; index < aIndexes.length; index++) {
                            if (temp.length() > 0) {
                                temp += ",";
                            }
                            temp += aIndexes[index];
                        }
                        Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " indexes:{" + temp + "}");
                        break;
                    case MILSTD_MODIFIER_UPDATED:
                        Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " modifier: " + updateData.getChangedModifier().name() + " {" + ((MilStdSymbol) oFeature).getStringModifier(updateData.getChangedModifier()) + "}");
                        break;
                    case ACM_ATTRIBUTE_UPDATED:
                        Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " ACM attribute:{" + updateData.getChangedModifier().name() + "}");
                        break;
                }
            }
            if (!MainActivity.this.map.isSelected(oFeature)) {
                MainActivity.this.map.selectFeature(oFeature);
                MainActivity.this.oCurrentSelectedFeature = oFeature;
                FeatureLocationDialog oDialog = new FeatureLocationDialog();
                oDialog.show(MainActivity.this.getFragmentManager(), null);
                oDialog.setFeature(oFeature);
                MainActivity.this.oSelectedDialogHash.put(oFeature.getGeoId(), oDialog);
            } else {
                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(oFeature.getGeoId());
                oDialog.updateDialog();
            }
        }

        @Override
        public void onDrawComplete(IMap map, IFeature feature) {
            Log.d(TAG, "Draw Complete.");
            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
            MainActivity.this.oEditorCompleteBtn.hide();
            MainActivity.this.oEditorCancelBtn.hide();
            MainActivity.this.oFeatureHash.put(feature.getGeoId(), feature);
            try {
                MainActivity.this.oRootOverlay.addFeature(feature, true);
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDrawCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "Draw Canceled.");
            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
            MainActivity.this.oEditorCompleteBtn.hide();
            MainActivity.this.oEditorCancelBtn.hide();
        }

        @Override
        public void onDrawError(IMap map, String errorMessage) {
            Log.d(TAG, "Draw Error.");
        }
    }

    private double randomLocalLatitude() {
        return (Math.random() * 30.0) + 10.0;
    }

    private double randomLocalLongitude() {
        return (Math.random() * 30.0) - 110.0;
    }

    protected IGeoPosition getRandomCoordinate() {
        IGeoPosition oPos = new GeoPosition();
        double dTemp;

        dTemp = this.oCamera.getLatitude() + (3 * Math.random()) - 1.5;
        oPos.setLatitude(dTemp);
        dTemp = this.oCamera.getLongitude() + (3 * Math.random()) - 1.5;
        oPos.setLongitude(dTemp);
        //oPos.setLongitude((Math.random() * 360.0) - 180.0);
        //oPos.setLatitude((Math.random() * 180.0) - 90);
        oPos.setAltitude(Math.random() * 16000.0);
        //oPos.setAltitude(0);

        return oPos;
    }

    private java.util.List<IGeoPosition> getCameraPosition() {
        java.util.List<IGeoPosition> oPosList = new java.util.ArrayList<>();
        IGeoPosition oPos = new GeoPosition();

        oPos.setLatitude(this.oCamera.getLatitude());
        oPos.setLongitude(this.oCamera.getLongitude());
        oPos.setAltitude(0);

        oPosList.add(oPos);
        return oPosList;
    }

    private java.util.List<IGeoPosition> getPositions(int iCount, double dAltitude) {
        IGeoPosition oPos;
        java.util.List<IGeoPosition> oPosList = new java.util.ArrayList<>();
        double dLat = this.randomLocalLatitude();
        double dLong = this.randomLocalLongitude();

        switch (iCount) {
            case 0:
                return oPosList;
            case 1:
                oPos = new GeoPosition();
                oPos.setAltitude(dAltitude);
                oPos.setLatitude(dLat);
                oPos.setLongitude(dLong);
                oPosList.add(oPos);
                break;
            default:
                for (int iIndex = 0; iIndex < iCount; iIndex++) {
                    oPos = new GeoPosition();
                    oPos.setAltitude(dAltitude);
                    oPos.setLatitude(dLat);
                    oPos.setLongitude(dLong);
                    oPosList.add(oPos);

                    if (dLat > 0.0) {
                        dLat -= 0.5;
                    } else {
                        dLat += 0.5;
                    }
                    if (dLong > 0.0) {
                        dLong -= 0.5;
                    } else {
                        dLong += 0.5;
                    }
                }   break;
        }
        return oPosList;
    }

    private void plotManyMilStd(int iMaxCount) {
        UnitDefTable oDefTable = UnitDefTable.getInstance();
        org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStandard =  org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525C;
        int iMilStdVersion = (eStandard == org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B) ? armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525Bch2_USAS_13_14 :
                armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525C;
        java.util.Map<java.lang.String,UnitDef> oDefMap = oDefTable.getAllUnitDefs(iMilStdVersion);
        java.util.Set<String> oSymbols = oDefMap.keySet();
        UnitDef oSymDef;
        int iCount = 0;
        java.util.List<IFeature> oFeatureList = new java.util.ArrayList<>();
        java.util.List<IGeoPosition> oPosList;
        java.util.List<MilStdSymbol.Affiliation> oAffiliationList = new java.util.ArrayList<>();
        java.util.List<MilStdSymbol.Echelon> oEchelonList = new java.util.ArrayList<>();

        oAffiliationList.add(MilStdSymbol.Affiliation.FRIEND);
        oAffiliationList.add(MilStdSymbol.Affiliation.HOSTILE);
        oAffiliationList.add(MilStdSymbol.Affiliation.NEUTRAL);
        //oAffiliationList.add(MilStdSymbol.Affiliation.SUSPECT);

        oEchelonList.add(MilStdSymbol.Echelon.UNIT);
        oEchelonList.add(MilStdSymbol.Echelon.SQUAD);
        oEchelonList.add(MilStdSymbol.Echelon.PLATOON_DETACHMENT);
        oEchelonList.add(MilStdSymbol.Echelon.COMPANY_BATTERY_TROOP);
        oEchelonList.add(MilStdSymbol.Echelon.BATTALION_SQUADRON);
        oEchelonList.add(MilStdSymbol.Echelon.BRIGADE);

        top: {
            for (String sBasicSymbolCode : oSymbols) {
                for (MilStdSymbol.Echelon eEchelon : oEchelonList) {
                    for (MilStdSymbol.Affiliation eAffiliation : oAffiliationList) {
                        //Log.d(TAG, "Symbol " + sBasicSymbolCode);
                        if (SymbolUtilities.isWarfighting(sBasicSymbolCode)) {
                            oSymDef = oDefMap.get(sBasicSymbolCode);

                            oPosList = new java.util.ArrayList<>();
                            oPosList.add(this.getRandomCoordinate());

                            try {
                                // Allocate the new MilStd Symbol with a MilStd version and the symbol code.
                                mil.emp3.api.MilStdSymbol oSPSymbol = new mil.emp3.api.MilStdSymbol(eStandard, sBasicSymbolCode);

                                // Set the symbols affiliation.
                                oSPSymbol.setAffiliation(eAffiliation);
                                // Set the echelon.
                                oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, eEchelon);
                                // Set the symbols altitude mode.
                                //oSPSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                                //oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.HQ_BRIGADE);

                                // Set the position list with 1 position.
                                oSPSymbol.setPositions(oPosList);

                                //Set a single modifier.
                                oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymDef.getDescription());
                                iCount++;

                                // Give the feature a name.
                                oSPSymbol.setName("Unit " + iCount);

                                if ((iCount % 2) == 0) {
                                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSPSymbol.getName());
                                }

                                //Add it to the list we will be adding to the overlay.
                                oFeatureList.add(oSPSymbol);

                                // Keep a reference to the feature in a hash for later use.
                                this.oFeatureHash.put(oSPSymbol.getGeoId(), oSPSymbol);
                                if (oFeatureList.size() >= 200) {
                                    try {
                                        // Add all the features to the overlay.
                                        // It is more efficient to add them in bulk.
                                        oRootOverlay.addFeatures(oFeatureList, true);
                                        Log.d(TAG, "Added " + oFeatureList.size() + " features.");
                                        oFeatureList.clear();
                                    } catch (EMP_Exception Ex) {

                                    }
                                }
                            } catch (EMP_Exception Ex) {

                            }
                        }

                        if ((iMaxCount > 0) && (iCount >= iMaxCount)) {
                            break top;
                        }
                    }
                }
            }
        }
        if (!oFeatureList.isEmpty()) {
            try {
                // Add all the features to the overlay.
                // It is more efficient to add them in bulk.
                oRootOverlay.addFeatures(oFeatureList, true);
                Log.d(TAG, "Added " + oFeatureList.size() + " features.");
            } catch (EMP_Exception Ex) {

            }
        }
    }

    private void processSymbolTable(java.util.Map<java.lang.String,UnitDef> oDefMap,
            org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStandard,
            int iMilStdVersion,
            int iMaxCount) {
        java.util.Set<String> oSymbols = oDefMap.keySet();
        UnitDef oSymDef;
        String sSymbolCode;
        int iCount = 0;
        java.util.List<IFeature> oFeatureList = new java.util.ArrayList<>();

        for (String sBasicSymbolCode: oSymbols) {
            //Log.d(TAG, "Symbol " + sBasicSymbolCode);
            if (SymbolUtilities.isWarfighting(sBasicSymbolCode)) {
                oSymDef = oDefMap.get(sBasicSymbolCode);

                java.util.List<IGeoPosition> oPosList = this.getPositions(1, 20000.0);

                try {
                    // Allocate the new MilStd Symbol with a MilStd version and the symbol code.
                    mil.emp3.api.MilStdSymbol oSPSymbol = new mil.emp3.api.MilStdSymbol(eStandard, sBasicSymbolCode);

                    // Set the symbols affiliation.
                    oSPSymbol.setAffiliation(MilStdSymbol.Affiliation.FRIEND);
                    // Set the symbols altitude mode.
                    //oSPSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                    //oSPSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.HQ_BRIGADE);

                    // Set the position list with 1 position.
                    oSPSymbol.setPositions(oPosList);

                    //Set a single modifier.
                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymDef.getDescription());
                    iCount++;

                    // Give the feature a name.
                    oSPSymbol.setName("Unit " + iCount);

                    //oSPSymbol.setFillStyle(iconStyleFill);
                    //oSPSymbol.setStrokeStyle(iconStyleStroke);
                    //oSPSymbol.setLabelStyle(iconStyleLabel);

                    //Add it to the list we will be adding to the overlay.
                    oFeatureList.add(oSPSymbol);

                    // Keep a reference to the feature in a hash for later use.
                    this.oFeatureHash.put(oSPSymbol.getGeoId(), oSPSymbol);
                } catch (EMP_Exception Ex) {

                }
            }

            if ((iMaxCount > 0) && (iCount >= iMaxCount)) {
                break;
            }
        }
        if (!oFeatureList.isEmpty()) {
            try {
                // Add all the features to the overlay.
                // It is more efficient to add them in bulk.
                oRootOverlay.addFeatures(oFeatureList, true);
                Log.d(TAG, "Added " + oFeatureList.size() + " features.");
            } catch (EMP_Exception Ex) {

            }
        }
    }

    private void plotMilStd(org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStandard, int iCount) {
        int iMilStdVersion = (eStandard == org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B)? armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525Bch2_USAS_13_14:
                armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525C;
        UnitDefTable oDefTable = UnitDefTable.getInstance();
        java.util.Map<java.lang.String,UnitDef> oDefMap = oDefTable.getAllUnitDefs(iMilStdVersion);

        processSymbolTable(oDefMap, eStandard, iMilStdVersion, iCount);
    }

    protected void removeAllFeatures() {
        if (!this.oFeatureHash.isEmpty()) {
            java.util.List<IFeature> oFeatureList = new java.util.ArrayList<>();

            for (java.util.UUID uuid: this.oFeatureHash.keySet()) {
                oFeatureList.add(this.oFeatureHash.get(uuid));
                if (MainActivity.this.oSelectedDialogHash.containsKey(uuid)) {
                    FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(uuid);
                    oDialog.dismiss();
                }
            }

            try {
                this.oRootOverlay.removeFeatures(oFeatureList);
                Log.d(TAG, "Removed " + oFeatureList.size() + " features.");
                this.oFeatureHash.clear();
            } catch (EMP_Exception Ex) {

            }
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "In onRestoreInstanceState");
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "In onSaveInstanceState");
        Emp3LifeCycleManager.onSaveInstanceState(false); // Let EMP3 restore the map
    }

    @Override
    protected void onPause() {
        // this.removeAllFeatures();
        Emp3LifeCycleManager.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Emp3LifeCycleManager.onResume();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (stateHandler != null) {
            map.removeEventListener(stateHandler);
            map.removeEventListener(userInteraction);
            map.removeEventListener(featureInteraction);
            map.removeEventListener(viewChange);
            oCamera.removeEventListener(cameraHandler);
        }
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SymbolPropertiesDialog.loadSymbolTables();
        TacticalGraphicPropertiesDialog.loadSymbolTables();

        org.cmapi.primitives.IGeoColor oColor = new GeoColor();

        oColor.setRed(255);
        oColor.setGreen(255);
        oColor.setBlue(0);
        oColor.setAlpha(1.0);
        this.oSelectedStrokeStyle.setStrokeColor(oColor);
        this.oSelectedStrokeStyle.setStrokeWidth(5);

        map = (IMap) findViewById(R.id.map);

        oCamera = new mil.emp3.api.Camera();
        oCamera.setName("Main Cam");
        oCamera.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
        oCamera.setAltitude(9000.0);
        oCamera.setHeading(0.0);
        oCamera.setLatitude(40.0);
        oCamera.setLongitude(-105.0);
        oCamera.setRoll(0.0);
        oCamera.setTilt(0.0);

        // Set lookat to random position, but near camera

        oLookAt = new mil.emp3.api.LookAt();
        oLookAt.setName("LookAt Point");
        oLookAt.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
        oLookAt.setAltitude(3000.0);
        oLookAt.setHeading(0.0);
        oLookAt.setLatitude(40.0);
        oLookAt.setLongitude(-105.0);
        oLookAt.setRange(1000.0);
        oLookAt.setTilt(0.0);

        try {
            stateHandler = map.addMapStateChangeEventListener(new IMapStateChangeEventListener() {
                @Override
                public void onEvent(MapStateChangeEvent event) {
                    Log.d(TAG, "mapStateChangeEvent " + event.getNewState());
                    MainActivity.this.oRootOverlay = new Overlay();
                    try {
                        if (event.getNewState() == MapStateEnum.MAP_READY) {
                            MainActivity.this.map.addOverlay(MainActivity.this.oRootOverlay, true);
                            MainActivity.this.map.setCamera(MainActivity.this.oCamera, false);
                            MainActivity.this.setEventListeners();

                            MainActivity.this.createCrossHair();
                            //Log.i(TAG, "Map State: " + MainActivity.this.map.getState().name());

                        }
                    } catch (Exception e) {
                        Log.e(TAG, "ERROR: " + e.getMessage(), e);
                    }
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener failed. " + e.toString());
        }
        //Log.d(TAG, "Registered for Map Ready.");

        TextView oCameraSetting = (TextView) this.findViewById(R.id.camerasettings);
        String sTemp = String.format("Lat:%7.5f\u00b0 Lon:%8.5f\u00b0 Alt:%.0f m Heading:%3.0f\u00b0 Tilt:%3.0f\u00b0 Roll:%3.0f\u00b0", oCamera.getLatitude(), oCamera.getLongitude(), oCamera.getAltitude(), oCamera.getHeading(), oCamera.getTilt(), oCamera.getRoll());
        oCameraSetting.setText(sTemp);

        java.util.List<String> oLayers = new java.util.ArrayList<>();

        oLayers.add("imagery_part2-2.0.0.1-wsmr.gpkg");
        try {
            this.wmsService = new mil.emp3.api.WMS(
                    "http://172.16.20.99:5000/WmsServerStrict",
                    WMSVersionEnum.VERSION_1_3_0,
                    "image/png",
                    true,
                    oLayers
            );
            this.wmsService.setLayerResolution(1.0);
            oCamera.setLatitude(32.897);
            oCamera.setLongitude(-105.939);
            oCamera.setAltitude(500.0);
            oCamera.apply(false);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        this.oEditorCompleteBtn = (FloatingActionButton) this.findViewById(R.id.editorCompleteBtn);
        this.oEditorCancelBtn = (FloatingActionButton) this.findViewById(R.id.editorCancelBtn);
        this.oEditorCompleteBtn.hide();
        this.oEditorCancelBtn.hide();

        this.oEditorCompleteBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    switch (MainActivity.this.ePlotMode) {
                        case EDIT_FEATURE:
                            MainActivity.this.map.completeEdit();
                            break;
                        case DRAW_FEATURE:
                            MainActivity.this.map.completeDraw();
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
                    switch (MainActivity.this.ePlotMode) {
                        case EDIT_FEATURE:
                            MainActivity.this.map.cancelEdit();
                            break;
                        case DRAW_FEATURE:
                            MainActivity.this.map.cancelDraw();
                            break;
                    }
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            }
        });

        registerComponentCallbacks(new MyComponentCallbacks());

        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        Log.e(TAG, "Get Memory Class " + am.getMemoryClass());
    }

    private void createCrossHair() {
        final mil.emp3.api.MapView mapView = (mil.emp3.api.MapView) MainActivity.this.findViewById(R.id.map);
        final ImageView crossHairImage = (ImageView) MainActivity.this.findViewById(R.id.CrossHairImage);
        final RelativeLayout crossHairLayout = (RelativeLayout) MainActivity.this.findViewById(R.id.CrossHairLayout);
        int lineLength = getResources().getDisplayMetrics().densityDpi / 2;
        final int mapWidth = mapView.getWidth();
        final int mapHeight = mapView.getHeight();
        int centerX = mapWidth / 2;
        int centerY = mapHeight / 2;
        Bitmap bitmap = Bitmap.createBitmap(lineLength, lineLength, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        crossHairImage.setImageBitmap(bitmap);

        // Line
        Paint paint = new Paint();
        paint.setColor(Color.rgb(255, 255, 0));
        paint.setStrokeWidth(3);
        canvas.drawLine(lineLength / 2, 0, lineLength / 2, lineLength, paint);
        canvas.drawLine(0, lineLength / 2, lineLength, lineLength / 2, paint);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int posX = mapWidth / 2 + (int) mapView.getX() - (crossHairImage.getWidth() / 2);
                int posY = mapHeight / 2 + (int) mapView.getY() - (crossHairImage.getHeight() / 2);

                MainActivity.this.bCrossHairsOn = true;
                crossHairLayout.setX(posX);
                crossHairLayout.setY(posY);
                crossHairLayout.setVisibility(View.VISIBLE);
            }
        }, 500);
    }

    private void setEventListeners() {
        map.setFarDistanceThreshold(2000000.0);
        map.setMidDistanceThreshold(1900000.0);
        try {
            userInteraction = map.addMapInteractionEventListener(new MapInteractionEventListener());
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapInteractionEventListener failed. " + e.toString());
        }
        try {
            featureInteraction = map.addFeatureInteractionEventListener(new FeatureInteractionEventListener());
        } catch (EMP_Exception e) {
            Log.e(TAG, "addFeatureInteractionEventListener failed. " + e.toString());
        }

        try {
            viewChange = map.addMapViewChangeEventListener(new IMapViewChangeEventListener(){
                @Override
                public void onEvent(MapViewChangeEvent mapViewChangeEvent) {
                    Log.d(TAG, "mapViewChangeEvent");
                    if (mapViewChangeEvent.getCamera().getGeoId().compareTo(MainActivity.this.oCamera.getGeoId()) == 0) {
                        //oCamera.copySettingsFrom(mapViewChangeEvent.getCamera());
                    }
                    if (mapViewChangeEvent.getLookAt().getGeoId().compareTo(MainActivity.this.oLookAt.getGeoId()) == 0) {
                        //oCamera.copySettingsFrom(mapViewChangeEvent.getCamera());
                    }
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ICamera camera = MainActivity.this.oCamera;
                            if (MainActivity.this.CoordToast != null) {
                                MainActivity.this.CoordToast.cancel();
                                MainActivity.this.CoordToast = null;
                            }
                            MainActivity.this.CoordToast = Toast.makeText(MainActivity.this, "Center Latitude: " + camera.getLatitude() + " Longitude: " +
                                    camera.getLongitude(), Toast.LENGTH_LONG);
                            MainActivity.this.CoordToast.show();
                        }
                    });
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapViewChangeEventListener failed. " + e.toString());
        }

        try {
            cameraHandler = oCamera.addCameraEventListener(new ICameraEventListener(){
                @Override
                public void onEvent(CameraEvent cameraEvent) {
                    //Log.d(TAG, "cameraEvent on " + cameraEvent.getCamera().getName());

                    final ICamera oCamera = cameraEvent.getTarget();

                    if (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId()) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                TextView oCameraSetting = (TextView) MainActivity.this.findViewById(R.id.camerasettings);
                                String sTemp = String.format("Lat:%7.5f\u00b0 Lon:%8.5f\u00b0 Alt:%,.0f m Heading:%3.0f\u00b0 Tilt:%3.0f\u00b0 Roll:%3.0f\u00b0", oCamera.getLatitude(), oCamera.getLongitude(), oCamera.getAltitude(), oCamera.getHeading(), oCamera.getTilt(), oCamera.getRoll());
                                oCameraSetting.setText(sTemp);
                            }
                        });
                    } else {
                        TextView oCameraSetting = (TextView) MainActivity.this.findViewById(R.id.camerasettings);
                        String sTemp = String.format("Lat:%7.5f\u00b0 Lon:%8.5f\u00b0 Alt:%,.0f m Heading:%3.0f\u00b0 Tilt:%3.0f\u00b0 Roll:%3.0f\u00b0", oCamera.getLatitude(), oCamera.getLongitude(), oCamera.getAltitude(), oCamera.getHeading(), oCamera.getTilt(), oCamera.getRoll());
                        oCameraSetting.setText(sTemp);
                    }
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addCameraEventListener failed. " + e.toString());
        }

        final TextView oFarField = (TextView) this.findViewById(R.id.farThreshold);

        if (oFarField != null) {
            oFarField.post(new Runnable() {
                @Override
                public void run() {
                    oFarField.setText(MainActivity.this.map.getFarDistanceThreshold() + " m");
                    ImageButton oFarUpBtn = (ImageButton) MainActivity.this.findViewById(R.id.farThresholdUp);
                    if (oFarUpBtn != null) {
                        oFarUpBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                double dValue = MainActivity.this.map.getFarDistanceThreshold();
                                dValue += 5000.0;
                                MainActivity.this.map.setFarDistanceThreshold(dValue);
                                dValue = MainActivity.this.map.getFarDistanceThreshold();
                                oFarField.setText(dValue + " m");
                            }
                        });
                    }
                    ImageButton oFarDownBtn = (ImageButton) MainActivity.this.findViewById(R.id.farThresholdDown);
                    if (oFarDownBtn != null) {
                        oFarDownBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                double dValue = MainActivity.this.map.getFarDistanceThreshold();
                                dValue -= 5000.0;
                                MainActivity.this.map.setFarDistanceThreshold(dValue);
                                dValue = MainActivity.this.map.getFarDistanceThreshold();
                                oFarField.setText(dValue + " m");
                            }
                        });
                    }
                }
            });
        }

        final TextView oMidField = (TextView) this.findViewById(R.id.midThreshold);

        if (oMidField != null) {
            oMidField.post(new Runnable() {
                @Override
                public void run() {
                    oMidField.setText(MainActivity.this.map.getMidDistanceThreshold() + " m");
                    ImageButton oMidUpBtn = (ImageButton) MainActivity.this.findViewById(R.id.midThresholdUp);
                    if (oMidUpBtn != null) {
                        oMidUpBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                double dValue = MainActivity.this.map.getMidDistanceThreshold();
                                dValue += 5000.0;
                                MainActivity.this.map.setMidDistanceThreshold(dValue);
                                dValue = MainActivity.this.map.getMidDistanceThreshold();
                                oMidField.setText(dValue + " m");
                            }
                        });
                    }
                    ImageButton oMidDownBtn = (ImageButton) MainActivity.this.findViewById(R.id.midThresholdDown);
                    if (oMidDownBtn != null) {
                        oMidDownBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                double dValue = MainActivity.this.map.getMidDistanceThreshold();
                                dValue -= 5000.0;
                                MainActivity.this.map.setMidDistanceThreshold(dValue);
                                dValue = MainActivity.this.map.getMidDistanceThreshold();
                                oMidField.setText(dValue + " m");
                            }
                        });
                    }
                }
            });
        }
        this.oPerformanceDlg = (RelativeLayout) this.findViewById(R.id.performance_dialog);
        this.oStartBtn = (Button) this.findViewById(R.id.prf_start);
        this.oStopBtn = (Button) this.findViewById(R.id.prf_stop);
        this.oCloseBtn = (Button) this.findViewById(R.id.prf_close);
        this.oCountTb = (EditText) this.findViewById(R.id.prf_count);
        this.oAffiliationCkb = (CheckBox) this.findViewById(R.id.prf_changeaffiliation);
        this.oBatchUpdateCkb = (CheckBox) this.findViewById(R.id.prf_batch);
        this.oResults = (TextView) this.findViewById(R.id.prf_results);

        if ((oStartBtn == null) || (oStopBtn == null) || (oCloseBtn == null) ||
                (oCountTb == null) || (oAffiliationCkb == null)) {
            Log.e(TAG, "Button not found.");
        }

        this.oStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.oStartBtn.setEnabled(false);
                MainActivity.this.oStopBtn.setEnabled(true);
                MainActivity.this.oCloseBtn.setEnabled(false);
                int iCount = Integer.parseInt(MainActivity.this.oCountTb.getText().toString());
                boolean bBatch = MainActivity.this.oBatchUpdateCkb.isChecked();
                boolean bAff = MainActivity.this.oAffiliationCkb.isChecked();

                MainActivity.this.oPerformanceTestThread = new PerformanceTestThread(MainActivity.this, iCount, bAff, bBatch);
                MainActivity.this.oPerformanceTestThread.start();
            }
        });

        this.oStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.this.oPerformanceTestThread != null) {
                    MainActivity.this.oPerformanceTestThread.stopTest();
                    MainActivity.this.oPerformanceTestThread = null;

                    MainActivity.this.oStartBtn.setEnabled(true);
                    MainActivity.this.oStopBtn.setEnabled(false);
                    MainActivity.this.oCloseBtn.setEnabled(true);
                }
            }
        });

        this.oCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MenuItem oItem = MainActivity.this.oMenu.findItem(R.id.action_tests);
                oItem.setEnabled(true);

                MainActivity.this.oPerformanceDlg.setVisibility(View.GONE);
                MainActivity.this.oPerformanceDlg.setEnabled(false);
            }
        });
        this.oResultTextView = (TextView) this.findViewById(R.id.resultText);
        this.oResultTextView.setMovementMethod(new ScrollingMovementMethod());
        this.oResultPanel = (LinearLayout) this.findViewById(R.id.resultPanel);

        ImageButton zoomOutButton = (ImageButton) findViewById(R.id.ZoomOut);
        if (zoomOutButton != null) {
            zoomOutButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ICamera camera = MainActivity.this.oCamera;
                    double initAltitude = camera.getAltitude();
                    if (initAltitude <= 1e8 / 1.2) {
                        initAltitude *= 1.2;
                        camera.setAltitude(initAltitude);
                        camera.apply(false);
                    } else {
                        //Toast.makeText(CustomActivity.this, "Can't zoom out any more, altitude " + initAltitude, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "Zoom out button not found");
        }
        // The Zoom+ button zooms 20% each time it is pressed
        // The altitude is limited to 1 km
        ImageButton zoomInButton = (ImageButton) findViewById(R.id.ZoomIn);
        if (zoomInButton != null) {
            zoomInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ICamera camera = MainActivity.this.oCamera;
                    double initAltitude = camera.getAltitude();
                    // lowest possible camera altitude set to 100 meters
                    if (initAltitude >= 120) {
                        initAltitude /= 1.2;
                        camera.setAltitude(initAltitude);
                        camera.apply(false);
                    } else {
                        //Toast.makeText(CustomActivity.this, "Can't zoom in any more, altitude " + initAltitude, Toast.LENGTH_LONG).show();
                    }
                }
            });
        } else {
            Log.e(TAG, "Zoom in button not found");
        }
        // Pan left turns the camera left 5 degrees
        // each time the button is pressed
        ImageButton panLeft = (ImageButton) findViewById(R.id.PanLeft);
        if (panLeft != null) {
            panLeft.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ICamera camera = MainActivity.this.oCamera;
                    try {
                        double dPan = camera.getHeading();

                        dPan -= 5.0;
                        if (dPan < 0.0) {
                            dPan += 360.0;
                        }

                        camera.setHeading(dPan);
                        camera.apply(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Pan left button not found");
        }
        // Pan right turns the camera right 5 degrees
        // each time the button is pressed
        ImageButton panRight = (ImageButton) findViewById(R.id.PanRight);
        if (panRight != null) {
            panRight.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ICamera camera = MainActivity.this.oCamera;
                    try {
                        double dPan = camera.getHeading();

                        dPan += 5.0;
                        if (dPan >= 360.0) {
                            dPan -= 360.0;
                        }

                        camera.setHeading(dPan);
                        camera.apply(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Pan right button not found");
        }

        // Tilt up another 5 degrees, within limits
        ImageButton tiltUp = (ImageButton) findViewById(R.id.TiltUp);
        if (tiltUp != null) {
            tiltUp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ICamera camera = MainActivity.this.oCamera;
                    try {
                        double dTilt = camera.getTilt();

                        if (dTilt <= 85.0) {
                            dTilt += 5;
                            camera.setTilt(dTilt);
                            camera.apply(false);
                        } else {
                            //Toast.makeText(CustomActivity.this, "Can't tilt any higher", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Tilt up button not found");
        }

        // Tilt down another 5 degrees, within limits
        ImageButton tiltDown = (ImageButton) findViewById(R.id.TiltDown);
        if (tiltDown != null) {
            tiltDown.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ICamera camera = MainActivity.this.oCamera;
                    try {
                        double dTilt = camera.getTilt();

                        if (dTilt >= -85.0) {
                            dTilt -= 5;
                            camera.setTilt(dTilt);
                            camera.apply(false);
                        } else {
                            //Toast.makeText(CustomActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Tilt down button not found");
        }

        ImageButton rollCCW = (ImageButton) findViewById(R.id.rollCCW);
        if (rollCCW != null) {
            rollCCW.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    ICamera camera = MainActivity.this.oCamera;
                    try {
                        double dRoll = camera.getRoll();

                        if (dRoll <= 175.0) {
                            dRoll += 5;
                            camera.setRoll(dRoll);
                            camera.apply(false);
                        } else {
                            //Toast.makeText(CustomActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        ImageButton rollCW = (ImageButton) findViewById(R.id.rollCW);
        if (rollCW != null) {
            rollCW.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    ICamera camera = MainActivity.this.oCamera;
                    try {
                        double dRoll = camera.getRoll();

                        if (dRoll >= -175.0) {
                            dRoll -= 5;
                            camera.setRoll(dRoll);
                            camera.apply(false);
                        } else {
                            //Toast.makeText(CustomActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.oMenu = menu;

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_iconStyleFillColorDefault).setChecked(true);
        menu.findItem(R.id.action_iconStyleFillPatternDefault).setChecked(true);

        menu.findItem(R.id.action_iconStyleStrokeColorDefault).setChecked(true);
        menu.findItem(R.id.action_iconStyleStrokePatternDefault).setChecked(true);

        menu.findItem(R.id.action_iconStyleLabelColorDefault).setChecked(true);
        menu.findItem(R.id.action_iconStyleLabelJustificationDefault).setChecked(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_plot2units2525B:
                removeAllFeatures();
                plotManyMilStd(10000);
                return true;
            case R.id.action_plotunits2525B:
                removeAllFeatures();
                plotMilStd(org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525B, 0);
                return true;
            case R.id.action_plotunits2525C:
                removeAllFeatures();
                plotMilStd(org.cmapi.primitives.GeoMilSymbol.SymbolStandard.MIL_STD_2525C, 0);
                return true;
            case R.id.action_removeAll:
                removeAllFeatures();
                return true;
            case R.id.action_exit:
                Emp3LifeCycleManager.onSaveInstanceState(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    this.finishAndRemoveTask();
                } else {
                    this.finish();
                }
                return true;
            case R.id.action_addWMS:
                try {
                    map.addMapService(this.wmsService);
                    MenuItem oItem = this.oMenu.findItem(R.id.action_removeWMS);
                    oItem.setEnabled(true);
                    oItem = this.oMenu.findItem(R.id.action_addWMS);
                    oItem.setEnabled(false);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_removeWMS:
                try {
                    map.removeMapService(this.wmsService);
                    MenuItem oItem = this.oMenu.findItem(R.id.action_removeWMS);
                    oItem.setEnabled(false);
                    oItem = this.oMenu.findItem(R.id.action_addWMS);
                    oItem.setEnabled(true);
                } catch (EMP_Exception ex) {
                }
                return true;
/*
            case R.id.action_plotsymbol:
                if (this.ePlotMode == PlotModeEnum.IDLE) {
                    if (this.oCurrentSelectedFeature == null) {
                        this.ePlotMode = PlotModeEnum.NEW;
                    } else {
                        this.ePlotMode = PlotModeEnum.EDIT_PROPERTIES;
                    }
                    openFeatureProperties();
                }
                return true;
*/
            case R.id.action_plotPoint:
                try {
                    List<IGeoPosition> oPosList = this.getCameraPosition();
                    Point oPoint = new Point();

                    oPoint.setPosition(oPosList.get(0));
                    this.oRootOverlay.addFeature(oPoint, true);

                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_plotUrlPoint:
                try {
                    List<IGeoPosition> oPosList = this.getCameraPosition();
                    Point oPoint = new Point();
                    IGeoIconStyle oIconStyle = new GeoIconStyle();

                    oPoint.setPosition(oPosList.get(0));
                    oPoint.setIconURI("http://maps.google.com/mapfiles/kml/pushpin/ylw-pushpin.png");
                    oIconStyle.setOffSetY(0);
                    oIconStyle.setOffSetX(20);
                    oPoint.setIconStyle(oIconStyle);
                    this.oRootOverlay.addFeature(oPoint, true);

                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_iconsizetiny:
                try {
                    this.map.setIconSize(IconSizeEnum.TINY);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_iconsizesmall:
                try {
                    this.map.setIconSize(IconSizeEnum.SMALL);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_iconsizemedium:
                try {
                    this.map.setIconSize(IconSizeEnum.MEDIUM);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_iconsizelarge:
                try {
                    this.map.setIconSize(IconSizeEnum.LARGE);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_requiredLabels:
                try {
                    this.map.setMilStdLabels(MilStdLabelSettingEnum.REQUIRED_LABELS);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_commonLabels:
                try {
                    this.map.setMilStdLabels(MilStdLabelSettingEnum.COMMON_LABELS);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_allLabels:
                try {
                    this.map.setMilStdLabels(MilStdLabelSettingEnum.ALL_LABELS);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_performanceTest: {
                MenuItem oItem = this.oMenu.findItem(R.id.action_tests);
                oItem.setEnabled(false);

                if (this.oPerformanceDlg != null) {
                    this.oPerformanceDlg.setVisibility(View.VISIBLE);
                    this.oPerformanceDlg.setEnabled(true);

                    this.oStartBtn.setEnabled(true);
                    this.oStopBtn.setEnabled(false);
                    this.oCloseBtn.setEnabled(true);
                }

                return true;
            }
            case R.id.action_freehandDraw: {
                try {
                    IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                    IGeoColor geoColor = new EmpGeoColor(1.0, 255, 255, 0);

                    strokeStyle.setStrokeColor(geoColor);
                    strokeStyle.setStrokeWidth(5);

                    this.map.drawFreehand(strokeStyle, new IFreehandEventListener() {
                        private int iPosCnt = 0;

                        @Override
                        public void onEnterFreeHandDrawMode(IMap map) {
                            Log.d(TAG, "Enter Freehand Draw Mode.");
                            MainActivity.this.oResultTextView.setText("Start Draw Points.\n");
                            MainActivity.this.oResultTextView.scrollTo(0, 0);
                        }

                        @Override
                        public void onFreeHandLineDrawStart(IMap map, IGeoPositionGroup positionList) {
                            Log.d(TAG, "Freehand Draw Start.");
                            IGeoPosition oPos;
                            StringBuilder stringBuilder = new StringBuilder();

                            stringBuilder.append("Start Draw Points.\n");
                            iPosCnt = 0;
                            int iCurrentSize = positionList.getPositions().size();
                            for (int iIndex = iPosCnt; iIndex < iCurrentSize; iIndex++) {
                                oPos = positionList.getPositions().get(iIndex);
                                stringBuilder.append(String.format("[%d] Lat: %2$8.5f Lon: %3$8.5f\n", iIndex, oPos.getLatitude(), oPos.getLongitude()));
                            }

                            MainActivity.this.oResultTextView.setText(stringBuilder.toString());
                            MainActivity.this.oResultTextView.scrollTo(0, 0);
                            iPosCnt = iCurrentSize;
                        }

                        @Override
                        public void onFreeHandLineDrawUpdate(IMap map, IGeoPositionGroup positionList) {
                            Log.d(TAG, "Freehand Draw Update.");
                            IGeoPosition oPos;
                            StringBuilder stringBuilder = new StringBuilder();

                            stringBuilder.append("");
                            int iCurrentSize = positionList.getPositions().size();
                            for (int iIndex = iPosCnt; iIndex < iCurrentSize; iIndex++) {
                                oPos = positionList.getPositions().get(iIndex);
                                stringBuilder.append(String.format("[%d] Lat: %2$8.5f Lon: %3$8.5f\n", iIndex, oPos.getLatitude(), oPos.getLongitude()));
                            }

                            MainActivity.this.oResultTextView.append(stringBuilder.toString());
                            final int scrollAmount = MainActivity.this.oResultTextView.getLayout().getLineTop(MainActivity.this.oResultTextView.getLineCount()) - MainActivity.this.oResultTextView.getHeight();
                            if (scrollAmount > 0)
                                MainActivity.this.oResultTextView.scrollTo(0, scrollAmount);
                            else
                                MainActivity.this.oResultTextView.scrollTo(0, 0);
                            iPosCnt = iCurrentSize;
                        }

                        @Override
                        public void onFreeHandLineDrawEnd(IMap map, IGeoStrokeStyle style, IGeoPositionGroup positionList) {
                            Log.d(TAG, "Freehand Draw Complete.");
                            IGeoPosition oPos;
                            StringBuilder stringBuilder = new StringBuilder();

                            stringBuilder.append("");
                            int iCurrentSize = positionList.getPositions().size();
                            for (int iIndex = iPosCnt; iIndex < iCurrentSize; iIndex++) {
                                oPos = positionList.getPositions().get(iIndex);
                                stringBuilder.append(String.format("[%d] Lat: %2$8.5f Lon: %3$8.5f\n", iIndex, oPos.getLatitude(), oPos.getLongitude()));
                            }

                            MainActivity.this.oResultTextView.append(stringBuilder.toString());
                            iPosCnt = iCurrentSize;
                        }

                        @Override
                        public void onExitFreeHandDrawMode(IMap map) {
                            Log.d(TAG, "Exit Freehand Draw mode.");
                        }

                        @Override
                        public void onDrawError(IMap map, String errorMessage) {

                        }
                    });

                    MenuItem oItem = this.oMenu.findItem(R.id.action_tests);
                    oItem.setEnabled(false);

                    oItem = this.oMenu.findItem(R.id.action_freehandDrawExit);
                    oItem.setEnabled(true);

                    if (this.oResultPanel != null) {
                        this.oResultPanel.setVisibility(View.VISIBLE);
                        this.oResultPanel.setEnabled(true);
                    }
                } catch (EMP_Exception e) {
                    Log.e(TAG, "ERROR: " + e.getMessage(), e);
                }

                return true;
            }
            case R.id.action_freehandDrawExit: {

                MenuItem oItem = this.oMenu.findItem(R.id.action_tests);
                oItem.setEnabled(true);

                oItem = this.oMenu.findItem(R.id.action_freehandDrawExit);
                oItem.setEnabled(false);

                if (this.oResultPanel != null) {
                    this.oResultPanel.setVisibility(View.GONE);
                    this.oResultPanel.setEnabled(false);
                }
                try {
                    this.map.drawFreehandExit();
                } catch(EMP_Exception Ex) {

                }

                return true;
            }
            case R.id.action_drawText: {
                IGeoLabelStyle labelStyle = new GeoLabelStyle();
                mil.emp3.api.Text textFeature = new mil.emp3.api.Text();

                labelStyle.setColor(new EmpGeoColor(1.0, 0, 255, 255));
                labelStyle.setJustification(IGeoLabelStyle.Justification.CENTER);
                textFeature.setLabelStyle(labelStyle);
                //textFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

                try {
                    this.map.drawFeature(textFeature, new FeatureDrawListener(textFeature));
                } catch(EMP_Exception Ex) {
                    Log.e(TAG, "Draw Text failed.");
                    //oItem.setEnabled(true);
                }

                return true;
            }
            case R.id.action_drawLine: {
                IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
                Path linePath = new Path();

                strokeStyle.setStrokeColor(geoColor);
                strokeStyle.setStrokeWidth(5);
                strokeStyle.setStrokePattern(IGeoStrokeStyle.StrokePattern.dashed);
                linePath.setStrokeStyle(strokeStyle);
                //linePath.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

                try {
                    this.map.drawFeature(linePath, new FeatureDrawListener(linePath));
                } catch(EMP_Exception Ex) {
                    Log.e(TAG, "Draw line failed.");
                    //oItem.setEnabled(true);
                }

                return true;
            }
            case R.id.action_drawPolygon: {
                IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                IGeoFillStyle fillStyle = new GeoFillStyle();
                IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
                IGeoColor geoFillColor = new EmpGeoColor(0.7, 0, 0, 255);
                Polygon polygon = new Polygon();

                strokeStyle.setStrokeColor(geoColor);
                strokeStyle.setStrokeWidth(5);
                strokeStyle.setStrokePattern(IGeoStrokeStyle.StrokePattern.dotted);
                polygon.setStrokeStyle(strokeStyle);

                fillStyle.setFillColor(geoFillColor);
                fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
                polygon.setFillStyle(fillStyle);

                //polygon.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.map.drawFeature(polygon, new FeatureDrawListener(polygon));
                } catch(EMP_Exception Ex) {
                    Log.e(TAG, "Draw ploygon failed.");
                    //oItem.setEnabled(true);
                }

                return true;
            }
            case R.id.action_drawMilStdIcon: {
                if (this.ePlotMode == PlotModeEnum.IDLE) {
                    this.ePlotMode = PlotModeEnum.NEW;
                    openFeatureProperties();
                }
                return true;
            }
            case R.id.action_drawTG: {
                if (this.ePlotMode == PlotModeEnum.IDLE) {
                    this.ePlotMode = PlotModeEnum.DRAW_FEATURE;
                    openTGProperties();
                }
                return true;
            }
            case R.id.action_getMapState: {
                // 1. Instantiate an AlertDialog.Builder with its constructor
                AlertDialog.Builder builder = new AlertDialog.Builder(this);

                // 2. Chain together various setter methods to set the dialog characteristics
                builder.setMessage("The Map State is: " + this.map.getState().name())
                        .setTitle("Map State");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        dialog.dismiss();
                    }
                });

                // 3. Get the AlertDialog from create()
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
            case R.id.action_clearselected: {
                int prevCount = this.map.getSelected().size();

                this.map.clearSelected();
                for(FeatureLocationDialog oDialog: this.oSelectedDialogHash.values()) {
                    oDialog.dismiss();
                }
                this.oSelectedDialogHash.clear();

                Log.i(TAG, "Clear Map selected. The map had " + prevCount + " and now has " + this.map.getSelected().size() + ".");
                return true;
            }
            case R.id.action_swapMap: {
                final IEmpPropertyList properties = new EmpPropertyList();
                properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.worldwind.MapInstance");
                properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.worldwind");

                try {
                    map.swapMapEngine(properties);
                } catch(EMP_Exception e) {
                    Log.e(TAG, "Swap Engine failed: " + e, e);
                }
                return true;
            }
            case R.id.action_toggleCrossHairs: {
                RelativeLayout crossHairLayout = (RelativeLayout) this.findViewById(R.id.CrossHairLayout);
                ImageView crossHairImage = (ImageView) this.findViewById(R.id.CrossHairImage);

                if (this.bCrossHairsOn) {
                    // Turn them off.
                    this.bCrossHairsOn = false;
                    crossHairLayout.setVisibility(View.GONE);
                } else {
                    // Turn them on.
                    mil.emp3.api.MapView mapView = (mil.emp3.api.MapView) this.findViewById(R.id.map);
                    int mapWidth = mapView.getWidth();
                    int mapHeight = mapView.getHeight();
                    int posX = mapWidth / 2 + (int) mapView.getX() - (crossHairImage.getWidth() / 2);
                    int posY = mapHeight / 2 + (int) mapView.getY() - (crossHairImage.getHeight() / 2);

                    this.bCrossHairsOn = true;
                    crossHairLayout.setVisibility(View.VISIBLE);
                    crossHairLayout.setX(posX);
                    crossHairLayout.setY(posY);
                }

                return true;
            }

            case R.id.action_iconStyleFillColorDefault: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleFillColorDefault);
                menuItem.setChecked(true);
                iconStyleFill.setFillColor(new GeoColor());
                break;
            }
            case R.id.action_iconStyleFillColorRed: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleFillColorRed);
                menuItem.setChecked(true);
                iconStyleFill.setFillColor(new EmpGeoColor(1, 255, 0, 0));
                break;
            }
            case R.id.action_iconStyleFillColorGreen: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleFillColorGreen);
                menuItem.setChecked(true);
                iconStyleFill.setFillColor(new EmpGeoColor(1, 0, 255, 0));
                break;
            }
            case R.id.action_iconStyleFillColorBlue: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleFillColorBlue);
                menuItem.setChecked(true);
                iconStyleFill.setFillColor(new EmpGeoColor(1, 0, 0, 255));
                break;
            }

            case R.id.action_iconStyleFillPatternDefault: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleFillPatternDefault);
                menuItem.setChecked(true);
                iconStyleFill.setFillPattern(IGeoFillStyle.FillPattern.solid);
                break;
            }
            case R.id.action_iconStyleFillPatternSolid: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleFillPatternSolid);
                menuItem.setChecked(true);
                iconStyleFill.setFillPattern(IGeoFillStyle.FillPattern.solid);
                break;
            }
            case R.id.action_iconStyleFillPatternHatched: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleFillPatternHatched);
                menuItem.setChecked(true);
                iconStyleFill.setFillPattern(IGeoFillStyle.FillPattern.hatched);
                break;
            }
            case R.id.action_iconStyleFillPatternCrossHatched: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleFillPatternCrossHatched);
                menuItem.setChecked(true);
                iconStyleFill.setFillPattern(IGeoFillStyle.FillPattern.crossHatched);
                break;
            }

            case R.id.action_iconStyleStrokeColorDefault: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokeColorDefault);
                menuItem.setChecked(true);
                iconStyleStroke.setStrokeColor(new GeoColor());
                break;
            }
            case R.id.action_iconStyleStrokeColorRed: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokeColorRed);
                menuItem.setChecked(true);
                iconStyleStroke.setStrokeColor(new EmpGeoColor(1, 255, 0, 0));
                break;
            }
            case R.id.action_iconStyleStrokeColorGreen: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokeColorGreen);
                menuItem.setChecked(true);
                iconStyleStroke.setStrokeColor(new EmpGeoColor(1, 0, 255, 0));
                break;
            }
            case R.id.action_iconStyleStrokeColorBlue: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokeColorBlue);
                menuItem.setChecked(true);
                iconStyleStroke.setStrokeColor(new EmpGeoColor(1, 0, 0, 255));
                break;
            }

            case R.id.action_iconStyleStrokePatternDefault: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokePatternDefault);
                menuItem.setChecked(true);
                iconStyleStroke.setStrokePattern(IGeoStrokeStyle.StrokePattern.solid);
                break;
            }
            case R.id.action_iconStyleStrokePatternSolid: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokePatternSolid);
                menuItem.setChecked(true);
                iconStyleStroke.setStrokePattern(IGeoStrokeStyle.StrokePattern.solid);
                break;
            }
            case R.id.action_iconStyleStrokePatternDashed: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokePatternDashed);
                menuItem.setChecked(true);
                iconStyleStroke.setStrokePattern(IGeoStrokeStyle.StrokePattern.dashed);
                break;
            }
            case R.id.action_iconStyleStrokePatternDotted: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokePatternDotted);
                menuItem.setChecked(true);
                iconStyleStroke.setStrokePattern(IGeoStrokeStyle.StrokePattern.dotted);
                break;
            }

            case R.id.action_iconStyleLabelColorGroup: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleLabelColorGroup);
                menuItem.setChecked(true);
                iconStyleLabel.setColor(new GeoColor());
                break;
            }
            case R.id.action_iconStyleLabelColorRed: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleLabelColorRed);
                menuItem.setChecked(true);
                iconStyleLabel.setColor(new EmpGeoColor(1, 255, 0, 0));
                break;
            }
            case R.id.action_iconStyleLabelColorGreen: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleLabelColorGreen);
                menuItem.setChecked(true);
                iconStyleLabel.setColor(new EmpGeoColor(1, 0, 255, 0));
                break;
            }
            case R.id.action_iconStyleLabelColorBlue: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleLabelColorBlue);
                menuItem.setChecked(true);
                iconStyleLabel.setColor(new EmpGeoColor(1, 0, 0, 255));
                break;
            }

            case R.id.action_iconStyleLabelJustificationDefault: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleLabelJustificationDefault);
                menuItem.setChecked(true);
                iconStyleLabel.setJustification(IGeoLabelStyle.Justification.LEFT);
                break;
            }
            case R.id.action_iconStyleLabelJustificationLeft: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleLabelJustificationLeft);
                menuItem.setChecked(true);
                iconStyleLabel.setJustification(IGeoLabelStyle.Justification.LEFT);
                break;
            }
            case R.id.action_iconStyleLabelJustificationCenter: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleLabelJustificationCenter);
                menuItem.setChecked(true);
                iconStyleLabel.setJustification(IGeoLabelStyle.Justification.CENTER);
                break;
            }
            case R.id.action_iconStyleLabelJustificationRight: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleLabelJustificationRight);
                menuItem.setChecked(true);
                iconStyleLabel.setJustification(IGeoLabelStyle.Justification.RIGHT);
                break;
            }
            case R.id.action_plotText: {
                mil.emp3.api.Text textFeature;
                IGeoLabelStyle labelStyle;
                IGeoPosition pos1 = new GeoPosition();
                IGeoPosition pos2 = new GeoPosition();
                IGeoPosition pos3 = new GeoPosition();

                pos2.setLatitude(this.oCamera.getLatitude());
                pos2.setLongitude(this.oCamera.getLongitude());
                pos2.setAltitude(0);

                pos1.setLatitude(pos2.getLatitude() + 0.5);
                pos1.setLongitude(pos2.getLongitude());
                pos1.setAltitude(0);

                pos3.setLatitude(pos2.getLatitude() - 0.5);
                pos3.setLongitude(pos2.getLongitude());
                pos3.setAltitude(0);

                labelStyle = new GeoLabelStyle();
                labelStyle.setColor(new EmpGeoColor(1.0, 0, 255, 255));
                labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
                textFeature = new mil.emp3.api.Text();
                textFeature.setName("This Text is LEFT Justified.");
                textFeature.setLabelStyle(labelStyle);
                textFeature.setPosition(pos1);
                //textFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.oRootOverlay.addFeature(textFeature, true);
                    this.oFeatureHash.put(textFeature.getGeoId(), textFeature);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }

                labelStyle = new GeoLabelStyle();
                labelStyle.setColor(new EmpGeoColor(1.0, 0, 255, 255));
                labelStyle.setJustification(IGeoLabelStyle.Justification.CENTER);
                textFeature = new mil.emp3.api.Text();
                textFeature.setName("This Text is CENTER Justified.");
                textFeature.setLabelStyle(labelStyle);
                textFeature.setPosition(pos2);
                //textFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.oRootOverlay.addFeature(textFeature, true);
                    this.oFeatureHash.put(textFeature.getGeoId(), textFeature);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }

                labelStyle = new GeoLabelStyle();
                labelStyle.setColor(new EmpGeoColor(1.0, 0, 255, 255));
                labelStyle.setJustification(IGeoLabelStyle.Justification.RIGHT);
                textFeature = new mil.emp3.api.Text();
                textFeature.setName("This Text is RIGHT Justified.");
                textFeature.setLabelStyle(labelStyle);
                textFeature.setPosition(pos3);
                //textFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.oRootOverlay.addFeature(textFeature, true);
                    this.oFeatureHash.put(textFeature.getGeoId(), textFeature);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }


                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void openTGProperties() {
        TacticalGraphicPropertiesDialog oDialog = new TacticalGraphicPropertiesDialog();

        switch (this.ePlotMode) {
            case NEW:
                break;
            case EDIT_PROPERTIES:
                break;
            case DRAW_FEATURE: {
                oDialog.show(this.getFragmentManager(), "Tactical Graphic Properties");
                break;
            }
        }
    }

    @Override
    public void onSaveClick(TacticalGraphicPropertiesDialog dialog) {
        switch (this.ePlotMode) {
            case NEW:
                break;
            case EDIT_PROPERTIES:
                break;
            case DRAW_FEATURE: {
                MilStdSymbol oSymbol = null;
                armyc2.c2sd.renderer.utilities.SymbolDef symbolDef = dialog.getCurrentDef();
                String modifiers = symbolDef.getModifiers();
                try {
                    oSymbol = new MilStdSymbol(dialog.getMilStdVersion(), dialog.getSymbolCode());

                    oSymbol.setSymbolCode(dialog.getSymbolCode());
                    oSymbol.setName(dialog.getFeatureName());
                    //oSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

                    switch (SymbolUtilities.getBasicSymbolID(dialog.getSymbolCode())) {
                        case "G*G*ALC---****X": // AIR_CORRIDOR:
                            switch (dialog.getMilStdVersion()) {
                                case MIL_STD_2525C:
                                    oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymbol.getName());
                                    oSymbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 0, 10000);
                                    oSymbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 1, 20000);
                                    oSymbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP, "201000Z");
                                    oSymbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP_2, "231000Z");
                                    break;
                                case MIL_STD_2525B:
                                    oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymbol.getName());
                                    oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_2, "UD 2");
                                    break;
                            }
                            break;
                        case "G*G*GLB---****X": // BOUNDARIES
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "10 Mtn Div");
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_2, "3 Inf Div");
                            oSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.DIVISION);
                            break;
                        case "G*G*GLP---****X": // PHASE LINE
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymbol.getName());
                            break;
                        case "G*G*OAO---****X": // OBJECTIVE
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymbol.getName());
                            break;
                        case "G*G*OPP---****X": // POINT OF DEPARTURE
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_1, "Info 1");
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymbol.getName());
                            oSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.COMPANY_BATTERY_TROOP);
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP, "201000Z");
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP_2, "231000Z");
                            break;
                        case "G*S*LRM---****X": // MAIN SUPPLY ROUTE
                        case "G*S*LRA---****X": // ALTERNATE SUPPLY ROUTE
                        case "G*G*OAK---****X": // ATTACK POSITION
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymbol.getName());
                            break;
                        case "G*G*GAA---****X": // ASSEMBLY AREA
                            oSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymbol.getName());
                            oSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.COMPANY_BATTERY_TROOP);
                            break;
                    }

                    this.map.drawFeature(oSymbol, new FeatureDrawListener(oSymbol));
                } catch (EMP_Exception e) {
                    Log.d(TAG, "Cant draw " + dialog.getSymbolCode(), e);
                    this.ePlotMode = PlotModeEnum.IDLE;
                }
                break;
            }
        }
    }

    @Override
    public void onCancelClick(TacticalGraphicPropertiesDialog dialog) {
        this.ePlotMode = PlotModeEnum.IDLE;
    }

    private void openFeatureProperties() {
        SymbolPropertiesDialog featurePropertiesDlg;

        switch (this.ePlotMode) {
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
                        this.ePlotMode =    PlotModeEnum.IDLE;
                    }
                }
                break;
        }
    }

    @Override
    public void onSymbolPropertiesSaveClick(SymbolPropertiesDialog oDialog) {
        armyc2.c2sd.renderer.utilities.UnitDef oUnitDef = oDialog.getCurrentUnitDef();
        try {
            //Log.d(TAG, "Symbol properties Save Btn");
            switch (this.ePlotMode) {
                case NEW:
                    java.util.List<IGeoPosition> oPositionList = new java.util.ArrayList<>();
                    //IGeoPosition oPosition;
                    //oPosition = new GeoPosition();
                    //oPosition.setLatitude(randomLocalLatitude());
                    //oPosition.setLongitude(randomLocalLongitude());
                    //oPosition.setAltitude(Math.random() * 1000000.0);
                    //oPositionList.add(oPosition);
                    final MilStdSymbol oNewSymbol = new MilStdSymbol(oDialog.getMilStdVersion(), oDialog.getSymbolCode());
                    //oNewSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                    if ((oDialog.getFeatureName() != null) && !oDialog.getFeatureName().isEmpty()) {
                        oNewSymbol.setName(oDialog.getFeatureName());
                    } else {
                        oNewSymbol.setName(null);
                    }
                    oNewSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oUnitDef.getDescription());

                    this.map.drawFeature(oNewSymbol, new IDrawEventListener() {
                        @Override
                        public void onDrawStart(IMap map) {
                            Log.d(TAG, "Draw Start");
                            MainActivity.this.ePlotMode = PlotModeEnum.DRAW_FEATURE;
                            MainActivity.this.oEditorCompleteBtn.show();
                            MainActivity.this.oEditorCancelBtn.show();
                            FeatureLocationDialog oDialog = new FeatureLocationDialog();
                            oDialog.setFeature(oNewSymbol);
                            oDialog.show(MainActivity.this.getFragmentManager(), null);
                            MainActivity.this.oSelectedDialogHash.put(oNewSymbol.getGeoId(), oDialog);
                        }

                        @Override
                        public void onDrawUpdate(IMap map, IFeature feature, List<IEditUpdateData> updateList) {
                            Log.d(TAG, "Draw Update");
                            if (MainActivity.this.oSelectedDialogHash.containsKey(feature.getGeoId())) {
                                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(feature.getGeoId());
                                oDialog.updateDialog();
                            }
                        }

                        @Override
                        public void onDrawComplete(IMap map, IFeature feature) {
                            Log.d(TAG, "Draw Complete");
                            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
                            MainActivity.this.oEditorCompleteBtn.hide();
                            MainActivity.this.oEditorCancelBtn.hide();
                            try {
                                MainActivity.this.oRootOverlay.addFeature(oNewSymbol, true);
                                MainActivity.this.oFeatureHash.put(oNewSymbol.getGeoId(), oNewSymbol);
                                if (MainActivity.this.oSelectedDialogHash.containsKey(feature.getGeoId())) {
                                    FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(feature.getGeoId());
                                    oDialog.updateDialog();
                                }
                                MainActivity.this.map.selectFeature(oNewSymbol);
                                MainActivity.this.oCurrentSelectedFeature = oNewSymbol;
                            } catch (EMP_Exception e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onDrawCancel(IMap map, IFeature originalFeature) {
                            Log.d(TAG, "Draw Cancel");
                            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
                            MainActivity.this.oEditorCompleteBtn.hide();
                            MainActivity.this.oEditorCancelBtn.hide();
                            if (MainActivity.this.oSelectedDialogHash.containsKey(originalFeature.getGeoId())) {
                                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(originalFeature.getGeoId());
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
                            this.ePlotMode = MainActivity.PlotModeEnum.IDLE;
                        }
                    }
                    break;
            }
        } catch (EMP_Exception ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            this.ePlotMode = PlotModeEnum.IDLE;
        }
    }

    @Override
    public void onSymbolPropertiesCancelClick(SymbolPropertiesDialog dialog) {
        Log.d(TAG, "Feature properties Cancel Btn");
        this.ePlotMode = MainActivity.PlotModeEnum.IDLE;
    }
}
