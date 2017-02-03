package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;
/**
 * Created by jgiovino on 1/29/16.
 */
public enum FeatureEventEnum implements IEventEnum {
    /**
     * IFeature event indicating a feature has been selected(highlighted)
     */
    FEATURE_SELECTED,

    /**
     * IFeature event indicating a feature has been unselected(normal)
     */
    FEATURE_DESELECTED
}

