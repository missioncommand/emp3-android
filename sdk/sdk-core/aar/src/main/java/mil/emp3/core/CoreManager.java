package mil.emp3.core;

import android.graphics.Point;
import android.util.Log;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.List;

import mil.emp3.api.enums.CameraEventEnum;
import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.LookAtEventEnum;
import mil.emp3.api.enums.MapGridTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.core.storage.IClientMapRestoreData;
import mil.emp3.api.interfaces.core.storage.IClientMapToMapInstance;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.core.utils.ZoomToUtils;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the EMP V3 core manager. It should never be called by the client nor the map.
 */
public class CoreManager implements ICoreManager {
    private static final String TAG = CoreManager.class.getSimpleName();

    private IStorageManager storageManager;
    private IEventManager eventManager;

    private static java.util.Set<IGeoMilSymbol.Modifier> oRequiredLabels = new java.util.HashSet<>();
    private static java.util.Set<IGeoMilSymbol.Modifier> oCommonLabels = new java.util.HashSet<>();

    @Override
    public void setStorageManager(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    @Override
    public void setEventManager(IEventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public MapStateEnum getState(IMap clientMap) {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);
        
        if (mapMapping == null) {
            return null;
        }
        return mapMapping.getMapState();
    }

    @Override
    public void setCamera(IMap clientMap, ICamera camera, boolean animate, Object userContext) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);
        
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_MAP, "Can't setCamera to an invalid map.");
        }
        
        IMapInstance oMapInstance = mapMapping.getMapInstance();
        
        oMapInstance.setCamera(camera, animate, userContext);
        mapMapping.setCamera(camera);

        // We will need this when client executes swapMapEngine and it will be resused for activity restart also.
        // This is stored again when activity is destroyed but that doesn't help pure swapMapEngine scenario.
        IClientMapRestoreData cmrd = storageManager.getRestoreData(clientMap);
        if(null != cmrd) {
            cmrd.setCamera(camera);
        }
    }

    @Override
    public ICamera getCamera(IMap clientMap) {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);
        
        if (mapMapping == null) {
            return null;
        }
        return mapMapping.getCamera();
    }

    @Override
    public void setLookAt(IMap clientMap, ILookAt lookAt, boolean animate, Object userContext) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_MAP, "Can't setLookAt to an invalid map.");
        }

        IMapInstance oMapInstance = mapMapping.getMapInstance();

        oMapInstance.setLookAt(lookAt, animate, userContext);
        mapMapping.setLookAt(lookAt);

        // We will need this when client executes swapMapEngine and it will be resused for activity restart also.
        // This is stored again when activity is destroyed but that doesn't help pure swapMapEngine scenario.
        IClientMapRestoreData cmrd = storageManager.getRestoreData(clientMap);
        if(null != cmrd) {
            cmrd.setLookAt(lookAt);
        }
    }

    public ILookAt getLookAt(IMap clientMap) {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        if (mapMapping == null) {
            return null;
        }
        return mapMapping.getLookAt();
    }

    public void processCameraSettingChange(ICamera camera, boolean animate, Object userContext) {
        //storageManager.processCameraSettingChange(camera);
        eventManager.generateCameraEvent(CameraEventEnum.CAMERA_IN_MOTION, camera, animate, userContext);
        List<IClientMapToMapInstance> mappings = storageManager.getMappings(camera);
        if(null != mappings) {
            for(IClientMapToMapInstance mapping : mappings) {
                if(null != mapping.getMapInstance()) {
                    mapping.getMapInstance().applyCameraChange(camera, animate, userContext);
                }
            }
        }
    }

    public void processLookAtSettingChange(ILookAt lookAt, boolean animate, Object userContext) {
        eventManager.generateLookAtEvent(LookAtEventEnum.LOOKAT_IN_MOTION, lookAt, animate, userContext);
        List<IClientMapToMapInstance> mappings = storageManager.getMappings(lookAt);
        if(null != mappings) {
            for(IClientMapToMapInstance mapping : mappings) {
                if(null != mapping.getMapInstance()) {
                    mapping.getMapInstance().applyLookAtChange(lookAt, animate, userContext);
                }
            }
        }
    }


    @Override
    public void setMotionLockMode(IMap clientMap, MapMotionLockEnum mode) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        if (mapMapping == null) {
            return;
        }

        mapMapping.setLockMode(mode);
    }

    @Override
    public MapMotionLockEnum getMotionLockMode(IMap clientMap) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }
        return mapMapping.getLockMode();
    }

    @Override
    public EditorMode getEditorMode(IMap clientMap) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }
        return mapMapping.getEditorMode();
    }

    @Override
    public void editFeature(IMap clientMap, IFeature oFeature, IEditEventListener listener) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }

        if (!storageManager.isOnMap(clientMap, oFeature)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Not Supported. The feature is not on the map.");
        }

        if (!mapMapping.canEdit(oFeature)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Not Supported. The current map can not display a feature of this type.");
        }

        mapMapping.editFeature(oFeature, listener);
    }

    @Override
    public void editCancel(IMap clientMap) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        if (mapMapping == null) {
            return;
        }

        mapMapping.editCancel();
    }

    @Override
    public void editComplete(IMap clientMap) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        if (mapMapping == null) {
            return;
        }

        mapMapping.editComplete();
    }

    @Override
    public void drawFeature(IMap clientMap, IFeature oFeature, IDrawEventListener listener) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        if (mapMapping == null) {
            return;
        }

        //if (storageManager.isOnMap(clientMap, oFeature)) {
        //    throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Not Supported. The feature is on the map.");
        //}

        if (!mapMapping.canEdit(oFeature)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Not Supported. The current map can not display a feature of this type.");
        }

        mapMapping.drawFeature(oFeature, listener, (storageManager.findContainer(oFeature.getGeoId()) == null));
    }

    @Override
    public void drawCancel(IMap clientMap) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        if (mapMapping == null) {
            return;
        }

        mapMapping.drawCancel();
    }

    @Override
    public void drawComplete(IMap clientMap) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        if (mapMapping == null) {
            return;
        }

        mapMapping.drawComplete();
    }
    
    /**
     * Find the center of all the visible features from featureList, find the boundingBox that encloses all the features, then
     *     set Camera tilt, roll, heading to zero
     *     set Camera lat/long to center
     *     set Camera altitude to a distance calculated using bounding box and field of view..
     *
     * @param clientMap
     * @param featureList
     */
    @Override
    public void zoomTo(IMap clientMap, List<IFeature> featureList, boolean animate) {
        ZoomToUtils.getInstance().zoomTo(clientMap, featureList, animate);
    }

    /**
     * Fetch all the features for the overlay and then invoke zoomTo on that feature list.
     * @param clientMap
     * @param overlay
     */
    @Override
    public void zoomTo(IMap clientMap, IOverlay overlay, boolean animate) {
        if((null == clientMap) || (null == overlay)) {
            Log.e(TAG, "clientMap and overlay must be non null");
            return;
        }
        List<IFeature> featureList = overlay.getFeatures();
        zoomTo(clientMap, featureList, animate);
    }

    /**
     * setBounds is similar to zoomIn except user has already specified the bounds. So the procedure is the same:
     *     Using the bounds find the center
     *     set Camera tilt, roll, heading to zero
     *     set Camera lat/long to center
     *     set Camera altitude to a distance calculated using bounding box and field of view..
     *
     *     We will reuse the methods from ZoomInUtils class.
     * @param bounds
     */
    @Override
    public void setBounds(IMap clientMap, IGeoBounds bounds, boolean animate) {
        if((null == bounds) ||
                !((bounds.getSouth() <= 90.0) && (bounds.getSouth() >= -90.0)) ||
                !((bounds.getNorth() <= 90.0) && (bounds.getNorth() >= -90.0)) ||
                !((bounds.getWest() <= 180.0) && (bounds.getWest() >= -180.0)) ||
                !((bounds.getEast() <= 180.0) && (bounds.getEast() >= -180.0))) {
            Log.e(TAG, "setExtents: invalid bounds");
            return;
        }

        ZoomToUtils.getInstance().zoomTo(clientMap, bounds, animate);
    }

    /**
     * getBounds gets the map's viewing area
     *
     * @param clientMap
     * @return
     */
    @Override
    public IGeoBounds getBounds(IMap clientMap) {
        return storageManager.getBounds(clientMap);
    }

    @Override
    public void drawFreehand(IMap clientMap, IGeoStrokeStyle initialStyle, IFreehandEventListener listener) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        // Make sure the map exists.
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }

        mapMapping.freehandDraw(initialStyle, listener);
    }

    @Override
    public void setFreehandStyle(IMap clientMap, IGeoStrokeStyle style) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        // Make sure the map exists.
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }

        mapMapping.setFreehandDrawStyle(style);
    }

    @Override
    public void drawFreehandExit(IMap clientMap) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        // Make sure the map exists.
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }

        mapMapping.freehandComplete();
    }

    @Override
    public Point geoToScreen(IMap clientMap, IGeoPosition pos) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        // Make sure the map exists.
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }
        IMapInstance oMapInstance = mapMapping.getMapInstance();
        return oMapInstance.geoToScreen(pos);
    }

    @Override
    public IGeoPosition screenToGeo(IMap clientMap, Point point) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        // Make sure the map exists.
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }
        IMapInstance oMapInstance = mapMapping.getMapInstance();
        return oMapInstance.screenToGeo(point);
    }

    @Override
    public Point geoToContainer(IMap clientMap, IGeoPosition pos) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        // Make sure the map exists.
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }
        IMapInstance oMapInstance = mapMapping.getMapInstance();
        return oMapInstance.geoToContainer(pos);
    }

    @Override
    public IGeoPosition containerToGeo(IMap clientMap, Point point) throws EMP_Exception {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        // Make sure the map exists.
        if (mapMapping == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map not found.");
        }
        IMapInstance oMapInstance = mapMapping.getMapInstance();
        return oMapInstance.containerToGeo(point);
    }

    @Override
    public void setMapGridType(IMap clientMap, MapGridTypeEnum gridType) {
        IClientMapToMapInstance mapMapping = storageManager.getMapMapping(clientMap);

        // Make sure the map exists.
        if (mapMapping == null) {
            throw new IllegalArgumentException("Map not found.");
        }
        mapMapping.setMapGridType(gridType);
    }

    private void loadMilStdLabelLists() {
        synchronized (CoreManager.oCommonLabels) {
            if (CoreManager.oCommonLabels.isEmpty()) {
                // These modifiers, if provided in the MilStd feature, must be shown
                // along with the icon. They are the only modifiers displayed when the label setting is REQUIRED.
                CoreManager.oRequiredLabels.add(IGeoMilSymbol.Modifier.EQUIPMENT_TYPE);
                CoreManager.oRequiredLabels.add(IGeoMilSymbol.Modifier.SIGNATURE_EQUIPMENT);
                CoreManager.oRequiredLabels.add(IGeoMilSymbol.Modifier.OFFSET_INDICATOR);
                CoreManager.oRequiredLabels.add(IGeoMilSymbol.Modifier.SPECIAL_C2_HEADQUARTERS);
                CoreManager.oRequiredLabels.add(IGeoMilSymbol.Modifier.FEINT_DUMMY_INDICATOR);
                CoreManager.oRequiredLabels.add(IGeoMilSymbol.Modifier.INSTALLATION);

                // These modifiers, if provided in the MilStd feature, are to be displayed with the icon
                // when the label setting is COMMON.
                CoreManager.oCommonLabels.addAll(CoreManager.oRequiredLabels);

                CoreManager.oCommonLabels.add(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_1);
                CoreManager.oCommonLabels.add(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_2);
                CoreManager.oCommonLabels.add(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_3);
                CoreManager.oCommonLabels.add(IGeoMilSymbol.Modifier.HIGHER_FORMATION);
                CoreManager.oCommonLabels.add(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1);
                CoreManager.oCommonLabels.add(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_2);
            }
        }
    }

    @Override
    public java.util.Set<IGeoMilSymbol.Modifier> getMilStdModifierLabelList(MilStdLabelSettingEnum eLabelSetting) {
        java.util.Set<IGeoMilSymbol.Modifier> set = null;

        if (null == eLabelSetting) {
            return null;
        }

        if (CoreManager.oCommonLabels.isEmpty()) {
            loadMilStdLabelLists();
        }
        switch (eLabelSetting) {
            case ALL_LABELS:
                break;
            case COMMON_LABELS:
                set = CoreManager.oCommonLabels;
                break;
            case REQUIRED_LABELS:
                set = CoreManager.oRequiredLabels;
                break;
        }

        return set;
    }
}
