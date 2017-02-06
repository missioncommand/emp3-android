package mil.emp3.test.emp3vv.optItems;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoBounds;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.dialogs.BoundsDialog;
import mil.emp3.test.emp3vv.dialogs.CameraDialog;


public class Bounds extends OptItemBase implements BoundsDialog.IBoundsDialogListener {

    private static String TAG = Bounds.class.getSimpleName();

    public Bounds(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        showBoundsDialog(ExecuteTest.getCurrentMap());
    }

    private void showBoundsDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                BoundsDialog boundsDialogFragment = BoundsDialog.newInstanceForOptItem("Bounds", maps[whichMap], Bounds.this);
                boundsDialogFragment.show(fm, "fragment_bounds_dialog_optItem");
            }
        };
        mainHandler.post(myRunnable);
    }
    @Override
    public boolean boundsSet(BoundsDialog dialog) {
        dialog.getMap().setBounds(dialog.getBounds(), true);
        return true;
    }
}
