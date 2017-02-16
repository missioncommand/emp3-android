package mil.emp3.api.events;

import mil.emp3.api.enums.MapFeatureEventEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;

public abstract class MapFeatureRemovedEvent extends Event<MapFeatureEventEnum, IMap> {
    final private IFeature feature;

    protected MapFeatureRemovedEvent(MapFeatureEventEnum eEvent, IMap map, IFeature feature) {
        super(eEvent, map);
        this.feature = feature;
    }

    /**
     * Returns the feature for which the event was generated.
     * @return the feature removed
     */
    public IFeature getFeature() {
        return feature;
    }
}
