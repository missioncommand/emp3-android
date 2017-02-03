package mil.emp3.openstreet.utils;

import android.os.Looper;

/**
 *
 */
public class SystemUtils {
    public static boolean isCurrentThreadUIThread() {
        return (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId());
    }
}
