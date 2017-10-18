package mil.emp3.api;

import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
import mil.emp3.mapengine.abstracts.CoreMapInstance;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.interfaces.IMapEngineCapabilities;
import mil.emp3.mapengine.interfaces.IMapEngineProperties;
import mil.emp3.mapengine.interfaces.IMapEngineRequirements;
import mil.emp3.mapengine.interfaces.IMapGridLines;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;

/**
 * This class implements the IMap interface. It provides applications access to a remote
 * map via Mirror Cache without the burden of having to manage a local visible map.
 * Map swapping is not supported on this map.
 * I.E. SA manager should use this class to reach a remote map.
 */
public class RemoteMap implements IMap {
    private static final String TAG = RemoteMap.class.getSimpleName();

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final private IEventManager eventManager     = ManagerFactory.getInstance().getEventManager();

    final private /*MirroredMap*/MapViewFragmentBase oClientMap;
    final private IMapInstance mapInstance;

    public RemoteMap(final String remoteMapId, final Context context, final MirrorCacheModeEnum mode) {
        this(remoteMapId, context, mode, new NoMap());
    }

    /**
     * This is used by RemoteMapTest - We want to disable mirrorcache and use a mock MapInstance during unit test
     * @param remoteMapId
     * @param context
     * @param mode
     * @param userMapInstance
     */
    RemoteMap(final String remoteMapId, final Context context, final MirrorCacheModeEnum mode, final IMapInstance userMapInstance) {
        final IEmpPropertyList propList = new EmpPropertyList();

        propList.put("TAG", TAG);
        propList.put(Property.MIRROR_CACHE_MODE.getValue(), mode);
        propList.put(Property.ENGINE_CLASSNAME.getValue(), "noMap");
        this.mapInstance = userMapInstance == null ? new NoMap() : userMapInstance;
        this.oClientMap  = new /*MirroredMap*/MapViewFragmentBase(propList);

        init(context);
    }

    /**
     * addMapInstance is no longer a public method. swapMapInstance will invoke addmapInstance when required.
     * @param context
     */
    private void init(final Context context) {
        //this.oClientMap.onCreate(context);

        try {
            storageManager.swapMapInstance(oClientMap, mapInstance);
        } catch (final EMP_Exception e) {
            throw new IllegalStateException(e);
        }

        /*
         * Set up the timer only if we are using the NoMap inner class.
         * If user supplied the mapInstance then let the user decide
         * how to deal with this.
         */
        if (mapInstance instanceof NoMap) {
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    // force to run on UI thread
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            ((NoMap) mapInstance).generateStateChangeEvent(MapStateEnum.MAP_READY);
                            Log.d(TAG, "NoMap state change: " + MapStateEnum.MAP_READY);
                        }
                    });
                }
            }, 500);
        }
    }

    public void onDestroy() {
        //this.oClientMap.onDestroy();
    }

    @Override
    public void setMirrorCacheMode(final MirrorCacheModeEnum mode) {
        oClientMap.setMirrorCacheMode(mode);
    }

    @Override
    public boolean isEgressEnabled() {
        return oClientMap.isEgressEnabled();
    }

    @Override
    public boolean isIngressEnabled() {
        return oClientMap.isIngressEnabled();
    }

    @Override
    public MapStateEnum getState() {
        return this.oClientMap.getState();
    }

    @Override
    public void getScreenCapture(final IScreenCaptureCallback callback) {
        throw new UnsupportedOperationException("This map engine does not support this operation.");
    }

    @Override
    public ICamera getCamera() {
        return this.oClientMap.getCamera();
    }

    @Override
    public void setCamera(final ICamera camera, final boolean animate, final Object userContext) throws EMP_Exception {
        this.oClientMap.setCamera(camera, animate, userContext);
    }

    public void setCamera(final ICamera camera, final boolean animate) throws EMP_Exception {
        this.oClientMap.setCamera(camera, animate);
    }

    @Override
    public ILookAt getLookAt() {
        return this.oClientMap.getLookAt();
    }

    @Override
    public void setLookAt(final ILookAt lookAt, final boolean animate, final Object userContext) throws EMP_Exception {
        this.oClientMap.setLookAt(lookAt, animate, userContext);
    }

    @Override
    public void setLookAt(final ILookAt lookAt, final boolean animate) throws EMP_Exception {
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
    public void addOverlay(final IOverlay overlay, final boolean visible, final Object userContext) throws EMP_Exception {
        this.oClientMap.addOverlay(overlay, visible, userContext);
    }

    @Override
    public void addOverlays(final java.util.List<IOverlay> overlays, final boolean visible, final Object userContext) throws EMP_Exception {
        this.oClientMap.addOverlays(overlays, visible, userContext);
    }

    @Override
    public void removeOverlay(final IOverlay overlay, final Object userContext) throws EMP_Exception {
        this.oClientMap.removeOverlay(overlay, userContext);
    }

    @Override
    public void removeOverlays(final java.util.List<IOverlay> overlays, final Object userContext) throws EMP_Exception {
        this.oClientMap.removeOverlays(overlays, userContext);
    }

    @Override
    public void addOverlay(final IOverlay overlay, final boolean visible) throws EMP_Exception {
        this.oClientMap.addOverlay(overlay, visible);
    }

    @Override
    public void addOverlays(final java.util.List<IOverlay> overlays, final boolean visible) throws EMP_Exception {
        this.oClientMap.addOverlays(overlays, visible);
    }

    @Override
    public void removeOverlay(final IOverlay overlay) throws EMP_Exception {
        this.oClientMap.removeOverlay(overlay);
    }

    @Override
    public void removeOverlays(final java.util.List<IOverlay> overlays) throws EMP_Exception {
        this.oClientMap.removeOverlays(overlays);
    }

    @Override
    public void addMapService(final IMapService mapService) throws EMP_Exception {
        this.oClientMap.addMapService(mapService);
    }

    @Override
    public void removeMapService(final IMapService mapService) throws EMP_Exception {
        this.oClientMap.removeMapService(mapService);
    }

    @Override
    public void addMapService(final IMapService mapService, final IMapServiceResult result) throws EMP_Exception {
        this.oClientMap.addMapService(mapService, result);
    }

    @Override
    public void removeMapService(final IMapService mapService, final IMapServiceResult result) throws EMP_Exception {
        this.oClientMap.removeMapService(mapService, result);
    }

    @Override
    public void setVisibility(final IContainer target, final VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(target, actionEnum);
    }

    @Override
    public void setVisibility(final IContainerSet targetList, final VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetList, actionEnum);
    }

    @Override
    public void setVisibility(final java.util.UUID targetId, final VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetId, actionEnum);
    }

    @Override
    public void setVisibility(final IUUIDSet targetIdList, final VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetIdList, actionEnum);
    }

    @Override
    public void setVisibility(final IContainer target, final IContainer parent, final VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(target, parent, actionEnum);
    }

    @Override
    public void setVisibility(final java.util.UUID targetId, final java.util.UUID parentId, final VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetId, parentId, actionEnum);
    }

    @Override
    public void setVisibility(final IContainer target, final VisibilityActionEnum actionEnum, final Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(target, actionEnum, userContext);
    }

    @Override
    public void setVisibility(final IContainerSet targetList, final VisibilityActionEnum actionEnum, final Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetList, actionEnum, userContext);
    }

    @Override
    public void setVisibility(final java.util.UUID targetId, final VisibilityActionEnum actionEnum, final Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetId, actionEnum, userContext);
    }

    @Override
    public void setVisibility(final IUUIDSet targetIdList, final VisibilityActionEnum actionEnum, final Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetIdList, actionEnum, userContext);
    }

    @Override
    public void setVisibility(final IContainer target, final IContainer parent, final VisibilityActionEnum actionEnum, final Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(target, parent, actionEnum, userContext);
    }

    @Override
    public void setVisibility(final java.util.UUID targetId, final java.util.UUID parentId, final VisibilityActionEnum actionEnum, final Object userContext)
            throws EMP_Exception {
        this.oClientMap.setVisibility(targetId, parentId, actionEnum, userContext);
    }

    @Override
    public VisibilityStateEnum getVisibility(final IContainer target) {
        return this.oClientMap.getVisibility(target);
    }

    @Override
    public VisibilityStateEnum getVisibility(final IContainer target, final IContainer parent) {
        return this.oClientMap.getVisibility(target, parent);
    }

    @Override
    public EventListenerHandle addMapStateChangeEventListener(final IMapStateChangeEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_STATE_CHANGE_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addMapViewChangeEventListener(final IMapViewChangeEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_VIEW_CHANGE_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addMapInteractionEventListener(final IMapInteractionEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_INTERACTION_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addMapFeatureAddedEventListener(final IMapFeatureAddedEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_FEATURE_ADDED_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addMapFeatureRemovedEventListener(final IMapFeatureRemovedEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.MAP_FEATURE_REMOVED_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addCameraEventListener(final ICameraEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.CAMERA_EVENT_LISTENER, this, listener);
    }

    @Override
    public void setIconSize(final IconSizeEnum eSize) throws EMP_Exception {
        this.oClientMap.setIconSize(eSize);
    }

    @Override
    public void setIconSize(final IconSizeEnum eSize, final Object userContext) throws EMP_Exception {
        this.oClientMap.setIconSize(eSize, userContext);
    }

    @Override
    public IconSizeEnum getIconSize() {
        return this.oClientMap.getIconSize();
    }

    @Override
    public void setMilStdLabels(final MilStdLabelSettingEnum labelSetting) throws EMP_Exception {
        this.oClientMap.setMilStdLabels(labelSetting);
    }

    @Override
    public void setMilStdLabels(final MilStdLabelSettingEnum labelSetting, final Object userContext) throws EMP_Exception {
        this.oClientMap.setMilStdLabels(labelSetting, userContext);
    }

    @Override
    public MilStdLabelSettingEnum getMilStdLabels() {
        return this.oClientMap.getMilStdLabels();
    }

    @Override
    public IMap swapMapEngine(final IEmpPropertyList properties) throws EMP_Exception {
        throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Map swap is not supported on a remote map.");
    }

    @Override
    public void editFeature(final IFeature oFeature) throws EMP_Exception {
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
    public void clearContainer(final Object userContext) throws EMP_Exception {
        this.oClientMap.clearContainer(userContext);
    }

    @Override
    public void clearContainer() throws EMP_Exception {
        this.oClientMap.clearContainer();
    }

    @Override
    public EventListenerHandle addContainerEventListener(final IContainerEventListener listener) throws EMP_Exception {
        return this.oClientMap.addContainerEventListener(listener);
    }

    @Override
    public EventListenerHandle addVisibilityEventListener(final IVisibilityEventListener listener) throws EMP_Exception {
        return this.oClientMap.addVisibilityEventListener(listener);
    }

    @Override
    public EventListenerHandle addFeatureEventListener(final IFeatureEventListener listener) throws EMP_Exception {
        return this.oClientMap.addFeatureEventListener(listener);
    }

    @Override
    public EventListenerHandle addFeatureInteractionEventListener(final IFeatureInteractionEventListener listener) throws EMP_Exception {
        return this.oClientMap.addFeatureInteractionEventListener(listener);
    }

    @Override
    public EventListenerHandle addFeatureEditEventListener(final IFeatureEditEventListener listener) throws EMP_Exception {
        return this.oClientMap.addFeatureEditEventListener(listener);
    }

    @Override
    public void removeEventListener(final EventListenerHandle hListener) {
        this.oClientMap.removeEventListener(hListener);
    }

    @Override
    public void setName(final String string) {
        this.oClientMap.setName(string);
    }

    @Override
    public String getName() {
        return this.oClientMap.getName();
    }

    @Override
    public void setGeoId(final java.util.UUID uuid) {
        this.oClientMap.setGeoId(uuid);
    }

    @Override
    public java.util.UUID getGeoId() {
        return this.oClientMap.getGeoId();
    }

    @Override
    public void setDataProviderId(final String s) {
        this.oClientMap.setDataProviderId(s);
    }

    @Override
    public String getDataProviderId() {
        return this.oClientMap.getDataProviderId();
    }

    @Override
    public void setDescription(final String string) {
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
    public void setReadOnly(final boolean b) {
        this.oClientMap.setReadOnly(b);
    }

    @Override
    public boolean getReadOnly() {
        return this.oClientMap.getReadOnly();
    }

    @Override
    public java.util.List<IMapService> getMapServices() {
        return this.oClientMap.getMapServices();
    }

    @Override
    public EventListenerHandle addMapFreehandEventListener(final IMapFreehandEventListener listener) throws EMP_Exception {
        return this.oClientMap.addMapFreehandEventListener(listener);
    }

    @Override
    public EventListenerHandle addFeatureDrawEventListener(final IFeatureDrawEventListener listener) throws EMP_Exception {
        return this.oClientMap.addFeatureDrawEventListener(listener);
    }

    @Override
    public void drawFeature(final IFeature oFeature) throws EMP_Exception {
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
    public void drawFreehand(final IGeoStrokeStyle initialStyle) throws EMP_Exception {
        this.oClientMap.drawFreehand(initialStyle);
    }

    @Override
    public void setFreehandStyle(final IGeoStrokeStyle style) throws EMP_Exception {
        this.oClientMap.setFreehandStyle(style);
    }

    @Override
    public void drawFreehandExit() throws EMP_Exception {
        this.oClientMap.drawFreehandExit();
    }

    @Override
    public void zoomTo(final IFeature feature, final boolean animate) { this.oClientMap.zoomTo(feature, animate); }

    @Override
    public void zoomTo(final List<IFeature> featureList, final boolean animate) { this.oClientMap.zoomTo(featureList, animate); }

    @Override
    public void zoomTo(final IOverlay overlay, final boolean animate) { this.oClientMap.zoomTo(overlay, animate); }

    @Override
    public void setBounds(final IGeoBounds bounds, final boolean animate) { this.oClientMap.setBounds(bounds, animate); }

    @Override
    public IGeoBounds getBounds() {
        return this.oClientMap.getBounds();
    }

    @Override
    public void setMotionLockMode(final MapMotionLockEnum mode) throws EMP_Exception {
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
    public void setFarDistanceThreshold(final double dValue) {
        this.oClientMap.setFarDistanceThreshold(dValue);
    }

    @Override
    public double getFarDistanceThreshold() {
        return this.oClientMap.getFarDistanceThreshold();
    }

    @Override
    public void setMidDistanceThreshold(final double dValue) {
        this.oClientMap.setMidDistanceThreshold(dValue);
    }

    @Override
    public double getMidDistanceThreshold() {
        return this.oClientMap.getMidDistanceThreshold();
    }

    @Override
    public Point geoToScreen(final IGeoPosition pos) throws EMP_Exception {
        return this.oClientMap.geoToScreen(pos);
    }

    @Override
    public IGeoPosition screenToGeo(final Point point) throws EMP_Exception {
        return this.oClientMap.screenToGeo(point);
    }

    @Override
    public Point geoToContainer(final IGeoPosition pos) throws EMP_Exception {
        return this.oClientMap.geoToContainer(pos);
    }

    @Override
    public IGeoPosition containerToGeo(final Point point) throws EMP_Exception {
        return this.oClientMap.containerToGeo(point);
    }

    @Override
    public void selectFeature(final IFeature feature) {
        this.oClientMap.selectFeature(feature);
    }

    @Override
    public void selectFeatures(final java.util.List<IFeature> features) {
        this.oClientMap.selectFeatures(features);
    }

    @Override
    public void deselectFeature(final IFeature feature) {
        this.oClientMap.deselectFeature(feature);
    }

    @Override
    public void deselectFeatures(final java.util.List<IFeature> features) {
        this.oClientMap.deselectFeatures(features);
    }

    @Override
    public List<IFeature> getSelected() {
        return this.oClientMap.getSelected();
    }

    @Override
    public void clearSelected() {
        this.oClientMap.clearSelected();
    }

    @Override
    public void selectFeature(final IFeature feature, final Object userContext) {
        this.oClientMap.selectFeature(feature, userContext);
    }

    @Override
    public void selectFeatures(final java.util.List<IFeature> features, final Object userContext) {
        this.oClientMap.selectFeatures(features, userContext);
    }

    @Override
    public void deselectFeature(final IFeature feature, final Object userContext) {
        this.oClientMap.deselectFeature(feature, userContext);
    }

    @Override
    public void deselectFeatures(final java.util.List<IFeature> features, final Object userContext) {
        this.oClientMap.deselectFeatures(features, userContext);
    }

    @Override
    public void clearSelected(final Object userContext) {
        this.oClientMap.clearSelected(userContext);
    }

    @Override
    public boolean isSelected(final IFeature feature) {
        return this.oClientMap.isSelected(feature);
    }

    @Override
    public void editFeature(final IFeature oFeature, final IEditEventListener listener) throws EMP_Exception {
        this.oClientMap.editFeature(oFeature, listener);
    }

    @Override
    public void drawFeature(final IFeature oFeature, final IDrawEventListener listener) throws EMP_Exception {
        this.oClientMap.drawFeature(oFeature, listener);
    }

    @Override
    public void drawFreehand(final IGeoStrokeStyle initialStyle, final IFreehandEventListener listener) throws EMP_Exception {
        this.oClientMap.drawFreehand(initialStyle, listener);
    }

    @Override
    public FontSizeModifierEnum getFontSizeModifier() {
        return this.oClientMap.getFontSizeModifier();
    }

    @Override
    public void setFontSizeModifier(final FontSizeModifierEnum value)
            throws EMP_Exception {
        this.oClientMap.setFontSizeModifier(value);
    }

    @Override
    public boolean containsProperty(final String propertyName) {
        return this.oClientMap.containsProperty(propertyName);
    }

    @Override
    public String getProperty(final String propertyName) {
        return this.oClientMap.getProperty(propertyName);
    }

    @Override
    public void setProperty(final String propertyName, final String propertyValue) {
        this.oClientMap.setProperty(propertyName, propertyValue);
    }

    @Override
    public void removeProperty(final String propertyName) {
        this.oClientMap.removeProperty(propertyName);
    }

    @Override
    public boolean getBooleanProperty(final String propertyName) {
        return this.oClientMap.getBooleanProperty(propertyName);
    }

    @Override
    public int getIntegerProperty(final String propertyName) {
        return this.oClientMap.getIntegerProperty(propertyName);
    }

    @Override
    public float getFloatProperty(final String propertyName) {
        return this.oClientMap.getFloatProperty(propertyName);
    }

    @Override
    public double getDoubleProperty(final String propertyName) {
        return this.oClientMap.getDoubleProperty(propertyName);
    }

    @Override
    public void setBackgroundBrightness(final int setting) {
        this.oClientMap.setBackgroundBrightness(setting);
    }

    @Override
    public int getBackgroundBrightness() {
        return this.oClientMap.getBackgroundBrightness();
    }

    static private class NoMap extends CoreMapInstance {
        static final private String TAG = NoMap.class.getSimpleName();

        private ICamera currentCamera;
        private ILookAt currentLookAt;

        NoMap() {
            super(TAG, null);
        }
        @Override
        public void addFeatures(final FeatureVisibilityList features, final Object userContext) {

        }

        @Override
        public View getMapInstanceAndroidView() {
            return null;
        }

        @Override
        public void onResume() {

        }

        @Override
        public void onPause() {

        }

        @Override
        public void onDestroy() {

        }

        @Override
        public View onCreateView() {
            Log.d(TAG, "onCreateView");
            return null;
        }

        @Override
        public IMapEngineProperties getMapEngineProperties() {
            return null;
        }

        @Override
        public IMapEngineCapabilities getCapabilities() {
            return null;
        }

        @Override
        public IMapEngineRequirements getRequirements() {
            return null;
        }

        @Override
        public void removeFeatures(final IUUIDSet features, final Object userContext) {

        }

        @Override
        public void addMapService(final IMapService mapService) {

        }

        @Override
        public void removeMapService(final IMapService mapService) {

        }

        @Override
        public void addMapService(final IMapService mapService, final IMapServiceResult result) {

        }

        @Override
        public void removeMapService(final IMapService mapService, final IMapServiceResult result) {

        }

        @Override
        public ICamera getCamera() {
            return currentCamera;
        }

        @Override
        public void setCamera(final ICamera oCamera, final boolean animate, final Object userContext) {
            this.currentCamera = oCamera;
        }

        @Override
        public void applyCameraChange(final ICamera oCamera, final boolean animate, final Object userContext) { }

        @Override
        public void setLookAt(final ILookAt lookAt, final boolean animate, final Object userContext) {
            this.currentLookAt = lookAt;
        }

        @Override
        public ILookAt getLookAt() {
            return currentLookAt;
        }

        @Override
        public void applyLookAtChange(final ILookAt oLookAt, final boolean animate, final Object userContext) { }

        @Override
        public void setBounds(final IGeoBounds bounds) {

        }

        @Override
        public void setMotionLockMode(final MapMotionLockEnum mode) {

        }

        @Override
        public void registerMilStdRenderer(final IMilStdRenderer oRenderer) {

        }

        @Override
        public double getFieldOfView() {
            return 0;
        }

        @Override
        public int getViewHeight() {
            return 0;
        }

        @Override
        public int getViewWidth() {
            return 0;
        }

        @Override
        public void setFarDistanceThreshold(final double dValue) {
        }

        @Override
        public void setMidDistanceThreshold(final double dValue) {
        }

        @Override
        public void getVersionInformation(final StringBuilder builder, final List<String> buildConfigFields) {

        }

        @Override
        public IGeoBounds getMapBounds() {
            return null;
        }

        @Override
        public Point geoToScreen(final IGeoPosition pos) {
            return null;
        }

        @Override
        public IGeoPosition screenToGeo(final Point point) {
            return null;
        }

        @Override
        public Point geoToContainer(final IGeoPosition pos) {
            return null;
        }

        @Override
        public IGeoPosition containerToGeo(final Point point) {
            return null;
        }

        @Override
        public void selectFeatures(final List<IFeature> featureList) {

        }

        @Override
        public void deselectFeatures(final List<IFeature> featureList) {

        }

        @Override
        public void setFontSizeModifier(final FontSizeModifierEnum value) {

        }

        @Override
        public void setBackgroundBrightness(final int setting) {
        }

        @Override
        public int getBackgroundBrightness() {
            return 50;
        }

        @Override
        public void setMapGridGenerator(final IMapGridLines gridGenerator) {

        }

        @Override
        public void scheduleMapRedraw() {

        }

        @Override
        public View showMiniMap() {
            return null;
        }

        @Override
        public void hideMiniMap() {
        }
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
    public void setGridType(final MapGridTypeEnum grid) {
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
