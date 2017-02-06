package mil.emp3.test.emp3vv.dialogs.utils;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.WindowManager;

import mil.emp3.test.emp3vv.navItems.zoom_and_bounds.ZoomAndBoundsTest;
import mil.emp3.test.emp3vv.navItems.zoom_and_bounds.ZoomToDialog;

public class ErrorDialog {

    public static void showError(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // continue with delete
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static void showMessageWaitForConfirm(final Context context, final String message) {

        final Object object = new Object();
        Handler mainHandler = new Handler(context.getMainLooper());
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

                alertDialogBuilder.setTitle("Message")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                synchronized (object) {
                                    object.notify();
                                }
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert);

                AlertDialog dialog = alertDialogBuilder.create();

                dialog.getWindow().setGravity(Gravity.RIGHT | Gravity.BOTTOM);
                dialog.getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
                dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
                dialog.show();
            }
        };
        mainHandler.post(myRunnable);
        synchronized (object) {
            try {
                object.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
