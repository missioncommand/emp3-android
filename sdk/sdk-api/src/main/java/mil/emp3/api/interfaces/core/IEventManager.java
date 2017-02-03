package mil.emp3.api.interfaces.core;

import android.graphics.Point;

import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.EnumSet;

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
import mil.emp3.api.events.FeatureDrawEvent;
import mil.emp3.api.events.FeatureEditEvent;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.events.MapFreehandEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IEventListener;

public interface IEventManager {
    void setStorageManager(IStorageManager storageManager);

    void clear();

    EventListenerHandle addEventHandler(EventListenerTypeEnum eEventType, IGeoBase oRegistrationObject, IEventListener oListener) throws EMP_Exception;

    void generateContainerEvent(ContainerEventEnum eEvent,
                                IContainer oEventedObject,
                                IGeoBase oChild);

    void generateContainerEvent(ContainerEventEnum eEvent,
                                IContainer oEventedObject,
                                java.util.List<? extends IGeoBase> oList);

    void generateFeatureEvent(FeatureEventEnum eEvent, IFeature oTarget, boolean bSelected);

    void  generateFeatureInteractionEvent(UserInteractionEventEnum eEvent,
                                          EnumSet<UserInteractionKeyEnum> keys,
                                          UserInteractionMouseButtonEnum button,
                                          java.util.List<IFeature> oTargetList,
                                          IMap oMap,
                                          Point oPoint,
                                          IGeoPosition oPosition,
                                          IGeoPosition oStartPosition);

    void generateMapStateChangeEvent(MapStateEnum ePreviousState, MapStateEnum eNewState, IMap oMap);

    void generateMapInteractionEvent(UserInteractionEventEnum eEvent,
                                     EnumSet<UserInteractionKeyEnum> keys,
                                     UserInteractionMouseButtonEnum button,
                                     IMap oMap,
                                     Point oPoint,
                                     IGeoPosition oPosition,
                                     IGeoPosition oStartPosition);

    void generateMapViewChangeEvent(MapViewEventEnum viewEventEnum, ICamera oCamera, ILookAt oLookAt,
                                    IGeoBounds bounds, IMap oMap);

    void generateMapFeatureAddedEvent(MapFeatureEventEnum eventEnum, IMap map, IFeature feature);

    void generateMapFeatureRemovedEvent(MapFeatureEventEnum eventEnum, IMap map, IFeature feature);

    void generateVisibilityEvent(VisibilityActionEnum eEvent, IContainer oTarget, IContainer oParent, IMap oOnMap);

    void generateCameraEvent(CameraEventEnum eventEnum, ICamera camera, boolean animate);

    void generateLookAtEvent(LookAtEventEnum eventEnum, ILookAt lookAt, boolean animate);

    void removeEventHandler(mil.emp3.api.listeners.EventListenerHandle oHandle);

    void generateFeatureEditEvent(FeatureEditEvent oEvent);
    void generateFeatureDrawEvent(FeatureDrawEvent oEvent);
    void generateFreehandDrawEvent(MapFreehandEvent event);
}
