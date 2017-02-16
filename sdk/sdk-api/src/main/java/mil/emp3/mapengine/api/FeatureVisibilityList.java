package mil.emp3.mapengine.api;

import java.util.ArrayList;
import java.util.UUID;

/**
 * This class implements a list of {@link FeatureVisibility} objects.
 */
public class FeatureVisibilityList extends ArrayList<FeatureVisibility> {

    /**
     * Returns true if Feature with specified uuId is present in the list.
     * @param uuId
     * @return
     */
    public boolean contains(UUID uuId) {
        for (FeatureVisibility oRec: this) {
            if (oRec.feature.getGeoId().compareTo(uuId) == 0) {
                return true;
            }
        }
        return false;
    }
}
