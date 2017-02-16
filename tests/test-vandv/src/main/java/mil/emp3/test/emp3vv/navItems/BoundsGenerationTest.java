package mil.emp3.test.emp3vv.navItems;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.Overlay;
import mil.emp3.api.Point;
import mil.emp3.api.Text;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;

import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;

import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
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

    public BoundsGenerationTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Start", "Get Bounds"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        return null;
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
                }
                testThread.interrupt();
            } else if (userAction.equals("ClearMap")) {
                clearMaps();
            } else if (userAction.equals("Start")) {
                ICamera camera = maps[whichMap].getCamera();
                if(cameraListener[whichMap] == null) {
                    overlay[whichMap] = new Overlay();
                    maps[whichMap].addOverlay(overlay[whichMap], true);
                    cameraPoint[whichMap] = new Point();
                    cameraPoint[whichMap].getPositions().add(new MyGeoPosition(camera.getLatitude(), camera.getLongitude(), 0));
                    overlay[whichMap].addFeature(cameraPoint[whichMap], true);

                    cameraText[whichMap] = new Text();
                    cameraText[whichMap].setText("Camera");
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
            } else if (userAction.equals("Get Bounds")) {
                final IGeoBounds bounds = maps[whichMap].getBounds();
//                IGeoPosition pos = maps[whichMap].containerToGeo(new android.graphics.Point(0, 0));
//                Log.d(TAG, "0/0 " + pos.getLatitude() + " " + pos.getLongitude());
//
//                pos = maps[whichMap].containerToGeo(new android.graphics.Point(768, 0));
//                Log.d(TAG, "768/0 " + pos.getLatitude() + " " + pos.getLongitude());
//
//                pos = maps[whichMap].containerToGeo(new android.graphics.Point(0, 869));
//                Log.d(TAG, "0/869 " + pos.getLatitude() + " " + pos.getLongitude());
//
//                pos = maps[whichMap].containerToGeo(new android.graphics.Point(768, 869));
//                Log.d(TAG, "768/869 " + pos.getLatitude() + " " + pos.getLongitude());
                if (null != bounds) {
                    if (sePoint[whichMap] == null) {
                        sePoint[whichMap] = new Point();
                        swPoint[whichMap] = new Point();
                        nePoint[whichMap] = new Point();
                        nwPoint[whichMap] = new Point();
                        sePoint[whichMap].getPositions().add(new MyGeoPosition(bounds.getSouth(), bounds.getEast(), 0));
                        swPoint[whichMap].getPositions().add(new MyGeoPosition(bounds.getSouth(), bounds.getWest(), 0));
                        nePoint[whichMap].getPositions().add(new MyGeoPosition(bounds.getNorth(), bounds.getEast(), 0));
                        nwPoint[whichMap].getPositions().add(new MyGeoPosition(bounds.getNorth(), bounds.getWest(), 0));
                        overlay[whichMap].addFeature(sePoint[whichMap], true);
                        overlay[whichMap].addFeature(swPoint[whichMap], true);
                        overlay[whichMap].addFeature(nePoint[whichMap], true);
                        overlay[whichMap].addFeature(nwPoint[whichMap], true);

                        seText[whichMap] = new Text();
                        swText[whichMap] = new Text();
                        neText[whichMap] = new Text();
                        nwText[whichMap] = new Text();
                        seText[whichMap].setText("SE");
                        swText[whichMap].setText("SW");
                        neText[whichMap].setText("NE");
                        nwText[whichMap].setText("NW");
                        seText[whichMap].getPositions().add(new MyGeoPosition(bounds.getSouth(), bounds.getEast(), 0));
                        swText[whichMap].getPositions().add(new MyGeoPosition(bounds.getSouth(), bounds.getWest(), 0));
                        neText[whichMap].getPositions().add(new MyGeoPosition(bounds.getNorth(), bounds.getEast(), 0));
                        nwText[whichMap].getPositions().add(new MyGeoPosition(bounds.getNorth(), bounds.getWest(), 0));
                        overlay[whichMap].addFeature(seText[whichMap], true);
                        overlay[whichMap].addFeature(swText[whichMap], true);
                        overlay[whichMap].addFeature(neText[whichMap], true);
                        overlay[whichMap].addFeature(nwText[whichMap], true);
                    } else {
                        sePoint[whichMap].getPositions().clear();
                        swPoint[whichMap].getPositions().clear();
                        nePoint[whichMap].getPositions().clear();
                        nwPoint[whichMap].getPositions().clear();

                        sePoint[whichMap].getPositions().add(new MyGeoPosition(bounds.getSouth(), bounds.getEast(), 0));
                        swPoint[whichMap].getPositions().add(new MyGeoPosition(bounds.getSouth(), bounds.getWest(), 0));
                        nePoint[whichMap].getPositions().add(new MyGeoPosition(bounds.getNorth(), bounds.getEast(), 0));
                        nwPoint[whichMap].getPositions().add(new MyGeoPosition(bounds.getNorth(), bounds.getWest(), 0));

                        sePoint[whichMap].apply();
                        swPoint[whichMap].apply();
                        nePoint[whichMap].apply();
                        nwPoint[whichMap].apply();

                        seText[whichMap].getPositions().clear();
                        swText[whichMap].getPositions().clear();
                        neText[whichMap].getPositions().clear();
                        nwText[whichMap].getPositions().clear();

                        seText[whichMap].getPositions().add(new MyGeoPosition(bounds.getSouth(), bounds.getEast(), 0));
                        swText[whichMap].getPositions().add(new MyGeoPosition(bounds.getSouth(), bounds.getWest(), 0));
                        neText[whichMap].getPositions().add(new MyGeoPosition(bounds.getNorth(), bounds.getEast(), 0));
                        nwText[whichMap].getPositions().add(new MyGeoPosition(bounds.getNorth(), bounds.getWest(), 0));

                        seText[whichMap].apply();
                        swText[whichMap].apply();
                        neText[whichMap].apply();
                        nwText[whichMap].apply();
                    }

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            ErrorDialog.showMessageWaitForConfirm(activity, "NEWS " + bounds.getNorth() + " " + bounds.getEast() + " " +
                                    bounds.getWest() + " " + bounds.getSouth());
                        }
                    });
                    t.start();
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
        }
    }

}
