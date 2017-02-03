package mil.emp3.test.emp3vv.optItems;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.dialogs.CameraDialog;
import mil.emp3.test.emp3vv.dialogs.DistanceThresholdsDialog;

/**
 * Distance threshold settings.
 */
public class DistanceThresholds extends OptItemBase implements DistanceThresholdsDialog.IDistanceThresholdsDialogListener{
    private static String TAG = DistanceThresholds.class.getSimpleName();

    public DistanceThresholds(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        showDistanceThresholdDialog(ExecuteTest.getCurrentMap());
    }

    private void showDistanceThresholdDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                DistanceThresholdsDialog distanceThresholdsDialogFragment = DistanceThresholdsDialog.newInstanceForOptItem("Distance Threshold", DistanceThresholds.this, maps[whichMap]);
                distanceThresholdsDialogFragment.show(fm, "fragment_distance_threshold_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }


    /**
     * These callback are currently empty, all the required processing is done by the dialog.
     * @param distanceThresholdDialog
     */
    @Override
    public void setFarDistanceThreshold(DistanceThresholdsDialog distanceThresholdDialog) {

    }

    @Override
    public void setMidDistanceThreshold(DistanceThresholdsDialog distanceThresholdDialog) {

    }
}
