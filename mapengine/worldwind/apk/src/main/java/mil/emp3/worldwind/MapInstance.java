package mil.emp3.worldwind;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

import org.cmapi.primitives.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import gov.nasa.worldwind.*;
import gov.nasa.worldwind.geom.*;
import gov.nasa.worldwind.layer.*;

import gov.nasa.worldwind.ogc.*;
import gov.nasa.worldwind.ogc.gpkg.GeoPackage;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderResourceCache;
import gov.nasa.worldwind.shape.OmnidirectionalSightline;
import gov.nasa.worldwind.shape.ShapeAttributes;
import gov.nasa.worldwind.shape.SurfaceImage;
import gov.nasa.worldwind.util.Logger;
import mil.emp3.api.enums.*;
import mil.emp3.api.interfaces.*;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.FontUtilities;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.mapengine.abstracts.CoreMapInstance;
import mil.emp3.mapengine.api.*;
import mil.emp3.mapengine.interfaces.*;
import mil.emp3.worldwind.controller.PickNavigateController;
import mil.emp3.worldwind.feature.FeatureRenderableMapping;
import mil.emp3.worldwind.feature.support.MilStd2525LevelOfDetailSelector;
import mil.emp3.worldwind.layer.*;
import mil.emp3.worldwind.utils.*;

public class MapInstance extends CoreMapInstance {
    private static final String TAG = MapInstance.class.getSimpleName();
    private static final int BRIGHTNESS_BITMAP_WIDTH_HEIGHT = 4;
    private static final int HATCH_SIZE = (Double.valueOf(0.178571 * Resources.getSystem().getDisplayMetrics().densityDpi)).intValue();

    private Context context;
    private gov.nasa.worldwind.WorldWindow ww;
    private SurfaceHolder mySurfaceHolder;                 // This is required for WallPaper Service
    private final IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    private PickNavigateController mapController;
    private Emp3NavigationListener oMapViewController;

    private double FAR_THRESHOLD = 30000;
    private double MID_THRESHOLD = 10000;

    private Map<UUID, mil.emp3.worldwind.feature.FeatureRenderableMapping> featureHash;
    private Set<UUID> dirtyOnMapMove;
    private RenderableLayer imageLayer;
    private Map<UUID, SurfaceImage> surfaceLayerHash;
    private Map<UUID, Layer> layerHash;
    private RenderableLayer brightnessLayer;

    private boolean brightnessProcessingPosted = false;
    private int backgroundBrightness = 50;
    private android.graphics.Bitmap blackBitmap;
    private android.graphics.Bitmap whiteBitmap;
    private EmpSurfaceImage blackSurfaceImage;
    private EmpSurfaceImage whiteSurfaceImage;
    private ImageSource blackImageSource;
    private ImageSource whiteImageSource;
    private android.graphics.Bitmap hatchBitmap;
    private android.graphics.Bitmap crossHatchBitmap;
    private ImageSource hatchImageSource;
    private ImageSource crossHatchImageSource;
    private MapGridLayer gridLayer = null;
    private KMLServiceLayer kmlServiceLayer = null; // KML Service created KML Features are added her.

    private final Map<FeatureTypeEnum, EmpLayer> empLayerMap = new HashMap<>();

    private MiniMapWorldWindow miniMap = null;

    // If you ever need to provide a trace of render cache to NASA then following constants and handler are important.
    // Handler is kicked of by the last line in initMapEngine which remains commented until required.

    protected static final int PRINT_METRICS = 1;
    protected static final int PRINT_METRICS_DELAY = 3000;

    private Handler logHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == PRINT_METRICS) {
                return printMetrics();
            } else {
                return false;
            }
        }
    });

    /**
     * This handler is used to explicitly run posted messages on the main UI thread.
     *
     * This is required because in some situations, messages posted via
     * {@link android.view.View#post(Runnable)} will never get executed. For example,
     * as a result of the Wallpaper Service not attaching to a {@link android.view.Window},
     * no handler will be made available to satisfy posted messages.
     *
     * @see <a href="http://stackoverflow.com/a/10457064">Runnable is posted successfully but not run</a>
     */
    private Handler handler;

    public MapInstance(Context context, View parentView, IEmpResources empResources) {
        super(TAG, empResources);
        Log.i(TAG, "constructor Context View  IEmpImageInfo");
        initMapEngine(context, null);
    }

    public MapInstance(Context context, IEmpResources empResources) {
        super(TAG, empResources);
        Log.i(TAG, "constructor Context IEmpImageInfo");
        initMapEngine(context, null);
    }

    /**
     * This constructor was specifically created for WallPaper service to get the surfaceHolder
     * @param context
     * @param empResources
     * @param surfaceHolder
     */
    public MapInstance(Context context, IEmpResources empResources, SurfaceHolder surfaceHolder) {
        super(TAG, empResources);
        Log.i(TAG, "constructor Context IEmpImageInfo SurfaceHolder");
        mySurfaceHolder = surfaceHolder;
        initMapEngine(context, null);
    }

    public MapInstance(Context context, AttributeSet attrs, IEmpResources empResources) {
        super(TAG, empResources);
        Log.i(TAG, "constructor Context AttributeSet IEmpImageInfo");
        initMapEngine(context, attrs);
    }

    /**
     * A developer may want to either extend MapInstance or the MapInstance.WorldWindow inner class. If you are extending MapInstance then
     * be sure to invoke super.initMapEngine and override one of getWW methods that takes non-empty arguments. In future we may want to allow
     * the user to create a custom feature but that is currently not supported. One approach would be to create a FeatureType CUSTOM and then
     * invoke methods from user's Feature object to render the feature. User's Feature object would need to implement IFeature and ICustomFeature
     * (doesn't exist yet). Developer should also create a separate layer for the custom features and add it by fetching Layer List from the MapInstance
     * getEmpLayerMap. What is being done under EMP-3024 is a first step and more work like allowing developers to Capabilities
     * is still to be done.
     * @param context
     * @param attrs
     */
    protected void initMapEngine(Context context, AttributeSet attrs) {
        this.context = context;

        // Create the World Window and set it as the content view for this activity.
        Log.i(TAG, "initMapEngine()");
        this.oEngineCapabilities = new Capabilities(
            new FeatureTypeEnum[] {
                    FeatureTypeEnum.GEO_MIL_SYMBOL,
                    FeatureTypeEnum.GEO_POINT,
                    FeatureTypeEnum.GEO_PATH,
                    FeatureTypeEnum.GEO_POLYGON,
                    FeatureTypeEnum.GEO_TEXT,
                    FeatureTypeEnum.GEO_CIRCLE,
                    FeatureTypeEnum.GEO_ELLIPSE,
                    FeatureTypeEnum.GEO_RECTANGLE,
                    FeatureTypeEnum.GEO_SQUARE,
                    FeatureTypeEnum.KML
            },
            new WMSVersionEnum[] {
                    WMSVersionEnum.VERSION_1_1,
                    WMSVersionEnum.VERSION_1_1_1,
                    WMSVersionEnum.VERSION_1_3,
                    WMSVersionEnum.VERSION_1_3_0
            },
                new WMTSVersionEnum[] {
                        WMTSVersionEnum.VERSION_1_0_0}
        );

        ww = null == attrs ? getWW(context) : getWW(context, attrs);
        /*
           Here is some explanation from NASA for the following two lines of code:
           Thanks for the logcat output. After reviewing the latest EMP3 code, it appears that the LevelOfDetailSelector implementation is correct and
           optimal. World Wind's default RenderCache capacity appears to be too large for this test machine’s capabililties, and we’ll need to decrease it.

           We encountered the behavior you’re currently encountering during our stress tests, and the solution was to decrease RenderCache capacity.
           The Android OS itself uses a very large portion of system memory, and is quite aggressive in terminating apps that use too much of the small
           remaining amount. The RenderCache is designed to fill to capacity before it frees any memory, and when that capacity is too large Android will
           terminate the app. In the case of this test machine the capacity is set to 192MB but the process terminates before the cache contents reach
           150MB. We’ll be revisiting our choice of default RenderCache capacity, and will likely make it scale dynamically scale using Android’s
           onTrimMemory/onLowMemory messages.

           Until we incorporate these changes you’ll need to decrease the RenderCache capacity in the EMP3 codebase. Immediately after constructing a WorldWindow, set a custom RenderCache capacity as follows:
           WorldWindow wwd = // reference to your WorldWindow
           RenderResourceCache customCache = new RenderResourceCache(96 * 1024 * 1024); // 96 megabyte render cache capacity, for example
           wwd.setRenderResourceCache(customCache);
         */
        RenderResourceCache customCache = new RenderResourceCache(96 * 1024 * 1024); // 96 megabyte render cache capacity, for example
        ww.setRenderResourceCache(customCache);

        this.oMapViewController = new Emp3NavigationListener(this, ww);
        this.handler = new Handler(Looper.getMainLooper());
        this.layerHash = new HashMap<>();
        this.featureHash = new HashMap<>(); // zoom operation and re-rendering of Tactical Graphics will otherwise crash
        this.dirtyOnMapMove = new HashSet<>();
        /*
         * Layers to be rendered, in order.
         */
        EmpLayer empLayer;

        ww.getLayers().addLayer(new BackgroundLayer());

        this.imageLayer = new RenderableLayer();
        ww.getLayers().addLayer(this.imageLayer);

        this.kmlServiceLayer = new KMLServiceLayer(this); // Is this the correct order?
        ww.getLayers().addLayer(kmlServiceLayer);

        this.brightnessLayer = new RenderableLayer();
        ww.getLayers().addLayer(this.brightnessLayer);

        this.surfaceLayerHash = new HashMap<>();

        this.gridLayer = new MapGridLayer("Grid Line Layer", this);
        ww.getLayers().addLayer(this.gridLayer);

        empLayer = new PolygonLayer(this);
        ww.getLayers().addLayer(empLayer);
        empLayerMap.put(FeatureTypeEnum.GEO_POLYGON, empLayer);

        empLayer = new RenderedFeatureLayer(this);
        ww.getLayers().addLayer(empLayer);
        empLayerMap.put(FeatureTypeEnum.GEO_ELLIPSE, empLayer);
        empLayerMap.put(FeatureTypeEnum.GEO_CIRCLE, empLayer);
        empLayerMap.put(FeatureTypeEnum.GEO_RECTANGLE, empLayer);
        empLayerMap.put(FeatureTypeEnum.GEO_SQUARE, empLayer);

        empLayer = new PathLayer(this);
        ww.getLayers().addLayer(empLayer);
        empLayerMap.put(FeatureTypeEnum.GEO_PATH, empLayer);

        empLayer = new KMLLayer(this);
        ww.getLayers().addLayer(empLayer);
        empLayerMap.put(FeatureTypeEnum.KML, empLayer);

        empLayer = new GeoJSONLayer(this);
        ww.getLayers().addLayer(empLayer);
        empLayerMap.put(FeatureTypeEnum.GEOJSON, empLayer);

        empLayer = new MilStdSymbolLayer(this);
        ww.getLayers().addLayer(empLayer);
        empLayerMap.put(FeatureTypeEnum.GEO_MIL_SYMBOL, empLayer);

        empLayer = new TextLayer(this);
        ww.getLayers().addLayer(empLayer);
        empLayerMap.put(FeatureTypeEnum.GEO_TEXT, empLayer);

        empLayer = new IconLayer(this);
        ww.getLayers().addLayer(empLayer);
        empLayerMap.put(FeatureTypeEnum.GEO_POINT, empLayer);


        createBrightnessBitmaps();
        // Uncomment the following line if you want to start tracing the render cache status.
        // This should always be the last line of code in this method.
        // logHandler.sendEmptyMessageDelayed(PRINT_METRICS, PRINT_METRICS_DELAY);
    }

    //
    // Following four methods allow the developer to extends the WorldWindow class and use it instead of
    //  using the inner WorldWindow class defined here. EMP-2857
    //

    public gov.nasa.worldwind.WorldWindow getWW() {
        return ww;
    }

    protected gov.nasa.worldwind.WorldWindow getWW(Context context) {
        return new WorldWindow(context);
    }

    protected gov.nasa.worldwind.WorldWindow getWW(Context context, AttributeSet attrs) {
        return new WorldWindow(context, attrs);
    }

    protected void onDetachedFromWindow() {
        ((WorldWindow) ww).onDetachedFromWindow();
        if (null != this.miniMap) {
            ((WorldWindow) this.miniMap).onDetachedFromWindow();
        }
    }

    // Above four methods created for MapInstance/WorldWindow extensibility EMP-2857
    public Context getContext() {
        return this.context;
    }

    @Override
    public View getMapInstanceAndroidView() {
        return ww;
    }

    @Override
    public void onDestroy() {

        Log.i(TAG, "onDestroy");
        UUID oUniqueId;
        this.generateStateChangeEvent(MapStateEnum.SHUTDOWN_IN_PROGRESS);
        this.oMapViewController.Destroy();

        this.dirtyOnMapMove.clear();

        ww.getLayers().clearLayers();
        layerHash.clear();

        while (!this.featureHash.isEmpty()) {
            oUniqueId = (UUID) this.featureHash.keySet().toArray()[0];
            this.removeFeature(oUniqueId, null);
        }


        this.generateStateChangeEvent(MapStateEnum.SHUTDOWN);

        onDetachedFromWindow();
        Log.i(TAG, "onDestroy complete");
    }

    @Override
    public void onResume() {
        ww.onResume();
        if (null != this.miniMap) {
            this.miniMap.onResume();
        }
    }

    @Override
    public void onPause() {
        ww.onPause();
        if (null != this.miniMap) {
            this.miniMap.onPause();
        }
    }

    @Override
    public View onCreateView() {
        this.mapController = new PickNavigateController(this, ww);
        ww.setWorldWindowController(this.mapController);

        final MapInstance oThis = this;

        ww.addNavigatorListener(this.oMapViewController);


        // We post it to the UI thread after 500 msec to allow the app to finish its startup UI stuff.
        /*
         * SEE HANDLER NOTES ABOVE.
         */
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // MAP is ready core and engine cameras are synced, so calculate the initial bounds.
                BoundsGeneration.initialize(MapInstance.this);

                MapInstance.this.checkForMapReady();
            }
        }, 500);

        return ww;
    }

    private void checkForMapReady() {
        IEmpBoundingArea bounds = BoundsGeneration.getBounds(this);

        if (null == bounds) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    MapInstance.this.checkForMapReady();
                }
            }, 500);
        } else {
            Log.d(TAG, "Map Ready firing.");
            MapInstance.this.generateStateChangeEvent(MapStateEnum.MAP_READY);
        }
    }

    @Override
    public IMapEngineProperties getMapEngineProperties() {
        return new MapEngineProperties();
    }

    /**
     * NOTE : This method is going to redraw what ever is in dirtyOnMapMove list. Based EMP-3065
     * this now included RenderableFeatures (Rectangle, Square, Circle and Ellipse).
     */
    public void reRenderMPTacticalGraphics() {
        boolean issueRedraw = false;

        for (Iterator<UUID> geoIds = dirtyOnMapMove.iterator(); geoIds.hasNext();) {
            FeatureRenderableMapping mapping = featureHash.get(geoIds.next());
            mapping.setDirty(true);
            issueRedraw = true;
        }
        if (issueRedraw) {
            if (!SystemUtils.isCurrentThreadUIThread()) {
                /*
                 * SEE HANDLER NOTES ABOVE.
                 */
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ww.requestRedraw();
                    }
                });
            } else {
                ww.requestRedraw();
            }
        }
    }

    public IconSizeEnum getIconSizeSetting() {
        return storageManager.getIconSize(this);
    }

    private void plotFeature(IFeature feature, boolean bVisible) {
        getRenderableLayer(feature).plot(feature, bVisible);
    }

    /**
     * Assumption here is that core software (Storage Manager has taken care of thread safety. So we will not need a lock
     * here. View is updated on UI thread from renderableList and removeRenderableList
     *
     * @param features
     */
    @Override
    public void addFeatures(final FeatureVisibilityList features, final Object userContext) {
        // Update the view on UI thread.
        if (!SystemUtils.isCurrentThreadUIThread()) {
            /*
             * SEE HANDLER NOTES ABOVE.
             */
            handler.post(new Runnable() {
                @Override
                public void run() {
                    for (FeatureVisibility featureVisibility: features) {
                        //if (null != featureVisibility.feature.getStrokeStyle()) {
                        //    Log.d(TAG, "Add Feature Stroke Width: " + featureVisibility.feature.getStrokeStyle().getStrokeWidth());
                        //}
                        MapInstance.this.plotFeature(featureVisibility.feature, featureVisibility.visible);
                        MapInstance.this.generateFeatureAddedEvent(featureVisibility.feature, userContext);
                    }

                    ww.requestRedraw();
                }
            });
        } else {
            for (FeatureVisibility featureVisibility: features) {
                MapInstance.this.plotFeature(featureVisibility.feature, featureVisibility.visible);
                generateFeatureAddedEvent(featureVisibility.feature, userContext);
            }

            ww.requestRedraw();
        }
    }

    private void removeFeature(UUID uniqueId, Object userContext) {
        FeatureRenderableMapping oWrapper = this.featureHash.get(uniqueId);
        if (oWrapper != null) {
            dirtyOnMapMove.remove(uniqueId);

            if (oWrapper != null) {
                IFeature feature = oWrapper.getFeature();
                getRenderableLayer(feature).removeFeatureRenderables(uniqueId);
                generateFeatureRemovedEvent(feature, userContext);
                this.featureHash.remove(uniqueId);
            }
        }
    }

    /**
     * Assumption here is that core software Storage Manager has taken care of thread safety. So we will not need a lock
     * here. View is updated from removeRenderableList
     *
     * @param features
     */
    @Override
    public void removeFeatures(final IUUIDSet features, Object userContext) {
        for (java.util.UUID uniqueId: features) {
            this.removeFeature(uniqueId, userContext);
        }

        // Update the view on UI thread.
        if (!SystemUtils.isCurrentThreadUIThread()) {
            /*
             * SEE HANDLER NOTES ABOVE.
             */
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ww.requestRedraw();
                }
            });
        } else {
            ww.requestRedraw();
        }
    }

    private void addWCSService(final IWCS wcs) {

        // Specify the bounding sector - provided by the WCS
//        Sector coverageSector = Sector.fromDegrees(-90.0, -180.0, 180.0, 360.0);
        // Specify the number of levels to match data resolution
//        int numberOfLevels = 12;
        // Specify the version 1.0.0 WCS address
        // Create an elevation coverage from a version 1.0.0 WCS
        Wcs201ElevationCoverage aster = new Wcs201ElevationCoverage(wcs.getServiceURL(), wcs.getCoverageName());

        // Remove any existing coverages from the Globe
        ww.getGlobe().getElevationModel().clearCoverages();

        // Add the coverage to the Globes elevation model
        ww.getGlobe().getElevationModel().addCoverage(aster);
    }

    private void addWMSService(final IWMS wms, IMapServiceResult result) {
        // Create a layer factory, World Wind's general component for creating layers
        // from complex data sources.
        LayerFactory layerFactory = new LayerFactory();
        // Create an OGC Web Map Service (WMS) layer to display the
        // surface temperature layer from NASA's Near Earth Observations WMS.
        layerFactory.createFromWms(
            wms.getURL().toString(), // WMS server URL
            wms.getLayers(),  // WMS layer names
            new LayerFactory.Callback() {
                @Override
                public void creationSucceeded(LayerFactory factory, Layer layer) {
                    // try to insert layer before the image layer
                    final int insertIdx = ww.getLayers().indexOfLayer(MapInstance.this.imageLayer);
                    if (insertIdx != -1) {
                        ww.getLayers().addLayer(insertIdx, layer);
                        MapInstance.this.layerHash.put(wms.getGeoId(), layer);
                        layer.putUserProperty(wms.getGeoId(), "WMS");
                        Log.i(TAG, "WMS layer creation succeeded " + wms.getGeoId());
                        if (result != null) {
                            result.result(true, wms.getGeoId(), null);
                        }
                        // redraw should be automatic
                    } else {
                        if (result != null) {
                            result.result(false, null, new IllegalStateException("ERROR: unable to locate tactical graphic layer."));
                        }
                    }
                }

                @Override
                public void creationFailed(LayerFactory factory, Layer layer, Throwable ex) {
                    // Something went wrong connecting to the WMS server.
                    ex.printStackTrace();
                    if (result != null) {
                        result.result(false, null, ex);
                    }

                }
            }
        );
    }

    private void addWMTSService(final IWMTS wmts, IMapServiceResult result) {
        // Create a layer factory, World Wind's general component for creating layers
        // from complex data sources.
        LayerFactory layerFactory = new LayerFactory();
        String sLayers = "";
        for (String layerName : wmts.getLayers()) {
            if (sLayers.length() > 0) {
                sLayers += ",";
            }
            sLayers += layerName;
        }
        // Create an OGC Web Map Service (WMS) layer to display the
        // surface temperature layer from NASA's Near Earth Observations WMS.
        layerFactory.createFromWmts(
                wmts.getURL().toString(), // WMS server URL
                sLayers,  // WMS layer name
                new LayerFactory.Callback() {
                    @Override
                    public void creationSucceeded(LayerFactory factory, Layer layer) {
                        // try to insert layer before the image layer
                        final int insertIdx = ww.getLayers().indexOfLayer(MapInstance.this.imageLayer);
                        if (insertIdx != -1) {
                            ww.getLayers().addLayer(insertIdx, layer);
                            MapInstance.this.layerHash.put(wmts.getGeoId(), layer);
                            layer.putUserProperty(wmts.getGeoId(), "WMTS");
                            Log.i(TAG, "WMTS layer creation succeeded " + wmts.getGeoId());
                            if (result != null) {
                                result.result(true, wmts.getGeoId(), null);
                            }
                            // redraw should be automatic
                        } else {
                            if (result != null) {
                                result.result(false, null, new IllegalStateException("ERROR: unable to locate tactical graphic layer."));
                            }
                        }
                     }

                    @Override
                    public void creationFailed(LayerFactory factory, Layer layer, Throwable ex) {
                        // Something went wrong connecting to the WMS server.
                        ex.printStackTrace();
                        if (result != null) {
                            result.result(false, null, ex);
                        }
                    }
                }
        );
    }

    private void addImageLayer(IImageLayer imageLayer) {
        IGeoBounds bBox = imageLayer.getBoundingBox();
        double deltaLatitude = bBox.getNorth() - bBox.getSouth();
        double deltaLongitude = bBox.getEast() - bBox.getWest();
        Sector sector = Sector.fromDegrees(bBox.getSouth(), bBox.getWest(), deltaLatitude, deltaLongitude);
        ImageSource imageSource = ImageSource.fromUrl(imageLayer.getURL().toString());
        SurfaceImage surfaceImage = new SurfaceImage(sector, imageSource);

        if (this.surfaceLayerHash.containsKey(imageLayer.getGeoId())) {
            this.imageLayer.removeRenderable(this.surfaceLayerHash.get(imageLayer.getGeoId()));
        }
        this.imageLayer.addRenderable(surfaceImage);
        ww.requestRedraw();
        this.surfaceLayerHash.put(imageLayer.getGeoId(), surfaceImage);
    }

    private void addGeoPackage(final IGeoPackage geoPackage, IMapServiceResult result) {
        // Create a layer factory, World Wind's general component for creating layers
        // from complex data sources.
        LayerFactory layerFactory = new LayerFactory();
        final String geoPackagePath = geoPackage.getURL().getPath();
        Log.i(TAG, "gpkg file located at " + geoPackagePath);
        // Create an OGC GeoPackage layer
        layerFactory.createFromGeoPackage(
                geoPackagePath, // file path on the local Android filesystem
                new LayerFactory.Callback() {
                    @Override
                    public void creationSucceeded(LayerFactory factory, Layer layer) {
                        // Add the finished GeoPackage layer to the World Window.
                        final int insertIdx = ww.getLayers().indexOfLayer(MapInstance.this.imageLayer);
                        if (insertIdx != -1) {
                            ww.getLayers().addLayer(insertIdx, layer);
                            MapInstance.this.layerHash.put(geoPackage.getGeoId(), layer);
                            layer.putUserProperty(geoPackage.getGeoId(), "GPKG");
                            Log.i(TAG, "GeoPackage layer creation succeeded");
                            if (result != null) {
                                result.result(true, geoPackage.getGeoId(), null);
                            }
                            ww.requestRedraw();
                        } else {
                            if (result != null) {
                                result.result(false, null, new IllegalStateException("ERROR: unable to locate tactical graphic layer."));
                            }
                        }
                    }

                    @Override
                    public void creationFailed(LayerFactory factory, Layer layer, Throwable ex) {
                        // Something went wrong reading the GeoPackage.
                        ex.printStackTrace();
                        if (result != null) {
                            result.result(false, null, ex);
                        }
                    }
                }
        );
    }

    private void addMapServiceEx(final IMapService mapService, IMapServiceResult result) {
        if (null == mapService) {
            if (result != null) {
                result.result(false, null, new IllegalArgumentException("ERROR: map service argument is null "));
            }
            return;
        }
        if (mapService instanceof IWMS) {
            addWMSService((IWMS) mapService, result);
        } else if (mapService instanceof IImageLayer) {
            addImageLayer((IImageLayer) mapService);
            if (result != null) {
                result.result(true, mapService.getGeoId(), null);
            }
        } else if (mapService instanceof IGeoPackage) {
            addGeoPackage((IGeoPackage) mapService, result);
        } else if (mapService instanceof IWMTS) {
            addWMTSService((IWMTS) mapService, result);
        } else if (mapService instanceof IWCS) {
            this.addWCSService((IWCS) mapService);
            if (result != null) {
                result.result(true, mapService.getGeoId(), null);
            }
        } else if (mapService instanceof IKMLS) {
            // clientApplication used addMapService for KMLService. Features generated based on that add are added directly
            // to a special background layer. We need to investigate if we should generate an event for this.
            kmlServiceLayer.plot(((IKMLS) mapService).getFeature(), true);
            ww.requestRedraw();
            if (result != null) {
                result.result(true, mapService.getGeoId(), null);
            }
        } else if (mapService instanceof ILineOfSight){
            addLineOfSight((ILineOfSight)mapService);
        } else {
            Log.e(TAG, "This MapService is NOT supported " + mapService.getClass().getName());
        }
    }

    @Override
    public void addMapService(final IMapService mapService, IMapServiceResult result) {
        if (!SystemUtils.isCurrentThreadUIThread()) {
                /*
                 * SEE HANDLER NOTES ABOVE.
                 */
            handler.post(new Runnable() {
                @Override
                public void run() {
                    addMapServiceEx(mapService, result);
                }
            });
        } else {
            addMapServiceEx(mapService, result);
        }
    }

    private void addLineOfSight(final ILineOfSight los) {
        // Specify the sightline position, which is the origin of the line of sight calculation
        Position position = new Position(los.getPosition().getLatitude(),
                los.getPosition().getLongitude(),
                los.getPosition().getAltitude());
        // Specify the range of the sightline (meters)
        double range = los.getRange();
        // Create attributes for the visible terrain
        ShapeAttributes visibleAttributes = new ShapeAttributes();
        visibleAttributes.setInteriorColor(new gov.nasa.worldwind.render.Color(
                los.getVisibleAttributes().getRed() / (float) 0xFF,
                los.getVisibleAttributes().getGreen() / (float) 0xFF,
                los.getVisibleAttributes().getBlue() / (float) 0xFF,
                (float)los.getVisibleAttributes().getAlpha()));
        // Create attributes for the occluded terrain
        ShapeAttributes occludedAttributes = new ShapeAttributes();
        occludedAttributes.setInteriorColor(new gov.nasa.worldwind.render.Color(
                los.getOccludeAttributes().getRed() / (float) 0xFF,
                los.getOccludeAttributes().getGreen() / (float) 0xFF,
                los.getOccludeAttributes().getBlue() / (float) 0xFF,
                (float)los.getOccludeAttributes().getAlpha()));
        // Create the sightline
        OmnidirectionalSightline sightline = new OmnidirectionalSightline(position, range);
        // Set the attributes
        sightline.setAttributes(visibleAttributes);
        sightline.setOccludeAttributes(occludedAttributes);

        // Create a layer for the sightline
        RenderableLayer sightlineLayer = new RenderableLayer();
        sightlineLayer.addRenderable(sightline);
            ww.getLayers().addLayer(sightlineLayer);
            MapInstance.this.layerHash.put(los.getGeoId(), sightlineLayer);
            sightlineLayer.putUserProperty(los.getGeoId(), "LOS");
            Log.i(TAG, "LoS layer creation succeeded");
            ww.requestRedraw();
    }


    @Override
    public void addMapService(final IMapService mapService) {
        if (!SystemUtils.isCurrentThreadUIThread()) {
                /*
                 * SEE HANDLER NOTES ABOVE.
                 */
            handler.post(new Runnable() {
                @Override
                public void run() {
                    addMapServiceEx(mapService, null);
                }
            });
        } else {
            addMapServiceEx(mapService, null);
        }
    }

    private void removeLayers(gov.nasa.worldwind.WorldWindow wwLocal, IMapService mapService) {
        Log.i(TAG, "Removing layer " + mapService.getGeoId().toString());
        boolean removed = true;
        // Outer loop added because WW can have layer duplicates
        while (removed) {
            removed = false;
            LayerList layerList = wwLocal.getLayers();
            for (int i = 0; i < layerList.count(); i++) {
                Layer ll = layerList.getLayer(i);
                if (ll != null && ll.hasUserProperty(mapService.getGeoId())) {
                    layerList.removeLayer(i);
                    Log.i(TAG, "removed layer " + mapService.getGeoId());
                    removed = true;
                    break;
                }
            }
        }
        wwLocal.requestRedraw();
    }

    private void removeMapServiceEx(IMapService mapService, IMapServiceResult result) {
        if (mapService instanceof IWMS || mapService instanceof IWMTS
                || mapService instanceof GeoPackage || mapService instanceof ILineOfSight) {
            if (this.layerHash.containsKey(mapService.getGeoId())) {
                removeLayers(ww, mapService);

                if (null != this.miniMap) {
                    removeLayers(miniMap, mapService);
                }
                this.layerHash.remove(mapService.getGeoId());
                if (result != null) {
                    result.result(true, mapService.getGeoId(), null);
                }
            } else {
                if (result != null) {
                    result.result(false, null, new IllegalStateException("ERROR: unable to locate tactical graphic layer."));
                }
            }
        } else if (mapService instanceof IImageLayer) {
            if (this.surfaceLayerHash.containsKey(mapService.getGeoId())) {
                this.imageLayer.removeRenderable(this.surfaceLayerHash.get(mapService.getGeoId()));
                ww.requestRedraw();
                this.surfaceLayerHash.remove(mapService.getGeoId());
                if (result != null) {
                    result.result(true, mapService.getGeoId(), null);
                }
            } else {
                if (result != null) {
                    result.result(false, null, new IllegalStateException("ERROR: unable to locate tactical graphic layer."));
                }
            }
        } else if (mapService instanceof  IWCS) {
            ww.getGlobe().getElevationModel().clearCoverages();
            ww.requestRedraw();
            if (result != null) {
                result.result(true, mapService.getGeoId(), null);
            }
        } else if (mapService instanceof IKMLS) {
            IKMLS kmlService = (IKMLS) mapService;
            if (null != kmlService.getFeature()) {
                removeFeature(kmlService.getFeature().getGeoId(), null);
            }
            ww.requestRedraw();
            if (result != null) {
                result.result(true, mapService.getGeoId(), null);
            }
        } else {
            Log.e(TAG, "Failed removing unsupported MapService " + mapService.getClass().getCanonicalName());
        }
    }

    @Override
    public void removeMapService(final IMapService wmsService) {
        if (!SystemUtils.isCurrentThreadUIThread()) {
            /*
             * SEE HANDLER NOTES ABOVE.
             */
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MapInstance.this.removeMapServiceEx(wmsService, null);
                }
            });
        } else {
            this.removeMapServiceEx(wmsService, null);
        }
    }

    @Override
    public void removeMapService(final IMapService wmsService, IMapServiceResult result) {
        if (!SystemUtils.isCurrentThreadUIThread()) {
            /*
             * SEE HANDLER NOTES ABOVE.
             */
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MapInstance.this.removeMapServiceEx(wmsService, result);
                }
            });
        } else {
            this.removeMapServiceEx(wmsService, result);
        }
    }

    @Override
    public ICamera getCamera() {
        return this.oMapViewController.getCamera();
    }

    @Override
    public void setCamera(ICamera oCamera, boolean animate, Object userContext) {
        this.oMapViewController.setCamera(oCamera, animate);
    }

    @Override
    public void applyCameraChange(ICamera oCamera, boolean animate, Object userContext) {
        this.oMapViewController.applyCameraChange(oCamera, animate);
    }

    @Override
    public ILookAt getLookAt() {
        return this.oMapViewController.getLookAt();
    }

    @Override
    public void setLookAt(ILookAt oLookAt, boolean animate, Object userContext) {
        this.oMapViewController.setLookAt(oLookAt, animate);
    }

    @Override
    public void applyLookAtChange(ILookAt oLookAt, boolean animate, Object userContext) {
        this.oMapViewController.applyLookAtChange(oLookAt, animate);
    }
    /**
     * If this was designed for zoomIt support then it should be removed as all calculations are done in the core. Methids
     * getFieldOfView(), getViewHeight() and getViewWidth() were added to this interface for zoomIt support.
     * @param bounds
     */
    @Override
    public void setBounds(IGeoBounds bounds) {
        throw new UnsupportedOperationException("setBounds is not supported");
    }

    @Override
    public void setMotionLockMode(MapMotionLockEnum mode) {
        this.mapController.setLockMode(mode);
    }

    @Override
    public IMapEngineRequirements getRequirements() {
        return null;
    }

    @Override
    public void getCapture(IScreenCaptureCallback callback) {
        ScreenCapture screenCapture = new ScreenCapture(callback);
        screenCapture.generateScreenCapture(getWW());
    }

    @Override
    public void registerMilStdRenderer(IMilStdRenderer oRenderer) {
        super.registerMilStdRenderer(oRenderer);
        MilStd2525LevelOfDetailSelector.initInstance(oRenderer);
    }

    /**
     * Return field of view in degrees
     * @return
     */
    @Override
    public double getFieldOfView() {
        return ww.getFieldOfView();
    }

    /**
     * Return view height
     * @return
     */
    @Override
    public int getViewHeight() {
        return ww.getHeight();
    }

    /**
     * Return view width
     * @return
     */
    @Override
    public int getViewWidth() {
        return ww.getWidth();
    }

    /**
     * This method sets the threshold of the distances beyond which the map displays MilStd single point
     * icons as dots.
     * @param dValue Distance in meters.
     */
    @Override
    public void setFarDistanceThreshold(double dValue) {
        MilStd2525LevelOfDetailSelector.setFarThreshold(dValue);
        this.FAR_THRESHOLD = dValue;
        ww.requestRedraw();
    }

    /**
     * This method sets the threshold of the distances beyond which the map displays MilStd single point
     * icons with no modifiers. Icons at distances less than this threshold get displayed as fully qualified icons
     * as per the label setting.
     * @param dValue Distance in meters.
     */
    @Override
    public void setMidDistanceThreshold(double dValue) {
        MilStd2525LevelOfDetailSelector.setMidThreshold(dValue);
        this.MID_THRESHOLD = dValue;
        ww.requestRedraw();
    }

    public double getFarDistanceThreshold() {
        return this.FAR_THRESHOLD;
    }

    public double getMidDistanceThreshold() {
        return this.MID_THRESHOLD;
    }

    /**
     * Fetch Version information from BuildConfig
     * @param builder
     * @param buildConfigFields
     */
    @Override
    public void getVersionInformation(StringBuilder builder, List<String> buildConfigFields) {
        getVersionInformation(builder, buildConfigFields, "mil.emp3.worldwind.BuildConfig");
    }

    /**
     * This code is picked up straight from NASA examples and you will need to uncomment the last line in initMapEngine.
     */
    protected boolean printMetrics() {
        // Assemble the current system memory info.
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);

        // Assemble the current World Wind frame metrics.
        FrameMetrics fm = ww.getFrameMetrics();

        // Print a log message with the system memory, World Wind cache usage, and World Wind average frame time.
        Logger.log(Logger.INFO, String.format(Locale.US, "System memory %,.0f KB    Heap memory %,.0f KB    Render cache %,.0f KB    Frame time %.1f ms + %.1f ms",
                (mi.totalMem - mi.availMem) / 1024.0,
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024.0,
                fm.getRenderResourceCacheUsedCapacity() / 1024.0,
                fm.getRenderTimeAverage(),
                fm.getDrawTimeAverage()));

        // Reset the accumulated World Wind frame metrics.
        fm.reset();

        // Print the frame metrics again after the configured delay.
        return logHandler.sendEmptyMessageDelayed(PRINT_METRICS, PRINT_METRICS_DELAY);
    }
    /**
     * This must be an inner class of mapInstance so that it can access the surfaceHolder in its constructor.
     */
    protected class WorldWindow extends gov.nasa.worldwind.WorldWindow {
        private String TAG = WorldWindow.class.getSimpleName();

        public WorldWindow(Context context) {
            super(context);
            Log.i(TAG, "Constructor WorldWindow Context");
        }
        public WorldWindow(Context context, AttributeSet attrs) {
            super(context, attrs);
            Log.i(TAG, "Constructor WorldWindow Context, AttributeSet");
        }

        @Override
        protected void init(EGLConfigChooser configChooser) {
            Log.i(TAG, "In init");
            super.init(new ConfigChooser());
            Log.i(TAG, "Out init");
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
        }

        @Override
        public SurfaceHolder getHolder() {
            Log.d(TAG, "getHolder ");
            if(null == mySurfaceHolder) {
                return super.getHolder();
            } else {
                Log.d(TAG, "getHolder mySurfaceHolder");
                return mySurfaceHolder;
            }
        }

        @Override
        public void onResume() {
            super.onResume();
        }

        @Override
        public void onPause() {
            super.onPause();
        }
    }

    protected class MiniMapWorldWindow extends WorldWindow {
        private String TAG = MiniMapWorldWindow.class.getSimpleName();

        public MiniMapWorldWindow(Context context) {
            super(context);
            Log.i(TAG, "Constructor MiniMapWorldWindow Context");
        }

        public MiniMapWorldWindow(Context context, AttributeSet attrs) {
            super(context, attrs);
            Log.i(TAG, "Constructor MiniMapWorldWindow Context, AttributeSet");
        }

        public boolean onTouchEvent(MotionEvent event) {
            return false;
        }
    }

    /**
     * Don't recalculate the bounding area, it is calculated when MAP MOTION is STOPPED. Return what was calculated.
     * This could be null if map is in motion. You can look at Emp3navigationListener to see when bounding area is
     * recalculated.
     * @return
     */
    @Override
    public IGeoBounds getMapBounds() {
        return BoundsGeneration.getCurrentBoundingArea(this);
    }

    /**
     * Caller must be on UI thread.
     * @param pos
     * @return
     */
    @Override
    public Point geoToContainer(IGeoPosition pos) {
        if(!SystemUtils.isCurrentThreadUIThread()) {
            Log.w(TAG, "geoToContainer not on UI thread, result may not be correct");
        }
        Point result = new Point();
        if (mapController.groundPositionToScreenPoint(pos.getLatitude(), pos.getLongitude(), result)) {
            return result;
        }
        return null;
    }

    /**
     * Caller must be on UI thread
     *
     * Deprecated: Use containerToGeo with PointF instead for
     * more precise calculations
     * @param point
     * @return
     */
    @Deprecated
    @Override
    public IGeoPosition containerToGeo(Point point) {
        return this.containerToGeo(new PointF(point.x, point.y));
    }

    /**
     * Caller must be on UI thread
     * @param point
     * @return
     */
    @Override
    public IGeoPosition containerToGeo(PointF point) {
        if(!SystemUtils.isCurrentThreadUIThread()) {
            Log.w(TAG, "containerToGeo not on UI thread, result may not be correct");
        }
        IGeoPosition geoPosition = null;
        Position pos = new Position();
        if (mapController.screenPointToGroundPosition(point.x, point.y, pos)) {
            geoPosition = new GeoPosition();
            geoPosition.setLatitude(pos.latitude);
            geoPosition.setLongitude(pos.longitude);
        }
        return geoPosition;
    }

    private void processSelection(List<IFeature> featureList, boolean selected) {
        for (IFeature feature: featureList) {
            FeatureRenderableMapping oMapping = this.featureHash.get(feature.getGeoId());
            if (oMapping != null) {
                oMapping.setSelected(selected);
                oMapping.setDirty(true);
            }
        }

        // Update the view on UI thread.
        if (!SystemUtils.isCurrentThreadUIThread()) {
            /*
             * SEE HANDLER NOTES ABOVE.
             */
            handler.post(new Runnable() {
                @Override
                public void run() {
                    ww.requestRedraw();
                }
            });
        } else {
            ww.requestRedraw();
        }
    }

    @Override
    public void selectFeatures(List<IFeature> featureList) {
        this.processSelection(featureList, true);
    }

    @Override
    public void deselectFeatures(List<IFeature> featureList) {
        this.processSelection(featureList, false);
    }

    private void updateFontSize() {
        float fontPixelSize;

        for (FeatureRenderableMapping oMapping: this.featureHash.values()) {
            fontPixelSize = FontUtilities.getTextPixelSize(oMapping.getFeature().getLabelStyle(), this.fontSizeModifier);

            for (Object renderable: oMapping.getRenderableList()) {
                if (renderable instanceof gov.nasa.worldwind.shape.Label) {
                    gov.nasa.worldwind.shape.Label label = (gov.nasa.worldwind.shape.Label) renderable;

                    label.getAttributes().setTextSize(fontPixelSize);
                }
            }
        }

        ww.requestRedraw();
    }

    @Override
    public void setFontSizeModifier(FontSizeModifierEnum value) {
        this.fontSizeModifier = value;

        if (!SystemUtils.isCurrentThreadUIThread()) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    MapInstance.this.updateFontSize();
                }
            });
        } else {
            this.updateFontSize();
        }
    }

    public Map<UUID, mil.emp3.worldwind.feature.FeatureRenderableMapping> getFeatureHash() {
        return featureHash;
    }

    /**
     * Retrieves the layer responsible for handling a particular IFeature.
     */
    private EmpLayer getRenderableLayer(IFeature feature) {
        final EmpLayer layer = empLayerMap.get(feature.getFeatureType());
        if (layer == null) {
            throw new IllegalStateException("layer == null");
        }
        return layer;
    }

    public void addToDirtyOnMapMove(UUID geoId) {
        this.dirtyOnMapMove.add(geoId);
    }

    /**
     * Users extending MapInstance to add their own feature can add their layer.
     * @return
     */
    protected Map<FeatureTypeEnum, EmpLayer> getEmpLayerMap() {
        return empLayerMap;
    }

    private void createBrightnessBitmaps() {
        gov.nasa.worldwind.render.ImageOptions imageOptions = new gov.nasa.worldwind.render.ImageOptions();

        imageOptions.imageConfig = WorldWind.RGBA_8888;
        //imageOptions.resamplingMode = WorldWind.BILINEAR;
        imageOptions.wrapMode = WorldWind.REPEAT;

        blackBitmap = android.graphics.Bitmap.createBitmap(Resources.getSystem().getDisplayMetrics(), BRIGHTNESS_BITMAP_WIDTH_HEIGHT, BRIGHTNESS_BITMAP_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);
        whiteBitmap = android.graphics.Bitmap.createBitmap(Resources.getSystem().getDisplayMetrics(), BRIGHTNESS_BITMAP_WIDTH_HEIGHT, BRIGHTNESS_BITMAP_WIDTH_HEIGHT, Bitmap.Config.ARGB_8888);

        Sector globeSector = Sector.fromDegrees(-90, -180, 180, 360);

        blackSurfaceImage = new EmpSurfaceImage();
        whiteSurfaceImage = new EmpSurfaceImage();

        blackSurfaceImage.setDisplayName("Black SurfaceImage");
        whiteSurfaceImage.setDisplayName("White SurfaceImage");

        blackSurfaceImage.setSector(globeSector);
        whiteSurfaceImage.setSector(globeSector);

        blackSurfaceImage.setImageOptions(imageOptions);
        whiteSurfaceImage.setImageOptions(imageOptions);

        blackImageSource = ImageSource.fromBitmap(blackBitmap);
        whiteImageSource = ImageSource.fromBitmap(whiteBitmap);

        blackSurfaceImage.setImageSource(blackImageSource);
        whiteSurfaceImage.setImageSource(whiteImageSource);

        setBitmapBrightness();

        this.brightnessLayer.addRenderable(blackSurfaceImage);
        this.brightnessLayer.addRenderable(whiteSurfaceImage);

        hatchBitmap = android.graphics.Bitmap.createBitmap(Resources.getSystem().getDisplayMetrics(),
                HATCH_SIZE, HATCH_SIZE, Bitmap.Config.ARGB_8888);
        crossHatchBitmap = android.graphics.Bitmap.createBitmap(Resources.getSystem().getDisplayMetrics(),
                HATCH_SIZE, HATCH_SIZE, Bitmap.Config.ARGB_8888);

        setHatchPatterns();
        hatchImageSource = ImageSource.fromBitmap(hatchBitmap);
        crossHatchImageSource = ImageSource.fromBitmap(crossHatchBitmap);
    }

    private void setBitmapBrightness() {
        int blackAlpha = 0;
        int whiteAlpha = 0;

        if (backgroundBrightness < 50) {
            blackAlpha = (int) ((double) (50 - backgroundBrightness) / 50.0 * 255);
        } else if (backgroundBrightness > 50) {
            whiteAlpha = (int) ((double) (backgroundBrightness - 50) / 50.0 * 255);
        }
        this.brightnessProcessingPosted = false;

        int blackColor = android.graphics.Color.argb(blackAlpha, 0, 0, 0);
        int whiteColor = android.graphics.Color.argb(whiteAlpha, 255, 255, 255);

        for (int y = 0; y < BRIGHTNESS_BITMAP_WIDTH_HEIGHT; y++) {
            for (int x = 0; x < BRIGHTNESS_BITMAP_WIDTH_HEIGHT; x++) {
                blackBitmap.setPixel(x, y, blackColor);
                whiteBitmap.setPixel(x, y, whiteColor);
            }
        }
        blackSurfaceImage.imageSourceUpdated();
        whiteSurfaceImage.imageSourceUpdated();

        Log.d(TAG, "Brightness processed (" + backgroundBrightness + ").");
    }

    /**
     * What you’re trying to do is one of our intended use cases, and is achieved by assembling the ‘hatch’ grid lines in white,
     * rather than black. Said another way, the template Bitmap must be filled with white pixels and transparent pixels.
     * World Wind shapes supporting an image and an attribute color are rendered by multiplying each image pixel's RGBA color
     * by the attribute’s RGBA color. We call this rendering mode modulation. This happens on the GPU and has no performance cost.
     * A white/transparent Bitmap therefore acts as a color template, since modulation replaces white pixels with the attribute color
     * but leaves transparent pixels unchanged.
     */
    private void setHatchPatterns() {
        int transparent = android.graphics.Color.argb(0, 0, 0, 0);

        for (int y = 0; y < HATCH_SIZE; y++) {
            for (int x = 0; x < HATCH_SIZE; x++) {
                if (y == x || (y - 1) == x || y == (x - 1)) {
                    hatchBitmap.setPixel(HATCH_SIZE - 1 - x, y, Color.WHITE);
                    crossHatchBitmap.setPixel(HATCH_SIZE - 1 - x, y, Color.WHITE);
                } else {
                    hatchBitmap.setPixel(HATCH_SIZE - 1 - x, y, transparent);
                    crossHatchBitmap.setPixel(HATCH_SIZE - 1 - x, y, transparent);
                }
            }
            // Don't merge these loops or the transparent pixels will get overwritten
            // in the cross hatch bitmap
            for (int x = 0; x < HATCH_SIZE; x++) {
                if (y == x || (y - 1) == x || y == (x - 1)) {
                    crossHatchBitmap.setPixel(x, y, Color.WHITE);
                }
            }
        }

        Log.d(TAG, "Hatch processed ");
    }

    public ImageSource getHatchImage() {
        return hatchImageSource;
    }

    public ImageSource getCrossHatchImage() {
        return crossHatchImageSource;
    }

    @Override
    public void setBackgroundBrightness(int setting) {
        if (setting < 0) {
            setting = 0;
        } else if (setting > 100) {
            setting = 100;
        }

        // Set the new brightness setting.
        backgroundBrightness = setting;
        if (!this.brightnessProcessingPosted) {
            if (!SystemUtils.isCurrentThreadUIThread()) {
                this.brightnessProcessingPosted = true;
                Log.d(TAG, "Scheduling brightness processing.");
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        setBitmapBrightness();
                        ww.requestRedraw();
                    }
                });
            } else {
                setBitmapBrightness();
                ww.requestRedraw();
            }
        }
    }

    @Override
    public int getBackgroundBrightness() {
        return backgroundBrightness;
    }

    public void updateMiniMapCamera() {
        if (null != this.miniMap) {
            Navigator mapNavigator = this.ww.getNavigator();
            Navigator miniMapNavigator = this.miniMap.getNavigator();

            if (null != mapNavigator) {
                double altitude = mapNavigator.getAltitude();
                double latitude = mapNavigator.getLatitude();
                double longitude = mapNavigator.getLongitude();
                if (null != miniMapNavigator) {
                    miniMapNavigator.setLatitude(latitude);
                    miniMapNavigator.setLongitude(longitude);
                    miniMapNavigator.setAltitude(altitude * 4);
                    miniMapNavigator.setHeading(0);
                    miniMapNavigator.setRoll(0);
                    miniMapNavigator.setTilt(0);
                }
            }
            this.miniMap.requestRedraw();
        }
    }

    @Override
    public android.view.View showMiniMap() {
        if (null == this.miniMap) {
            Context context = ww.getContext();
            this.miniMap = new MiniMapWorldWindow(context);

            // Now we must load all map layers.
            ww.queueEvent(new Runnable() {
                @Override
                public void run() {
                    MapInstance.this.miniMap.getLayers().addLayer(new BackgroundLayer());

                    for (UUID id: MapInstance.this.layerHash.keySet()) {
                        Layer layer = MapInstance.this.layerHash.get(id);
                        MapInstance.this.miniMap.getLayers().addLayer(layer);
                    }
                    MapInstance.this.updateMiniMapCamera();
                }
            });
        }
        return this.miniMap;
    }

    @Override
    public void hideMiniMap() {
        if (null != this.miniMap) {
            if (null != this.miniMap.getParent()) {
                if (this.miniMap.getParent() instanceof android.view.ViewGroup) {
                    ((android.view.ViewGroup) this.miniMap.getParent()).removeView(this.miniMap);
                }
            }
            this.miniMap.onDetachedFromWindow();
            this.miniMap = null;
        }
    }

    public PickNavigateController getMapController() {
        return mapController;
    }

    @Override
    public void setMapGridGenerator(IMapGridLines gridGenerator) {
        if (null != this.gridLayer) {
            this.gridLayer.setMapGridGenerator(gridGenerator);
        }
    }

    @Override
    public void scheduleMapRedraw() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                ww.requestRedraw();
            }
        });
    }
}
