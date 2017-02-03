package mil.emp3.api.events;

import org.cmapi.primitives.IGeoPosition;

import java.util.EnumSet;

import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.UserInteractionEventEnum;

/**
 * This event is generated when the user interacts with a specific map.
 * To register for this event you must instantiate an object that implements the IMapInteractionEventListener
 * interface and register for the event on an IMap.
 */
public abstract class MapUserInteractionEvent extends UserInteractionEvent<IMap> {
    /**
     * This constructor is used by the core to generate map user interaction events other than drag.
     * @param eEvent The event being generated.
     * @param oMap The map the event occurred on.
     * @param oPoint The x/y coordinate of the event.
     * @param oPosition The geographic coordinate of the event.
     * @param oButton Mouse button, left, right or middle
     * @param oKeys Keyboard keys, shift, ctrl and/or alt
     */
    protected MapUserInteractionEvent(UserInteractionEventEnum eEvent,
                                      EnumSet<UserInteractionKeyEnum> oKeys,
                                      UserInteractionMouseButtonEnum oButton,
                                      IMap oMap,
                                      android.graphics.Point oPoint,
                                      IGeoPosition oPosition) {
        super(eEvent, oKeys, oButton, oMap, oPoint, oPosition);
    }

    /**
     * This constructor is used by the core to generate map user interaction drag events.
     * @param eEvent The event being generated.
     * @param oMap The map the event occurred on.
     * @param oPoint The x/y coordinate of the event.
     * @param oPosition The geographic coordinate of the end of the current drag segment.
     * @param oStartPosition The geographic coordinate of the start of the drag segment.
     * @param oButton Mouse button, left, right or middle
     * @param oKeys Keyboard keys, shift, ctrl and/or alt
     */
    protected MapUserInteractionEvent(UserInteractionEventEnum eEvent,
                                      EnumSet<UserInteractionKeyEnum> oKeys,
                                      UserInteractionMouseButtonEnum oButton,
                                      IMap oMap,
                                      android.graphics.Point oPoint,
                                      IGeoPosition oPosition,
                                      IGeoPosition oStartPosition) {
        super(eEvent, oKeys, oButton, oMap, oPoint, oPosition, oStartPosition);
    }
}
