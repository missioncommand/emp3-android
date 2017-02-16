package mil.emp3.api.events;

import java.util.List;

import mil.emp3.api.enums.FeatureDrawEventEnum;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;

/**
 * This event class is generated during the process of drawing a feature.
 */
public abstract class FeatureDrawEvent extends Event<FeatureDrawEventEnum, IFeature> {
    protected FeatureDrawEvent(FeatureDrawEventEnum eEvent, IFeature oTarget) {
        super(eEvent, oTarget);
    }

    /**
     * This method return the map the event occurred on.
     * @return An object that implements the {@link IMap} interface.
     */
    public abstract IMap getMap();

    /**
     * This method retrieves a list of object that identifies what was updated in the feature. It only applies to DRAW_UPDATE events.
     * @return A list of {@link IEditUpdateData} object for DRAW_UPDATE events or null otherwise.
     */
    public abstract List<IEditUpdateData> getUpdateData();
}
