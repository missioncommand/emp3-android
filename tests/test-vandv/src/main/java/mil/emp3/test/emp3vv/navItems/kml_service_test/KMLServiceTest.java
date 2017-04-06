package mil.emp3.test.emp3vv.navItems.kml_service_test;

import android.app.Activity;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.KMLS;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.common.StyleManager;

public class KMLServiceTest extends NavItemBase {

    private static String TAG = KMLServiceTest.class.getSimpleName();

    private final StyleManager styleManager;

    public KMLServiceTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
        styleManager = new StyleManager(activity, maps);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Add Service", "Remove Service", "Show Services"};
        return actions;
    }

    @Override
    public String[] getMoreActions() {
        return styleManager.getMoreActions(null);
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
                testThread.interrupt();
            } else if (userAction.equals("ClearMap")) {
                clearMaps();
            } else if (userAction.equals("Add Service")) {
                KMLS kmls = new KMLS(activity, "https://github.com/downloads/brazzy/nikki/example.kmz");
                maps[whichMap].addMapService(kmls);
            } else if (userAction.equals("Remove Service")) {

            } else if (userAction.equals("Show Services")) {

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
        return (actOn(userAction));
    }
}
