package mil.emp3.test.emp3vv.containers;


import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.List;

import mil.emp3.api.Rectangle;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;

import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.RectanglePropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddRectangleFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<RectanglePropertiesDialog> {
    private static String TAG = AddMilStdSymbol.class.getSimpleName();

    public AddRectangleFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        return showPropertiesDialog(RectanglePropertiesDialog.newInstance("Rectangle Properties", map, parentList,
                featureName, visible, AddRectangleFeature.this), "fragment_rectangle_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(RectanglePropertiesDialog dialog) {

        final Rectangle newFeature;
        try {
            newFeature = new Rectangle();
            newFeature.setWidth(dialog.getWidthValue());
            newFeature.setHeight(dialog.getHeightValue());
            newFeature.setAzimuth(dialog.getAzimuthValue());
            newFeature.setBuffer(dialog.getBufferValue());
            addFeature(newFeature, dialog);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "onFeaturePropertiesSaveClick", e);
            statusListener.updateStatus(TAG, e.getMessage());
            ErrorDialog.showError(activity, "Failed to add Feature " + e.getMessage());
            return false;
        }
    }

    @Override
    public void onFeaturePropertiesCancelClick(RectanglePropertiesDialog dialog) {

    }
}
