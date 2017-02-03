package mil.emp3.api;

import android.app.Activity;
import android.app.Fragment;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mil.emp3.api.abstracts.MapViewFragmentBase;
import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.enums.Property;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ICapture;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IContainerSet;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.core.storage.IClientMapRestoreData;
import mil.emp3.api.interfaces.core.storage.IClientMapToMapInstance;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IContainerEventListener;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFeatureDrawEventListener;
import mil.emp3.api.listeners.IFeatureEditEventListener;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.listeners.IMapFeatureAddedEventListener;
import mil.emp3.api.listeners.IMapFeatureRemovedEventListener;
import mil.emp3.api.listeners.IMapFreehandEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;
import mil.emp3.api.listeners.IVisibilityEventListener;
import mil.emp3.api.utils.EmpPropertyList;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.api.view.utils.TypeArrayParser;
import mil.emp3.mapengine.CoreMapInstance;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.view.R;

/**
 * This class implements a map as an android fragment. An object of this type must be
 * used to add a map in the activity layout.
 * 
 */
public class MapFragment extends Fragment implements IMap {
    private static final String TAG = MapFragment.class.getSimpleName();

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final private IEventManager eventManager     = ManagerFactory.getInstance().getEventManager();
    
    private MapViewFragmentBase oClientMap;
    private FrameLayout mapContainer;

    public MapFragment() {
    }

    /*
     * ANDROID FRAGMENT LIFECYCLE SECTION
     */
    @Override
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        Log.d(TAG, "MapFragment onInflate");

        if((null == activity) || (null == attrs)) {
            Log.e(TAG, "onInflate activity and attrs must be non-null");
            return;
        }
        TypedArray a = activity.obtainStyledAttributes(attrs, R.styleable.EMP3Fragment);
        try {
            IEmpPropertyList propList = new EmpPropertyList();
            String temp = TypeArrayParser.getStringAttribute(a, R.styleable.EMP3Fragment_map_engine_name);

            if (temp != null) {
                propList.put(Property.ENGINE_CLASSNAME.getValue(), temp);
            }

            temp = TypeArrayParser.getStringAttribute(a, R.styleable.EMP3Fragment_map_engine_apk_name);
            if (temp != null) {
                propList.put(Property.ENGINE_APKNAME.getValue(), temp);
            }

            temp = TypeArrayParser.getStringAttribute(a, R.styleable.EMP3Fragment_map_name);
            if (temp != null) {
                propList.put("MAPNAME", temp);
            }

            MirrorCacheModeEnum tempMode = TypeArrayParser.getMirrorCacheModeAttribute(a, R.styleable.EMP3Fragment_mirrorCache_mode);
            if (tempMode != null) {
                propList.put(Property.MIRROR_CACHE_MODE.getValue(), tempMode);
            }

            propList.put("TAG", TAG);

            this.oClientMap = new MapViewFragmentBase(propList);
            //oClientMap.getStyledAttributes(a);
            super.onInflate(activity, attrs, savedInstanceState);
        } finally {
            a.recycle();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (this.oClientMap == null) {
            IEmpPropertyList propList = new EmpPropertyList();

            propList.put("TAG", TAG);
            this.oClientMap = new MapViewFragmentBase(propList);
        }

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate()");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView()");
        mapContainer = new FrameLayout(inflater.getContext());
        if(null == oClientMap.getEngineClassName()) {
            Log.d(TAG, "No Engine Class Name specified, return a dummy view");
            Log.d(TAG, "Application must call swapMapEngine to change this to a Map");
        } else {
            mapContainer.addView(oClientMap.onCreateView(inflater.getContext(), null));
        }
        return mapContainer;
    }

    // Following should really be called on CoreManager ...
    @Override
    public void onResume() {
        super.onResume();
        
        IMapInstance mapInstance = storageManager.getMapInstance(this);
        
        Log.d(TAG, "onResume()");
        
        if(null != mapInstance) {
            mapInstance.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        IMapInstance mapInstance = storageManager.getMapInstance(this);
        Log.d(TAG, "onPause()");

        if (null != mapInstance) {
            mapInstance.onPause();
        }
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy()");

        IMapInstance mapInstance = storageManager.getMapInstance(this);
        if (null != mapInstance) {
            mapInstance.onDestroy();
        }
        this.oClientMap.onDestroy();
        super.onDestroy();
    }

    // IMap implementation

    @Override
    public void setMirrorCacheMode(MirrorCacheModeEnum mode) {
        this.oClientMap.setMirrorCacheMode(mode);
    }

    @Override
    public boolean isEgressEnabled() {
        return this.oClientMap.isEgressEnabled();
    }

    @Override
    public boolean isIngressEnabled() {
        return this.oClientMap.isIngressEnabled();
    }

    @Override
    public MapStateEnum getState() {
        return this.oClientMap.getState();
    }

    @Override
    public ICapture getScreenCapture() {
        return this.oClientMap.getScreenCapture();
    }

    @Override
    public ICamera getCamera() {
        return this.oClientMap.getCamera();
    }

    @Override
    public void setCamera(ICamera camera, boolean animate) throws EMP_Exception {
        this.oClientMap.setCamera(camera, animate);
    }

    @Override
    public ILookAt getLookAt() {
        return this.oClientMap.getLookAt();
    }

    @Override
    public void setLookAt(ILookAt lookAt, boolean animate) throws EMP_Exception {
        this.oClientMap.setLookAt(lookAt, animate);
    }

    @Override
    public java.util.List<IFeature> getAllFeatures() {
        return this.oClientMap.getAllFeatures();
    }

    @Override
    public java.util.List<IOverlay> getAllOverlays() {
        return this.oClientMap.getAllOverlays();
    }

    @Override
    public void addOverlay(IOverlay overlay, boolean visible) throws EMP_Exception {
        this.oClientMap.addOverlay(overlay, visible);
    }

    @Override
    public void addOverlays(java.util.List<IOverlay> overlays, boolean visible) throws EMP_Exception {
        this.oClientMap.addOverlays(overlays, visible);
    }

    @Override
    public void removeOverlay(IOverlay overlay) throws EMP_Exception {
        this.oClientMap.removeOverlay(overlay);
    }

    @Override
    public void removeOverlays(java.util.List<IOverlay> overlays) throws EMP_Exception {
        this.oClientMap.removeOverlays(overlays);
    }

    @Override
    public void addMapService(IMapService mapService) throws EMP_Exception {
        this.oClientMap.addMapService(mapService);
    }

    @Override
    public void removeMapService(IMapService mapService) throws EMP_Exception {
        this.oClientMap.removeMapService(mapService);
    }

    @Override
    public void setVisibility(IContainer target, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(target, actionEnum);
    }

    @Override
    public void setVisibility(IContainerSet targetList, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetList, actionEnum);
    }

    @Override
    public void setVisibility(java.util.UUID targetId, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetId, actionEnum);
    }

    @Override
    public void setVisibility(IUUIDSet targetIdList, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetIdList, actionEnum);
    }

    @Override
    public void setVisibility(IContainer target, IContainer parent, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(target, parent, actionEnum);
    }

    @Override
    public void setVisibility(java.util.UUID targetId, java.util.UUID parentId, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetId, parentId, actionEnum);
    }
    
    @Override
    public VisibilityStateEnum getVisibility(IContainer target) {
        return this.oClientMap.getVisibility(target);
    }
    
    @Override
    public VisibilityStateEnum getVisibility(IContainer target, IContainer parent) {
        return this.oClientMap.getVisibility(target, parent);
    }

    @Override
    public EventListenerHandle addMapStateChangeEventListener(IMapStateChangeEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_STATE_CHANGE_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addMapViewChangeEventListener(IMapViewChangeEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_VIEW_CHANGE_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addMapInteractionEventListener(IMapInteractionEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_INTERACTION_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addMapFeatureAddedEventListener(IMapFeatureAddedEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_FEATURE_ADDED_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addMapFeatureRemovedEventListener(IMapFeatureRemovedEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_FEATURE_REMOVED_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addCameraEventListener(ICameraEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.CAMERA_EVENT_LISTENER, this, listener);
    }

    @Override
    public void setIconSize(IconSizeEnum eSize) throws EMP_Exception {
        this.oClientMap.setIconSize(eSize);
    }

    @Override
    public IconSizeEnum getIconSize() {
        return this.oClientMap.getIconSize();
    }

    @Override
    public void setMilStdLabels(MilStdLabelSettingEnum labelSetting) throws EMP_Exception {
        this.oClientMap.setMilStdLabels(labelSetting);
    }

    @Override
    public MilStdLabelSettingEnum getMilStdLabels() {
        return this.oClientMap.getMilStdLabels();
    }

    /**
     * This method will swap the current map instance with a new one. It is assumed
     * the following properties exist:
     * <ul>
     * <li>engine.className - The class name of the new map instance. must be non-null
     * <li>engine.apkName - The property values needed to support map swapping.
     * </ul>
     *
     * @param properties The property values needed to support map swapping.
     * @return The new android.view.View object for the new map instance.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    @Override
    public IMap swapMapEngine(IEmpPropertyList properties) throws EMP_Exception {
        Log.d(TAG, "swapMapEngine");

        /*
         * If map is currently in edit, draw, or freehand mode, we cancel the swap attempt.
         */
        final IClientMapToMapInstance cm2mInstance = storageManager.getMapMapping(oClientMap);
        if (cm2mInstance != null && cm2mInstance.isEditing()) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "The map is currently in edit/draw/freehand mode. No map swap will occur.");
        }

        final String sEngineClassName;
        final String sAPKName;

        if (properties.get(Property.ENGINE_CLASSNAME.getValue()) instanceof String) {
            sEngineClassName = (String) properties.get(Property.ENGINE_CLASSNAME.getValue());
        } else {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Property not valid: " + Property.ENGINE_CLASSNAME.getValue());
        }

        if (properties.get(Property.ENGINE_APKNAME.getValue()) instanceof String) {
            sAPKName = (String) properties.get(Property.ENGINE_APKNAME.getValue());
        } else {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Property not valid: " + Property.ENGINE_APKNAME.getValue());
        }

        /*
         * Fire event: MAP_SWAP_IN_PROGRESS
         */
        final IMapInstance mapEngine = storageManager.getMapInstance(oClientMap);
        if (mapEngine instanceof CoreMapInstance) {
            ((CoreMapInstance) mapEngine).generateStateChangeEvent(MapStateEnum.MAP_SWAP_IN_PROGRESS);
        }

        /*
         * Perform the map swap. Queued to run on UI thread if not already
         */
        if (Looper.myLooper() != Looper.getMainLooper()) {
            final Handler mainHandler = new Handler(mapContainer.getContext().getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    mapContainer.removeAllViews();
                    try {
                        mapContainer.addView(MapFragment.this.oClientMap.swapMapEngineInternal(sEngineClassName, sAPKName, mapContainer.getContext()));
                    } catch (EMP_Exception e) {
                        Log.e(TAG, "swapMapEngine " + sEngineClassName + ";" + sAPKName, e);
                    }
                }
            });

        } else {
            mapContainer.removeAllViews();
            mapContainer.addView(this.oClientMap.swapMapEngineInternal(sEngineClassName, sAPKName, mapContainer.getContext()));
        }

        return this;
    }

    @Override
    public void editFeature(IFeature oFeature) throws EMP_Exception {
        this.oClientMap.editFeature(oFeature);
    }

    @Override
    public void cancelEdit() throws EMP_Exception {
        this.oClientMap.cancelEdit();
    }

    @Override
    public void completeEdit() throws EMP_Exception {
        this.oClientMap.completeEdit();
    }

    @Override
    public java.util.List<IContainer> getParents() {
        return this.oClientMap.getParents();
    }

    @Override
    public void clearContainer() throws EMP_Exception {
        this.oClientMap.clearContainer();
    }

    @Override
    public EventListenerHandle addContainerEventListener(IContainerEventListener listener) throws EMP_Exception {
        return this.oClientMap.addContainerEventListener(listener);
    }

    @Override
    public EventListenerHandle addVisibilityEventListener(IVisibilityEventListener listener) throws EMP_Exception {
        return this.oClientMap.addVisibilityEventListener(listener);
    }

    @Override
    public EventListenerHandle addFeatureEventListener(IFeatureEventListener listener) throws EMP_Exception {
        return this.oClientMap.addFeatureEventListener(listener);
    }

    @Override
    public EventListenerHandle addFeatureInteractionEventListener(IFeatureInteractionEventListener listener) throws EMP_Exception {
        return this.oClientMap.addFeatureInteractionEventListener(listener);
    }

    @Override
    public EventListenerHandle addFeatureEditEventListener(IFeatureEditEventListener listener) throws EMP_Exception {
        return this.oClientMap.addFeatureEditEventListener(listener);
    }

    @Override
    public void removeEventListener(EventListenerHandle hListener) {
        this.oClientMap.removeEventListener(hListener);
    }

    // Container overrides.
    @Override
    public boolean hasChildren() {
        return this.oClientMap.hasChildren();
    }

    @Override
    public java.util.List<IGeoBase> getChildren() {
        return this.oClientMap.getChildren();
    }

    @Override
    public void setReadOnly(boolean b) {
        this.oClientMap.setReadOnly(b);
    }

    @Override
    public boolean getReadOnly() {
        return this.oClientMap.getReadOnly();
    }

    /**
     * On activity restart, if EMP3 middleware is responsible for restoring map data then we use the map name
     * to correlate old and new maps. NOTE this is required because of multiple map support. Applications must set name
     * on client map for restoration feature to work.
     *
     * This code is repeated in mapView.
     */
    @Override
    public void setName(String string) {
        this.oClientMap.setName(string);
        try {

            // If this is the first time for this client map then an entry is created.
            // If this is not the first time then entry is updated, specifically geoId/UUID
            IClientMapRestoreData cmrd = storageManager.addMapNameToRestoreDataMapping(this);
            Log.d(TAG, "setName " + cmrd.getEngineClassName() + " " + cmrd.getEngineApkName());

            // If this is not the first time then we will find the engine name and start the engine.
            if (null != cmrd.getEngineClassName()) {

                final IEmpPropertyList properties = new EmpPropertyList();
                properties.put(Property.ENGINE_CLASSNAME.getValue(), cmrd.getEngineClassName());
                properties.put(Property.ENGINE_APKNAME.getValue(), cmrd.getEngineApkName());

                swapMapEngine(properties);
            }
        } catch (EMP_Exception e) {
            Log.e(TAG, "addMapNameToRestoreDataMapping failed ", e);
        }
    }

    @Override
    public String getName() {
        return this.oClientMap.getName();
    }

    @Override
    public void setGeoId(java.util.UUID uuid) {
        this.oClientMap.setGeoId(uuid);
    }

    @Override
    public java.util.UUID getGeoId() {
        return this.oClientMap.getGeoId();
    }

    @Override
    public void setDataProviderId(String s) {
        this.oClientMap.setDataProviderId(s);
    }

    @Override
    public String getDataProviderId() {
        return this.oClientMap.getDataProviderId();
    }

    @Override
    public void setDescription(String string) {
        this.oClientMap.setDescription(string);
    }

    @Override
    public String getDescription() {
        return this.oClientMap.getDescription();
    }

    @Override
    public HashMap<String, Object> getProperties() {
        return this.oClientMap.getProperties();
    }

    @Override
    public List<IMapService> getMapServices() {
        return this.oClientMap.getMapServices();
    }

    @Override
    public EventListenerHandle addMapFreehandEventListener(IMapFreehandEventListener listener) throws EMP_Exception {
        return this.oClientMap.addMapFreehandEventListener(listener);
    }

    @Override
    public EventListenerHandle addFeatureDrawEventListener(IFeatureDrawEventListener listener) throws EMP_Exception {
        return this.oClientMap.addFeatureDrawEventListener(listener);
    }

    @Override
    public void drawFeature(IFeature oFeature) throws EMP_Exception {
        this.oClientMap.drawFeature(oFeature);
    }

    @Override
    public void cancelDraw() throws EMP_Exception {
        this.oClientMap.cancelDraw();
    }

    @Override
    public void completeDraw() throws EMP_Exception {
        this.oClientMap.completeDraw();
    }

    @Override
    public void drawFreehand(IGeoStrokeStyle initialStyle) throws EMP_Exception {
        this.oClientMap.drawFreehand(initialStyle);
    }

    @Override
    public void setFreehandStyle(IGeoStrokeStyle style) throws EMP_Exception {
        this.oClientMap.setFreehandStyle(style);
    }

    @Override
    public void drawFreehandExit() throws EMP_Exception {
        this.oClientMap.drawFreehandExit();
    }

    @Override
    public void zoomTo(IFeature feature, boolean animate) { this.oClientMap.zoomTo(feature, animate); }

    @Override
    public void zoomTo(List<IFeature> featureList, boolean animate) { this.oClientMap.zoomTo(featureList, animate); }

    @Override
    public void zoomTo(IOverlay overlay, boolean animate) { this.oClientMap.zoomTo(overlay, animate); }

    @Override
    public void setBounds(IGeoBounds bounds, boolean animate) { this.oClientMap.setBounds(bounds, animate); }

    @Override
    public IGeoBounds getBounds() {
        return this.oClientMap.getBounds();
    }

    @Override
    public void setMotionLockMode(MapMotionLockEnum mode) throws EMP_Exception {
        this.oClientMap.setMotionLockMode(mode);
    }

    @Override
    public MapMotionLockEnum getMotionLockMode() throws EMP_Exception {
        return this.oClientMap.getMotionLockMode();
    }

    @Override
    public EditorMode getEditorMode() throws EMP_Exception {
        return this.oClientMap.getEditorMode();
    }

    @Override
    public void setFarDistanceThreshold(double dValue) {
        this.oClientMap.setFarDistanceThreshold(dValue);
    }

    @Override
    public double getFarDistanceThreshold() {
        return this.oClientMap.getFarDistanceThreshold();
    }

    @Override
    public void setMidDistanceThreshold(double dValue) {
        this.oClientMap.setMidDistanceThreshold(dValue);
    }

    @Override
    public double getMidDistanceThreshold() {
        return this.oClientMap.getMidDistanceThreshold();
    }

    /**
     * This method marks the feature as selected on the map. If the feature is already mark selected
     * no action is taken.
     *
     * @param feature The feature to mark selected.
     */
    @Override
    public void selectFeature(IFeature feature) {
        this.oClientMap.selectFeature(feature);
    }

    @Override
    public void selectFeatures(java.util.List<IFeature> features) {
        this.oClientMap.selectFeatures(features);
    }

    /**
     * This method marks the feature as NOT selected on the map. If the feature is not mark selected
     * no action is taken.
     *
     * @param feature The feature to deselected.
     */
    @Override
    public void deselectFeature(IFeature feature) {
        this.oClientMap.deselectFeature(feature);
    }

    @Override
    public void deselectFeatures(java.util.List<IFeature> features) {
        this.oClientMap.deselectFeatures(features);
    }

    /**
     * This method retrieves the list of feature that are marked selected on the map.
     *
     * @return A list of IFeatures. If there are no features selected the list is empty.
     */
    @Override
    public List<IFeature> getSelected() {
        return this.oClientMap.getSelected();
    }

    /**
     * This method clears the map selected list.
     */
    @Override
    public void clearSelected() {
        this.oClientMap.clearSelected();
    }

    @Override
    public boolean isSelected(IFeature feature) {
        return this.oClientMap.isSelected(feature);
    }

    @Override
    public Point geoToScreen(IGeoPosition pos)  throws EMP_Exception {
        return this.oClientMap.geoToScreen(pos);
    }

    @Override
    public IGeoPosition screenToGeo(Point point) throws EMP_Exception {
        return this.oClientMap.screenToGeo(point);
    }

    @Override
    public Point geoToContainer(IGeoPosition pos) throws EMP_Exception {
        return this.oClientMap.geoToContainer(pos);
    }

    @Override
    public IGeoPosition containerToGeo(Point point) throws EMP_Exception {
        return this.oClientMap.containerToGeo(point);
    }

    @Override
    public void editFeature(IFeature oFeature, IEditEventListener listener) throws EMP_Exception {
        this.oClientMap.editFeature(oFeature, listener);
    }

    @Override
    public void drawFeature(IFeature oFeature, IDrawEventListener listener) throws EMP_Exception {
        this.oClientMap.drawFeature(oFeature, listener);
    }

    @Override
    public void drawFreehand(IGeoStrokeStyle initialStyle, IFreehandEventListener listener) throws EMP_Exception {
        this.oClientMap.drawFreehand(initialStyle, listener);
    }
}
