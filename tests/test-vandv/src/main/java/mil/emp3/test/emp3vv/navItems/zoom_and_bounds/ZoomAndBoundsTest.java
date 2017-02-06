package mil.emp3.test.emp3vv.navItems.zoom_and_bounds;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoBounds;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.containers.AddContainer;
import mil.emp3.test.emp3vv.containers.dialogs.milstdunits.SymbolPropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.BoundsDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

/**
 * After each call to apply there is a Thread.sleep(bulkUpdateInterval) to account for bulk updates in the EMP3 implementation.
 */
public class ZoomAndBoundsTest extends NavItemBase implements ZoomToDialog.IZoomToDialogListener, BoundsDialog.IBoundsDialogListener {
    private static String TAG = ZoomAndBoundsTest.class.getSimpleName();

    public ZoomAndBoundsTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Add Overlay", "Add Feature", "Zoom To", "Set Bounds"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        String[] actions = { "Basic Zoom Test", "Altitude Test", "Basic Bounds Test", "Near Pole Test", "Globe Surround Test" };
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
            }  else if(userAction.contains("Add Feature")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, null);
                addContainer.showAddFeatureDialog();
            } else if(userAction.contains("Zoom To")) {
                Handler mainHandler = new Handler(activity.getMainLooper());
                Runnable myRunnable = new Runnable() {
                    @Override
                    public void run() {
                        FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                        ZoomToDialog zoomToDialog = ZoomToDialog.newInstance("Zoom To Test", maps[whichMap], ZoomAndBoundsTest.this);
                        zoomToDialog.show(fm, "fragment_zoom_to_dialog");
                    }
                };
                mainHandler.post(myRunnable);
            } else if(userAction.contains("Set Bounds")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        setBoundsTest(whichMap);
                    }
                });
                t.start();

            } else if (userAction.contains("Basic Zoom Test")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        basicZoomToTest(whichMap);
                    }
                });
                t.start();
            } else if (userAction.contains("Altitude Test")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        altitudeTest(whichMap);
                    }
                });
                t.start();
            } else if (userAction.contains("Basic Bounds Test")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        basicBoundsTest(whichMap);
                    }
                });
                t.start();
            } else if (userAction.contains("Near Pole Test")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        nearPoleTest(whichMap);
                    }
                });
                t.start();
            } else if (userAction.contains("Globe Surround Test")) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        globeSurroundTest(whichMap);
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
    public void zoomToOverlay(ZoomToDialog dialog) {
        IContainer c =  MapNamesUtility.getContainer(dialog.getMap(), dialog.getSelectedOverlay());
        if((null != c) && (c instanceof IOverlay)) {
            dialog.getMap().zoomTo((IOverlay)c, true);
        }
    }

    @Override
    public void zoomToFeatures(ZoomToDialog dialog) {
        List<String> featureNames = dialog.getSelectedFeatures();
        List<IFeature> features = new ArrayList<>();
        if(null != featureNames) {
            for(String featureName: featureNames) {
                IContainer c = MapNamesUtility.getContainer(dialog.getMap(), featureName);
                if(c instanceof IFeature) {
                    features.add((IFeature) c);
                }
            }
        }

        if(features.size() == 0) {
            return;
        } else if(features.size() == 1) {
            dialog.getMap().zoomTo(features.get(0), true);
        } else {
            dialog.getMap().zoomTo(features, true);
        }
    }

    private void setBoundsTest(final int whichMap) {
        try {
            List<IFeature> features = maps[whichMap].getAllFeatures();
            if (null != features) {
                for (IFeature feature : features) {
                    if (feature instanceof MilStdSymbol) {
                        updateDesignator((MilStdSymbol) feature);
                        feature.apply();
                    }
                }
                Thread.sleep(bulkUpdateInterval);
            }
            Handler mainHandler = new Handler(activity.getMainLooper());
            Runnable myRunnable = new Runnable() {
                @Override
                public void run() {
                    FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                    BoundsDialog boundsDialog = BoundsDialog.newInstance("Set Bounds", maps[whichMap], ZoomAndBoundsTest.this);
                    boundsDialog.show(fm, "fragment_bounds_dialog");
                }
            };
            mainHandler.post(myRunnable);
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void cannedTestSetup(int whichMap) {
        try {

            clearMaps();
            createCannedOverlaysAndFeatures();

            maps[whichMap].setMidDistanceThreshold(500000);
            maps[whichMap].setFarDistanceThreshold(1000000);

            updateDesignator(p1);
            updateDesignator(p2);
            updateDesignator(p3);
            updateDesignator(p1_1);

            maps[whichMap].addOverlay(o1, true);
            o1.addOverlay(o3, true);
            o1.addOverlay(o2, true);
            o2.addOverlay(o3, true);
            o3.addFeature(p1, true);
            o2.addFeature(p2, true);
            o1.addFeature(p3, true);
            p1.addFeature(p1_1, true);
            o3.addFeature(p1_1, true);
            Thread.sleep(bulkUpdateInterval);
        } catch (EMP_Exception | InterruptedException e) {
            Log.e(TAG, "cannedTestSetup", e);
            updateStatus(TAG, e.getMessage());
        }
    }

    private void basicZoomToTest(int whichMap) {
        try {
            startTest("basicZoomTest");
            cannedTestSetup(whichMap);

            maps[whichMap].zoomTo(p1, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo TRUCK0");

            List<IFeature> list = new ArrayList<>();
            list.add(p2); list.add(p3);
            maps[whichMap].zoomTo(list, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo on TRUCK2, TRUCK3");

            maps[whichMap].zoomTo(o1, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo overlay o1, features TRUCK0-3");

            maps[whichMap].zoomTo(o2, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo overlay o2, features TRUCK0-2");

            updateMilStdSymbolPosition(p1, 40.1, 179.5);
            updateMilStdSymbolPosition(p2, 40.4, -179.5);
            p1.apply(); p2.apply(); Thread.sleep(bulkUpdateInterval);
            list.clear();
            list.add(p1); list.add(p2);
            maps[whichMap].zoomTo(list, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo feature list, features TRUCK0 and TRUCK2");

            updateMilStdSymbolPosition(p3, 40.0, 180.0);
            p3.apply(); Thread.sleep(bulkUpdateInterval);
            maps[whichMap].zoomTo(p3, false); Thread.sleep(bulkUpdateInterval);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo TRUCK3 at 40.0, 180.0");

            updateMilStdSymbolPosition(p3, -40.0, -180.0);
            p3.apply(); Thread.sleep(bulkUpdateInterval);
            maps[whichMap].zoomTo(p3, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo TRUCK3 -40.0, -180.0");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void altitudeTest(int whichMap) {
        try {
            startTest("altitudeTest");
            cannedTestSetup(whichMap);

            updateMilStdSymbolAltitude(p3, 300000);
            p3.apply(); Thread.sleep(bulkUpdateInterval);
            maps[whichMap].zoomTo(o1, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo overlay o1, features TRUCK0-3, TRUCK3 at 300000 m.");

            updateMilStdSymbolAltitude(p3, 100);
            p3.apply(); Thread.sleep(bulkUpdateInterval);
            maps[whichMap].zoomTo(o1, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo overlay o1, features TRUCK0-3, TRUCK3 at 100 m.");

            maps[whichMap].zoomTo(p3, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo TRUCK3, TRUCK3 at 100 m.");

            updateMilStdSymbolAltitude(p2, 1000);
            p2.apply(); Thread.sleep(bulkUpdateInterval);
            maps[whichMap].zoomTo(o1, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo overlay o1, features TRUCK0-3, TRUCK2 at 1000 m.");

            maps[whichMap].zoomTo(p2, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo TRUCK2, TRUCK3 at 1000 m.");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void basicBoundsTest(int whichMap) {
        try {
            startTest("basicBoundsTest");
            cannedTestSetup(whichMap);

            updateMilStdSymbolPosition(p1, -.9, -.9 );
            updateMilStdSymbolPosition(p1_1, -.9, .9);
            updateMilStdSymbolPosition(p2, .9, .9);
            updateMilStdSymbolPosition(p3, .9, -.9);
            p1.apply();
            p1_1.apply();
            p2.apply();
            p3.apply(); Thread.sleep(bulkUpdateInterval);

            IGeoBounds bounds = new GeoBounds();
            bounds.setSouth(-1.0);
            bounds.setNorth(1.0);
            bounds.setWest(-1.0);
            bounds.setEast(1.0);
            maps[whichMap].setBounds(bounds, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "setBounds -1.0, 1.0, -1.0, 1.0 TRCUK0-3 at four corners");


            updateMilStdSymbolPosition(p1, 40.1, -72.9 );
            updateMilStdSymbolPosition(p1_1, 40.1, -71.1);
            updateMilStdSymbolPosition(p2, 41.9, -72.9);
            updateMilStdSymbolPosition(p3, 41.9, -71.1);
            p1.apply();
            p1_1.apply();
            p2.apply();
            p3.apply(); Thread.sleep(bulkUpdateInterval);

            displayStatus("setBounds 40.0, 42.0, -73.0, -71.0 TRCUK0-3 at four corners");
            bounds.setSouth(40.0);
            bounds.setNorth(42.0);
            bounds.setWest(-73.0);
            bounds.setEast(-71.0);
            maps[whichMap].setBounds(bounds, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "setBounds 40.0, 42.0, -73.0, -71.0 TRCUK0-3 at four corners");

            updateMilStdSymbolPosition(p1, 40.01, -72.99 );
            updateMilStdSymbolPosition(p1_1, 40.01, -72.91);
            updateMilStdSymbolPosition(p2, 40.09, -72.99);
            updateMilStdSymbolPosition(p3, 40.09, -72.91);
            p1.apply();
            p1_1.apply();
            p2.apply();
            p3.apply(); Thread.sleep(bulkUpdateInterval);

            bounds.setSouth(40.0);
            bounds.setNorth(40.1);
            bounds.setWest(-73.0);
            bounds.setEast(-72.9);
            maps[whichMap].setBounds(bounds, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "setBounds 40.0, 40.1, -73.0, -72.9 TRCUK0-3 at four corners");

            updateMilStdSymbolPosition(p3, 40.0, -73.0);
            p3.apply(); Thread.sleep(bulkUpdateInterval);
            bounds.setSouth(40.0);
            bounds.setNorth(40.0);
            bounds.setWest(-73.0);
            bounds.setEast(-73.0);
            maps[whichMap].setBounds(bounds, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "setBounds 40.0, 40.0, -73.0, -73.0, TRUCK3 at 40.0, -73.0");

            updateMilStdSymbolPosition(p3, 40.0, 180.0);
            p3.apply(); Thread.sleep(bulkUpdateInterval);
            displayStatus("setBounds 40.0, 40.0, 179.99, 179.99, TRUCK3 40.0, 180.0");
            bounds.setSouth(40.0);
            bounds.setNorth(40.0);
            bounds.setWest(179.99);
            bounds.setEast(179.99);
            maps[whichMap].setBounds(bounds, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "setBounds 40.0, 40.0, 179.99, 179.99, TRUCK3 40.0, 180.0");

            updateMilStdSymbolPosition(p3, 40.0, -180.0);
            p3.apply(); Thread.sleep(bulkUpdateInterval);
            bounds.setSouth(40.0);
            bounds.setNorth(40.0);
            bounds.setWest(-179.99);
            bounds.setEast(-179.99);
            maps[whichMap].setBounds(bounds, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "setBounds 40.0, 40.0, -179.99, -179.99, TRUCK3 40.0, -180.0");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void nearPoleTest(int whichMap) {
        try {
            startTest("nearPoleTest");
            cannedTestSetup(whichMap);

            updateMilStdSymbolPosition(p3, 90.0, 0.0);
            p3.apply(); Thread.sleep(bulkUpdateInterval);
            displayStatus("zoomTo TRUCK3 90.0/0.0");
            maps[whichMap].zoomTo(p3, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo TRUCK3 at 90.0 0.0");

            updateMilStdSymbolPosition(p3, 89.0, 0.0);
            p3.apply(); Thread.sleep(bulkUpdateInterval);

            IGeoBounds bounds = new GeoBounds();
            displayStatus("setBounds 89.0, 1.0, 89.0, -1.0, TRUCK3 89.0, 0.0");
            bounds.setSouth(89.0);
            bounds.setNorth(89.0);
            bounds.setWest(-1.0);
            bounds.setEast(1.0);
            maps[whichMap].setBounds(bounds, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "setBounds 89.0, 1.0, 89.0, -1.0, TRUCK3 at 89.0, 0.0");

        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    private void globeSurroundTest(int whichMap) {
        try {
            startTest("nearPoleTest");
            cannedTestSetup(whichMap);

            updateMilStdSymbolPosition(p1, 0.0, 0.0);
            p1.apply(); Thread.sleep(bulkUpdateInterval);

            updateMilStdSymbolPosition(p1_1, 0.0, 90.0);
            p1_1.apply(); Thread.sleep(bulkUpdateInterval);

            updateMilStdSymbolPosition(p2, 0.0, 180.0);
            p2.apply(); Thread.sleep(bulkUpdateInterval);

            updateMilStdSymbolPosition(p3, 0.0, -90.0);
            p3.apply(); Thread.sleep(bulkUpdateInterval);
            maps[whichMap].zoomTo(o1, false);
            ErrorDialog.showMessageWaitForConfirm(activity, "zoomTo overlay o1, features TRUCK0-3 at 0/0, 0,90, 0,180, 0,-90");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            endTest();
        }
    }

    @Override
    public boolean boundsSet(BoundsDialog dialog) {
        dialog.getMap().setBounds(dialog.getBounds(), true);
        return true;
    }
}
