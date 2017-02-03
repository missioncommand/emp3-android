package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.List;

import mil.emp3.api.Point;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.PointPropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddPointFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<PointPropertiesDialog>{
    private static String TAG = AddCircleFeature.class.getSimpleName();

    public AddPointFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        return showPropertiesDialog(PointPropertiesDialog.newInstance("Point Properties", map, parentList,
                featureName, visible, AddPointFeature.this), "fragment_point_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(PointPropertiesDialog dialog) {
        final Point newFeature;
        try {
            newFeature = new Point();
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
    public void onFeaturePropertiesCancelClick(PointPropertiesDialog dialog) {

    }
}