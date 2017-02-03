package mil.emp3.api.utils;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.io.File;

import dalvik.system.DexClassLoader;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.mapengine.CopySharedObjectFile;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;

public class ManagerFactory {
    static private String TAG = ManagerFactory.class.getSimpleName();

    static private ManagerFactory instance;

    final private IStorageManager storageManager;
    final private IEventManager eventManager;
    final private ICoreManager coreManager;

    final private IMilStdRenderer milStdRenderer;

    static final private Application application;

    // All classes are loaded from the same APK so use one dexLoader for all managers. Actually it is a must otherwise
    // Classes created by these managers will belong to different class loaders and to begin with single instance logic
    // for those classes will fail. Example is BitmapCachefactory.

    private DexClassLoader dexLoader = null;

    static {
        try { // retrieves the Application object
            application = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null, (Object[]) null);
        } catch (Exception e) {
            throw new IllegalStateException("ERROR: " + e.getMessage(), e);
        }
    }

    private ManagerFactory() {
        try {
            storageManager = (IStorageManager) dexLoadClass("mil.emp3.emp3_android_sdk_core_apk", "mil.emp3.core.storage.StorageManager");
            eventManager   = (IEventManager)   dexLoadClass("mil.emp3.emp3_android_sdk_core_apk", "mil.emp3.core.EventManager");
            coreManager    = (ICoreManager)    dexLoadClass("mil.emp3.emp3_android_sdk_core_apk", "mil.emp3.core.CoreManager");

            milStdRenderer = (IMilStdRenderer) dexLoadClass("mil.emp3.emp3_android_sdk_core_apk", "mil.emp3.core.utils.MilStdRenderer");


            storageManager.setEventManager(eventManager);
            eventManager.setStorageManager(storageManager);
            coreManager.setStorageManager(storageManager);
            coreManager.setEventManager(eventManager);

            milStdRenderer.setStorageManager(storageManager);

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
            throw new IllegalStateException("Unable to initialize ManagerFactory: " + e.getMessage(), e);
        }
    }

    static public ManagerFactory getInstance() {
        if (instance == null) {
            synchronized (ManagerFactory.class) {
                if (instance == null) {
                    instance = new ManagerFactory();
                }
            }
        }
        return instance;
    }

    static private Context getContext() {
        return application.getApplicationContext();
    }

    public IStorageManager getStorageManager() {
        return storageManager;
    }

    public IEventManager getEventManager() {
        return eventManager;
    }

    public ICoreManager getCoreManager() {
        return coreManager;
    }

    public IMilStdRenderer getMilStdRenderer() {
        return milStdRenderer;
    }

    /**
     * If application compiled in the aar then class will be loaded locally, else it wil be loaded from the apk.
     *     compile (group: 'mil.army.sec.smartClient', name: 'emp3-android-sdk-core', version: "$version_emp3Android", ext: 'aar') { transitive = true }
     *     OR
     *     adb install emp3-android-sdk-core-version.apk
     * @param apkName
     * @param className
     * @return
     * @throws ClassNotFoundException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object dexLoadClass(String apkName, String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Log.d(TAG, "dexLoadClass: " + className);

        try {
            // Attempt to load locally, this will be the case for st5and-alone application
            Class<?> clazz = (Class<IMapInstance>) Class.forName(className, true, getClass().getClassLoader());
            return clazz.newInstance();
        } catch (ClassNotFoundException e) {
            Log.i(TAG, className + " is not part of the host application, will attempt to load from the standalone APK ");
        }

        if(null == dexLoader) {
            final File dexOutputDir = getContext().getDir("dex" + className, Context.MODE_PRIVATE);
            Log.d(TAG, "dexOutputDir: " + dexOutputDir);

            final String apkPath = getApkPath(apkName);
            Log.d(TAG, "apkPath: " + apkPath);

            if (apkPath != null) {
                dexLoader = new DexClassLoader(apkPath, dexOutputDir.getAbsolutePath(), CopySharedObjectFile.getNativeLibDir(getContext(), apkName), getContext().getClassLoader());
                return dexLoader.loadClass(className).newInstance();

            } else {
                throw new IllegalStateException("ERROR: " + apkName + " not found. Did you install it?");
            }
        } else {
            return dexLoader.loadClass(className).newInstance();
        }
    }

    private String getApkPath(String apkName) {
        Log.d(TAG, "getApkPath: " + apkName);

        for (ApplicationInfo app : getContext().getPackageManager().getInstalledApplications(0)) {
            if (!((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) {
                if (!((app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)) {
                    final String uri = app.sourceDir;
                    if (uri.contains(apkName)) {
                        return uri;
                    }
                }
            }
        }
        return null;
    }

}
