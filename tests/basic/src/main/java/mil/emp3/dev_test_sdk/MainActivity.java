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
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
import mil.emp3.api.KMLS;
import mil.emp3.api.LineOfSight;
import mil.emp3.api.LookAt;
import mil.emp3.api.MilStdSymbol;
//import mil.emp3.api.MirrorCache;
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
import mil.emp3.api.enums.KMLSEventEnum;
import mil.emp3.api.enums.MapGridTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.enums.Property;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.FeatureUserInteractionEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IScreenCaptureCallback;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.api.utils.kml.EmpKMLExporter;
import mil.emp3.api.utils.kmz.EmpKMZExporter;
import mil.emp3.core.utils.CoreMilStdUtilities;
import mil.emp3.dev_test_sdk.databinding.ActivityMainBinding;
import mil.emp3.dev_test_sdk.databinding.WmsParametersDialogBinding;
import mil.emp3.dev_test_sdk.dialogs.FeatureLocationDialog;
import mil.emp3.dev_test_sdk.dialogs.MiniMapDialog;
import mil.emp3.dev_test_sdk.dialogs.milstdtacticalgraphics.TacticalGraphicPropertiesDialog;
import mil.emp3.dev_test_sdk.dialogs.milstdunits.SymbolPropertiesDialog;
import mil.emp3.dev_test_sdk.utils.CameraUtility;
import mil.emp3.dev_test_sdk.utils.KMLSServiceListener;
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
    private WCS wcsService = null;
    private WMS wmsService = null;
    private WMTS wmtsService = null;
    private LineOfSight los = null;
    private mil.emp3.api.GeoPackage geoPackage;
    protected HashMap<UUID, IFeature> oFeatureHash = new HashMap<>();
    private IFeature oCurrentSelectedFeature;
    private final org.cmapi.primitives.IGeoStrokeStyle oSelectedStrokeStyle = new org.cmapi.primitives.GeoStrokeStyle();
    private HashMap<UUID, FeatureLocationDialog> oSelectedDialogHash = new HashMap<>();
    private Menu oMenu = null;
    private RelativeLayout oPerformanceDlg;
    private EditText oCountTb;
    private CheckBox oAffiliationCkb;
    private CheckBox oBatchUpdateCkb;
    protected TextView oResults;
    private PerformanceTestThread oPerformanceTestThread;
    protected TextView oResultTextView;
    private EventListenerHandle stateHandler = null;
    private EventListenerHandle userInteraction = null;
    private EventListenerHandle featureInteraction = null;
    private EventListenerHandle viewChange = null;
    private EventListenerHandle cameraHandler = null;
    private Handler handler;
    private MiniMapDialog miniMapDialog = null;
    private TextView brightnessCtrl = null;

    private boolean bCrossHairsOn = false;

    private IGeoFillStyle iconStyleFill     = new GeoFillStyle();
    private IGeoStrokeStyle iconStyleStroke = new GeoStrokeStyle();
    private IGeoLabelStyle iconStyleLabel   = new GeoLabelStyle();

    private String[] gpkgList = null;
    private File downloadFolder = new File("/sdcard/Download/");
    private String gpkgPick;
    private static final String SUFFIX = ".gpkg";
    private WmsParametersDialogBinding wmsBinding = null;
    private ActivityMainBinding mainBinding = null;
    private Dialog wmsDialog;

//    private MirrorCache mc;

    public enum PlotModeEnum {
        IDLE,
        NEW,
        EDIT_PROPERTIES,
        EDIT_FEATURE,
        DRAW_FEATURE
    }

    private MainActivity.PlotModeEnum ePlotMode = MainActivity.PlotModeEnum.IDLE;

    public static MilStdSymbol generateMilStdSymbol(String description, UUID uuid, double latitude, double longitude) {
        java.util.List<IGeoPosition> oPositionList = new java.util.ArrayList<>();
        IGeoPosition oPosition = new GeoPosition();
        oPosition.setLatitude(latitude);
        oPosition.setLongitude(longitude);
        oPositionList.add(oPosition);
        MilStdSymbol oSPSymbol = null;
        try {
            oSPSymbol = new MilStdSymbol(
                    IGeoMilSymbol.SymbolStandard.MIL_STD_2525C,
                    "SFAPMFF--------");
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }

        oSPSymbol.getPositions().clear();
        oSPSymbol.getPositions().addAll(oPositionList);
        oSPSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "My First Icon");
        oSPSymbol.setName(description);
        oSPSymbol.setDescription(description);
        return oSPSymbol;
    }

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

                        IGeoPosition pos2 = MainActivity.this.map.containerToGeo(event.getPointF());
                        Log.d(TAG, "Map User container to geo Float: Lat/Lon: " + pos.getLatitude() + " / " +
                                pos.getLongitude());

                        android.graphics.Point point = MainActivity.this.map.geoToContainer(event.getCoordinate());
                        Log.d(TAG, "Map User geo to container: X/Y: " + point.x + " / " +
                                point.y);
                        Toast.makeText(MainActivity.this, "Container: X/Y: " + point.x + " / " +
                                point.y + " Lat/Lon: " + pos.getLatitude() + " / " +
                                pos.getLongitude(), Toast.LENGTH_LONG).show();
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
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
                    }
                    break;
                default:
                    Log.i(TAG, "Unsupported touch event");
                    break;
            }
        }
    }

    public class FeatureInteractionEventListener implements IFeatureInteractionEventListener {

        @Override
        public void onEvent(FeatureUserInteractionEvent event) {
            IFeature oFeature = event.getTarget().get(0);
            //SelectedFeature oSelectedItem;

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
                    if (oFeature != null) {
                        // If there is a feature on the list, we want to place it into draw mode.
                        try {
                            if ((MainActivity.this.ePlotMode == PlotModeEnum.IDLE) &&
                                    MainActivity.this.map.isSelected(oFeature)) {
                                Log.d(TAG, "FeatureUserInteractionEvent  Entering edit mode.");
                                MainActivity.this.oCurrentSelectedFeature = oFeature;
                                MainActivity.this.map.drawFeature(oFeature, new FeatureDrawListener(oFeature));
                            }
                        } catch (EMP_Exception Ex) {
                            Log.d(TAG, "drawFeature failed.", Ex);
                        }
                    }
                    break;
                default:
                    Log.i(TAG, "Unsupported touch event");
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
            mainBinding.editorCompleteBtn.show();
            mainBinding.editorCancelBtn.show();
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
            mainBinding.editorCompleteBtn.hide();
            mainBinding.editorCancelBtn.hide();
            if (MainActivity.this.oSelectedDialogHash.containsKey(feature.getGeoId())) {
                FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(feature.getGeoId());
                oDialog.updateDialog();
            }
        }

        @Override
        public void onEditCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "Edit Canceled.");
            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
            mainBinding.editorCompleteBtn.hide();
            mainBinding.editorCancelBtn.hide();
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
            mainBinding.editorCompleteBtn.show();
            mainBinding.editorCancelBtn.show();
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
            mainBinding.editorCompleteBtn.hide();
            mainBinding.editorCancelBtn.hide();
            if (!MainActivity.this.oFeatureHash.containsKey(feature.getGeoId())) {
                // Only add it if it does not exists. A feature can be placed back into draw mode.
                try {
                    MainActivity.this.oRootOverlay.addFeature(feature, true);
                    MainActivity.this.oFeatureHash.put(feature.getGeoId(), feature);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onDrawCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "Draw Canceled.");
            MainActivity.this.ePlotMode = PlotModeEnum.IDLE;
            mainBinding.editorCompleteBtn.hide();
            mainBinding.editorCancelBtn.hide();
            FeatureLocationDialog oDialog = MainActivity.this.oSelectedDialogHash.get(originalFeature.getGeoId());
            if (null != oDialog) {
                oDialog.dismiss();
            }
        }

        @Override
        public void onDrawError(IMap map, String errorMessage) {
            Log.d(TAG, "Draw Error.");
        }
    }

    public class GeoJsonCallBack implements IEmpExportToStringCallback {
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

    private List<IGeoPosition> getCameraPosition() {
        List<IGeoPosition> oPosList = new ArrayList<>();
        IGeoPosition oPos = new GeoPosition();

        oPos.setLatitude(this.oCamera.getLatitude());
        oPos.setLongitude(this.oCamera.getLongitude());
        oPos.setAltitude(0);

        oPosList.add(oPos);
        return oPosList;
    }

    private List<IGeoPosition> getPositions(int iCount, double dAltitude) {
        IGeoPosition oPos;
        List<IGeoPosition> oPosList = new ArrayList<>();
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
        Map<String,UnitDef> oDefMap = oDefTable.getAllUnitDefs(iMilStdVersion);
        Set<String> oSymbols = oDefMap.keySet();
        UnitDef oSymDef;
        int iCount = 0;
        List<IFeature> oFeatureList = new ArrayList<>();
        List<IGeoPosition> oPosList;
        final List<MilStdSymbol.Affiliation> oAffiliationList = Arrays.asList(MilStdSymbol.Affiliation.FRIEND,
                MilStdSymbol.Affiliation.HOSTILE,
                MilStdSymbol.Affiliation.NEUTRAL);
        //,MilStdSymbol.Affiliation.SUSPECT);
        final List<MilStdSymbol.Echelon> oEchelonList = Arrays.asList(MilStdSymbol.Echelon.UNIT,
                MilStdSymbol.Echelon.SQUAD,
                MilStdSymbol.Echelon.PLATOON_DETACHMENT,
                MilStdSymbol.Echelon.COMPANY_BATTERY_TROOP,
                MilStdSymbol.Echelon.BATTALION_SQUADRON,
                MilStdSymbol.Echelon.BRIGADE);

        top: {
            for (String sBasicSymbolCode : oSymbols) {
                for (MilStdSymbol.Echelon eEchelon : oEchelonList) {
                    for (MilStdSymbol.Affiliation eAffiliation : oAffiliationList) {
                        //Log.d(TAG, "Symbol " + sBasicSymbolCode);
                        if (SymbolUtilities.isWarfighting(sBasicSymbolCode)) {
                            oSymDef = oDefMap.get(sBasicSymbolCode);

                            oPosList = new ArrayList<>();
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

    private void processSymbolTable(Map<java.lang.String,UnitDef> oDefMap,
            org.cmapi.primitives.GeoMilSymbol.SymbolStandard eStandard,
            int iMilStdVersion,
            int iMaxCount) {
        Set<String> oSymbols = oDefMap.keySet();
        UnitDef oSymDef;
        String sSymbolCode;
        int iCount = 0;
        List<IFeature> oFeatureList = new ArrayList<>();

        for (String sBasicSymbolCode: oSymbols) {
            //Log.d(TAG, "Symbol " + sBasicSymbolCode);
            if (SymbolUtilities.isWarfighting(sBasicSymbolCode)) {
                oSymDef = oDefMap.get(sBasicSymbolCode);

                List<IGeoPosition> oPosList = this.getPositions(1, 20000.0);

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
        Map<java.lang.String,UnitDef> oDefMap = oDefTable.getAllUnitDefs(iMilStdVersion);

        processSymbolTable(oDefMap, eStandard, iMilStdVersion, iCount);
    }

    protected void removeAllFeatures() {
        if (!this.oFeatureHash.isEmpty()) {
            List<IFeature> oFeatureList = new ArrayList<>();

            for (UUID uuid: this.oFeatureHash.keySet()) {
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
/*        if (mc != null) {
            try {
                mc.disconnect();

            } catch (EMP_Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }*/
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mainBinding.setMainmap(this);
        Toolbar toolbar = mainBinding.toolbar;
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

        map = mainBinding.map;

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
            stateHandler = map.addMapStateChangeEventListener(event -> {
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
                        MainActivity.this.runOnUiThread(() -> {
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
                        });

                        map.addCameraEventListener(event1 -> MainActivity.this.runOnUiThread(() -> { // to indicate that an imap camera event was triggered
                            //Toast.makeText(MainActivity.this, "IMap.cameraEventListener.onEvent() triggered: " + event.getEvent(), Toast.LENGTH_SHORT).show();
                        }));
                    }
                } catch (Exception e) {
                    Log.e(TAG, "ERROR: " + e.getMessage(), e);
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapStateChangeEventListener failed. " + e.toString());
        }
        //Log.d(TAG, "Registered for Map Ready.");

        TextView oCameraSetting = mainBinding.camerasettings;
        String sTemp = String.format(Locale.US, "Lat:%7.5f\u00b0 Lon:%8.5f\u00b0 Alt:%.0f m Heading:%3.0f\u00b0 Tilt:%3.0f\u00b0 Roll:%3.0f\u00b0", oCamera.getLatitude(), oCamera.getLongitude(), oCamera.getAltitude(), oCamera.getHeading(), oCamera.getTilt(), oCamera.getRoll());
        oCameraSetting.setText(sTemp);

        List<String> oLayers = new ArrayList<>();

        oLayers.add("imagery_part2-2.0.0.1-wsmr.gpkg");


        mainBinding.editorCompleteBtn.hide();
        mainBinding.editorCancelBtn.hide();
        registerComponentCallbacks(new MyComponentCallbacks());

        ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
        Log.e(TAG, "Get Memory Class " + am.getMemoryClass());
    }

    public void editorComplete(View view) {
        try {
            switch (MainActivity.this.ePlotMode) {
                case EDIT_FEATURE:
                    MainActivity.this.map.completeEdit();
                    break;
                case DRAW_FEATURE:
                    MainActivity.this.map.completeDraw();
                    break;
                default:
                    Log.i(TAG, "Invalid draw mode");
                    break;
            }
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    public void editorCancel(View view) {
        try {
            //Log.d(TAG, "Edit/Draw Canceled.");
            switch (MainActivity.this.ePlotMode) {
                case EDIT_FEATURE:
                    MainActivity.this.map.cancelEdit();
                    break;
                case DRAW_FEATURE:
                    MainActivity.this.map.cancelDraw();
                    break;
                default:
                    Log.i(TAG, "Invalid draw mode");
                    break;
            }
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private void createCrossHair() {
        final mil.emp3.api.MapView mapView = mainBinding.map;
        final ImageView crossHairImage = mainBinding.CrossHairImage;
        final RelativeLayout crossHairLayout = mainBinding.CrossHairLayout;
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
        map.setFarDistanceThreshold(500000.0);
        map.setMidDistanceThreshold(490000.0);
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
            viewChange = map.addMapViewChangeEventListener(mapViewChangeEvent -> {
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
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapViewChangeEventListener failed. " + e.toString());
        }

        try {
            cameraHandler = oCamera.addCameraEventListener(cameraEvent -> {
                //Log.d(TAG, "cameraEvent on " + cameraEvent.getCamera().getName());

                final ICamera oCamera1 = cameraEvent.getTarget();

                if (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId()) {
                    MainActivity.this.runOnUiThread(() -> {
                        TextView oCameraSetting = (TextView) MainActivity.this.findViewById(R.id.camerasettings);
                        String sTemp = String.format("Lat:%7.5f\u00b0 Lon:%8.5f\u00b0 Alt:%,.0f m Heading:%3.0f\u00b0 Tilt:%3.0f\u00b0 Roll:%3.0f\u00b0", oCamera1.getLatitude(), oCamera1.getLongitude(), oCamera1.getAltitude(), oCamera1.getHeading(), oCamera1.getTilt(), oCamera1.getRoll());
                        oCameraSetting.setText(sTemp);
                    });
                } else {
                    TextView oCameraSetting = (TextView) MainActivity.this.findViewById(R.id.camerasettings);
                    String sTemp = String.format("Lat:%7.5f\u00b0 Lon:%8.5f\u00b0 Alt:%,.0f m Heading:%3.0f\u00b0 Tilt:%3.0f\u00b0 Roll:%3.0f\u00b0", oCamera1.getLatitude(), oCamera1.getLongitude(), oCamera1.getAltitude(), oCamera1.getHeading(), oCamera1.getTilt(), oCamera1.getRoll());
                    oCameraSetting.setText(sTemp);
                }
            });
        } catch (EMP_Exception e) {
            Log.e(TAG, "addCameraEventListener failed. " + e.toString());
        }

        this.oPerformanceDlg = mainBinding.performanceDialog;
        this.oCountTb = mainBinding.prfCount;
        this.oAffiliationCkb = mainBinding.prfChangeaffiliation;
        this.oBatchUpdateCkb = mainBinding.prfBatch;
        this.oResults = mainBinding.prfResults;
        this.oResultTextView = mainBinding.resultText;
        this.oResultTextView.setMovementMethod(new ScrollingMovementMethod());
        brightnessCtrl = mainBinding.brightnessValue;
        brightnessCtrl.setText("" + MainActivity.this.map.getBackgroundBrightness());
        mainBinding.farThreshold.post(() -> {
            mainBinding.farThreshold.setText(MainActivity.this.map.getFarDistanceThreshold() + " m");
        });
        mainBinding.midThreshold.post(() -> {
            mainBinding.midThreshold.setText(MainActivity.this.map.getMidDistanceThreshold() + " m");
        });
    }

    public void farUp(View v) {
        double dValue = MainActivity.this.map.getFarDistanceThreshold();
        dValue += 5000.0;
        MainActivity.this.map.setFarDistanceThreshold(dValue);
        dValue = MainActivity.this.map.getFarDistanceThreshold();
        mainBinding.farThreshold.setText(dValue + " m");
    }

    public void farDown(View v) {
        double dValue = MainActivity.this.map.getFarDistanceThreshold();
        dValue -= 5000.0;
        MainActivity.this.map.setFarDistanceThreshold(dValue);
        dValue = MainActivity.this.map.getFarDistanceThreshold();
        mainBinding.farThreshold.setText(dValue + " m");
    }

    public void midUp(View v) {
        double dValue = MainActivity.this.map.getMidDistanceThreshold();
        dValue += 5000.0;
        MainActivity.this.map.setMidDistanceThreshold(dValue);
        dValue = MainActivity.this.map.getMidDistanceThreshold();
        mainBinding.midThreshold.setText(dValue + " m");
    }

    public void midDown(View v) {
        double dValue = MainActivity.this.map.getMidDistanceThreshold();
        dValue -= 5000.0;
        MainActivity.this.map.setMidDistanceThreshold(dValue);
        dValue = MainActivity.this.map.getMidDistanceThreshold();
        mainBinding.midThreshold.setText(dValue + " m");
    }
    
    public void startBtn(View v) {
        mainBinding.startBtn.setEnabled(false);
        mainBinding.trackBtn.setEnabled(true);
        mainBinding.stopBtn.setEnabled(true);
        mainBinding.closeBtn.setEnabled(false);
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

    public void trackBtn(View v) {
        if (MainActivity.this.oPerformanceTestThread != null) {
            MainActivity.this.oPerformanceTestThread.toggleTrackingMode();
        }
    }

    public void stopBtn(View v) {
        if (MainActivity.this.oPerformanceTestThread != null) {
            MainActivity.this.oPerformanceTestThread.stopTest();
            MainActivity.this.oPerformanceTestThread = null;

            mainBinding.startBtn.setEnabled(true);
            mainBinding.trackBtn.setEnabled(false);
            mainBinding.stopBtn.setEnabled(false);
            mainBinding.closeBtn.setEnabled(true);
        }
    }

    public void closeBtn(View v) {
        MenuItem oItem = MainActivity.this.oMenu.findItem(R.id.action_tests);
        oItem.setEnabled(true);

        MainActivity.this.oPerformanceDlg.setVisibility(View.GONE);
        MainActivity.this.oPerformanceDlg.setEnabled(false);
    }


    public void brightnessUp(View v) {
        int value = Integer.parseInt(brightnessCtrl.getText().toString()) + 5;

        if (value > 100) {
            value = 100;
        }
        brightnessCtrl.setText("" + value);
        MainActivity.this.map.setBackgroundBrightness(value);
    }

    public void brightnessDown(View v) {
        int value = Integer.parseInt(brightnessCtrl.getText().toString()) - 5;

        if (value < 0) {
            value = 0;
        }
        brightnessCtrl.setText("" + value);
        MainActivity.this.map.setBackgroundBrightness(value);
    }


    public void zoomOut(View v) {
        ICamera camera = MainActivity.this.oCamera;
        double initAltitude = camera.getAltitude();
        if (initAltitude <= 1e8 / 1.2) {
            initAltitude *= 1.2;
            camera.setAltitude(initAltitude);
            camera.apply(false);
        }
    }
    
    // The Zoom+ button zooms 20% each time it is pressed
    // The altitude is limited to 10 m
    public void zoomIn(View v) {
        ICamera camera = MainActivity.this.oCamera;
        double initAltitude = camera.getAltitude();
        // lowest possible camera altitude set to 10 meters
        if (initAltitude > 10) {
            initAltitude /= 1.2;
            camera.setAltitude(initAltitude);
            camera.apply(false);
        }
    }

    // Pan left turns the camera left 5 degrees
    // each time the button is pressed
    public void panLeft(View v) {
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

    // Pan right turns the camera right 5 degrees
    // each time the button is pressed
    public void panRight(View v) {
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

    // Tilt up another 5 degrees, within limits
    public void tiltUp(View v) {
        ICamera camera = MainActivity.this.oCamera;
        try {
            double dTilt = camera.getTilt();
            if (dTilt < global.CAMERA_TILT_MAXIMUM) {
                dTilt += 5;
                camera.setTilt(dTilt);
                camera.apply(false);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    // Tilt down another 5 degrees, within limits
    public void tiltDown(View v) {
        ICamera camera = MainActivity.this.oCamera;
        try {
            double dTilt = camera.getTilt();

            if (dTilt > global.CAMERA_TILT_MINIMUM) {
                dTilt -= 5;
                camera.setTilt(dTilt);
                camera.apply(false);
            }
        } catch (Exception e) {
            //e.printStackTrace();
        }
    }

    public void rollCCW(View v) {
        ICamera camera = MainActivity.this.oCamera;
        try {
            double dRoll = camera.getRoll();
            if (dRoll <= 175.0) {
                dRoll += 5;
                camera.setRoll(dRoll);
                camera.apply(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void rollCW(View v) {
        ICamera camera = MainActivity.this.oCamera;
        try {
            double dRoll = camera.getRoll();
            if (dRoll >= -175.0) {
                dRoll -= 5;
                camera.setRoll(dRoll);
                camera.apply(false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void resetCamera(View v) {
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

    private void getGpkgList() {
        try {
            downloadFolder.mkdirs();
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        if (downloadFolder.exists()) {
            FilenameFilter filter = (dir, filename) -> {
                File sel = new File(dir, filename);
                return filename.contains(SUFFIX) || sel.isDirectory();
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
            /*
            case R.id.action_mirrorcache_connect: {
                MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_connect).setEnabled(false);
                LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.mirrorcache_url_dialog, null);
                new AlertDialog.Builder(MainActivity.this)
                               .setMessage("WebSocket endpoint:")
                               .setView(v)
                               .setNegativeButton(android.R.string.no, null)
                               .setPositiveButton(android.R.string.yes, (dialog, which) -> {
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
                LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.mirrorcache_subscribe_dialog, null);
                new AlertDialog.Builder(MainActivity.this)
                               .setMessage("Product Id:")
                               .setView(v)
                               .setNegativeButton(android.R.string.no, null)
                               .setPositiveButton(android.R.string.yes, (dialog, which) -> {
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
                               }).create().show();
                return true;
            }
            case R.id.action_mirrorcache_unsubscribe: {
                MainActivity.this.oMenu.findItem(R.id.action_mirrorcache_unsubscribe).setEnabled(false);
                LayoutInflater inflater = (LayoutInflater)this.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.mirrorcache_subscribe_dialog, null);
                new AlertDialog.Builder(MainActivity.this)
                               .setMessage("Product Id:")
                               .setView(v)
                               .setNegativeButton(android.R.string.no, null)
                               .setPositiveButton(android.R.string.yes, (dialog, which) -> {
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
            }*/
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
            case R.id.action_exportMapToKMZ:
            {
                try
                {
                    final String kmzFileName = "KmzMap.kmz";
                    final File defaultDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    final File kmzExport = new File(defaultDirectory, String.format("KMZExport%s%s", File.separator, kmzFileName));
                    if(kmzExport.exists())
                    {
                        kmzExport.delete();
                    }
                    EmpKMZExporter.exportToKMZ(this.map,
                                               true,
                                               new IEmpExportToTypeCallBack<File>(){
                                                                                       @Override
                                                                                       public void exportSuccess(File exportObject)
                                                                                       {
                                                                                           MainActivity.this.makeToast("Export Successful");
                                                                                       }
                                                                                       @Override
                                                                                       public void exportFailed(Exception Ex)
                                                                                       {
                                                                                           Log.e(TAG, "Map export to KMZ failed.", Ex);
                                                                                           MainActivity.this.makeToast(String.format("Export failed %s", Ex.getMessage()));
                                                                                       }
                                                                                   },
                                               getApplicationContext().getExternalFilesDir(null).getAbsolutePath(),
                                    kmzFileName);
                }
                catch (Exception Ex)
                {
                    Log.e(TAG, "Map export to KMZ failed.", Ex);
                    MainActivity.this.makeToast(String.format("Export failed %s", Ex.getMessage()));
                }

                return true;
            }
            case R.id.action_exportOverlayToKMZ:
            {
                try
                {
                    final String kmzFileName = "KmzOverlay.kmz";
                    final File defaultDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    final File kmzExport = new File(defaultDirectory, String.format("KMZExport%s%s", File.separator, kmzFileName));
                    if(kmzExport.exists())
                    {
                        kmzExport.delete();
                    }
                    EmpKMZExporter.exportToKMZ(this.map,
                                               this.oRootOverlay,
                                               true,
                                               new IEmpExportToTypeCallBack<File>(){
                                                                                       @Override
                                                                                       public void exportSuccess(File exportObject)
                                                                                       {
                                                                                           MainActivity.this.makeToast("Export Successful");
                                                                                       }

                                                                                       @Override
                                                                                       public void exportFailed(Exception Ex)
                                                                                       {
                                                                                           Log.e(TAG, "Map export to KMZ failed.", Ex);
                                                                                           MainActivity.this.makeToast("Export failed");
                                                                                       }
                                                                                   },
                                               getApplicationContext().getExternalFilesDir(null).getAbsolutePath(),
                                               kmzFileName);
                }
                catch (Exception Ex)
                {
                    Log.e(TAG, "Map export to KMZ failed.", Ex);
                    MainActivity.this.makeToast("Export failed");
                }

                return true;
            }
            case R.id.action_exportFeatureToKMZ:
            {
                try
                {
//                    if (this.oCurrentSelectedFeature == null)
//                    {
//                        MainActivity.this.makeToast("Cannot Export Feature unless a feature is selected.");
//                        return true;
//                    }

                    final MilStdSymbol milSymbol = generateMilStdSymbol("TRUCK", UUID.randomUUID(),40, -75);
                    final Overlay overlay = new Overlay();
                    overlay.setName("MilStdSymbolTest");
                    this.map.addOverlay(overlay, true);
                    overlay.addFeature(milSymbol, true);

                    final String kmzFileName = "KmzFeature.kmz";
                    final File defaultDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    final File kmzExport = new File(defaultDirectory, String.format("KMZExport%s%s", File.separator, kmzFileName));
                    if(kmzExport.exists())
                    {
                        kmzExport.delete();
                    }

                    EmpKMZExporter.exportToKMZ(this.map,
                                               this.oCurrentSelectedFeature,
                                               true,
                                               new IEmpExportToTypeCallBack<File>()
                                                                                   {
                                                                                       @Override
                                                                                       public void exportSuccess(File exportObject)
                                                                                       {
                                                                                           MainActivity.this.makeToast("Export Successful");
                                                                                       }
                                                                                       @Override
                                                                                       public void exportFailed(Exception Ex)
                                                                                       {
                                                                                           Log.e(TAG, "Map export to KMZ failed.", Ex);
                                                                                           MainActivity.this.makeToast("Export failed");
                                                                                       }
                                                                                   },
                                               getApplicationContext().getExternalFilesDir(null).getAbsolutePath(),
                                               kmzFileName);
                } catch (Exception Ex)
                {
                    Log.e(TAG, "Map export to KMZ failed.", Ex);
                    MainActivity.this.makeToast("Export failed");
                }
                return true;
            }

//=======
            //Test for many features on the map
            case R.id.action_importManyKMZ: {
                try( InputStream stream = getApplicationContext().getResources().openRawResource(R.raw.many))
                {
                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);

                    File targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +File.separator + "many.kmz");
                    if(targetFile.exists()){
                        targetFile.delete();
                    }
                    try(OutputStream outStream = new FileOutputStream(targetFile))
                    {
                        outStream.write(buffer);

                        Context context = getApplicationContext();
                        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();

                        IMapService mapService = new KMLS(context, targetFile.toURI().toURL().toString(), new KMLSServiceListener(queue));
                        mapService.setName("kmzSample_Test");
                        this.map.addMapService(mapService);
                    }

                } catch (Exception e) {
                }
                return true;
            }
            //Test for handdrawn pictures and a few downloaded symbols
            case R.id.action_importExampleKMZ: {
                try( InputStream stream = getApplicationContext().getResources().openRawResource(R.raw.example))
                {
                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);

                    File targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +File.separator + "example.kmz");
                    if(targetFile.exists()){
                        targetFile.delete();
                    }
                    try(OutputStream outStream = new FileOutputStream(targetFile))
                    {
                        outStream.write(buffer);

                        Context context = getApplicationContext();
                        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();

                        IMapService mapService = new KMLS(context, targetFile.toURI().toURL().toString(), new KMLSServiceListener(queue));
                        mapService.setName("kmzSample_Test");
                        this.map.addMapService(mapService);
                    }

                } catch (Exception e) {
                }
                return true;
            }
            //Test more handdrawn symbols
            case R.id.action_importSymbolsKMZ: {
                try( InputStream stream = getApplicationContext().getResources().openRawResource(R.raw.symbols))
                {
                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);

                    File targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +File.separator + "symbols.kmz");
                    if(targetFile.exists()){
                        targetFile.delete();
                    }
                    try(OutputStream outStream = new FileOutputStream(targetFile))
                    {
                        outStream.write(buffer);

                        Context context = getApplicationContext();
                        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();

                        IMapService mapService = new KMLS(context, targetFile.toURI().toURL().toString(), new KMLSServiceListener(queue));
                        mapService.setName("kmzSample_Test");
                        this.map.addMapService(mapService);
                    }

                } catch (Exception e) {
                }
                return true;
            }
            //Test multiple overlayws
            case R.id.action_importOverlayKMZ: {
                try( InputStream stream = getApplicationContext().getResources().openRawResource(R.raw.overlays))
                {
                    byte[] buffer = new byte[stream.available()];
                    stream.read(buffer);

                    File targetFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString() +File.separator + "overlay.kmz");
                    if(targetFile.exists()){
                        targetFile.delete();
                    }
                    try(OutputStream outStream = new FileOutputStream(targetFile))
                    {
                        outStream.write(buffer);

                        Context context = getApplicationContext();
                        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();

                        IMapService mapService = new KMLS(context, targetFile.toURI().toURL().toString(), new KMLSServiceListener(queue));
                        mapService.setName("kmzOutput_test");
                        this.map.addMapService(mapService);
                    }

                } catch (Exception e) {
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
                    GeoJsonCallBack callback = new GeoJsonCallBack();
                    if (featureList.size() == 1) {
                        GeoJsonCaller.exportToString(map, featureList.get(0), false, callback);
                    } else {
                        GeoJsonCaller.exportToString(map, featureList, false, callback);
                    }
                    // repeat with map and overlay calls
                    if (map.getAllOverlays().size() > 0) {
                        GeoJsonCaller.exportToString(map, map.getAllOverlays().get(0), false, callback);
                    }
                    GeoJsonCaller.exportToString(map, false, callback);
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
                                (dialog1, which) -> dialog1.dismiss());
                        dialog.show();
                    } else {
                        builder.setTitle("Choose GeoPackage file");
                        builder.setItems(gpkgList, (dialog, which) -> {
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
                try (InputStream stream = getApplicationContext().getResources().openRawResource(R.raw.kml_samples))
                {
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
                    KML kmlFeature = new KML(stream);
                    this.oRootOverlay.addFeature(kmlFeature, true);
                    this.oFeatureHash.put(kmlFeature.getGeoId(), kmlFeature);
                    ICamera camera = this.map.getCamera();
                    camera.setAltitude(400);
                    camera.setLongitude(-122.08447);
                    camera.setLatitude(37.42198);
                    camera.apply(true);
                } catch (Exception Ex) {
                    Log.e(TAG, "KML failed.", Ex);
                }
                return true;
            }
            case R.id.action_addGeoJSON:
                InputStream stream = null;
                try {
                    stream = getApplicationContext().getResources().openRawResource(R.raw.communes_69);
                    IFeature feature = new GeoJSON(stream);
                    Log.i(TAG, feature.toString());
                    this.oRootOverlay.addFeature(feature, true);
                    this.oFeatureHash.put(feature.getGeoId(), feature);
                    stream.close();
                    stream = getApplicationContext().getResources().openRawResource(R.raw.random_geoms);
                    feature = new GeoJSON(stream);
                    Log.i(TAG, feature.toString());
                    this.oRootOverlay.addFeature(feature, true);
                    this.oFeatureHash.put(feature.getGeoId(), feature);
                    stream.close();
                    stream = getApplicationContext().getResources().openRawResource(R.raw.rhone);
                    feature = new GeoJSON(stream);
                    Log.i(TAG, feature.toString());
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
                LayoutInflater inflater = (LayoutInflater)getApplicationContext().getSystemService (Context.LAYOUT_INFLATER_SERVICE);
                View v = inflater.inflate(R.layout.plot_unit_dialog, null);
                new AlertDialog.Builder(MainActivity.this)
                        .setMessage("Number of Units:")
                        .setView(v)
                        .setNegativeButton(android.R.string.no, null)
                        .setPositiveButton(android.R.string.yes, (dialog, which) -> {
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
                try {
                    this.map.getMapServices();
                    ArrayList<String> layers = new ArrayList<>();
                    layers.add("matrikkel_bakgrunn");

                    wmtsService = new WMTS(
                            "http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts",
                            null, null, layers);
                    Log.i(TAG, wmtsService.toString());
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
                    this.map.getMapServices();
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
                    this.map.getMapServices();
                    final Dialog dialog = new Dialog(MainActivity.this);
                    dialog.setContentView(R.layout.wcs_parameters_dialog);
                    dialog.setTitle("Title...");
                    Button okButton = (Button) dialog.findViewById(R.id.OKButton);
                    // if button is clicked, close the custom dialog
                    okButton.setOnClickListener(v1 -> {
                        EditText urlText = (EditText) dialog.findViewById(R.id.WcsUrlText);
                        EditText coverageText = (EditText) dialog.findViewById(R.id.CoverageText);

                        String url = urlText.getText().toString();
                        dialog.dismiss();

                        try {
                            wcsService = new WCS(url,
                                    coverageText.getText().toString());
                            Log.i(TAG, wcsService.toString());
                            map.addMapService(wcsService);
                            MenuItem oItem = MainActivity.this.oMenu.findItem(R.id.action_removeWCS);
                            oItem.setEnabled(true);
                            oItem = MainActivity.this.oMenu.findItem(R.id.action_addWCS);
                            oItem.setEnabled(false);
                            ILookAt calculatedLookAt = CameraUtility.setupLookAt(37.5, -105.5, 5000,
                                    37.577227, -105.485845, 4374);
                            map.setLookAt(calculatedLookAt, false);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    Button cancelButton = (Button) dialog.findViewById(R.id.CancelButton);
                    // if button is clicked, don't add WMS service
                    cancelButton.setOnClickListener(v12 -> dialog.dismiss());
                    dialog.show();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_removeWCS:
                try {
                    this.map.getMapServices();
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
                    this.map.getMapServices();
                    if (wmsBinding == null) {
                        wmsBinding = DataBindingUtil.inflate(LayoutInflater.from(MainActivity.this),
                                R.layout.wms_parameters_dialog, null, false);
                        wmsDialog = new Dialog(MainActivity.this);
                        wmsDialog.setContentView(wmsBinding.getRoot());
                        wmsDialog.setTitle("Title...");
                        Button okButton = (Button) wmsDialog.findViewById(R.id.OKButton);
                        // if button is clicked, close the custom dialog
                        okButton.setOnClickListener(v1 -> {
                            String url = wmsBinding.UrlText.getText().toString();
                            String version = wmsBinding.VersionText.getText().toString();
                            if (version == null)
                                version = "";
                            String tileFormat = wmsBinding.TileFormatText.getText().toString();
                            String transparent = wmsBinding.TransparentText.getText().toString();
                            String layer = wmsBinding.LayerText.getText().toString();
                            String resolution = wmsBinding.ResolutionText.getText().toString();
                            Resources res = getBaseContext().getResources();
                            wmsDialog.dismiss();
                            WMSVersionEnum wmsVersion = null;
                            switch (version) {
                                case "1.1.1":
                                    wmsVersion = WMSVersionEnum.VERSION_1_1_1;
                                    break;
                                case "1.1":
                                    wmsVersion = WMSVersionEnum.VERSION_1_1;
                                    break;
                                case "1.3.0":
                                    wmsVersion = WMSVersionEnum.VERSION_1_3_0;
                                    break;
                                case "1.3":
                                    wmsVersion = WMSVersionEnum.VERSION_1_3;
                                    break;
                                default:
                                    wmsVersion = WMSVersionEnum.VERSION_1_3_0;
                                    ;
                            }
                            ArrayList<String> layers1 = new ArrayList<>();
                            layers1.add(layer);
                            try {
                                wmsService = new WMS(
                                        url,
                                        wmsVersion,
                                        tileFormat,
                                        transparent.equalsIgnoreCase("true"),
                                        layers1
                                );
                                Log.i(TAG, wmsService.toString());
                            } catch (MalformedURLException ex) {
                                ex.printStackTrace();
                            }
                            MainActivity.this.wmsService.setLayerResolution(Double.valueOf(resolution));
                            try {
                                map.addMapService(MainActivity.this.wmsService,
                                        (success, geoId, t) -> {
                                            if (success) {
                                                Toast.makeText(MainActivity.this, "WMS connected", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(MainActivity.this, t.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            } catch (EMP_Exception e) {
                                e.printStackTrace();
                            }
                            MenuItem oItem = MainActivity.this.oMenu.findItem(R.id.action_removeWMS);
                            oItem.setEnabled(true);
                            oItem = MainActivity.this.oMenu.findItem(R.id.action_addWMS);
                            oItem.setEnabled(false);
                        });
                        Button cancelButton = (Button) wmsDialog.findViewById(R.id.CancelButton);
                        // if button is clicked, don't add WMS service
                        cancelButton.setOnClickListener(v12 -> wmsDialog.dismiss());
                    }
                    wmsDialog.show();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                return true;
            case R.id.action_removeWMS:
                try {
                    this.map.getMapServices();
                    map.removeMapService(this.wmsService);
                    MenuItem oItem = this.oMenu.findItem(R.id.action_removeWMS);
                    oItem.setEnabled(false);
                    oItem = this.oMenu.findItem(R.id.action_addWMS);
                    oItem.setEnabled(true);
                } catch (EMP_Exception ex) {
                }
            return true;
            case R.id.action_addLoS:
                try {
//                    if (wcsService != null) {
//                        map.removeMapService(wcsService);
//                    }
//                    wcsService = new WCS ("https://worldwind26.arc.nasa.gov/wcs", "USGS-NED");
                    // instead of hard coding, let user add WCS first
                    this.map.getMapServices();
                    map.addMapService(wcsService);
                    Thread.sleep(1000);
                    EmpGeoPosition position = new EmpGeoPosition(46.230, -122.190, 2500.0);
                    EmpGeoColor visibleAttr = new EmpGeoColor(0.5d, 0, 25, 0);
                    EmpGeoColor occludeAttr = new EmpGeoColor(0.8d, 25, 25, 25);
                    double range = 10000.0d;
                    los = new LineOfSight(position, range, visibleAttr, occludeAttr);
                    map.addMapService(los);
                    LookAt lookAt = new LookAt(46.230, -122.190, 500, IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                    lookAt.setRange(1.5e4); /*range*/
                    lookAt.setHeading(45.0); /*heading*/
                    lookAt.setTilt(70.0); /*tilt*/
                    /*0 roll*/
                    ;
                    map.setLookAt(lookAt, false);
                    MenuItem oItem = this.oMenu.findItem(R.id.action_addLoS);
                    oItem.setEnabled(false);
                    oItem = this.oMenu.findItem(R.id.action_removeLoS);
                    oItem.setEnabled(true);
                } catch (EMP_Exception | InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            case R.id.action_removeLoS:
                try {
                    this.map.getMapServices();
                    map.removeMapService(los);
                    MenuItem oItem = this.oMenu.findItem(R.id.action_removeLoS);
                    oItem.setEnabled(false);
                    oItem = this.oMenu.findItem(R.id.action_addLoS);
                    oItem.setEnabled(true);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
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
                    Log.i(TAG, oPoint.toString());
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
                    Log.i(TAG, oPoint.toString());
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

                    mainBinding.startBtn.setEnabled(true);
                    mainBinding.stopBtn.setEnabled(false);
                    mainBinding.closeBtn.setEnabled(true);
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
                    mainBinding.resultPanel.setVisibility(View.VISIBLE);
                    mainBinding.resultPanel.setEnabled(true);
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
                mainBinding.resultPanel.setVisibility(View.GONE);
                mainBinding.resultPanel.setEnabled(false);
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
                Log.i(TAG, textFeature.toString());
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
                Log.i(TAG, linePath.toString());
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
                Log.i(TAG, polygon.toString());
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
                strokeStyle.setStipplingPattern((short) 0xEEEE);
                strokeStyle.setStipplingFactor(3);
                circle.setStrokeStyle(strokeStyle);

                fillStyle.setFillColor(geoFillColor);
                fillStyle.setFillPattern(IGeoFillStyle.FillPattern.hatched);
                //circle.setFillStyle(fillStyle);
                circle.setName("Circle");
                circle.setDescription("Draw Circle Feature.");

                circle.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                Log.i(TAG, circle.toString());
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
                Log.i(TAG, ellipse.toString());
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
                Log.i(TAG, feature.toString());
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
                Log.i(TAG, feature.toString());
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

            case R.id.action_toggleLock: {
                try {
                    if (map.getMotionLockMode() == MapMotionLockEnum.SMART_LOCK) {
                        Toast.makeText(MainActivity.this.getApplicationContext(),
                                "Map motion mode is SMART_LOCK, toggle failed",
                                Toast.LENGTH_SHORT).show();
                    } else if (map.getMotionLockMode() != MapMotionLockEnum.LOCKED) {
                        map.setMotionLockMode(MapMotionLockEnum.LOCKED);
                    } else {
                        map.setMotionLockMode(MapMotionLockEnum.UNLOCKED);
                    }
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
                break;
            }

            case R.id.action_toggleSmartLock: {
                try {
                    if (map.getMotionLockMode() == MapMotionLockEnum.LOCKED) {
                        Toast.makeText(MainActivity.this.getApplicationContext(),
                                "Map motion mode is LOCKED, toggle failed",
                                Toast.LENGTH_SHORT).show();
                    } else if (map.getMotionLockMode() != MapMotionLockEnum.SMART_LOCK) {
                        map.setMotionLockMode(MapMotionLockEnum.SMART_LOCK);
                    } else {
                        map.setMotionLockMode(MapMotionLockEnum.UNLOCKED);
                    }
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
                break;
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
        Map<java.lang.String, armyc2.c2sd.renderer.utilities.SymbolDef> oDefList = oDefTable.GetAllSymbolDefs( armyc2.c2sd.renderer.utilities.MilStdSymbol.Symbology_2525C);

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
                GeographicLib.computePositionAt(180.0, 2500.0, position, position);
                countPerLine = 0;
            } else {
                GeographicLib.computePositionAt(90.0, 2500.0, position, position);
            }
        }
    }

    private void plotTacticalGraphic(String name, armyc2.c2sd.renderer.utilities.SymbolDef oSymbolDef, IGeoPosition position) {
        try {
            List<IGeoPosition> posList = new ArrayList<>();
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
                        pos = GeographicLib.computePositionAt(270, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeographicLib.computePositionAt(90, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                    } else {
                        pos = new GeoPosition();
                        pos.setLatitude(position.getLatitude());
                        pos.setLongitude(position.getLongitude());
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeographicLib.computePositionAt(90, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                    }

                    break;
                case 3:
                default:
                    pos = GeographicLib.computePositionAt(240.0, 1000.0, position);
                    pos.setAltitude(0.0);
                    posList.add(pos);
                    pos = GeographicLib.computePositionAt(0.0, 1000.0, position);
                    pos.setAltitude(0.0);
                    posList.add(pos);
                    pos = GeographicLib.computePositionAt(120.0, 1000.0, position);
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
                        GeographicLib.computePositionAt(90.0, 500.0, posList.get(0), pos);
                        pos.setAltitude(0.0);
                        posList.add(pos);

                        pos = new GeoPosition();
                        GeographicLib.computePositionAt(90.0, 1000.0, posList.get(0), pos);
                        pos.setAltitude(0.0);
                        posList.add(pos);

                        pos = new GeoPosition();
                        GeographicLib.computePositionAt(90.0, 1500.0, posList.get(0), pos);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                    } else {
                        pos = GeographicLib.computePositionAt(225.0, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeographicLib.computePositionAt(315.0, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeographicLib.computePositionAt(45.0, 1000.0, position);
                        pos.setAltitude(0.0);
                        posList.add(pos);
                        pos = GeographicLib.computePositionAt(135.0, 1000.0, position);
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
                                symbol.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 0, 0);
                            }
                            break;
                        case "AN#":
                            if (CoreMilStdUtilities.SECTOR_RANGE_FAN.equals(symbol.getBasicSymbol()) ||
                                    CoreMilStdUtilities.CIRCULAR_RANGE_FAN.equals(symbol.getBasicSymbol())) {
                            } else {
                                symbol.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 0, 45);
                                symbol.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 1, 315);
                                symbol.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 2, 45);
                                symbol.setModifier(IGeoMilSymbol.Modifier.AZIMUTH, 3, 315);
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

                    this.setModifiers(oSymbol, symbolDef);
/*
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
*/
                    this.map.drawFeature(oSymbol, new FeatureDrawListener(oSymbol));
                } catch (EMP_Exception e) {
                    Log.d(TAG, "Cant draw " + dialog.getSymbolCode(), e);
                    this.ePlotMode = PlotModeEnum.IDLE;
                }
                break;
            }
        }
    }

    private void setModifiers(MilStdSymbol symbol, armyc2.c2sd.renderer.utilities.SymbolDef symbolDef) {
        try {
            switch (symbol.getBasicSymbol()) {
                case "G*G*GAA---****X": // ASSEMBLY AREA
                    symbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.COMPANY_BATTERY_TROOP);
                    break;
                case "G*G*OPP---****X": // POINT OF DEPARTURE
                    symbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.COMPANY_BATTERY_TROOP);
                    break;
                case "G*G*GLB---****X": // BOUNDARIES
                    symbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.DIVISION);
                    break;
            }
        } catch (Exception Ex) {}

        if (true) {
            // For now add them all.
            String modifiers = "W.W1.T.T1.X.X1.H.H1.H2.N.C.A.B.Y.V.Q";
            // Needs modifiers.
            String[] aModifiers = modifiers.split("\\.");
            for (int index = 0; index < aModifiers.length; index++) {
                switch (aModifiers[index]) {
                    case "W":
                        symbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP, "0800Z");
                        break;
                    case "W1":
                        symbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP_2, "2300Z");
                        break;
                    case "T":
                        switch (symbol.getBasicSymbol()) {
                            case CoreMilStdUtilities.BOUNDARY:
                                symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "10 Mtn Div");
                                break;
                            default:
                                if ((null != symbol.getName()) && !symbol.getName().isEmpty()) {
                                    symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, symbol.getName());
                                } else {
                                    symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "UD1");
                                }
                                break;
                        }
                        break;
                    case "T1":
                        switch (symbol.getBasicSymbol()) {
                            case CoreMilStdUtilities.BOUNDARY:
                                symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_2, "3 Inf Div");
                                break;
                            default:
                                symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_2, "UD2");
                                break;
                        }
                        break;
                    case "AM":
                        break;
                    case "AM#":
                        break;
                    case "AN":
                        break;
                    case "AN#":
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
                        Log.e(TAG, "Modifier not set " + aModifiers[index] + ". " + symbolDef.getDescription() + " getModifiers() " + symbolDef.getModifiers());
                        break;
                }
            }
        }

        if (null == symbol.getStringModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1)) {
            if ((null != symbol.getName()) && !symbol.getName().isEmpty()) {
                symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, symbol.getName());
            } else {
                symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "UD1");
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
                    List<IGeoPosition> oPositionList = new ArrayList<>();
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
                            mainBinding.editorCompleteBtn.show();
                            mainBinding.editorCancelBtn.show();
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
                            mainBinding.editorCompleteBtn.hide();
                            mainBinding.editorCancelBtn.hide();
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
                            mainBinding.editorCompleteBtn.hide();
                            mainBinding.editorCancelBtn.hide();
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
        handler.post(() -> Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show());
    }
}
