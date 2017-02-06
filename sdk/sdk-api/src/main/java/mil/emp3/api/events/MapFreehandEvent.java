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

    /**
     * Gets the style that was used to draw the line.
     * @return style of line
     */
    public abstract IGeoStrokeStyle getStyle();

    /**
     * Gets the points from the drawing
     * @return points from drawing
     */
    public abstract IGeoPositionGroup getPositionGroup();
}
