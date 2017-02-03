package mil.emp3.worldwind.utils;

import android.opengl.GLSurfaceView;
import android.util.Log;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLDisplay;

public class ConfigChooser implements GLSurfaceView.EGLConfigChooser {
    static private final String kTag = "GDC11";

    private int[] mValue;
    private int numConfigs = 64;

    @Override
    public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
        mValue = new int[1];

        int[] configSpec = {
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_NONE
        };

        // Get all matching configurations.
        EGLConfig[] configs = new EGLConfig[numConfigs];
        if (!egl.eglChooseConfig(display, configSpec, configs, numConfigs, mValue)) {
            throw new IllegalArgumentException("data eglChooseConfig failed");
        }
        numConfigs = mValue[0];
        Log.i(ConfigChooser.class.getSimpleName(), "eglChooseConfig returned " + numConfigs + " configurations");

        // CAUTION! eglChooseConfigs returns configs with higher bit depth
        // first: Even though we asked for rgb565 configurations, rgb888
        // configurations are considered to be "better" and returned first.
        // You need to explicitly filter the data returned by eglChooseConfig!
        int index = 0;
        for (int i = 0; i < numConfigs; ++i) {
            Log.i(kTag, "config " + i);

            int res = findConfigAttrib(egl, display, configs[i], EGL10.EGL_RED_SIZE, 0);
            Log.i(kTag, "R " + res);

            res = findConfigAttrib(egl, display, configs[i], EGL10.EGL_GREEN_SIZE, 0);
            Log.i(kTag, "G " + res);

            res = findConfigAttrib(egl, display, configs[i], EGL10.EGL_BLUE_SIZE, 0);
            Log.i(kTag, "B " + res);

            res = findConfigAttrib(egl, display, configs[i], EGL10.EGL_ALPHA_SIZE, 0);
            Log.i(kTag, "A " + res);

            res = findConfigAttrib(egl, display, configs[i], EGL10.EGL_DEPTH_SIZE, 0);
            Log.i(kTag, "D " + res);
        }

        if (index == -1) {
            Log.w(kTag, "Did not find sane config, using first");
        }
        EGLConfig config = numConfigs > 0 ? configs[index] : null;

        if (config == null) {
            throw new IllegalArgumentException("No config chosen");
        }
        return config;
    }

    private int findConfigAttrib(EGL10 egl, EGLDisplay display, EGLConfig config, int attribute, int defaultValue) {
        if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
            return mValue[0];
        }
        return defaultValue;
    }
}