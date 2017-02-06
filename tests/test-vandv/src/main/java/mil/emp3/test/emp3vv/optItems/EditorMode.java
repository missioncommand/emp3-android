package mil.emp3.test.emp3vv.optItems;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;
import mil.emp3.test.emp3vv.dialogs.EditorModeDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;

public class EditorMode extends OptItemBase implements EditorModeDialog.IEditorModeDialogListener {
    private static String TAG = EditorMode.class.getSimpleName();

    public EditorMode(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        showEditorModeDialog(ExecuteTest.getCurrentMap());
    }

    private void showEditorModeDialog(final int whichMap) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                EditorModeDialog editorModeDialogFragment = EditorModeDialog.newInstanceForOptItem("Editor Mode", EditorMode.this, maps[whichMap]);
                editorModeDialogFragment.show(fm, "fragment_editor_mode_dialog");
            }
        };
        mainHandler.post(myRunnable);
    }

    @Override
    public void setMapMotionLockMode(EditorModeDialog editorModeDialog) {
        try {
            editorModeDialog.getMap().setMotionLockMode(editorModeDialog.getSelectedMapMotionLockMode());
        } catch (EMP_Exception e) {
            Log.e(TAG, "setMapMotionLockMode ", e);
            ErrorDialog.showError(activity, e.getMessage());
        }
    }
}
