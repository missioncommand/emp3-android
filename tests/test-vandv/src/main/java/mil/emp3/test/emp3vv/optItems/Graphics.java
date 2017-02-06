package mil.emp3.test.emp3vv.optItems;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.dialogs.GraphicsDialog;

public class Graphics extends OptItemBase implements GraphicsDialog.IGraphicsDialogListener {
    private static String TAG = Graphics.class.getSimpleName();

    public Graphics(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        showGraphicsDialog(ExecuteTest.getCurrentMap());
    }

    private void showGraphicsDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                GraphicsDialog graphicsDialogFragment = GraphicsDialog.newInstanceForOptItem("Graphics", Graphics.this, maps[whichMap]);
                graphicsDialogFragment.show(fm, "fragment_graphics_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void graphicsSet(GraphicsDialog cameraDialog) {

    }
}
