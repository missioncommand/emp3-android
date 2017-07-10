package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.IGeoRenderable;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import mil.emp3.api.GeoJSON;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeatureFromFilePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddGeoJSONFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<FeatureFromFilePropertiesDialog> {
    private static String TAG = AddGeoJSONFeature.class.getSimpleName();

    public AddGeoJSONFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {

        FeatureFromFilePropertiesDialog instance = FeatureFromFilePropertiesDialog.newInstance("GeoJSON Properties", map, parentList,
                featureName, visible, AddGeoJSONFeature.this, ".geojson");

        List<String> availableFiles = instance.getFileList(".geojson");
        if((null == availableFiles) || (0 == availableFiles.size())) {
            ErrorDialog.showError(activity, "Either Storage Permission is not set or there are no .kml files in external-storage/testFiles");
            return "no_fragment_displayed";
        }
        return showPropertiesDialog(instance, "fragment_geo_json_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(FeatureFromFilePropertiesDialog dialog) {
        String fileName = dialog.getSelectedFile();
        Log.i(TAG, "GeoJSON file " + fileName);
        try {
            InputStream stream = new FileInputStream(fileName);
            IFeature<IGeoRenderable> newFeature = new GeoJSON(stream);
            addFeature(newFeature, dialog);
            return true;
        } catch (EMP_Exception | IOException e) {
            statusListener.updateStatus(TAG, e.getMessage());
            ErrorDialog.showError(activity, "Failed to add Feature " + e.getMessage());
        }
        return false;
    }

    @Override
    public void onFeaturePropertiesCancelClick(FeatureFromFilePropertiesDialog dialog) {

    }
}
