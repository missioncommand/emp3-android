package mil.emp3.dev_test_sdk.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.dev_test_sdk.R;

/**
 * This class implements the MiniMap modeless Dialog box
 */

public class MiniMapDialog extends DialogFragment {
    private static final String TAG = MiniMapDialog.class.getSimpleName();

    private IMap empMap;
    private View oDialogView;
    private int iCurrentX;
    private int iCurrentY;

    public MiniMapDialog() {
        super();
    }

    public void setMap(IMap map) {
        empMap = map;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog oDialog = super.onCreateDialog(savedInstanceState);

        this.setStyle(DialogFragment.STYLE_NORMAL, R.style.ModelessWithTitleDialog);
        oDialog.setTitle("Mini Map");
        Window window = oDialog.getWindow();
        //window.setTitle("Mini Map");
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setGravity(Gravity.LEFT);

        return oDialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        oDialogView = inflater.inflate(R.layout.feature_location_dialog, null);

        if (null != empMap) {
            View miniMap = empMap.showMiniMap();
            ((RelativeLayout) oDialogView).addView(miniMap);
            //miniMap.setAlpha(0.6F);
        }

        oDialogView.setOnTouchListener(new View.OnTouchListener() {
            private int dx = 0;
            private int dy = 0;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dx = MiniMapDialog.this.iCurrentX - (int) motionEvent.getRawX();
                        dy = MiniMapDialog.this.iCurrentY - (int) motionEvent.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        MiniMapDialog.this.iCurrentX = (int) (motionEvent.getRawX() + dx);
                        MiniMapDialog.this.iCurrentY = (int) (motionEvent.getRawY() + dy);
                        Dialog oDialog =  MiniMapDialog.this.getDialog();
                        //oDialog.setTitle(this.oFeature.getName());
                        Window window = oDialog.getWindow();
                        WindowManager.LayoutParams params = window.getAttributes();
                        params.x = MiniMapDialog.this.iCurrentX;
                        params.y = MiniMapDialog.this.iCurrentY;
                        window.setAttributes(params);
                        break;
                }
                return true;
            }
        });

        return oDialogView;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog oDialog =  this.getDialog();
        //oDialog.setTitle("Mini Map");
        Window window = oDialog.getWindow();
        Resources oResource = getActivity().getResources();
        int pixels = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 320, oResource.getDisplayMetrics());
        WindowManager.LayoutParams params = window.getAttributes();
        params.width = pixels;
        params.height = pixels * 3 / 4;
        //params.setTitle("Mini Map");

        window.setAttributes(params);
        MiniMapDialog.this.iCurrentX = params.x;
        MiniMapDialog.this.iCurrentY = params.y;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if (null != this.empMap) {
            this.empMap.hideMiniMap();
            this.empMap = null;
        }
    }
}
