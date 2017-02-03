package mil.emp3.wallpaper;

import android.app.ActivityManager;
import android.content.Context;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

import mil.emp3.api.abstracts.MirroredMap;
import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.enums.Property;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.api.view.utils.EmpResources;
import mil.emp3.worldwind.MapInstance;

/**
 * This class allows for EMP3 Map engine as wall paper. It is currently hard code for worldwind android. It will need to be refactored
 * so that it can be used with other engines in future. It directly refers to the worldwind MapInstance class, that also needs to change.
 * I have created JIRA ticket: EMP-2369.
 */
public class EMP3WallpaperService extends WallpaperService {
    private static final String TAG = EMP3WallpaperService.class.getSimpleName();

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final private IMilStdRenderer milStdRenderer = ManagerFactory.getInstance().getMilStdRenderer();

    private MirroredMap map;

    public EMP3WallpaperService() {
        IEmpPropertyList propList = new EmpPropertyList();

        propList.put("TAG", TAG);
        propList.put(Property.MIRROR_CACHE_MODE.getValue(), MirrorCacheModeEnum.INGRESS);
        map = new MirroredMap(propList);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");
        map.onCreate(getApplicationContext());
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        map.onDestroy();
        super.onDestroy();
    }

    @Override
    public Engine onCreateEngine() {
        Log.d(TAG, "onCreateEngine");
        return new WallpaperEngine(getApplicationContext());
    }


    public class WallpaperEngine extends Engine implements SurfaceHolder.Callback {
        private static final String TAG = "WallpaperEngine";

        private Context     context;
        private MapInstance mapEngine;

        public WallpaperEngine(Context context) {
            this.context = context;
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            Log.d(TAG, "onCreate [surfaceHolder=" + surfaceHolder + "]");

            surfaceHolder.addCallback(this);

            try {
                setTouchEventsEnabled(true);

                // Supply the surfaceHolder that map engine should use. That is the hook between wall paper and the map engine.
                mapEngine = new MapInstance(context, EmpResources.getInstance(context.getResources()), surfaceHolder);
                storageManager.swapMapInstance(map, mapEngine);

                // In future this information will be injected into Bitmap Cache. For now it is simply logged here.
                // Bitmap Cache size will be based on this value in future.
                ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
                Log.e(TAG, "Get Memory Class " + am.getMemoryClass());

                milStdRenderer.setRendererCacheDir(this.context.getCacheDir().getAbsoluteFile().getAbsolutePath(), am.getMemoryClass());
                mapEngine.registerMilStdRenderer(milStdRenderer);

                mapEngine.onCreateView();

                // set a default camera
                final ICamera camera = map.getCamera();
                camera.setAltitude(1e7);
                map.setCamera(camera, false);

                map.setFarDistanceThreshold(Double.parseDouble(getResources().getString(R.string.ftdValue)));
                //map.setMidDistanceThreshold(Double.parseDouble(getResources().getString(R.string.mtdValue)));

            } catch (Exception e) {
                Log.e(TAG, "ERROR: " + e.getMessage(), e);
            }
        }

        @Override
        public void onDestroy() {
            Log.d(TAG, "onDestroy");
            setTouchEventsEnabled(false);

            mapEngine.onDestroy();
            //TODO CoreManager.getInstance().removeMapInstance(map, mapEngine);
            super.onDestroy();
        }



        @Override
        public void onVisibilityChanged(boolean visible) {
            if (visible) {
                mapEngine.onResume();
            } else {
                mapEngine.onPause();
            }
        }

        @Override
        public void onTouchEvent(final MotionEvent event) {
            mapEngine.getMapInstanceAndroidView().dispatchTouchEvent(event);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mapEngine.getMapInstanceAndroidView().layout(0, 0, width, height);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }
}
