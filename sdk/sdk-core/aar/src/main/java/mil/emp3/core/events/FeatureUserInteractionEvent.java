package mil.emp3.core.events;

import android.graphics.Point;

import org.cmapi.primitives.IGeoPosition;

import java.util.EnumSet;

import mil.emp3.api.enums.UserInteractionKeyEnum;
import mil.emp3.api.enums.UserInteractionMouseButtonEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.enums.UserInteractionEventEnum;

/**
 * This class is used by the core to generate feature user interaction events.
 */
public class FeatureUserInteractionEvent extends mil.emp3.api.events.FeatureUserInteractionEvent {
    private final IMap oEventOnMap;

    public FeatureUserInteractionEvent(UserInteractionEventEnum eEvent, EnumSet<UserInteractionKeyEnum> keys,
                                       UserInteractionMouseButtonEnum button, java.util.List<IFeature> featureList,
                                       IMap oMap, Point oPoint, IGeoPosition oPosition) {
        super(eEvent, keys, button, featureList, oPoint, oPosition);
        this.oEventOnMap = oMap;
    }

    public FeatureUserInteractionEvent(UserInteractionEventEnum eEvent, EnumSet<UserInteractionKeyEnum> keys,
                                       UserInteractionMouseButtonEnum button, java.util.List<IFeature> featureList,
                                       IMap oMap, Point oPoint, IGeoPosition oPosition, IGeoPosition oStartPosition) {
        super(eEvent, keys, button, featureList, oPoint, oPosition, oStartPosition);
        this.oEventOnMap = oMap;
    }

    @Override
    public IMap getMapEventOccurredOn() {
        return this.oEventOnMap;
    }
}
