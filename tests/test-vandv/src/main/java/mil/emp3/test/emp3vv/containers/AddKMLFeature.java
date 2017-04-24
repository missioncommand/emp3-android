package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.util.Log;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

import mil.emp3.api.KML;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeatureFromFilePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddKMLFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<FeatureFromFilePropertiesDialog> {
    private static String TAG = AddKMLFeature.class.getSimpleName();

    public AddKMLFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {

        FeatureFromFilePropertiesDialog instance = FeatureFromFilePropertiesDialog.newInstance("KML Properties", map, parentList,
                featureName, visible, AddKMLFeature.this, ".kml");

        List<String> availableFiles = instance.getFileList(".kml");
        if((null == availableFiles) || (0 == availableFiles.size())) {
            ErrorDialog.showError(activity, "Either Storage Permission is not set or there are no .kml files in external-storage/testFiles");
            return "no_fragment_displayed";
        }
        return showPropertiesDialog(instance, "fragment_kml_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(FeatureFromFilePropertiesDialog dialog) {

        String fileName = dialog.getSelectedFile();
        try {
            InputStream stream = new FileInputStream(fileName);
            IFeature newFeature = new KML(stream);
            KML kml = (KML) newFeature;

            Log.d(TAG, "KML " + kml.toString());
            for(IFeature f: kml.getFeatureList()) {
                Log.d(TAG, f.toString() + EmpGeoPosition.toString(f.getPositions()));
            }
            addFeature(newFeature, dialog);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "onFeaturePropertiesSaveClick", e);
            statusListener.updateStatus(TAG, e.getMessage());
            ErrorDialog.showError(activity, "Failed to add Feature " + e.getMessage());
        }
        return false;
    }

    @Override
    public void onFeaturePropertiesCancelClick(FeatureFromFilePropertiesDialog dialog) {

    }
}
