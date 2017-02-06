package mil.emp3.test.emp3vv.optItems;


import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.dialogs.LookAtDialog;

public class LookAt extends OptItemBase implements LookAtDialog.ILookAtDialogListener{
    private static String TAG = Camera.class.getSimpleName();

    public LookAt(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        showLookAtDialog(ExecuteTest.getCurrentMap());
    }

    private void showLookAtDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                LookAtDialog cameraDialogFragment = LookAtDialog.newInstanceForOptItem("Camera", LookAt.this, maps[whichMap].getLookAt(), maps[whichMap]);
                cameraDialogFragment.show(fm, "fragment_look_at_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void lookAtSet(LookAtDialog dialog) {
        IMap map = dialog.getMap();
        ILookAt lookAt = dialog.getLookAt();
        Log.d(TAG, "lookAt altitudeMode " + lookAt.getAltitudeMode().toString());
        map.getLookAt().copySettingsFrom(dialog.getLookAt());
        map.getLookAt().apply(true);
    }
}
