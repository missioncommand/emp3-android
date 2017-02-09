package mil.emp3.api.abstracts;

import android.graphics.Point;
import android.util.Log;
import android.view.View;

import org.cmapi.primitives.GeoContainer;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

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
import mil.emp3.api.interfaces.IScreenCaptureCallback;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.ICameraEventListener;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFeatureDrawEventListener;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.listeners.IMapFeatureAddedEventListener;
import mil.emp3.api.listeners.IMapFeatureRemovedEventListener;
import mil.emp3.api.listeners.IMapFreehandEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.api.utils.UUIDSet;

/**
 * Map represents the container that encapsulates the map displayed on the users screen. All overlays and Features are
 * contained within the Map. Map is responsible for starting and displaying a map engine of users choice. An application
 * can display multiple maps on the screen. Applications use either MapView or MapFragment to build and display a map.
 */
public abstract class Map extends Container implements IMap {
    private String TAG = Map.class.getSimpleName();
    final private ICoreManager coreManager       = ManagerFactory.getInstance().getCoreManager();

    // Each application on Android has its own private Map. Mirror Cache is a mechanism used to shared map objects
    // among applications. This is work in progress.
    private MirrorCacheModeEnum mode = MirrorCacheModeEnum.DISABLED;

    // Map encapsulates map engines supplied by various vendors. Currently only supported engine is NASA WorldWind.
    /**
     * Class that implements IMapInstance and encapsulates a map engine
     */
    protected String engineClassName;

    /**
     * Android Application that has the implementation of engineClassName (optional)
     */
    protected String engineAPKName;

    private void processProperties(IEmpPropertyList properties) {
        this.engineClassName = properties.getStringValue(Property.ENGINE_CLASSNAME.getValue());

        this.engineAPKName = properties.getStringValue(Property.ENGINE_APKNAME.getValue());

        if (properties.getMirrorCacheModeEnum(Property.MIRROR_CACHE_MODE.getValue()) != null) {
            this.mode = properties.getMirrorCacheModeEnum(Property.MIRROR_CACHE_MODE.getValue());
        }

        if (properties.containsKey("TAG")) {
            this.TAG = properties.getStringValue("TAG");
        }

        if (properties.containsKey("MAPNAME")) {
            this.setName(properties.getStringValue("MAPNAME"));
        }
    }

    public Map(IEmpPropertyList properties) {
        super(new GeoContainer());
        this.processProperties(properties);
    }

    @Override
    public void setMirrorCacheMode(MirrorCacheModeEnum mode) {
        this.mode = mode;
    }

    @Override
    public boolean isEgressEnabled() {
        return mode == MirrorCacheModeEnum.EGRESS || mode == MirrorCacheModeEnum.BIDIRECTIONAL;
    }

    @Override
    public boolean isIngressEnabled() {
        return mode == MirrorCacheModeEnum.INGRESS || mode == MirrorCacheModeEnum.BIDIRECTIONAL;
    }

    /**
     * Swap the map engine associated with this map.
     * @param properties The property key value pair needed to support map swapping. The required properties
     *                   are {@link mil.emp3.api.enums.Property#ENGINE_CLASSNAME} and {@link mil.emp3.api.enums.Property#ENGINE_APKNAME} if
     *                   the map is implemented in an APK.
     * @return
     * @throws EMP_Exception
     */
    @Override
    public IMap swapMapEngine(IEmpPropertyList properties) throws EMP_Exception {
        Log.e(TAG, "Must override this method");
        throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "You have to override this like in MapFragment or MapView.");
    }

    // IMap implementation
    @Override
    public final MapStateEnum getState() {
        return coreManager.getState(this);
    }

    @Override
    public void getScreenCapture(IScreenCaptureCallback callback) {
        if (null == callback) {
            throw new InvalidParameterException("The callback can not be null.");
        }
        storageManager.getMapInstance(this).getCapture(callback);
    }

    @Override
    public ICamera getCamera() {
        return coreManager.getCamera(this);
    }

    @Override
    public void setCamera(ICamera camera, boolean animate) throws EMP_Exception {
        coreManager.setCamera(this, camera, animate);
    }

    @Override
    public ILookAt getLookAt() {
        return coreManager.getLookAt(this);
    }

    @Override
    public void setLookAt(ILookAt lookAt, boolean animate) throws EMP_Exception {
        coreManager.setLookAt(this, lookAt, animate);
    }

    @Override
    public List<IFeature> getAllFeatures() {
        return storageManager.getChildFeatures(this);
    }

    @Override
    public List<IOverlay> getAllOverlays() {
        return storageManager.getChildOverlays(this);
    }

    @Override
    public void addOverlay(IOverlay overlay, boolean visible)
            throws EMP_Exception {
        if (overlay == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "parameter to Map.addOverlay can not be null.");
        }
        
        ArrayList<IOverlay> oList = new ArrayList<>();
        oList.add(overlay);
        this.addOverlays(oList, visible);
    }

    @Override
    public void addOverlays(List<IOverlay> overlays, boolean visible)
            throws EMP_Exception {
        if (overlays == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "parameter to Map.addOverlays can not be null.");
        } else if (overlays.size() > 0) {
            storageManager.addOverlays(this, overlays, visible);
        }
    }

    @Override
    public void removeOverlay(IOverlay overlay)
            throws EMP_Exception {
        if (overlay == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "parameter to Map.removeOverlay can not be null.");
        }
        
        ArrayList<IOverlay> oList = new ArrayList<>();
        oList.add(overlay);
        this.removeOverlays(oList);
    }

    @Override
    public void removeOverlays(List<IOverlay> overlays)
            throws EMP_Exception {
        if (overlays == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "parameter to Map.removeOverlays can not be null.");
        } else if (overlays.size() > 0) {
            storageManager.removeOverlays(this, overlays);
        }
    }

    @Override
    public void addMapService(IMapService mapService)
            throws EMP_Exception {
        storageManager.addMapService(this, mapService);
    }

    @Override
    public void removeMapService(IMapService mapService)
            throws EMP_Exception {
        storageManager.removeMapService(this, mapService);
    }
    
    @Override
    public List<IMapService> getMapServices()
    {
        return storageManager.getMapServices(this);
    }

    @Override
    public void setVisibility(IContainer target, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        if ((target == null) || (actionEnum == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameter to Map.setVisibility can not be null.");
        }
        
        UUIDSet oSet = new UUIDSet();
        oSet.add(target.getGeoId());
        storageManager.setVisibilityOnMap(this, oSet, actionEnum);
    }

    @Override
    public void setVisibility(IContainerSet targetList, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        if ((targetList == null) || (actionEnum == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "parameter to Map.setVisibility can not be null.");
        } else if (targetList.size() > 0) {
            UUIDSet oSet = new UUIDSet();
            
            for (IContainer container: targetList) {
                oSet.add(container.getGeoId());
            }
            storageManager.setVisibilityOnMap(this, oSet, actionEnum);
        }
    }

    @Override
    public void setVisibility(java.util.UUID targetId, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        if ((targetId == null) || (actionEnum == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "parameter to Map.setVisibility can not be null.");
        }
        
        UUIDSet oSet = new UUIDSet();
        oSet.add(targetId);
        storageManager.setVisibilityOnMap(this, oSet, actionEnum);
    }

    @Override
    public void setVisibility(IUUIDSet targetIdList, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        if ((targetIdList == null) || (actionEnum == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "parameters to Map.setVisibility can not be null.");
        } else if (targetIdList.size() > 0) {
            storageManager.setVisibilityOnMap(this, targetIdList, actionEnum);
        }
    }

    @Override
    public void setVisibility(IContainer target, IContainer parent, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        if ((target == null) || (parent == null) || (actionEnum == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "parameters to Map.setVisibility can not be null.");
        }
        
        storageManager.setVisibilityOnMap(this, target, parent, actionEnum);
    }

    @Override
    public void setVisibility(java.util.UUID targetId, java.util.UUID parentId, VisibilityActionEnum actionEnum)
            throws EMP_Exception {
        if ((targetId == null) || (parentId == null) || (actionEnum == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parameters to Map.setVisibility can not be null.");
        }
        
        IContainer target = storageManager.findContainer(targetId);
        IContainer parent = storageManager.findContainer(parentId);

        if (target == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Target container not found.");
        }

        if (parent == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Parent container not found.");
        }

        storageManager.setVisibilityOnMap(this, target, parent, actionEnum);
    }

    @Override
    public VisibilityStateEnum getVisibility(IContainer target) {
        return storageManager.getVisibilityOnMap(this, target);
    }

    @Override
    public VisibilityStateEnum getVisibility(IContainer target, IContainer parent) {
        return storageManager.getVisibilityOnMap(this, target, parent);
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
    public EventListenerHandle addCameraEventListener(ICameraEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.CAMERA_EVENT_LISTENER, this, listener);
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
    public void setIconSize(IconSizeEnum eSize)
            throws EMP_Exception {
        storageManager.setIconSize(this, eSize);
    }

    @Override
    public IconSizeEnum getIconSize() {
        return storageManager.getIconSize(this);
    }

    @Override
    public void setMilStdLabels(MilStdLabelSettingEnum labelSetting)
            throws EMP_Exception {
        storageManager.setMilStdLabels(this, labelSetting);
    }

    @Override
    public MilStdLabelSettingEnum getMilStdLabels() {
        return storageManager.getMilStdLabels(this);
    }

    @Override
    public void editFeature(IFeature oFeature)
            throws EMP_Exception {
        if (oFeature == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid parameter. The feature must be provided.");
        }

        coreManager.editFeature(this, oFeature, null);
    }

    @Override
    public void cancelEdit()
            throws EMP_Exception {
        coreManager.editCancel(this);
    }

    @Override
    public void completeEdit()
            throws EMP_Exception {
        coreManager.editComplete(this);
    }

    @Override
    public EventListenerHandle addMapFreehandEventListener(IMapFreehandEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.FREEHAND_DRAW_EVENT_LISTENER, this, listener);
    }

    @Override
    public EventListenerHandle addFeatureDrawEventListener(IFeatureDrawEventListener listener) throws EMP_Exception {
        return eventManager.addEventHandler(EventListenerTypeEnum.FEATURE_DRAW_EVENT_LISTENER, this, listener);
    }

    @Override
    public void drawFeature(IFeature oFeature) throws EMP_Exception {
        if (oFeature == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid parameter. The feature must be provided.");
        }

        coreManager.drawFeature(this, oFeature, null);
    }

    @Override
    public void cancelDraw() throws EMP_Exception {
        coreManager.drawCancel(this);
    }

    @Override
    public void completeDraw() throws EMP_Exception {
        coreManager.drawComplete(this);
    }

    @Override
    public void drawFreehand(IGeoStrokeStyle initialStyle) throws EMP_Exception {
        this.drawFreehand(initialStyle, null);
    }

    @Override
    public void setFreehandStyle(IGeoStrokeStyle style) throws EMP_Exception {
        coreManager.setFreehandStyle(this, style);
    }

    @Override
    public void drawFreehandExit() throws EMP_Exception {
        coreManager.drawFreehandExit(this);
    }

    @Override
    public void zoomTo(IFeature feature, boolean animate) {
        if(null == feature) {
            return;
        }
        List<IFeature> featureList = new ArrayList<>();
        featureList.add(feature);
        zoomTo(featureList, animate);
    }

    @Override
    public void zoomTo(List<IFeature> featureList, boolean animate) {
        if((null == featureList) || (0 == featureList.size())) {
            return;
        }
        coreManager.zoomTo(this, featureList, animate);
    }

    @Override
    public void zoomTo(IOverlay overlay, boolean animate) {
        if(null == overlay) {
            return;
        }
        coreManager.zoomTo(this, overlay, animate);
    }

    @Override
    public void setBounds(IGeoBounds bounds, boolean animate) {
        if(null != bounds) {
            coreManager.setBounds(this, bounds, animate);
        }
    }

    @Override
    public IGeoBounds getBounds() {
        return coreManager.getBounds(this);
    }

    @Override
    public void setMotionLockMode(MapMotionLockEnum mode) throws EMP_Exception {
        coreManager.setMotionLockMode(this, mode);
    }

    @Override
    public MapMotionLockEnum getMotionLockMode() throws EMP_Exception {
        return coreManager.getMotionLockMode(this);
    }

    @Override
    public EditorMode getEditorMode() throws EMP_Exception {
        return coreManager.getEditorMode(this);
    }

    @Override
    public void editFeature(IFeature oFeature, IEditEventListener listener) throws EMP_Exception {
        if (listener == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid parameter. The listener must be provided.");
        }
        if (oFeature == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid parameter. The feature must be provided.");
        }

        coreManager.editFeature(this, oFeature, listener);
    }

    @Override
    public void drawFeature(IFeature oFeature, IDrawEventListener listener) throws EMP_Exception {
        if (listener == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid parameter. The listener must be provided.");
        }
        if (oFeature == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid parameter. The feature must be provided.");
        }

        coreManager.drawFeature(this, oFeature, listener);
    }

    @Override
    public void drawFreehand(IGeoStrokeStyle initialStyle, IFreehandEventListener listener) throws EMP_Exception {
        coreManager.drawFreehand(this, initialStyle, listener);
    }

    @Override
    public void setFarDistanceThreshold(double dValue) {
        storageManager.setFarDistanceThreshold(this, dValue);
    }

    @Override
    public double getFarDistanceThreshold() {
        return storageManager.getFarDistanceThreshold(this);
    }

    @Override
    public void setMidDistanceThreshold(double dValue) {
        storageManager.setMidDistanceThreshold(this, dValue);
    }

    @Override
    public double getMidDistanceThreshold() {
        return storageManager.getMidDistanceThreshold(this);
    }


    @Override
    public Point geoToScreen(IGeoPosition pos) throws EMP_Exception {
        return coreManager.geoToScreen(this, pos);
    }

    @Override
    public IGeoPosition screenToGeo(Point point) throws EMP_Exception {
        return coreManager.screenToGeo(this, point);
    }

    @Override
    public Point geoToContainer(IGeoPosition pos) throws EMP_Exception {
        return coreManager.geoToContainer(this, pos);
    }

    @Override
    public IGeoPosition containerToGeo(Point point) throws EMP_Exception {
        return coreManager.containerToGeo(this, point);
    }

    @Override
    public void selectFeature(IFeature feature) {
        if (feature == null) {
            return;
        }
        List<IFeature> list = new ArrayList<>();
        list.add(feature);
        storageManager.selectFeatures(this, list);
    }

    @Override
    public void selectFeatures(List<IFeature> features) {
        if (features == null) {
            return;
        }
        storageManager.selectFeatures(this, features);
    }

    @Override
    public void deselectFeature(IFeature feature) {
        if (feature == null) {
            return;
        }
        List<IFeature> list = new ArrayList<>();
        list.add(feature);
        storageManager.deselectFeatures(this, list);
    }

    @Override
    public void deselectFeatures(List<IFeature> features) {
        if (features == null) {
            return;
        }
        storageManager.deselectFeatures(this, features);
    }

    @Override
    public List<IFeature> getSelected() {
        return storageManager.getSelected(this);
    }

    @Override
    public void clearSelected() {
        storageManager.clearSelected(this);
    }

    @Override
    public boolean isSelected(IFeature feature) {
        return storageManager.isSelected(this, feature);
    }

    @Override
    public FontSizeModifierEnum getFontSizeModifier() {
        return storageManager.getFontSizeModifier(this);
    }

    @Override
    public void setFontSizeModifier(FontSizeModifierEnum value) throws EMP_Exception {
        storageManager.setFontSizeModifier(this, value);
    }

    @Override
    public View showMiniMap() {
        return storageManager.getMapInstance(this).showMiniMap();
    }

    @Override
    public void hideMiniMap() {
        storageManager.getMapInstance(this).hideMiniMap();
    }
    
    @Override
    public void setBackgroundBrightness(int setting) {
        storageManager.getMapInstance(this).setBackgroundBrightness(setting);
    }

    @Override
    public int getBackgroundBrightness() {
        return storageManager.getMapInstance(this).getBackgroundBrightness();
    }

    @Override
    public void setGridType(MapGridTypeEnum grid) {
        storageManager.getMapInstance(this).setGridType(grid);
    }
}
