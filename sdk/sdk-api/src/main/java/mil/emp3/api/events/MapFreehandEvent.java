package mil.emp3.api.events;

import mil.emp3.api.enums.MapFreehandEventEnum;
import mil.emp3.api.interfaces.IMap;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;

/**
 * This class contains the map freehand event data.
 */
public abstract class MapFreehandEvent extends Event<MapFreehandEventEnum, IMap> {
    protected MapFreehandEvent(
            MapFreehandEventEnum eEvent,
            IMap map) {
        super(eEvent, map);
    }
    
    public abstract IGeoStrokeStyle getStyle();
    
    public abstract IGeoPositionGroup getPositionGroup();
}
