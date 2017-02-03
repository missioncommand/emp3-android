package mil.emp3.api.events;

import org.cmapi.primitives.IGeoPosition;

import java.util.EnumSet;
import java.util.List;

import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.UserInteractionEventEnum;

/**
 * This event is generated when the user interacts with features on a specific map.
 * To register for this event you must instantiate an object that implements the IFeatureInteractionEventListener
 * interface and register for the event on an IMap, IOverlay or IFeature.
 */
public abstract class FeatureUserInteractionEvent extends Event<UserInteractionEventEnum, List<IFeature>> {
    private final android.graphics.Point oPoint;
    private final IGeoPosition oCoordinate;
    private final IGeoPosition oStartCoordinate;
    private final UserInteractionMouseButtonEnum oButton;
    private final EnumSet<UserInteractionKeyEnum> oKeys;

    /**
     * This constructor is used by the core to generate feature user interaction events other than DRAG events.
     * @param eEvent The event being evented (Any event except drag).
     * @param oFeatureList The list of features affected by the event.
     * @param oPoint The X/Y coordinates of the event.
     * @param oPosition The geographic coordinate of the event.
     */
    protected FeatureUserInteractionEvent(UserInteractionEventEnum eEvent,
            EnumSet<UserInteractionKeyEnum> keys,
            UserInteractionMouseButtonEnum button,
            List<IFeature> oFeatureList,
            android.graphics.Point oPoint,
            IGeoPosition oPosition) {
        super(eEvent, oFeatureList);
        this.oPoint = oPoint;
        this.oCoordinate = oPosition;
        this.oStartCoordinate = null;
        this.oButton = button;
        this.oKeys = keys;
    }

    /**
     * This constructor is used by the core to generate feature user interaction drag events.
     * @param eEvent UserInteractionEventEnum.DRAG
     * @param oFeatureList The list of features affected by the event.
     * @param oPoint The X/Y coordinates of the event.
     * @param oPosition The geographic coordinate of the event end of the drag segment.
     * @param oStartPosition The geographic coordinate of the start of the drag segment.
     */
    protected FeatureUserInteractionEvent(UserInteractionEventEnum eEvent,
            EnumSet<UserInteractionKeyEnum> keys,
            UserInteractionMouseButtonEnum button,
            List<IFeature> oFeatureList,
            android.graphics.Point oPoint,
            IGeoPosition oPosition,
            IGeoPosition oStartPosition) {
        super(eEvent, oFeatureList);
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
     * @return {@android.graphics.Point}
     */
    public android.graphics.Point getPoint() {
        return this.oPoint;
    }

    /**
     * This method return the geographic coordinates of the location where the event occurred at.
     * The value of the coordinate is only valid for the duration of the event callback. The event handler
     * <b>MUST NOT</b> keep a reference to the coordinate object.
     * @return An object that implements the IGeoPosition interface, or null if the event occurred off the map.
     */
    public IGeoPosition getCoordinate() {
        return this.oCoordinate;
    }

    /**
     * This method returns the object that implements the {@link IMap} interface the event was generated on.
     * @return {@link IMap}
     */
    public abstract IMap getMapEventOccurredOn();

    /**
     * This method returns the start coordinate. It is only valid for DRAG event. It matches the coordinate
     * returned by the getCoordindate() of the previous drag event or the coordinate where the down event occured at.
     * The value of the coordinate is only valid for the duration of the event callback. The event handler
     * <b>MUST NOT</b> keep a reference to the coordinate object.
     * @return An object that implements the IGeoPosition interface, or null if the event occurred off the map.
     */
    public IGeoPosition getStartCoordinate() {
        return this.oStartCoordinate;
    }
    /**
     * This method returns the mouse button if pressed.
     * Values are left, right, middle or none.
     * @return enumerated keys
     */
    public UserInteractionMouseButtonEnum getButton() {
        return this.oButton;
    }

    /**
     * This method returns any keys if pressed
     * Values are Shift, Ctrl and Alt
     * @return enumerated keys
     */

    public EnumSet<UserInteractionKeyEnum> getKeys() {
        return this.oKeys;
    }
}
