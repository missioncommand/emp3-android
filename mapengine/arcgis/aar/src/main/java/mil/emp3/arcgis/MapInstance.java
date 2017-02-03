package mil.emp3.arcgis;

import android.content.Context;

import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.esri.android.map.GraphicsLayer;
import com.esri.android.map.MapOnTouchListener;
import com.esri.android.map.MapOptions;
import com.esri.android.map.MapView;
import com.esri.android.map.ags.ArcGISTiledMapServiceLayer;
import com.esri.android.map.event.OnPanListener;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.map.event.OnZoomListener;
import com.esri.core.geometry.CoordinateConversion;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.SpatialReference;

import mil.emp3.api.Camera;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MapViewEventEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.events.CameraEvent;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.interfaces.IWMS;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.arcgis.plotters.IRenderer;
import mil.emp3.arcgis.plotters.RendererFactory;
import mil.emp3.arcgis.util.FeatureGraphicMapper;
import mil.emp3.mapengine.CoreMapInstance;
import mil.emp3.mapengine.api.Capabilities;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.api.Requirements;
import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.mapengine.interfaces.IEmpResources;
import mil.emp3.mapengine.interfaces.IMapEngineProperties;
import mil.emp3.mapengine.interfaces.IMapEngineRequirements;

import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.mapengine.interfaces.ISetVisibilityList;
import mil.emp3.mapengine.listeners.MapInstanceFeatureAddedEventListener;
import mil.emp3.mapengine.listeners.MapInstanceFeatureRemovedEventListener;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;


/**
 * This class implements the ARCGIS map engine
 *
 * NOTE: THIS CLASS IS NOT COMPLETE NOR DOES IT SUPPORT ALL THE REQUIRED FUNCTIONALITY. IT WAS USED AS A SURROGATE UNTIL
 * NASA WW WAS MADE AVAILABLE. IT IS MAINTAINED HERE TO SHOW MULTI MAP ENGINE CAPABILITY AND SWAP MAP ENGINE CAPABILITY
 */
public class MapInstance extends CoreMapInstance implements ICameraEventListener {

    private static final String TAG = MapInstance.class.getSimpleName();

    MapView mapView = null;
    ArcGISTiledMapServiceLayer tiledMapServiceLayer;

    private Context context;
    private AttributeSet attributeSet;
    private int currentZoom = 16;

    private java.util.HashMap<java.util.UUID, com.esri.android.map.ogc.WMSLayer> wmsHash;
    private java.util.HashMap<java.util.UUID, FeatureGraphicMapper> featureHash;


    // The current Camera.
    private ICamera currentCamera;
    private ILookAt currentLookAt;
    private EventListenerHandle hCameraListener = null;
    private boolean bMapChangedCamera = false;

    private Capabilities oCapabilities;
    private Requirements oRequirements;

    public MapInstance(Context context, AttributeSet attrs, IEmpResources resources) {
        super(TAG, resources);
        Log.e(TAG, "constructor context attributes");

        this.context = context;
        this.attributeSet = attributeSet;
        initMapEngine();
    }

    public void initMapEngine() {

        FeatureTypeEnum[] aFeatureTypes = new FeatureTypeEnum[]{
                //FeatureTypeEnum.GEO_ACM,
                //FeatureTypeEnum.GEO_CIRCLE,
                //FeatureTypeEnum.GEO_ELLIPSE,
                //FeatureTypeEnum.GEO_MIL_SYMBOL,
                FeatureTypeEnum.GEO_PATH,
                FeatureTypeEnum.GEO_POINT,
                //FeatureTypeEnum.GEO_POLYGON,
                //FeatureTypeEnum.GEO_RECTANGLE,
                FeatureTypeEnum.GEO_SQUARE
                //FeatureTypeEnum.GEO_TEXT
        };
        WMSVersionEnum[] aVersions = new WMSVersionEnum[] {
            WMSVersionEnum.VERSION_1_1,
            WMSVersionEnum.VERSION_1_3
        };

        this.currentCamera = new mil.emp3.api.Camera();
        this.currentLookAt = new mil.emp3.api.LookAt();
        try {
            this.hCameraListener = this.currentCamera.addCameraEventListener(this);
        } catch (EMP_Exception e) {}
        this.wmsHash = new java.util.HashMap<>();
        this.featureHash = new java.util.HashMap<>();

        this.oCapabilities = new Capabilities(aFeatureTypes, aVersions);
        this.oRequirements = new Requirements(false); // For now set it to false.
    }

    @Override
    public View getMapInstanceAndroidView() {
        if (null == mapView) onCreateView();
        return mapView;
    }

    @Override
    public void onDestroy() {
        java.util.UUID oUniqueId;
        this.generateStateChangeEvent(MapStateEnum.SHUTDOWN_IN_PROGRESS);

        while (!this.wmsHash.isEmpty()) {
            oUniqueId = (java.util.UUID) this.wmsHash.keySet().toArray()[0];
            com.esri.android.map.ogc.WMSLayer oLayer = this.wmsHash.get(oUniqueId);
            this.wmsHash.remove(oUniqueId);
        }

        while (!this.featureHash.isEmpty()) {
            oUniqueId = (java.util.UUID) this.featureHash.keySet().toArray()[0];
            this.removeFeature(oUniqueId);
        }

        this.generateStateChangeEvent(MapStateEnum.SHUTDOWN);
    }

    private void generateViewChangeEvent(MapViewEventEnum eNewView) {

            double dNorth = -90.0;
            double dSouth = 90.0;
            double dWest = 180.0;
            double dEast = -180.0;
            com.esri.core.geometry.Point oVertex;
            IGeoBounds oBounds = new GeoBounds();
            com.esri.core.geometry.Polygon oPolygon= mapView.getExtent();
            int iMaxIndex = oPolygon.getPointCount();
            
            oPolygon = (com.esri.core.geometry.Polygon) GeometryEngine.project(oPolygon, mapView.getSpatialReference(), mapView.getSpatialReference().getGCS());
            
            for (int iIndex = 0; iIndex < iMaxIndex; iIndex++) {
                oVertex = oPolygon.getPoint(iIndex);
                if (oVertex.getY() > dNorth) {
                    dNorth = oVertex.getY();
                }
                
                if (oVertex.getY() < dSouth) {
                    dSouth = oVertex.getY();
                }
                
                if (oVertex.getX() < dWest) {
                    dWest = oVertex.getX();
                }
                
                if (oVertex.getX() > dEast) {
                    dEast = oVertex.getX();
                }
            }
            
            oBounds.setNorth(dNorth);
            oBounds.setSouth(dSouth);
            oBounds.setEast(dEast);
            oBounds.setWest(dWest);
            
            Log.d(TAG, "ESRI view change: " + eNewView);
            MapInstanceViewChangeEvent event = new MapInstanceViewChangeEvent(this, eNewView, currentCamera,
                    currentLookAt, oBounds, mapView.getWidth(), mapView.getHeight());
            generateViewChangeEvent(event);
    }

    @Override
    public View onCreateView() {
        // final MapInstance oThis = this;
        MapOptions options = new MapOptions(MapOptions.MapType.TOPO, 40.2171, -74.7429,
                currentZoom);
        mapView = new MapView(context, options);

        tiledMapServiceLayer = new com.esri.android.map.ags.ArcGISTiledMapServiceLayer("http://services.arcgisonline.com/ArcGIS/rest/services/World_Street_Map/MapServer");
        //tiledMapServiceLayer.setOnStatusChangedListener(new OnStatusChangedListener() {
            /*
             * This callback method will be invokes when status of layer
             * changes
             */
            //public void onStatusChanged(Object arg0, STATUS status) {
						/*
						 * Check if layer's new status = INITIALIZED. If it is,
						 * initialize UI elements
						 */
            //    if (status == OnStatusChangedListener.STATUS.INITIALIZED) {
            //        generateStateChangeEvent(MapStateEnum.MAP_READY);
            //    }
            //}
        //});

        mapView.addLayer(tiledMapServiceLayer);
        StateManager.getInstance().setGraphicsLayer(new GraphicsLayer());
        mapView.addLayer(StateManager.getInstance().getGraphicsLayer());
        StateManager.getInstance().setMapView(mapView);

        // Set a single tap listener on the MapView.
/*
        mapView.setOnSingleTapListener(new OnSingleTapListener() {

            public void onSingleTap(float x, float y) {
                Log.d(TAG, "onSingleTap RS " + x + "-" + y);
                com.esri.core.geometry.Point p1 = new com.esri.core.geometry.Point(x, y);
                Log.d(TAG, "onSingleTap RS lat/Lon " + CoordinateConversion.pointToDecimalDegrees(p1, mapView.getSpatialReference(), 6));
                // When a single tap gesture is received, reset the map to its default rotation angle,
                // where North is shown at the top of the device.

                mapView.centerAt(x, y, false);
                mapView.setRotationAngle(0);
                generateViewChangeEvent(MapViewEventEnum.VIEW_MOTION_STOPPED);
            }
        });
*/
        // Set a listener for map status changes; this will be called when switching basemaps.
        mapView.setOnStatusChangedListener(new OnStatusChangedListener() {

            @Override
            public void onStatusChanged(Object source, STATUS status) {

                // MAP_NEW, INIT_IN_PROGRESS, MAP_SWAP_IN_PROGRESS, MAP_READY, SHUTDOWN_IN_PROGRESS, SHUTDOWN;
                switch (status) {
                    case INITIALIZATION_FAILED:
                        break;
                    case INITIALIZED:
                        generateStateChangeEvent(MapStateEnum.MAP_READY);
                        break;
                    case LAYER_LOADED:
                        //generateStateChangeEvent(MapStateEnum.MAP_NEW);
                        break;
                    case LAYER_LOADING_FAILED:
                        break;
                }
           }
        });
        mapView.setOnZoomListener(new OnZoomListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void postAction (float pivotX, float pivotY, double factor) {
                // According to the documentation this should fire after a zoom
                if (currentCamera == null) {
                    currentCamera = new mil.emp3.api.Camera();
                }
                com.esri.core.geometry.Point mapPoint = mapView.getCenter();
                SpatialReference spacRef = SpatialReference.create(SpatialReference.WKID_WGS84);

                com.esri.core.geometry.Point ltLn = (com.esri.core.geometry.Point) GeometryEngine.project(mapPoint, mapView.getSpatialReference(), spacRef);
                currentCamera.setLongitude(ltLn.getX());
                currentCamera.setLatitude(ltLn.getY());
                currentCamera.setAltitude(ltLn.getZ());
                
                generateViewChangeEvent(MapViewEventEnum.VIEW_MOTION_STOPPED);
            }

            @Override
            public void preAction (float pivotX, float pivotY, double factor) {
                
            }
        });

        mapView.setOnPanListener(new OnPanListener() {
            @Override
            public void prePointerMove(float fromX, float fromY, float toX, float toY) {
                Log.d(TAG, "OnPanListener pre move ");
            }

            @Override
            public void postPointerMove(float fromX, float fromY, float toX, float toY) {
                Log.d(TAG, "OnPanListener post move to x:" + toX + " y:" + toY);
                //IGeoPosition newPosition = new GeoPosition();
                //newPosition.setLatitude((double)toX);
                //newPosition.setLongitude((double)toY);
                //generateUserInteractionEvent(UserInteractionEventEnum.CLICKED,
                //        null, newPosition);
            }

            @Override
            public void prePointerUp(float fromX, float fromY, float toX, float toY) {
                Log.d(TAG, "OnPanListener pre up ");
            }

            @Override
            public void postPointerUp(float fromX, float fromY, float toX, float toY) {
                Log.d(TAG, "OnPanListener post up to x:" + toX + " y:" + toY);
                //IGeoPosition newPosition = new GeoPosition();
                //newPosition.setLatitude((double)toX);
                //newPosition.setLongitude((double)toY);
                //generateUserInteractionEvent(UserInteractionEventEnum.CLICKED,
                //        null, newPosition);
            }
        });
        return mapView;
    }

    @Override
    public IMapEngineProperties getMapEngineProperties() {
        return null;
    }

    @Override
    public void setVisibility(ISetVisibilityList visibilityList) {
    }

    @Override
    public void addFeatures(FeatureVisibilityList features) {
        IRenderer renderer;
        com.esri.core.map.Graphic oGraphic;
        
        for(FeatureVisibility fv : features) {
            try {
                renderer = RendererFactory.getInstance().getRenderer(fv.feature);
                
                if (renderer != null) {
                    oGraphic = renderer.buildGraphic(fv.feature);
                    
                    if (oGraphic != null) {
                        GraphicsManager.getInstance().add(featureHash, new FeatureGraphicMapper(fv.feature, oGraphic));
                    } else {
                        // The build failed we should do something about it.
                        Log.e(TAG, "ERROR Couldn't build graphic for " + fv.feature.getFeatureType() + fv.feature.getDescription());
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception processing " + fv.feature.getClass().getCanonicalName());
            }
        }
    }

    private void removeFeature(java.util.UUID uniqueId) {
       GraphicsManager.getInstance().remove(featureHash, uniqueId);
    }

    @Override
    public void removeFeatures(IUUIDSet features) {
        GraphicsManager.getInstance().remove(featureHash, features);
    }

    @Override
    public ICamera getCamera() {
        ICamera cam = new Camera();
        cam.setLatitude(currentCamera.getLatitude());
        cam.setLongitude(currentCamera.getLongitude());
        cam.setAltitude(currentCamera.getAltitude());
        cam.setAltitudeMode(currentCamera.getAltitudeMode());
        cam.setHeading(currentCamera.getHeading());
        cam.setTilt(currentCamera.getTilt());
        cam.setRoll(currentCamera.getRoll());
        return cam;
    }

    private void moveCamera(ICamera oCamera) {
        //mapView.setCameraDistance((float)oCamera.getAltitude());
        mapView.centerAt(oCamera.getLatitude(), oCamera.getLongitude(), false);
        mapView.refreshDrawableState();
        //generateViewChangeEvent(MapViewEventEnum.VIEW_MOTION_STOPPED);
    }

    @Override
    public void onEvent(CameraEvent event) {
        if (!bMapChangedCamera) {
            if (this.currentCamera.getGeoId().compareTo(event.getCamera().getGeoId()) == 0) {
                this.moveCamera(event.getCamera());
            }
        }
    }

    @Override
    public void setCamera(ICamera oCamera, boolean animate) {
        // VIEW_IN_MOTION, VIEW_MOTION_STOPPED;
        this.moveCamera(oCamera);
        this.currentCamera = oCamera;

        // Set the event listener.
        //if (hCameraListener != null) {
        //    oCamera.removeEventListener(hCameraListener);
        //}
        //hCameraListener = oCamera.addCameraEventListener(this);
        //generateViewChangeEvent(MapViewEventEnum.VIEW_MOTION_STOPPED);
        mapView.invalidate();
    }

    @Override
    public void setLookAt(ILookAt oLookAt, boolean animate) {

    }

    @Override
    public ILookAt getLookAt() {
        return null;
    }

    class MyTouchListener extends MapOnTouchListener {
        public MyTouchListener(Context context, MapView view) {
            super(context, view);
        }

        @Override
        public boolean onSingleTap(MotionEvent point) {
            Log.d(TAG, "onSingleTap " + point.getX() + "-" + point.getY());
            com.esri.core.geometry.Point p1 = new com.esri.core.geometry.Point(point.getX(), point.getY());
            Log.d(TAG, "onSingleTap lat/Lon " + CoordinateConversion.pointToDecimalDegrees(p1, mapView.getSpatialReference(), 6));
            Point p = new Point((int)point.getX(), (int)point.getY());
            generateMapUserInteractionEvent(UserInteractionEventEnum.CLICKED, null, null, p, null, null );
            return false;
        }
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }

    private void addWMS(IWMS wmsService) {
        com.esri.android.map.ogc.WMSLayer layer;

        // If its an update we need to remove the old one.
        if (this.wmsHash.containsKey(wmsService.getGeoId())) {
            layer = this.wmsHash.get(wmsService.getGeoId());
            mapView.removeLayer(layer);
        }

        layer = new com.esri.android.map.ogc.WMSLayer(wmsService.getURL().toString());
        layer.setImageFormat(wmsService.getTileFormat());
        layer.setTransparent(wmsService.getTransaparent());
        if (wmsService.getLayers().size() > 0) {
            layer.setVisibleLayer(wmsService.getLayers().toArray(new String[wmsService.getLayers().size()]));
        }

        this.wmsHash.put(wmsService.getGeoId(), layer);
        mapView.addLayer(layer);
    }

    @Override
    public void addMapService(IMapService mapService) {
        if (mapService instanceof IWMS) {
            this.addWMS((IWMS) mapService);
        }
    }

    @Override
    public void removeMapService(IMapService mapService) {
        com.esri.android.map.ogc.WMSLayer layer;

        if (this.wmsHash.containsKey(mapService.getGeoId())) {
            layer = this.wmsHash.get(mapService.getGeoId());
            this.wmsHash.remove(mapService.getGeoId());
            mapView.removeLayer(layer);
        }
    }

    @Override
    public void setBounds(IGeoBounds bounds) {
    }

    @Override
    public void setMotionLockMode(MapMotionLockEnum mode) {
    }

    private class Capture implements mil.emp3.api.interfaces.ICapture {
        private final android.graphics.Bitmap oBitmap;

        public Capture(android.graphics.Bitmap bitmap) {
            this.oBitmap = bitmap;
        }

        @Override
        public android.graphics.Bitmap screenshot() {
            return this.oBitmap;
        }
    }

    @Override
    public Capabilities getCapabilities() {
        return null;
    }

    @Override
    public ICapture getCapture() {
        return null;
    }

    @Override
    public IMapEngineRequirements getRequirements() {
        return null;
    }

    @Override
    public void registerMilStdRenderer(IMilStdRenderer oRenderer) {

    }

    /**
     * This method sets the threshold of the distances beyond which the map displays MilStd single point
     * icons as dots.
     * @param dValue Distance in meters.
     */
    @Override
    public void setFarDistanceThreshold(double dValue) {

    }

    /**
     * This method sets the threshold of the distances beyond which the map displays MilStd single point
     * icons with no modifiers. Icons at distances less than this threshold get displayed as fully qualified icons
     * as per the label setting.
     * @param dValue Distance in meters.
     */
    @Override
    public void setMidDistanceThreshold(double dValue) {

    }

    @Override
    public void addMapInstanceFeatureAddedEventListener(MapInstanceFeatureAddedEventListener listener) {

    }

    @Override
    public void addMapInstanceFeatureRemovedEventListener(MapInstanceFeatureRemovedEventListener listener) {

    }

    @Override
    public double getFieldOfView() {
        return 0.0;
    }

    @Override
    public int getViewHeight() {
        return mapView.getHeight();
    }

    @Override
    public int getViewWidth() {
        return mapView.getWidth();
    }

    /**
     * Fetch Version information from BuildConfig
     * @param builder
     * @param buildConfigFields
     */
    @Override
    public void getVersionInformation(StringBuilder builder, List<String> buildConfigFields) {
        getVersionInformation(builder, buildConfigFields, "mil.emp3.arcgisapk.BuildConfig");
    }

    @Override
    public IGeoBounds getMapBounds() {
        return null;
    }

    @Override
    public Point geoToScreen(IGeoPosition pos) {
        return null;
    }

    @Override
    public IGeoPosition screenToGeo(Point point) {
        return null;
    }

    @Override
    public Point geoToContainer(IGeoPosition pos) {
        return null;
    }

    @Override
    public IGeoPosition containerToGeo(Point point) {
        return null;
    }

    @Override
    public void selectFeatures(List<IFeature> featureList) {

    }

    @Override
    public void deselectFeatures(List<IFeature> featureList) {

    }
}
