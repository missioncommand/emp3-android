package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.util.Log;

import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.List;

import mil.emp3.api.Polygon;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.FeaturePropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.PolygonPropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class AddPolygonFeature extends AddEntityBase implements FeaturePropertiesDialog.FeaturePropertiesDialogListener<PolygonPropertiesDialog>{
    private static String TAG = AddCircleFeature.class.getSimpleName();

    public AddPolygonFeature(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }

    public String showPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        return showPropertiesDialog(PolygonPropertiesDialog.newInstance("Polygon Properties", map, parentList,
                featureName, visible, AddPolygonFeature.this), "fragment_polygon_properties_dialog");
    }

    @Override
    public boolean onFeaturePropertiesSaveClick(PolygonPropertiesDialog dialog) {
        if(dialog.getPositionUtility().getPositionList().size() < 3) {
            ErrorDialog.showError(activity, "Polygon Feature needs at least three positions[position_count = " +
                    dialog.getPositionUtility().getPositionList().size() + "], tap on the screen to create positions");
            return false;
        }
        final Polygon newFeature;
        try {
            newFeature = new Polygon();
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
    public void onFeaturePropertiesCancelClick(PolygonPropertiesDialog dialog) {

    }
}
