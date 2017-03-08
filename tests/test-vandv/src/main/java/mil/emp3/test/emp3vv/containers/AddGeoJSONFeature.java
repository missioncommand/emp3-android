package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mil.emp3.api.GeoJSON;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.GeoJSONPropertiesDialog;

public class AddGeoJSONFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<GeoJSONPropertiesDialog>{
    private static String TAG = AddGeoJSONFeature.class.getSimpleName();

    public AddGeoJSONFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        if(parentList.size() == 0) {
            statusListener.updateStatus(TAG, "You need at least one overlay to add a feature");
            return "fragment_geojson_properties_dialog";
        }

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                GeoJSONPropertiesDialog geoJSONPropertiesDialog = GeoJSONPropertiesDialog.newInstance("GeoJSON Properties", map, parentList,
                              featureName, visible, AddGeoJSONFeature.this);
                geoJSONPropertiesDialog.show(fm, "fragment_geojson_properties_dialog");
            }
        };
        mainHandler.post(myRunnable);

        return "fragment_geojson_properties_dialog";
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(GeoJSONPropertiesDialog dialog) {
        String fileName = "/sdcard/Download/" + dialog.getSelectedGeoJSON();
        Log.i(TAG, "GeoJSON file " + fileName);
        try {
            InputStream stream = new FileInputStream(fileName);
            IFeature feature = new GeoJSON(stream);
            addFeature(feature, dialog);
            return true;
        } catch (EMP_Exception | IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onFeaturePropertiesCancelClick(GeoJSONPropertiesDialog dialog) {

    }
}
