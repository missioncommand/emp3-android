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

/**
 * This class is launched when 'CAMERA' item from the actions menu is selected.
 *
 * How do I add a new Item to the action Menu?
 *    - Read the ValidateAndVerify.java documentation on how to make the item for your test appear in the menu.
 *    - Create a class in package optItems that extends OptItemBase and implements the required action. You can create a new package under
 *        optItems if you think you need more than one class to implement the action.
 *    - Now update the common/ExecuteTest.java@optionSettings (static block initialization).
 *
 * What are the requirements on the new class I create?
 *    - You must extend OptItemBase and override the 'run()' method.
 *    - It really depends on the complexity of your action but goal is to keep this simple.
 *    - Your only input is via the dialogs you put up as you have no access to the test specific buttons.
 */
public class Camera extends OptItemBase implements CameraDialog.ICameraDialogListener {
    private static String TAG = Camera.class.getSimpleName();

    public Camera(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        showCameraDialog(ExecuteTest.getCurrentMap());
    }

    private void showCameraDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                CameraDialog cameraDialogFragment = CameraDialog.newInstanceForOptItem("Camera", Camera.this, maps[whichMap].getCamera(), maps[whichMap]);
                cameraDialogFragment.show(fm, "fragment_camera_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void cameraSet(CameraDialog dialog) {
        IMap map = dialog.getMap();
        ICamera camera = dialog.getCamera();
        Log.d(TAG, "camera altitudeMode " + camera.getAltitudeMode().toString());
        map.getCamera().copySettingsFrom(dialog.getCamera());
        map.getCamera().apply(true);
    }
}
