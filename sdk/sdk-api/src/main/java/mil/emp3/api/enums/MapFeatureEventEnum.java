package mil.emp3.api.enums;

import mil.emp3.api.interfaces.IEventEnum;

/**
 * This class enumerates the different feature-related behaviors a map can have.
 */
public enum MapFeatureEventEnum implements IEventEnum {

    /**
     * Indicates a feature has been added.
     */
    MAP_FEATURE_ADDED,

    /**
     * Indicates a feature has been removed.
     */
    MAP_FEATURE_REMOVED,

    ;
}
