package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoRenderable;

import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;

/**
 * This interface is the base for all features.
 *
 * All single point and multi point shapes and symbols that are displayed on the Map use Feature as their base class. Feature class provides for
 * graphical properties, hierarchy placement and geo spatial positions among other attributes. Feature is a Container and as such it can contain
 * other Features. A Feature can be a child of many other containers and it can have many children. Visibility of a feature is governed by its
 * own visibility and visibility of its parents.
 */
public interface IFeature<T extends IGeoRenderable> extends IContainer, IGeoRenderable {

    /**
     * This method returns a reference of the GeoRenderable object encapsulated in the feature.
     * @return An object derived from {@link IGeoRenderable}
     */
    T getRenderable();

    /**
     * This method returns the feature type.
     * @return See {@link FeatureTypeEnum}
     */
    FeatureTypeEnum getFeatureType();

    /**
     * This method retrieves a list of all the feature's child features, which includes all descendants.
     * @return An ArrayList of zero or more IFeature objects.
     */
    List<IFeature> getChildFeatures();

    /**
     * This method retrieves the list of all parent overlays of this feature.
     * @return List of IOverlays
     */
    List<IOverlay> getParentOverlays();

    /**
     * This method retrieves the list of parent features of this feature.
     * @return List of IFeatures
     */
    List<IFeature> getParentFeatures();

    /**
     * This method adds a child feature to this feature.
     * @param feature The child to add.
     * @param visible True if the feature is to be made visible or false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void addFeature(IFeature feature, boolean visible)
            throws EMP_Exception;
    /**
     * This method adds one or more child features to this feature.
     * @param features A list of features to add.
     * @param visible True if the features are to be made visible or false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void addFeatures(List<IFeature> features, boolean visible)
            throws EMP_Exception;

    /**
     * This method removes the child feature from this feature.
     * @param feature The child to remove.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void removeFeature(IFeature feature)
            throws EMP_Exception;
    /**
     * This method removes one or more descendant features from this feature.
     * @param features A list of features to remove.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    void removeFeatures(List<IFeature> features)
            throws EMP_Exception;

    /**
     * This method triggers an update of the feature. After changing any feature properties, this
     * method MUST be called to force an update to be sent to the map(s).
     */
    void apply();

    /**
     * This method validates the feature.  It throws an unchecked exception if validation fails.
     */
    void validate();
}
