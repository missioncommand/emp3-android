package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoDocument;
import org.cmapi.primitives.IGeoRenderable;

import java.util.List;

/**
 * This class defines the interface to a KML Feature object.
 */
public interface IKML extends IFeature<IGeoRenderable>, IGeoDocument {
    /**
     * This method retrieves the list of feature generated from the KML document. This method is for
     * internal use only. Client software manipulating the list or the features listed, will cause unforeseen results.
     * @return The list of features.
     */
    List<IFeature> getFeatureList();

    /**
     * This method retrieves the list of image layers generated from the ground overlays in the KML document. This method is for
     * internal use only. Client software manipulating the list or the image layers listed, will cause unforeseen results.
     * @return The list of image layers.
     */
    List<IImageLayer> getImageLayerList();

    /**
     * This method retrieves a list of features which are generated from a KML placemark with the specified KML id.
     * @param kmlId The placemark Id.
     * @return The list of features with the specified KML id.
     */
    List<IFeature> findKMLId(String kmlId);

    /**
     * This method retrieves a KML string representing all contained features and image layers. It is important
     * to understand that the string returned is a valid KML document, but may not match the initial document.
     * @return
     */
    String exportToKML();
}
