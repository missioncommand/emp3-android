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

import mil.emp3.api.WMS;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.dialogs.WmsRemoveDialog;
import mil.emp3.test.emp3vv.dialogs.WmsSettingsDialog;

public class WmsSettings extends OptItemBase implements WmsSettingsDialog.IWmsSettingsDialogListener,
        WmsRemoveDialog.IWmsRemoveDialogListener {

    final private static String TAG = WmsSettings.class.getSimpleName();
    private boolean actionSet = true;
    private int currentMap = 0;
    private static HashMap<String, WMS> wmsMap = new HashMap<>();

    public WmsSettings(Activity activity, IMap map1, IMap map2){
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
            showWmsSettingsDialog();
        } else {
            removeWms();
        }
    }

    private void removeWms() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                List<String> wmsNames = new ArrayList(wmsMap.keySet());
                WmsRemoveDialog wmsRemoveDialogFragment = WmsRemoveDialog.newInstance("WmsRemove",
                        WmsSettings.this, maps[currentMap], wmsNames);
                wmsRemoveDialogFragment.show(fm, "fm_wmsRemove");
            }
        };
        mainHandler.post(myRunnable);
    }

    private void showWmsSettingsDialog() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                WmsSettingsDialog wmsSettingsDialogFragment = WmsSettingsDialog.newInstance("WmsSettings",
                        WmsSettings.this, maps[currentMap]);
                wmsSettingsDialogFragment.show(fm, "fm_wmsSettings");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void addWmsService(IMap map, String name, String url, WMSVersionEnum wmsVersion, String tileFormat,
                              boolean transparent, List<String> layers, double resolution) {
        try {
            WMS wmsService = wmsMap.get(name);
            if (wmsService == null) {
                wmsService = new mil.emp3.api.WMS(
                        url,
                        wmsVersion,
                        tileFormat,
                        transparent,
                        layers
                );
                wmsService.setLayerResolution(resolution);
                map.addMapService(wmsService);
                wmsMap.put(name, wmsService);
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle("ERROR")
                        .setMessage("WMS with that name exists")
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
    public void removeWmsService(IMap map, String name) {
        try {
            WMS wmsToRemove = wmsMap.get(name);
            if (wmsToRemove == null) {
                new AlertDialog.Builder(activity)
                        .setTitle("ERROR")
                        .setMessage("No WMS with that name")
                        .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        }).create().show();
            } else {
                map.removeMapService(wmsToRemove);
                wmsMap.remove(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
