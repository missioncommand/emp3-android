package mil.emp3.test.emp3vv.optItems;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.dialogs.MapBrightnessDialog;

/**
 * This class test the map brightness capability.
 */

public class MapBrightness extends OptItemBase implements MapBrightnessDialog.IBrightnessDialogListener {
    private static String TAG = MapBrightness.class.getSimpleName();

    public MapBrightness(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        showBrightnessDialog(ExecuteTest.getCurrentMap());
    }

    private void showBrightnessDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                MapBrightnessDialog dialogFragment = MapBrightnessDialog.newInstanceForOptItem("Map Brightness", MapBrightness.this, maps[whichMap]);
                dialogFragment.show(fm, "fragment_brightness_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void onDone() {

    }
}
