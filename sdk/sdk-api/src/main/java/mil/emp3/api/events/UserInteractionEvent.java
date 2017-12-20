package mil.emp3.api.events;

import android.graphics.Point;
import android.graphics.PointF;

import org.cmapi.primitives.IGeoPosition;

import java.util.EnumSet;

import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.enums.UserInteractionEventEnum;

/**
 * This event is the base class for the FeatureUserInteractionEvent and the MapUserInteractionEvent.
 * @param <TargetType> A java type derived from {@link IContainer}.
 */
public abstract class UserInteractionEvent<TargetType extends IContainer> extends Event<UserInteractionEventEnum, TargetType>  {
    private final android.graphics.PointF oPoint;
    private final IGeoPosition oCoordinate;
    private final IGeoPosition oStartCoordinate;
    private UserInteractionMouseButtonEnum oButton;
    private final EnumSet<UserInteractionKeyEnum> oKeys;

//    protected UserInteractionEvent(UserInteractionEventEnum eEvent,
//                                   EnumSet<UserInteractionKeyEnum> keys,
//                                   UserInteractionMouseButtonEnum button,
//                                   TargetType oContainer,
//                                   android.graphics.Point oPoint,
//                                   IGeoPosition oPosition) {
//        super(eEvent, oContainer);
//        this.oPoint =  new PointF(oPoint.x, oPoint.y);
//        this.oCoordinate = oPosition;
//        this.oStartCoordinate = null;
//        this.oButton = button;
//        this.oKeys = keys;
//    }
//
//    protected UserInteractionEvent(UserInteractionEventEnum eEvent,
//                                   EnumSet<UserInteractionKeyEnum> keys,
//                                   UserInteractionMouseButtonEnum button,
//                                   TargetType oContainer,
//                                   android.graphics.Point oPoint,
//                                   IGeoPosition oPosition,
//                                   IGeoPosition oStartPosition) {
//        super(eEvent, oContainer);
//        this.oPoint = new PointF(oPoint.x, oPoint.y);
//        this.oCoordinate = oPosition;
//        this.oStartCoordinate = oStartPosition;
//        this.oButton = button;
//        this.oKeys = keys;
//    }

    protected UserInteractionEvent(UserInteractionEventEnum eEvent,
                                   EnumSet<UserInteractionKeyEnum> keys,
                                   UserInteractionMouseButtonEnum button,
                                   TargetType oContainer,
                                   android.graphics.PointF oPoint,
                                   IGeoPosition oPosition) {
        super(eEvent, oContainer);
        this.oPoint = oPoint;
        this.oCoordinate = oPosition;
        this.oStartCoordinate = null;
        this.oButton = button;
        this.oKeys = keys;
    }

    protected UserInteractionEvent(UserInteractionEventEnum eEvent,
                                   EnumSet<UserInteractionKeyEnum> keys,
                                   UserInteractionMouseButtonEnum button,
                                   TargetType oContainer,
                                   android.graphics.PointF oPoint,
                                   IGeoPosition oPosition,
                                   IGeoPosition oStartPosition) {
        super(eEvent, oContainer);
        this.oPoint = oPoint;
        this.oCoordinate = oPosition;
        this.oStartCoordinate = oStartPosition;
        this.oButton = button;
        this.oKeys = keys;
    }

    /**
     * This method returns the x,y coordinate of the location the event occurred at. Where 0,0
     * is the origin of the map container.
     * The value of the object is only valid for the duration of the event callback. The event handler
     * <b>MUST NOT</b> keep a reference to the object.
     *
     * Deprecated: Use getPointF() for a more precise event location
     *
     * @return {@android.graphics.Point}
     */
    @Deprecated
    public android.graphics.Point getPoint() {  return new Point((int)this.oPoint.x, (int)this.oPoint.y);  }


    /**
     * This method returns the x,y coordinate of the location the event occurred at. Where 0,0
     * is the origin of the map container.
     * The value of the object is only valid for the duration of the event callback. The event handler
     * <b>MUST NOT</b> keep a reference to the object.
     * @return {@android.graphics.Point}
     */
    public android.graphics.PointF getPointF() {
        return this.oPoint;
    }

    /**
     * This method return the geographic coordinates of the location where the event occurred at.
     * The value of the object is only valid for the duration of the event callback. The event handler
     * <b>MUST NOT</b> keep a reference to the object.
     * @return An object that implements the {@link org.cmapi.primitives.IGeoPosition} interface, or null if the event occurred off the map.
     */
    public IGeoPosition getCoordinate() {
        return this.oCoordinate;
    }

    /**
     * This method returns the start coordinate. It is only valid for DRAG event.
     * The value of the object is only valid for the duration of the event callback. The event handler
     * <b>MUST NOT</b> keep a reference to the object.
     * @return
     */
    public IGeoPosition getStartCoordinate() {
        return this.oStartCoordinate;
    }

    /**
     * This method returns the mouse button if pressed.
     * Values are left, right, middle or none.
     * @return
     */
    public UserInteractionMouseButtonEnum getButton() {
        return this.oButton;
    }

    /**
     * This method returns any keys if pressed
     * Values are Shift, Ctrl and Alt
     * @return
     */

    public EnumSet<UserInteractionKeyEnum> getKeys() {
        return this.oKeys;
    }
}
