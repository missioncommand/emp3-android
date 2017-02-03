package mil.emp3.mapengine.api;

/**
 *
 * This class implements a list of {@link FeatureVisibility} objects.
 */
public class FeatureVisibilityList extends java.util.ArrayList<FeatureVisibility> {
    public boolean contains(java.util.UUID uuId) {
        for (FeatureVisibility oRec: this) {
            if (oRec.feature.getGeoId().compareTo(uuId) == 0) {
                return true;
            }
        }
        
        return false;
    }
}
