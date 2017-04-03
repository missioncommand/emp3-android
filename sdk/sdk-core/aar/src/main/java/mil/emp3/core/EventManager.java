package mil.emp3.core;

import android.graphics.Point;
import android.util.Log;

import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.enums.CameraEventEnum;
import mil.emp3.api.enums.ContainerEventEnum;
import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.enums.FeatureEventEnum;
import mil.emp3.api.enums.LookAtEventEnum;
import mil.emp3.api.enums.MapFeatureEventEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MapViewEventEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.events.Event;
import mil.emp3.api.events.FeatureDrawEvent;
import mil.emp3.api.events.FeatureEditEvent;
import mil.emp3.api.events.MapFreehandEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IContainerSet;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.listeners.IEventListener;
import mil.emp3.core.events.ContainerEvent;
import mil.emp3.core.events.EventListenerHandle;
import mil.emp3.core.events.FeatureEvent;
import mil.emp3.core.events.FeatureUserInteractionEvent;
import mil.emp3.core.events.MapFeatureAddedEvent;
import mil.emp3.core.events.MapFeatureRemovedEvent;
import mil.emp3.core.events.MapStateChangeEvent;
import mil.emp3.core.events.MapUserInteractionEvent;
import mil.emp3.core.events.MapViewChangeEvent;
import mil.emp3.core.events.VisibilityEvent;

/**
 * This class implements the EMP V3 Event Manager.
 */
public class EventManager implements IEventManager {
    private final static String TAG = EventManager.class.getSimpleName();

    private IStorageManager storageManager;

    private class ListenerHandleList extends ArrayList<EventListenerHandle> {}
    private class ListenerRegistrationHashMap extends HashMap<UUID, ListenerHandleList> {}
    private class EventRegistrationHashMap extends HashMap<EventListenerTypeEnum, ListenerRegistrationHashMap> {}

    // When new members are added review the 'clear' method for update.
    // This hash contains the event listener that were registers on an IMap.
    private final EventRegistrationHashMap oRegisteredOnMap = new EventRegistrationHashMap();

    // This hash contains the event listener that were registers on an IOverlay.
    private final EventRegistrationHashMap oRegisteredOnOverlay = new EventRegistrationHashMap();

    // This hash contains the event listener that were registers on an IFeature.
    private final EventRegistrationHashMap oRegisteredOnFeature = new EventRegistrationHashMap();

    // This hash contains the event listener that were registers on an ILookAt.
    private final EventRegistrationHashMap oRegisteredOnLookAt = new EventRegistrationHashMap();

    // This hash contains the event listener that were registers on an ICamera.
    private final EventRegistrationHashMap oRegisteredOnCamera = new EventRegistrationHashMap();

    // This hash contains the event listener that were registers on an IContainer.
    private final EventRegistrationHashMap oRegisteredOnContainer = new EventRegistrationHashMap();

    @Override
    public void setStorageManager(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * Application activity has restarted and application will restore state.
     */
    @Override
    public void clear() {
        oRegisteredOnMap.clear();
        oRegisteredOnOverlay.clear();
        oRegisteredOnFeature.clear();
        oRegisteredOnCamera.clear();
        oRegisteredOnContainer.clear();
    }

    private EventListenerHandle addEventHandler(
            EventRegistrationHashMap oRegistrationList,
            EventListenerTypeEnum eEventType,
            IGeoBase oRegistrationObject,
            IEventListener oListener) {
        ListenerRegistrationHashMap oEventTypeHandlers;
        ListenerHandleList oHandleList;
        EventListenerHandle oHandle = new EventListenerHandle(eEventType, oRegistrationObject, oListener);

        if (!oRegistrationList.containsKey(eEventType)) {
            oEventTypeHandlers = new ListenerRegistrationHashMap();
            oRegistrationList.put(eEventType, oEventTypeHandlers);
        } else {
            oEventTypeHandlers = oRegistrationList.get(eEventType);
        }

        if (!oEventTypeHandlers.containsKey(oRegistrationObject.getGeoId())) {
            oHandleList = new ListenerHandleList();
            oEventTypeHandlers.put(oRegistrationObject.getGeoId(), oHandleList);
        } else {
            oHandleList = oEventTypeHandlers.get(oRegistrationObject.getGeoId());
        }

        oHandleList.add(oHandle);
        return oHandle;
    }

    @Override
    public EventListenerHandle addEventHandler(EventListenerTypeEnum eEventType, IGeoBase oRegistrationObject, IEventListener oListener) throws EMP_Exception {
        EventListenerHandle oHandle = null;

        if (oListener == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "The listener can not be null.");
        }
        if (oRegistrationObject instanceof IMap) {
            oHandle = this.addEventHandler(this.oRegisteredOnMap, eEventType, oRegistrationObject, oListener);
        } else if (oRegistrationObject instanceof IOverlay) {
            oHandle = this.addEventHandler(this.oRegisteredOnOverlay, eEventType, oRegistrationObject, oListener);
        } else if (oRegistrationObject instanceof IFeature) {
            oHandle = this.addEventHandler(this.oRegisteredOnFeature, eEventType, oRegistrationObject, oListener);
        } else if (oRegistrationObject instanceof ICamera) {
            oHandle = this.addEventHandler(this.oRegisteredOnCamera, eEventType, oRegistrationObject, oListener);
        } else if (oRegistrationObject instanceof ILookAt) {
            oHandle = this.addEventHandler(this.oRegisteredOnLookAt, eEventType, oRegistrationObject, oListener);
        } else if (oRegistrationObject instanceof IContainer) {
            oHandle = this.addEventHandler(this.oRegisteredOnContainer, eEventType, oRegistrationObject, oListener);
        }

        return oHandle;
    }

    private void getEventHandlersFromHash(UUID sContainerId,
                                          ListenerRegistrationHashMap oEventTypeHandlers,
                                          List<EventListenerHandle> oList) {
        ArrayList<EventListenerHandle> oHandleList;

        if (oEventTypeHandlers.containsKey(sContainerId)) {
            oHandleList = oEventTypeHandlers.get(sContainerId);
            for (EventListenerHandle oHandle: oHandleList) {
                if (!oList.contains(oHandle)) {
                    oList.add(oHandle);
                }
            }
        }
    }

    private void getContainerEventHandlers(EventListenerTypeEnum eEventType,
                                         IGeoBase oEventedObject,
                                         List<EventListenerHandle> oList) {
        IContainerSet oParentList;

        if (this.oRegisteredOnContainer.containsKey(eEventType)) {
            if (oEventedObject instanceof IFeature) {
                // Only process features.
                this.getEventHandlersFromHash(oEventedObject.getGeoId(), this.oRegisteredOnContainer.get(eEventType), oList);
            }
            oParentList = storageManager.getParentsOf(oEventedObject);
            for (IContainer oParentContainer : oParentList) {
                if (oParentContainer instanceof IContainer) {
                    this.getContainerEventHandlers(eEventType, oParentContainer, oList);
                }
            }
        }
    }

    private void getFeatureEventHandlers(EventListenerTypeEnum eEventType,
            IGeoBase oEventedObject,
            List<EventListenerHandle> oList) {
        IContainerSet oParentList;

        if (this.oRegisteredOnFeature.containsKey(eEventType)) {
            if (oEventedObject instanceof IFeature) {
                // Only process features.
                this.getEventHandlersFromHash(oEventedObject.getGeoId(), this.oRegisteredOnFeature.get(eEventType), oList);
            }
            oParentList = storageManager.getParentsOf(oEventedObject);
            for (IContainer oParentContainer : oParentList) {
                if (oParentContainer instanceof IFeature) {
                    this.getFeatureEventHandlers(eEventType, oParentContainer, oList);
                }
            }
        }
    }

    private void getOverlayEventHandlers(EventListenerTypeEnum eEventType,
            IGeoBase oEventedObject,
            List<EventListenerHandle> oList) {
        IContainerSet oParentList;

        if (this.oRegisteredOnOverlay.containsKey(eEventType)) {
            if (oEventedObject instanceof IOverlay) {
                // Only check overlays
                this.getEventHandlersFromHash(oEventedObject.getGeoId(), this.oRegisteredOnOverlay.get(eEventType), oList);
            }
            oParentList = storageManager.getParentsOf(oEventedObject);
            for (IContainer oParentContainer : oParentList) {
                this.getOverlayEventHandlers(eEventType, oParentContainer, oList);
            }
        }
    }

    private void getMapEventHandlers(EventListenerTypeEnum eEventType,
            IGeoBase oEventedObject,
            List<EventListenerHandle> oList) {
        IContainerSet oParentList;

        if (this.oRegisteredOnMap.containsKey(eEventType)) {
            if (oEventedObject instanceof IMap) {
                // Only check maps.
                this.getEventHandlersFromHash(oEventedObject.getGeoId(), this.oRegisteredOnMap.get(eEventType), oList);
            }
            oParentList = storageManager.getParentsOf(oEventedObject);
            for (IContainer oParentContainer : oParentList) {
                this.getMapEventHandlers(eEventType, oParentContainer, oList);
            }
        }
    }

    private void getEventHandlers(EventListenerTypeEnum eEventType,
            IGeoBase oEventedObject,
            List<EventListenerHandle> oList) {
        
        if (oEventedObject instanceof IMap) {
            this.getMapEventHandlers(eEventType, oEventedObject, oList);
        } else if (oEventedObject instanceof IOverlay) {
            this.getOverlayEventHandlers(eEventType, oEventedObject, oList);
            this.getMapEventHandlers(eEventType, oEventedObject, oList);
        } else if (oEventedObject instanceof IFeature) {
            this.getFeatureEventHandlers(eEventType, oEventedObject, oList);
            this.getOverlayEventHandlers(eEventType, oEventedObject, oList);
            this.getMapEventHandlers(eEventType, oEventedObject, oList);
        } else if (oEventedObject instanceof IContainer) {
            this.getContainerEventHandlers(eEventType, oEventedObject, oList);
            this.getFeatureEventHandlers(eEventType, oEventedObject, oList);
            this.getOverlayEventHandlers(eEventType, oEventedObject, oList);
            this.getMapEventHandlers(eEventType, oEventedObject, oList);

        }
    }
    
    private void callEventHandlers(ListenerHandleList handlerList, Event oEvent) {
        for (EventListenerHandle oHandle : handlerList) {
            try {
                oHandle.getListener().onEvent(oEvent);
            } catch (Exception Ex) {
                Log.e(TAG, "The client event handler for " + EventListenerTypeEnum.class.getSimpleName() + " generated an exception.", Ex);
            }
        }
    }

    private void processEvent(EventListenerTypeEnum eEventType, IGeoBase oEventedObject, Event oEvent) {
        ListenerHandleList oList = new ListenerHandleList();

        this.getEventHandlers(eEventType, oEventedObject, oList);
        this.callEventHandlers(oList, oEvent);
    }

    private void processEvent(EventListenerTypeEnum eEventType, List<IFeature> featureList, Event oEvent) {
        ListenerHandleList oList = new ListenerHandleList();

        for (IFeature feature: featureList) {
            this.getEventHandlers(eEventType, feature, oList);
        }
        this.callEventHandlers(oList, oEvent);
    }

    @Override
    public void generateContainerEvent(ContainerEventEnum eEvent,
                                       IContainer oEventedObject,
                                       IGeoBase oChild,
                                       Object userContext) {
        ContainerEvent oEvent = new ContainerEvent(eEvent, oEventedObject, oChild);
        oEvent.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, oEventedObject, oEvent);
    }

    @Override
    public void generateContainerEvent(ContainerEventEnum eEvent,
                                       IContainer oEventedObject,
                                       List<? extends IGeoBase> oList,
                                       Object userContext) {
        ContainerEvent oEvent = new ContainerEvent(eEvent, oEventedObject, oList);
        oEvent.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, oEventedObject, oEvent);
    }

    @Override
    public void generateFeatureEvent(FeatureEventEnum eEvent, IFeature oTarget, boolean bSelected, Object userContext) {
        FeatureEvent oEvent = new FeatureEvent(eEvent, oTarget, bSelected);
        oEvent.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.FEATURE_EVENT_LISTENER, oTarget, oEvent);
    }

    @Override
    public void  generateFeatureInteractionEvent(UserInteractionEventEnum eEvent,
                                                 EnumSet<UserInteractionKeyEnum> keys,
                                                 UserInteractionMouseButtonEnum button,
                                                 List<IFeature> oTargetList,
                                                 IMap oMap,
                                                 Point oPoint,
                                                 IGeoPosition oPosition,
                                                 IGeoPosition oStartPosition,
                                                 Object userContext) {
        FeatureUserInteractionEvent oEvent = new FeatureUserInteractionEvent(eEvent, keys, button, oTargetList, oMap, oPoint, oPosition, oStartPosition);
        oEvent.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.FEATURE_INTERACTION_EVENT_LISTENER, oTargetList, oEvent);
    }

    @Override
    public void generateMapStateChangeEvent(MapStateEnum ePreviousState, MapStateEnum eNewState,
                                            IMap oMap, Object userContext) {
        MapStateChangeEvent oEvent = new MapStateChangeEvent(ePreviousState, eNewState, oMap);
        oEvent.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.MAP_STATE_CHANGE_EVENT_LISTENER, oMap, oEvent);
    }

    @Override
    public void generateMapInteractionEvent(UserInteractionEventEnum eEvent,
                                            EnumSet<UserInteractionKeyEnum> oKeys,
                                            UserInteractionMouseButtonEnum oButton,
                                            IMap oMap,
                                            Point oPoint,
                                            IGeoPosition oPosition,
                                            IGeoPosition oStartPosition,
                                            Object userContext) {
        MapUserInteractionEvent oEvent = new MapUserInteractionEvent(eEvent, oKeys, oButton, oMap, oPoint, oPosition, oStartPosition);
        oEvent.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.MAP_INTERACTION_EVENT_LISTENER, oMap, oEvent);
    }

    @Override
    public void generateMapViewChangeEvent(MapViewEventEnum viewEventEnum, ICamera oCamera,
                                           ILookAt oLookAt, IGeoBounds bounds, IMap oMap, Object userContext) {
        MapViewChangeEvent oEvent = new MapViewChangeEvent(viewEventEnum, oCamera, oLookAt, bounds, oMap);
        oEvent.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.MAP_VIEW_CHANGE_EVENT_LISTENER, oMap, oEvent);
    }

    @Override
    public void generateMapFeatureAddedEvent(MapFeatureEventEnum eventEnum, IMap map,
                                             IFeature feature, Object userContext) {
        final MapFeatureAddedEvent event = new MapFeatureAddedEvent(eventEnum, map, feature);
        event.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.MAP_FEATURE_ADDED_EVENT_LISTENER, map, event);
    }

    @Override
    public void generateMapFeatureRemovedEvent(MapFeatureEventEnum eventEnum, IMap map,
                                               IFeature feature, Object userContext) {
        final MapFeatureRemovedEvent event = new MapFeatureRemovedEvent(eventEnum, map, feature);
        event.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.MAP_FEATURE_REMOVED_EVENT_LISTENER, map, event);
    }

    @Override
    public void generateMapCameraEvent(CameraEventEnum eventEnum, IMap map, ICamera camera,
                                       boolean animate, Object userContext) {
        mil.emp3.core.events.CameraEvent cameraEvent;
        ListenerRegistrationHashMap registrationList;
        ListenerHandleList handlerList;

        if (this.oRegisteredOnMap.containsKey(EventListenerTypeEnum.CAMERA_EVENT_LISTENER)) {
            registrationList = this.oRegisteredOnMap.get(EventListenerTypeEnum.CAMERA_EVENT_LISTENER);
            if (registrationList.containsKey(map.getGeoId())) {
                cameraEvent = new mil.emp3.core.events.CameraEvent(eventEnum, camera, animate);
                cameraEvent.setUserObject(userContext);
                handlerList = registrationList.get(map.getGeoId());
                this.callEventHandlers(handlerList, cameraEvent);
            }
        }
    }

    @Override
    public void generateVisibilityEvent(VisibilityActionEnum eEvent, IContainer oTarget,
                                        IContainer oParent, IMap oOnMap, Object userContext) {
        VisibilityEvent oEvent = new VisibilityEvent(eEvent, oTarget, oParent, oOnMap);
        oEvent.setUserObject(userContext);
        this.processEvent(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, oOnMap, oEvent);
    }
    
    @Override
    public void generateCameraEvent(CameraEventEnum eventEnum, ICamera camera,
                                    boolean animate, Object userContext) {
        mil.emp3.core.events.CameraEvent cameraEvent;
        ListenerRegistrationHashMap registrationList;
        ListenerHandleList handlerList;
        
        if (this.oRegisteredOnCamera.containsKey(EventListenerTypeEnum.CAMERA_EVENT_LISTENER)) {
            registrationList = this.oRegisteredOnCamera.get(EventListenerTypeEnum.CAMERA_EVENT_LISTENER);
            if (registrationList.containsKey(camera.getGeoId())) {
                cameraEvent = new mil.emp3.core.events.CameraEvent(eventEnum, camera, animate);
                cameraEvent.setUserObject(userContext);
                handlerList = registrationList.get(camera.getGeoId());
                this.callEventHandlers(handlerList, cameraEvent);
            }
        }
    }

    @Override
    public void generateLookAtEvent(LookAtEventEnum eventEnum, ILookAt lookAt,
                                    boolean animate, Object userContext) {
        mil.emp3.core.events.LookAtEvent lookAtEvent;
        ListenerRegistrationHashMap registrationList;
        ListenerHandleList handlerList;

        if (this.oRegisteredOnLookAt.containsKey(EventListenerTypeEnum.LOOKAT_EVENT_LISTENER)) {
            registrationList = this.oRegisteredOnLookAt.get(EventListenerTypeEnum.LOOKAT_EVENT_LISTENER);
            if (registrationList.containsKey(lookAt.getGeoId())) {
                lookAtEvent = new mil.emp3.core.events.LookAtEvent(eventEnum, lookAt, animate);
                lookAtEvent.setUserObject(userContext);
                handlerList = registrationList.get(lookAt.getGeoId());
                this.callEventHandlers(handlerList, lookAtEvent);
            }
        }
    }

    private void removeListener(EventListenerHandle oHandle, ArrayList<EventListenerHandle> oHandleList) {
        if (oHandleList.contains(oHandle)) {
            oHandleList.remove(oHandle);
        }
    }

    @Override
    public void removeEventHandler(mil.emp3.api.listeners.EventListenerHandle oHandle) {

        if (oHandle != null) {
            ListenerRegistrationHashMap oListenerList = null;
            EventListenerTypeEnum eListenerType = oHandle.getListenerType();
            IGeoBase oRegistrationObject = ((EventListenerHandle) oHandle).getRegistrationObject();

            if (oRegistrationObject instanceof IMap) {
                if (this.oRegisteredOnMap.containsKey(eListenerType)) {
                    oListenerList = this.oRegisteredOnMap.get(eListenerType);
                }
            } else if (oRegistrationObject instanceof IOverlay) {
                if (this.oRegisteredOnOverlay.containsKey(eListenerType)) {
                    oListenerList = this.oRegisteredOnOverlay.get(eListenerType);
                }
            } else if (oRegistrationObject instanceof IFeature) {
                if (this.oRegisteredOnFeature.containsKey(eListenerType)) {
                    oListenerList = this.oRegisteredOnFeature.get(eListenerType);
                }
            } else if (oRegistrationObject instanceof ICamera) {
                if (this.oRegisteredOnCamera.containsKey(eListenerType)) {
                    oListenerList = this.oRegisteredOnCamera.get(eListenerType);
                }
            } else if (oRegistrationObject instanceof IContainer) {
                if (this.oRegisteredOnContainer.containsKey(eListenerType)) {
                    oListenerList = this.oRegisteredOnContainer.get(eListenerType);
                }
            } else if (oRegistrationObject instanceof ILookAt) {
                if (this.oRegisteredOnLookAt.containsKey(eListenerType)) {
                    oListenerList = this.oRegisteredOnLookAt.get(eListenerType);
                }
            }

            if (oListenerList != null) {
                if (oListenerList.keySet().contains(oRegistrationObject.getGeoId())) {
                    this.removeListener((EventListenerHandle) oHandle, oListenerList.get(oRegistrationObject.getGeoId()));
                }
            }
        }
    }

    @Override
    public void generateFeatureEditEvent(FeatureEditEvent oEvent) {
        this.processEvent(EventListenerTypeEnum.FEATURE_EDIT_EVENT_LISTENER, oEvent.getTarget(), oEvent);
    }

    @Override
    public void generateFeatureDrawEvent(FeatureDrawEvent oEvent) {
        this.processEvent(EventListenerTypeEnum.FEATURE_DRAW_EVENT_LISTENER, oEvent.getTarget(), oEvent);
    }

    @Override
    public void generateFreehandDrawEvent(MapFreehandEvent event) {
        this.processEvent(EventListenerTypeEnum.FREEHAND_DRAW_EVENT_LISTENER, event.getTarget(), event);
    }
}
