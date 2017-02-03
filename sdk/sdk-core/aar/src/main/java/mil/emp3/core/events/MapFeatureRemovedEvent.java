package mil.emp3.core.events;

import mil.emp3.api.enums.MapFeatureEventEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;

public class MapFeatureRemovedEvent extends mil.emp3.api.events.MapFeatureRemovedEvent {
    public MapFeatureRemovedEvent(MapFeatureEventEnum eventEnum, IMap map, IFeature feature) {
        super(eventEnum, map, feature);
    }
}