package mil.emp3.mapengine.events;

import mil.emp3.api.enums.MapFeatureEventEnum;
import mil.emp3.api.events.Event;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.mapengine.interfaces.IMapInstance;

public class MapInstanceFeatureRemovedEvent extends Event<MapFeatureEventEnum, IMapInstance> {
    final private IFeature feature;

    /**
     * This constructor must be called by the map engines to creates a MapInstanceUserInteractionEvent event
     * @param mapInstance The actual map instance. The this property of the object that implements the IMapInstance interface.
     * @param event The feature event generated. See {@link MapFeatureEventEnum}.
     * @param feature The feature this event is associated with. See {@link IFeature}.
     */
    public MapInstanceFeatureRemovedEvent(IMapInstance mapInstance, MapFeatureEventEnum event, IFeature feature) {
        super(event, mapInstance);
        this.feature = feature;
    }

    public IFeature getFeature() {
        return feature;
    }
}
