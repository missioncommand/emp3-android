package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.util.Log;

import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.PathPropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddPathFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<PathPropertiesDialog>{
    private static String TAG = AddCircleFeature.class.getSimpleName();

    public AddPathFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        return showPropertiesDialog(PathPropertiesDialog.newInstance("Path Properties", map, parentList,
                featureName, visible, AddPathFeature.this), "fragment_path_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(PathPropertiesDialog dialog) {

        if(dialog.getPositionUtility().getPositionList().size() < 2) {
            ErrorDialog.showError(activity, "Path Feature needs at least two positions[position_count = " +
                    dialog.getPositionUtility().getPositionList().size() + "], tap on the screen to create positions");
            return false;
        }
        final Path newFeature;
        try {
            newFeature = new Path();
            newFeature.setBuffer(dialog.getBufferValue());
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
    public void onFeaturePropertiesCancelClick(PathPropertiesDialog dialog) {

    }
}
