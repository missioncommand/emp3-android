package mil.emp3.joystick;

import android.app.Activity;
import android.os.Bundle;

import wei.mark.standout.StandOutWindow;


public class TestCameraActivity extends Activity {

    private static final String TAG = TestCameraActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_test);

        StandOutWindow.show(getBaseContext(), WallpaperController.class, StandOutWindow.DEFAULT_ID);
    }
}
