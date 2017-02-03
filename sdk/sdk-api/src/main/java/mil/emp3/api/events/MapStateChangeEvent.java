package mil.emp3.api.events;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.MapEventEnum;
import mil.emp3.api.enums.MapStateEnum;

/**
 * This event is generated when a specific map changes state.
 * To register for this event you must instantiate an object that implements the {@link IMapStateChangeEventListener}
 * interface and register for the event on an IMap.
 */
public abstract class MapStateChangeEvent extends Event<MapEventEnum, IMap> {
    protected MapStateChangeEvent(IMap oMap) {
        super(MapEventEnum.STATE_CHANGE, oMap);
    }

    /**
     * This method return the new state of the map.
     * @return {@link MapStateEnum}
     */
    public abstract MapStateEnum getNewState();

    /**
     * This method return the previous state of the map.
     * @return {@link MapStateEnum}
     */
    public abstract MapStateEnum getPreviousState();
}
