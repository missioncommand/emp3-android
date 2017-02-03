package mil.emp3.api.events;

import mil.emp3.api.interfaces.ICamera;
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
    
    public abstract IGeoBounds getBounds();

    public abstract ILookAt getLookAt();
}
