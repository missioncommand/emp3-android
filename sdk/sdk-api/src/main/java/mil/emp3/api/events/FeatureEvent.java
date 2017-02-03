package mil.emp3.api.events;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.enums.FeatureEventEnum;

/**
 * This event class is generated when a feature's selection state changes.
 */
public abstract class FeatureEvent extends Event<FeatureEventEnum, IFeature> {
    protected FeatureEvent(FeatureEventEnum eEvent, IFeature oTarget) {
        super(eEvent, oTarget);
    }

    public abstract boolean getSelected();
}
