package mil.emp3.test.emp3vv.navItems.camera_and_look_at;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;


import org.cmapi.primitives.IGeoAltitudeMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mil.emp3.api.Camera;
import mil.emp3.api.LookAt;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.LookAtEvent;
import mil.emp3.api.events.MapViewChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.ILookAtEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.containers.AddContainer;
import mil.emp3.test.emp3vv.containers.dialogs.milstdunits.SymbolPropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.navItems.zoom_and_bounds.ZoomAndBoundsTest;
import mil.emp3.test.emp3vv.navItems.zoom_and_bounds.ZoomToDialog;
import mil.emp3.test.emp3vv.utils.CameraUtility;


public class CameraAndLookAtTest extends NavItemBase implements CameraAndLookAtDialog.ICameraAndLookAtDialogListener {
    private static String TAG = CameraAndLookAtTest.class.getSimpleName();

    List<EventListenerHandle> eventListenerHandles = new ArrayList<>();
    public CameraAndLookAtTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Add Overlay", "Add Feature", "Camera Test", "Look At Test"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = { "setCameraTest", "setLookAtTest", "applyCameraTest", "applyLookAtTest",
                "startCameraListenerTest", "startLookAtListenerTest", "startListenerTest", "stopListenerTest"};
        return actions;
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

            if(0 != eventListenerHandles.size()) {
                if(userAction.contains("stopListenerTest")) {
                    stopListenerTest(whichMap);
                    return true;
                } else {
                    updateStatus("stop Listener Test first");
                    return false;
                }
            }

            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                clearMaps();
            } else if(userAction.contains("Add Overlay")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, null);
                addContainer.showAddOverlayDialog();
            } else if(userAction.contains("Add Feature")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, null);
                addContainer.showAddFeatureDialog();
            } else if(userAction.contains("Camera Test")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                        CameraAndLookAtDialog cameraAndLookAtDialog = CameraAndLookAtDialog.newInstance("Camera Test", maps[whichMap], CameraAndLookAtTest.this, false);
                        cameraAndLookAtDialog.show(fm, "fragment_camera_and_look_at_dialog");
                    }
                };
                mainHandler.post(myRunnable);
            } else if(userAction.contains("Look At Test")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                        CameraAndLookAtDialog cameraAndLookAtDialog = CameraAndLookAtDialog.newInstance("Look At Test", maps[whichMap], CameraAndLookAtTest.this, true);
                        cameraAndLookAtDialog.show(fm, "fragment_camera_and_look_at_dialog");
                    }
                };
                mainHandler.post(myRunnable);
            } else if (userAction.contains("setCameraTest")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setCameraTest(whichMap);
                    }
                });
                t.start();
            } else if (userAction.contains("setLookAtTest")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setLookAtTest(whichMap);
                    }
                });
                t.start();
            } else if (userAction.contains("applyCameraTest")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        applyCameraTest(whichMap);
                    }
                });
                t.start();
            } else if (userAction.contains("applyLookAtTest")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        applyLookAtTest(whichMap);
                    }
                });
                t.start();
            } else if (userAction.contains("startCameraListenerTest")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startCameraListenerTest(whichMap);
                    }
                });
                t.start();
            } else if (userAction.contains("startLookAtListenerTest")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startLookAtListenerTest(whichMap);
                    }
                });
                t.start();
            }  else if (userAction.contains("startListenerTest")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startLookAtAndCameraListenerTest(whichMap);
                    }
                });
                t.start();
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

    @Override
    public void applyCamera(CameraAndLookAtDialog dialog) {
        ICamera currentCamera = dialog.getMap().getCamera();
        currentCamera.copySettingsFrom(dialog.getCamera());
        currentCamera.apply(false);
    }

    @Override
    public void setCamera(CameraAndLookAtDialog dialog) {
        try {
            dialog.getMap().setCamera(dialog.getCamera(), false);
        } catch (EMP_Exception e) {
            Log.e(TAG, "setCamera", e);
            updateStatus(TAG, e.getMessage());
        }
    }

    @Override
    public void applyLookAt(CameraAndLookAtDialog dialog) {
        ILookAt currentLookAt = dialog.getMap().getLookAt();

        ILookAt target = dialog.getLookAt();
        ICamera source = dialog.getCamera();

        ILookAt calculatedLookAt = CameraUtility.setupLookAt(source.getLatitude(), source.getLongitude(), source.getAltitude(),
                target.getLatitude(), target.getLongitude(), target.getAltitude());
        currentLookAt.copySettingsFrom(calculatedLookAt);
        currentLookAt.apply(false);
    }

    @Override
    public void setLookAt(CameraAndLookAtDialog dialog) {
        ILookAt target = dialog.getLookAt();
        ICamera source = dialog.getCamera();

        ILookAt calculatedLookAt = CameraUtility.setupLookAt(source.getLatitude(), source.getLongitude(), source.getAltitude(),
                target.getLatitude(), target.getLongitude(), target.getAltitude());
        try {
            dialog.getMap().setLookAt(calculatedLookAt, false);
        } catch (EMP_Exception e) {
            Log.e(TAG, "setLookAt", e);
            updateStatus(TAG, e.getMessage());
        }
    }

    private void cannedTestSetup(int whichMap) {
        try {
            clearMaps();

            maps[whichMap].setMidDistanceThreshold(5000000);
            maps[whichMap].setFarDistanceThreshold(10000000);

            maps[whichMap].addOverlay(o1, true);

            updateDesignator(p1);
            updateDesignator(p2);
            updateDesignator(p3);
            updateDesignator(p1_1);

            o1.addFeature(p1, true);
            o1.addFeature(p2, true);
            o1.addFeature(p3, true);
            o1.addFeature(p1_1, true);
            Thread.sleep(300); // bulk update
        } catch (EMP_Exception | InterruptedException e) {
            Log.e(TAG, "cannedTestSetup ", e);
            updateStatus(TAG, e.getMessage());
        }
    }

    private void setCameraTest(int whichMap) {
        try {
            startTest("setCameraTest");
            createCannedOverlaysAndFeatures();
            updateMilStdSymbolPosition(p1, 40.0, -70.0);
            updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
            updateMilStdSymbolPosition(p2, 45.0, -70.0);
            updateMilStdSymbolPosition(p3, 45.0, -80.0);
            cannedTestSetup(whichMap);

            ICamera camera = maps[whichMap].getCamera();
            String status  = String.format(Locale.US, "Starting Camera " +
                            " (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());
            ErrorDialog.showMessageWaitForConfirm(activity, status);

            ICamera myCamera = new Camera();
            myCamera.copySettingsFrom(camera);

            setupCameraTest(whichMap, myCamera, 40.0, -70.0, 1000.0,0.0, 0.0, 0.0, "TRUCK0");
            setupCameraTest(whichMap, myCamera, 40.0, -80.0, 1000.0,0.0, 0.0, 0.0, "TRUCK1");
            setupCameraTest(whichMap, myCamera, 45.0, -70.0, 1000.0,0.0, 0.0, 0.0, "TRUCK2");
            setupCameraTest(whichMap, myCamera, 45.0, -80.0, 1000.0,0.0, 0.0, 0.0, "TRUCK3");

        } finally {
            endTest();
        }
    }

    private void setupCameraTest(int whichMap, ICamera myCamera, double lat, double lon, double alt, double tilt, double roll, double heading, String featureSeen) {
        try {
            myCamera.setLatitude(lat);
            myCamera.setLongitude(lon);
            myCamera.setAltitude(alt);
            myCamera.setTilt(tilt);
            myCamera.setRoll(roll);
            myCamera.setHeading(heading);

            maps[whichMap].setCamera(myCamera, false);

            ICamera camera = maps[whichMap].getCamera();
            String status  = String.format(Locale.US, "Camera.set Target " + featureSeen +
                            " Camera " + " (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());
            ErrorDialog.showMessageWaitForConfirm(activity, status);
        }catch (EMP_Exception e) {
            Log.e(TAG, "setupCameraTest ", e);
            updateStatus(TAG, e.getMessage());
        }
    }

    private void setLookAtTest(int whichMap) {
        try {

            startTest("setLookAtTest");
            createCannedOverlaysAndFeatures();
            updateMilStdSymbolPosition(p1, 33.9424368, -118.4081222);
            updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
            updateMilStdSymbolPosition(p2, 45.0, -70.0);
            updateMilStdSymbolPosition(p3, 45.0, -80.0);
            cannedTestSetup(whichMap);

            setupLookAt(whichMap, 34.0158333, -118.4513056, 2500.0, 33.9424368, -118.4081222, 0.0, "TRUCK0");
            setupLookAt(whichMap, 39.9, -80.1, 2000.0, 40.0, -80.0, 0.0, "TRUCK1");
            setupLookAt(whichMap, 45.2, -69.5, 4000.0, 45.0, -70.0, 0.0, "TRUCK2");
            setupLookAt(whichMap, 45.3, -80.3, 5000.0, 45.0, -80.0, 0.0, "TRUCK3");

        } finally {
            endTest();
        }
    }

    private void setupLookAt(int whichMap, double sourceLat, double sourceLon, double sourceAlt, double targetLat, double targetLon, double targetAlt,
                             String featureSeen) {
        try {
            maps[whichMap].setCamera(CameraUtility.buildCamera(sourceLat, sourceLon, sourceAlt), false);

            ICamera camera1 = maps[whichMap].getCamera();
            String status = String.format(Locale.US, "LookAt.set Camera position L:N:A %1$6.3f %2$6.3f %3$6.0f T:R:H %4$6.3f %5$6.3f %6$6.3f no feature seen",
                    camera1.getLatitude(), camera1.getLongitude(), camera1.getAltitude(),
                    camera1.getTilt(), camera1.getRoll(), camera1.getHeading());
            ErrorDialog.showMessageWaitForConfirm(activity, status);


            // Compute heading and distance
            ILookAt calculatedLookAt = CameraUtility.setupLookAt(sourceLat, sourceLon, sourceAlt, targetLat, targetLon, targetAlt);

            maps[whichMap].setLookAt(calculatedLookAt, false);

            Thread.sleep(small_waitInterval);
            ICamera camera2 = maps[whichMap].getCamera();
            status = String.format(Locale.US, "LookAt.set " + featureSeen + " (L:N:A %1$6.3f %2$6.3f %3$6.0f) " +
                            "Camera " + " (L:N:A %4$6.3f %5$6.3f %6$6.0f H:T:R %7$6.3f %8$6.3f %9$6.3f) ",
                    calculatedLookAt.getLatitude(), calculatedLookAt.getLongitude(), calculatedLookAt.getAltitude(),
                    camera2.getLatitude(), camera2.getLongitude(), camera2.getAltitude(),
                    camera2.getHeading(), camera2.getTilt(), camera2.getRoll());

            ErrorDialog.showMessageWaitForConfirm(activity, status);

        } catch (EMP_Exception | InterruptedException e) {
            Log.e(TAG, "setupLookAt", e);
            updateStatus(TAG, e.getMessage());
        }
    }

    private void applyCameraTest(int whichMap) {
        try {

            startTest("applyCameraTest");
            createCannedOverlaysAndFeatures();
            updateMilStdSymbolPosition(p1, 40.0, -70.0);
            updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
            updateMilStdSymbolPosition(p2, 45.0, -70.0);
            updateMilStdSymbolPosition(p3, 45.0, -80.0);
            cannedTestSetup(whichMap);

            ICamera camera = maps[whichMap].getCamera();
            String status  = String.format(Locale.US, "Camera.apply start pos Camera (L:N:A %1$6.3f %2$6.3f %3$6.0f  H:T:R %4$6.3f %5$6.3f %6$6.3f) no feature seen",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());
            ErrorDialog.showMessageWaitForConfirm(activity, status);

            setupApplyCamera(camera, 40.0, -70.0, 2000.0, 0.0, 0.0, 0.0, "TRUCK0");
            setupApplyCamera(camera, 40.0, -80.0, 2000.0, 0.0, 0.0, 0.0, "TRUCK1");
            setupApplyCamera(camera, 45.0, -70.0, 2000.0, 0.0, 0.0, 0.0, "TRUCK2");
            setupApplyCamera(camera, 45.0, -80.0, 2000.0, 0.0, 0.0, 0.0, "TRUCK3");
        } finally {
            endTest();
        }
    }

    private void setupApplyCamera(ICamera camera, double lat, double lon, double alt, double tilt, double roll, double heading, String featureSeen) {
        try {

            camera.setLatitude(lat);
            camera.setLongitude(lon);
            camera.setAltitude(alt);
            camera.setTilt(tilt);
            camera.setRoll(roll);
            camera.setHeading(heading);

            camera.apply(false);
            Thread.sleep(small_waitInterval);

            String status  = String.format(Locale.US, "Camera.apply Target " + featureSeen +
                            " Camera " + " (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());
           ErrorDialog.showMessageWaitForConfirm(activity, status);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void applyLookAtTest(int whichMap) {
        try {

            startTest("applyLookAtTest");
            createCannedOverlaysAndFeatures();

            updateMilStdSymbolPosition(p1, 40.0, -70.0);
            updateMilStdSymbolPosition(p1_1, 40.0, -80.0);
            updateMilStdSymbolPosition(p2, 45.0, -70.0);
            updateMilStdSymbolPosition(p3, 45.0, -80.0);

            cannedTestSetup(whichMap);

            ILookAt lookAt = maps[whichMap].getLookAt();

            lookAt = setupApplyLookAt(whichMap, lookAt, 42.5, -75.0, 2000.0, 40.0, -70.0, 0.0, "TRUCK0");
            lookAt = setupApplyLookAt(whichMap, lookAt, 42.5, -75.0, 2000.0, 40.0, -80.0, 0.0, "TRUCK1");
            lookAt = setupApplyLookAt(whichMap, lookAt, 42.5, -75.0, 2000, 45.0, -70.0, 0.0, "TRUCK2");
            setupApplyLookAt(whichMap, lookAt, 42.5, -75.0, 2000, 45.0, -80.0, 0.0, "TRUCK3");

        } finally {
            endTest();
        }
    }

    private ILookAt setupApplyLookAt(int whichMap, ILookAt lookAt, double sourceLat, double sourceLon, double sourceAlt, double targetLat, double targetLon, double targetAlt,
                                     String featureSeen) {
        try {
            // Compute heading and distance
            ILookAt calculatedLookAt = CameraUtility.setupLookAt(sourceLat, sourceLon, sourceAlt, targetLat, targetLon, targetAlt);

            if(null == lookAt) {
                lookAt = calculatedLookAt;
                try {
                    maps[whichMap].setLookAt(lookAt, false);
                } catch (EMP_Exception e) {
                    e.printStackTrace();
                }
            } else {
                lookAt.copySettingsFrom(calculatedLookAt);
                lookAt.apply(false);
            }
            Thread.sleep(small_waitInterval);
            ILookAt lookAt2 = maps[whichMap].getLookAt();
            ICamera camera2 = maps[whichMap].getCamera();

            String lookat  = String.format(Locale.US, "LookAt.apply " + featureSeen + " (L:N:A %1$6.3f %2$6.3f %3$6.0f) " +
                            "Camera " + " (L:N:A %4$6.3f %5$6.3f %6$6.0f H:T:R %7$6.3f %8$6.3f %9$6.3f) ",
                    lookAt2.getLatitude(), lookAt2.getLongitude(), lookAt2.getAltitude(),
                    camera2.getLatitude(), camera2.getLongitude(), camera2.getAltitude(),
                    camera2.getHeading(), camera2.getTilt(), camera2.getRoll());

            ErrorDialog.showMessageWaitForConfirm(activity, lookat);
            return lookAt;

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void startCameraListenerTest(int whichMap) {
        try {
            eventListenerHandles.clear();
            eventListenerHandles.add(maps[whichMap].addCameraEventListener(new CameraListener("On Map")));
            eventListenerHandles.add(maps[whichMap].getCamera().addCameraEventListener(new CameraListener("On Camera")));
            eventListenerHandles.add(maps[whichMap].addMapViewChangeEventListener(new MapViewChangeEventListener()));
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private void startLookAtListenerTest(int whichMap) {
        try {
            eventListenerHandles.clear();
            eventListenerHandles.add(maps[whichMap].getLookAt().addLookAtEventListener(new LookAtListener()));
            eventListenerHandles.add(maps[whichMap].addMapViewChangeEventListener(new MapViewChangeEventListener()));
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private void startLookAtAndCameraListenerTest(int whichMap) {
        try {
            eventListenerHandles.clear();
            eventListenerHandles.add(maps[whichMap].addCameraEventListener(new CameraListener("On Map")));
            eventListenerHandles.add(maps[whichMap].getCamera().addCameraEventListener(new CameraListener("On Camera")));
            eventListenerHandles.add(maps[whichMap].getLookAt().addLookAtEventListener(new LookAtListener()));
            eventListenerHandles.add(maps[whichMap].addMapViewChangeEventListener(new MapViewChangeEventListener()));
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    private void stopListenerTest(int whichMap) {
        for(EventListenerHandle elh: eventListenerHandles) {
            // Just remove from every possible target, we don't want to track this
            maps[whichMap].removeEventListener(elh);
            maps[whichMap].getCamera().removeEventListener(elh);
            maps[whichMap].getLookAt().removeEventListener(elh);
        }
        eventListenerHandles.clear();
    }

    class CameraListener implements ICameraEventListener {
        String onWhat;
        CameraListener(String onWhat) {
            this.onWhat = onWhat;
        }
        @Override
        public void onEvent(CameraEvent event) {
            ICamera camera = event.getCamera();
            String eventString  = String.format(Locale.US,
                    "ICameraEventListener " + onWhat + " (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());
            ErrorDialog.showMessageWaitForConfirm(activity, eventString);
        }
    }

    class LookAtListener implements ILookAtEventListener {
        @Override
        public void onEvent(LookAtEvent event) {
            ILookAt lookAt = event.getLookAt();
            String eventString  = String.format(Locale.US,
                    "ILookAtEventListener " + " (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                    lookAt.getLatitude(), lookAt.getLongitude(), lookAt.getAltitude(),
                    lookAt.getHeading(), lookAt.getTilt(), lookAt.getRange());
            ErrorDialog.showMessageWaitForConfirm(activity, eventString);
        }
    }

    class MapViewChangeEventListener implements IMapViewChangeEventListener {
        @Override
        public void onEvent(MapViewChangeEvent event) {
            ICamera camera = event.getCamera();
            String eventString  = String.format(Locale.US,
                    "MapViewChangeEventListener " + "Camera (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) \n",
                    camera.getLatitude(), camera.getLongitude(), camera.getAltitude(),
                    camera.getHeading(), camera.getTilt(), camera.getRoll());
            ILookAt lookAt = event.getLookAt();
            String eventString2  = String.format(Locale.US,
                    "LookAt (L:N:A %1$6.3f %2$6.3f %3$6.0f H:T:R %4$6.3f %5$6.3f %6$6.3f) ",
                    lookAt.getLatitude(), lookAt.getLongitude(), lookAt.getAltitude(),
                    lookAt.getHeading(), lookAt.getTilt(), lookAt.getRange());
            ErrorDialog.showMessageWaitForConfirm(activity, eventString + eventString2);
        }
    }
}
