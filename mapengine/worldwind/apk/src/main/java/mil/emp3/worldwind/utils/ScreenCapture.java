package mil.emp3.worldwind.utils;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.EGLContext;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IScreenCaptureCallback;

/**
 * This class handles the screen capture capability and implements the ICapture interface to return to the EMP core.
 */

public class ScreenCapture implements ICapture {
    final static private String TAG = ScreenCapture.class.getSimpleName();

    private final IScreenCaptureCallback outcomeCallback;
    private Bitmap screenShotBitmap = null;

    public ScreenCapture(IScreenCaptureCallback callback) {
        outcomeCallback = callback;
    }

    private void createBitmapFromGLSurface(final int x, final int y, final int w, final int h) {
        final int bitmapBuffer[] = new int[w * h];
        final int bitmapSource[] = new int[w * h];
        java.nio.Buffer intBuffer = java.nio.IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        // This runs in the UI thread.
        try {
            GLES20.glFinish();
            GLES20.glReadPixels(x, y, w, h, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, intBuffer);
        } catch (GLException e) {
            Log.e(TAG, "createBitmapFromGLSurface: " + e.getMessage(), e);
            throw e;
        }

        // Once its done, process the buffer in another thread.
        Thread buildBitmapThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    int offset1, offset2;
                    for (int i = 0; i < h; i++) {
                        offset1 = i * w;
                        offset2 = (h - i - 1) * w;
                        for (int j = 0; j < w; j++) {
                            int texturePixel = bitmapBuffer[offset1 + j];
                            int blue = (texturePixel >> 16) & 0xff;
                            int red = (texturePixel << 16) & 0x00ff0000;
                            int pixel = (texturePixel & 0xff00ff00) | red | blue;
                            bitmapSource[offset2 + j] = pixel;
                        }
                    }
                    ScreenCapture.this.screenShotBitmap = Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
                    ScreenCapture.this.outcomeCallback.captureSuccess(ScreenCapture.this);
                } catch (Exception Ex) {
                    ScreenCapture.this.outcomeCallback.captureFailed(Ex);
                }
            }
        });
        buildBitmapThread.start();
    }

    public void generateScreenCapture(gov.nasa.worldwind.WorldWindow ww) {
        final int x = ww.getLeft();
        final int y = ww.getTop();
        final int width = ww.getWidth();
        final int height = ww.getHeight();

        try {
            ww.queueEvent(new Runnable() {
                @Override
                public void run() {
                    try {
                        createBitmapFromGLSurface(x, y, width, height);
                    } catch (Exception Ex) {
                        outcomeCallback.captureFailed(Ex);
                    }
                }
            });
        } catch (Exception Ex) {
            outcomeCallback.captureFailed(Ex);
        }
    }

    @Override
    public Bitmap screenshotBitmap() {
        return screenShotBitmap;
    }

    @Override
    public String screenshotDataURL() {

        if (null == screenShotBitmap) {
            return null;
        }
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            screenShotBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();

            return "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception Ex) {

        }

        return null;
    }
}
