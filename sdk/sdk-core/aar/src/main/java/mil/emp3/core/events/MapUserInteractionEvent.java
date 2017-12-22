package mil.emp3.core.events;

import android.graphics.Point;
import android.graphics.PointF;

import org.cmapi.primitives.IGeoPosition;

import java.util.EnumSet;

import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.UserInteractionEventEnum;

/**
 * This class is used by the core to generate map user interaction events.
 */
public class MapUserInteractionEvent extends mil.emp3.api.events.MapUserInteractionEvent {

    @Deprecated
    public MapUserInteractionEvent(UserInteractionEventEnum eEvent,
                                   EnumSet<UserInteractionKeyEnum> oKeys,
                                   UserInteractionMouseButtonEnum oButton,
                                   IMap oMap,
                                   Point oPoint,
                                   IGeoPosition oPosition) {
        super(eEvent, oKeys, oButton, oMap, oPoint == null ? null : new PointF(oPoint.x, oPoint.y), oPosition);
    }

    @Deprecated
    public MapUserInteractionEvent(UserInteractionEventEnum eEvent,
                                   EnumSet<UserInteractionKeyEnum> oKeys,
                                   UserInteractionMouseButtonEnum oButton,
                                   IMap oMap,
                                   Point oPoint,
                                   IGeoPosition oPosition,
                                   IGeoPosition oStartPosition) {
        super(eEvent, oKeys, oButton, oMap,  oPoint == null ? null : new PointF(oPoint.x, oPoint.y), oPosition, oStartPosition);
    }

    public MapUserInteractionEvent(UserInteractionEventEnum eEvent,
                                   EnumSet<UserInteractionKeyEnum> oKeys,
                                   UserInteractionMouseButtonEnum oButton,
                                   IMap oMap,
                                   PointF oPoint,
                                   IGeoPosition oPosition) {
        super(eEvent, oKeys, oButton, oMap, oPoint, oPosition);
    }

    public MapUserInteractionEvent(UserInteractionEventEnum eEvent,
                                   EnumSet<UserInteractionKeyEnum> oKeys,
                                   UserInteractionMouseButtonEnum oButton,
                                   IMap oMap,
                                   PointF oPoint,
                                   IGeoPosition oPosition,
                                   IGeoPosition oStartPosition) {
        super(eEvent, oKeys, oButton, oMap, oPoint, oPosition, oStartPosition);
    }
}
