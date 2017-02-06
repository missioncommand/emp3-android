package mil.emp3.test.emp3vv.navItems;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.List;

import mil.emp3.api.enums.Property;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.test.emp3vv.common.Emp3TesterDialogBase;
import mil.emp3.test.emp3vv.common.NavItemBase;
import mil.emp3.test.emp3vv.containers.AddContainer;
import mil.emp3.test.emp3vv.containers.UpdateContainer;
import mil.emp3.test.emp3vv.utils.MapNamesUtility;

public class LaunchMap extends NavItemBase {

    private static String TAG = LaunchMap.class.getSimpleName();
    public LaunchMap(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG);
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
    private void launchMap(int whichMap) {
        try {
            updateStatus(TAG, "Launching WorldWind Map Please WAIT");
            IEmpPropertyList properties = new EmpPropertyList();
            properties.put(Property.ENGINE_CLASSNAME.getValue(), "mil.emp3.worldwind.MapInstance");
            properties.put(Property.ENGINE_APKNAME.getValue(), "mil.emp3.worldwind");
            maps[whichMap].swapMapEngine(properties);
        } catch (EMP_Exception e) {
            Log.e(TAG, "launch map failed", e);
            updateStatus(TAG, "Launching WorldWind Map FAILED: " + e.getMessage());
        }
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

    @Override
    public String[] getSupportedUserActions() {

        String[] actions = {"Add WorldWind Instance-1", "Add WorldWind Instance-2"};
        return actions;

    }

    @Override
    public boolean actOn(String userAction) {
        final int whichMap = userAction.contains("-2") ? 1 : 0;

        try {
            if(Emp3TesterDialogBase.isEmp3TesterDialogBaseActive()) {
                updateStatus("Dismiss the dialog first");
                return false;
            }

            if (userAction.equals("Exit")) {
                testThread.interrupt();
            } else if(userAction.equals("ClearMap")) {
                clearMaps();
            } else if(userAction.contains("Add WorldWind Instance")) {
                launchMap(whichMap);
            }
        } catch (Exception e) {
            updateStatus(TAG, e.getMessage());
            e.printStackTrace();
        }
        return true;
    }
}
