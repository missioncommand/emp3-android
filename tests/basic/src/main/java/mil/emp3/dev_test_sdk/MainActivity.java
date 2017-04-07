package mil.emp3.dev_test_sdk;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDefTable;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.UnitDef;
import armyc2.c2sd.renderer.utilities.UnitDefTable;
import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.Emp3LifeCycleManager;
import mil.emp3.api.GeoJSON;
import mil.emp3.api.GeoPackage;
import mil.emp3.api.ImageLayer;
import mil.emp3.api.KML;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.MirrorCache;
import mil.emp3.api.Overlay;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.WCS;
import mil.emp3.api.WMS;
import mil.emp3.api.WMTS;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MapGridTypeEnum;
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
import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IScreenCaptureCallback;
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
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.utils.kml.EmpKMLExporter;
import mil.emp3.core.utils.CoreMilStdUtilities;
import mil.emp3.dev_test_sdk.dialogs.FeatureLocationDialog;
import mil.emp3.dev_test_sdk.dialogs.MiniMapDialog;
import mil.emp3.dev_test_sdk.dialogs.milstdtacticalgraphics.TacticalGraphicPropertiesDialog;
import mil.emp3.dev_test_sdk.dialogs.milstdunits.SymbolPropertiesDialog;
import mil.emp3.dev_test_sdk.utils.CameraUtility;
import mil.emp3.json.geoJson.GeoJsonCaller;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;


public class MainActivity extends AppCompatActivity
        implements SymbolPropertiesDialog.SymbolPropertiesDialogListener,
        TacticalGraphicPropertiesDialog.SymbolPropertiesDialogListener {
    private final static String TAG = MainActivity.class.getSimpleName();
    private IMap map;
    protected Overlay oRootOverlay;
    private ICamera oCamera;
    private ILookAt oLookAt;
    private WCS wcsService;
    private WMS wmsService;
    private WMTS wmtsService;
    private mil.emp3.api.GeoPackage geoPackage;
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
    private Button oTrackBtn;
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
    private Handler handler;
    private MiniMapDialog miniMapDialog = null;

    private boolean swapMapFlag;

    private boolean bCrossHairsOn = false;
    private Toast CoordToast = null;

    private IGeoFillStyle iconStyleFill     = new GeoFillStyle();
    private IGeoStrokeStyle iconStyleStroke = new GeoStrokeStyle();
    private IGeoLabelStyle iconStyleLabel   = new GeoLabelStyle();

    private String[] gpkgList = null;
    private File downloadFolder = new File("/sdcard/Download/");
    private String gpkgPick;
    private static final String SUFFIX = ".gpkg";

    private MirrorCache mc;

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
                                oSPSymbol.getPositions().clear();
                                oSPSymbol.getPositions().addAll(oPosList);

                                //Set a single modifier.
                                oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymDef.getDescription());
                                iCount++;

                                // Give the feature a name.
                                oSPSymbol.setName(String.format("Unit-%04d", iCount));

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
                    oSPSymbol.getPositions().clear();
                    oSPSymbol.getPositions().addAll(oPosList);

                    //Set a single modifier.
                    oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymDef.getDescription());
                    iCount++;

                    // Give the feature a name.
                    oSPSymbol.setName(String.format("Unit-%04d", iCount));

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
        if (mc != null) {
            try {
                mc.disconnect();

            } catch (EMP_Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
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
        oCamera.setAltitude(100000.0);
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
                    MainActivity.this.oRootOverlay.setName("Test Overlay");
                    try {
                        if (event.getNewState() == MapStateEnum.MAP_READY) {
                            MainActivity.this.map.setName("EMP V3 Map");
                            MainActivity.this.map.addOverlay(MainActivity.this.oRootOverlay, true);
                            MainActivity.this.setEventListeners();

                            MainActivity.this.createCrossHair();
                            //Log.i(TAG, "Map State: " + MainActivity.this.map.getState().name());
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        MainActivity.this.map.setCamera(MainActivity.this.oCamera, false);
                                        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
                                        if (permissionCheck !=  PERMISSION_GRANTED) {
                                            ActivityCompat.requestPermissions(MainActivity.this,
                                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                        }
                                    } catch (EMP_Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            map.addCameraEventListener(new ICameraEventListener() {
                                @Override
                                public void onEvent(final CameraEvent event) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() { // to indicate that an imap camera event was triggered
                                            //Toast.makeText(MainActivity.this, "IMap.cameraEventListener.onEvent() triggered: " + event.getEvent(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
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
        String sTemp = String.format(Locale.US, "Lat:%7.5f\u00b0 Lon:%8.5f\u00b0 Alt:%.0f m Heading:%3.0f\u00b0 Tilt:%3.0f\u00b0 Roll:%3.0f\u00b0", oCamera.getLatitude(), oCamera.getLongitude(), oCamera.getAltitude(), oCamera.getHeading(), oCamera.getTilt(), oCamera.getRoll());
        oCameraSetting.setText(sTemp);

        java.util.List<String> oLayers = new java.util.ArrayList<>();

        oLayers.add("imagery_part2-2.0.0.1-wsmr.gpkg");
/*
        try {
            this.wmsService = new mil.emp3.api.WMS(
                    "http://172.16.20.99:5000/WmsServerStrict",
                    WMSVersionEnum.VERSION_1_3_0,
                    "image/png",
                    true,
                    oLayers
            );
            //this.wmsService.setLayerResolution(1.0);
            //oCamera.setLatitude(32.897);
            //oCamera.setLongitude(-105.939);
            //oCamera.setAltitude(500.0);
            //oCamera.apply(false);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
*/

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

        handler = new Handler(Looper.getMainLooper());
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
        map.setFarDistanceThreshold(20000.0);
        map.setMidDistanceThreshold(10000.0);
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
            viewChange = map.addMapViewChangeEventListener(new IMapViewChangeEventListener() {
                @Override
                public void onEvent(MapViewChangeEvent mapViewChangeEvent) {
                    Log.d(TAG, "mapViewChangeEvent");
                    if (mapViewChangeEvent.getCamera().getGeoId().compareTo(MainActivity.this.oCamera.getGeoId()) == 0) {
                        //oCamera.copySettingsFrom(mapViewChangeEvent.getCamera());
                    }
                    if (mapViewChangeEvent.getLookAt().getGeoId().compareTo(MainActivity.this.oLookAt.getGeoId()) == 0) {
                        //oCamera.copySettingsFrom(mapViewChangeEvent.getCamera());
                    }
/*
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
*/
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapViewChangeEventListener failed. " + e.toString());
        }

        try {
            cameraHandler = oCamera.addCameraEventListener(new ICameraEventListener() {
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
        this.oTrackBtn = (Button) this.findViewById(R.id.prf_track);
        this.oStopBtn = (Button) this.findViewById(R.id.prf_stop);
        this.oCloseBtn = (Button) this.findViewById(R.id.prf_close);
        this.oCountTb = (EditText) this.findViewById(R.id.prf_count);
        this.oAffiliationCkb = (CheckBox) this.findViewById(R.id.prf_changeaffiliation);
        this.oBatchUpdateCkb = (CheckBox) this.findViewById(R.id.prf_batch);
        this.oResults = (TextView) this.findViewById(R.id.prf_results);

        if ((oStartBtn == null) || (oStopBtn == null) || (oCloseBtn == null) ||
                (oCountTb == null) || (oAffiliationCkb == null) || (null == oTrackBtn)) {
            Log.e(TAG, "Button not found.");
        }

        this.oStartBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.oStartBtn.setEnabled(false);
                MainActivity.this.oTrackBtn.setEnabled(true);
                MainActivity.this.oStopBtn.setEnabled(true);
                MainActivity.this.oCloseBtn.setEnabled(false);
                int iCount = 5000;

                try {
                    iCount = Integer.parseInt(MainActivity.this.oCountTb.getText().toString());
                } catch (NumberFormatException ex) {
                    iCount = 5000;
                }
                boolean bBatch = MainActivity.this.oBatchUpdateCkb.isChecked();
                boolean bAff = MainActivity.this.oAffiliationCkb.isChecked();

                MainActivity.this.oPerformanceTestThread = new PerformanceTestThread(MainActivity.this, iCount, bAff, bBatch);
                MainActivity.this.oPerformanceTestThread.start();
            }
        });

        this.oTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.this.oPerformanceTestThread != null) {
                    MainActivity.this.oPerformanceTestThread.toggleTrackingMode();
                }
            }
        });

        this.oStopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.this.oPerformanceTestThread != null) {
                    MainActivity.this.oPerformanceTestThread.stopTest();
                    MainActivity.this.oPerformanceTestThread = null;

                    MainActivity.this.oStartBtn.setEnabled(true);
                    MainActivity.this.oTrackBtn.setEnabled(false);
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
        // The altitude is limited to 10 m
        ImageButton zoomInButton = (ImageButton) findViewById(R.id.ZoomIn);
        if (zoomInButton != null) {
            zoomInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ICamera camera = MainActivity.this.oCamera;
                    double initAltitude = camera.getAltitude();
                    // lowest possible camera altitude set to 10 meters
                    if (initAltitude > 10) {
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

                        if (dTilt < global.CAMERA_TILT_MAXIMUM) {
                            dTilt += 5;
                            camera.setTilt(dTilt);
                            camera.apply(false);
                        } else {
                            //Toast.makeText(CustomActivity.this, "Can't tilt any higher", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
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

                        if (dTilt > global.CAMERA_TILT_MINIMUM) {
                            dTilt -= 5;
                            camera.setTilt(dTilt);
                            camera.apply(false);
                        } else {
                            //Toast.makeText(CustomActivity.this, "Can't tilt any lower", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            });
        } else {
            Log.e(TAG, "Tilt down button not found");
        }

        ImageButton rollCCW = (ImageButton) findViewById(R.id.rollCCW);
        if (rollCCW != null) {
            rollCCW.setOnClickListener(new View.OnClickListener() {
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
            rollCW.setOnClickListener(new View.OnClickListener() {
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

        ImageButton resetCameraBtn = (ImageButton) findViewById(R.id.reset);
        if (resetCameraBtn != null) {
            resetCameraBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ICamera camera = MainActivity.this.oCamera;
                    try {
                        camera.setHeading(0);
                        camera.setTilt(0);
                        camera.setRoll(0);
                        camera.apply(false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        final TextView brightnessCtrl = (TextView) findViewById(R.id.brightnessValue);
        if (null != brightnessCtrl) {
            brightnessCtrl.setText("" + MainActivity.this.map.getBackgroundBrightness());

            ImageButton increaseBrightnessBtn = (ImageButton) findViewById(R.id.brightnessUp);
            if (null != increaseBrightnessBtn) {
                increaseBrightnessBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int value = Integer.parseInt(brightnessCtrl.getText().toString()) + 5;

                        if (value > 100) {
                            value = 100;
                        }
                        brightnessCtrl.setText("" + value);
                        MainActivity.this.map.setBackgroundBrightness(value);
                    }
                });
            }

            ImageButton decreaseBrightnessBtn = (ImageButton) findViewById(R.id.brightnessDown);
            if (null != decreaseBrightnessBtn) {
                decreaseBrightnessBtn.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        int value = Integer.parseInt(brightnessCtrl.getText().toString()) - 5;

                        if (value < 0) {
                            value = 0;
                        }
                        brightnessCtrl.setText("" + value);
                        MainActivity.this.map.setBackgroundBrightness(value);
                    }
                });
            }
        }
    }

    private void getGpkgList() {
        try {
            downloadFolder.mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (downloadFolder.exists()) {
            FilenameFilter filter = new FilenameFilter() {

                @Override
                public boolean accept(File dir, String filename) {
                    File sel = new File(dir, filename);
                    return filename.contains(SUFFIX) || sel.isDirectory();
                }

            };
            gpkgList = downloadFolder.list(filter);
        } else {
            gpkgList = new String[0];
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
            case R.id.action_mirrorcache_connect: {
                MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_connect).setEnabled(false);

                new AlertDialog.Builder(MainActivity.this)
                               .setMessage("WebSocket endpoint:")
                               .setView(R.layout.mirrorcache_url_dialog)
                               .setNegativeButton(android.R.string.no, null)
                               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int which) {
                                       final EditText text = (EditText) (((Dialog) dialog).findViewById(R.id.input));

                                       final String endpointStr = text.getText().toString();
                                       Log.d(TAG, "endpointStr: " + endpointStr);

                                       new AsyncTask<Void, Void, Exception>() {
                                           @Override
                                           protected Exception doInBackground(final Void... params) {
                                               try {
                                                   mc = new MirrorCache(new URI(endpointStr));
                                                   mc.connect();

                                               } catch (Exception e) {
                                                   return e;
                                               }
                                               return null;
                                           }
                                           @Override
                                           protected void onPostExecute(final Exception e) {
                                               if (e == null) {
                                                   MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_disconnect).setEnabled(true);
                                                   MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_subscribe).setEnabled(true);
                                                   MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_addproduct).setEnabled(true);

                                               } else if (e != null) {
                                                   Log.e(TAG, e.getMessage(), e);
                                                   new AlertDialog.Builder(MainActivity.this).setTitle("ERROR:").setMessage(e.getMessage()).create().show();
                                                   MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_connect).setEnabled(true);
                                               }
                                           }
                                       }.execute();
                                   }
                               }).create().show();
                return true;
            }
            case R.id.action_mirrorcache_disconnect: {
                MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_disconnect).setEnabled(false);

                try {
                    mc.disconnect();
                    MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_connect).setEnabled(true);

                } catch (EMP_Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    new AlertDialog.Builder(MainActivity.this).setTitle("ERROR:").setMessage(e.getMessage()).create().show();
                    MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_disconnect).setEnabled(true);
                }
                return true;
            }
            case R.id.action_mirrorcache_subscribe: {
                MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_subscribe).setEnabled(false);

                new AlertDialog.Builder(MainActivity.this)
                               .setMessage("Product Id:")
                               .setView(R.layout.mirrorcache_subscribe_dialog)
                               .setNegativeButton(android.R.string.no, null)
                               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int which) {
                                       final EditText text = (EditText) (((Dialog) dialog).findViewById(R.id.input));

                                       final String productId = text.getText().toString();
                                       Log.d(TAG, "productId: " + productId);

                                       new AsyncTask<Void, Void, EMP_Exception>() {
                                           @Override
                                           protected EMP_Exception doInBackground(final Void... params) {
                                               try {
                                                   final Overlay overlay = mc.subscribe(productId);
                                                   MainActivity.this.map.addOverlay(overlay, true);

                                               } catch (EMP_Exception e) {
                                                   return e;
                                               }
                                               return null;
                                           }
                                           @Override
                                           protected void onPostExecute(final EMP_Exception e) {
                                               if (e == null) {
                                                   MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_unsubscribe).setEnabled(true);

                                               } else if (e != null) {
                                                   Log.e(TAG, e.getMessage(), e);
                                                   new AlertDialog.Builder(MainActivity.this).setTitle("ERROR:").setMessage(e.getMessage()).create().show();
                                                   MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_subscribe).setEnabled(true);
                                               }
                                           }
                                       }.execute();
                                   }
                               }).create().show();
                return true;
            }
            case R.id.action_mirrorcache_unsubscribe: {
                MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_unsubscribe).setEnabled(false);

                new AlertDialog.Builder(MainActivity.this)
                               .setMessage("Product Id:")
                               .setView(R.layout.mirrorcache_subscribe_dialog)
                               .setNegativeButton(android.R.string.no, null)
                               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                   public void onClick(DialogInterface dialog, int which) {
                                       final EditText text = (EditText) (((Dialog) dialog).findViewById(R.id.input));

                                       final String productId = text.getText().toString();
                                       Log.d(TAG, "productId: " + productId);

                                       new AsyncTask<Void, Void, EMP_Exception>() {
                                           @Override
                                           protected EMP_Exception doInBackground(final Void... params) {
                                               try {
                                                   mc.unsubscribe(productId);

                                               } catch (EMP_Exception e) {
                                                   return e;
                                               }
                                               return null;
                                           }
                                           @Override
                                           protected void onPostExecute(final EMP_Exception e) {
                                               if (e == null) {
                                                   MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_subscribe).setEnabled(true);

                                               } else if (e != null) {
                                                   Log.e(TAG, e.getMessage(), e);
                                                   new AlertDialog.Builder(MainActivity.this).setTitle("ERROR:").setMessage(e.getMessage()).create().show();
                                                   MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_unsubscribe).setEnabled(true);
                                               }
                                           }
                                       }.execute();
                                   }
                               }).create().show();
                return true;
            }
            case R.id.action_mirrorcache_addproduct: {
                MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_addproduct).setEnabled(false);

                try {
                    mc.addProduct(MainActivity.this.oRootOverlay);
                    MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_deleteproduct).setEnabled(true);

                } catch (EMP_Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    new AlertDialog.Builder(MainActivity.this).setTitle("ERROR:").setMessage(e.getMessage()).create().show();
                    MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_addproduct).setEnabled(true);
                }
                return true;
            }
            case R.id.action_mirrorcache_deleteproduct: {
                MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_deleteproduct).setEnabled(false);

                try {
                    mc.removeProduct(MainActivity.this.oRootOverlay, false);
                    MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_addproduct).setEnabled(true);

                } catch (EMP_Exception e) {
                    Log.e(TAG, e.getMessage(), e);
                    new AlertDialog.Builder(MainActivity.this).setTitle("ERROR:").setMessage(e.getMessage()).create().show();
                    MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_deleteproduct).setEnabled(false);
                }
                return true;
            }
            case R.id.action_mapgridnone: {
                this.map.setGridType(MapGridTypeEnum.NONE);
                return true;
            }
            case R.id.action_mapgridmgrs: {
                this.map.setGridType(MapGridTypeEnum.MGRS);
                return true;
            }
            case R.id.action_mapgridutm: {
                try {
                    this.map.setGridType(MapGridTypeEnum.UTM);
                } catch (Exception Ex) {
                    Log.e(TAG, "setGridType failed.", Ex);
                }
                return true;
            }
            case R.id.action_mapgridDMS: {
                try {
                    this.map.setGridType(MapGridTypeEnum.DMS);
                } catch (Exception Ex) {
                    Log.e(TAG, "setGridType failed.", Ex);
                }
                return true;
            }
            case R.id.action_mapgridDD: {
                try {
                    this.map.setGridType(MapGridTypeEnum.DD);
                } catch (Exception Ex) {
                    Log.e(TAG, "setGridType failed.", Ex);
                }
                return true;
            }
            case R.id.action_minimap: {
                if (null == this.miniMapDialog) {
                    this.miniMapDialog = new MiniMapDialog();
                    this.miniMapDialog.setMap(this.map);
                    this.miniMapDialog.show(MainActivity.this.getFragmentManager(), null);
                } else {
                    this.miniMapDialog.dismiss();
                    this.miniMapDialog = null;
                }
                return true;
            }
            case R.id.action_exportMapToKML: {
                try {
                    EmpKMLExporter.exportToString(this.map, true, new IEmpExportToStringCallback() {

                        @Override
                        public void exportSuccess(String kmlString) {
                            //Log.i(TAG, kmlString);
                            FileOutputStream out = null;
                            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            File dest = new File(sd, "mapexport.kml");
                            if (dest.exists()) {
                                dest.delete();
                            }
                            try {
                                out = new FileOutputStream(dest);
                                byte[] byteArray = kmlString.getBytes();
                                out.write(byteArray, 0, byteArray.length);
                                out.flush();
                                MainActivity.this.makeToast("Export complete");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to save kml file.", e);
                                MainActivity.this.makeToast("Export failed");
                            } finally {
                                try {
                                    if (out != null) {
                                        out.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void exportFailed(Exception Ex) {
                            Log.e(TAG, "Map export to KML failed.", Ex);
                            MainActivity.this.makeToast("Export failed");
                        }
                    });
                } catch (Exception Ex) {
                    Log.e(TAG, "Map export to KML failed.", Ex);
                    MainActivity.this.makeToast("Export failed");
                }
                return true;
            }
            case R.id.action_exportOverlayToKML: {
                try {
                    EmpKMLExporter.exportToString(this.map, this.oRootOverlay, true, new IEmpExportToStringCallback() {

                        @Override
                        public void exportSuccess(String kmlString) {
                            FileOutputStream out = null;
                            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                            File dest = new File(sd, "overlayexport.kml");
                            if (dest.exists()) {
                                dest.delete();
                            }
                            try {
                                out = new FileOutputStream(dest);
                                byte[] byteArray = kmlString.getBytes();
                                out.write(byteArray, 0, byteArray.length);
                                out.flush();
                                MainActivity.this.makeToast("Export complete");
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to save kml file.", e);
                                MainActivity.this.makeToast("Export failed");
                            } finally {
                                try {
                                    if (out != null) {
                                        out.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void exportFailed(Exception Ex) {
                            Log.e(TAG, "Map export to KML failed.", Ex);
                            MainActivity.this.makeToast("Export failed");
                        }
                    });
                } catch (Exception Ex) {
                    Log.e(TAG, "Map export to KML failed.", Ex);
                    MainActivity.this.makeToast("Export failed");
                }
                return true;
            }
            case R.id.action_exportFeatureToKML: {
                try {
                    if (null != this.oCurrentSelectedFeature) {
                        EmpKMLExporter.exportToString(this.map, this.oCurrentSelectedFeature, true, new IEmpExportToStringCallback() {

                            @Override
                            public void exportSuccess(String kmlString) {
                                //Log.i(TAG, kmlString);
                                FileOutputStream out = null;
                                File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                File dest = new File(sd, "featureexport.kml");
                                if (dest.exists()) {
                                    dest.delete();
                                }
                                try {
                                    out = new FileOutputStream(dest);
                                    byte[] byteArray = kmlString.getBytes();
                                    out.write(byteArray, 0, byteArray.length);
                                    out.flush();
                                    MainActivity.this.makeToast("Export complete");
                                } catch (Exception e) {
                                    Log.e(TAG, "Failed to save kml file.", e);
                                    MainActivity.this.makeToast("Export failed");
                                } finally {
                                    try {
                                        if (out != null) {
                                            out.close();
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void exportFailed(Exception Ex) {
                                Log.e(TAG, "Map export to KML failed.", Ex);
                                MainActivity.this.makeToast("Export failed");
                            }
                        });
                    }
                } catch (Exception Ex) {
                    Log.e(TAG, "Map export to KML failed.", Ex);
                    MainActivity.this.makeToast("Export failed");
                }
                return true;
            }
            case R.id.action_exportFeatureToGeoJSON:
                try {
                    List<IFeature> featureList = this.map.getSelected();
                    if (featureList.size() == 1) {
                        GeoJsonCaller.exportToString(map, featureList.get(0), false, new IEmpExportToStringCallback() {
                            @Override
                            public void exportSuccess(String geoJSON) {
                                // Quick and dirty way to show the output
                                String[] splits = geoJSON.split("\n");
                                for (int i = 0; i < splits.length; i++) {
                                    Log.i(TAG, splits[i]);
                                }
                            }

                            @Override
                            public void exportFailed(Exception e) {
                                Log.i(TAG, "geojson export failed", e);
                            }
                        });
                    } else {
                        GeoJsonCaller.exportToString(map, featureList, false, new IEmpExportToStringCallback() {
                            @Override
                            public void exportSuccess(String geoJSON) {
                                // Quick and dirty way to show the output
                                String[] splits = geoJSON.split("\n");
                                for (int i = 0; i < splits.length; i++) {
                                    Log.i(TAG, splits[i]);
                                }
                            }

                            @Override
                            public void exportFailed(Exception e) {
                                Log.i(TAG, "geojson export failed", e);
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_screenShot: {
                this.map.getScreenCapture(new IScreenCaptureCallback() {
                    @Override
                    public void captureSuccess(ICapture capture) {
                        String dataURL = capture.screenshotDataURL();
                        FileOutputStream out = null;
                        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE);
                        if (permissionCheck !=  PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        } else {
                            try {
                                File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                                File dest = new File(sd, "empscreenshot.png");
                                if (dest.exists()) {
                                    dest.delete();
                                }
                                out = new FileOutputStream(dest);
                                if (!capture.screenshotBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)) {
                                    Log.e(TAG, "The Bitmap compress return false.");
                                }
                                // PNG is a loss less format, the compression factor (100) is ignored
                            } catch (Exception e) {
                                Log.e(TAG, "screen shot failed.", e);
                            } finally {
                                try {
                                    if (out != null) {
                                        out.flush();
                                        out.close();
                                    }
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }

                    @Override
                    public void captureFailed(Exception Ex) {
                        Log.e(TAG, "Screen Capture Failed.", Ex);
                    }
                });
                return true;
            }
            case R.id.action_addImageLayer: {
                try {
                    IGeoBounds bBox = new GeoBounds();

                    bBox.setNorth(37.91904192681665);
                    bBox.setSouth(37.46543388598137);
                    bBox.setEast(15.35832653742206);
                    bBox.setWest(14.60128369746704);
                    ImageLayer imageLayer = new ImageLayer("https://developers.google.com/kml/documentation/images/etna.jpg", bBox);

                    this.map.addMapService(imageLayer);
                } catch (Exception Ex) {
                    Log.e(TAG, "Image Layer failed.", Ex);
                }
                return true;
            }
            case R.id.action_addGeoPackage: {
                try {
                    getGpkgList();
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    if (gpkgList == null || (gpkgList.length == 0)) {
                        AlertDialog dialog = builder.create();
                        dialog.setTitle("GeoPackage Chooser");
                        dialog.setMessage("No geopackage file found");
                        dialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        dialog.show();
                    } else {
                        builder.setTitle("Choose GeoPackage file");
                        builder.setItems(gpkgList, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                gpkgPick = gpkgList[which];
                                Log.i(TAG, "File picked is " + gpkgPick);
                                try {
                                    geoPackage = new GeoPackage("File:///sdcard/Download/" + gpkgPick);
                                    MainActivity.this.map.addMapService(geoPackage);
                                    ICamera camera = MainActivity.this.map.getCamera();
                                    // Place the camera directly over the GeoPackage image.
                                    camera.setLatitude(39.54795);
                                    camera.setLongitude(-76.16334);
                                    camera.setAltitude(2580);
                                    camera.apply(true);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        builder.show();
                    }

                    //Using hard coded values here because it is extremely difficult to locate otherwise
                } catch (Exception Ex) {
                    Log.e(TAG, "Image Layer failed.", Ex);
                }
                return true;
            }
            case R.id.action_plotKML: {
                InputStream stream = null;
                try {
/*
                    KML kmlOverlay = new KML();

                    kmlOverlay.setKMLString("<kml>\n" +
                            "                  <Document>\n" +
                            "                    <Placemark id=\"f2\">\n" +
                            "                      <name>\n" +
                            "                        Feature Collection Test 1\n" +
                            "                      </name>\n" +
                            "                      <description>\n" +
                            "                        <![CDATA[<br/><table border=\\\"1\\\" cellpadding=\\\"2\\\" cellspacing=\\\"0\\\" width=100%><tr ><td><b>lineColor</b></td><td>7f00ffff</td></tr><tr  bgcolor=\\\"#F0F0F0\\\"><td><b>lineThickness</b></td><td>5</td></tr></table>]]>\n" +
                            "                      </description>\n" +
                            "                      <Style id=\"geomcollection\">\n" +
                            "                        <LineStyle>\n" +
                            "                          <color>7fffff00</color>\n" +
                            "                          <width>5</width>\n" +
                            "                        </LineStyle>\n" +
                            "                        <PolyStyle>\n" +
                            "                          <color>88000000</color>\n" +
                            "                        </PolyStyle>\n" +
                            "                        <LabelStyle>\n" +
                            "                          <color>FFFFFFFF</color>\n" +
                            "                        </LabelStyle>\n" +
                            "                        <IconStyle>\n" +
                            "                          <color>00000000</color>\n" +
                            "                        </IconStyle>\n" +
                            "                      </Style>\n" +
                            "                      <MultiGeometry>\n" +
                            "                        <LineString>\n" +
                            "                          <tessellate>1</tessellate>\n" +
                            "                          <coordinates>-110,40 -111,41 -112,40 -113,41</coordinates>\n" +
                            "                        </LineString>\n" +
                            "                        <Point>\n" +
                            "                          <coordinates>-110.0,42.0</coordinates>\n" +
                            "                        </Point>\n" +
                            "                      </MultiGeometry>\n" +
                            "                    </Placemark>\n" +
                            "                  </Document>\n" +
                            "                </kml>");
*/
                    stream = getApplicationContext().getResources().openRawResource(R.raw.kml_samples);

                    KML kmlFeature = new KML(stream);
                    this.oRootOverlay.addFeature(kmlFeature, true);
                    this.oFeatureHash.put(kmlFeature.getGeoId(), kmlFeature);
                } catch (Exception Ex) {
                    Log.e(TAG, "KML failed.", Ex);
                } finally {
                    if (null != stream) {
                        try {
                            stream.close();
                        }catch (IOException ex) {
                        }
                    }
                }
                return true;
            }
            case R.id.action_addGeoJSON:
                InputStream stream = null;
                try {
                    stream = getApplicationContext().getResources().openRawResource(R.raw.communes_69);
                    IFeature feature = new GeoJSON(stream);
                    this.oRootOverlay.addFeature(feature, true);
                    this.oFeatureHash.put(feature.getGeoId(), feature);
                    stream.close();
                    stream = getApplicationContext().getResources().openRawResource(R.raw.random_geoms);
                    feature = new GeoJSON(stream);
                    this.oRootOverlay.addFeature(feature, true);
                    this.oFeatureHash.put(feature.getGeoId(), feature);
                    stream.close();
                    stream = getApplicationContext().getResources().openRawResource(R.raw.rhone);
                    feature = new GeoJSON(stream);
                    this.oRootOverlay.addFeature(feature, true);
                    this.oFeatureHash.put(feature.getGeoId(), feature);
                    stream.close();
                    ICamera camera = this.map.getCamera();
                    camera.setLatitude(44.5);
                    camera.setLongitude(1);
                    camera.setAltitude(5e5);
                    camera.apply(false);
                } catch (IOException|EMP_Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (stream != null) {
                            stream.close();
                        }
                    } catch (IOException ioe) {
                        ioe.printStackTrace();
                    }
                }
                return true;
            case R.id.action_plot2units2525B:
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Number of Units:")
                        .setView(R.layout.plot_unit_dialog)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                final EditText text = (EditText) (((Dialog) dialog).findViewById(R.id.input));
                                final CheckBox checkBox = (CheckBox) (((Dialog) dialog).findViewById(R.id.remove_features));

                                final String numUnits = text.getText().toString();
                                Log.d(TAG, "numUnits: " + numUnits);

                                final boolean shouldRemoveAllFeatures = checkBox.isChecked();
                                Log.d(TAG, "shouldRemoveAllFeatures: " + shouldRemoveAllFeatures);

                                if (shouldRemoveAllFeatures) {
                                    removeAllFeatures();
                                }

                                plotManyMilStd(Integer.parseInt(numUnits));
                            }
                        }).create().show();
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
            case R.id.action_addWMTS:
                ArrayList<String> layers = new ArrayList<>();
                layers.add("matrikkel_bakgrunn");
                try {
                    wmtsService = new WMTS(
                            "http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts",
                            null, null, layers);
                    map.addMapService(MainActivity.this.wmtsService);
                    ICamera camera = this.map.getCamera();
                    camera.setLatitude(64.27);
                    camera.setLongitude(10.12);
                    camera.setAltitude(225000);
                    camera.apply(false);
                    MenuItem oItem = MainActivity.this.oMenu.findItem(R.id.action_removeWMTS);
                    oItem.setEnabled(true);
                    oItem = MainActivity.this.oMenu.findItem(R.id.action_addWMTS);
                    oItem.setEnabled(false);
                } catch (MalformedURLException | EMP_Exception m) {
                    m.printStackTrace();
                }
                return true;
            case R.id.action_removeWMTS:
                try {
                    map.removeMapService(this.wmtsService);
                    MenuItem oItem = this.oMenu.findItem(R.id.action_removeWMTS);
                    oItem.setEnabled(false);
                    oItem = this.oMenu.findItem(R.id.action_addWMTS);
                    oItem.setEnabled(true);
                } catch (EMP_Exception ex) {
                }
                return true;
            case R.id.action_addWCS:
                try {
                    wcsService = new WCS("https://worldwind26.arc.nasa.gov/wcs",
                            "aster_v2");
                    map.addMapService(wcsService);
                    MenuItem oItem = MainActivity.this.oMenu.findItem(R.id.action_removeWCS);
                    oItem.setEnabled(true);
                    oItem = MainActivity.this.oMenu.findItem(R.id.action_addWCS);
                    oItem.setEnabled(false);
                    // 37.577227, -105.485845, 4374
                    ILookAt calculatedLookAt = CameraUtility.setupLookAt(37.5, -105.5, 5000,
                            37.577227, -105.485845, 4374);
                    map.setLookAt(calculatedLookAt, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_removeWCS:
                try {
                    map.removeMapService(this.wcsService);
                    MenuItem oItem = this.oMenu.findItem(R.id.action_removeWCS);
                    oItem.setEnabled(false);
                    oItem = this.oMenu.findItem(R.id.action_addWCS);
                    oItem.setEnabled(true);
                } catch (EMP_Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            case R.id.action_addWMS:
                try {
                    final Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.wms_parameters_dialog);
                    dialog.setTitle("Title...");
                    Button okButton = (Button) dialog.findViewById(R.id.OKButton);
                    // if button is clicked, close the custom dialog
                    okButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            EditText urlText = (EditText) dialog.findViewById(R.id.UrlText);
                            EditText versionText = (EditText) dialog.findViewById(R.id.VersionText);
                            EditText tileFormatText = (EditText) dialog.findViewById(R.id.TileFormatText);
                            EditText transparentText = (EditText) dialog.findViewById(R.id.TransparentText);
                            EditText layerText = (EditText) dialog.findViewById(R.id.LayerText);
                            EditText resolutionText = (EditText) dialog.findViewById(R.id.ResolutionText);

                            String url = urlText.getText().toString();
                            String version = versionText.getText().toString();
                            if (version == null)
                                version = "";
                            String tileFormat = tileFormatText.getText().toString();
                            String transparent = transparentText.getText().toString();
                            String layer = layerText.getText().toString();
                            String resolution = resolutionText.getText().toString();
                            Resources res = getBaseContext().getResources();
                            dialog.dismiss();
                            WMSVersionEnum wmsVersion = null;
                            switch (version) {
                                case "1.1.1" : wmsVersion = WMSVersionEnum.VERSION_1_1_1;
                                    break;
                                case "1.1" : wmsVersion = WMSVersionEnum.VERSION_1_1;
                                    break;
                                case "1.3.0" :wmsVersion = WMSVersionEnum.VERSION_1_3_0;
                                    break;
                                case "1.3" : wmsVersion = WMSVersionEnum.VERSION_1_3;
                                    break;
                                default:
                                    wmsVersion = WMSVersionEnum.VERSION_1_3_0;;
                            }
                            ArrayList<String> layers = new ArrayList<>();
                            layers.add(layer);
                            try {
                                wmsService = new mil.emp3.api.WMS(
                                        url,
                                        wmsVersion,
                                        tileFormat,
                                        transparent.equalsIgnoreCase("true"),
                                        layers
                                );
                            } catch (MalformedURLException ex) {
                                ex.printStackTrace();
                            }
                            MainActivity.this.wmsService.setLayerResolution(Double.valueOf(resolution));
                            try {
                                map.addMapService(MainActivity.this.wmsService);
                            } catch (EMP_Exception e) {
                                e.printStackTrace();
                            }
                            MenuItem oItem = MainActivity.this.oMenu.findItem(R.id.action_removeWMS);
                            oItem.setEnabled(true);
                            oItem = MainActivity.this.oMenu.findItem(R.id.action_addWMS);
                            oItem.setEnabled(false);
                        }
                    });
                    Button cancelButton = (Button) dialog.findViewById(R.id.CancelButton);
                    // if button is clicked, don't add WMS service
                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
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
                textFeature.setName("Text");
                textFeature.setDescription("Draw Text Feature.");
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
                strokeStyle.setStipplingPattern((short) 0);
                linePath.setStrokeStyle(strokeStyle);
                linePath.setName("Line");
                linePath.setDescription("Draw Line Feature.");
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
                strokeStyle.setStipplingPattern((short) 0);
                polygon.setStrokeStyle(strokeStyle);

                fillStyle.setFillColor(geoFillColor);
                fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
                polygon.setFillStyle(null); //fillStyle
                polygon.setName("Polygon");
                polygon.setDescription("Draw Polygon Feature.");

                //polygon.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.map.drawFeature(polygon, new FeatureDrawListener(polygon));
                } catch(EMP_Exception Ex) {
                    Log.e(TAG, "Draw ploygon failed.");
                    //oItem.setEnabled(true);
                }

                return true;
            }
            case R.id.action_drawCircle: {
                IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                IGeoFillStyle fillStyle = new GeoFillStyle();
                IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
                IGeoColor geoFillColor = new EmpGeoColor(0.5, 0, 0, 255);
                Circle circle = new Circle();

                strokeStyle.setStrokeColor(geoColor);
                strokeStyle.setStrokeWidth(5);
                strokeStyle.setStipplingPattern((short) 0);
                circle.setStrokeStyle(strokeStyle);

                fillStyle.setFillColor(geoFillColor);
                fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
                //circle.setFillStyle(fillStyle);
                circle.setName("Circle");
                circle.setDescription("Draw Circle Feature.");

                circle.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.map.drawFeature(circle, new FeatureDrawListener(circle));
                } catch(EMP_Exception Ex) {
                    Log.e(TAG, "Draw circle failed.");
                    //oItem.setEnabled(true);
                }

                return true;
            }
            case R.id.action_drawEllipse: {
                IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                IGeoFillStyle fillStyle = new GeoFillStyle();
                IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
                IGeoColor geoFillColor = new EmpGeoColor(0.5, 255, 0, 0);
                Ellipse ellipse = new Ellipse();

                strokeStyle.setStrokeColor(geoColor);
                strokeStyle.setStrokeWidth(5);
                strokeStyle.setStipplingPattern((short) 0);
                ellipse.setStrokeStyle(strokeStyle);

                fillStyle.setFillColor(geoFillColor);
                fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
                //ellipse.setFillStyle(fillStyle);
                ellipse.setName("Ellipse");
                ellipse.setDescription("Draw Ellipse Feature.");

                ellipse.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.map.drawFeature(ellipse, new FeatureDrawListener(ellipse));
                } catch(EMP_Exception Ex) {
                    Log.e(TAG, "Draw ellipse failed.");
                    //oItem.setEnabled(true);
                }

                return true;
            }
            case R.id.action_drawRectangle: {
                IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                IGeoFillStyle fillStyle = new GeoFillStyle();
                IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
                IGeoColor geoFillColor = new EmpGeoColor(0.5, 255, 0, 0);
                Rectangle feature = new Rectangle();

                strokeStyle.setStrokeColor(geoColor);
                strokeStyle.setStrokeWidth(5);
                strokeStyle.setStipplingPattern((short) 0);
                feature.setStrokeStyle(strokeStyle);

                fillStyle.setFillColor(geoFillColor);
                fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
                feature.setFillStyle(null);
                feature.setName("Rectangle");
                feature.setDescription("Draw Rectangle Feature.");

                feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.map.drawFeature(feature, new FeatureDrawListener(feature));
                } catch(EMP_Exception Ex) {
                    Log.e(TAG, "Draw Rectangle failed.");
                    //oItem.setEnabled(true);
                }

                return true;
            }
            case R.id.action_drawSquare: {
                IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                IGeoFillStyle fillStyle = new GeoFillStyle();
                IGeoColor geoColor = new EmpGeoColor(1.0, 0, 255, 255);
                IGeoColor geoFillColor = new EmpGeoColor(0.5, 255, 0, 0);
                Square feature = new Square();

                strokeStyle.setStrokeColor(geoColor);
                strokeStyle.setStrokeWidth(5);
                strokeStyle.setStipplingPattern((short) 0);
                feature.setStrokeStyle(strokeStyle);

                fillStyle.setFillColor(geoFillColor);
                fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
                feature.setFillStyle(null);
                feature.setName("Square");
                feature.setDescription("Draw Square Feature.");

                feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.map.drawFeature(feature, new FeatureDrawListener(feature));
                } catch(EMP_Exception Ex) {
                    Log.e(TAG, "Draw Rectangle failed.");
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
                iconStyleStroke.setStipplingPattern((short) 0);
                break;
            }
            case R.id.action_iconStyleStrokePatternSolid: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokePatternSolid);
                menuItem.setChecked(true);
                iconStyleStroke.setStipplingPattern((short) 0);
                break;
            }
            case R.id.action_iconStyleStrokePatternDashed: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokePatternDashed);
                menuItem.setChecked(true);
                iconStyleStroke.setStipplingPattern((short) 0xCCCC);
                iconStyleStroke.setStipplingFactor(8);
                break;
            }
            case R.id.action_iconStyleStrokePatternDotted: {
                final MenuItem menuItem = oMenu.findItem(R.id.action_iconStyleStrokePatternDotted);
                menuItem.setChecked(true);
                iconStyleStroke.setStipplingPattern((short) 0xCCCC);
                iconStyleStroke.setStipplingFactor(2);
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

                pos1.setLatitude(pos2.getLatitude() + 0.01);
                pos1.setLongitude(pos2.getLongitude());
                pos1.setAltitude(0);

                pos3.setLatitude(pos2.getLatitude() - 0.01);
                pos3.setLongitude(pos2.getLongitude());
                pos3.setAltitude(0);

                labelStyle = new GeoLabelStyle();
                labelStyle.setColor(new EmpGeoColor(1.0, 0, 255, 255));
                labelStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
                labelStyle.setSize(14.0);
                labelStyle.setOutlineColor(null);
                textFeature = new mil.emp3.api.Text();
                textFeature.setName("This Text is LEFT Justified 14pt.");
                textFeature.setLabelStyle(labelStyle);
                textFeature.setPosition(pos1);
                textFeature.setRotationAngle(45.0);
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
                labelStyle.setSize(12.0);
                //labelStyle.setOutlineColor(null);
                textFeature = new mil.emp3.api.Text();
                textFeature.setName("This Text is CENTER Justified 12pt.");
                textFeature.setLabelStyle(labelStyle);
                textFeature.setPosition(pos2);
                textFeature.setRotationAngle(45.0);
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
                labelStyle.setSize(10.0);
                labelStyle.setOutlineColor(null);
                textFeature = new mil.emp3.api.Text();
                textFeature.setName("This Text is RIGHT Justified 10pt.");
                textFeature.setLabelStyle(labelStyle);
                textFeature.setPosition(pos3);
                textFeature.setRotationAngle(45.0);
                //textFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                try {
                    this.oRootOverlay.addFeature(textFeature, true);
                    this.oFeatureHash.put(textFeature.getGeoId(), textFeature);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }


                return true;
            }
            case R.id.action_plotTG2525C: {
                this.plot2525CTG();
                return true;
            }
            case R.id.action_textsizeSmallest: {
                try {
                    this.map.setFontSizeModifier(FontSizeModifierEnum.SMALLEST);
                } catch (EMP_Exception e) {
                    Log.e(TAG, "setFontSizeModifier failed.", e);
                }
                return true;
            }
            case R.id.action_textsizeSmaller: {
                try {
                    this.map.setFontSizeModifier(FontSizeModifierEnum.SMALLER);
                } catch (EMP_Exception e) {
                    Log.e(TAG, "setFontSizeModifier failed.", e);
                }
                return true;
            }
            case R.id.action_textsizeNormal: {
                try {
                    this.map.setFontSizeModifier(FontSizeModifierEnum.NORMAL);
                } catch (EMP_Exception e) {
                    Log.e(TAG, "setFontSizeModifier failed.", e);
                }
                return true;
            }
            case R.id.action_textsizeLarger: {
                try {
                    this.map.setFontSizeModifier(FontSizeModifierEnum.LARGER);
                } catch (EMP_Exception e) {
                    Log.e(TAG, "setFontSizeModifier failed.", e);
                }
                return true;
            }
            case R.id.action_textsizeLargest: {
                try {
                    this.map.setFontSizeModifier(FontSizeModifierEnum.LARGEST);
                } catch (EMP_Exception e) {
                    Log.e(TAG, "setFontSizeModifier failed.", e);
                }
                return true;
            }
            case R.id.action_plotRectangle: {
                try {
                    IGeoColor lineColor = new EmpGeoColor(1.0, 0, 0, 255);
                    IGeoColor fillColor = new EmpGeoColor(0.6, 255, 0, 0);
                    IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                    IGeoFillStyle fillStyle = new GeoFillStyle();
                    List<IGeoPosition> oPosList = this.getCameraPosition();
                    Rectangle oFeature = new Rectangle();

                    strokeStyle.setStrokeColor(lineColor);
                    strokeStyle.setStrokeWidth(3);
                    fillStyle.setFillColor(fillColor);

                    oFeature.setPosition(oPosList.get(0));
                    oFeature.setHeight(100000);
                    oFeature.setWidth(50000);
                    oFeature.setAzimuth(0);
                    oFeature.setStrokeStyle(strokeStyle);
                    oFeature.setFillStyle(fillStyle);
                    oFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                    this.oRootOverlay.addFeature(oFeature, true);
                    this.oFeatureHash.put(oFeature.getGeoId(), oFeature);

                } catch (EMP_Exception ex) {
                }
                return true;
            }
            case R.id.action_plotSquare: {
                try {
                    IGeoColor lineColor = new EmpGeoColor(1.0, 0, 0, 255);
                    IGeoColor fillColor = new EmpGeoColor(0.5, 255, 0, 0);
                    IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                    IGeoFillStyle fillStyle = new GeoFillStyle();
                    List<IGeoPosition> oPosList = this.getCameraPosition();
                    Square oFeature = new Square();

                    strokeStyle.setStrokeColor(lineColor);
                    strokeStyle.setStrokeWidth(3);
                    fillStyle.setFillColor(fillColor);

                    oFeature.setPosition(oPosList.get(0));
                    oFeature.setWidth(50000);
                    oFeature.setAzimuth(0);
                    oFeature.setStrokeStyle(strokeStyle);
                    oFeature.setFillStyle(fillStyle);
                    oFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                    this.oRootOverlay.addFeature(oFeature, true);
                    this.oFeatureHash.put(oFeature.getGeoId(), oFeature);
                } catch (EMP_Exception ex) {
                }
                return true;
            }
            case R.id.action_plotCircle: {
                try {
                    IGeoColor lineColor = new EmpGeoColor(1.0, 0, 0, 255);
                    IGeoColor fillColor = new EmpGeoColor(0.5, 255, 0, 0);
                    IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                    IGeoFillStyle fillStyle = new GeoFillStyle();
                    List<IGeoPosition> oPosList = this.getCameraPosition();
                    Circle oFeature = new Circle();

                    strokeStyle.setStrokeColor(lineColor);
                    strokeStyle.setStrokeWidth(3);
                    fillStyle.setFillColor(fillColor);

                    oFeature.setPosition(oPosList.get(0));
                    oFeature.setRadius(25000);
                    oFeature.setStrokeStyle(strokeStyle);
                    oFeature.setFillStyle(fillStyle);
                    oFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                    this.oRootOverlay.addFeature(oFeature, true);
                    this.oFeatureHash.put(oFeature.getGeoId(), oFeature);
                } catch (EMP_Exception ex) {
                }
                return true;
            }
            case R.id.action_plotEllipse: {
                try {
                    IGeoColor lineColor = new EmpGeoColor(1.0, 0, 0, 255);
                    IGeoColor fillColor = new EmpGeoColor(0.5, 255, 0, 0);
                    IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                    IGeoFillStyle fillStyle = new GeoFillStyle();
                    List<IGeoPosition> oPosList = this.getCameraPosition();
                    Ellipse oFeature = new Ellipse();

                    strokeStyle.setStrokeColor(lineColor);
                    strokeStyle.setStrokeWidth(3);
                    fillStyle.setFillColor(fillColor);

                    oFeature.setPosition(oPosList.get(0));
                    oFeature.setSemiMajor(50000);
                    oFeature.setSemiMinor(25000);
                    oFeature.setAzimuth(0);
                    oFeature.setStrokeStyle(strokeStyle);
                    oFeature.setFillStyle(fillStyle);
                    oFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                    this.oRootOverlay.addFeature(oFeature, true);
                    this.oFeatureHash.put(oFeature.getGeoId(), oFeature);
                } catch (EMP_Exception ex) {
                }
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void plot2525CTG() {
        int tgCount = 0;
        String name;
        int countPerLine = 0;
        ICamera camera = this.map.getCamera();
        IGeoPosition position = new GeoPosition();
        SymbolDefTable oDefTable = SymbolDefTable.getInstance();
        java.util.Map<java.lang.String, armyc2.c2sd.renderer.utilities.SymbolDef> oDefList = oDefTable.GetAllSymbolDefs( armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525C);

        position.setLatitude(camera.getLatitude());
        position.setLongitude(camera.getLongitude());
        position.setAltitude(0.0);


        for (armyc2.c2sd.renderer.utilities.SymbolDef oSymbolDef: oDefList.values()) {
            if (oSymbolDef.getHierarchy().charAt(0) == '0') {
                continue;
            }

            if (oSymbolDef.getDrawCategory() == SymbolDef.DRAW_CATEGORY_DONOTDRAW) {
                continue;
            }

            tgCount++;

            countPerLine++;
            this.plotTacticalGraphic("TG-" + String.format("%04d", tgCount), oSymbolDef, position);
            if (countPerLine == 20) {
                position.setLongitude(camera.getLongitude());
                GeoLibrary.computePositionAt(180.0, 2500.0, position, position);
                countPerLine = 0;
            } else {
                GeoLibrary.computePositionAt(90.0, 2500.0, position, position);
            }
        }
    }

    private void plotTacticalGraphic(String name, armyc2.c2sd.renderer.utilities.SymbolDef oSymbolDef, IGeoPosition position) {
        try {
            java.util.List<IGeoPosition> posList = new java.util.ArrayList<>();
            IGeoPosition pos;
            MilStdSymbol symbol = new MilStdSymbol(IGeoMilSymbol.SymbolStandard.MIL_STD_2525C, oSymbolDef.getBasicSymbolId());

            symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, oSymbolDef.getDescription());
            symbol.setName(name);
            symbol.setDescription(oSymbolDef.getDescription());
            symbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

            switch (oSymbolDef.getMinPoints()) {
                case 1:
                    pos = new GeoPosition();
                    pos.setLatitude(position.getLatitude());
                    pos.setLongitude(position.getLongitude());
                    pos.setAltitude(0.0);
                    posList.add(pos);

                    if (CoreMilStdUtilities.SECTOR_RANGE_FAN.equals(symbol.getBasicSymbol())) {
                        symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, 1000);
                        symbol.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 0, 315);
                        symbol.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 1, 45);
                    } else if (CoreMilStdUtilities.CIRCULAR_RANGE_FAN.equals(symbol.getBasicSymbol())) {
                        symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, 500);
                        symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 1, 1000);
                        symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 2, 1500);
                    }

                    break;
                case 2:
                    if (oSymbolDef.getDrawCategory() == SymbolDef.DRAW_CATEGORY_LINE) {
                        pos = GeoLibrary.computePositionAt(270, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeoLibrary.computePositionAt(90, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                    } else {
                        pos = new GeoPosition();
                        pos.setLatitude(position.getLatitude());
                        pos.setLongitude(position.getLongitude());
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeoLibrary.computePositionAt(90, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                    }

                    break;
                case 3:
                default:
                    pos = GeoLibrary.computePositionAt(240.0, 1000.0, position);
                    pos.setAltitude(0.0);
                    posList.add(pos);
                    pos = GeoLibrary.computePositionAt(0.0, 1000.0, position);
                    pos.setAltitude(0.0);
                    posList.add(pos);
                    pos = GeoLibrary.computePositionAt(120.0, 1000.0, position);
                    pos.setAltitude(0.0);
                    posList.add(pos);

                    break;
                case 4:
                    if (CoreMilStdUtilities.MS_NBC_MINIMUM_SAFE_DISTANCE_ZONES.equals(symbol.getBasicSymbol())) {
                        pos = new GeoPosition();
                        pos.setLatitude(position.getLatitude());
                        pos.setLongitude(position.getLongitude());
                        pos.setAltitude(0.0);
                        posList.add(pos);

                        pos = new GeoPosition();
                        GeoLibrary.computePositionAt(90.0, 500.0, posList.get(0), pos);
                        pos.setAltitude(0.0);
                        posList.add(pos);

                        pos = new GeoPosition();
                        GeoLibrary.computePositionAt(90.0, 1000.0, posList.get(0), pos);
                        pos.setAltitude(0.0);
                        posList.add(pos);

                        pos = new GeoPosition();
                        GeoLibrary.computePositionAt(90.0, 1500.0, posList.get(0), pos);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                    } else {
                        pos = GeoLibrary.computePositionAt(225.0, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeoLibrary.computePositionAt(315.0, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeoLibrary.computePositionAt(45.0, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeoLibrary.computePositionAt(135.0, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                    }

                    break;
            }

            if (!oSymbolDef.getModifiers().isEmpty()) {
                // Needs modifiers.
                String[] aModifiers = oSymbolDef.getModifiers().split("\\.");
                for (int index = 0; index < aModifiers.length; index++) {
                    switch (aModifiers[index]) {
                        case "W":
                            symbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP, "0800Z");
                            break;
                        case "W1":
                            symbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP_2, "2300Z");
                            break;
                        case "T":
                            symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, symbol.getName());
                            break;
                        case "T1":
                            symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_2, "UD2");
                            break;
                        case "AM":
                            if (CoreMilStdUtilities.SECTOR_RANGE_FAN.equals(symbol.getBasicSymbol()) ||
                                    CoreMilStdUtilities.CIRCULAR_RANGE_FAN.equals(symbol.getBasicSymbol())) {
                            } else {
                                symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, 1000);
                            }
                            break;
                        case "AM#":
                            if (CoreMilStdUtilities.SECTOR_RANGE_FAN.equals(symbol.getBasicSymbol()) ||
                                    CoreMilStdUtilities.CIRCULAR_RANGE_FAN.equals(symbol.getBasicSymbol())) {
                            } else {
                                symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, 500);
                                symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 1, 1000);
                            }
                            break;
                        case "AN":
                            if (CoreMilStdUtilities.SECTOR_RANGE_FAN.equals(symbol.getBasicSymbol()) ||
                                    CoreMilStdUtilities.CIRCULAR_RANGE_FAN.equals(symbol.getBasicSymbol())) {
                            } else {
                                symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, 0);
                            }
                            break;
                        case "AN#":
                            if (CoreMilStdUtilities.SECTOR_RANGE_FAN.equals(symbol.getBasicSymbol()) ||
                                    CoreMilStdUtilities.CIRCULAR_RANGE_FAN.equals(symbol.getBasicSymbol())) {
                            } else {
                                symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 0, 45);
                                symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 1, 315);
                                symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 2, 45);
                                symbol.setModifier(IGeoMilSymbol.Modifier.DISTANCE, 3, 315);
                            }
                            break;
                        case "X":
                            if (CoreMilStdUtilities.SECTOR_RANGE_FAN.equals(symbol.getBasicSymbol())) {
                                symbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 0, 10000);
                            } else if (CoreMilStdUtilities.CIRCULAR_RANGE_FAN.equals(symbol.getBasicSymbol())) {
                                symbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 0, 10000);
                                //symbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 1, 20000);
                            } else {
                                symbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 0, 10000);
                            }
                            break;
                        case "X1":
                            //symbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 0, 10000);
                            symbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 1, 20000);
                            break;
                        case "H":
                            symbol.setModifier(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_1, "Info 1");
                            break;
                        case "H1":
                            symbol.setModifier(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_2, "Info 2");
                            break;
                        case "H2":
                            symbol.setModifier(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_3, "Info 3");
                            break;
                        case "N":
                            symbol.setModifier(IGeoMilSymbol.Modifier.HOSTILE, "N-ENY");
                            break;
                        case "C":
                            symbol.setModifier(IGeoMilSymbol.Modifier.QUANTITY, "12");
                            break;
                        case "A":
                            symbol.setModifier(IGeoMilSymbol.Modifier.SYMBOL_ICON, "SI");
                            break;
                        case "B":
                            symbol.setModifier(IGeoMilSymbol.Modifier.ECHELON, "Echelon");
                            break;
                        case "Y":
                            symbol.setModifier(IGeoMilSymbol.Modifier.LOCATION, "Location");
                            break;
                        case "V":
                            symbol.setModifier(IGeoMilSymbol.Modifier.EQUIPMENT_TYPE, "Eq Type");
                            break;
                        case "Q":
                            symbol.setModifier(IGeoMilSymbol.Modifier.DIRECTION_OF_MOVEMENT, "45");
                            break;
                        default:
                            Log.e(TAG, "Modifier not set " + aModifiers[index] + ". " + oSymbolDef.getDescription() + " getModifiers() " + oSymbolDef.getModifiers());
                            break;
                    }
                }
            }
            symbol.getPositions().clear();
            symbol.getPositions().addAll(posList);
            this.oFeatureHash.put(symbol.getGeoId(), symbol);
            this.oRootOverlay.addFeature(symbol, true);
        } catch (EMP_Exception ex) {
            Log.e(TAG, "Error plot TG. " + oSymbolDef.getDescription(), ex);
        }
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
                    MilStdSymbol newSymbol = null;
                    if (oDialog.getSymbolCode() == null || oDialog.getSymbolCode().isEmpty()) {
                        newSymbol = new MilStdSymbol();
                    } else {
                        newSymbol = new MilStdSymbol(oDialog.getMilStdVersion(), oDialog.getSymbolCode());
                    }
                    final MilStdSymbol oNewSymbol = newSymbol;
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

    public ICamera getCamera() {
        return this.oCamera;
    }


    private void makeToast(final String text) {
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
