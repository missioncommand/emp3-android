package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.util.Log;

import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.CirclePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddCircleFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<CirclePropertiesDialog> {
    private static String TAG = AddCircleFeature.class.getSimpleName();

    public AddCircleFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        return showPropertiesDialog(CirclePropertiesDialog.newInstance("Circle Properties", map, parentList,
                featureName, visible, AddCircleFeature.this), "fragment_circle_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(CirclePropertiesDialog dialog) {

        final Circle newFeature;
        try {
            newFeature = new Circle();
            newFeature.setRadius(dialog.getRadiusValue());
            newFeature.setBuffer(dialog.getBufferValue());
            addFeature(newFeature, dialog);
            Log.d(TAG, newFeature.toString());
            return true;
        } catch (Exception e) {
            Log.e(TAG, "onFeaturePropertiesSaveClick", e);
            statusListener.updateStatus(TAG, e.getMessage());
            ErrorDialog.showError(activity, "Failed to add Feature " + e.getMessage());
        }
        return false;
    }

    @Override
    public void onFeaturePropertiesCancelClick(CirclePropertiesDialog dialog) {

    }
}
