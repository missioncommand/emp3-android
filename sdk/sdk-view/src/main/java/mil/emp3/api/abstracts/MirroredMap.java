package mil.emp3.api.abstracts;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPoint;

import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.events.CameraEvent;
import mil.emp3.api.events.MapFeatureAddedEvent;
import mil.emp3.api.events.MapFeatureRemovedEvent;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IMapFeatureAddedEventListener;
import mil.emp3.api.listeners.IMapFeatureRemovedEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.api.utils.UUIDSet;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mirrorcache.api.IMirrorCacheStateChangeListener;
import mil.emp3.mirrorcache.api.IMirrorable;
import mil.emp3.mirrorcache.api.MirrorCache;
import mil.emp3.mirrorcache.mirrorables.MirrorableCamera;
import mil.emp3.mirrorcache.mirrorables.MirrorableMilStdSymbol;
import mil.emp3.mirrorcache.mirrorables.MirrorablePoint;


/**
 * <b>This class is intended for internal use only.</b><br/><br/>
 * This class serves to integrate {@link MirrorCache} features with IMap instances.
 */
public class MirroredMap extends Map {
    private String TAG = MirroredMap.class.getSimpleName();

    /**
     * A 'global camera' is a Camera instance
     * that is shared between all IMap instances having a mirrorCacheMode
     * value other than {@link MirrorCacheModeEnum#DISABLED}.
     */
    static final private String GLOBAL_CAMERA = "global_camera";

    private EventListenerHandle mapFeatureAddedEventHandle;
    private EventListenerHandle mapFeatureRemovedEventHandle;
    private EventListenerHandle mapStateChangeEventHandle;

    private InitializeCameraTask initCameraTask;

    public MirroredMap(IEmpPropertyList properties) {
        super(properties);
        if (properties.containsKey("TAG")) {
            this.TAG = properties.getStringValue("TAG");
        }
    }
/*
    public MirroredMap(String TAG, MirrorCacheModeEnum mode) {
        super(TAG, mode);
        this.TAG = TAG;
    }
*/
    public void onCreate(final Context context) {
        Log.d(TAG, "MirroredMap.onCreate");

        ensureOnUiThread();

        if (isEgressEnabled() || isIngressEnabled()) { // only if MirrorCache is not disabled

            Log.d(TAG, "Initializing the MirrorCache..");
            MirrorCache.getInstance().onCreate(context); // initialize the MirrorCache

            Log.d(TAG, "Registering for mapState events..");
            try {
                mapStateChangeEventHandle = this.addMapStateChangeEventListener(new MapStateListener(this));

            } catch (EMP_Exception ex) {
                Log.e(TAG, "addMapStateChangeEventListener.", ex);
            }
        }
    }

    public void onDestroy() {
        Log.d(TAG, "onDestroy");

        if (isEgressEnabled() || isIngressEnabled()) {
            MirrorCache.getInstance().onDestroy();

            if (initCameraTask != null) {
                initCameraTask.cancel(true);
            }

            if (mapStateChangeEventHandle != null) {
                this.removeEventListener(mapStateChangeEventHandle);
                mapStateChangeEventHandle = null;
            }
            if (mapFeatureAddedEventHandle != null) {
                this.removeEventListener(mapFeatureAddedEventHandle);
                mapFeatureAddedEventHandle = null;
            }
            if (mapFeatureRemovedEventHandle != null) {
                this.removeEventListener(mapFeatureRemovedEventHandle);
                mapFeatureRemovedEventHandle = null;
            }
        }
    }
/*
    public void getStyledAttributes(TypedArray a) {
        CharSequence mirrorCache_mode = a.getText(R.styleable.EMP3Fragment_mirrorCache_mode);
        if (mirrorCache_mode != null) {
            Log.d(TAG, "mirrorCache_mode : " + mirrorCache_mode.toString());
            setMirrorCacheMode(MirrorCacheModeEnum.valueOf(mirrorCache_mode.toString()));
        }
    }
*/
    static private void ensureOnUiThread() {
        if (!Looper.getMainLooper().equals(Looper.myLooper())) {
            throw new IllegalStateException("ERROR: This is NOT the UI thread.");
        }
    }

    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

    /**
     * This task will try to retrieve the GLOBAL_CAMERA from the mirrorcache.
     * If it does not exist, it will create a new camera and populate the mirrorcache with it.
     * After executing, the task will set the Map's camera instance to the GLOBAL_CAMERA. If
     * EGRESS is enabled it will register to receive camera events and spawn UpdateMirrorCacheTask tasks.
     */
    static private class InitializeCameraTask extends AsyncTask<Void, Void, MirrorableCamera> {
        private String TAG = InitializeCameraTask.class.getSimpleName();

        final private IMap map;

        private InitializeCameraTask(IMap map) {
            this.map = map;
        }

        @Override
        protected MirrorableCamera doInBackground(Void... params) {
            Log.d(TAG, "doInBackground");

            /*
             * Attempt to retrieve an existing GLOBAL_CAMERA. Create new instance if necessary.
             */
            MirrorableCamera globalCamera = (MirrorableCamera) MirrorCache.getInstance().get(GLOBAL_CAMERA);
            Log.d(TAG, "globalCamera: " + globalCamera);

            if (globalCamera == null) { // create camera and send to MC as the globalCamera
                globalCamera = new MirrorableCamera();
                globalCamera.setAltitude(1e7);

                if (!isCancelled()) {
                    MirrorCache.getInstance().update(globalCamera, null, GLOBAL_CAMERA);
                }
            }

            Log.d(TAG, "globalCamera.getGeoId(): " + globalCamera.getGeoId());
            Log.d(TAG, "globalCamera.getMirrorKey(): " + globalCamera.getMirrorKey());

            return globalCamera;
        }

        @Override
        protected void onCancelled(MirrorableCamera mirrorableCamera) {
            Log.d(TAG, "onCancelled");
        }

        @Override
        protected void onPostExecute(final MirrorableCamera globalCamera) {
            Log.d(TAG, "onPostExecute");

            try {
                /*
                 * Configure Map to use GLOBAL_CAMERA instance
                 */
                map.setCamera(globalCamera, false);

                /*
                 * Register to receive local camera and feature-add events so that we may push updates.
                 */
                if (map.isEgressEnabled()) {
                    Log.d(TAG, "Registering for globalCamera events..");
                    globalCamera.addCameraEventListener(new ICameraEventListener() {
                        @Override
                        public void onEvent(CameraEvent ce) {
                            new UpdateMirrorCacheTask().execute(new IdentifiedMirrorable(globalCamera, GLOBAL_CAMERA));
                        }
                    });
                }

            } catch (EMP_Exception e) {
                Log.e(TAG, "ERROR: " + e.getMessage(), e);
            }
        }
    }

    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

    /**
     * This task will ensure that all outbound messages for the mirrorcache
     * are processed in a background thread.
     */
    static private class UpdateMirrorCacheTask extends AsyncTask<IdentifiedMirrorable, Void, Void> {
        private String TAG = UpdateMirrorCacheTask.class.getSimpleName();

        @Override
        protected Void doInBackground(final IdentifiedMirrorable... features) {
            Log.d(TAG, "doInBackground");

            for (IdentifiedMirrorable feature : features) {
                MirrorCache.getInstance().update(feature.mirrorable, null, feature.geoId);
            }
            return null;
        }
    }

    /**
     * This task will ensure that all outbound messages for the mirrorcache
     * are processed in a background thread.
     */
    static private class DeleteMirrorCacheTask extends AsyncTask<IdentifiedMirrorable, Void, Void> {
        private String TAG = DeleteMirrorCacheTask.class.getSimpleName();

        @Override
        protected Void doInBackground(final IdentifiedMirrorable... features) {
            Log.d(TAG, "doInBackground");

            for (IdentifiedMirrorable feature : features) {
                MirrorCache.getInstance().delete(feature.geoId);
            }
            return null;
        }
    }

    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

    /**
     * Utility bean to encapsulate an IMirrorable with a geoId
     */
    static private class IdentifiedMirrorable {
        final private IMirrorable mirrorable;
        final private  String geoId;

        private IdentifiedMirrorable(IMirrorable mirrorable, String geoId) {
            this.mirrorable = mirrorable;
            this.geoId      = geoId;
        }
    }

    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

    /**
     * This class is responsible for processing specific MapFeatureEvents.
     * Upon receipt of a supported feature type, an UpdateMirrorCacheTask or
     * DeleteMirrorCacheTask will be spawned.
     */
    static private class MapFeatureHandler {
        static private String TAG = MapFeatureHandler.class.getSimpleName();

        public void onEvent(MapFeatureAddedEvent event) {
            ensureOnUiThread();
            try {
                if (event.getFeature() instanceof IGeoMilSymbol) {
                    final MirrorableMilStdSymbol feature = new MirrorableMilStdSymbol((IGeoMilSymbol) event.getFeature());
                    new UpdateMirrorCacheTask().execute(new IdentifiedMirrorable(feature, feature.getGeoId().toString()));

                } else if (event.getFeature() instanceof IGeoPoint) {
                    final MirrorablePoint feature = new MirrorablePoint((IGeoPoint) event.getFeature());
                    new UpdateMirrorCacheTask().execute(new IdentifiedMirrorable(feature, feature.getGeoId().toString()));

                } else {
                    Log.w(TAG, "Unsupported MirrorCache feature type added: " + event.getFeature());
                }

            } catch (EMP_Exception e) {
                Log.e(TAG, "ERROR: " + e.getMessage(), e);
            }
        }

        public void onEvent(MapFeatureRemovedEvent event) {
            ensureOnUiThread();
            try {
                if (event.getFeature() instanceof IGeoMilSymbol) {
                    final MirrorableMilStdSymbol feature = new MirrorableMilStdSymbol((IGeoMilSymbol) event.getFeature());
                    new DeleteMirrorCacheTask().execute(new IdentifiedMirrorable(feature, feature.getGeoId().toString()));

                } else if (event.getFeature() instanceof IGeoPoint) {
                    final MirrorablePoint feature = new MirrorablePoint((IGeoPoint) event.getFeature());
                    new DeleteMirrorCacheTask().execute(new IdentifiedMirrorable(feature, feature.getGeoId().toString()));

                } else {
                    Log.w(TAG, "Unsupported MirrorCache feature type added: " + event.getFeature());
                }

            } catch (EMP_Exception e) {
                Log.e(TAG, "ERROR: " + e.getMessage(), e);
            }
        }
    }

    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

    /**
     * This class is responsible for responding to MirrorCache update events.
     * The map instance is updated for each supported Mirrorable type received.
     */
    static private class MirrorCacheStateListener implements IMirrorCacheStateChangeListener {
        static private String TAG = MirrorCacheStateListener.class.getSimpleName();

        final private IMap map;
        final private Handler handler;
        final private IStorageManager storageManager;

        private MirrorCacheStateListener(IMap map) {
            this.map = map;
            this.handler = new Handler(Looper.getMainLooper());
            this.storageManager = ManagerFactory.getInstance().getStorageManager();
        }

        @Override
        public void onUpdate(final IMirrorable o) {
            handler.post(new Runnable() { // force to run on UI thread
                @Override
                public void run() {
                    if (o instanceof MirrorableCamera) {
                        try {
                            map.setCamera((MirrorableCamera) o, false);
                        } catch (EMP_Exception e) {
                            Log.e(TAG, "ERROR: " + e.getMessage(), e);
                        }

                    } else if (o instanceof MirrorableMilStdSymbol) {
                        final FeatureVisibilityList features = new FeatureVisibilityList();
                        features.add(new FeatureVisibility((MirrorableMilStdSymbol) o, true));

                        final IMapInstance engine = storageManager.getMapInstance(map);
                        engine.addFeatures(features);

                    } else if (o instanceof MirrorablePoint) {
                        final FeatureVisibilityList features = new FeatureVisibilityList();
                        features.add(new FeatureVisibility((MirrorablePoint) o, true));

                        final IMapInstance engine = storageManager.getMapInstance(map);
                        engine.addFeatures(features);
                    }
                }
            });
        }
        @Override
        public void onMirrorred() {
        }

        @Override
        public void onDelete(final IMirrorable o) {
            handler.post(new Runnable() { // force to run on UI thread
                @Override
                public void run() {
                    if (o instanceof MirrorableMilStdSymbol) {
                        final IUUIDSet features = new UUIDSet();
                        features.add(((MirrorableMilStdSymbol) o).getGeoId());

                        final IMapInstance engine = storageManager.getMapInstance(map);
                        engine.removeFeatures(features);

                    } else if (o instanceof MirrorablePoint) {
                        final IUUIDSet features = new UUIDSet();
                        features.add(((MirrorablePoint) o).getGeoId());

                        final IMapInstance engine = storageManager.getMapInstance(map);
                        engine.removeFeatures(features);
                    }
                }
            });
        }
    }

    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //
    // -+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+- //

    /**
     * This class listens for MapStateChangeEvents, specifically MapStateEnum.MAP_READY.
     * Upon receiving a MAP_READY event, this class will instantiate and execute an
     * InitializeCameraTask instance, register a MapFeatureAddedEventListener if EGRESS
     * is enabled, and a register a MirrorCacheStateListener if INGRESS is enabled.
     */
    static private class MapStateListener implements IMapStateChangeEventListener {
        private String TAG = MapStateListener.class.getSimpleName();

        final private MirroredMap map;
        private MirrorCacheStateListener stateListener;

        private MapStateListener(MirroredMap map) {
            this.map = map;
        }

        @Override
        public void onEvent(MapStateChangeEvent event) {
            Log.d(TAG, "onEvent(MapStateChangeEvent): " + event.getNewState());
            ensureOnUiThread();
            if (event.getNewState() == MapStateEnum.MAP_READY) {

                map.initCameraTask = new InitializeCameraTask(map);
                map.initCameraTask.execute(); // get and set the globalCamera; register camera listener

                if (map.isEgressEnabled()) {
                    Log.d(TAG, "Registering for mapFeature events..");
                    try {
                        final MapFeatureHandler mapFeatureLHandler = new MapFeatureHandler();

                        map.mapFeatureAddedEventHandle = map.addMapFeatureAddedEventListener(new IMapFeatureAddedEventListener() {
                            @Override
                            public void onEvent(MapFeatureAddedEvent event) {
                                mapFeatureLHandler.onEvent(event);
                            }
                        });
                        map.mapFeatureRemovedEventHandle = map.addMapFeatureRemovedEventListener(new IMapFeatureRemovedEventListener() {
                            @Override
                            public void onEvent(MapFeatureRemovedEvent event) {
                                mapFeatureLHandler.onEvent(event);
                            }
                        });

                    } catch (EMP_Exception ex) {
                        Log.e(TAG, "addMapFeatureAddedEventListener.", ex);
                    }
                }

                if (map.isIngressEnabled()) {
                    Log.d(TAG, "Registering for MirrorCache events..");
                    MirrorCache.getInstance().addStateChangeListener(stateListener = new MirrorCacheStateListener(map));
                }

            } else if (event.getNewState() == MapStateEnum.SHUTDOWN_IN_PROGRESS) {
                if (stateListener != null) { //cleanup
                    MirrorCache.getInstance().removeStateChangeListener(stateListener);
                }
            }
        }
    }
}
