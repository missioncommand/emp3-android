package mil.emp3.mirrorcache.api;

import android.util.Log;

import java.util.HashMap;

/**
 * This class loader is used by the underlying MirrorCache data
 * synchronization mechanism and is not part of the developer API.
 */
public class MirrorableLoader {
    private static final String TAG = MirrorableLoader.class.getSimpleName();

    private static final HashMap<String, Class> loadedClasses = new HashMap<>();

    public static IMirrorable newMirrorable(String className) {
        try {
            Class clazz = loadedClasses.get(className);
            if (clazz == null) {
                clazz = MirrorableLoader.class.getClassLoader().loadClass(className);
                loadedClasses.put(className, clazz);
            }

            return (IMirrorable) clazz.newInstance();

        } catch (Exception e) {
            Log.e(TAG, "ERROR: " + e.getMessage(), e);
            throw new IllegalStateException(e);
        }
    }

}
