package mil.emp3.api.abstracts;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import dalvik.system.DexClassLoader;
import mil.emp3.api.About;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.core.storage.IClientMapRestoreData;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.api.view.utils.EmpResources;
import mil.emp3.mapengine.CopySharedObjectFile;
import mil.emp3.mapengine.MapEngineContext;
import mil.emp3.mapengine.interfaces.IEmpResources;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.view.R;

/**
 * This class is the base for the MapFragment and MapView classes.
 */
public class MapViewFragmentBase extends MirroredMap {
    private String TAG = MapViewFragmentBase.class.getSimpleName();

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final private IMilStdRenderer milStdRenderer = ManagerFactory.getInstance().getMilStdRenderer();

    // Following are specific to Android
    //private String engineClassName;
    //private String engineApkName;
    private String mapName;             // client map name, used to restore state after activity start (EMP3DataManager)
    private Context context;            // This is required for swapMapEngine
    private AttributeSet attributeSet;  // This is required for swapEngine

    public MapViewFragmentBase(IEmpPropertyList properties) {
        super(properties);
        if (properties.containsKey("TAG")) {
            this.TAG = properties.getStringValue("TAG");
        }
        this.context = properties.getContext("CONTEXT");
    }
/*
    public MapViewFragmentBase(String TAG) {
        this(TAG, null);
    }
    public MapViewFragmentBase(String TAG, Context context) {
        super(TAG);

        this.TAG = TAG;
        this.context = context;
    }
*/
    public String getEngineClassName() {
        return this.engineClassName;
    }

    public void setEngineClassName(String engineClassName) {
        this.engineClassName = engineClassName;
    }

    public String getEngineApkName() {
        return this.engineAPKName;
    }

    public void setEngineApkName(String engineApkName) {
        this.engineAPKName = engineApkName;
    }

    public String getMapName() {
        return mapName;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
/* Loading and starting Map Engine if this class is used inside MapFragment or MapView */

    /**
     * Copies so files from the Map Engine APK to the host application. Only the so for correct cpu
     * architecture are copied.
     *
     * @param context of the host application
     * @param dexPath dexPath of the map engine APK
     * @param engineAPKPackageName name of the main package in map engine APK
     */
    private void copySharedObjects(Context context, String dexPath, String engineAPKPackageName) {
        try {
            int currentapiVersion = android.os.Build.VERSION.SDK_INT;
            String abis[];
            if(currentapiVersion >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                Log.d(TAG, "We are LOLLIPOP or higher");
                abis = Build.SUPPORTED_ABIS;
            } else {
                Log.d(TAG, "We are less than LOLLIPOP");
                abis = new String[1];
                abis[0] = Build.CPU_ABI;
            }
            if(null == abis) {
                Log.e(TAG, "ERROR copySharedObjects No supported ABIs?");
                return;
            } else {
                for(int ii = 0; ii < abis.length; ii++) {
                    Log.d(TAG, "Supported ABI " + abis[ii]);
                }
            }

            ZipFile zipFile = new ZipFile(dexPath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            // Start with first abi in the supported abi as that is the preferred ABI

            boolean copied = false;
            for(int ii = 0; ii < abis.length && !copied; ii++) {
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    if (zipEntry.isDirectory()) {
                        continue;
                    }
                    String zipEntryName = zipEntry.getName();
                    if (zipEntryName.endsWith(".so")) {
                        Log.d(TAG, "SO File " + zipEntryName);
                    }
                    if (zipEntryName.endsWith(".so") && zipEntryName.contains(abis[ii])) {
                        final long lastModify = zipEntry.getTime();
                        if (lastModify == CopySharedObjectFile.getSoLastModifiedTime(context, zipEntryName)) {
                            // exist and no change
                            Log.d(TAG, "skip copying, the so lib is exist and not change: " + zipEntryName);
                            continue;
                        }
                        Log.d(TAG, "Copy " + zipEntry.getName());
                        CopySharedObjectFile csof = new CopySharedObjectFile(context, zipFile, zipEntry, lastModify, engineAPKPackageName);
                        csof.copy();
                        copied = true; // no need to check any more abis
                    }
                }
            }
            zipFile.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * These attributes should be assigned values in Fragment or View layout.
     * @param a
     */
/*
    @Override
    public void getStyledAttributes(TypedArray a) {
        super.getStyledAttributes(a);

        CharSequence map_engine_name = a.getText(R.styleable.EMP3Fragment_map_engine_name);
        if (map_engine_name != null) {
            Log.v(TAG, "map_engine_name : " + map_engine_name.toString());
            setEngineClassName(map_engine_name.toString());
        }

        CharSequence map_engine_apk_name = a.getText(R.styleable.EMP3Fragment_map_engine_apk_name);
        if (map_engine_apk_name != null) {
            Log.v(TAG, "map_engine_apk_name : " + map_engine_apk_name.toString());
            setEngineApkName(map_engine_apk_name.toString());
        }

        CharSequence map_name = a.getText(R.styleable.EMP3Fragment_map_name);
        if(map_name != null) {
            Log.v(TAG, "map_name : " + map_name.toString());
            setMapName(map_name.toString());
        }
    }
*/
    /**
     * Attempt to load the specified map engine class within the SystemClassLoader. If host application
     * had for some reason compiled in the specified map engine then that attempt will be successful. We do
     * not recommend that applications have a compile time dependency on the map engine.
     *
     * If the map engine is not locally available then an attempt is made to load it from another APK
     * that may host the map engine.
     *
     * @param context
     * @param attributeSet
     * @return
     */
    public View onCreateView(Context context, AttributeSet attributeSet) {
        Log.d(TAG, "onCreateView()");

        if(null == context) {
            Log.e(TAG, "onCreateView invoked with null context");
            return null;
        }
        this.context = context;
        this.attributeSet = attributeSet;

        onCreate(context);
        return buildView(false);
    }

    /**
     * Builds Map Engine View
     * @parm isSwap is it a swap or add of a view?
     */
    private View buildView(boolean isSwap) {
        Log.d(TAG, "buildView().getEngineClassName(): " + getEngineClassName());

        if (getEngineClassName() == null) {
            Log.e(TAG, "Application must specify the engine class name that needs to be loaded.");
            Log.e(TAG, "For example: app:map_engine_name=\"mil.emp3.worldwind.MapInstance\"");
            return null;
        }

        if ((mapName != null) && (0 != mapName.length())) {
            Log.d(TAG, "Map Name was specified in the layout: " + mapName);
            // If this is the first time for this client map then an entry is created.
            // If this is not the first time then entry is updated, specifically geoId/UUID
            this.setName(mapName);
            if (!isSwap) {
                Log.i(TAG, "This is not swapMapEngine call by application");
                try {
                    IClientMapRestoreData cmrd = storageManager.addMapNameToRestoreDataMapping(this);
                    Log.d(TAG, "setName " + cmrd.getEngineClassName() + " " + cmrd.getEngineApkName());

                    // If this is not the first time then we will find the engine name and start the engine.
                    if (null != cmrd.getEngineClassName()) {
                        // Looks like we are restarting the activity, so get the last engine information set by the application.
                        setEngineClassName(cmrd.getEngineClassName());
                        setEngineApkName(cmrd.getEngineApkName());
                    }
                } catch (EMP_Exception e) {
                    Log.e(TAG, "buildView ", e);
                }
            } else {
                Log.i(TAG, "This is swapMapEngine call by application");
            }
        }

        try {
            boolean loadedEngineClass = false;
            Class<IMapInstance> clazz = null;
            IMapInstance engine = null;
            try {

                // Attempt to load locally, this is not recommended way of deploying.
                clazz = (Class<IMapInstance>) Class.forName(engineClassName, true, getClass().getClassLoader());
                loadedEngineClass = true;
            } catch (ClassNotFoundException e) {
                Log.i(TAG, engineClassName + " is not part of the host application, will attempt to load from the standalone APK ");
            }

            if (!loadedEngineClass) {
                // Attempt to load from another APK.
                if (getEngineApkName() == null) {
                    Log.e(TAG, "Application must specify the engine APK package name that needs to be loaded.");
                    Log.e(TAG, "For example: app:map_engine_apk_name=\"mil.emp3.worldwind\"");
                    return null;
                }

                Log.d(TAG, "onCreateView()  map with engine: " + getEngineClassName() + " engine APK name " + getEngineApkName());

                // Search for the APK containing required class
                String uri = null;
                for (ApplicationInfo app : context.getPackageManager().getInstalledApplications(0)) {
                    if (!((app.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) {
                        if (!((app.flags & ApplicationInfo.FLAG_SYSTEM) == ApplicationInfo.FLAG_SYSTEM)) {
                            uri = app.sourceDir;
                            if (uri.contains(getEngineApkName() + "-")) { // After the '-' is the version number of APK.
                                Log.d(TAG, "Found APK " + uri);
                                break;
                            }
                            uri = null;
                        }
                    }
                }

                if (null != uri) {
                    File dexOutputDir = context.getDir("dex" + engineClassName , Context.MODE_PRIVATE);
                    Log.d(TAG, ".so files will be copied to " + CopySharedObjectFile.getNativeLibDir(context, getEngineApkName()));
                    copySharedObjects(context, uri, getEngineApkName());
                    Log.d(TAG, "Copy .so files complete ");

                    DexClassLoader dexLoader = new DexClassLoader(uri, dexOutputDir.getAbsolutePath(), CopySharedObjectFile.getNativeLibDir(context, getEngineApkName()), context.getClassLoader());
                    clazz = (Class<IMapInstance>) Class.forName(engineClassName, true, dexLoader);

                    Constructor<?> constructor = clazz.getConstructor(Context.class, AttributeSet.class, IEmpResources.class);

                    // Decorate the context class to override getResources and getAsset methods of the Context object
                    // This allows the map engine in the other APK to locate its resources.
                    MapEngineContext mapEngineContext = new MapEngineContext(context, getEngineApkName(), uri);
                    engine = (IMapInstance) constructor.newInstance(mapEngineContext, null, EmpResources.getInstance(context.getResources()));
                } else {
                    Log.e(TAG, "APK wasn't found for " + getEngineApkName() + " did you install it?");
                }
            } else {
                Constructor<?> constructor = clazz.getConstructor(Context.class, AttributeSet.class, IEmpResources.class);
                engine = (IMapInstance) constructor.newInstance(context, attributeSet, EmpResources.getInstance(context.getResources()));
            }

            if (null != engine) {
                try {
                    // swapMapInstance will create and add new mapping if one doesn't exist
                    // It will also redraw the features if required.
                    storageManager.swapMapInstance(this, engine);

                    // In future this information will be injected into Bitmap Cache. For now it is simply logged here.
                    // Bitmap Cache size will be based on this value in future.
                    ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                    Log.e(TAG, "Get Memory Class " + am.getMemoryClass());

                    milStdRenderer.setRendererCacheDir(this.context.getCacheDir().getAbsoluteFile().getAbsolutePath(), am.getMemoryClass());
                    engine.registerMilStdRenderer(milStdRenderer);

                    // Update the engine information in case we need to restore the map state on activity restart.
                    storageManager.updateMapNameToRestoreDataMapping(this, this.engineClassName, this.engineAPKName);

                } catch (EMP_Exception Ex) {
                    Log.d(TAG, "EmpCoreManager addMapInstance generated an exception.");
                }

                About.getVersionInformation(this);
                return engine.onCreateView(); // first time android lifecycle for underlying engine view*/ ;
            }

            About.getVersionInformation(this);
        } catch (NoSuchMethodException |
                ClassNotFoundException |
                java.lang.InstantiationException |
                IllegalAccessException |
                InvocationTargetException e) {
            Log.e(TAG, "buildView failed: " + e.getMessage(), e);
        }
        return null;
    }

    /**
     * Build the map view.
     * @param sEngineClassName
     * @param sAPKName
     * @return
     * @throws EMP_Exception
     */
    public View swapMapEngineInternal(String sEngineClassName, String sAPKName) throws EMP_Exception {

        if (null == context) {
            Log.e(TAG, "Context is null, You are probably invoking this on Map directly rather than MapFragment or MapView");
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "Invalid context for map swap.");
        }

        this.setEngineClassName(sEngineClassName);
        this.setEngineApkName(sAPKName);

        return buildView(true);
    }

    /**
     *
     * @param sEngineClassName
     * @param sAPKName
     * @param context
     * @return
     * @throws EMP_Exception
     */
    public View swapMapEngineInternal(String sEngineClassName, String sAPKName, Context context) throws EMP_Exception {

        if (null == context) {
            Log.e(TAG, "Context is null, You are probably invoking this on Map directly rather than MapFragment or MapView");
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "Invalid context for map swap.");
        }

        this.context = context;

        this.setEngineClassName(sEngineClassName);
        this.setEngineApkName(sAPKName);

        return buildView(true);
    }
}
