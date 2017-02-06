package mil.emp3.test.emp3vv.navItems;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoPositionGroup;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.MapFreehandEventEnum;
import mil.emp3.api.events.MapFreehandEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.listeners.IMapFreehandEventListener;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.containers.AddContainer;
import mil.emp3.test.emp3vv.dialogs.StrokeStyleDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.navItems.basic_shapes_editors.BasicShapesEditorsTest;
import mil.emp3.test.emp3vv.navItems.select_feature_test.SelectFeatureDialog;
import mil.emp3.test.emp3vv.navItems.select_feature_test.SelectFeatureTest;


public class FreehandDrawTest extends NavItemBase implements StrokeStyleDialog.IStrokeStyleDialogListener {
    private static String TAG = AddRemoveGetTest.class.getSimpleName();

    MapFreehandEventListener[] mapFreehandEventListener = new MapFreehandEventListener[ExecuteTest.MAX_MAPS];
    FreehandeventListener[] freehandEventListener = new FreehandeventListener[ExecuteTest.MAX_MAPS];
    EventListenerHandle[] mapFreehandEventListenerHandle = new EventListenerHandle[ExecuteTest.MAX_MAPS];
    IGeoStrokeStyle[] customStrokeStyle = new IGeoStrokeStyle[ExecuteTest.MAX_MAPS];

    public FreehandDrawTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Draw", "Exit Draw", "Set Style", "Create Style"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = {"Add IMapFreehandEventListener", "Add IFreehandEventListener", "Remove Listeners",
                "Default Stroke Style"};
        return actions;
    }

    protected void test0() {

        try {
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
                    freehandEventListener[ii] = null;
                    if (null != mapFreehandEventListenerHandle[ii]) {
                        maps[ii].removeEventListener(mapFreehandEventListenerHandle[ii]);
                        mapFreehandEventListener[ii] = null;
                        mapFreehandEventListenerHandle[ii] = null;
                    }
                    if(null != maps[ii]) {
                        try {
                            maps[ii].drawFreehandExit();
                        } catch(EMP_Exception e) {

                        }
                    }
                }
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                clearMaps();
            } else if(userAction.equals("Remove Listeners")) {
                freehandEventListener[whichMap] = null;
                if(null != mapFreehandEventListenerHandle[whichMap]) {
                    maps[whichMap].removeEventListener(mapFreehandEventListenerHandle[whichMap]);
                    mapFreehandEventListener[whichMap] = null;
                    mapFreehandEventListenerHandle[whichMap] = null;
                }
            } else if(userAction.equals("Draw")) {
                try {
                    IGeoStrokeStyle strokeStyle = customStrokeStyle[whichMap];
                    if(null == strokeStyle) {
                        strokeStyle = new GeoStrokeStyle();
                        IGeoColor geoColor = new EmpGeoColor(1.0, 255, 255, 0);

                        strokeStyle.setStrokeColor(geoColor);
                        strokeStyle.setStrokeWidth(5);
                    }
                    if(null == freehandEventListener[whichMap]) {
                        maps[whichMap].drawFreehand(strokeStyle);
                    } else {
                        maps[whichMap].drawFreehand(strokeStyle, freehandEventListener[whichMap]);
                    }
                } catch(EMP_Exception e) {
                    ErrorDialog.showError(activity, e.getMessage());
                }
            } else if(userAction.equals("Exit Draw")) {
                if(maps[whichMap].getEditorMode() == EditorMode.FREEHAND_MODE) {
                    maps[whichMap].drawFreehandExit();
                } else {
                    ErrorDialog.showError(activity, "Map is neither in EDIT nor in FREEHAND_MODE");
                }
            } else if(userAction.equals("Add IMapFreehandEventListener")) {
                if(null == mapFreehandEventListener[whichMap]) {
                    if (freehandEventListener[whichMap] != null) {
                        freehandEventListener[whichMap] = null;
                    }
                    mapFreehandEventListener[whichMap] = new MapFreehandEventListener(maps[whichMap]);
                    mapFreehandEventListenerHandle[whichMap] = maps[whichMap].addMapFreehandEventListener(mapFreehandEventListener[whichMap]);
                }
            } else if(userAction.equals("Add IFreehandEventListener")) {
                if(null == freehandEventListener[whichMap]) {
                    if(null != mapFreehandEventListenerHandle[whichMap]) {
                        maps[whichMap].removeEventListener(mapFreehandEventListenerHandle[whichMap]);
                        mapFreehandEventListener[whichMap] = null;
                        mapFreehandEventListenerHandle[whichMap] = null;
                    }
                    freehandEventListener[whichMap] = new FreehandeventListener(maps[whichMap]);
                }
            } else if(userAction.equals("Create Style")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                        StrokeStyleDialog strokeStyleDialog = StrokeStyleDialog.newInstance("Stroke Style", FreehandDrawTest.this, maps[whichMap], customStrokeStyle[whichMap]);
                        strokeStyleDialog.show(fm, "fragment_stroke_style_dialog");
                    }
                };
                mainHandler.post(myRunnable);
            } else if(userAction.equals("Default Stroke Style")) {
                customStrokeStyle[whichMap] = null;
            } else if(userAction.equals("Set Style")) {
                if(null == customStrokeStyle[whichMap]) {
                    ErrorDialog.showError(activity, "You must first create a custom style");
                } else {
                    try {
                        maps[whichMap].setFreehandStyle(customStrokeStyle[whichMap]);
                    } catch(EMP_Exception e) {
                        ErrorDialog.showError(activity, e.getMessage());
                    }
                }
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
        return (actOn(userAction));
    }

    class MapFreehandEventListener implements IMapFreehandEventListener {
        IMap theMap;

        MapFreehandEventListener(IMap map) {
            this.theMap = map;
        }

        @Override
        public void onEvent(MapFreehandEvent event) {
            switch(event.getEvent()) {

                case MAP_ENTERED_FREEHAND_DRAW_MODE:
                    updateStatus(TAG, "IMapFreehandEventListener: onEnterFreeHandDrawMode");
                    break;

                case MAP_FREEHAND_LINE_DRAW_START:
                    updateStatus(TAG, "IMapFreehandEventListener: onFreeHandLineDrawStart");
                    break;

                case MAP_FREEHAND_LINE_DRAW_UPDATE:
                    updateStatus(TAG, "IMapFreehandEventListener: onFreeHandLineDrawUpdate");
                    break;

                case MAP_FREEHAND_LINE_DRAW_END:
                    updateStatus(TAG, "IMapFreehandEventListener: onFreeHandLineDrawEnd");
                    Path path = new Path();
                    path.getPositions().clear();
                    path.getPositions().addAll(event.getPositionGroup().getPositions());
                    path.setStrokeStyle(event.getStyle());
                    IOverlay overlay = createOverlay(theMap);
                    try {
                        overlay.addFeature(path, true);
                    } catch (EMP_Exception e) {
                        ErrorDialog.showError(activity, e.getMessage());
                    }
                    break;

                case MAP_EXIT_FREEHAND_DRAW_MODE:
                    updateStatus(TAG, "IMapFreehandEventListener: onExitFreeHandDrawMode");
                    break;
                default:
                    updateStatus(TAG, "IMapFreehandEventListener: onDrawError");
                    Log.e(TAG, "Unsupported event received " + event.toString());
            }
        }
    }

    class FreehandeventListener implements IFreehandEventListener {
        IMap theMap;

        FreehandeventListener(IMap map) {
            this.theMap = map;
        }
        @Override
        public void onEnterFreeHandDrawMode(IMap map) {
            updateStatus(TAG, "IFreehandEventListener: onEnterFreeHandDrawMode");
        }

        @Override
        public void onFreeHandLineDrawStart(IMap map, IGeoPositionGroup positionList) {
            updateStatus(TAG, "IFreehandEventListener: onFreeHandLineDrawStart");
        }

        @Override
        public void onFreeHandLineDrawUpdate(IMap map, IGeoPositionGroup positionList) {
            updateStatus(TAG, "IFreehandEventListener: onFreeHandLineDrawUpdate");
        }

        @Override
        public void onFreeHandLineDrawEnd(IMap map, IGeoStrokeStyle style, IGeoPositionGroup positionList) {
            Path path = new Path();
            path.getPositions().clear();
            path.getPositions().addAll(positionList.getPositions());
            path.setStrokeStyle(style);
            IOverlay overlay = createOverlay(theMap);
            try {
                overlay.addFeature(path, true);
            } catch (EMP_Exception e) {
                ErrorDialog.showError(activity, e.getMessage());
            }
        }

        @Override
        public void onExitFreeHandDrawMode(IMap map) {
            updateStatus(TAG, "IFreehandEventListener: onExitFreeHandDrawMode");
        }

        @Override
        public void onDrawError(IMap map, String errorMessage) {
            updateStatus(TAG, "IFreehandEventListener: onDrawError");
        }
    }

    @Override
    public void setStrokeStyle(StrokeStyleDialog strokeStyleDialog) {
        customStrokeStyle[ExecuteTest.getCurrentMap()] = strokeStyleDialog.getStrokeStyle();
    }
}
