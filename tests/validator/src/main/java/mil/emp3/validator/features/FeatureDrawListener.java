package mil.emp3.validator.features;

import android.app.FragmentManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.validator.ContentActivity;
import mil.emp3.validator.PlotModeEnum;
import mil.emp3.validator.dialogs.FeatureLocationDialog;
import mil.emp3.validator.model.ManagedMapFragment;

/**
 * Created by raju on 9/21/2016.
 */

public class FeatureDrawListener implements IDrawEventListener {

    private static final String TAG = FeatureDrawListener.class.getSimpleName();
    private ManagedMapFragment currentMap;
    private FragmentManager oFragmentManager;

    public FeatureDrawListener(ManagedMapFragment map,
                               FragmentManager fragmentManager) {
        currentMap = map;
        oFragmentManager = fragmentManager;
    }

    @Override
    public void onDrawStart(IMap map) {
        Log.d(TAG, "Draw Start.");
        currentMap.setPlotMode(PlotModeEnum.DRAW_FEATURE);
        currentMap.show();
    }

    @Override
    public void onDrawUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
        Log.d(TAG, "Draw Update.");
        int[] aIndexes;
        String temp;

        for (IEditUpdateData updateData: updateList) {
            switch (updateData.getUpdateType()) {
                case COORDINATE_ADDED:
                case COORDINATE_MOVED:
                case COORDINATE_DELETED:
                    aIndexes = updateData.getCoordinateIndexes();
                    temp = "";
                    for (int index = 0; index < aIndexes.length; index++) {
                        if (temp.length() > 0) {
                            temp += ",";
                        }
                        temp += aIndexes[index];
                    }
                    Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " indexes:{" + temp + "}");
                    break;
                case MILSTD_MODIFIER_UPDATED:
                    Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " modifier: " + updateData.getChangedModifier().name() + " {" + ((MilStdSymbol) oFeature).getStringModifier(updateData.getChangedModifier()) + "}");
                    break;
                case ACM_ATTRIBUTE_UPDATED:
                    Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " ACM attribute:{" + updateData.getChangedModifier().name() + "}");
                    break;
            }
        }
        if (!currentMap.get().isSelected(oFeature)) {
            currentMap.get().selectFeature(oFeature);
            currentMap.setSelectedFeature(oFeature);
            FeatureLocationDialog oDialog = new FeatureLocationDialog();
            oDialog.show(oFragmentManager, null);
            oDialog.setFeature(oFeature);
            currentMap.putDialog(oFeature.getGeoId(), oDialog);
        } else {
            FeatureLocationDialog oDialog = currentMap.getDialog(oFeature.getGeoId());
            oDialog.updateDialog();
        }
    }

    @Override
    public void onDrawComplete(IMap map, IFeature feature) {
        Log.d(TAG, "Draw Complete.");
        currentMap.setPlotMode(PlotModeEnum.IDLE);
        currentMap.hide();
        currentMap.putFeature(feature.getGeoId(), feature);
        try {
            currentMap.getRootOverlay().addFeature(feature, true);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDrawCancel(IMap map, IFeature originalFeature) {
        Log.d(TAG, "Draw Canceled.");
        currentMap.setPlotMode(PlotModeEnum.IDLE);
        currentMap.hide();
    }

    @Override
    public void onDrawError(IMap map, String errorMessage) {
        Log.d(TAG, "Draw Error.");
    }
}
