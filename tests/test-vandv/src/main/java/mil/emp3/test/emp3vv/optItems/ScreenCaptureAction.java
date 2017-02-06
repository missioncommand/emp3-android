package mil.emp3.test.emp3vv.optItems;

import android.Manifest;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IScreenCaptureCallback;
import mil.emp3.test.emp3vv.common.ExecuteTest;
import mil.emp3.test.emp3vv.common.OptItemBase;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * This class implements the screen capture capability of EMP.
 */

public class ScreenCaptureAction extends OptItemBase {
    private static String TAG = ScreenCaptureAction.class.getSimpleName();

    public ScreenCaptureAction(Activity activity, IMap map1, IMap map2) {
        super(activity, map1, map2, TAG, true);
    }

    @Override
    public void run() {
        int iMap = ExecuteTest.getCurrentMap();

        if (iMap == -1) {
            return;
        }

        maps[iMap].getScreenCapture(new IScreenCaptureCallback() {
            @Override
            public void captureSuccess(ICapture capture) {
                String dataURL = capture.screenshotDataURL();
                FileOutputStream out = null;
                int permissionCheck = ContextCompat.checkSelfPermission(ScreenCaptureAction.super.activity,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE);
                if (permissionCheck !=  PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        //ScreenCaptureAction.super.activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                        ActivityCompat.requestPermissions(ScreenCaptureAction.super.activity,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    }
                } else {
                    try {
                        File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                        final File dest = new File(sd, "empscreenshot.png");
                        if (dest.exists()) {
                            dest.delete();
                        }
                        out = new FileOutputStream(dest);
                        if (!capture.screenshotBitmap().compress(Bitmap.CompressFormat.PNG, 100, out)) {
                            Log.e(TAG, "The Bitmap compress return false.");
                        }
                        // PNG is a loss less format, the compression factor (100) is ignored
                        ScreenCaptureAction.super.activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ScreenCaptureAction.super.activity, "Screenshot stored in Pictures/empscreenshot.png", Toast.LENGTH_LONG).show();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            if (out != null) {
                                out.flush();
                                out.close();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void captureFailed(Exception Ex) {
                Log.e(TAG, "Screen Capture Failed.", Ex);
            }
        });
    }
}
