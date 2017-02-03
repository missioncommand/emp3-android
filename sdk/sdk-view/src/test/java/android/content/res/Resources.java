package android.content.res;

import android.test.mock.MockResources;
import android.util.DisplayMetrics;

/**
 * Created by ish.rivera on 7/6/2016.
 */
public class Resources {
    private static Resources mSystem;
    final DisplayMetrics mMetrics = new DisplayMetrics();

    public static Resources getSystem() {
        Resources ret = mSystem;
        if (ret == null) {
            ret = new Resources();
            mSystem = ret;
        }

        return ret;
    }

    private Resources() {

    }

    public DisplayMetrics getDisplayMetrics() {
         return mMetrics;
    }
}
