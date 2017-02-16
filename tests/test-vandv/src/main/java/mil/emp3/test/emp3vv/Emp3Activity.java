package mil.emp3.test.emp3vv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import mil.emp3.api.Emp3LifeCycleManager;

/**
 * Implements the methods required by Emp3LifeCycleManager. This can be uses as a base class by Applications that use EMP3 API
 */
abstract public class Emp3Activity extends AppCompatActivity {
    private static String TAG = Emp3Activity.class.getSimpleName();
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "In onRestoreInstanceState");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "In onSaveInstanceState");
        Emp3LifeCycleManager.onSaveInstanceState(true); // Let EMP3 restore the map
    }

    @Override
    protected void onPause() {
        Emp3LifeCycleManager.onPause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        Emp3LifeCycleManager.onResume();
        super.onResume();
    }
}
