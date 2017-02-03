package mil.emp3.core.events;

import mil.emp3.api.enums.MapFreehandEventEnum;
import mil.emp3.api.interfaces.IMap;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;

/**
 *
 */
public class MapFreehandEvent extends mil.emp3.api.events.MapFreehandEvent {
    private final IGeoStrokeStyle geoStrokeStyle;
    private final IGeoPositionGroup geoPositionGroup;
    
    public MapFreehandEvent(
            MapFreehandEventEnum eEvent,
            IMap map,
            IGeoStrokeStyle style,
            IGeoPositionGroup positionGroup) {
        super(eEvent, map);
        this.geoPositionGroup = positionGroup;
        this.geoStrokeStyle = style;
    }
    
    @Override
    public IGeoStrokeStyle getStyle() {
        return this.geoStrokeStyle;
    }
    
    @Override
    public IGeoPositionGroup getPositionGroup() {
        return this.geoPositionGroup;
    }
}
