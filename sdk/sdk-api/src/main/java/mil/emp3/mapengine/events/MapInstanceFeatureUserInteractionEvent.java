package mil.emp3.mapengine.events;

import org.cmapi.primitives.IGeoPosition;

import java.util.EnumSet;

import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.events.Event;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the Map Instance Feature User Interaction Event. It must be generated by all map engine
 * instances when the user interacts with a feature. See {@link UserInteractionEventEnum}.
 * All events of this type are handled by the EMP core code.
 */
public class MapInstanceFeatureUserInteractionEvent extends Event<UserInteractionEventEnum, IMapInstance> {
    private final android.graphics.Point oPoint;
    private final IGeoPosition oCoordinate;
    private final IGeoPosition oStartCoordinate;
    private final java.util.List<IFeature> oFeatureList;
    private UserInteractionMouseButtonEnum oButton;
    private final EnumSet<UserInteractionKeyEnum> oKeys;

    // eventConsumed is used internally by MapInstanceEventHandler. It is looked at by the GestureHandler (WW PickNavigateController)
    // for DRAG event only. Remember that DRAG and DRAG_COMPLETE events never propagate to applications. Applications should not
    // use this member.
    private boolean eventConsumed;
    /**
     * This constructor must be called by the map engines to creates a MapInstanceFeatureUserInteractionEvent event
     * @param oMapInstance The actual map instance. The this property of the object that implements the IMapInstance interface.
     * @param eEvent The enumerated value of the event that occurred. See {@link UserInteractionEventEnum}.
     * @param featureList A list of references to the features on which the event occurred.
     * @param oPointCoordinate The geographic coordinate of the location the event occurred.
     * @param oPosition The cartesian coordinate relative to the origin of the view container.
     * @param button Mouse button, left, right or middle
     * @param keys Keyboard keys, shift, ctrl and/or alt
     */
    public MapInstanceFeatureUserInteractionEvent(IMapInstance oMapInstance,
            UserInteractionEventEnum eEvent,
            EnumSet<UserInteractionKeyEnum> keys,
            UserInteractionMouseButtonEnum button,
            java.util.List<IFeature> featureList,
            android.graphics.Point oPointCoordinate,
            IGeoPosition oPosition) {
        super(eEvent, oMapInstance);
        this.oPoint = oPointCoordinate;
        this.oCoordinate = oPosition;
        this.oStartCoordinate = null;
        this.oFeatureList = featureList;
        this.oButton = button;
        this.oKeys = keys;
    }

    /**
     * This constructor must be called by the map engines to creates a MapInstanceFeatureUserInteractionEvent event
     * @param oMapInstance The actual map instance. The this property of the object that implements the IMapInstance interface.
     * @param eEvent The enumerated value of the event that occurred. See {@link UserInteractionEventEnum}.
     * @param featureList A list of references to the features on which the event occurred.
     * @param oPointCoordinate The geographic coordinate of the location the event occurred.
     * @param oPosition The cartesian coordinate relative to the origin of the view container.
     * @param button Mouse button, left, right or middle
     * @param keys Keyboard keys, shift, ctrl and/or alt
     */
    public MapInstanceFeatureUserInteractionEvent(IMapInstance oMapInstance,
            UserInteractionEventEnum eEvent,
            EnumSet<UserInteractionKeyEnum> keys,
            UserInteractionMouseButtonEnum button,
            java.util.List<IFeature> featureList,
            android.graphics.Point oPointCoordinate,
            IGeoPosition oPosition,
            IGeoPosition oStartPosition) {
        super(eEvent, oMapInstance);
        this.oPoint = oPointCoordinate;
        this.oCoordinate = oPosition;
        this.oStartCoordinate = oStartPosition;
        this.oFeatureList = featureList;
        this.oButton = button;
        this.oKeys = keys;
    }

    public android.graphics.Point getLocation() {
        return this.oPoint;
    }

    public IGeoPosition getCoordinate() {
        return this.oCoordinate;
    }

    public IGeoPosition getStartCoordinate() {
        return this.oStartCoordinate;
    }

    public java.util.List<IFeature> getFeatures() {
        return this.oFeatureList;
    }

    public UserInteractionMouseButtonEnum getButton() {
        return this.oButton;
    }

    public EnumSet<UserInteractionKeyEnum> getKeys() {
        return this.oKeys;
    }

    /**
     * eventConsumed is used internally by MapInstanceEventHandler. It is looked at by the GestureHandler (WW PickNavigateController)
     * for DRAG event only. Remember that DRAG and DRAG_COMPLETE events never propagate to applications. Applications should not
     * use this method.
     * @return true if event was consumed
     */
    public boolean isEventConsumed() {
        return eventConsumed;
    }

    /**
     * eventConsumed is used internally by MapInstanceEventHandler. It is looked at by the GestureHandler (WW PickNavigateController)
     * for DRAG event only. Remember that DRAG and DRAG_COMPLETE events never propagate to applications. Applications should not
     * use this method.
     * @param eventConsumed
     */
    public void setEventConsumed(boolean eventConsumed) {
        this.eventConsumed = eventConsumed;
    }
}
