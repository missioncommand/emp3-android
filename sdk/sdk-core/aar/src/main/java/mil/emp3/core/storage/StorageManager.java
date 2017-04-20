package mil.emp3.core.storage;

import android.os.Looper;
import android.util.Log;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import mil.emp3.api.Camera;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Point;
import mil.emp3.api.enums.ContainerEventEnum;
import mil.emp3.api.enums.FeatureEventEnum;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IContainerSet;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.core.storage.IClientMapRestoreData;
import mil.emp3.api.interfaces.core.storage.IClientMapToMapInstance;
import mil.emp3.api.interfaces.core.storage.IParentRelationship;
import mil.emp3.api.interfaces.core.storage.IStorageObjectWrapper;
import mil.emp3.api.utils.ContainerSet;
import mil.emp3.api.utils.UUIDSet;
import mil.emp3.core.utils.IdentifierVisibilityHash;
import mil.emp3.core.utils.KMLSProvider;
import mil.emp3.core.utils.milstd2525.icons.BitmapCacheFactory;
import mil.emp3.mapengine.api.FeatureVisibility;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the storage management of the core.
 */
public class StorageManager implements IStorageManager {
    private final static String TAG = StorageManager.class.getSimpleName();

    private IEventManager eventManager;

    /*
        WARNING: If you add any private members then make sure they are appropriately cleaned up in onDestroy, else there will
        be a memory leak.
    */

    private final java.util.HashMap<java.util.UUID, StorageObjectWrapper> oObjectHash = new java.util.HashMap<>();

    private final java.util.HashMap<IMap, ClientMapToMapInstance> oClientMapToMapInstanceMapping = new java.util.HashMap<>();
    private final java.util.HashMap<IMapInstance, ClientMapToMapInstance> oMapInstanceToClientMapMapping = new java.util.HashMap<>();

    // If an activity restarts e.g. orientation changes then we need to restore map engine and features. Please see Emp3DataManager for details
    // of how this works. Following data structure assists in preserving that required data.

    private final java.util.Map<String, ClientMapRestoreData> oMapNameToRestoreDataMapping = new java.util.HashMap<>();

    // The ReentrantLock is used to make manipulation of Storage Manager assets thread safe. Application threads and MapInstance threads
    // access Storage Manager assets. There are methods in Storage Manager that should only be invoked on UI thread and hence are not protected by
    // ReentrantLock but a check is made to make sure current thread is UI thread. There are some get methods that do not need protecting.
    // DON'T use the ReentrantLock in a private method as calling method has already put up a lock if required. There is no harm in doing so but
    // incurs unnecessary overhead. This could be counter intuitive but consider the data structures maintained by Storage Manager and the need
    // to manipulate them atomically makes it necessary.

    private ReentrantLock lock = new ReentrantLock();

    // We store the default stroke style for MilStd Icons keyed by the affiliation.
    private final java.util.concurrent.ConcurrentHashMap<MilStdSymbol.Affiliation, IGeoStrokeStyle> defaultIconStrokeStyleCache = new java.util.concurrent.ConcurrentHashMap<>();
    // We store the default fill style for MilStd Icons keyed by the affiliation.
    private final java.util.concurrent.ConcurrentHashMap<MilStdSymbol.Affiliation, IGeoFillStyle> defaultIconFillStyleCache = new java.util.concurrent.ConcurrentHashMap<>();

    // This object stores the bulk update feature apply list.
    private final java.util.HashMap<java.util.UUID, FeatureVisibilityList> bulkFeatureApplyList = new java.util.HashMap<>();
    private int bulkFeatureApplyListCount = 0;
    private static final int MAX_BULK_FEATURE_APPLY_COUNT = 500;

    private Thread bulkFeatureApplyThread = null;


    @Override
    public void setEventManager(IEventManager eventManager) {
        this.eventManager = eventManager;
    }

    @Override
    public IContainer findContainer(java.util.UUID targetId) {
        if (this.oObjectHash.containsKey(targetId)) {
            return this.oObjectHash.get(targetId).getObject();
        }

        return null;
    }

    /**
     *
     * @return
     */
    private boolean isCurrentThreadUIThread() {
        return (Thread.currentThread().getId() == Looper.getMainLooper().getThread().getId());
    }
    /**
     * In order to restore the map after activity restart, it is required that application invoke setName on the instance of the
     * Map they retrieved from the view hierarchy, like MapView. When setName is invoked we create a mapping between the name
     * and other required data like UUID, engineApkName, engineClassName etc.
     * On restart we find this information and use it to replace the UUID of the map object so that old overlay/feature hierarchy
     * or anything else referring to it can find it.
     * @param map
     * @return
     * @throws EMP_Exception
     */

    @Override
    public ClientMapRestoreData addMapNameToRestoreDataMapping(IMap map) throws EMP_Exception {
        if((null == map.getName()) || (0 == map.getName().length())) {
            Log.e(TAG, "You must set the map name before calling addMapNameToRestoreDataMapping to enable EMP middleware state restoration on activity restart");
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "You must set the map name before calling addMapNameToRestoreDataMapping");
        }

        ClientMapRestoreData cmrd = null;
        try {
            lock.lock();
            cmrd = oMapNameToRestoreDataMapping.get(map.getName());
            if (null != cmrd) {
                map.setGeoId(cmrd.getUuid()); // Use the previous UUID.
            } else {
                cmrd = new ClientMapRestoreData();
                cmrd.setName(map.getName());
                cmrd.setUuid(map.getGeoId());
                oMapNameToRestoreDataMapping.put(map.getName(), cmrd);
            }
        } finally {
            lock.unlock();
        }
        return cmrd;
    }

    /**
     * When application associates a specific map engine with a client map we want to maintain that information across activity restart
     * so that we can bring up the correct engine. This method must be executed on UI thread; this is not obvious by looking at the
     * content of this method. It is currently executed when new map engine is associated with the map.
     *
     * @param map
     * @param engineClassName
     * @param engineApkName
     */
    @Override
    public void updateMapNameToRestoreDataMapping(IMap map, String engineClassName, String engineApkName) throws EMP_Exception {

        if(!isCurrentThreadUIThread()) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "Must be executed on UI Thread");
        }

        if((null == map.getName()) || (0 == map.getName().length())) {
            Log.i(TAG, "You must set the map name before calling updateMapNameToRestoreDataMapping");
            return;
        }

        ClientMapRestoreData cmrd = oMapNameToRestoreDataMapping.get(map.getName());
        if(null == cmrd) {
            Log.e(TAG, "updateMapNameToRestoreDataMapping: Map " + map.getName() + " not found");
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Map " + map.getName() + " not found");
        }

        if((null == engineClassName) || (0 == engineClassName.length())) {
            Log.e(TAG, "updateMapNameToRestoreDataMapping: engineClass Name must be non null");
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "engineClass Name must be non null");
        }

        cmrd.setEngineApkName(engineApkName);
        cmrd.setEngineClassName(engineClassName);
    }

    @Override
    public ClientMapRestoreData getRestoreData(IMap map) {
        if((null == map.getName()) || (0 == map.getName().length())) {
            Log.i(TAG, "You must set the map name before calling getRestoreData if you expect EMP3 middleware to restore state on activity restart");
            return null;
        }
        return oMapNameToRestoreDataMapping.get(map.getName());
    }
    /**
     * When activity is destroyed application can either choose to delete all EMP3 storage data and take on the responsibility
     * to setup maps/overalys/features. OR they can let the EMP3 middleware do the restoration.
     *
     * clearMapMapping is invoked from Emp3Manager.onDestroy when application expects EMP3 middleware to restore map engine/overlays/features.
     * NOTE that clientMap will still be created by Android based on view hierarchy and RestoreDataMappings helps us reconcile that clientMap.
     */
    private void clearMapMapping() {
        java.util.HashMap<IMap, ClientMapToMapInstance> tmp = new java.util.HashMap<>();
        tmp.putAll(oClientMapToMapInstanceMapping);

        for(Map.Entry<IMap, ClientMapToMapInstance> cm2mi: tmp.entrySet()) {
            IMap clientMap = cm2mi.getKey();
            ClientMapRestoreData cmrd = oMapNameToRestoreDataMapping.get(clientMap.getName());
            if(null == cmrd) {
                Log.e(TAG, "ClientMapRestoreData not found for " + clientMap.getName() + ", restore will NOT work");
            } else {
                cmrd.setObjectWrapper(this.oObjectHash.get(clientMap.getGeoId()));
                if(null == cm2mi.getValue().getCamera()) {
                    Log.e(TAG, "clearMapMapping camera is null");
                } else {
                    // Default camera object causes NASA WW to throw an exception. Until that is fixed treat default camera object
                    // as null and don't set it.
                    // TODO This code will eventually be removed.
                    ICamera tmpCamera = new Camera();
                    if((tmpCamera.getLongitude() == cm2mi.getValue().getCamera().getLongitude()) &&
                            (tmpCamera.getLatitude() == cm2mi.getValue().getCamera().getLatitude()) &&
                            (tmpCamera.getAltitude() == cm2mi.getValue().getCamera().getAltitude()) &&
                            (tmpCamera.getAltitudeMode() == cm2mi.getValue().getCamera().getAltitudeMode()) &&
                            (tmpCamera.getHeading() == cm2mi.getValue().getCamera().getHeading()) &&
                            (tmpCamera.getRoll() == cm2mi.getValue().getCamera().getRoll()) &&
                            (tmpCamera.getTilt() == cm2mi.getValue().getCamera().getTilt())) {

                    } else {
                        cmrd.setCamera(cm2mi.getValue().getCamera());
                    }
                }
            }
            removeMapMapping(clientMap, cm2mi.getValue().getMapInstance());
        }
        tmp.clear();
    }

    /**
     * If keepState is true then delete all storage manager data
     * else only clear the mapping between client map and map engine
     * This is invoked as part of Android Activity life cycle and will be executed on UI thread so no check is required.
     *
     * @param keepState
     */
    @Override
    public void onSaveInstanceState(boolean keepState) {
        if(!keepState) {
            Log.i(TAG, "EMP3 shall not restore the map");
            this.oObjectHash.clear();
            this.oClientMapToMapInstanceMapping.clear();
            this.oMapInstanceToClientMapMapping.clear();
            this.oMapNameToRestoreDataMapping.clear();
            this.defaultIconFillStyleCache.clear();
            this.defaultIconStrokeStyleCache.clear();
            eventManager.clear();
        } else {
            Log.i(TAG, "EMP3 shall restore the map");
            clearMapMapping();
        }
    }

    /**
     * If this mapping is created as a result of restart of an activity then we need to fetch the RestoreData (If it exists)
     * From the restoration data we mainly get the childList, this is encapsulated in the copy method of StorageWobjecWrapper.
     * @param clientMap
     * @param mapInstance
     */
    private void addMapMapping(IMap clientMap, IMapInstance mapInstance) {
        ClientMapToMapInstance oMapping = new ClientMapToMapInstance(clientMap, mapInstance);
        StorageObjectWrapper oWrapper = new StorageObjectWrapper<>(clientMap);

        this.oClientMapToMapInstanceMapping.put(clientMap, oMapping);
        this.oMapInstanceToClientMapMapping.put(mapInstance, oMapping);

        // Check and restore data like childList if this is an activity restart and application has chosen this option
        // in Emp3DataManager.

        IClientMapRestoreData cmrd = oMapNameToRestoreDataMapping.get(clientMap.getName());
        if(null != cmrd) {
            if(null != cmrd.getObjectWrapper()) {
                oWrapper.copy(cmrd.getObjectWrapper());
            }
            cmrd.setObjectWrapper(oWrapper);
        }

        this.oObjectHash.put(clientMap.getGeoId(), oWrapper);
    }

    private void removeMapMapping(IMap clientMap, IMapInstance mapInstance) {
        if(null != clientMap) {
            this.oClientMapToMapInstanceMapping.remove(clientMap);
            this.oObjectHash.remove(clientMap.getGeoId());
        }
        if(null != mapInstance) {
            this.oMapInstanceToClientMapMapping.remove(mapInstance);
        }
    }

    @Override
    public IMapInstance getMapInstance(IMap clientMap) {
        if (this.oClientMapToMapInstanceMapping.containsKey(clientMap)) {
            return this.oClientMapToMapInstanceMapping.get(clientMap).getMapInstance();
        }
        return null;
    }

    @Override
    public IClientMapToMapInstance getMapMapping(IMapInstance mapInstance) {
        if (this.oMapInstanceToClientMapMapping.containsKey(mapInstance)) {
            return this.oMapInstanceToClientMapMapping.get(mapInstance);
        }
        return null;
    }

    @Override
    public IClientMapToMapInstance getMapMapping(IMap clientMap) {
        if (this.oClientMapToMapInstanceMapping.containsKey(clientMap)) {
            return this.oClientMapToMapInstanceMapping.get(clientMap);
        }
        return null;
    }

    /**
     * This was changed to private as MapView and MapFragment will always call swapMapInstance.
     * @param clientMap
     * @param mapInstance
     * @throws EMP_Exception
     */
    private void addMapInstance(IMap clientMap, IMapInstance mapInstance)
            throws EMP_Exception {
        if ((clientMap == null) || (mapInstance == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "clientMap nor mapInstance can be null");
        }

        addMapMapping(clientMap, mapInstance);
        this.registerMapInstanceEvents(mapInstance);
    }

    private void registerMapInstanceEvents(IMapInstance mapInstance){
        IClientMapToMapInstance mapping = getMapMapping(mapInstance);

        mapInstance.addMapInstanceFeatureUserInteractionEventListener(mapping);
        mapInstance.addMapInstanceUserInteractionEventListener(mapping);
        mapInstance.addMapInstanceStateChangeEventListener(mapping);
        mapInstance.addMapInstanceViewChangeEventListener(mapping);
        mapInstance.addMapInstanceFeatureAddedEventListener(mapping);
        mapInstance.addMapInstanceFeatureRemovedEventListener(mapping);
    }

    /**
     * If mapping between clientMap and old mapInstance doesn't exist the add one.
     * If mapping exists then remove and add a new mapping; copy the old parameters from old Mapping to new mapping.
     * This preserves things like iconSize, mapService etc.
     * This method will be invoked on UI thread by either MapFragment and MapView and no check is added for thread safe operation
     *
     * @param clientMap
     * @param newMapInstance
     * @throws EMP_Exception
     */

    @Override
    public void swapMapInstance(IMap clientMap, IMapInstance newMapInstance)
            throws EMP_Exception {

        IClientMapToMapInstance mapping = getMapMapping(clientMap);
        if (null == mapping) {
            Log.d(TAG, "No mapping exists, add a new one");
            addMapInstance(clientMap, newMapInstance);
        } else {
            removeMapMapping(clientMap, getMapInstance(clientMap));
            addMapInstance(clientMap, newMapInstance);
            IClientMapToMapInstance newMapping = getMapMapping(clientMap);
            newMapping.copy(mapping);
        }
    }

    /**
     * Give a camera get all the maps that use this camera. This is used to proces changes to the camera and apply them to
     * appropriate maps.
     * @param camera
     * @return
     */
    public  List<IClientMapToMapInstance> getMappings(ICamera camera) {
        List<IClientMapToMapInstance> list = new ArrayList<>();

        if((null != camera) && (null != camera.getGeoId())) {
            for(Map.Entry<IMap, ClientMapToMapInstance> cm2mi: oClientMapToMapInstanceMapping.entrySet()) {
                if((null != cm2mi.getValue().getCamera()) && (0 == camera.getGeoId().compareTo(cm2mi.getValue().getCamera().getGeoId()))) {
                    list.add(cm2mi.getValue());
                }
            }
        }
        return list;
    }

    @Override
    public List<IClientMapToMapInstance> getMappings(ILookAt lookAt) {
        List<IClientMapToMapInstance> list = new ArrayList<>();

        if((null != lookAt) && (null != lookAt.getGeoId())) {
            for(Map.Entry<IMap, ClientMapToMapInstance> cm2mi: oClientMapToMapInstanceMapping.entrySet()) {
                if((null != cm2mi.getValue().getLookAt()) && (0 == lookAt.getGeoId().compareTo(cm2mi.getValue().getLookAt().getGeoId()))) {
                    list.add(cm2mi.getValue());
                }
            }
        }
        return list;
    }

    @Override
    public IContainerSet getParentsOf(IGeoBase oObject) {
        IContainerSet oList;
        StorageObjectWrapper oWrapper;
        try {
            lock.lock();
            if (this.oObjectHash.containsKey(oObject.getGeoId())) {
                oWrapper = this.oObjectHash.get(oObject.getGeoId());
                oList = oWrapper.getParents();
            } else {
                oList = new ContainerSet();
            }
        } finally {
            lock.unlock();
        }

        return oList;
    }

    @Override
    public void setVisibilityOnMap(IMap map, IUUIDSet targetIdList, VisibilityActionEnum actionEnum,
            Object userContext) throws EMP_Exception {
        IStorageObjectWrapper targetWrapper;
        IStorageObjectWrapper parentWrapper;
        VisibilityStateEnum prevVisibileState;
        java.util.UUID mapId = map.getGeoId();
        IdentifierVisibilityHash idVisibilityList = new IdentifierVisibilityHash();
        Map<UUID, IParentRelationship> parentRelationshipHash;

        try {
            lock.lock();
            for (java.util.UUID targetId : targetIdList) {
                targetWrapper = this.oObjectHash.get(targetId);
                parentRelationshipHash = targetWrapper.getParentList();
                prevVisibileState = targetWrapper.getVisibilityOnMap(mapId);

                switch (actionEnum) {
                    case SHOW_ALL:
                    case TOGGLE_ON:
                        this.setVisibilityOnChildren(idVisibilityList, mapId, targetWrapper, actionEnum);
                        for (java.util.UUID parentId : parentRelationshipHash.keySet()) {
                            parentWrapper = this.oObjectHash.get(parentId);
                            targetWrapper.setVisibilityWithParentOnMap(mapId, parentId, VisibilityStateEnum.VISIBLE);

                            // If something is being turn on we must go up the tree.
                            this.setVisibilityOnParents(idVisibilityList, mapId, parentWrapper, actionEnum);
                        }
                        if (prevVisibileState == VisibilityStateEnum.HIDDEN) {
                            // The target was off now its on.
                            idVisibilityList.putFeature(targetWrapper.getObject(), true);
                        }
                        break;
                    case HIDE_ALL:
                    case TOGGLE_OFF:
                    default:
                        for (java.util.UUID parentId : parentRelationshipHash.keySet()) {
                            targetWrapper.setVisibilityWithParentOnMap(mapId, parentId, VisibilityStateEnum.HIDDEN);
                        }
                        this.setVisibilityOnChildren(idVisibilityList, mapId, targetWrapper, actionEnum);
                        if ((prevVisibileState == VisibilityStateEnum.VISIBLE) &&
                                (targetWrapper.getVisibilityOnMap(mapId) == VisibilityStateEnum.HIDDEN)) {
                            // The target was visible but now its hidden.
                            idVisibilityList.putFeature(targetWrapper.getObject(), false);
                        }
                        break;
                }
                // Generate the event.
                eventManager.generateVisibilityEvent(actionEnum, targetWrapper.getObject(), null, map, userContext);
            }

            processVisibilityList(map, idVisibilityList, userContext);
        } finally {
            lock.unlock();
        }
    }

    private void setVisibilityOnChildren(
            IdentifierVisibilityHash idVisibilityList,
            java.util.UUID mapId,
            IStorageObjectWrapper wrapper,
            VisibilityActionEnum actionEnum) {
        VisibilityStateEnum visibilityStateOnMap;
        java.util.UUID wrapperId = wrapper.getGeoId();
        java.util.Map<java.util.UUID, IStorageObjectWrapper> childrenList =
                wrapper.getChildrenList();

        for (IStorageObjectWrapper childWraper: childrenList.values()) {
            switch (actionEnum) {
                case SHOW_ALL:
                    visibilityStateOnMap = childWraper.getVisibilityOnMap(mapId);
                    childWraper.setVisibilityWithParentOnMap(mapId, wrapperId, VisibilityStateEnum.VISIBLE);
                    if (visibilityStateOnMap == VisibilityStateEnum.HIDDEN) {
                        // The child is off on the map. We need to turn it on.
                        idVisibilityList.putFeature(childWraper.getObject(), true);
                    }
                    break;
                case TOGGLE_ON:
                    visibilityStateOnMap = childWraper.getVisibilityWithParentOnMap(mapId, wrapperId);
                    switch (visibilityStateOnMap) {
                        case VISIBLE_ANCESTOR_HIDDEN:
                            // This child was visible but hidden do to the parent.
                            // Now that the parent is visible it must be made visible.
                            childWraper.setVisibilityWithParentOnMap(mapId, wrapperId, VisibilityStateEnum.VISIBLE);
                            idVisibilityList.putFeature(childWraper.getObject(), true);
                            break;
                        case VISIBLE:
                            // The child was already visible. no change needed.
                        case HIDDEN:
                        default:
                            // This child is hidden so we leave it hidden.
                            // If its visibility does not change we don't need to check its children.
                            continue;
                    }
                    break;
                case HIDE_ALL:
                    visibilityStateOnMap = childWraper.getVisibilityOnMap(mapId);
                    childWraper.setVisibilityWithParentOnMap(mapId, wrapperId, VisibilityStateEnum.HIDDEN);
                    if (visibilityStateOnMap == VisibilityStateEnum.VISIBLE) {
                        // The child was previously visible on the map.
                        // we need to see if it is now set hidden.
                        if (childWraper.getVisibilityOnMap(mapId) == VisibilityStateEnum.HIDDEN) {
                            // We need to turn it off on the map.
                            idVisibilityList.putFeature(childWraper.getObject(), false);
                        }
                    }
                    break;
                case TOGGLE_OFF:
                default:
                    visibilityStateOnMap = childWraper.getVisibilityWithParentOnMap(mapId, wrapperId);
                    switch (visibilityStateOnMap) {
                        case VISIBLE:
                            // This child was visible, but now the parent is not.
                            // So we set it to VISIBLE_ANCESTOR_HIDDEN.
                            childWraper.setVisibilityWithParentOnMap(mapId, wrapperId, VisibilityStateEnum.VISIBLE_ANCESTOR_HIDDEN);
                            idVisibilityList.putFeature(childWraper.getObject(), false);
                            break;
                        case VISIBLE_ANCESTOR_HIDDEN:
                        case HIDDEN:
                        default:
                            // The child's visibility does not change state so we don't need
                            // to check its children.
                            continue;
                    }
                    break;
            }
            this.setVisibilityOnChildren(idVisibilityList, mapId, childWraper, actionEnum);
        }
    }

    private void setVisibilityOnParents(
            IdentifierVisibilityHash idVisibilityList,
            java.util.UUID mapId,
            IStorageObjectWrapper wrapper,
            VisibilityActionEnum actionEnum) {
        java.util.UUID parentId;
        VisibilityStateEnum currentVisibility;
        VisibilityStateEnum parentVisibility;
        IStorageObjectWrapper parentWrapper;
        java.util.Map<java.util.UUID, ParentRelationship> parentList = wrapper.getParentList();

        if (wrapper.getObject() instanceof IMap) {
            return;
        }

        currentVisibility = wrapper.getVisibilityOnMap(mapId);
        switch (actionEnum) {
            case SHOW_ALL:
            case TOGGLE_ON:
                if (currentVisibility == VisibilityStateEnum.HIDDEN) {
                    // This container was off and now it needs to be on.
                    idVisibilityList.putFeature(wrapper.getObject(), true);
                }
                for (ParentRelationship relationship: parentList.values()) {
                    parentWrapper = relationship.getParentWrapper();

                    if (!(parentWrapper.getObject() instanceof IMap)) {
                        parentVisibility = parentWrapper.getVisibilityOnMap(mapId);
                        parentId = parentWrapper.getGeoId();

                        // All parents up to the root get turn on
                        wrapper.setVisibilityWithParentOnMap(mapId, parentId, VisibilityStateEnum.VISIBLE);
                        if (parentVisibility == VisibilityStateEnum.HIDDEN) {
                            // This parent was off and now its on.
                            idVisibilityList.putFeature(parentWrapper.getObject(), true);
                        }
                    }
                }
                break;
            case HIDE_ALL:
            case TOGGLE_OFF:
            default:
                // These should never happen.
                // If something is being turn off you don't go up the tree.
                break;
        }
    }

    @Override
    public boolean isOnMap(IMap map, IContainer target) {
        StorageObjectWrapper oWrapper = this.oObjectHash.get(target.getGeoId());
        if (oWrapper == null) {
            return false;
        }

        return oWrapper.isOnMap(map.getGeoId());
    }

    @Override
    public void setVisibilityOnMap(IMap map, IContainer target, IContainer parent, VisibilityActionEnum actionEnum,
                                   Object userContext) throws EMP_Exception {
        java.util.UUID parentId = parent.getGeoId();
        java.util.UUID targetId = target.getGeoId();
        java.util.UUID mapId = map.getGeoId();

        try {
            lock.lock();
            StorageObjectWrapper targetWrapper = this.oObjectHash.get(targetId);
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(parentId);
            VisibilityStateEnum prevVisibileState = targetWrapper.getVisibilityOnMap(mapId);
            IdentifierVisibilityHash idVisibilityList = new IdentifierVisibilityHash();

            if (!targetWrapper.isOnMap(mapId)) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Target is not on the map.");
            } else if (!targetWrapper.hasParent(parentId)) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid parent.");
            }

            switch (actionEnum) {
                case SHOW_ALL:
                case TOGGLE_ON:
                    targetWrapper.setVisibilityWithParentOnMap(mapId, parentId, VisibilityStateEnum.VISIBLE);
                    this.setVisibilityOnChildren(idVisibilityList, mapId, targetWrapper, actionEnum);
                    if (prevVisibileState == VisibilityStateEnum.HIDDEN) {
                        // The target was off now its on.
                        idVisibilityList.putFeature(target, true);
                    }
                    // If something is being turn on we must go up the tree.
                    this.setVisibilityOnParents(idVisibilityList, mapId, parentWrapper, actionEnum);
                    break;
                case HIDE_ALL:
                case TOGGLE_OFF:
                default:
                    targetWrapper.setVisibilityWithParentOnMap(mapId, parentId, VisibilityStateEnum.HIDDEN);
                    this.setVisibilityOnChildren(idVisibilityList, mapId, targetWrapper, actionEnum);
                    if ((prevVisibileState == VisibilityStateEnum.VISIBLE) &&
                            (targetWrapper.getVisibilityOnMap(mapId) == VisibilityStateEnum.HIDDEN)) {
                        // The target was visible but now its hidden.
                        idVisibilityList.putFeature(target, false);
                    }
                    break;
            }

            processVisibilityList(map, idVisibilityList, userContext);
        } finally {
            lock.unlock();
        }
        // Generate the event.
        eventManager.generateVisibilityEvent(actionEnum, target, parent, map, userContext);
    }

    /**
     * Translate visibility actions into add/remove calls on MapInstance.
     * @param map
     * @param idVisibilityList
     */
    private void processVisibilityList(IMap map, IdentifierVisibilityHash idVisibilityList, Object userContext) {
        if (!idVisibilityList.isEmpty()) {
            // Now send it to the map.
            IMapInstance mapInstance = this.getMapInstance(map);
            // mapInstance.setVisibility(idVisibilityList);

            IUUIDSet removeSet = new UUIDSet();
            FeatureVisibilityList fvList = new FeatureVisibilityList();
            for (UUID uuid : idVisibilityList.keySet()) {
                if (idVisibilityList.get(uuid)) {
                    StorageObjectWrapper sow = this.oObjectHash.get(uuid);
                    if ((null != sow) && (sow.getObject() instanceof IFeature)) {
                        fvList.add(new FeatureVisibility((IFeature) sow.getObject(), true));
                    }
                } else {
                    removeSet.add(uuid);
                }
            }
            if (!fvList.isEmpty()) {
                mapInstance.addFeatures(fvList, userContext);
            }
            if (!removeSet.isEmpty()) {
                mapInstance.removeFeatures(removeSet, userContext);
            }
        }
    }
    @Override
    public VisibilityStateEnum getVisibilityOnMap(IMap map, IContainer target) {
        VisibilityStateEnum eRet = VisibilityStateEnum.HIDDEN;
        try {
            lock.lock();
            StorageObjectWrapper targetWrapper = this.oObjectHash.get(target.getGeoId());

            if (null != targetWrapper) {
                eRet = targetWrapper.getVisibilityOnMap(map.getGeoId());
            }
        } finally {
            lock.unlock();
        }

        return eRet;
    }

    @Override
    public VisibilityStateEnum getVisibilityOnMap(IMap map, IContainer target, IContainer parent) {
        try {
            lock.lock();
            StorageObjectWrapper targetWrapper = this.oObjectHash.get(target.getGeoId());

            return targetWrapper.getVisibilityWithParentOnMap(map.getGeoId(), parent.getGeoId());
        } finally {
            lock.unlock();
        }
    }

    /**
     * Recursively removes children of container wrapper if container wrapper has no parents. Removes container wrapper from
     * oObjectHash if container wrapper has no parents (not doing this would cause a memory leak).
     * @param transactionList
     * @param mapId - from which to remove the containerWrapper
     * @param containerWrapper - that is being removed.
     */
    private void proessChildrenRemoves(TransactionList transactionList,
            java.util.UUID mapId,
            StorageObjectWrapper containerWrapper) {
        StorageObjectWrapper childWrapper;
        boolean bHasParents = containerWrapper.hasParents();
        java.util.Set<java.util.UUID> childIdList = new java.util.HashSet<>(containerWrapper.getChildIdList());

        //Log.d(TAG, "containerWrapper " + containerWrapper.getGeoId() + " bHasParents " + bHasParents + " children " + childIdList.size() );
        for (java.util.UUID uuId: childIdList) {

            childWrapper = this.oObjectHash.get(uuId);
            //Log.d(TAG, "containerWrapper " + containerWrapper.getGeoId() + " childWrapper " + childWrapper.getGeoId() +
            //        " Name " + childWrapper.getObject().getName());

            if (!bHasParents) {
                // The container has no more parents so we need to remove all the children.
                //Log.d(TAG, "containerWrapper " + containerWrapper.getGeoId() + " remove childWrapper " + childWrapper.getGeoId() +
                //        " Name " + childWrapper.getObject().getName());
                containerWrapper.removeChild(uuId);
            } else {
                // childWrapper ParentRelationShip mapVisibility needs to be cleaned up.
                // childWrapper cannot reach the 'mapId' via this containerWrapper.
                //Log.d(TAG, "containerWrapper " + containerWrapper.getGeoId() + " clearVisibilityOnMap childWrapper " + childWrapper.getGeoId() +
                //        " Name " + childWrapper.getObject().getName());
                childWrapper.clearMapVisibility(containerWrapper, mapId);
            }


            if (!childWrapper.isOnMap(mapId)) {
                //Log.d(TAG, "childWrapper " + childWrapper.getGeoId() + " is not on map ");
                // The child is no longer on the map.
                if (childWrapper.hasChildren()) {
                    this.proessChildrenRemoves(transactionList, mapId, childWrapper);
                } else {
                    // As we are not calling processChildrenRemoves on this, we must remove it here if it has no parent
                    if(!childWrapper.hasParents()) {
                        this.oObjectHash.remove(childWrapper.getGeoId());
                    }
                }
                transactionList.removeContainerFromMap(mapId, childWrapper);
            }
        }
        if(!bHasParents) {
            this.oObjectHash.remove(containerWrapper.getGeoId());
        }
    }

    private void proessChildrenAdds(TransactionList transactionList,
            java.util.UUID mapId,
            StorageObjectWrapper containerWrapper) {
        StorageObjectWrapper childWrapper;
        boolean bHasParents = containerWrapper.hasParents();
        java.util.Set<java.util.UUID> childIdList = new java.util.HashSet<>(containerWrapper.getChildIdList());

        for (java.util.UUID uuId: childIdList) {
            childWrapper = this.oObjectHash.get(uuId);

            if (childWrapper.isOnMap(mapId)) {
                // The child is no longer on the map.
                if (childWrapper.hasChildren()) {
                    this.proessChildrenAdds(transactionList, mapId, childWrapper);
                }
                transactionList.addContainerToMap(mapId, childWrapper);
            }
        }
    }

    /**
     * Updates the transaction list and removes any children of childWrapper that need to be removed.
     * @param transactionList
     * @param preMapList
     * @param postMapList
     * @param parentWrapper
     * @param childWrapper
     */
    private void processRequest(TransactionList transactionList,
            IUUIDSet preMapList,
            IUUIDSet postMapList,
            StorageObjectWrapper parentWrapper,
            StorageObjectWrapper childWrapper) {

        for (java.util.UUID uuId: preMapList) {
            if (!postMapList.contains(uuId)) {
                // This child was on the map before and now not.
                // It need to br removed from this map.
                transactionList.removeContainerFromMap(uuId, childWrapper);
                this.proessChildrenRemoves(transactionList, uuId, childWrapper);
            }
        }

        for (java.util.UUID uuId: postMapList) {
            if (!preMapList.contains(uuId)) {
                // The child was not on this map and it is now.
                // We need to be added to this map.
                transactionList.addContainerToMap(uuId, childWrapper);
                this.proessChildrenAdds(transactionList, uuId, childWrapper);
            }
        }
    }

    private void addChildren(TransactionList transactionList, StorageObjectWrapper parentWrapper, StorageObjectWrapper childWrapper, boolean visible)
             throws EMP_Exception {
        IUUIDSet preMapList;
        IUUIDSet postMapList;

        // Add it to the parent if it is not a child already.
        if (!parentWrapper.hasChild(childWrapper.getGeoId())) {
            // Get The map list before the operation
            preMapList = childWrapper.getMapList();
            // We need to add it as a child of the parent.
            parentWrapper.addChild(childWrapper, (visible ? VisibilityStateEnum.VISIBLE : VisibilityStateEnum.HIDDEN));
            // Get the map list after the operation.
            postMapList = childWrapper.getMapList();

            this.processRequest(transactionList, preMapList, postMapList, parentWrapper, childWrapper);
        }
    }

    /**
     * This method send the features in the bulkFeatureApplyList to the map engines.
     */
    private void processBulkFeatureApplyList(Object userContext) {
        if (this.bulkFeatureApplyList.isEmpty()) {
            return;
        }

        // Loop thru the bulk Feature Apply List.
        for (java.util.UUID uuid: this.bulkFeatureApplyList.keySet()) {
            StorageObjectWrapper sowMap = this.oObjectHash.get(uuid);

            if (null != sowMap) {
                // Get the IMap.
                IMap clientMap = (IMap) sowMap.getObject();
                IClientMapToMapInstance mapMapping = this.getMapMapping(clientMap);

                if (null != mapMapping) {
                    FeatureVisibilityList fvList = this.bulkFeatureApplyList.get(uuid);
                    VisibilityStateEnum visibility;
                    FeatureVisibility featureVisibility;
                    Iterator<FeatureVisibility> iterator = fvList.iterator();

                    // Go thru all the feature and get their latest visibility.
                    while (iterator.hasNext()) {
                        featureVisibility = iterator.next();
                        if (this.oObjectHash.containsKey(featureVisibility.feature.getGeoId())) {
                            visibility = getVisibilityOnMap(clientMap, featureVisibility.feature);
                            featureVisibility.visible = (visibility == VisibilityStateEnum.VISIBLE);
                        } else {
                            // The feature was removed from the system.
                            iterator.remove();
                        }
                    }

                    if (!fvList.isEmpty()) {
                        // Send the list to the map engine.
                        mapMapping.getMapInstance().addFeatures(fvList, userContext);
                    }
                }
            }
        }
        this.bulkFeatureApplyListCount = 0;
        this.bulkFeatureApplyList.clear();
        //Log.d(TAG, "Processed List.");
    }

    /**
     * Find all MapInstances and invoke apply method on each.
     * @param feature
     * @param batch If set to true the request is queued and processed in batch mode. If false the request is triggered immediately along with all others in the queue.
     * @throws mil.emp3.api.exceptions.EMP_Exception
     */
    @Override
    public void apply(IFeature feature, boolean batch, Object userContext) throws EMP_Exception {
        // Ensure the feature has an altitude mode.
        this.setDefaultAltitudeMode(feature);
        try {
            lock.lock();
            StorageObjectWrapper sow = this.oObjectHash.get(feature.getGeoId());
            IUUIDSet oMapList = null;
            FeatureVisibilityList fvList = null;

            if (null != sow) {
                oMapList = sow.getMapList(); // This is the list of IMap on which the feature was added
                if (null != oMapList) {
                    for (java.util.UUID uuid : oMapList) {
                        StorageObjectWrapper sowMap = this.oObjectHash.get(uuid);
                        VisibilityStateEnum visibility = getVisibilityOnMap((IMap) sowMap.getObject(), sow.getObject());
                        IClientMapToMapInstance mapMapping = this.getMapMapping((IMap) sowMap.getObject());
                        if ((null != mapMapping) && mapMapping.canPlot(feature)) {
                            FeatureVisibility fv = new FeatureVisibility(feature, (VisibilityStateEnum.VISIBLE == visibility));

                            if (this.bulkFeatureApplyList.containsKey(uuid)) {
                                fvList = this.bulkFeatureApplyList.get(uuid);
                            } else {
                                fvList = new FeatureVisibilityList();
                                this.bulkFeatureApplyList.put(uuid, fvList);
                            }
                            fvList.add(fv);
                            this.bulkFeatureApplyListCount++;
                        }
                    }
                }
            }
            if ((null == sow) || (null == oMapList)) {
                Log.e(TAG, "StorageObjectWrapper not found " + feature.getClass().getSimpleName() + " " + feature.getGeoId());
                throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, "Feature is not on map " + feature.getClass().getSimpleName() +
                        " " + feature.getGeoId());
            }

            if ((null != fvList) && !fvList.isEmpty()) {
                if (!batch || (this.bulkFeatureApplyListCount >= StorageManager.MAX_BULK_FEATURE_APPLY_COUNT)) {
                    //Log.d(TAG, "Non Batch Apply processing :" + this.bulkFeatureApplyListCount);
                    this.processBulkFeatureApplyList(userContext);
                } else {
                    //Log.d(TAG, "Batch Apply processing :" + this.bulkFeatureApplyListCount);
                    this.startFeatureApplyThread(userContext);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void startFeatureApplyThread(final Object userContext) {
        if (null == this.bulkFeatureApplyThread) {
            this.bulkFeatureApplyThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    boolean bContinue = true;

                    while (bContinue) {
                        try {
                            Thread.sleep(100);

                            if (StorageManager.this.bulkFeatureApplyListCount > 0) {
                                try {
                                    lock.lock();
                                    //Log.d(TAG, "Thread processing list:" + StorageManager.this.bulkFeatureApplyListCount);
                                    StorageManager.this.processBulkFeatureApplyList(userContext);
                                } finally {
                                    lock.unlock();
                                }
                            } else {
                                bContinue = false;
                            }
                        } catch (InterruptedException ex) {
                            bContinue = false;
                        }
                    }
                    StorageManager.this.bulkFeatureApplyThread = null;
                    //Log.d(TAG, "Thread exiting.");
                }
            }, "Bulk Feature Apply Thread");
            this.bulkFeatureApplyThread.start();
            //Log.d(TAG, "Starting Thread.");
        }
    }

    private void executeTransaction(TransactionList transactionList, Object userContext) {
        StorageObjectWrapper wrapper;
        IMap map;
        IMapInstance mapInstance;
        ClientMapToMapInstance cm2mInstance;
        FeatureVisibilityList addFeatureList;
        IUUIDSet removeFeatureList;
        java.util.HashMap<java.util.UUID, IUUIDSet> mapRemoveFeatures;
        java.util.HashMap<java.util.UUID, FeatureVisibilityList> mapAddFeatures;

        mapRemoveFeatures = transactionList.getFeatureRemoves();
        for (java.util.UUID mapId: mapRemoveFeatures.keySet()) {
            wrapper = this.oObjectHash.get(mapId);
            map = (IMap) wrapper.getObject();
            cm2mInstance = this.oClientMapToMapInstanceMapping.get(map);
            mapInstance = cm2mInstance.getMapInstance();
            removeFeatureList = mapRemoveFeatures.get(mapId);

            for (Iterator<UUID> iter = removeFeatureList.iterator(); iter.hasNext(); ) {
                UUID featureId = iter.next();

                // If feature was selected using IMap.selectFeature interface then remove feature from selected list
                cm2mInstance.deselectFeature(featureId);

                // checks if a feature being removed is being edited, and cancels it
                // We will stopEditing only if we are not already in the finishing state of the editor.
                if (cm2mInstance.isEditing() && !cm2mInstance.getEditor().isFinishing()) {
                    if (cm2mInstance.getEditor().getFeatureGeoId().equals(featureId)) {
                        try {
                            cm2mInstance.stopEditing();
                        } catch (Exception e) {
                            Log.e(TAG, "ERROR attempting to cancel edit: " + e.getMessage(), e);
                        }
                    }
                }
            }

            mapInstance.removeFeatures(removeFeatureList, userContext);
        }

        mapAddFeatures = transactionList.getFeatureAdds();
        for (java.util.UUID mapId: mapAddFeatures.keySet()) {
            addFeatureList = mapAddFeatures.get(mapId);

            //Log.d(TAG, "Execute Transaction added " + addFeatureList.size());
            if (this.bulkFeatureApplyList.containsKey(mapId)) {
                FeatureVisibilityList fvList = this.bulkFeatureApplyList.get(mapId);
                fvList.addAll(addFeatureList);
            } else {
                this.bulkFeatureApplyList.put(mapId, addFeatureList);
            }
            this.bulkFeatureApplyListCount += addFeatureList.size();
        }

        if (mapAddFeatures.size() > 0) {
            this.processBulkFeatureApplyList(userContext);
        }
    }

    /**
     * Recursively gets all children of parentWrapper
     * @param children
     * @param parentWrapper
     */
    private void getChildren(java.util.Set<IContainer> children,
                                       StorageObjectWrapper parentWrapper) {
        if(null == parentWrapper) return;

        //Log.d(TAG, "getChildren " + parentWrapper.getObject().getName());
        java.util.Set<java.util.UUID> childIdList = new java.util.HashSet<>(parentWrapper.getChildIdList());
        if(null == childIdList) return;

        for (java.util.UUID uuId: childIdList) {

            StorageObjectWrapper childWrapper = this.oObjectHash.get(uuId);
            if((null == childWrapper) || (children.contains(childWrapper.getObject()))) {
                continue;
            } else {
                children.add(childWrapper.getObject());
                getChildren(children, childWrapper);
            }
        }
    }

    /**
     * This invoked to get all Features for map, overlay or feature. It recursively gets all direct and indirect child
     * features for the specified parent It presumes that 'add' methods ensured that there are no cycles.
     * @param parent
     */
    @Override
    public List<IFeature> getChildFeatures(IContainer parent) {

        List<IFeature> features = new ArrayList<>();
        try {
            lock.lock();
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(parent.getGeoId());
            if (null != parentWrapper) {
                java.util.Set<IContainer> children = new java.util.HashSet<>();
                getChildren(children, parentWrapper);
                for (IContainer container : children) {
                    if (container instanceof IFeature) {
                        features.add((IFeature) container);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return features;
    }

    /**
     * This invoked to get all Overlays for map or overlay. It recursively gets all direct and indirect child
     * overlays for the specified parent It presumes that 'add' methods ensured that there are no cycles.
     * @param parent
     */
    @Override
    public List<IOverlay> getChildOverlays(IContainer parent) {

        List<IOverlay> overlays = new ArrayList<>();
        try {
            lock.lock();
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(parent.getGeoId());
            if (null != parentWrapper) {
                java.util.Set<IContainer> children = new java.util.HashSet<>();
                getChildren(children, parentWrapper);
                for (IContainer container : children) {
                    if (container instanceof IOverlay) {
                        overlays.add((IOverlay) container);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return overlays;
    }

    /**
     * Returns immediate children of the specified container
     * @param container
     * @return
     */
    @Override
    public List<IGeoBase> getImmediateChildren(IContainer container) {

        List<IGeoBase> children = new ArrayList<>();
        try {
            lock.lock();
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(container.getGeoId());
            if((null != parentWrapper) && (parentWrapper.getChildIdList().size() != 0)) {
                Collection<StorageObjectWrapper> childList = parentWrapper.getChildrenList().values();
                if(null != childList) {
                    for(StorageObjectWrapper child: childList) {
                        children.add(child.getObject());
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return children;
    }

    /**
     *
     * @param childWrapper
     * @param parents
     */
    private void getParents(StorageObjectWrapper childWrapper, List<IContainer> parents) {
        if(null == childWrapper) return;

        Map<UUID, IParentRelationship> parentList = childWrapper.getParentList();
        if(null != parentList) {
            for(java.util.UUID uuid : parentList.keySet()) {
                StorageObjectWrapper parentWrapper = this.oObjectHash.get(uuid);
                if(null != uuid) {
                    parents.add(parentWrapper.getObject());
                }
            }
        }
    }

    @Override
    public List<IContainer> getParents(IContainer childContainer) {
        List<IContainer> parents = new ArrayList<>();
        try {
            lock.lock();
            StorageObjectWrapper childContainerWrapper;
            if ((null != childContainer) && (null != (childContainerWrapper = this.oObjectHash.get(childContainer.getGeoId())))) {
                getParents(childContainerWrapper, parents);
            }
        } finally {
            lock.unlock();
        }
        return parents;
    }
    /**
     *
     * @param childFeature
     * @return
     */
    @Override
    public List<IOverlay> getParentOverlays(IFeature childFeature) {
        StorageObjectWrapper childFeatureWrapper;
        List<IOverlay> parentOverlays = new ArrayList<>();
        try {
            lock.lock();
            if ((null != childFeature) && (null != (childFeatureWrapper = this.oObjectHash.get(childFeature.getGeoId())))) {
                List<IContainer> parents = new ArrayList<>();
                getParents(childFeatureWrapper, parents);
                for (IContainer container : parents) {
                    if (container instanceof IOverlay) {
                        parentOverlays.add((IOverlay) container);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return parentOverlays;
    }

    /**
     *
     * @param childFeature
     * @return
     */
    @Override
    public List<IFeature> getParentFeatures(IFeature childFeature) {
        StorageObjectWrapper childFeatureWrapper;
        List<IFeature> parentFeatures = new ArrayList<>();
        try {
            lock.lock();
            if ((null != childFeature) && (null != (childFeatureWrapper = this.oObjectHash.get(childFeature.getGeoId())))) {
                List<IContainer> parents = new ArrayList<>();
                getParents(childFeatureWrapper, parents);
                for (IContainer container : parents) {
                    if (container instanceof IFeature) {
                        parentFeatures.add((IFeature) container);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
        return parentFeatures;
    }

    /**
     * This method adds overlays to the root of the map.
     * @param map
     * @param overlays
     * @param visible
     * @throws EMP_Exception
     */
    @Override
    public void addOverlays(IMap map, List<IOverlay> overlays, boolean visible, Object userContext) throws EMP_Exception {
        add(map, overlays, visible, userContext);
    }

    @Override
    public void addOverlays(IOverlay parentOverlay, List<IOverlay> overlays, boolean visible, Object userContext) throws EMP_Exception {
        if (null == this.oObjectHash.get(parentOverlay.getGeoId())) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARENT, "Parent is not yet added");
        }
        add(parentOverlay, overlays, visible, userContext);
    }

    @Override
    public void addFeatures(IOverlay parentOverlay, List<IFeature> featureList, boolean visible, Object userContext) throws EMP_Exception {
        if (null == this.oObjectHash.get(parentOverlay.getGeoId())) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARENT, "Parent is not yet added");
        }
        add(parentOverlay, featureList, visible, userContext);
    }

    @Override
    public void addFeatures(IFeature parentFeature, List<IFeature> featureList, boolean visible, Object userContext) throws EMP_Exception {
        if (null == this.oObjectHash.get(parentFeature.getGeoId())) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARENT, "Parent is not yet added");
        }
        add(parentFeature, featureList, visible, userContext);
    }

    /**
     * This is invoked as a result of client calling removeFeature(s) on IOverlay or iFeature. creates the pre and post map
     * list for the childWrapper.
     * @param transactionList
     * @param parentWrapper - parent of the object to be removed
     * @param childWrapper - Object to be removed
     * @throws EMP_Exception
     */
    private void removeChild(TransactionList transactionList, StorageObjectWrapper parentWrapper, StorageObjectWrapper childWrapper)
            throws EMP_Exception {
        IUUIDSet preMapList;
        IUUIDSet postMapList;

        //Log.d(TAG, "removeChildren childId " + childWrapper.getGeoId() + " parentId " + parentWrapper.getGeoId() + " hasChild " +
        //        parentWrapper.hasChild(childWrapper.getGeoId()));

        // Remove from parent if it is a child.
        if (parentWrapper.hasChild(childWrapper.getGeoId())) {
            // Get The map list before the operation
            preMapList = childWrapper.getMapList();
            // We need to add it as a child of the parent.
            parentWrapper.removeChild(childWrapper.getGeoId());
            // Get the map list after the operation.
            postMapList = childWrapper.getMapList();

            this.processRequest(transactionList, preMapList, postMapList, parentWrapper, childWrapper);
        }
    }

    /**
     * This is from IFeature API
     * @param parentFeature
     * @param features
     * @throws EMP_Exception
     */
    @Override
    public void removeFeatures(IFeature parentFeature, List<IFeature> features, Object userContext)
            throws EMP_Exception {
        if((null == parentFeature) || (null == features)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, " parentFeature or features list is NULL");
        }

        //Log.d(TAG, "removeFeatures from Feature " + parentFeature.getName());
        try {
            lock.lock();
            //Log.v(TAG, "b4remove removeFeatures(IFeature parentFeature oObjectHash.size() " + oObjectHash.size());
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(parentFeature.getGeoId());

            if (null == parentWrapper) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARENT, "Feature Name " + parentFeature.getName() +
                        " Feature Id " + parentFeature.getGeoId());
            }

            for (IFeature aFeature : features) {
                //Log.d(TAG, "removeFeatures from parentFeature " + aFeature.getName());
                if (!parentWrapper.getChildIdList().contains(aFeature.getGeoId())) {
                    Log.e(TAG, "removeFeatures from parentFeature " + aFeature.getGeoId() + " is not a child of " + parentFeature.getGeoId());
                    throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_CHILD, "removeFeatures feature " + aFeature.getGeoId() +
                            " is not a child of " + parentFeature.getGeoId());
                }
            }

            TransactionList transactionList = new TransactionList();
            for (IFeature aFeature : features) {
                StorageObjectWrapper childWrapper = this.oObjectHash.get(aFeature.getGeoId());
                if (null == childWrapper) {
                    Log.e(TAG, "Feature " + aFeature.getName() + " " + aFeature.getGeoId() + " Not in ObjectHash, skip");
                    continue;
                }
                removeChild(transactionList, parentWrapper, childWrapper);
            }
            executeTransaction(transactionList, userContext);
        } finally {
            //Log.v(TAG, "after removeFeatures(IFeature parentFeature oObjectHash.size() " + oObjectHash.size());
            lock.unlock();
            eventManager.generateContainerEvent(ContainerEventEnum.OBJECT_REMOVED, parentFeature, features, userContext);
        }
    }

    /**
     * This is from IOverlay API. Only those features that are direct children of parentOverlay are removed. This is enforced
     * by the remove child method. Note that child features (recursively) of direct feature children are also removed.
     * @param parentOverlay
     * @param features
     * @throws EMP_Exception
     */
    @Override
    public void removeFeatures(IOverlay parentOverlay, List<IFeature> features, Object userContext)
            throws EMP_Exception {
        if((null == parentOverlay) || (null == features)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, " parentOverlay or features list is NULL");
        }

        //Log.d(TAG, "removeFeatures from Overlay " + parentOverlay.getName());
        try {
            lock.lock();
            //Log.v(TAG, "b4remove removeFeatures(IOverlay parentOverlay oObjectHash.size() " + oObjectHash.size());
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(parentOverlay.getGeoId());

            if (null == parentWrapper) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARENT, "Overlay Name " + parentOverlay.getName() +
                        " Overlay Id " + parentOverlay.getGeoId());
            }

            for (IFeature aFeature : features) {
                //Log.d(TAG, "removeFeatures from parentOverlay " + aFeature.getName());
                if (!parentWrapper.getChildIdList().contains(aFeature.getGeoId())) {
                    Log.e(TAG, "removeFeatures from parentOverlay " + aFeature.getGeoId() + " is not a child of " + parentOverlay.getGeoId());
                    throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_CHILD, "removeFeatures feature " + aFeature.getGeoId() +
                            " is not a child of " + parentOverlay.getGeoId());
                }
            }

            TransactionList transactionList = new TransactionList();
            for (IFeature aFeature : features) {
                StorageObjectWrapper childWrapper = this.oObjectHash.get(aFeature.getGeoId());
                if (null == childWrapper) {
                    Log.e(TAG, "Feature " + aFeature.getName() + " " + aFeature.getGeoId() + " Not in ObjectHash, skip");
                    continue;
                }
                removeChild(transactionList, parentWrapper, childWrapper);
            }
            executeTransaction(transactionList, userContext);
        } finally {
            //Log.v(TAG, "after removeFeatures(IOverlay parentOverlay oObjectHash.size() " + oObjectHash.size());
            lock.unlock();
            eventManager.generateContainerEvent(ContainerEventEnum.OBJECT_REMOVED, parentOverlay, features, userContext);
        }
    }

    @Override
    public void removeOverlays(IMap clientMap, List<IOverlay> overlays, Object userContext) throws EMP_Exception {
        // validate the overlay list to ensure that each of the overlays is a direct child of clientMap
        //Log.d(TAG, "removeOverlays from IMap");
        if((null == clientMap) || (null == overlays)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, " parentOverlay or features list is NULL");
        }
        try {
            lock.lock();
            //Log.v(TAG, "b4remove removeOverlays(IMap clientMap oObjectHash.size() " + oObjectHash.size());
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(clientMap.getGeoId());
            if (null == parentWrapper) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARENT, "Map Name " + clientMap.getName() +
                        " Parent Id " + clientMap.getGeoId());
            }

            for (IOverlay anOverlay : overlays) {
                //Log.d(TAG, "removeOverlays from IMap " + anOverlay.getName());
                if (!parentWrapper.getChildIdList().contains(anOverlay.getGeoId())) {
                    Log.e(TAG, "removeOverlays from clientMap overlay " + anOverlay.getGeoId() + " is not a child of " + clientMap.getGeoId());
                    throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_CHILD, "removeOverlays overlay " + anOverlay.getGeoId() +
                            " is not a child of " + clientMap.getGeoId());
                }
            }

            TransactionList transactionList = new TransactionList();
            for (IOverlay anOverlay : overlays) {
                StorageObjectWrapper childWrapper = this.oObjectHash.get(anOverlay.getGeoId());
                removeChild(transactionList, parentWrapper, childWrapper);
            }
            executeTransaction(transactionList, userContext);
        } finally {
            //Log.v(TAG, "after removeOverlays(IMap clientMap oObjectHash.size() " + oObjectHash.size());
            lock.unlock();
            eventManager.generateContainerEvent(ContainerEventEnum.OBJECT_REMOVED, clientMap, overlays, userContext);
        }
    }

    /**
     * This is invoked from IOverlay API. Take care to remove direct children of the overlays (included children of
     * features) othereise low level methods will fail. Each overlay on the list must be a direct child of the parentOverlay.
     * All descendants of overlay on the list will be removed from the map to which parentOverlay belongs.
     * @param parentOverlay
     * @param overlays
     * @throws EMP_Exception
     */
    @Override
    public void removeOverlays(IOverlay parentOverlay, List<IOverlay> overlays, Object userContext) throws EMP_Exception {
        if((null == parentOverlay) || (null == overlays)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, " parentOverlay or overlays list is NULL");
        }
        try {
            lock.lock();
            //Log.v(TAG, "b4remove removeOverlays(IOverlay parentOverlay oObjectHash.size() " + oObjectHash.size());
            // Validate the overlay list to make sure that each overlay is a child of the specified parentOverlay
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(parentOverlay.getGeoId());
            if (null == parentWrapper) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARENT, "Overlay Name " + parentOverlay.getName() +
                        " Overlay Id " + parentOverlay.getGeoId());
            }

            for (IOverlay anOverlay : overlays) {
                if (!parentWrapper.getChildIdList().contains(anOverlay.getGeoId())) {
                    Log.e(TAG, "removeOverlays overlay " + anOverlay.getGeoId() + " is not a child of " + parentOverlay.getGeoId());
                    throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_CHILD, "removeOverlays overlay " + anOverlay.getGeoId() +
                            " is not a child of " + parentOverlay.getGeoId());
                }
            }

            TransactionList transactionList = new TransactionList();
            for (IOverlay anOverlay : overlays) {
                StorageObjectWrapper childWrapper = this.oObjectHash.get(anOverlay.getGeoId());
                removeChild(transactionList, parentWrapper, childWrapper);
            }
            executeTransaction(transactionList, userContext);
        } finally {
            //Log.v(TAG, "after removeOverlays(IOverlay parentOverlay oObjectHash.size() " + oObjectHash.size());
            lock.unlock();
            eventManager.generateContainerEvent(ContainerEventEnum.OBJECT_REMOVED, parentOverlay, overlays, userContext);
        }
    }

    /**
     * Invoked by Container.clearContainer()
     * @param parentContainer
     * @throws EMP_Exception
     */
    @Override
    public void removeChildren(IContainer parentContainer, Object userContext) throws EMP_Exception {
        StorageObjectWrapper parentContainerWrapper;
        List<IGeoBase> childrenToTriggerEvents = new ArrayList<>();
        try {
            lock.lock();
            //Log.v(TAG, "b4remove removeChildren(IContainer parentContainer oObjectHash.size() " + oObjectHash.size());
            if ((null == parentContainer) || (null == (parentContainerWrapper = this.oObjectHash.get(parentContainer.getGeoId())))) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "removeChildren invalid parentContainer ");
            }

            Map<UUID, StorageObjectWrapper> childrenList = parentContainerWrapper.getChildrenList();
            if (null != childrenList) {
                TransactionList transactionList = new TransactionList();
                java.util.Collection<StorageObjectWrapper> childList = new ArrayList<>();
                childList.addAll(childrenList.values()); // To avoid ConcurrentModification issue
                for (StorageObjectWrapper childWrapper : childList) {
                    removeChild(transactionList, parentContainerWrapper, childWrapper);

                    if (childWrapper.getObject() instanceof IGeoBase) {
                        childrenToTriggerEvents.add((IGeoBase) childWrapper.getObject());
                    }
                }
                executeTransaction(transactionList, userContext);
            }
        } finally {
            //Log.v(TAG, "after removeChildren(IContainer parentContainer oObjectHash.size() " + oObjectHash.size());
            lock.unlock();
            eventManager.generateContainerEvent(ContainerEventEnum.OBJECT_REMOVED, parentContainer, childrenToTriggerEvents, userContext);
        }
    }

    @Override
    public void redrawAllFeatures(IMap clientMap, Object userContext) {
        IMapInstance mapInstance = getMapInstance(clientMap);
        if (null == mapInstance) {
            //Log.i(TAG, "redrawAllFeatures: mapInstance is null, skip");
            return;
        }

        try {
            lock.lock();
            List<IFeature> features = getChildFeatures(clientMap);
            if ((null == features) || (0 == features.size())) {
                //Log.i(TAG, "redrawAllfeatures: there are no features.");
                return;
            }

            FeatureVisibilityList fvList = new FeatureVisibilityList();
            for (IFeature feature : features) {
                if (VisibilityStateEnum.VISIBLE == getVisibilityOnMap(clientMap, feature)) {
                    fvList.add(new FeatureVisibility(feature, true));
                } else {
                    //Log.d(TAG, "feature is not visible " + feature.getClass().getSimpleName() + " " + feature.getGeoId());
                }
            }
            if (fvList.size() > 0) {
                mapInstance.addFeatures(fvList, userContext);
                mapInstance.selectFeatures(this.getSelected(clientMap));
            } else {
                //Log.i(TAG, "redrawAllFeatures: There are no visible faetures");
            }
        } finally {
            lock.unlock();
        }
    }
    @Override
    public void addMapService(IMap map, IMapService mapService) throws EMP_Exception {
        ClientMapToMapInstance mapMapping;

        try {
            lock.lock();
            if(mapService instanceof IKMLS) {
                if(KMLSProvider.create(this).addMapService(map, (IKMLS) mapService)) {
                    // We will need this when client does a swap engine or activity is restored.
                    // When activity is restored we still save the entire list anyway, that may be redundant
                    ClientMapRestoreData cmrd = oMapNameToRestoreDataMapping.get(map.getName());
                    if (null != cmrd) {
                        cmrd.addMapService(mapService);
                    }
                }
            } else if (this.oClientMapToMapInstanceMapping.containsKey(map)) {
                mapMapping = this.oClientMapToMapInstanceMapping.get(map);
                mapMapping.getMapInstance().addMapService(mapService);
                mapMapping.addMapService(mapService);

                // We will need this when client does a swap engine or activity is restored.
                // When activity is restored we still save the entire list anyway, that may be redundant
                ClientMapRestoreData cmrd = oMapNameToRestoreDataMapping.get(map.getName());
                if (null != cmrd) {
                    cmrd.addMapService(mapService);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeMapService(IMap map, IMapService mapService) throws EMP_Exception {
        ClientMapToMapInstance mapMapping;

        try {
            lock.lock();
            if (this.oClientMapToMapInstanceMapping.containsKey(map)) {
                mapMapping = this.oClientMapToMapInstanceMapping.get(map);
                if (mapMapping.removeMapService(mapService)) {
                    mapMapping.getMapInstance().removeMapService(mapService);
                }
                // We will need this when client does a swap engine or activity is restored.
                // When activity is restored we still save the entire list anyway, that may be redundant
                ClientMapRestoreData cmrd = oMapNameToRestoreDataMapping.get(map.getName());
                if (null != cmrd) {
                    cmrd.removeMapService(mapService);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public List<IMapService> getMapServices(IMap map) {
        List<IMapService> oList;

        ClientMapToMapInstance mapMapping;

        if (this.oClientMapToMapInstanceMapping.containsKey(map)) {
            mapMapping = this.oClientMapToMapInstanceMapping.get(map);
            oList = mapMapping.getMapServices();
        } else {
            oList = new ArrayList<>();
        }
        return oList;
    }

    @Override
    public void MapServiceUpdated(IMapService mapService) throws EMP_Exception {
        java.util.UUID wmsId = mapService.getGeoId();

        try {
            lock.lock();
            for (ClientMapToMapInstance mapMapping : this.oClientMapToMapInstanceMapping.values()) {
                if (mapMapping.hasWMS(wmsId)) {
                    mapMapping.addMapService(mapService);
                    mapMapping.getMapInstance().addMapService(mapService);

                    // We will need this when client does a swap engine or activity is restored.
                    // When activity is restored we still save the entire list anyway, that may be redundant
                    ClientMapRestoreData cmrd = oMapNameToRestoreDataMapping.get(mapMapping.getClientMap().getName());
                    if (null != cmrd) {
                        cmrd.addMapService(mapService);
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    private void dirtySinglePointOnMap(IMap map, Object userContext) {
        StorageObjectWrapper oWrapper;

        IMapInstance mapInstance = getMapInstance(map);
        if (null == mapInstance) {
            return;
        }
        FeatureVisibilityList fvList = new FeatureVisibilityList();

        // We need to force an update on all single point icon features
        // on the specified map.
        for (java.util.UUID uuId: this.oObjectHash.keySet()) {
            oWrapper = this.oObjectHash.get(uuId);
            if (oWrapper.getObject() instanceof MilStdSymbol) {
                if (oWrapper.isOnMap(map.getGeoId())) {
                    MilStdSymbol oSymbol = (MilStdSymbol) oWrapper.getObject();
                    if (!oSymbol.isTacticalGraphic()) {
                        if (VisibilityStateEnum.VISIBLE == getVisibilityOnMap(map, oSymbol)) {
                            fvList.add(new FeatureVisibility(oSymbol, true));
                        } else {
                            //Log.d(TAG, "feature is not visible " + oSymbol.getClass().getSimpleName() + " " + oSymbol.getGeoId());
                        }
                    }
                }
            } else if (oWrapper.getObject() instanceof Point) {
                if (oWrapper.isOnMap(map.getGeoId())) {
                    Point oPoint = (Point) oWrapper.getObject();
                    if (VisibilityStateEnum.VISIBLE == getVisibilityOnMap(map, oPoint)) {
                        fvList.add(new FeatureVisibility(oPoint, true));
                    }
                }
            }
        }
        if (fvList.size() > 0) {
            mapInstance.addFeatures(fvList, userContext);
        }
    }

    private void dirtyMilStdOnMap(IMap map, Object userContext) {
        StorageObjectWrapper oWrapper;

        IMapInstance mapInstance = getMapInstance(map);
        if (null == mapInstance) {
            return;
        }
        FeatureVisibilityList fvList = new FeatureVisibilityList();

        // We need to force an update on all MilStd features
        // on the specified map.
        for (java.util.UUID uuId: this.oObjectHash.keySet()) {
            oWrapper = this.oObjectHash.get(uuId);
            if (oWrapper.getObject() instanceof MilStdSymbol) {
                if (oWrapper.isOnMap(map.getGeoId())) {
                    MilStdSymbol oSymbol = (MilStdSymbol) oWrapper.getObject();
                    if (VisibilityStateEnum.VISIBLE == getVisibilityOnMap(map, oSymbol)) {
                        fvList.add(new FeatureVisibility(oSymbol, true));
                    }
                }
            }
        }
        if (fvList.size() > 0) {
            mapInstance.addFeatures(fvList, userContext);
        }
    }

    @Override
    public void setIconSize(IMap map, IconSizeEnum eSize, Object userContext)
            throws EMP_Exception {
        try {
            lock.lock();
            IClientMapToMapInstance oMapping = this.getMapMapping(map);

            if (oMapping == null) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_MAP, "Invalid map");
            }

            oMapping.setIconSize(eSize);
            this.dirtySinglePointOnMap(map, userContext);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public IconSizeEnum getIconSize(IMap map) {
        IClientMapToMapInstance oMapping = this.getMapMapping(map);

        if (oMapping == null) {
            return IconSizeEnum.SMALL;
        }
        return oMapping.getIconSize();
    }

    @Override
    public void setMilStdLabels(IMap map, MilStdLabelSettingEnum labelSetting, Object userContext)
            throws EMP_Exception {
        try {
            lock.lock();
            IClientMapToMapInstance oMapping = this.getMapMapping(map);

            if (oMapping == null) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_MAP, "Invalid map");
            }

            oMapping.setMilStdLabels(labelSetting);
            this.dirtyMilStdOnMap(map, userContext);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public MilStdLabelSettingEnum getMilStdLabels(IMap map) {
        IClientMapToMapInstance oMapping = this.getMapMapping(map);

        if (oMapping == null) {
            return null;
        }
        return oMapping.getMilStdLabels();
    }

    @Override
    public IconSizeEnum getIconSize(IMapInstance mapInstance) {
        IClientMapToMapInstance oMapping = this.getMapMapping(mapInstance);

        if (oMapping == null) {
            return IconSizeEnum.SMALL;
        }
        return oMapping.getIconSize();
    }

    @Override
    public int getIconPixelSize(IMapInstance mapInstance) {
        IClientMapToMapInstance oMapping = this.getMapMapping(mapInstance);

        if (oMapping == null) {
            return 0;
        }
        return oMapping.getIconPixelSize();
    }

    @Override
    public int getIconPixelSize(IMap map) {
        IClientMapToMapInstance oMapping = this.getMapMapping(map);

        if (oMapping == null) {
            return 0;
        }
        return oMapping.getIconPixelSize();
    }

    @Override
    public MilStdLabelSettingEnum getMilStdLabels(IMapInstance mapInstance) {
        IClientMapToMapInstance oMapping = this.getMapMapping(mapInstance);

        if (oMapping == null) {
            return null;
        }
        return oMapping.getMilStdLabels();
    }

    @Override
    public void addDrawFeature(IMap oMap, IFeature oFeature) throws EMP_Exception {
        //Log.d(TAG, "addDrawFeature to map.");

        try {
            lock.lock();
            java.util.UUID childId;
            java.util.UUID parentId = oMap.getGeoId();
            StorageObjectWrapper wrapper;
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(parentId);
            if (null == parentWrapper) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARENT, "Parent is not yet added");
            }

            TransactionList transactionList = new TransactionList();

            // Get the unique Id.
            childId = oFeature.getGeoId();
            // See if it already exists.
            if (this.oObjectHash.containsKey(childId)) {
                // It does exists so update the object just in case.
                wrapper = this.oObjectHash.get(childId);
                wrapper.setObject(oFeature);
            } else {
                wrapper = new StorageObjectWrapper<>(oFeature);
                this.oObjectHash.put(childId, wrapper);
            }
            this.addChildren(transactionList, parentWrapper, wrapper, true);

            this.executeTransaction(transactionList, null);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void removeDrawFeature(IMap oMap, IFeature oFeature) throws EMP_Exception {
        //Log.d(TAG, "removeDrawFeatures from map.");

        try {
            lock.lock();
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(oMap.getGeoId());

            if (!parentWrapper.getChildIdList().contains(oFeature.getGeoId())) {
                Log.e(TAG, "removeDrawFeature from parentOverlay " + oFeature.getGeoId() + " is not a child of map.");
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_CHILD, "removeDrawFeature feature " + oFeature.getGeoId() +
                        " is not a child of map.");
            }

            TransactionList transactionList = new TransactionList();

            StorageObjectWrapper childWrapper = this.oObjectHash.get(oFeature.getGeoId());
            if (null == childWrapper) {
                Log.e(TAG, "Feature " + oFeature.getName() + " " + oFeature.getGeoId() + " Not in ObjectHash.");
                return;
            }
            removeChild(transactionList, parentWrapper, childWrapper);

            executeTransaction(transactionList, null);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setFarDistanceThreshold(IMap oMap, double dValue) {
        try {
            lock.lock();
            StorageObjectWrapper oMapWrapper = this.oObjectHash.get(oMap.getGeoId());
            IClientMapToMapInstance oMapping = this.getMapMapping((IMap) oMapWrapper.getObject());

            if (oMapping == null) {
                return;
            }
            oMapping.setFarDistanceThreshold(dValue);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public double getFarDistanceThreshold(IMap oMap) {
        try {
            lock.lock();
            // TODO This is a hack to test the far and mid distance threshold.
            StorageObjectWrapper oMapWrapper = this.oObjectHash.get(oMap.getGeoId());
            IClientMapToMapInstance oMapping = this.getMapMapping((IMap) oMapWrapper.getObject());

            if (oMapping == null) {
                return Double.NaN;
            }
            return oMapping.getFarDistanceThreshold();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public double getFarDistanceThreshold(IMapInstance mapInstance) {
        try {
            lock.lock();
            IClientMapToMapInstance oMapping = this.getMapMapping(mapInstance);

            if (oMapping == null) {
                return Double.NaN;
            }
            return oMapping.getFarDistanceThreshold();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void setMidDistanceThreshold(IMap oMap, double dValue) {
        try {
            lock.lock();
            StorageObjectWrapper oMapWrapper = this.oObjectHash.get(oMap.getGeoId());
            IClientMapToMapInstance oMapping = this.getMapMapping((IMap) oMapWrapper.getObject());

            if (oMapping == null) {
                return;
            }

            // Update the AdapativeBitmapCache with new information from aplication.
            boolean setOnMapInstance =  BitmapCacheFactory.instance().getBitMapCache().setMidDistanceThreshold(oMap, dValue);
            oMapping.setMidDistanceThreshold(dValue, setOnMapInstance);

        } finally {
            lock.unlock();
        }
    }

    /**
     * This method is invoked by AdaptiveBitmapCache to update Mid Distance threshold. Note that we don't updae the value set by
     * application in the MapStatus object. If we want to let application retrieve the actual value thebn we will need to provide
     * a method in IMap.
     *
     * @param oMap
     * @param dValue
     */
    @Override
    public void setMidDistanceThresholdActual(IMap oMap, double dValue) {
        try {
            lock.lock();
            StorageObjectWrapper oMapWrapper = this.oObjectHash.get(oMap.getGeoId());
            IClientMapToMapInstance oMapping = this.getMapMapping((IMap) oMapWrapper.getObject());

            if (oMapping == null) {
                Log.e(TAG, "Cannot adjust MidDistanceThreshold for this map " + oMap.getGeoId());
                return;
            } else {
                oMapping.getMapInstance().setMidDistanceThreshold(dValue);
            }

        } finally {
            lock.unlock();
        }
    }

    @Override
    public double getMidDistanceThreshold(IMap oMap) {
        try {
            lock.lock();
            if(null != oMap) {
                StorageObjectWrapper oMapWrapper = this.oObjectHash.get(oMap.getGeoId());
                IClientMapToMapInstance oMapping = this.getMapMapping((IMap) oMapWrapper.getObject());

                if (oMapping == null) {
                    return Double.NaN;
                }
                return oMapping.getMidDistanceThreshold();
            } else {
                return Double.NaN;
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public double getMidDistanceThreshold(IMapInstance mapInstance) {
        try {
            lock.lock();
            if(null != mapInstance) {
                IClientMapToMapInstance oMapping = this.getMapMapping(mapInstance);

                if (oMapping == null) {
                    return Double.NaN;
                }
                return oMapping.getMidDistanceThreshold();
            } else {
                return Double.NaN;
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Invoked by AdaptiveBitmapCache to retrieve all MidDistance thresholds on initialization.
     * @return
     */
    public Map<IMap, Double> getMidDistanceThreshold() {
        Map<IMap, Double> midDistanceThreshols = new HashMap<>();
        try {
            lock.lock();
            for(Map.Entry<IMap, ClientMapToMapInstance> entry : oClientMapToMapInstanceMapping.entrySet()) {
                midDistanceThreshols.put(entry.getKey(), entry.getValue().getMidDistanceThreshold());
            }
        } finally {
            lock.unlock();
        }
        return midDistanceThreshols;
    }

    @Override
    public void setBounds(IMap map, IGeoBounds bounds) {
        IClientMapToMapInstance oMapping = this.getMapMapping(map);
        oMapping.setBounds(bounds);
    }

    @Override
    public IGeoBounds getBounds (IMap map) {
        IClientMapToMapInstance oMapping = this.getMapMapping(map);
        return oMapping.getBounds();
    }

    /**
     * This method returns the default fill style for MilStd 2525 symbols.
     * @param symbol
     * @return
     */
    public IGeoFillStyle getDefaultFillStyle(MilStdSymbol symbol) {
        IGeoFillStyle fillStyle;
        boolean isTG = symbol.isTacticalGraphic();
        MilStdSymbol.Affiliation affiliation = symbol.getAffiliation();

        // We only cache style for MilStd Icons.
        if (!isTG && this.defaultIconFillStyleCache.containsKey(affiliation)) {
            fillStyle = this.defaultIconFillStyleCache.get(affiliation);
        } else {
            IGeoColor oFillColor;
            armyc2.c2sd.renderer.utilities.Color oColor = SymbolUtilities.getFillColorOfAffiliation(symbol.getSymbolCode());

            fillStyle = new GeoFillStyle();
            oFillColor = fillStyle.getFillColor();
            oFillColor.setAlpha(oColor.getAlpha() / 255.0);
            oFillColor.setRed(oColor.getRed());
            oFillColor.setGreen(oColor.getGreen());
            oFillColor.setBlue(oColor.getBlue());
            if (!isTG) {
                this.defaultIconFillStyleCache.put(affiliation, fillStyle);
            }
        }

        return fillStyle;
    }

    /**
     * This method returns the default stroke style for MilStd 2525 symbols.
     * @param symbol
     * @return
     */
    public IGeoStrokeStyle getDefaultStrokeStyle(MilStdSymbol symbol) {
        IGeoStrokeStyle strokeStyle;
        boolean isTG = symbol.isTacticalGraphic();
        MilStdSymbol.Affiliation affiliation = symbol.getAffiliation();

        // We only cache style for MilStd Icons.
        if (!isTG && this.defaultIconStrokeStyleCache.containsKey(affiliation)) {
            strokeStyle = this.defaultIconStrokeStyleCache.get(affiliation);
        } else {
            IGeoColor oLineColor;
            armyc2.c2sd.renderer.utilities.Color oColor = SymbolUtilities.getLineColorOfAffiliation(symbol.getSymbolCode());

            strokeStyle = new GeoStrokeStyle();
            oLineColor = strokeStyle.getStrokeColor();
            oLineColor.setAlpha(oColor.getAlpha() / 255.0);
            oLineColor.setRed(oColor.getRed());
            oLineColor.setGreen(oColor.getGreen());
            oLineColor.setBlue(oColor.getBlue());
            if (!isTG) {
                this.defaultIconStrokeStyleCache.put(affiliation, strokeStyle);
            }
        }

        return strokeStyle;
    }

    /**
     * This method marks the feature as selected on the map. If the feature is already mark selected
     * no action is taken. map engine is updated with the selection and FeatureEvent is fired.
     *
     * @param clientMap The map the selection it to be applied.
     * @param features The list of features to select.
     */
    @Override
    public void selectFeatures(IMap clientMap, List<IFeature> features, Object userContext) {
        IClientMapToMapInstance mapping;
        if ((features != null) && (null != clientMap) && (null != (mapping = this.getMapMapping(clientMap)))){
            List<IFeature> featureList = new ArrayList<>();
            try {
                lock.lock();
                StorageObjectWrapper wrapper;

                for (IFeature feature : features) {
                    if(null != feature) {
                        wrapper = this.oObjectHash.get(feature.getGeoId());
                        if ((wrapper != null) && wrapper.isOnMap(clientMap.getGeoId())) {
                            if (mapping.selectFeature((IFeature) wrapper.getObject())) {
                                featureList.add((IFeature) wrapper.getObject());
                            }
                        }
                    }
                }

                if ((!featureList.isEmpty() && (null != mapping.getMapInstance()))) {
                    mapping.getMapInstance().selectFeatures(featureList);
                }
            } finally {
                lock.unlock();
            }

            // Don't invoke application code within the lock
            if (!featureList.isEmpty()) {
                for (IFeature selected : featureList) {
                    eventManager.generateFeatureEvent(FeatureEventEnum.FEATURE_SELECTED, selected, true, userContext);
                }
            }
        }
    }

    /**
     * This method marks the feature as NOT selected on the map. If the feature is not marked selected
     * no action is taken. map engine is updated and FeatureEvent is fired.
     *
     * @param clientMap The map the deselection it to be applied.
     * @param features The feature to deselected.
     */
    @Override
    public void deselectFeatures(IMap clientMap, List<IFeature> features, Object userContext) {
        IClientMapToMapInstance mapping;
        if ((features != null) && (null != clientMap) && (null != (mapping = this.getMapMapping(clientMap)))) {
            List<IFeature> featureList = new ArrayList<>();

            try {
                lock.lock();
                StorageObjectWrapper wrapper;

                for (IFeature feature : features) {
                    if ((null != feature) && (mapping.isSelected(feature))) {
                        wrapper = this.oObjectHash.get(feature.getGeoId());
                        if ((wrapper != null) && wrapper.isOnMap(clientMap.getGeoId())) {
                            if (mapping.deselectFeature(wrapper.getGeoId())) {
                                featureList.add((IFeature) wrapper.getObject());
                            }
                        }
                    }
                }

                if (!featureList.isEmpty() && (null != mapping.getMapInstance())) {
                    mapping.getMapInstance().deselectFeatures(featureList);
                }
            } finally {
                lock.unlock();
            }

            // Don't invoke application code within a lock
            if (!featureList.isEmpty()) {
                for (IFeature selected : featureList) {
                    eventManager.generateFeatureEvent(FeatureEventEnum.FEATURE_DESELECTED, selected, false, userContext);
                }
            }
        }
    }

    /**
     * This method retrieves the list of feature that are marked selected on the map.
     *
     * @param clientMap The map to retrieve the list from.
     * @return A list of IFeatures. If there are no features selected the list is empty.
     */
    @Override
    public List<IFeature> getSelected(IMap clientMap) {
        IClientMapToMapInstance mapping;
        List<IFeature> list = new ArrayList<>();

        if ((null != clientMap) && (null != (mapping = this.getMapMapping(clientMap)))) {
            try {
                lock.lock();
                list.addAll(mapping.getSelected());
                return list;
            } finally {
                lock.unlock();
            }
        }
        return list;
    }

    /**
     * This method clears the map selected list.
     *
     * @param clientMap The map the selection list to clear.
     */
    @Override
    public void clearSelected(IMap clientMap, Object userContext) {
        IClientMapToMapInstance mapping;
        if ((null != clientMap) && (null != (mapping = this.getMapMapping(clientMap)))) {
            List<IFeature> deselectList = new ArrayList<>();

            try {
                lock.lock();
                StorageObjectWrapper wrapper;
                // Get the list of selected feature on the map.
                List<IFeature> featureList = mapping.getSelected();

                if (featureList != null) {
                    for (IFeature feature : featureList) {
                        if(null != feature) {
                            wrapper = this.oObjectHash.get(feature.getGeoId());
                            if ((wrapper != null) && wrapper.isOnMap(clientMap.getGeoId())) {
                                deselectList.add((IFeature) wrapper.getObject());
                            }
                        }
                    }

                    if (!deselectList.isEmpty() && (null != mapping.getMapInstance())) {
                        mapping.getMapInstance().deselectFeatures(deselectList);
                    }
                    mapping.clearSelected();
                }
            } finally {
                lock.unlock();
            }

            // Don't invoke application code within a lock
            if (!deselectList.isEmpty()) {
                for (IFeature selected : deselectList) {
                    eventManager.generateFeatureEvent(FeatureEventEnum.FEATURE_DESELECTED, selected, false, userContext);
                }
            }
        }
    }

    @Override
    public boolean isSelected(IMap clientMap, IFeature feature) {
        IClientMapToMapInstance mapping;
        if ((null != feature) && (null != clientMap) && (null != (mapping = this.getMapMapping(clientMap)))) {
            try {
                lock.lock();
                StorageObjectWrapper wrapper;

                wrapper = this.oObjectHash.get(feature.getGeoId());
                if ((wrapper != null) && wrapper.isOnMap(clientMap.getGeoId())) {
                    return mapping.isSelected(feature);
                }
            } finally {
                lock.unlock();
            }
        }
        return false;
    }

    @Override
    public IGeoStrokeStyle getSelectedStrokeStyle(IMapInstance mapInstance) {
        IClientMapToMapInstance mapping = this.getMapMapping(mapInstance);
        return mapping.getSelectStrokeStyle();
    }

    @Override
    public IGeoStrokeStyle getSelectedStrokeStyle(IMap map) {
        IClientMapToMapInstance mapping = this.getMapMapping(map);
        return mapping.getSelectStrokeStyle();
    }

    @Override
    public IGeoFillStyle getBufferFillStyle(IMapInstance mapInstance) {
        IClientMapToMapInstance mapping = this.getMapMapping(mapInstance);
        return mapping.getBufferFillStyle();
    }

    @Override
    public IGeoFillStyle getBufferFillStyle(IMap map) {
        IClientMapToMapInstance mapping = this.getMapMapping(map);
        return mapping.getBufferFillStyle();
    }

    /**
     * This method returns the select label style for the map instance.
     * @param mapInstance
     * @return
     */
    @Override
    public IGeoLabelStyle getSelectedLabelStyle(IMapInstance mapInstance) {
        IClientMapToMapInstance mapping = this.getMapMapping(mapInstance);
        return mapping.getSelectLabelStyle();
    }

    public IGeoLabelStyle getSelectedLabelStyle(IMap map) {
        IClientMapToMapInstance mapping = this.getMapMapping(map);
        return mapping.getSelectLabelStyle();
    }

    /**
     * This method returns the select icon scale for the map instance.
     * @param mapInstance
     * @return
     */
    @Override
    public double getSelectedIconScale(IMapInstance mapInstance) {
        IClientMapToMapInstance mapping = this.getMapMapping(mapInstance);
        return mapping.getSelectIconScale();
    }

    public double getSelectedIconScale(IMap map) {
        IClientMapToMapInstance mapping = this.getMapMapping(map);
        return mapping.getSelectIconScale();
    }

    /**
     * This method scan the position list and returns true if any position has an altitude other than 0. False otherwise.
     * @param feature
     * @return
     */
    private boolean hasAltitude(IFeature feature) {
        boolean bRet = false;
        List<IGeoPosition> posList = feature.getPositions();
        if ((null != posList) && !posList.isEmpty()) {
            for (int index = 0; index < posList.size(); index++) {
                if (Math.abs(posList.get(index).getAltitude()) < 1) {
                    // If one position has an altitude other than ~0, we return true.
                    bRet = true;
                    break;
                }
            }
        }

        return bRet;
    }

    /**
     * This method set the default altitude mode if the feature's altitude mode is not set.
     * @param feature
     */
    @Override
    public void setDefaultAltitudeMode(IFeature feature) {
        if (null == feature) {
            return;
        }

        if (null != feature.getAltitudeMode()) {
            // The feature has an altitude mode.
            return;
        }

        switch (feature.getFeatureType()) {
            case GEO_CIRCLE:
            case GEO_ELLIPSE:
            case GEO_PATH:
            case GEO_POINT:
            case GEO_POLYGON:
            case GEO_RECTANGLE:
            case GEO_SQUARE: {
                // The default is clamp to ground unless a position has an altitude.
                feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                if (this.hasAltitude(feature)) {
                    // If one position has an altitude other than 0, we set it to relative.
                    feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                }
                break;
            }
            case GEO_MIL_SYMBOL:
                MilStdSymbol symbol = (MilStdSymbol) feature;

                if (symbol.isTacticalGraphic()) {
                    feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                } else if (symbol.isAirTrack()) {
                    feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                } else if (symbol.isSpaceTrack()) {
                    feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.ABSOLUTE);
                } else if (this.hasAltitude(feature)) {
                    feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                } else {
                    feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                }
                break;
            case GEO_ACM:
                feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
                break;
            case GEO_TEXT:
                feature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
                break;
        }
    }

    @Override
    public FontSizeModifierEnum getFontSizeModifier(IMap map) {
        IClientMapToMapInstance mapping = this.getMapMapping(map);
        return mapping.getFontSizeModifier();

    }

    @Override
    public void setFontSizeModifier(IMap map, FontSizeModifierEnum value) throws EMP_Exception {
        if (value == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Font size modifier can not be null.");
        }

        IClientMapToMapInstance mapping = this.getMapMapping(map);
        mapping.setFontSizeModifier(value);
    }

    private void add(IContainer parent, List<? extends IContainer> children, boolean visible, Object userContext) throws EMP_Exception {
        try {
            lock.lock();
            java.util.UUID childId;
            java.util.UUID parentId = parent.getGeoId();
            StorageObjectWrapper wrapper;
            StorageObjectWrapper parentWrapper = this.oObjectHash.get(parentId);
            if (null == parentWrapper) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARENT, "Parent is not yet added");
            }

            TransactionList transactionList = new TransactionList();

            // Loop through the overlays.
            for (IContainer child : children) {
                // Ensure the feature has an altitude mode.
                if (child instanceof IFeature) {
                    this.setDefaultAltitudeMode((IFeature) child);
                }
                // Get the unique Id.
                childId = child.getGeoId();
                // See if it already exists.
                if (this.oObjectHash.containsKey(childId)) {
                    // It does exists so update the object just in case.
                    wrapper = this.oObjectHash.get(childId);
                    wrapper.setObject(child);
                } else {
                    wrapper = new StorageObjectWrapper<>(child);
                    this.oObjectHash.put(childId, wrapper);
                }
                this.addChildren(transactionList, parentWrapper, wrapper, visible);
            }

            this.executeTransaction(transactionList, userContext);
        } finally {
            lock.unlock();
            eventManager.generateContainerEvent(ContainerEventEnum.OBJECT_ADDED, parent, children, userContext);
        }
    }
}
