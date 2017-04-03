package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoContainer;

import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;

/**
 * This interface defines the the methods associated with an overlay.
 */
public interface IOverlay extends IContainer, IGeoContainer {
    /**
     * This method retrieves a list of this overlay's child overlays.
     *
     * @return An ArrayList of zero or more IOverlay objects.
     */
    public List<IOverlay> getOverlays();

    /**
     * This method adds a child overlay to this overlay.
     *
     * @param overlay The child overlay to add.
     * @param visible True if the overlay is to be made visible, false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addOverlay(IOverlay overlay, boolean visible)
            throws EMP_Exception;

    /**
     * This method adds one or more child overlays to this overlay.
     *
     * @param overlays A list of child overlays to add.
     * @param visible True if the overlays are to be made visible, false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addOverlays(List<IOverlay> overlays, boolean visible)
            throws EMP_Exception;

    /**
     * This method removes a child overlay from this overlay.
     *
     * @param overlay The child overlay to remove.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeOverlay(IOverlay overlay)
            throws EMP_Exception;

    /**
     * This method removes one or more child overlays from this overlay.
     *
     * @param overlays A list of child overlays to remove.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeOverlays(List<IOverlay> overlays)
            throws EMP_Exception;

    /**
     * This method adds a child overlay to this overlay.
     *
     * @param overlay The child overlay to add.
     * @param visible True if the overlay is to be made visible, false otherwise.
     * @param object user defined object
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addOverlay(IOverlay overlay, boolean visible, Object object)
            throws EMP_Exception;

    /**
     * This method adds one or more child overlays to this overlay.
     *
     * @param overlays A list of child overlays to add.
     * @param visible True if the overlays are to be made visible, false otherwise.
     * @param object user defined object
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addOverlays(List<IOverlay> overlays, boolean visible, Object object)
            throws EMP_Exception;

    /**
     * This method removes a child overlay from this overlay.
     *
     * @param overlay The child overlay to remove.
     * @param object user defined object
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeOverlay(IOverlay overlay, Object object)
            throws EMP_Exception;

    /**
     * This method removes one or more child overlays from this overlay.
     *
     * @param overlays A list of child overlays to remove.
     * @param object user defined object
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeOverlays(List<IOverlay> overlays, Object object)
            throws EMP_Exception;

    /**
     * This method retrieves all child features of this overlay.
     * @return An ArrayList of IFeatures.
     */
    public List<IFeature> getFeatures();

    /**
     * This method adds a feature to this overlay.
     * @param feature The feature to add
     * @param visible True if the feature is to be made visible, false otherwise.
     * @param object user defined object
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addFeature(IFeature feature, boolean visible, Object object)
            throws EMP_Exception;

    /**
     * This method adds one or more features to this overlay.
     * @param features ArrayList of IFeatures to add.
     * @param visible True if the features are to be made visible, false otherwise.
     * @param object user defined object
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addFeatures(List<IFeature> features, boolean visible, Object object)
            throws EMP_Exception;

    /**
     * This method removes a child feature from this overlay.
     *
     * @param feature The child feature to remove.
     * @param object user defined object
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeFeature(IFeature feature, Object object)
            throws EMP_Exception;

    /**
     * This method removes one or more child features from this overlay.
     *
     * @param features A list of child features to remove.
     * @param object user defined object
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeFeatures(List<IFeature> features, Object object)
            throws EMP_Exception;

    /**
     * This method adds a feature to this overlay.
     * @param feature The feature to add
     * @param visible True if the feature is to be made visible, false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addFeature(IFeature feature, boolean visible)
            throws EMP_Exception;

    /**
     * This method adds one or more features to this overlay.
     * @param features ArrayList of IFeatures to add.
     * @param visible True if the features are to be made visible, false otherwise.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void addFeatures(List<IFeature> features, boolean visible)
            throws EMP_Exception;

    /**
     * This method removes a child feature from this overlay.
     *
     * @param feature The child feature to remove.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeFeature(IFeature feature)
            throws EMP_Exception;

    /**
     * This method removes one or more child features from this overlay.
     *
     * @param features A list of child features to remove.
     * @throws EMP_Exception The exception is raised if a processing error is encountered.
     */
    public void removeFeatures(List<IFeature> features)
            throws EMP_Exception;

    /**
     * This method triggers an update of the overlay. After changing any properties, this
     * method MUST be called to force an update.
     */
    public void apply();
}
