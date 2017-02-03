package mil.emp3.api;


import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;

public class Emp3LifeCycleManager {
    static final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    private static boolean pausing = false;     // Maintains pause state based on onPause and onResume calls.
    /**
     *
     * https://developer.android.com/guide/topics/resources/runtime-changes.html
     *
     * Some device configurations can change during runtime (such as screen orientation, keyboard availability, and language). When such a change occurs,
     * Android restarts the running Activity (onDestroy() is called, followed by onCreate()). The restart behavior is designed to help your application
     * adapt to new configurations by automatically reloading your application with alternative resources that match the new device configuration.

     * To properly handle a restart, it is important that your activity restores its previous state through the normal Activity lifecycle, in which Android
     * calls onSaveInstanceState() before it destroys your activity so that you can save data about the application state. You can then restore the
     * state during onCreate() or onRestoreInstanceState().
     *
     * NOTE that applications can prevent Android from restarting an activity for some events by setting correct flags in the activity tag of
     * the manifest: android:configChanges="screenSize|orientation"
     *
     * Above text is straight from the link on the first line of this comment. Following is specific to the EMP3 API. When using EMP3 API applications have
     * two options:
     *     1. Destroy the data held by EMP3 API and take full responsibility for recreating the state i.e. map engine, camera, overlays, features, WMS
     *     2. Let EMP3 hold the current state of the map and recreate it on activity restart
     *
     * REQUIRED: In order for any of the options to work properly without any memory leaks applications need to take following steps:
     *
     *     1. Every instance of the Map should be named. As soon as application gets a reference to the IMap, application should invoke the setName method
     *          with unique name for each Map instance.
     *     2. Application Main Activity should override the on saveInstanceState method. In that method application should invoke the
     *           Emp3LifeCycleManager.onSaveInstanceState(boolean keepState) method.
     *           - To take on the full responsibility for map state restoration invoke the method with keepState = false
     *           - To let the EMP3 API restore the state invoke the method with keepState = true.
     *
     *     3. If application has chosen to let the EMP API restore the state then it needs to detect that activity restarted and avoid performing any
     *           initialization of the map other than invoking setName on the map.
     *
     *     4. If application is invoking the map from a layout file and application has set the "map_name" attribute then application doesn't have to
     *           set the name either.
     *     5. Applications must override onPause and onResume in main activity and invoke onPause and onResume methods of this class before invoking
     *           corresponding super class methods.
     *
     * @param keepState - set to true to restores Map Data on activity restart (Overlays, Features, MapService, Listeners)
     *                  - set to false to clean Map Data on activity restart, Application will restore from its own cache.
     *
     */

    public static void onSaveInstanceState(boolean keepState) {
        // If called during onPause then no action should be take.
        if(!pausing) {
            storageManager.onSaveInstanceState(keepState);
        }
    }

    /**
     * Mark the state as puaing
     */
    public static void onPause() {
        pausing = true;
    }

    /**
     * Remove from pausing state
     */
    public static void onResume() {
        pausing = false;
    }

}
