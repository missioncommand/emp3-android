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

import mil.emp3.api.GeoPackage;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.dialogs.GeoPackageRemoveDialog;
import mil.emp3.test.emp3vv.dialogs.GeoPackageSettingsDialog;

public class GeoPackageSettings extends OptItemBase implements GeoPackageSettingsDialog.IGeoPackageSettingsDialogListener,
    GeoPackageRemoveDialog.IGeoPackageRemoveDialogListener {
    final private static String TAG = GeoPackageSettings.class.getSimpleName();
    private boolean actionSet = true;
    private int currentMap = 0;
    private static HashMap<String, GeoPackage> geoPackageHashMap = new HashMap<>();

    public GeoPackageSettings(Activity activity, IMap map1, IMap map2){
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
            showGeoPackageSettingsDialog();
        } else {
            removeGeoPackage();
        }
    }

    private void removeGeoPackage() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                List<String> geoPackageNames = new ArrayList(geoPackageHashMap.keySet());
                GeoPackageRemoveDialog geoPackageRemoveDialog = GeoPackageRemoveDialog.newInstance("GeoPackageRemove",
                        GeoPackageSettings.this, maps[currentMap], geoPackageNames);
                geoPackageRemoveDialog.show(fm, "fm_geoPackageRemove");
            }
        };
        mainHandler.post(myRunnable);
    }

    private void showGeoPackageSettingsDialog() {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity) activity).getSupportFragmentManager();
                GeoPackageSettingsDialog geoPackageSettingsDialog = GeoPackageSettingsDialog.newInstance("GeoPackageSettings",
                        GeoPackageSettings.this, maps[currentMap]);
                geoPackageSettingsDialog.show(fm, "fm_geoPackageSettings");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void addGeoPackageService(IMap map, String name, String layer) {
        try {
            String url = "File://" + layer;
            GeoPackage geoPackage = geoPackageHashMap.get(name);
            if (geoPackage == null) {
                geoPackage = new mil.emp3.api.GeoPackage(url);
                map.addMapService(geoPackage);
                geoPackageHashMap.put(name, geoPackage);
            } else {
                new AlertDialog.Builder(activity)
                        .setTitle("ERROR")
                        .setMessage("GeoPackage with that name exists")
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
    public void removeGeoPackageService(IMap map, String name) {
        try {
            GeoPackage geoPackageToRemove = geoPackageHashMap.get(name);
            if (geoPackageToRemove == null) {
                new AlertDialog.Builder(activity)
                        .setTitle("ERROR")
                        .setMessage("No GeoPackage with that name")
                        .setNeutralButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface arg0, int arg1) {
                            }
                        }).create().show();
            } else {
                map.removeMapService(geoPackageToRemove);
                geoPackageHashMap.remove(name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
