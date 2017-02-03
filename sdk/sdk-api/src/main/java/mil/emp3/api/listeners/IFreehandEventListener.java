package mil.emp3.api.listeners;

import mil.emp3.api.interfaces.IMap;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;

/**
 * A class must implement this interface to process freehand draw events.
 */
public interface IFreehandEventListener {
    /**
     * This method is called when the map enters freehand draw mode.
     * @param map This parameter indicates the map the event occurred on.
     */
    public void onEnterFreeHandDrawMode(IMap map);
    /**
     * This method is called each time a freehand draw is started.
     * @param map This parameter indicates the map the event occurred on.
     * @param positionList The list of coordinates.
     */
    public void onFreeHandLineDrawStart(IMap map, IGeoPositionGroup positionList);
    /**
     * This method is called each time a freehand draw is updated.
     * @param map This parameter indicates the map the event occurred on.
     * @param positionList The list of coordinates.
     */
    public void onFreeHandLineDrawUpdate(IMap map, IGeoPositionGroup positionList);
    /**
     * This method is called when the freehand line draw completes (the use lifted its finger).
     * @param map This parameter indicates the map the event occurred on.
     * @param style The style the line was drawn with.
     * @param positionList The list of coordinates.
     */
    public void onFreeHandLineDrawEnd(IMap map, IGeoStrokeStyle style, IGeoPositionGroup positionList);
    /**
     * This method is called when the map exits free hand draw mode.
     * @param map This parameter indicates the map the event occurred on.
     */
    public void onExitFreeHandDrawMode(IMap map);
    /**
     * This method is called if an error is encountered.
     * @param map This parameter indicates the map the event occurred on.
     * @param errorMessage A text description of the error.
     */
    public void onDrawError(IMap map, String errorMessage);
}
