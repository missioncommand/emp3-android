package mil.emp3.test.emp3vv.common;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.containers.IStatusListener;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public abstract class NavItemBase implements UserAction, IStatusListener, Runnable {

    protected static String TAG;
    protected Thread testThread;

    protected double latitude = 40.2171;
    protected double longitude = -74.7429;
    protected MilStdSymbol p1, p1_1, p2, p3;
    protected IOverlay o1, o2, o3;

    protected final IMap m1, m2;
    protected final IMap maps[] = new IMap[2];
    protected final Activity activity;
    protected OnTestStatusUpdateListener statusUpdateListener;

    protected final int small_waitInterval = 1000;
    protected final int large_waitInterval = 30000;
    protected final int bulkUpdateInterval = 300;

    protected ITestMenuManager testMenuManager;
    protected int maxSupportedActions;

    public NavItemBase(Activity activity, IMap map1, IMap map2, String tag) {
        this.m1 = map1;
        this.m2 = map2;

        maps[0] = map1;
        maps[1] = map2;

        this.activity = activity;
        TAG = tag;

        if(activity instanceof OnTestStatusUpdateListener) statusUpdateListener = (OnTestStatusUpdateListener) activity;
        setUp();
    }

    protected void setUp()
    {

    }

    protected void createCannedOverlaysAndFeatures() {
        int count = 0;
        double multiplier = .01;
        p1 = generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * multiplier), longitude + (count * multiplier));
        o1 = new Overlay();
        o1.setName("o1");
        o2 = new Overlay();
        o2.setName("o2");
        o3 = new Overlay();
        o3.setName("o3");
        count++;
        p1_1 = generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * multiplier), longitude + (count * multiplier));
        count++;
        p2 = generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * multiplier), longitude + (count * multiplier));
        count++;
        p3 = generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * multiplier), longitude + (count * multiplier));
    }

    protected abstract void test0();

    @Override
    public void run() {

        testThread = Thread.currentThread();
        try {
            test0();
        } catch (Exception e) {
            Log.d(TAG, "run:" , e);
        } finally {
            testComplete();
        }
    }

    protected static MilStdSymbol generateMilStdSymbol(String description, UUID uuid, double latitude, double longitude) {
        List<IGeoPosition> oPositionList = new ArrayList<>();
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
        oSPSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        return oSPSymbol;
    }


    protected static List<IFeature> generateMilStdSymbolList(int howMany, double latitude, double longitude) {
        long startCount = 1000;
        List<IFeature> list = new ArrayList<>();
        for(int ii = 0; ii < howMany; ii++) {
            list.add(generateMilStdSymbol(String.valueOf(ii), new UUID(startCount, startCount++), latitude + (ii * .005), longitude + (ii * .005)));
        }
        return list;
    }

    protected void updateMilStdSymbolPosition(MilStdSymbol symbol, double latitude, double longitude) {
        IGeoPosition oPosition = symbol.getPositions().get(0);
        oPosition.setLatitude(latitude);
        oPosition.setLongitude(longitude);
        updateDesignator(symbol);
    }

    protected void updateMilStdSymbolAltitude(MilStdSymbol symbol, double altitude) {
        IGeoPosition oPosition = symbol.getPositions().get(0);
        oPosition.setAltitude(altitude);
        updateDesignator(symbol);
    }

    protected static void updateDesignator(MilStdSymbol symbol) {
        IGeoPosition oPosition = symbol.getPositions().get(0);

        String designator  = String.format(Locale.US, "%1$6.3f:%2$6.3f:%3$6.0f",
                oPosition.getLatitude(), oPosition.getLongitude(), oPosition.getAltitude());
        symbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, designator);
    }
    protected void updateStatus(String updatedStatus) {
        if(null != statusUpdateListener) {
            statusUpdateListener.onTestStatusUpdated(updatedStatus);
        }
    }

    public void updateStatus(String TAG, String updatedStatus) {
        if(null != statusUpdateListener) {
            statusUpdateListener.onTestStatusUpdated(TAG + " " + updatedStatus);
        }
    }

    protected void testComplete() {
        if(null != statusUpdateListener) {
            statusUpdateListener.onTestCompleted(this.getClass().getSimpleName());
        }
    }

    private String currentTest = null;
    protected void startTest(String test) {
        currentTest = test;
        updateStatus("Starting... " + this.getClass().getSimpleName() + ":[" + currentTest + "] ");
    }

    protected void endTest() {
        updateStatus("Ending... " + this.getClass().getSimpleName() + ":[" + currentTest + "] ");
        clearMaps();
    }

    protected void clearMaps() {
        if(null != m1) clearMap(m1);
        if(null != m2) clearMap(m2);
    }

    private void clearMap(IMap map) {
        if(null != map) {
            List<IOverlay> overlays = map.getAllOverlays();
            for(IOverlay overlay: overlays) {
                try {
                    map.removeOverlay(overlay);
                } catch (EMP_Exception e) {

                }
            }
        }
    }

    /**
     * This is test specific clear map based on an invocation from the options menu.
     */
    protected void clearMapForTest() {

    }

    /**
     * This is test specific EXIT based on an invocation from the optionas menu
     */

    protected abstract boolean exitTest();

    protected void displayStatus(String status) {
        updateStatus(this.getClass().getSimpleName() + ":[" + currentTest + "] " + status );
        try {
            Thread.sleep(small_waitInterval);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected Handler getHandler() {
        return statusUpdateListener.getHandler();
    }

    @Override
    public String[] getSupportedUserActions() {
        return null;
    }

    @Override
    public String[] getMoreActions() { return null; }

    @Override
    public void registerTestMenuManager(ITestMenuManager testMenuManager, int maxSupportedActions) {
        this.testMenuManager = testMenuManager;
        this.maxSupportedActions = maxSupportedActions;
    }

    protected IOverlay createOverlay(IMap map) {
        IOverlay overlay;
        List<IOverlay> overlays = map.getAllOverlays();
        if ((null == overlays) || (0 == overlays.size())) {
            overlay = new Overlay();
            overlay.setName("Edit/Draw Feature Overlay");
            try {
                map.addOverlay(overlay, true);
            } catch (EMP_Exception e) {
                Log.e(TAG, "createFeature ", e);
                updateStatus(TAG, "createFeature FAILED");
                ErrorDialog.showError(activity, e.getMessage());
                return null;
            }
        } else {
            overlay = overlays.get(0);
        }
        return overlay;
    }
}

