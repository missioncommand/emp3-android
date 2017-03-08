package mil.emp3.api.interfaces;

import org.cmapi.primitives.IGeoDocument;
import org.cmapi.primitives.IGeoRenderable;

import java.util.List;

public interface IGeoJSON extends IFeature<IGeoRenderable>, IGeoDocument {
    /**
     * This method retrieves the list of features generated from the GeoJSON string/stream. This method is for
     * internal use only. Client software manipulating the list or the features listed, will cause unforeseen results.
     * @return The list of features.
     */
    List<IFeature> getFeatureList();
}
