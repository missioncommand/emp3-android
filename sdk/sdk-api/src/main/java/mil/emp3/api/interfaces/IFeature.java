package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoRenderable;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;

/**
 * This interface is the base for all features.
 */
public interface IFeature extends IContainer, IGeoRenderable {

    /**
     * This method returns a reference of the GeoRenderable object encapsulated in the feature.
     * @return See {@link IGeoRenderable}
     */
    public IGeoRenderable getRenderable();

    /**
     * This method returns the feature type.
     * @return See {@link FeatureTypeEnum}
     */
    public FeatureTypeEnum getFeatureType();

    /**
     * This method retrieves a list of all the feature's child features.
     * @return An ArrayList of zero or more IFeature objects.
     */
    public java.util.List<IFeature> getChildFeatures();

    /**
     * This method retrieves the list of all parent overlays of this feature.
     * @return List of IOverlays
     */
    public java.util.List<IOverlay> getParentOverlays();

    /**
     * This method retrieves the list of parent features of this feature.
     * @return List of IFeatures
     */
    public java.util.List<IFeature> getParentFeatures();

    /**
     * This method adds a child feature to this feature.
     * @param feature The child to add.
     * @param visible True if the feature is to be made visible or false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addFeature(IFeature feature, boolean visible)
            throws EMP_Exception;
    /**
     * This method adds one or more child features to this feature.
     * @param features A list of features to add.
     * @param visible True if the features are to be made visible or false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addFeatures(java.util.List<IFeature> features, boolean visible)
            throws EMP_Exception;

    /**
     * This method removes the child feature from this feature.
     * @param feature The child to remove.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeFeature(IFeature feature)
            throws EMP_Exception;
    /**
     * This method removes one or more child feature from this feature.
     * @param features A list of features to remove.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeFeatures(java.util.List<IFeature> features)
            throws EMP_Exception;

    /**
     * This method triggers an update of the feature. After changing any feature properties, this
     * method MUST be called to force an update to be sent to the map(s).
     */
    public void apply();
}
