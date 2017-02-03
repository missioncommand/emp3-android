package mil.emp3.core.events;

import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.MapStateEnum;

/**
 * This class is used by the core to generate map state change events.
 */
public final class MapStateChangeEvent extends mil.emp3.api.events.MapStateChangeEvent {
    private final MapStateEnum eNewState;
    private final MapStateEnum ePreviousState;

    public MapStateChangeEvent(MapStateEnum ePreviousState, MapStateEnum eNewState, IMap oMap) {
        super(oMap);
        this.eNewState = eNewState;
        this.ePreviousState = ePreviousState;
    }

    @Override
    public MapStateEnum getNewState() {
        return this.eNewState;
    }

    @Override
    public MapStateEnum getPreviousState() {
        return this.ePreviousState;
    }
}
