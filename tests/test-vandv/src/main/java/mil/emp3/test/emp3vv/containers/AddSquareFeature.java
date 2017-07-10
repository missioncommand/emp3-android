package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.util.Log;

import java.util.List;

import mil.emp3.api.Square;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.SquarePropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddSquareFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<SquarePropertiesDialog> {
    private static String TAG = AddMilStdSymbol.class.getSimpleName();

    public AddSquareFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        return showPropertiesDialog(SquarePropertiesDialog.newInstance("Square Properties", map, parentList,
                featureName, visible, AddSquareFeature.this), "fragment_square_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(SquarePropertiesDialog dialog) {
        final Square newFeature;
        try {
            newFeature = new Square();
            newFeature.setWidth(dialog.getWidthValue());
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
    public void onFeaturePropertiesCancelClick(SquarePropertiesDialog dialog) {

    }
}

