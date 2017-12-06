package mil.emp3.test.emp3vv.navItems.basic_shapes_editors;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.cmapi.primitives.IGeoAltitudeMode;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.Text;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.events.FeatureDrawEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFeatureDrawEventListener;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.AddMilStdSymbol;
import mil.emp3.test.emp3vv.containers.dialogs.milstdtacticalgraphics.TacticalGraphicPropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.milstdunits.SymbolPropertiesDialog;

import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.CameraUtility;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;


public class BasicShapesEditorsTest extends NavItemBase {
    private static String TAG = BasicShapesEditorsTest.class.getSimpleName();

    DrawEventListener[] drawEventListener = new DrawEventListener[ExecuteTest.MAX_MAPS];
    FeatureDrawEventListener[] featureDrawEventListener = new FeatureDrawEventListener[ExecuteTest.MAX_MAPS];
    EventListenerHandle[] featureDrawEventListenerHandle = new EventListenerHandle[ExecuteTest.MAX_MAPS];
    EditEventListener[] editEventListener = new EditEventListener[ExecuteTest.MAX_MAPS];

    private final StyleManager styleManager;

    public BasicShapesEditorsTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
        styleManager = new StyleManager(activity, maps);
    }

    private static List<String> featureTypeNames = new ArrayList<>();
    private static java.util.Map<String, Class<? extends Feature>> featureType2Class;
    static {
        featureType2Class = new HashMap<>();
        featureType2Class.put("GEO_RECTANGLE", Rectangle.class);
        featureType2Class.put("GEO_POLYGON", Polygon.class);
        featureType2Class.put("GEO_SQUARE", Square.class);
        featureType2Class.put("GEO_ELLIPSE", Ellipse.class);
        featureType2Class.put("GEO_CIRCLE", Circle.class);
        featureType2Class.put("GEO_MIL_SYMBOL", MilStdSymbol.class);
        featureType2Class.put("GEO_PATH", Path.class);
        featureType2Class.put("GEO_POINT", Point.class);
        featureType2Class.put("GEO_TEXT", Text.class);


        for(FeatureTypeEnum fte: FeatureTypeEnum.values()) {
            featureTypeNames.add(fte.toString());
        }
    }
    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Draw Shape", "Edit Shape", "Complete", "Cancel"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = { "Add IDrawEventListener", "Add IFeatureDrawEventListener", "Add IEditEventListener", "Remove Listeners" };
        return styleManager.getMoreActions(actions);
    }
    protected void test0() {

        try {
            SymbolPropertiesDialog.loadSymbolTables();
            testThread = Thread.currentThread();
            while (!Thread.interrupted()) {
                try {
                    Thread.sleep(large_waitInterval * 10);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        } finally {
            endTest();
        }
    }

    @Override
    public boolean actOn(String userAction) {
        final int whichMap = ExecuteTest.getCurrentMap();

        try {

            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                for(int ii = 0; ii < ExecuteTest.MAX_MAPS; ii++) {
                    drawEventListener[ii] = null;
                    if (null != featureDrawEventListenerHandle[ii]) {
                        maps[ii].removeEventListener(featureDrawEventListenerHandle[ii]);
                        featureDrawEventListener[ii] = null;
                        featureDrawEventListenerHandle[ii] = null;
                    }
                    editEventListener[ii] = null;

                    if(null != maps[ii]) {
                        try {
                            maps[ii].cancelEdit();
                        } catch(EMP_Exception e) {

                        }

                        try {
                            maps[ii].cancelDraw();
                        } catch(EMP_Exception e) {

                        }
                    }
                }
                clearMaps();
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                clearMaps();
            } else if(userAction.contains("Draw Shape")) {
                final int selection = 0;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        final ArrayAdapter<String> featureTypeAdapterAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_checked, featureTypeNames);
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                                .setTitle("Choose Feature Type")
                                .setSingleChoiceItems(featureTypeAdapterAdapter, selection, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d(TAG, "Selected Feature " + which);
                                        Class<? extends Feature> featureClass = featureType2Class.get(featureTypeNames.get(which));
                                        if (null == featureClass) {
                                            ErrorDialog.showError(activity, featureTypeNames.get(which) + " is not yet supported for Draw");
                                        } else {
                                            if(featureTypeNames.get(which).equals("GEO_MIL_SYMBOL")) {
                                                singlePointOrTacticalGraphics(which, "Feature To Draw", false);
                                            } else {
                                                try {
                                                    Constructor<? extends Feature> c = featureClass.getConstructor();
                                                    IFeature featureInstance = c.newInstance();
                                                    styleManager.setStyles(featureInstance);
                                                    if (null != drawEventListener[whichMap]) {
                                                        maps[whichMap].drawFeature(featureInstance, drawEventListener[whichMap]);
                                                    } else {
                                                        maps[whichMap].drawFeature(featureInstance);
                                                    }
                                                } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException | EMP_Exception e) {
                                                    Log.e(TAG, "constructor not found for " + featureTypeNames.get(which));
                                                    updateStatus(TAG, e.getMessage());
                                                    ErrorDialog.showError(activity, e.getMessage());
                                                }
                                            }
                                        }
                                        dialog.cancel();
                                    }
                                });

                        final AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            } else if(userAction.contains("Edit Shape")) {
                final int selection = 0;
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        final ArrayAdapter<String> featureTypeAdapterAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_checked, featureTypeNames);
                        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                                .setTitle("Choose Feature Type")
                                .setSingleChoiceItems(featureTypeAdapterAdapter, selection, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        Log.d(TAG, "Selected Feature " + which);
                                        Class<? extends Feature> featureClass = featureType2Class.get(featureTypeNames.get(which));
                                        if (null == featureClass) {
                                            ErrorDialog.showError(activity, featureTypeNames.get(which) + " is not yet supported for Edit");
                                        } else {
                                            if(featureTypeNames.get(which).equals("GEO_MIL_SYMBOL")) {
                                                singlePointOrTacticalGraphics(which, "Feature To Edit", true);
                                            } else {
                                                Feature feature = createFeature(whichMap, featureClass);
                                                if (null != feature) {
                                                    try {
                                                        if (null != editEventListener[whichMap]) {
                                                            maps[whichMap].editFeature(feature, editEventListener[whichMap]);
                                                        } else {
                                                            maps[whichMap].editFeature(feature);
                                                        }
                                                    } catch (EMP_Exception e) {
                                                        Log.e(TAG, "failed to invoke editFeature " + featureTypeNames.get(which));
                                                        updateStatus(TAG, e.getMessage());
                                                        ErrorDialog.showError(activity, e.getMessage());
                                                    }
                                                }
                                            }
                                        }
                                        dialog.cancel();
                                    }
                                });

                        final AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
            } else if(userAction.equals("Complete")) {
                if(maps[whichMap].getEditorMode() == EditorMode.DRAW_MODE) {
                    maps[whichMap].completeDraw();
                } else if(maps[whichMap].getEditorMode() == EditorMode.EDIT_MODE) {
                    maps[whichMap].completeEdit();
                } else {
                    ErrorDialog.showError(activity, "Map is neither in EDIT nor in DRAW mode");
                }
            } else if(userAction.equals("Cancel")) {
                if(maps[whichMap].getEditorMode() == EditorMode.DRAW_MODE) {
                    maps[whichMap].cancelDraw();
                } else if(maps[whichMap].getEditorMode() == EditorMode.EDIT_MODE) {
                    maps[whichMap].cancelEdit();
                } else {
                    ErrorDialog.showError(activity, "Map is neither in EDIT nor in DRAW mode");
                }
            } else if(userAction.equals("Add IDrawEventListener")) {
                if(null == drawEventListener[whichMap]) {
                    if (featureDrawEventListenerHandle[whichMap] != null) {
                        maps[whichMap].removeEventListener(featureDrawEventListenerHandle[whichMap]);
                        featureDrawEventListener[whichMap] = null;
                        featureDrawEventListenerHandle[whichMap] = null;
                    }
                    drawEventListener[whichMap] = new DrawEventListener();
                }
            } else if(userAction.equals("Add IFeatureDrawEventListener")) {
                if(null == featureDrawEventListener[whichMap]) {
                    drawEventListener[whichMap] = null;
                    featureDrawEventListener[whichMap] = new FeatureDrawEventListener();
                    featureDrawEventListenerHandle[whichMap] = maps[whichMap].addFeatureDrawEventListener(featureDrawEventListener[whichMap]);
                }
            } else if(userAction.equals("Add IEditEventListener")) {
                if(null == editEventListener[whichMap]) {
                   editEventListener[whichMap] = new EditEventListener();
                }
            } else if(userAction.equals("Remove Listeners")) {
                drawEventListener[whichMap] = null;
                if(null != featureDrawEventListenerHandle[whichMap]) {
                    maps[whichMap].removeEventListener(featureDrawEventListenerHandle[whichMap]);
                    featureDrawEventListener[whichMap] = null;
                    featureDrawEventListenerHandle[whichMap] = null;
                }
                editEventListener[whichMap] = null;
            } else {
                styleManager.actOn(userAction);
            }
        } catch (Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }

        return true;
    }

    @Override
    protected void clearMapForTest() {
        String userAction = "ClearMap";
        actOn(userAction);
    }

    @Override
    protected boolean exitTest() {
        String userAction = "Exit";
        return(actOn(userAction));
    }

    private Feature createFeature(int whichMap, Class<? extends Feature> featureClass) {
        Feature createdFeature = null;
        IOverlay overlay = null;

        List<IFeature> features = maps[whichMap].getAllFeatures();
        if(null != features) {
            for(IFeature f: features) {
                if(f.getClass().getCanonicalName().equals(featureClass.getCanonicalName())) {
                    createdFeature = (Feature) f;
                    break;
                }
            }
        }

        if(null != createdFeature) {
            Log.d(TAG, "Found existing feature");
        }

        if(null == createdFeature) {
            overlay = createOverlay(maps[whichMap]);

            try {
                Constructor<? extends Feature> c = featureClass.getConstructor();
                createdFeature = c.newInstance();
                if(initialize(whichMap, createdFeature)) {
                    overlay.addFeature(createdFeature, true);
                    Thread.sleep(bulkUpdateInterval);
                } else {
                    ErrorDialog.showError(activity, "Feature Type " + createdFeature.getFeatureType() + ", try to Draw and then put it in Editor");
                    createdFeature = null;
                }
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException |
                    EMP_Exception | InterruptedException e) {
                Log.e(TAG, "createFeature ", e);
                updateStatus(TAG, "createFeature FAILED");
                ErrorDialog.showError(activity, e.getMessage());
                createdFeature = null;
            }
        }

        return createdFeature;
    }

    private boolean initialize(int whichMap, Feature feature) {
        switch(feature.getFeatureType()) {
            case GEO_RECTANGLE:
                Rectangle rectangle = (Rectangle) feature;
                rectangle.getPositions().clear();
                rectangle.getPositions().addAll(CameraUtility.getCameraPosition(maps[whichMap], null, false));
                rectangle.setWidth(maps[whichMap].getCamera().getAltitude() * .20);
                rectangle.setHeight(maps[whichMap].getCamera().getAltitude() * .10);
                rectangle.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                rectangle.setAzimuth(0);
                break;
            case GEO_CIRCLE:
                Circle circle = (Circle) feature;
                circle.getPositions().clear();
                circle.getPositions().addAll(CameraUtility.getCameraPosition(maps[whichMap], null, false));
                circle.setRadius(maps[whichMap].getCamera().getAltitude() * .20);
                circle.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                break;
            case GEO_ELLIPSE:
                Ellipse ellipse = (Ellipse) feature;
                ellipse.getPositions().clear();
                ellipse.getPositions().addAll(CameraUtility.getCameraPosition(maps[whichMap], null, false));
                ellipse.setSemiMajor(maps[whichMap].getCamera().getAltitude() * .20);
                ellipse.setSemiMinor(maps[whichMap].getCamera().getAltitude() * .10);
                ellipse.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                ellipse.setAzimuth(0);
                break;
            case GEO_SQUARE:
                Square square = (Square) feature;
                square.getPositions().clear();
                square.getPositions().addAll(CameraUtility.getCameraPosition(maps[whichMap], null, false));
                square.setWidth(maps[whichMap].getCamera().getAltitude() * .20);
                square.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                square.setAzimuth(0);
                break;
            default:
                // ErrorDialog.showError(activity, "Feature Type " + feature.getFeatureType() + " is NOT supported yet");
                return false;
        }

        styleManager.setStyles(feature);

        return true;
    }

    private void singlePointOrTacticalGraphics(final int featureType, final String featureName, final boolean forEdit) {
        final List<String> choices = new ArrayList<>();
        choices.add("Single Point");
        choices.add("Tactical Graphics");
        final int selection = 0;
        activity.runOnUiThread(new Runnable() {
            public void run() {
                final ArrayAdapter<String> choiceAdapterAdapter = new ArrayAdapter(activity, android.R.layout.simple_list_item_checked, choices);
                AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                        .setTitle("Choose GEO_MIL_SYMBOL Type")
                        .setSingleChoiceItems(choiceAdapterAdapter, selection, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(TAG, "Selected GEO_MIL_SYMBOL type " + which);
                                Class<? extends Feature> featureClass = featureType2Class.get(featureTypeNames.get(featureType));
                                if (null == featureClass) {
                                    ErrorDialog.showError(activity, featureTypeNames.get(which) + " is not yet supported for Edit");
                                } else {
                                    if(choices.get(which).equals("Single Point")) {
                                        GeoMilSymbolCreator gmsc = new GeoMilSymbolCreator(forEdit);
                                        gmsc.showSymbolPropertiesDialog(featureName);
                                    } else {
                                        GeoMilSymbolCreator gmsc = new GeoMilSymbolCreator(forEdit);
                                        gmsc.showTacticalGraphicsPropertiesDialog(featureName);
                                    }
                                }
                                dialog.cancel();
                            }
                        });

                final AlertDialog dialog = builder.create();
                dialog.show();
            }
        });
    }
    private void processEditUpdate(String listener, IFeature oFeature, IEditUpdateData updateData) {
        int[] aIndexes;
        String temp;
        switch(updateData.getUpdateType()) {
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
                Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " indexes:{" + temp + "}");
                updateStatus(listener, " Draw Update " + updateData.getUpdateType().name() + " indexes:{" + temp + "}");
                break;
            case MILSTD_MODIFIER_UPDATED:
                Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " modifier: " + updateData.getChangedModifier().name() + " {" + ((MilStdSymbol) oFeature).getStringModifier(updateData.getChangedModifier()) + "}");
                updateStatus(listener, " Draw Update " + updateData.getUpdateType().name() + " modifier: " + updateData.getChangedModifier().name() + " {" + ((MilStdSymbol) oFeature).getStringModifier(updateData.getChangedModifier()) + "}" );
                break;
            case ACM_ATTRIBUTE_UPDATED:
                Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " ACM attribute:{" + updateData.getChangedModifier().name() + "}");
                updateStatus(listener, " Draw Update " + updateData.getUpdateType().name() + " ACM attribute:{" + updateData.getChangedModifier().name() + "}");
                break;
            case FEATURE_PROPERTY_UPDATED:
                switch(updateData.getChangedProperty()) {
                    case HEIGHT_PROPERTY_CHANGED:
                        if (oFeature instanceof Rectangle) {
                            Log.i(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Rectangle) oFeature).getHeight());
                            updateStatus(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Rectangle) oFeature).getHeight());
                        }
                        break;

                    case WIDTH_PROPERTY_CHANGED:
                        if (oFeature instanceof Rectangle) {
                            Log.i(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Rectangle) oFeature).getWidth());
                            updateStatus(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Rectangle) oFeature).getWidth());
                        } else if(oFeature instanceof Square) {
                            Log.i(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Square) oFeature).getWidth());
                            updateStatus(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + ((Square) oFeature).getWidth());
                        }
                        break;

                    case AZIMUTH_PROPERTY_CHANGED:
                        Log.i(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + oFeature.getAzimuth());
                        updateStatus(listener, "   Draw Update " + updateData.getChangedProperty().toString() + " " + oFeature.getAzimuth());
                        break;
                }
                break;
            case POSITION_UPDATED:
                if(oFeature instanceof Rectangle) {
                    Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " " + ((Rectangle) oFeature).getPosition().getLatitude() + "/"
                            + ((Rectangle) oFeature).getPosition().getLongitude());
                    updateStatus(listener, "   Draw Update " + updateData.getUpdateType().name() + " " + ((Rectangle) oFeature).getPosition().getLatitude() + "/"
                            + ((Rectangle) oFeature).getPosition().getLongitude());
                } else if(oFeature instanceof Square) {
                    Log.i(listener, "   Draw Update " + updateData.getUpdateType().name() + " " + ((Square) oFeature).getPosition().getLatitude() + "/"
                            + ((Square) oFeature).getPosition().getLongitude());
                    updateStatus(listener, "   Draw Update " + updateData.getUpdateType().name() + " " + ((Square) oFeature).getPosition().getLatitude() + "/"
                            + ((Square) oFeature).getPosition().getLongitude());
                }
                break;
        }
    }
    public class DrawEventListener implements IDrawEventListener {

        @Override
        public void onDrawStart(IMap map) {
            Log.d(TAG, "Draw Start.");
            updateStatus(TAG, "IDrawEventListener: Draw Start" );
        }

        @Override
        public void onDrawUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "Draw Update.");

            for (IEditUpdateData updateData: updateList) {
                processEditUpdate("IDrawEventListener", oFeature, updateData);
            }
        }

        @Override
        public void onDrawComplete(IMap map, IFeature feature) {
            Log.d(TAG, "Draw Complete. " + feature.getClass().getSimpleName() + " pos count " + feature.getPositions().size());
            updateStatus(TAG, "IDrawEventListener: Draw Complete." + feature.getClass().getSimpleName());

            for(int ii = 0; ii < feature.getPositions().size(); ii++ ) {
                Log.d(TAG, "pos " + ii + " lat/lon " + feature.getPositions().get(ii).getLatitude() + "/" + feature.getPositions().get(ii).getLongitude());
            }

            try {
                IOverlay overlay = createOverlay(map);
                overlay.addFeature(feature, true);
                Log.d(TAG, "add feature");
            } catch (EMP_Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDrawCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "Draw Cancelled.");
            updateStatus(TAG, "IDrawEventListener: Draw Cancelled");
        }

        @Override
        public void onDrawError(IMap map, String errorMessage) {
            Log.d(TAG, "Draw Error. ");
            updateStatus(TAG, "IDrawEventListener: Draw Error");
        }
    }

    public class FeatureDrawEventListener implements IFeatureDrawEventListener {
        @Override
        public void onEvent(FeatureDrawEvent event) {
            if(null != event.getUpdateData()) {
                for (IEditUpdateData updateData : event.getUpdateData()) {
                    processEditUpdate("IFeatureDrawEventListener", event.getTarget(), updateData);
                }
            }
        }
    }

    public class EditEventListener implements IEditEventListener {

        @Override
        public void onEditStart(IMap map) {
            updateStatus(TAG, "IEditEventListener: Edit Start" );
        }

        @Override
        public void onEditUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "Edit Update.");

            for (IEditUpdateData updateData: updateList) {
                processEditUpdate("IEditEventListener", oFeature, updateData);
            }
        }

        @Override
        public void onEditComplete(IMap map, IFeature feature) {
            updateStatus(TAG, "IEditEventListener: Edit Complete.");
        }

        @Override
        public void onEditCancel(IMap map, IFeature originalFeature) {
            updateStatus(TAG, "IEditEventListener: Edit Canceled.");
        }

        @Override
        public void onEditError(IMap map, String errorMessage) {
            updateStatus(TAG, "IEditEventListener: Edit Error.");
        }
    }

    /**
     * GeoMilSymbolCreator is used when user selects GEO_MIL_SYMBOL from the list tt Draw or Edit. Much work needs to be done here.
     */
    class GeoMilSymbolCreator implements SymbolPropertiesDialog.SymbolPropertiesDialogListener, TacticalGraphicPropertiesDialog.SymbolPropertiesDialogListener {

        boolean forEdit = true;
        GeoMilSymbolCreator(boolean forEdit) {
            this.forEdit = forEdit;
        }
        public String showSymbolPropertiesDialog(final String featureName) {
            Handler mainHandler = new Handler(activity.getMainLooper());

            final IMap theMap = maps[ExecuteTest.getCurrentMap()];
            createOverlay(theMap);
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                    SymbolPropertiesDialog featurePropertiesDlg = SymbolPropertiesDialog.newInstance("Symbol Properties", theMap,
                            MapNamesUtility.getNames(theMap, false, true, false), featureName, true, GeoMilSymbolCreator.this);
                    featurePropertiesDlg.show(fm, "fragment_symbol_properties_dialog");
                }
            };
            mainHandler.post(myRunnable);
            return null;
        }

        @Override
        public void onSymbolPropertiesSaveClick(SymbolPropertiesDialog dialog) {

            try {
                MilStdSymbol newSymbol = AddMilStdSymbol.initializeSinglePoint(maps[ExecuteTest.getCurrentMap()], dialog.getMilStdVersion(),
                        dialog.getSymbolCode(), dialog.getFeatureName(), dialog.getCurrentUnitDef(), dialog.getPositionUtility(), dialog.getModifiers());
                drawOrEdit(newSymbol);
            } catch (EMP_Exception e) {
                Log.e(TAG, "onSymbolPropertiesSaveClick", e);
                ErrorDialog.showError(activity, e.getMessage());
            }
        }

        @Override
        public void onSymbolPropertiesCancelClick(SymbolPropertiesDialog dialog) {

        }

        public String showTacticalGraphicsPropertiesDialog(final String featureName) {
            Handler mainHandler = new Handler(activity.getMainLooper());

            final IMap theMap = maps[ExecuteTest.getCurrentMap()];
            createOverlay(theMap);
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                    TacticalGraphicPropertiesDialog tacticalGraphicPropertiesDialog = TacticalGraphicPropertiesDialog.newInstance("Symbol Properties", theMap,
                            MapNamesUtility.getNames(theMap, false, true, false), featureName, true, GeoMilSymbolCreator.this);
                    tacticalGraphicPropertiesDialog.show(fm, "fragment_tactical_graphic_properties_dialog");
                }
            };
            mainHandler.post(myRunnable);
            return null;
        }

        @Override
        public boolean onSaveClick(TacticalGraphicPropertiesDialog dialog) {
            try {
                IFeature newSymbol = AddMilStdSymbol.initializeTacticalGraphicsSymbol(maps[ExecuteTest.getCurrentMap()], dialog.getMilStdVersion(),
                            dialog.getSymbolCode(), dialog.getFeatureName(), dialog.getCurrentDef(), dialog.getPositionUtility(), dialog.getModifiers());
                drawOrEdit(newSymbol);
                return true;
            } catch (EMP_Exception | IllegalStateException e) {
                ErrorDialog.showError(activity, e.getMessage());
            }
            return false;
        }

        @Override
        public void onCancelClick(TacticalGraphicPropertiesDialog dialog) {

        }

        private void drawOrEdit(IFeature newSymbol) throws EMP_Exception {
            int whichMap = ExecuteTest.getCurrentMap();

            styleManager.setStyles(newSymbol);

            if(forEdit) {
                // Check if symbol exists, if it does then put it in edit mode else create a new symbol
                // and put it in edit mode.
                IFeature existingFeature = null;
                if(newSymbol instanceof MilStdSymbol) {
                    List<IFeature> features = maps[whichMap].getAllFeatures();
                    if (null != features) {
                        for (IFeature f : features) {
                            if ((f instanceof MilStdSymbol) && (((MilStdSymbol) f).getSymbolCode().equals(((MilStdSymbol) newSymbol).getSymbolCode()))) {
                                existingFeature = f;
                            }
                        }
                    }
                }

                if(null == existingFeature) {
                    IOverlay overlay = createOverlay(maps[whichMap]);
                    overlay.addFeature(newSymbol, true);
                } else {
                    newSymbol = existingFeature;
                }
                if (null != editEventListener[whichMap]) {
                    maps[whichMap].editFeature(newSymbol, editEventListener[whichMap]);
                } else {
                    maps[whichMap].editFeature(newSymbol);
                }
            } else {
                if(null != drawEventListener[whichMap]) {
                    maps[whichMap].drawFeature(newSymbol, drawEventListener[whichMap]);
                } else {
                    maps[whichMap].drawFeature(newSymbol);
                }
            }
        }
    }
}
