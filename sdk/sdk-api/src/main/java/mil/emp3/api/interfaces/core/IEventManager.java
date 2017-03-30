package mil.emp3.api.interfaces.core;

import android.graphics.Point;

import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.EnumSet;
import java.util.List;

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

/*
 * This is an internal interface class.  The app developer must not implement this interface.
 */
public interface IEventManager {
    void setStorageManager(IStorageManager storageManager);

    void clear();

    EventListenerHandle addEventHandler(EventListenerTypeEnum eEventType, IGeoBase oRegistrationObject, IEventListener oListener) throws EMP_Exception;

    void generateContainerEvent(ContainerEventEnum eEvent,
                                IContainer oEventedObject,
                                IGeoBase oChild,
                                Object object);

    void generateContainerEvent(ContainerEventEnum eEvent,
                                IContainer oEventedObject,
                                List<? extends IGeoBase> oList,
                                Object object);

    void generateFeatureEvent(FeatureEventEnum eEvent, IFeature oTarget,
                              boolean bSelected,
                              Object object);

    void  generateFeatureInteractionEvent(UserInteractionEventEnum eEvent,
                                          EnumSet<UserInteractionKeyEnum> keys,
                                          UserInteractionMouseButtonEnum button,
                                          List<IFeature> oTargetList,
                                          IMap oMap,
                                          Point oPoint,
                                          IGeoPosition oPosition,
                                          IGeoPosition oStartPosition,
                                          Object object);

    void generateMapStateChangeEvent(MapStateEnum ePreviousState,
                                     MapStateEnum eNewState,
                                     IMap oMap,
                                     Object object);

    void generateMapInteractionEvent(UserInteractionEventEnum eEvent,
                                     EnumSet<UserInteractionKeyEnum> keys,
                                     UserInteractionMouseButtonEnum button,
                                     IMap oMap,
                                     Point oPoint,
                                     IGeoPosition oPosition,
                                     IGeoPosition oStartPosition,
                                     Object object);

    void generateMapViewChangeEvent(MapViewEventEnum viewEventEnum, ICamera oCamera, ILookAt oLookAt,
                                    IGeoBounds bounds, IMap oMap, Object object);

    void generateMapFeatureAddedEvent(MapFeatureEventEnum eventEnum,
                                      IMap map,
                                      IFeature feature,
                                      Object object);

    void generateMapFeatureRemovedEvent(MapFeatureEventEnum eventEnum,
                                        IMap map,
                                        IFeature feature,
                                        Object object);

    void generateMapCameraEvent(CameraEventEnum eventEnum,
                                IMap map,
                                ICamera camera,
                                boolean animate,
                                Object object);

    void generateVisibilityEvent(VisibilityActionEnum eEvent,
                                 IContainer oTarget,
                                 IContainer oParent,
                                 IMap oOnMap,
                                 Object object);

    void generateCameraEvent(CameraEventEnum eventEnum,
                             ICamera camera,
                             boolean animate,
                             Object object);

    void generateLookAtEvent(LookAtEventEnum eventEnum,
                             ILookAt lookAt,
                             boolean animate,
                             Object object);

    void removeEventHandler(mil.emp3.api.listeners.EventListenerHandle oHandle);

    void generateFeatureEditEvent(FeatureEditEvent oEvent, Object object);
    void generateFeatureDrawEvent(FeatureDrawEvent oEvent, Object object);
    void generateFreehandDrawEvent(MapFreehandEvent event, Object object);
}
