package mil.emp3.worldwind;

import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import gov.nasa.worldwind.FrameMetrics;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.geom.Offset;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layer.BackgroundLayer;
import gov.nasa.worldwind.layer.RenderableLayer;
import gov.nasa.worldwind.ogc.WmsLayer;
import gov.nasa.worldwind.ogc.WmsLayerConfig;
import gov.nasa.worldwind.render.ImageSource;
import gov.nasa.worldwind.render.RenderResourceCache;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.shape.Placemark;
import gov.nasa.worldwind.shape.PlacemarkAttributes;
import gov.nasa.worldwind.util.Logger;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Path;
import mil.emp3.api.Polygon;
import mil.emp3.api.Text;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.interfaces.IWMS;
import mil.emp3.mapengine.CoreMapInstance;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.mapengine.api.Capabilities;
import mil.emp3.mapengine.interfaces.IEmpResources;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;

import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.mapengine.interfaces.IMapEngineProperties;
import mil.emp3.mapengine.interfaces.IMapEngineRequirements;
import mil.emp3.mapengine.interfaces.ISetVisibilityList;

import mil.emp3.worldwind.controller.PickNavigateController;
import mil.emp3.worldwind.feature.MilStd2525SinglePoint;
import mil.emp3.worldwind.feature.MilStd2525TacticalGraphic;
import mil.emp3.worldwind.feature.support.MilStd2525LevelOfDetailSelector;
import mil.emp3.worldwind.utils.ConfigChooser;
import mil.emp3.worldwind.utils.Conversion;
import mil.emp3.worldwind.utils.MapEngineProperties;
import mil.emp3.worldwind.utils.SystemUtils;

public class MapInstance extends CoreMapInstance {
    private static final String TAG = MapInstance.class.getSimpleName();

    private Context context;
    private WorldWindow ww;
    private SurfaceHolder mySurfaceHolder;                 // This is required for WallPaper Service
    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    
    private PickNavigateController mapController = null;
    private RenderableLayer oRenderableLayer = null;
    private TacticalGraphicLayer oTacticalGraphicLayer = null;
    protected Emp3NavigationListener oMapViewController;

    private double FAR_THRESHOLD = 30000;
    private double MID_THRESHOLD = 10000;


    private java.util.HashMap<java.util.UUID, WmsLayer> wmsHash;
    private java.util.Map<java.util.UUID, mil.emp3.worldwind.FeatureRenderableMapping> featureHash;

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

    public Emp3NavigationListener getCameraHandler() {
        return this.oMapViewController;
    }

    public void initMapEngine(Context context, AttributeSet attrs) {
        // Create the World Window and set it as the content view for this activity.
        Log.i(TAG, "initMapEngine()");
        this.oEngineCapabilities = new Capabilities(
                new FeatureTypeEnum[] {
                        FeatureTypeEnum.GEO_MIL_SYMBOL,
                        FeatureTypeEnum.GEO_POINT,
                        FeatureTypeEnum.GEO_PATH,
                        FeatureTypeEnum.GEO_POLYGON,
                        FeatureTypeEnum.GEO_TEXT
                },
                new WMSVersionEnum[] {WMSVersionEnum.VERSION_1_1,
                        WMSVersionEnum.VERSION_1_1_1,
                        WMSVersionEnum.VERSION_1_3,
                        WMSVersionEnum.VERSION_1_3_0});

        if(null == attrs) {
            ww = new WorldWindow(context);
        } else {
            ww = new WorldWindow(context, attrs);
        }

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

        this.context = context;
        this.oMapViewController = new Emp3NavigationListener(this, ww);
        this.handler = new Handler(Looper.getMainLooper());
        this.wmsHash = new java.util.HashMap<>();
        this.featureHash = new ConcurrentHashMap<>(); // zoom operation and re-rendering of Tactical Graphics will otherwise crash

        ww.getLayers().addLayer(new BackgroundLayer());
        //this.getLayers().addLayer(new BlueMarbleLandsatLayer());

        // First place the TG layer next so it renders before icons.
        this.oTacticalGraphicLayer = new TacticalGraphicLayer(this);
        ww.getLayers().addLayer(this.oTacticalGraphicLayer);

        //The the layer for all other stuff.
        this.oRenderableLayer = new RenderableLayer();
        ww.getLayers().addLayer(this.oRenderableLayer);

        // Uncomment the following line if you want to start tracing the render cache status.
        // This should always be the last line of code in this method.
        // logHandler.sendEmptyMessageDelayed(PRINT_METRICS, PRINT_METRICS_DELAY);
    }

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
        java.util.UUID oUniqueId;
        this.generateStateChangeEvent(MapStateEnum.SHUTDOWN_IN_PROGRESS);
        this.oMapViewController.Destroy();
        
        while (!this.wmsHash.isEmpty()) {
            oUniqueId = (java.util.UUID) this.wmsHash.keySet().toArray()[0];
            WmsLayer oLayer = this.wmsHash.get(oUniqueId);
            this.wmsHash.remove(oUniqueId);
            ww.getLayers().removeLayer(oLayer);
        }
        
        while (!this.featureHash.isEmpty()) {
            oUniqueId = (java.util.UUID) this.featureHash.keySet().toArray()[0];
            final java.util.List<Renderable> removeRenderableList = new java.util.ArrayList<>();
            this.removeFeature(oUniqueId, removeRenderableList);
            if(removeRenderableList.size() > 0) {
                // There should be max one item and we are in UI thread
                oRenderableLayer.removeRenderable(removeRenderableList.get(0));
            }
        }

        this.generateStateChangeEvent(MapStateEnum.SHUTDOWN);

        ww.onDetachedFromWindow();
        Log.i(TAG, "onDestroy complete");
    }

    @Override
    public void onResume() {
        ww.onResume();
    }

    @Override
    public void onPause() {
        ww.onPause();
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
                oThis.getCamera();
                Log.d(TAG, "Map Ready firing.");
                oThis.generateStateChangeEvent(MapStateEnum.MAP_READY);
            }
        }, 500);

        return ww;
    }

    @Override
    public IMapEngineProperties getMapEngineProperties() {
        return new MapEngineProperties();
    }

    @Override
    public void setVisibility(ISetVisibilityList visibilityList) {
    }

    public void reRenderMPTacticalGraphics() {
        boolean issueRedraw = false;

        for (FeatureRenderableMapping mapping: this.featureHash.values()) {
            if (mapping instanceof MilStd2525TacticalGraphic) {
                mapping.isDirty(true);
                issueRedraw = true;
            }
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

    public void renderMPTacticalGraphic(MilStd2525TacticalGraphic mapping, IGeoBounds bounds) {
        Renderable tempRenderable;
        java.util.List<Renderable> newRenderables;
        gov.nasa.worldwind.shape.Path wwPath;
        gov.nasa.worldwind.shape.Polygon wwPolygon;
        MilStdSymbol oSymbol = (MilStdSymbol) mapping.getFeature();

        newRenderables = mapping.getRenderable();
        newRenderables.clear();
        java.util.List<IFeature> featureList = this.oMilStdRenderer.getTGRenderableShapes(this, oSymbol, mapping.isSelected());

        for (IFeature feature: featureList) {
            tempRenderable = null;
            if (feature instanceof Path) {
                // Create a WW path object.
                java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = new java.util.ArrayList<>();
                gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
                wwPath = new gov.nasa.worldwind.shape.Path(wwPositionList, shapeAttribute);
                this.createWWPath((mil.emp3.api.Path) feature, wwPath, null);
                // shapeAttribute.setOutlineStippleFactor(MapEngineUtils.getTGStippleFactor(oSymbol.getBasicSymbol(), feature.getStrokeStyle())); TODO
                // shapeAttribute.setOutlineStipplePattern(MapEngineUtils.getTGStipplePattern(oSymbol.getBasicSymbol(), feature.getStrokeStyle())); TODO
                tempRenderable = wwPath;
            } else if (feature instanceof Polygon) {
                // Create a WW polygon object.
                java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = new java.util.ArrayList<>();
                gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
                wwPolygon = new gov.nasa.worldwind.shape.Polygon(wwPositionList, shapeAttribute);
                this.createWWPolygon((mil.emp3.api.Polygon) feature, wwPolygon, null);
                tempRenderable = wwPolygon;
            } else if (feature instanceof Text) {
                // Create a WW text object.
                mil.emp3.api.Text textFeature = (mil.emp3.api.Text) feature;
                IGeoLabelStyle labelStyle = textFeature.getLabelStyle();
                IGeoPosition textFeaturePosition = textFeature.getPosition();
                gov.nasa.worldwind.geom.Position textPosition = new gov.nasa.worldwind.geom.Position();
                gov.nasa.worldwind.shape.TextAttributes textAttribute = new gov.nasa.worldwind.shape.TextAttributes();

                textPosition.set(textFeaturePosition.getLatitude(), textFeaturePosition.getLongitude(), textFeaturePosition.getAltitude());

                //textAttribute.setScale(labelStyle.getScale());
                textAttribute.setTextColor(Conversion.covertColor(labelStyle.getColor()));

                gov.nasa.worldwind.geom.Offset textOffset;
                switch (labelStyle.getJustification()) {
                    case LEFT:
                        textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_FRACTION, 0.5);
                        break;
                    case RIGHT:
                        textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_INSET_PIXELS, 0, WorldWind.OFFSET_FRACTION, 0.5);
                        break;
                    case CENTER:
                    default:
                        textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5);
                        break;
                }
                textAttribute.setTextOffset(textOffset);
                textAttribute.setTextSize(textFeature.getFontSize());
                //textAttribute.setOutlineWidth(3);
                //textAttribute.setEnableOutline(true);

                switch (textFeature.getTypeFaceStyle()) {
                    case BOLD:
                        textAttribute.setTypeface(Typeface.create(textFeature.getFontFamily(), Typeface.BOLD));
                        break;
                    case ITALIC:
                        textAttribute.setTypeface(Typeface.create(textFeature.getFontFamily(), Typeface.ITALIC));
                        break;
                    case BOLD_ITALIC:
                        textAttribute.setTypeface(Typeface.create(textFeature.getFontFamily(), Typeface.BOLD_ITALIC));
                        break;
                    case NORMAL:
                    default:
                        textAttribute.setTypeface(Typeface.create(textFeature.getFontFamily(), Typeface.NORMAL));
                        break;
                }

                gov.nasa.worldwind.shape.Label label = new gov.nasa.worldwind.shape.Label(textPosition, textFeature.getText(), textAttribute);

                tempRenderable = label;
            }

            if (tempRenderable != null) {
                tempRenderable.setPickDelegate(oSymbol);
                newRenderables.add(tempRenderable);
            }
        }
    }

    private void plotTacticalGraphicSymbol(MilStdSymbol oSymbol, boolean bVisible) {
        MilStd2525TacticalGraphic mapping = (MilStd2525TacticalGraphic) this.findFeatureMapping(oSymbol);

        if (mapping == null) {
            // Its a new feature
            mapping = new MilStd2525TacticalGraphic(oSymbol, this);
            this.storeFeature(mapping);
            this.oTacticalGraphicLayer.addRenderable(mapping);
        } else {
            mapping.setFeature(oSymbol);
        }

        mapping.setVisible(bVisible);
    }

    /**
     * Make sure you don't touch the View in this method.
     * @param feature
     * @param renderable
     * @param visible
     * @param removeRenderablesList - old renderable that should be removed goes here
     */
    private void storeFeature(IFeature feature, Renderable renderable, boolean visible,
                              java.util.List<Renderable> removeRenderablesList) {
        FeatureRenderableMapping oMapping;
        
        if (this.featureHash.containsKey(feature.getGeoId())) {
            oMapping = this.featureHash.get(feature.getGeoId());
            
            removeRenderablesList.addAll(oMapping.getRenderable());
            oMapping.setFeature(feature);
            oMapping.setRenderable(renderable);
            oMapping.setVisible(visible);
        } else {
            oMapping = new FeatureRenderableMapping(feature, renderable);
            oMapping.setVisible(visible);
            this.featureHash.put(feature.getGeoId(), oMapping);
        }
    }

    /**
     * This method adds the mapping to the feature hash if does not exists.
     * @param mapping
     */
    private void storeFeature(FeatureRenderableMapping mapping) {
        if (!this.featureHash.containsKey(mapping.getFeature().getGeoId())) {
            this.featureHash.put(mapping.getFeature().getGeoId(), mapping);
        }
    }

    private FeatureRenderableMapping findFeatureMapping(IFeature feature) {
        if (this.featureHash.containsKey(feature.getGeoId())) {
            return this.featureHash.get(feature.getGeoId());
        }

        return null;
    }
    /**
     * Make sure you don't touch the View in this method.
     * @param oSymbol
     * @param bVisible
     * @param renderableList - new renderable that should be added to the map goes here
     * @param removeRenderablesList - old renderable that should be removed goes here
     */
    private void plotSinglePointSymbol(MilStdSymbol oSymbol, boolean bVisible, java.util.List<Renderable> renderableList,
                                       java.util.List<Renderable> removeRenderablesList) {
        if (this.featureHash.containsKey(oSymbol.getGeoId())) {
            FeatureRenderableMapping oMapping = this.featureHash.get(oSymbol.getGeoId());
            MilStd2525SinglePoint oWWSymbol = (MilStd2525SinglePoint) oMapping.getRenderable().get(0);

            oMapping.setFeature(oSymbol);
            oMapping.setVisible(bVisible);
            oWWSymbol.setSelected(oMapping.isSelected());
            oWWSymbol.updateSymbol(oSymbol);
        } else {
            IGeoPosition oPos = oSymbol.getPositions().get(0);
            MilStd2525SinglePoint oWWSymbol = new MilStd2525SinglePoint(this, this.oMilStdRenderer, Position.fromDegrees(oPos.getLatitude(), oPos.getLongitude(), oPos.getAltitude()), oSymbol, false);

            if (oWWSymbol == null) {
                Log.e(TAG, "MilStd2525SinglePoint failed to create.");
                return;
            }

            this.storeFeature(oSymbol, oWWSymbol, bVisible, removeRenderablesList);

            if (bVisible) {
                renderableList.add(oWWSymbol);
            }
        }
    }

    public IconSizeEnum getIconSizeSetting() {
        return storageManager.getIconSize(this);
    }

    /**
     * Make sure you don't touch the View in this method.
     * @param oPoint
     * @param bVisible
     * @param renderableList - new renderable that should be added to the map goes here
     * @param removeRenderablesList - old renderable that should be removed goes here
     */
    private void plotPoint(mil.emp3.api.Point oPoint, boolean bVisible, java.util.List<Renderable> renderableList,
                           java.util.List<Renderable> removeRenderablesList) {
        Offset imageOffset;
        PlacemarkAttributes oAttr;
        IGeoIconStyle oIconStyle = oPoint.getIconStyle();
        IGeoPosition oPos = oPoint.getPosition();
        String sURL = oPoint.getIconURI();
        IconSizeEnum iconSizeEnum = this.getIconSizeSetting();
        double dScale = iconSizeEnum.getScaleFactor() * oPoint.getIconScale();

        if (oPos == null) {
            Log.e(TAG, "Point feature with no coordinate.");
            return;
        }

        if (this.featureHash.containsKey(oPoint.getGeoId())) {
            FeatureRenderableMapping oMapping = this.featureHash.get(oPoint.getGeoId());
            oMapping.setVisible(bVisible);

            if (oMapping.isSelected()) {
                dScale = dScale * this.empResources.getSelectedIconScale(this);
            }
        }

        if (oPoint.getResourceId() != 0) {
            IEmpImageInfo imageInfo =  this.empResources.getAndroidResourceIconImageInfo(oPoint.getResourceId());

            if (imageInfo == null) {
                Log.e(TAG, "Android resource for point feature not found.");
                return;
            }

            Rect imageBounds = imageInfo.getImageBounds();      // The bounds of the entire image, including text

            if (oIconStyle != null) {
                imageOffset = new Offset(
                        gov.nasa.worldwind.WorldWind.OFFSET_FRACTION, oIconStyle.getOffSetX(), // x offset
                        gov.nasa.worldwind.WorldWind.OFFSET_FRACTION, 1.0 - oIconStyle.getOffSetY()); // y offset
            } else {
                imageOffset = new Offset(gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, 0, // x offset
                        gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, imageBounds.height()); // y offset
            }
            oAttr = PlacemarkAttributes.createWithImage(ImageSource.fromBitmap(imageInfo.getImage())).setImageOffset(imageOffset);
        } else if ((sURL == null) || (sURL.length() == 0)) {
            IEmpImageInfo imageInfo =  this.empResources.getDefaultIconImageInfo();

            if (imageInfo == null) {
                Log.e(TAG, "Failed to load default icon for point feature.");
                return;
            }

            Rect imageBounds = imageInfo.getImageBounds();      // The bounds of the entire image, including text
            Point centerPoint = imageInfo.getCenterPoint();     // The center of the core symbol
            IGeoIconStyle defaultIconStyle = this.empResources.getDefaultIconStyle();

            if (defaultIconStyle != null) {
                oIconStyle = defaultIconStyle;
            } else {
                Log.w(TAG, "Default icon style returned null.");
            }
            imageOffset = new Offset(
                    gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, oIconStyle.getOffSetX(), // x offset
                    gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, 1.0 - (oIconStyle.getOffSetY() / imageBounds.height())); // y offset
            oAttr = PlacemarkAttributes.createWithImage(ImageSource.fromBitmap(imageInfo.getImage())).setImageOffset(imageOffset);
        } else {
            if (oIconStyle != null) {
                imageOffset = new Offset(
                        gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, oIconStyle.getOffSetX(), // x offset
                        gov.nasa.worldwind.WorldWind.OFFSET_PIXELS, oIconStyle.getOffSetY() * -1); // y offset
            } else {
                imageOffset = new Offset(
                        gov.nasa.worldwind.WorldWind.OFFSET_FRACTION, 0, // x offset
                        gov.nasa.worldwind.WorldWind.OFFSET_FRACTION, 1.0); // y offset
            }
            oAttr = PlacemarkAttributes.createWithImage(ImageSource.fromUrl(sURL)).setImageOffset(imageOffset);
        }

        oAttr.setImageScale(dScale);

        Placemark oIcon = new Placemark(
            Position.fromDegrees(oPos.getLatitude(), oPos.getLongitude(), oPos.getAltitude()),
            oAttr);
        
        oIcon.setPickDelegate(oPoint);

        this.storeFeature(oPoint, oIcon, bVisible, removeRenderablesList);

        if (bVisible) {
            renderableList.add(oIcon);
        }
    }

    private  void createWWPath(mil.emp3.api.Path feature, gov.nasa.worldwind.shape.Path wwPath, IGeoStrokeStyle strokeStyle) {
        java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = wwPath.getPositions();
        gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = wwPath.getAttributes();

        if (shapeAttribute == null) {
            shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
        }

        if (wwPositionList == null) {
            wwPositionList = new java.util.ArrayList<>();
        }

        try {
            Conversion.convertToWWPositionList(feature.getPositions(), wwPositionList);
        } catch (EMP_Exception ex) {
            Log.e(TAG, "CoordinateConvertion.convertToWWPositionList fail.", ex);
            return;
        }

        shapeAttribute.setDrawVerticals(false);
        shapeAttribute.setDrawInterior(false);
        if (strokeStyle != null) {
            shapeAttribute.setDrawOutline(true);
            Conversion.covertColor(strokeStyle.getStrokeColor(), shapeAttribute.getOutlineColor());
            shapeAttribute.setOutlineWidth((float) strokeStyle.getStrokeWidth());

            // shapeAttribute.setOutlineStippleFactor(MapEngineUtils.getStippleFactor(strokeStyle)); TODO
            // shapeAttribute.setOutlineStipplePattern(MapEngineUtils.getStipplePattern(strokeStyle)); TODO
        } else if (feature.getStrokeStyle() != null) {
            strokeStyle = feature.getStrokeStyle();

            shapeAttribute.setDrawOutline(true);
            Conversion.covertColor(strokeStyle.getStrokeColor(), shapeAttribute.getOutlineColor());
            shapeAttribute.setOutlineWidth((float) strokeStyle.getStrokeWidth());

            // shapeAttribute.setOutlineStippleFactor(MapEngineUtils.getStippleFactor(strokeStyle)); TODO
            // shapeAttribute.setOutlineStipplePattern(MapEngineUtils.getStipplePattern(strokeStyle)); TODO
        } else {
            shapeAttribute.setDrawOutline(false);
        }

        Conversion.convertAndSetIGeoAltitudeMode(wwPath, feature.getAltitudeMode());

        if (feature.getAltitudeMode() == IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND){
            wwPath.setFollowTerrain(true);
        } else {
            wwPath.setFollowTerrain(false);
        }
        wwPath.setPickDelegate(feature);
        wwPath.setPositions(wwPositionList);
        wwPath.setAttributes(shapeAttribute);
        wwPath.setHighlighted(false);
    }

    private  void createWWPolygon(mil.emp3.api.Polygon feature, gov.nasa.worldwind.shape.Polygon wwPolygon, IGeoStrokeStyle strokeStyle) {
        java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = wwPolygon.getBoundary(0);
        gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = wwPolygon.getAttributes();

        if (shapeAttribute == null) {
            shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
        }

        if (wwPositionList == null) {
            wwPositionList = new java.util.ArrayList<>();
        }

        try {
            Conversion.convertToWWPositionList(feature.getPositions(), wwPositionList);
        } catch (EMP_Exception ex) {
            Log.e(TAG, "CoordinateConvertion.convertToWWPositionList fail.", ex);
            return;
        }

        shapeAttribute.setDrawVerticals(false);
        if (strokeStyle != null) {
            shapeAttribute.setDrawOutline(true);
            Conversion.covertColor(strokeStyle.getStrokeColor(), shapeAttribute.getOutlineColor());
            shapeAttribute.setOutlineWidth((float) strokeStyle.getStrokeWidth());
            // shapeAttribute.setOutlineStippleFactor(MapEngineUtils.getStippleFactor(strokeStyle)); TODO
            // shapeAttribute.setOutlineStipplePattern(MapEngineUtils.getStipplePattern(strokeStyle)); TODO
        } else if (feature.getStrokeStyle() != null) {
            strokeStyle = feature.getStrokeStyle();

            shapeAttribute.setDrawOutline(true);
            Conversion.covertColor(strokeStyle.getStrokeColor(), shapeAttribute.getOutlineColor());
            shapeAttribute.setOutlineWidth((float) strokeStyle.getStrokeWidth());
            // shapeAttribute.setOutlineStippleFactor(MapEngineUtils.getStippleFactor(strokeStyle)); TODO
            // shapeAttribute.setOutlineStipplePattern(MapEngineUtils.getStipplePattern(strokeStyle)); TODO
        } else {
            shapeAttribute.setDrawOutline(false);
        }

        if (feature.getFillStyle() != null) {
            IGeoFillStyle fillStyle = feature.getFillStyle();

            shapeAttribute.setDrawInterior(true);
            Conversion.covertColor(fillStyle.getFillColor(), shapeAttribute.getInteriorColor());
        } else {
            shapeAttribute.setDrawInterior(false);
        }

        Conversion.convertAndSetIGeoAltitudeMode(wwPolygon, feature.getAltitudeMode());

        if (feature.getAltitudeMode() == IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND){
            wwPolygon.setFollowTerrain(true);
        } else {
            wwPolygon.setFollowTerrain(false);
        }
        wwPolygon.setPickDelegate(feature);
        wwPolygon.setBoundary(0, wwPositionList);
        wwPolygon.setAttributes(shapeAttribute);
        wwPolygon.setHighlighted(false);
    }

    private void plotPath(mil.emp3.api.Path feature, boolean bVisible, java.util.List<Renderable> renderableList,
            java.util.List<Renderable> removeRenderablesList) {
        gov.nasa.worldwind.shape.Path wwPath = null;
        IGeoStrokeStyle selectedStrokeStyle = null;

        if (feature.getPositions().size() < 2) {
            Log.i(TAG, "Path feature (" + feature.getName() + ") with less than 2 points.");
            return;
        }

        if (this.featureHash.containsKey(feature.getGeoId())) {
            FeatureRenderableMapping oMapping = this.featureHash.get(feature.getGeoId());
            oMapping.setVisible(bVisible);
            java.util.List<Renderable> featureRenderableList = oMapping.getRenderable();

            if (featureRenderableList.size() > 0) {
                wwPath = (gov.nasa.worldwind.shape.Path) featureRenderableList.get(0);
            }

            if (oMapping.isSelected()) {
                selectedStrokeStyle = this.empResources.getSelectedStrokeStyle(this);
            }
        }

        if (wwPath == null) {
            java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = new java.util.ArrayList<>();
            gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
            wwPath = new gov.nasa.worldwind.shape.Path(wwPositionList, shapeAttribute);
        }

        this.createWWPath(feature, wwPath, selectedStrokeStyle);

        this.storeFeature(feature, wwPath, bVisible, removeRenderablesList);
        if (bVisible) {
            renderableList.add(wwPath);
        }
    }

    private void plotPolygon(mil.emp3.api.Polygon feature, boolean bVisible, java.util.List<Renderable> renderableList,
            java.util.List<Renderable> removeRenderablesList) {
        gov.nasa.worldwind.shape.Polygon wwPolygon = null;
        IGeoStrokeStyle selectedStrokeStyle = null;

        if (feature.getPositions().size() < 2) {
            Log.i(TAG, "Path feature (" + feature.getName() + ") with less than 2 points.");
            return;
        }

        if (this.featureHash.containsKey(feature.getGeoId())) {
            FeatureRenderableMapping oMapping = this.featureHash.get(feature.getGeoId());
            oMapping.setVisible(bVisible);
            java.util.List<Renderable> featureRenderableList = oMapping.getRenderable();

            if (featureRenderableList.size() > 0) {
                wwPolygon = (gov.nasa.worldwind.shape.Polygon) featureRenderableList.get(0);
            }

            if (oMapping.isSelected()) {
                selectedStrokeStyle = this.empResources.getSelectedStrokeStyle(this);
            }
        }

        if (wwPolygon == null) {
            java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = new java.util.ArrayList<>();
            gov.nasa.worldwind.shape.ShapeAttributes shapeAttribute = new gov.nasa.worldwind.shape.ShapeAttributes();
            wwPolygon = new gov.nasa.worldwind.shape.Polygon(wwPositionList, shapeAttribute);
        }

        this.createWWPolygon(feature, wwPolygon, selectedStrokeStyle);

        this.storeFeature(feature, wwPolygon, bVisible, removeRenderablesList);
        if (bVisible) {
            renderableList.add(wwPolygon);
        }
    }

    private void plotText(mil.emp3.api.Text feature, boolean bVisible, java.util.List<Renderable> renderableList,
            java.util.List<Renderable> removeRenderablesList) {
        gov.nasa.worldwind.shape.Label wwLabel = null;
        IGeoLabelStyle selectedLabelStyle = null;
        IGeoLabelStyle labelStyle;
        gov.nasa.worldwind.geom.Position textPosition;
        gov.nasa.worldwind.shape.TextAttributes textAttribute;

        if (feature.getPositions().size() < 1) {
            Log.i(TAG, "Path feature (" + feature.getName() + ") with no points.");
            return;
        }

        if (this.featureHash.containsKey(feature.getGeoId())) {
            FeatureRenderableMapping oMapping = this.featureHash.get(feature.getGeoId());
            oMapping.setVisible(bVisible);
            java.util.List<Renderable> featureRenderableList = oMapping.getRenderable();

            if (featureRenderableList.size() > 0) {
                wwLabel = (gov.nasa.worldwind.shape.Label) featureRenderableList.get(0);
            }

            if (oMapping.isSelected()) {
                selectedLabelStyle = this.empResources.getSelectedLabelStyle(this);
            }
        }

        IGeoPosition textFeaturePosition = feature.getPosition();
        if (wwLabel == null) {
            textPosition = new gov.nasa.worldwind.geom.Position();
            textAttribute = new gov.nasa.worldwind.shape.TextAttributes();
        } else {
            textPosition = wwLabel.getPosition();
            textAttribute = wwLabel.getAttributes();
            wwLabel.setText(feature.getText());
        }

        labelStyle = feature.getLabelStyle();
        textPosition.set(textFeaturePosition.getLatitude(), textFeaturePosition.getLongitude(), textFeaturePosition.getAltitude());

        gov.nasa.worldwind.geom.Offset textOffset;

        if (null != selectedLabelStyle) {
            textAttribute.setTextColor(Conversion.covertColor(selectedLabelStyle.getColor()));
        }

        if (null != labelStyle) {
            if ((null == selectedLabelStyle) && (null != labelStyle.getColor())) {
                textAttribute.setTextColor(Conversion.covertColor(labelStyle.getColor()));
            }

            switch (labelStyle.getJustification()) {
                case LEFT:
                    textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_FRACTION, 0.5);
                    break;
                case RIGHT:
                    textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_INSET_PIXELS, 0, WorldWind.OFFSET_FRACTION, 0.5);
                    break;
                case CENTER:
                default:
                    textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_FRACTION, 0.5, WorldWind.OFFSET_FRACTION, 0.5);
                    break;
            }
        } else {
            textOffset = new gov.nasa.worldwind.geom.Offset(WorldWind.OFFSET_PIXELS, 0, WorldWind.OFFSET_FRACTION, 0.5);
        }
        textAttribute.setTextOffset(textOffset);
        textAttribute.setTextSize(feature.getFontSize());

        switch (feature.getTypeFaceStyle()) {
            case BOLD:
                textAttribute.setTypeface(Typeface.create(feature.getFontFamily(), Typeface.BOLD));
                break;
            case ITALIC:
                textAttribute.setTypeface(Typeface.create(feature.getFontFamily(), Typeface.ITALIC));
                break;
            case BOLD_ITALIC:
                textAttribute.setTypeface(Typeface.create(feature.getFontFamily(), Typeface.BOLD_ITALIC));
                break;
            case NORMAL:
            default:
                textAttribute.setTypeface(Typeface.create(feature.getFontFamily(), Typeface.NORMAL));
                break;
        }

        if (null == wwLabel) {
            wwLabel = new gov.nasa.worldwind.shape.Label(textPosition, feature.getText(), textAttribute);
        }
        Conversion.convertAndSetIGeoAltitudeMode(wwLabel, feature.getAltitudeMode());
        wwLabel.setPickDelegate(feature);

        this.storeFeature(feature, wwLabel, bVisible, removeRenderablesList);
        if (bVisible) {
            renderableList.add(wwLabel);
        }
    }

    private void plotFeature(IFeature feature, boolean bVisible,
            java.util.List<Renderable> renderableList,
            java.util.List<Renderable> removeRenderablesList) {
        switch (feature.getFeatureType()) {
            case GEO_CIRCLE:
                break;
            case GEO_ELLIPSE:
                break;
            case GEO_PATH:
                this.plotPath((mil.emp3.api.Path) feature, bVisible, renderableList, removeRenderablesList);
                break;
            case GEO_POINT:
                this.plotPoint((mil.emp3.api.Point) feature, bVisible, renderableList, removeRenderablesList);
                break;
            case GEO_POLYGON:
                this.plotPolygon((mil.emp3.api.Polygon) feature, bVisible, renderableList, removeRenderablesList);
                break;
            case GEO_RECTANGLE:
                break;
            case GEO_SQUARE:
                break;
            case GEO_MIL_SYMBOL:
                this.plotMilStdSymbol(feature, bVisible, renderableList, removeRenderablesList);
                break;
            case GEO_ACM:
                break;
            case GEO_TEXT:
                this.plotText((mil.emp3.api.Text) feature, bVisible, renderableList, removeRenderablesList);
                break;
        }
    }

    /**
     * Assumption here is that core software (Storage Manager has taken care of thread safety. So we will not need a lock
     * here. View is updated on UI thread from renderableList and removeRenderableList
     *
     * @param features
     */
    @Override
    public void addFeatures(final FeatureVisibilityList features) {
        final java.util.List<Renderable> renderableList = new java.util.ArrayList<>();
        final java.util.List<Renderable> removeRenderablesList = new java.util.ArrayList<>();

        for (FeatureVisibility featureVisibility: features) {
            MapInstance.this.plotFeature(featureVisibility.feature, featureVisibility.visible, renderableList, removeRenderablesList);
            generateFeatureAddedEvent(featureVisibility.feature);
        }

        // Update the view on UI thread.
        if (!SystemUtils.isCurrentThreadUIThread()) {
            /*
             * SEE HANDLER NOTES ABOVE.
             */
            handler.post(new Runnable() {

                @Override
                public void run() {
                    for(Renderable item: removeRenderablesList) {
                        oRenderableLayer.removeRenderable(item);
                    }
                    for(Renderable item: renderableList ) {
                        oRenderableLayer.addRenderable(item);
                    }
                    ww.requestRedraw();
                }
            });
        } else {
            for(Renderable item: removeRenderablesList) {
                oRenderableLayer.removeRenderable(item);
            }
            for(Renderable item : renderableList ) {
                oRenderableLayer.addRenderable(item);
            }
            ww.requestRedraw();
        }
    }

    private void removeFeature(java.util.UUID uniqueId, java.util.List<Renderable> removeRenderableList) {
        if (this.featureHash.containsKey(uniqueId)) {
            FeatureRenderableMapping oWrapper = this.featureHash.remove(uniqueId);;

            if (oWrapper != null) {
                if (oWrapper instanceof MilStd2525TacticalGraphic) {
                    removeRenderableList.add((MilStd2525TacticalGraphic) oWrapper);
                } else {
                    removeRenderableList.addAll(oWrapper.getRenderable());
                }
                generateFeatureRemovedEvent(oWrapper.getFeature());
            }
        }
    }

    /**
     * Assumption here is that core software (Storage Manager has taken care of thread safety. So we will not need a lock
     * here. View is updated from removeRenderableList
     *
     * @param features
     */
    @Override
    public void removeFeatures(final IUUIDSet features) {
        final java.util.List<Renderable> removeRenderableList = new java.util.ArrayList<>();
        for (java.util.UUID uniqueId: features) {
            this.removeFeature(uniqueId, removeRenderableList);
        }

        // Update the view on UI thread.
        if (!SystemUtils.isCurrentThreadUIThread()) {
            /*
             * SEE HANDLER NOTES ABOVE.
             */
            handler.post(new Runnable() {

                @Override
                public void run() {
                    for(Renderable item: removeRenderableList) {
                        if (item instanceof MilStd2525TacticalGraphic) {
                            MapInstance.this.oTacticalGraphicLayer.removeRenderable((MilStd2525TacticalGraphic) item);
                        } else {
                            oRenderableLayer.removeRenderable(item);
                        }
                    }
                    ww.requestRedraw();
                }
            });
        } else {
            for(Renderable item: removeRenderableList) {
                if (item instanceof MilStd2525TacticalGraphic) {
                    this.oTacticalGraphicLayer.removeRenderable((MilStd2525TacticalGraphic) item);
                } else {
                    oRenderableLayer.removeRenderable(item);
                }
            }
            ww.requestRedraw();
        }
    }

    private void addWMSService(IWMS wms) {
        WmsLayer layer;
        WmsLayerConfig wmsConfig;
        String sLayers = "";
        String sStyles = "";
        int iLayerIndex;

        for (String Str: wms.getLayers()) {
            if (sLayers.length() > 0) {
                sLayers += ",";
            }
            sLayers += Str;
        }

        if (wms.getStyles() != null) {
            for (String Str : wms.getStyles()) {
                if (sStyles.length() > 0) {
                    sStyles += ",";
                }
                sStyles += Str;
            }
        }

        // If its an update we need to remove the old one.
        if (this.wmsHash.containsKey(wms.getGeoId())) {
            layer = this.wmsHash.get(wms.getGeoId());
            iLayerIndex = ww.getLayers().indexOfLayer(layer);
            ww.getLayers().removeLayer(layer);
        } else {
            iLayerIndex = ww.getLayers().indexOfLayer(this.oTacticalGraphicLayer);
        }

        wmsConfig = new WmsLayerConfig();
        wmsConfig.serviceAddress = wms.getURL().toString();
        wmsConfig.transparent = wms.getTransaparent();
        wmsConfig.wmsVersion = wms.getWMSVersion().toString();
        wmsConfig.layerNames = sLayers;
        if (wms.getCoordinateSystem() != null) {
            wmsConfig.coordinateSystem = wms.getCoordinateSystem();
        }
        if (sStyles.length() > 0) {
            wmsConfig.styleNames = sStyles;
        }
        if (wms.getTimeString() != null) {
            wmsConfig.timeString = wms.getTimeString();
        }

        double metersPerPixel = 10;
        if (wms.getLayerResolution() != 0) {
            metersPerPixel = wms.getLayerResolution();
        }
        layer = new WmsLayer(new Sector().setFullSphere(), metersPerPixel, wmsConfig);
        layer.setDetailControl(1.0);
        this.wmsHash.put(wms.getGeoId(), layer);
        ww.getLayers().addLayer(iLayerIndex, layer);
        ww.requestRedraw();
    }

    @Override
    public void addMapService(final IMapService mapService) {
        if (mapService instanceof IWMS) {
            if (!SystemUtils.isCurrentThreadUIThread()) {
                /*
                 * SEE HANDLER NOTES ABOVE.
                 */
                handler.post(new Runnable(){

                    @Override
                    public void run() {
                        MapInstance.this.addWMSService((IWMS) mapService);
                    }
                });
            } else {
                this.addWMSService((IWMS) mapService);
            }
        }
    }

    private void removeMapServiceEx(IMapService wmsService) {
        WmsLayer layer;

        if (this.wmsHash.containsKey(wmsService.getGeoId())) {
            layer = this.wmsHash.get(wmsService.getGeoId());
            ww.getLayers().removeLayer(layer);
            this.wmsHash.remove(wmsService.getGeoId());
            ww.requestRedraw();
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
                    MapInstance.this.removeMapServiceEx(wmsService);
                }
            });
        } else {
            this.removeMapServiceEx(wmsService);
        }
    }

    @Override
    public ICamera getCamera() {
        return this.oMapViewController.getCamera();
    }
    
    @Override
    public void setCamera(ICamera oCamera, boolean animate) {
        this.oMapViewController.setCamera(oCamera, animate);
    }

    @Override
    public ILookAt getLookAt() {
        return this.oMapViewController.getLookAt();
    }

    @Override
    public void setLookAt(ILookAt oLookAt, boolean animate) { this.oMapViewController.setLookAt(oLookAt, animate); }

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
    public ICapture getCapture() {
        return null;
    }

    public void registerMilStdRenderer(IMilStdRenderer oRenderer) {
        super.registerMilStdRenderer(oRenderer);
        MilStd2525LevelOfDetailSelector.initInstance(oRenderer, this);
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
    class WorldWindow  extends gov.nasa.worldwind.WorldWindow {
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

    @Override
    public IGeoBounds getMapBounds() {
        return this.oMapViewController.getBounds();
    }

    @Override
    public Point geoToContainer(IGeoPosition pos) {
        Point result = new Point();
        if (mapController.groundPositionToScreenPoint(pos.getLatitude(), pos.getLongitude(), result)) {
            return result;
        }
        return null;
    }

    @Override
    public IGeoPosition containerToGeo(Point point) {
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
        final java.util.List<Renderable> renderableList = new java.util.ArrayList<>();
        final java.util.List<Renderable> removeRenderablesList = new java.util.ArrayList<>();

        for (IFeature feature: featureList) {
            if (this.featureHash.containsKey(feature.getGeoId())) {
                FeatureRenderableMapping oMapping = this.featureHash.get(feature.getGeoId());
                oMapping.setSelected(selected);
                MapInstance.this.plotFeature(feature, oMapping.isVisible(), renderableList, removeRenderablesList);
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
                    for(Renderable item: removeRenderablesList) {
                        oRenderableLayer.removeRenderable(item);
                    }
                    for(Renderable item: renderableList ) {
                        oRenderableLayer.addRenderable(item);
                    }
                    ww.requestRedraw();
                }
            });
        } else {
            for(Renderable item: removeRenderablesList) {
                oRenderableLayer.removeRenderable(item);
            }
            for(Renderable item : renderableList ) {
                oRenderableLayer.addRenderable(item);
            }
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

    private void plotMilStdSymbol(IFeature feature, boolean bVisible, java.util.List<Renderable> renderableList,
            java.util.List<Renderable> removeRenderablesList) {
        MilStdSymbol oSymbol = (MilStdSymbol) feature;

        if (oSymbol.isSinglePoint()) {
            this.plotSinglePointSymbol(oSymbol, bVisible, renderableList, removeRenderablesList);
        } else {
            this.plotTacticalGraphicSymbol(oSymbol, bVisible);
        }
    }

    public WorldWindow getWW() {
        return this.ww;
    }
}
