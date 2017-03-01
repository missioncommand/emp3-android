package mil.emp3.test.emp3vv.navItems.geoJSON_test;

import android.app.Activity;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.AddContainer;

public class GeoJSONTest extends NavItemBase {

    private final StyleManager styleManager;
    public GeoJSONTest(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
        styleManager = new StyleManager(activity, maps);
    }

    @Override
    public String[] getSupportedUserActions() {
        String[] actions = {"Add Overlay", "Add GeoJSON"};
        return actions;
    }

    @Override
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
    protected boolean exitTest() {
        String userAction = "Exit";
        return (actOn(userAction));
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
            } else if(userAction.equals("Add Overlay")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, styleManager);
                addContainer.showAddOverlayDialog();
            }  else if(userAction.equals("Add GeoJSON")) {
                AddContainer addContainer = new AddContainer(activity, maps[whichMap], this, styleManager);
                addContainer.showAddGeoJSONDialog();
            } else {
                styleManager.actOn(userAction);
            }
        } catch (Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
