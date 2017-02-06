package mil.emp3.validator.features;

import android.app.FragmentManager;
import android.util.Log;

import mil.emp3.api.events.FeatureUserInteractionEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.validator.PlotModeEnum;
import mil.emp3.validator.dialogs.FeatureLocationDialog;
import mil.emp3.validator.model.ManagedMapFragment;

/**
 * Created by raju on 9/27/2016.
 */

public class FeatureInteractionEventListener implements IFeatureInteractionEventListener {

    final private static String TAG = FeatureInteractionEventListener.class.getSimpleName();

    private ManagedMapFragment currentMap;
    private FragmentManager oFragmentManager;

    public FeatureInteractionEventListener(ManagedMapFragment map,
                               FragmentManager fragmentManager) {
        currentMap = map;
        oFragmentManager = fragmentManager;
    }

    @Override
    public void onEvent(FeatureUserInteractionEvent event) {
        IFeature oFeature = event.getTarget().get(0);
        //SelectedFeature oSelectedItem;

//        bFeatureUIEventProcessed = true;
        if (event.getCoordinate() == null) {
            Log.d(TAG, "Feature User Interactive Event: " + event.getEvent().name() + " Feature Count: " + event.getTarget().size() + " X/Y: " + event.getPoint().x + " / " +
                    event.getPoint().y + "  Lat/Lon: null");
        } else {
            Log.d(TAG, "Feature User Interactive Event: " + event.getEvent().name() + " Feature Count: " + event.getTarget().size() + " X/Y: " + event.getPoint().x + " / " +
                    event.getPoint().y + "  Lat: " + event.getCoordinate().getLatitude() + " Lon: " + event.getCoordinate().getLongitude());
        }
        switch (event.getEvent()) {
            case CLICKED:
                if (currentMap.getPlotMode() == PlotModeEnum.IDLE) {
                    if (currentMap.get().isSelected(oFeature)) {
                        // Its being deselected.
                        currentMap.get().deselectFeature(oFeature);
                        FeatureLocationDialog oDialog = currentMap.getDialog(oFeature.getGeoId());
                        if (oDialog != null) {
                            oDialog.dismiss();
                            currentMap.removeDialog(oFeature.getGeoId());
                        }
                    } else {
                        // Its being selected.
                        currentMap.get().selectFeature(oFeature);
                        currentMap.setSelectedFeature(oFeature);
                        FeatureLocationDialog oDialog = new FeatureLocationDialog();
                        oDialog.show(oFragmentManager, null);
                        oDialog.setFeature(oFeature);
                        currentMap.putDialog(oFeature.getGeoId(), oDialog);
                    }
                }
                break;
            case DOUBLE_CLICKED:
                // On a tablet this is the double tap.
                if (oFeature != null) {
                    // If there is a feature and its selected, we want to place it in edit mode.
                    try {
                        if ((currentMap.getPlotMode() == PlotModeEnum.IDLE) &&
                                currentMap.get().isSelected(oFeature)) {
                            Log.d(TAG, "FeatureUserInteractionEvent  Entering edit mode.");
                            currentMap.setSelectedFeature(oFeature);
                            currentMap.get().editFeature(oFeature, new FeatureEditListener(currentMap,
                                    oFragmentManager));
                        }
                    } catch (EMP_Exception Ex) {
                        Log.d(TAG, "editFeature failed.", Ex);
                    }
                }
                break;
            case LONG_PRESS:
                if (event.getTarget().get(0) != null) {
                    // If there is a feature on the list, we want to place it in edit mode.
/*
                        if (currentMap.ePlotMode == PlotModeEnum.IDLE) {
                            Log.d(TAG, "FeatureUserInteractionEvent  Entering edit properties mode.");
                            currentMap.oCurrentSelectedFeature = event.getTarget().get(0);
                            currentMap.ePlotMode = PlotModeEnum.EDIT_PROPERTIES;
                            currentMap.openFeatureProperties();
                        }
*/
                }
                break;
            case DRAG:
                break;
        }
    }
}
