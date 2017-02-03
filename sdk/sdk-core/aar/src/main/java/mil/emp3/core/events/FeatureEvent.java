package mil.emp3.core.events;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.enums.FeatureEventEnum;

/**
 * This class is used by the core to generate feature events.
 */
public class FeatureEvent extends mil.emp3.api.events.FeatureEvent {
    private final boolean bSelected;

    public FeatureEvent(FeatureEventEnum eEvent, IFeature oTarget, boolean bSelected) {
        super(eEvent, oTarget);
        this.bSelected = bSelected;
    }

    @Override
    public boolean getSelected() {
        return this.bSelected;
    }
}
