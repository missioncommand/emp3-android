package mil.emp3.api.events;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpBoundingArea;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.MapViewEventEnum;
import org.cmapi.primitives.IGeoBounds;

/**
 * This event class is generated when a maps view is  changes.
 */
public abstract class MapViewChangeEvent extends Event<MapViewEventEnum, IMap> {
    protected MapViewChangeEvent(MapViewEventEnum eventEnum, IMap oMap) {
        super(eventEnum, oMap);
    }

    /**
     * This method return the new view parameters.
     * @return @See ICamera
     */
    public abstract ICamera getCamera();

    /**
     * Gets the map bounds after the event. Returned value could be null based on current camera settings.
     * If event is VIEW_IN_MOTION then getBounds will return null.
     * @return bounds if available else null
     */
    public abstract IGeoBounds getBounds();

    /**
     * gets the map bounding area, if available, after Map motion has stopped.
     * If event is VIEW_IN_MOTION then getBoundingArea will be null.
     * @return boundingArea if available else null
     */
    public abstract IEmpBoundingArea getBoundingArea();

    /**
     * Gets the updated lookAt object after the event.
     * @return @See ILookAt
     */
    public abstract ILookAt getLookAt();
}
