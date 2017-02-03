package mil.emp3.openstreet;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;


import java.util.LinkedList;
import java.util.List;


import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.WMS;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.mapengine.CoreMapInstance;
import mil.emp3.mapengine.api.Capabilities;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.mapengine.interfaces.IEmpResources;
import mil.emp3.mapengine.interfaces.IMapEngineProperties;
import mil.emp3.mapengine.interfaces.IMapEngineRequirements;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.mapengine.interfaces.ISetVisibilityList;
import mil.emp3.mapengine.listeners.MapInstanceFeatureAddedEventListener;
import mil.emp3.mapengine.listeners.MapInstanceFeatureRemovedEventListener;
import mil.emp3.openstreet.plotters.RendererFactory;
import mil.emp3.openstreet.utils.SystemUtils;


/**
 *  This class implements glue code for OPENSTREET MAP ENGINE map engine
 *
 * NOTE: THIS CLASS IS NOT COMPLETE NOR DOES IT SUPPORT ALL THE REQUIRED FUNCTIONALITY. IT WAS USED AS A SURROGATE UNTIL
 * NASA WW WAS MADE AVAILABLE. IT IS MAINTAINED HERE TO SHOW MULTI MAP ENGINE CAPABILITY AND SWAP MAP ENGINE CAPABILITY
 */
public class MapInstance extends CoreMapInstance {

    private static final String TAG = MapInstance.class.getSimpleName();
    private EventManager eventManager;
    private MapController mapController;
    private MapView mapView;
    private Context context;
    private AttributeSet attributeSet;

    public MapInstance(Context context, AttributeSet attributeSet, IEmpResources resources) {
        super(TAG, resources);
        this.context = context;
        this.attributeSet = attributeSet;
    }

    @Override
    public View getMapInstanceAndroidView() {
        if(null == mapView) onCreateView();
        return mapView;
    }

    @Override
    public IMapEngineProperties getMapEngineProperties() {
        return null;
    }

    @Override
    public Capabilities getCapabilities() {
        return null;
    }

    @Override
    public ICamera getCamera() {
        return null;
    }

    @Override
    public void setCamera(ICamera iCamera, boolean animate) {

    }

    @Override
    public void setLookAt(ILookAt oLookAt, boolean animate) {

    }

    @Override
    public ILookAt getLookAt() {
        return null;
    }

    @Override
    public void setVisibility(ISetVisibilityList visibilityList) {

    }

    @Override
    public void addFeatures(final FeatureVisibilityList features) {

        if (!SystemUtils.isCurrentThreadUIThread()) {
            mapView.post(new Runnable() {

                @Override
                public void run() {
                    for(FeatureVisibility fv : features) {
                        try {
                            RendererFactory.getInstance().getRenderer(fv.feature).render(fv.feature);
                        } catch (Exception e) {
                            Log.e(TAG, "Exception processing " + fv.feature.getClass().getCanonicalName(), e);
                        }
                    }
                }
            });
        } else {
            for(FeatureVisibility fv : features) {
                try {
                    RendererFactory.getInstance().getRenderer(fv.feature).render(fv.feature);
                } catch (Exception e) {
                    Log.e(TAG, "Exception processing " + fv.feature.getClass().getCanonicalName(), e);
                }
            }
        }
    }

    private void removeFeature(java.util.UUID uniqueId) {

        StateManager.getInstance().remove(uniqueId);
    }

    @Override
    public void removeFeatures(final IUUIDSet features) {

        if (!SystemUtils.isCurrentThreadUIThread()) {
            mapView.post(new Runnable() {

                @Override
                public void run() {
                    for (java.util.UUID uniqueId : features) {
                        removeFeature(uniqueId);
                    }
                }
            });
        } else {
            for (java.util.UUID uniqueId : features) {
                removeFeature(uniqueId);
            }
        }
    }

    /** Related to Android Life Cycle
     * @return  
     */

    @Override
    public View onCreateView() {

        mapView = new MapView(context, attributeSet);
        eventManager = new EventManager(this, mapView, mapController);

        mapView.setTileSource(TileSourceFactory.DEFAULT_TILE_SOURCE);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);
        mapController = (MapController) mapView.getController();
        mapController.setZoom(15);
        // GeoPoint point2 = new GeoPoint(51496994, -134733);
        GeoPoint point2 = new GeoPoint(40.2171, -74.7429);
        mapController.setCenter(point2);

        mapView.addOnFirstLayoutListener(new MapView.OnFirstLayoutListener() {
            @Override
            public void onFirstLayout(View v, int left, int top, int right, int bottom) {
                Log.d(TAG, "In onFirstLayout");

                generateStateChangeEvent(MapStateEnum.MAP_READY);
                Log.d(TAG, "Raised MAP_READY Event");
            }
        });

        StateManager.getInstance().setMapView(mapView);

        // This is a simple test to check if OSM resources are accessible after dynamic class load

        ItemizedIconOverlay overlay = new ItemizedIconOverlay(context, new LinkedList(), null);
//        OverlayItem item = new OverlayItem("title", "snippet", new GeoPoint(40.7127, -74.0059));
//        overlay.addItem(item);
//        mapView.getOverlays().add(overlay);
        StateManager.getInstance().setOverlay(overlay);

        // milStdSymbolRenderer = MilStdSymbolRenderer.getInstance(context, mapView);
        // if(null == milStdSymbolRenderer) return null;
        return mapView;
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public IMapEngineRequirements getRequirements() {
        return null;
    }

    @Override
    public ICapture getCapture() {
        return null;
    }

    @Override
    public void addMapService(IMapService mapService) {

    }

    @Override
    public void removeMapService(IMapService mapService) {

    }

    @Override
    public void setBounds(IGeoBounds bounds) {

    }

    @Override
    public void setMotionLockMode(MapMotionLockEnum mode) {

    }

    @Override
    public void registerMilStdRenderer(IMilStdRenderer misStdRenderer) {

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
        getVersionInformation(builder, buildConfigFields, "mil.emp3.openstreetapk.BuildConfig");
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
