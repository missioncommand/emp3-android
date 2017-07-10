package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.util.Log;

import java.util.List;

import mil.emp3.api.Ellipse;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.EllipsePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddEllipseFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<EllipsePropertiesDialog> {
    private static String TAG = AddCircleFeature.class.getSimpleName();

    public AddEllipseFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        return showPropertiesDialog(EllipsePropertiesDialog.newInstance("Ellipse Properties", map, parentList,
                featureName, visible, AddEllipseFeature.this), "fragment_ellipse_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(EllipsePropertiesDialog dialog) {

        final Ellipse newFeature;
        try {
            newFeature = new Ellipse();
            newFeature.setSemiMajor(dialog.getSemiMajorValue());
            newFeature.setSemiMinor(dialog.getSemiMinorValue());
            newFeature.setAzimuth(dialog.getAzimuthValue());
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
    public void onFeaturePropertiesCancelClick(EllipsePropertiesDialog dialog) {

    }
}
