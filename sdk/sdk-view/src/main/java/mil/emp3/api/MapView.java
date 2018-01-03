package mil.emp3.api;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Point;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.HashMap;
import java.util.List;

import mil.emp3.api.abstracts.MapViewFragmentBase;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MapGridTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.enums.Property;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IContainerSet;
import mil.emp3.api.interfaces.IEmpPropertyList;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IMapServiceResult;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.IScreenCaptureCallback;
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
import mil.emp3.mapengine.abstracts.CoreMapInstance;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.view.R;

/**
 * This class implements a map as an android FrameLayout.
 *
 */
public class MapView extends FrameLayout implements IMap {
    private final static String TAG = MapView.class.getSimpleName();

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final private IEventManager eventManager     = ManagerFactory.getInstance().getEventManager();
    
    private MapViewFragmentBase oClientMap;

    public MapView(Context context, AttributeSet set) {
        super(context, set);

        init(context, set);
    }

    private void initAttributes(Context context, AttributeSet set) {
        if((null == context) || (null == set)) {
            Log.e(TAG, "initAttributes context and set must be non-null");
            return;
        }

        TypedArray a = context.obtainStyledAttributes(set, R.styleable.EMP3Fragment);
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
            propList.put("CONTEXT", context);

            this.oClientMap = new MapViewFragmentBase(propList);
            //oClientMap.getStyledAttributes(a);
        } finally {
            a.recycle();
        }
    }

    private void init(Context context, AttributeSet set) {
        initAttributes(context, set);
        if(null == oClientMap.getEngineClassName()) {
            Log.d(TAG, "No Engine Class Specified, No Map will be created");
        } else {
            addView(onCreateView(context));
        }
    }

    public View onCreateView(Context context) {
        Log.d(TAG, "onCreateView()");
        return oClientMap.onCreateView(context, null);
    }

    @Override
    protected void onDetachedFromWindow() {
        //oClientMap.onDestroy();
        super.onDetachedFromWindow();
    }

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
    public void getScreenCapture(IScreenCaptureCallback callback) {
        this.oClientMap.getScreenCapture(callback);
    }

    @Override
    public ICamera getCamera() {
        return this.oClientMap.getCamera();
    }

    @Override
    public void setCamera(ICamera camera, boolean animate, Object userContext) throws EMP_Exception {
        this.oClientMap.setCamera(camera, animate, userContext);
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
    public void setLookAt(ILookAt lookAt, boolean animate, Object userContext) throws EMP_Exception {
        this.oClientMap.setLookAt(lookAt, animate, userContext);
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
    public void addOverlay(IOverlay overlay, boolean visible, Object userContext) throws EMP_Exception {
        this.oClientMap.addOverlay(overlay, visible, userContext);
    }

    @Override
    public void addOverlays(java.util.List<IOverlay> overlays, boolean visible, Object userContext) throws EMP_Exception {
        this.oClientMap.addOverlays(overlays, visible, userContext);
    }

    @Override
    public void removeOverlay(IOverlay overlay, Object userContext) throws EMP_Exception {
        this.oClientMap.removeOverlay(overlay, userContext);
    }

    @Override
    public void removeOverlays(java.util.List<IOverlay> overlays, Object userContext) throws EMP_Exception {
        this.oClientMap.removeOverlays(overlays, userContext);
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
    public void addMapService(IMapService mapService, IMapServiceResult result) throws EMP_Exception {
        this.oClientMap.addMapService(mapService, result);
    }

    @Override
    public void removeMapService(IMapService mapService, IMapServiceResult result) throws EMP_Exception {
        this.oClientMap.removeMapService(mapService, result);
    }

    @Override
    public void setVisibility(IContainer target, VisibilityActionEnum actionEnum, Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(target, actionEnum, userContext);
    }

    @Override
    public void setVisibility(IContainerSet targetList, VisibilityActionEnum actionEnum, Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetList, actionEnum, userContext);
    }

    @Override
    public void setVisibility(java.util.UUID targetId, VisibilityActionEnum actionEnum, Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetId, actionEnum, userContext);
    }

    @Override
    public void setVisibility(IUUIDSet targetIdList, VisibilityActionEnum actionEnum, Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetIdList, actionEnum, userContext);
    }

    @Override
    public void setVisibility(IContainer target, IContainer parent, VisibilityActionEnum actionEnum, Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(target, parent, actionEnum, userContext);
    }

    @Override
    public void setVisibility(java.util.UUID targetId, java.util.UUID parentId, VisibilityActionEnum actionEnum, Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetId, parentId, actionEnum, userContext);
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
    public void setIconSize(IconSizeEnum eSize, Object userContext) throws EMP_Exception {
        this.oClientMap.setIconSize(eSize, userContext);
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
    public void setMilStdLabels(MilStdLabelSettingEnum labelSetting, Object userContext) throws EMP_Exception {
        this.oClientMap.setMilStdLabels(labelSetting, userContext);
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
            final Handler mainHandler = new Handler(this.getContext().getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    removeAllViews();
                    try {
                        MapView.this.addView(MapView.this.oClientMap.swapMapEngineInternal(sEngineClassName, sAPKName));
                    } catch (EMP_Exception e) {
                        Log.e(TAG, "swapMapEngine " + sEngineClassName + ";" + sAPKName, e);
                    }
                }
            });

        } else {
            removeAllViews();
            addView(this.oClientMap.swapMapEngineInternal(sEngineClassName, sAPKName));
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
    public void clearContainer(Object userContext) throws EMP_Exception {
        this.oClientMap.clearContainer(userContext);
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
     * Please look in MapFragment for the comments.
     */
    @Override
    public void setName(String string) {
        this.oClientMap.setName(string);
        try {
            IClientMapRestoreData cmrd = storageManager.addMapNameToRestoreDataMapping(this);
            Log.d(TAG, "setName " + cmrd.getEngineClassName() + " " + cmrd.getEngineApkName());

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
    public HashMap<String, String> getProperties() {
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

    @Override
    public void setHighDetailThreshold(int threshold) {
        this.oClientMap.setHighDetailThreshold(threshold);
    }

    @Override
    public void setMidDetailThreshold(int threshold) {
        this.oClientMap.setMidDetailThreshold(threshold);
    }

    @Override
    public int getHighDetailThreshold() {
        return this.oClientMap.getHighDetailThreshold();
    }

    @Override
    public int getMidDetailThreshold() {
        return this.oClientMap.getMidDetailThreshold();
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

    @Deprecated
    @Override
    public IGeoPosition containerToGeo(Point point) throws EMP_Exception {
        return this.oClientMap.containerToGeo(point);
    }

    @Override
    public IGeoPosition containerToGeo(PointF point) throws EMP_Exception {
        return this.oClientMap.containerToGeo(point);
    }

    /**
     * This method marks the feature as selected on the map. If the feature is already mark selected
     * no action is taken.
     *
     * @param feature The feature to mark selected.
     */
    @Override
    public void selectFeature(IFeature feature, Object userContext) {
        this.oClientMap.selectFeature(feature, userContext);
    }

    @Override
    public void selectFeatures(java.util.List<IFeature> features, Object userContext) {
        this.oClientMap.selectFeatures(features, userContext);
    }

    /**
     * This method marks the feature as NOT selected on the map. If the feature is not mark selected
     * no action is taken.
     *
     * @param feature The feature to deselected.
     */
    @Override
    public void deselectFeature(IFeature feature, Object userContext) {
        this.oClientMap.deselectFeature(feature, userContext);
    }

    @Override
    public void deselectFeatures(java.util.List<IFeature> features, Object userContext) {
        this.oClientMap.deselectFeatures(features, userContext);
    }

    @Override
    public void selectFeature(IFeature feature) {
        this.oClientMap.selectFeature(feature);
    }

    @Override
    public void selectFeatures(java.util.List<IFeature> features) {
        this.oClientMap.selectFeatures(features);
    }

    @Override
    public void deselectFeature(IFeature feature) {
        this.oClientMap.deselectFeature(feature);
    }

    @Override
    public void deselectFeatures(java.util.List<IFeature> features) {
        this.oClientMap.deselectFeatures(features);
    }

    @Override
    public List<IFeature> getSelected() {
        return this.oClientMap.getSelected();
    }

    /**
     * This method clears the map selected list.
     */
    @Override
    public void clearSelected(Object userContext) {
        this.oClientMap.clearSelected(userContext);
    }

    @Override
    public void clearSelected() {
        this.oClientMap.clearSelected();
    }

    @Override
    public boolean isSelected(IFeature feature) {
        return this.oClientMap.isSelected(feature);
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

    @Override
    public FontSizeModifierEnum getFontSizeModifier() {
        return this.oClientMap.getFontSizeModifier();
    }

    @Override
    public void setFontSizeModifier(FontSizeModifierEnum value)
            throws EMP_Exception {
        this.oClientMap.setFontSizeModifier(value);
    }

    @Override
    public boolean containsProperty(String propertyName) {
        return this.oClientMap.containsProperty(propertyName);
    }

    @Override
    public String getProperty(String propertyName) {
        return this.oClientMap.getProperty(propertyName);
    }

    @Override
    public void setProperty(String propertyName, String propertyValue) {
        this.oClientMap.setProperty(propertyName, propertyValue);
    }

    @Override
    public void removeProperty(String propertyName) {
        this.oClientMap.removeProperty(propertyName);
    }

    @Override
    public boolean getBooleanProperty(String propertyName) {
        return this.oClientMap.getBooleanProperty(propertyName);
    }

    @Override
    public int getIntegerProperty(String propertyName) {
        return this.oClientMap.getIntegerProperty(propertyName);
    }

    @Override
    public float getFloatProperty(String propertyName) {
        return this.oClientMap.getFloatProperty(propertyName);
    }

    @Override
    public double getDoubleProperty(String propertyName) {
        return this.oClientMap.getDoubleProperty(propertyName);
    }

    @Override
    public void setBackgroundBrightness(int setting) {
        this.oClientMap.setBackgroundBrightness(setting);
    }

    @Override
    public int getBackgroundBrightness() {
        return this.oClientMap.getBackgroundBrightness();
    }
    
    @Override
    public View showMiniMap() {
        return this.oClientMap.showMiniMap();
    }

    @Override
    public void hideMiniMap() {
        this.oClientMap.hideMiniMap();
    }

    @Override
    public void setGridType(MapGridTypeEnum grid) {
        this.oClientMap.setGridType(grid);
    }

    @Override
    public double getSelectedIconScale() {
        return this.oClientMap.getSelectedIconScale();
    }

    @Override
    public IGeoLabelStyle getSelectedLabelStyle() {
        return this.oClientMap.getSelectedLabelStyle();
    }

    @Override
    public IGeoStrokeStyle getSelectedStrokeStyle() {
        return this.oClientMap.getSelectedStrokeStyle();
    }

    @Override
    public IGeoFillStyle getBufferFillStyle() {
        return this.oClientMap.getBufferFillStyle();
    }

    @Override
    public int getIconPixelSize() {
        return this.oClientMap.getIconPixelSize();
    }
}
