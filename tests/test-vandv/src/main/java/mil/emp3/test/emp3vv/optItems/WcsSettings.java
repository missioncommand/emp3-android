package mil.emp3.test.emp3vv.optItems;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.WCS;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.dialogs.WcsRemoveDialog;
import mil.emp3.test.emp3vv.dialogs.WcsSettingsDialog;

public class WcsSettings  extends OptItemBase implements WcsSettingsDialog.IWcsSettingsDialogListener,
        WcsRemoveDialog.IWcsRemoveDialogListener {

    final private static String TAG = WcsSettings.class.getSimpleName();
    private boolean actionSet = true;
    private int currentMap = 0;
    private static HashMap<String, WCS> wcsMap = new HashMap<>();

    public WcsSettings(Activity activity, IMap map1, IMap map2){
        super(activity, map1, map2, TAG, true);
        this.currentMap = ExecuteTest.getCurrentMap();
    }

    public boolean isActionSet() {
        return actionSet;
    }

    public void setActionSet(boolean actionSet) {
        this.actionSet = actionSet;
    }

    @Override
    public void run() {
        if (actionSet) {
            showWcsSettingsDialog();
        } else {
            removeWcs();
        }
    }

    private void removeWcs() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                List<String> wcsNames = new ArrayList(wcsMap.keySet());
                WcsRemoveDialog wcsRemoveDialogFragment = WcsRemoveDialog.newInstance("WcsRemove",
                        WcsSettings.this, maps[currentMap], wcsNames);
                wcsRemoveDialogFragment.show(fm, "fm_wcsRemove");
            }
        };
        mainHandler.post(myRunnable);
    }

    private void showWcsSettingsDialog() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                WcsSettingsDialog wcsSettingsDialogFragment = WcsSettingsDialog.newInstance("WcsSettings",
                        WcsSettings.this, maps[currentMap]);
                wcsSettingsDialogFragment.show(fm, "fm_wcsSettings");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void addWcsService(IMap map, String name, String url, String coverage) {
        try {
            WCS wcsService = wcsMap.get(name);
            if (wcsService == null) {
                wcsService = new mil.emp3.api.WCS(
                        url,
                        coverage
                );
                map.addMapService(wcsService);
                wcsMap.put(name, wcsService);
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle("ERROR")
                        .setMessage("WCS with that name exists")
                        .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        }).create().show();
            }
        } catch (MalformedURLException | EMP_Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void removeWcsService(IMap map, String name) {
        try {
            WCS wcsToRemove = wcsMap.get(name);
            if (wcsToRemove == null) {
                new AlertDialog.Builder(activity)
                        .setTitle("ERROR")
                        .setMessage("No WCS with that name")
                        .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        }).create().show();
            } else {
                map.removeMapService(wcsToRemove);
                wcsMap.remove(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

