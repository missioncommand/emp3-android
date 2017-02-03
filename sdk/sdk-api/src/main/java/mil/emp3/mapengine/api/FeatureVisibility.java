package mil.emp3.mapengine.api;

import mil.emp3.api.interfaces.IFeature;

/**
 *
 * This class contains a reference to a feature and its visibility. It is used to add
 * or update features on a map instance. If visible is true the feature should be
 * made visible. If its false the feature should not be visible.
 */
public class FeatureVisibility {
    /**
     * This property is a reference to the feature to add or update.
     */
    public final IFeature feature;
    /**
     * This property indicates the features visibility.
     */
    public final boolean visible;
    
    public FeatureVisibility(IFeature featureToAdd, boolean bVisible) {
        this.feature = featureToAdd;
        this.visible = bVisible;
    }
}
