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

import mil.emp3.api.WMTS;
import mil.emp3.api.enums.WMTSVersionEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.dialogs.WmtsRemoveDialog;
import mil.emp3.test.emp3vv.dialogs.WmtsSettingsDialog;

public class WmtsSettings extends OptItemBase implements WmtsSettingsDialog.IWmtsSettingsDialogListener,
        WmtsRemoveDialog.IWmtsRemoveDialogListener {

    final private static String TAG = WmtsSettings.class.getSimpleName();
    private boolean actionSet = true;
    private int currentMap = 0;
    private static HashMap<String, WMTS> wmtsMap = new HashMap<>();

    public WmtsSettings(Activity activity, IMap map1, IMap map2){
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
            showWmtsSettingsDialog();
        } else {
            removeWmts();
        }
    }

    private void removeWmts() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                List<String> wmtsNames = new ArrayList(wmtsMap.keySet());
                WmtsRemoveDialog wmtsRemoveDialogFragment = WmtsRemoveDialog.newInstance("WmtsRemove",
                        WmtsSettings.this, maps[currentMap], wmtsNames);
                wmtsRemoveDialogFragment.show(fm, "fm_wmtsRemove");
            }
        };
        mainHandler.post(myRunnable);
    }

    private void showWmtsSettingsDialog() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                WmtsSettingsDialog wmtsSettingsDialogFragment = WmtsSettingsDialog.newInstance("WmtsSettings",
                        WmtsSettings.this, maps[currentMap]);
                wmtsSettingsDialogFragment.show(fm, "fm_wmtsSettings");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void addWmtsService(IMap map, String name, String url, WMTSVersionEnum wmtsVersion, String tileFormat,
                               List<String> layers) {
        try {
            WMTS wmtsService = wmtsMap.get(name);
            if (wmtsService == null) {
                wmtsService = new mil.emp3.api.WMTS(
                        url,
                        wmtsVersion,
                        tileFormat,
                        layers
                );
                map.addMapService(wmtsService);
                wmtsMap.put(name, wmtsService);
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle("ERROR")
                        .setMessage("WMTS with that name exists")
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
    public void removeWmtsService(IMap map, String name) {
        try {
            WMTS wmtsToRemove = wmtsMap.get(name);
            if (wmtsToRemove == null) {
                new AlertDialog.Builder(activity)
                        .setTitle("ERROR")
                        .setMessage("No WMTS with that name")
                        .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        }).create().show();
            } else {
                map.removeMapService(wmtsToRemove);
                wmtsMap.remove(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

