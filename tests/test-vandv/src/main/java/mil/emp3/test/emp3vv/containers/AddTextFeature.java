package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.List;

import mil.emp3.api.Text;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.TextPropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddTextFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<TextPropertiesDialog>{
    private static String TAG = AddTextFeature.class.getSimpleName();

    public AddTextFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        return showPropertiesDialog(TextPropertiesDialog.newInstance("Text Properties", map, parentList,
                featureName, visible, AddTextFeature.this), "fragment_text_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(TextPropertiesDialog dialog) {
        final Text newFeature;
        try {
            newFeature = new Text();
            newFeature.setText(dialog.getTextForFeature());
            newFeature.setAzimuth(dialog.getAzimuthValue());
            addFeature(newFeature, dialog);
            return true;
        } catch (EMP_Exception e) {
            Log.e(TAG, "onFeaturePropertiesSaveClick", e);
            statusListener.updateStatus(TAG, e.getMessage());
            ErrorDialog.showError(activity, "Failed to add Feature " + e.getMessage());
        }
        return false;
    }

    @Override
    public void onFeaturePropertiesCancelClick(TextPropertiesDialog dialog) {

    }
}
