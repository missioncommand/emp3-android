package mil.emp3.worldwind.utils;

import android.os.Looper;

/**
 *
 */
public class SystemUtils {
    public static boolean isCurrentThreadUIThread() {
        return (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId());
    }
}
