package mil.emp3.validator.features;

import android.app.FragmentManager;
import android.util.Log;

import java.util.List;

import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.validator.PlotModeEnum;
import mil.emp3.validator.dialogs.FeatureLocationDialog;
import mil.emp3.validator.model.ManagedMapFragment;

/**
 * Created by raju on 9/27/2016.
 */

public class FeatureEditListener implements IEditEventListener {

    final static private String TAG = FeatureEditListener.class.getSimpleName();

    private ManagedMapFragment currentMap;
    private FragmentManager oFragmentManager;

    public FeatureEditListener(ManagedMapFragment map,
                               FragmentManager fragmentManager) {
        currentMap = map;
        oFragmentManager = fragmentManager;
    }

    @Override
    public void onEditStart(IMap map) {
        Log.d(TAG, "Edit Start.");
        currentMap.setPlotMode(PlotModeEnum.EDIT_FEATURE);
        currentMap.show();
    }

    @Override
    public void onEditUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
        Log.d(TAG, "Edit Update.");
        FeatureLocationDialog oDialog = currentMap.getDialog(oFeature.getGeoId());
        if (oDialog != null) {
            oDialog.updateDialog();
        }
    }

    @Override
    public void onEditComplete(IMap map, IFeature feature) {
        Log.d(TAG, "Edit Complete.");
        currentMap.setPlotMode(PlotModeEnum.IDLE);
        currentMap.hide();
        FeatureLocationDialog oDialog = currentMap.getDialog(feature.getGeoId());
        if (oDialog != null) {
            oDialog.updateDialog();
        }
    }

    @Override
    public void onEditCancel(IMap map, IFeature originalFeature) {
        Log.d(TAG, "Edit Canceled.");
        currentMap.setPlotMode(PlotModeEnum.IDLE);
        currentMap.hide();
        FeatureLocationDialog oDialog = currentMap.getDialog(originalFeature.getGeoId());
        if (oDialog != null) {
            oDialog.updateDialog();
        }
    }

    @Override
    public void onEditError(IMap map, String errorMessage) {
        Log.d(TAG, "Edit Error.");
    }
}
