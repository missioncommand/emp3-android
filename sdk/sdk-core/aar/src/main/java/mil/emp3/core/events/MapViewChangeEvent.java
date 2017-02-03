package mil.emp3.core.events;

import mil.emp3.api.enums.MapViewEventEnum;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.interfaces.IMap;
import org.cmapi.primitives.IGeoBounds;

/**
 * This class is used by the core to generate map view change events.
 */
public class MapViewChangeEvent extends mil.emp3.api.events.MapViewChangeEvent {
    private final ICamera oCamera;
    private final ILookAt oLookAt;
    private final IGeoBounds oBounds;

    public MapViewChangeEvent(MapViewEventEnum viewEventEnum,
            ICamera oCamera,
            ILookAt oLookAt,
            IGeoBounds bounds,
            IMap oMap) {
        super(viewEventEnum, oMap);
        this.oCamera = oCamera;
        this.oLookAt = oLookAt;
        this.oBounds = bounds;
    }

    @Override
    public ICamera getCamera() {
        return this.oCamera;
    }

    @Override
    public IGeoBounds getBounds() {
        return this.oBounds;
    }

    @Override
    public ILookAt getLookAt() { return  this.oLookAt; }
}
