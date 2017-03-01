package mil.emp3.test.emp3vv.navItems;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mil.emp3.api.Overlay;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.Text;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpBoundingArea;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;

import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;

import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.AddContainer;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;


public class BoundsGenerationTest extends NavItemBase {
    private static String TAG = BoundsGenerationTest.class.getSimpleName();

    EventListenerHandle listenerHandle[] = new EventListenerHandle[ExecuteTest.MAX_MAPS];
    ICameraEventListener cameraListener[] = new ICameraEventListener[ExecuteTest.MAX_MAPS];

    IOverlay overlay[] = new IOverlay[ExecuteTest.MAX_MAPS];
    Point cameraPoint[] = new Point[ExecuteTest.MAX_MAPS];
    Point swPoint[] = new Point[ExecuteTest.MAX_MAPS];
    Point sePoint[] = new Point[ExecuteTest.MAX_MAPS];
    Point nwPoint[] = new Point[ExecuteTest.MAX_MAPS];
    Point nePoint[] = new Point[ExecuteTest.MAX_MAPS];

    Text cameraText[] = new Text[ExecuteTest.MAX_MAPS];
    Text swText[] = new Text[ExecuteTest.MAX_MAPS];
    Text seText[] = new Text[ExecuteTest.MAX_MAPS];
    Text nwText[] = new Text[ExecuteTest.MAX_MAPS];
    Text neText[] = new Text[ExecuteTest.MAX_MAPS];

    EventListenerHandle mapInteractionHandle[] = new EventListenerHandle[ExecuteTest.MAX_MAPS];

    List<IFeature> boundingFeatures[] = new List[ExecuteTest.MAX_MAPS];
    private final StyleManager styleManager;
    public BoundsGenerationTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);

        for(int ii = 0; ii < ExecuteTest.MAX_MAPS; ii++) {
            boundingFeatures[ii] = new ArrayList<>();
        }
        styleManager = new StyleManager(activity, maps);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Start", "Show Emp Area", "Get Geo Bounds", "Get Emp Bounds" };
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = { "Add Feature", "Show Corners" };
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
            if (Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                for (int ii = 0; ii < ExecuteTest.MAX_MAPS; ii++) {
                    if (listenerHandle[ii] != null) {
                        maps[ii].removeEventListener(listenerHandle[ii]);
                    }

                    if (mapInteractionHandle[ii] != null) {
                        maps[ii].removeEventListener(mapInteractionHandle[ii]);
                    }
                }
                testThread.interrupt();
            } else if (userAction.equals("ClearMap")) {
                for(int ii = 0; ii < ExecuteTest.MAX_MAPS; ii++) {
                    cameraPoint[ii] = null;
                    swPoint[ii] = null;
                    sePoint[ii] = null;
                    nwPoint[ii] = null;
                    nePoint[ii] = null;

                    cameraText[ii] = null;
                    swText[ii] = null;
                    seText[ii] = null;
                    nwText[ii] = null;
                    neText[ii] = null;

                    if (listenerHandle[ii] != null) {
                        maps[ii].removeEventListener(listenerHandle[ii]);
                        listenerHandle[ii] = null;
                    }

                    if (mapInteractionHandle[ii] != null) {
                        maps[ii].removeEventListener(mapInteractionHandle[ii]);
                        mapInteractionHandle[ii] = null;
                    }
                }
                clearMaps();
            } else if (userAction.equals("Start")) {
                ICamera camera = maps[whichMap].getCamera();
                if(cameraListener[whichMap] == null) {
                    overlay[whichMap] = new Overlay();
                    overlay[whichMap].setName("Overlay Map " + whichMap);
                    maps[whichMap].addOverlay(overlay[whichMap], true);
                    cameraPoint[whichMap] = new Point();
                    cameraPoint[whichMap].getPositions().add(new MyGeoPosition(camera.getLatitude(), camera.getLongitude(), 0));
                    overlay[whichMap].addFeature(cameraPoint[whichMap], true);

                    IGeoLabelStyle labelStyle = new GeoLabelStyle();
                    labelStyle.setColor(new EmpGeoColor(1, 255, 0, 0));

                    cameraText[whichMap] = new Text();
                    cameraText[whichMap].setText("Camera");
                    cameraText[whichMap].setLabelStyle(labelStyle);
                    cameraText[whichMap].getPositions().add(new MyGeoPosition(camera.getLatitude(), camera.getLongitude(), 0));
                    overlay[whichMap].addFeature(cameraText[whichMap], true);

                    cameraListener[whichMap] = new CameraListener(whichMap);
                    listenerHandle[whichMap] = maps[whichMap].addCameraEventListener(cameraListener[whichMap]);
                } else {
                    cameraPoint[whichMap].getPositions().clear();
                    cameraPoint[whichMap].getPositions().add(new MyGeoPosition(camera.getLatitude(), camera.getLongitude(), 0));
                    cameraPoint[whichMap].apply();

                    cameraText[whichMap].getPositions().clear();
                    cameraText[whichMap].getPositions().add(new MyGeoPosition(camera.getLatitude(), camera.getLongitude(), 0));
                    cameraText[whichMap].apply();
                }
            } else if (userAction.equals("Show Corners")) {
                if(null == mapInteractionHandle[ExecuteTest.getCurrentMap()]) {
                    mapInteractionHandle[whichMap] = maps[whichMap].addMapInteractionEventListener(new MapInteractionListener());
                } else {
                    maps[whichMap].removeEventListener(mapInteractionHandle[whichMap]);
                    mapInteractionHandle[whichMap] = null;
                }
            } else if (userAction.equals("Show Emp Area")) {
                IGeoLabelStyle labelStyle = new GeoLabelStyle();
                labelStyle.setColor(new EmpGeoColor(1, 255, 0, 0));

                IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
                strokeStyle.setStrokeColor(new EmpGeoColor(1, 0, 255, 255));
                strokeStyle.setStrokeWidth(5);

                overlay[whichMap].removeFeatures(boundingFeatures[whichMap]);
                boundingFeatures[whichMap].clear();

                IGeoBounds geoBounds = maps[whichMap].getBounds();
                if(geoBounds instanceof IEmpBoundingArea) {
                    IEmpBoundingArea empBoundingArea = (IEmpBoundingArea) geoBounds;
                    IGeoPosition[] positions = empBoundingArea.getBoundingVertices();
                    Polygon p = new Polygon();
                    p.setStrokeStyle(strokeStyle);
                    boundingFeatures[whichMap].add(p);
                    for (IGeoPosition position : positions) {
                        Text text = new Text();

                    /*
                    sMessage = String.format(Locale.US, "%1s L:N:A %2$6.3f %3$6.3f %4$6.0f F:M %5$6.1f %6$6.1f %7$d ", oMap.getName(),
                            oCamera.getLatitude(), oCamera.getLongitude(), oCamera.getAltitude(),
                            oMap.getFarDistanceThreshold(), oMap.getMidDistanceThreshold(),
                            iCount);
                     */
                        String formattedLatLong = String.format(Locale.US, "%1$6.3f %2$6.3f", position.getLatitude(), position.getLongitude());
                        text.setText(formattedLatLong);
                        text.setLabelStyle(labelStyle);
                        text.getPositions().add(position);
                        boundingFeatures[whichMap].add(text);

                        Point point = new Point();
                        point.getPositions().add(position);
                        boundingFeatures[whichMap].add(point);

                        p.getPositions().add(position);
                    }
                    overlay[whichMap].addFeatures(boundingFeatures[whichMap], true);
                }
            } else if (userAction.equals("Get Geo Bounds")) {

                final IGeoBounds geoBounds = maps[whichMap].getBounds();
                if(null != geoBounds) {
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String formattedLatLong = String.format(Locale.US, "NEWS %1$6.3f %2$6.3f %3$6.3f %4$6.3f",
                                    geoBounds.getNorth(), geoBounds.getEast(), geoBounds.getWest(), geoBounds.getSouth());
                            ErrorDialog.showMessageWaitForConfirm(activity, formattedLatLong);
                        }
                    });
                    t.start();
                }
            } else if (userAction.equals("Get Emp Bounds")) {

                final IGeoBounds geoBounds = maps[whichMap].getBounds();
                if((null != geoBounds) && (geoBounds instanceof IEmpBoundingArea)) {
                    IEmpBoundingArea empBoundingArea = (IEmpBoundingArea) geoBounds;
                    final EmpBoundingBox empBoundingBox = empBoundingArea.getEmpBoundingBox();
                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            String formattedLatLong = String.format(Locale.US, "NEWS %1$6.3f %2$6.3f %3$6.3f %4$6.3f",
                                    empBoundingBox.north(), empBoundingBox.east(), empBoundingBox.west(), empBoundingBox.south());
                            ErrorDialog.showMessageWaitForConfirm(activity, formattedLatLong);
                        }
                    });
                    t.start();
                }
            }   else if(userAction.equals("Add Feature")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, styleManager);
                addContainer.showAddFeatureDialog();
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

    class MyGeoPosition extends GeoPosition {
        MyGeoPosition(double latitude, double longitude, double altitude) {
            setLatitude(latitude);
            setLongitude(longitude);
            setAltitude(altitude);
        }
    }

    class CameraListener implements ICameraEventListener {
        int whichMap;
        CameraListener(int whichMap) {
            this.whichMap = whichMap;
        }
        @Override
        public void onEvent(CameraEvent event) {
            ICamera camera = event.getCamera();
            cameraPoint[whichMap].getPositions().clear();
            cameraPoint[whichMap].getPositions().add(new MyGeoPosition(camera.getLatitude(), camera.getLongitude(), 0));
            cameraPoint[whichMap].apply();

            cameraText[whichMap].getPositions().clear();
            cameraText[whichMap].getPositions().add(new MyGeoPosition(camera.getLatitude(), camera.getLongitude(), 0));
            cameraText[whichMap].apply();
        }
    }

    class MapInteractionListener implements IMapInteractionEventListener {
        @Override
        public void onEvent(final MapUserInteractionEvent event) {
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        int ii, jj;
                        for(ii = 0, jj = 0; ; ii++, jj++){
                            IGeoPosition position = maps[ExecuteTest.getCurrentMap()].containerToGeo(new android.graphics.Point(ii, jj));
                            if (null != position) {
                                Log.d(TAG, "Position AT  " + ii + " " + jj + " " + position.getLatitude() + " " + position.getLongitude());
                                android.graphics.Point p = maps[ExecuteTest.getCurrentMap()].geoToScreen(position);
                                if(p != null) {
                                    Log.d(TAG, "Point AT  " + p.x + " " + p.y + " " + position.getLatitude() + " " + position.getLongitude());
                                } else {
                                    Log.d(TAG, "Null Point AT");
                                }
                            } else {
                                Log.d(TAG, "Null Position AT " + ii + " " + jj);
                                break;
                            }
                        }
                    } catch (EMP_Exception e) {
                        e.printStackTrace();
                    }
                    if(null != event.getCoordinate()) {
                        ErrorDialog.showMessageWaitForConfirm(activity, "X, Y [" + event.getPoint().x + ", " + event.getPoint().y +
                                "] Lat/Long [" + event.getCoordinate().getLatitude() + ", " + event.getCoordinate().getLongitude() + "]");
                    } else {
                        ErrorDialog.showMessageWaitForConfirm(activity, "X, Y [" + event.getPoint().x + ", " + event.getPoint().y +
                                "] Lat/Long [Not Available]");
                    }
                }
            });
            t.start();

//            try {
//                int ii, jj;
//                for (ii = 0, jj = 0; ; ii++, jj++) {
//                    IGeoPosition position = maps[ExecuteTest.getCurrentMap()].containerToGeo(new android.graphics.Point(ii, jj));
//                    if (null != position) {
//                        Log.d(TAG, "Position AT  " + ii + " " + jj + " " + position.getLatitude() + " " + position.getLongitude());
//                        android.graphics.Point p = maps[ExecuteTest.getCurrentMap()].geoToScreen(position);
//                        if (p != null) {
//                            Log.d(TAG, "Point AT  " + p.x + " " + p.y + " " + position.getLatitude() + " " + position.getLongitude());
//                        } else {
//                            Log.d(TAG, "Null Point AT");
//                        }
//                    } else {
//                        Log.d(TAG, "Null Position AT " + ii + " " + jj);
//                        break;
//                    }
//                }
//            } catch (EMP_Exception e) {
//                e.printStackTrace();
//            }
        }
    }
}
