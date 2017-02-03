package mil.emp3.api.events;

import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.FeatureEditEventEnum;

/**
 * This event class is generated during the process of editing a feature.
 */
public abstract class FeatureEditEvent extends Event<FeatureEditEventEnum, IFeature> {
    protected FeatureEditEvent(FeatureEditEventEnum eEvent, IFeature oTarget) {
        super(eEvent, oTarget);
    }

    /**
     * This method return the map the event occurred on.
     * @return An object that implements the {@link IMap} interface.
     */
    public abstract IMap getMap();

    /**
     * This method retrieves a list of object that identifies what was updated in the feature. It only applies to EDIT_UPDATE events.
     * @return A list of {@link IEditUpdateData} object for EDIT_UPDATE events or null otherwise.
     */
    public abstract java.util.List<IEditUpdateData> getUpdateData();
}
