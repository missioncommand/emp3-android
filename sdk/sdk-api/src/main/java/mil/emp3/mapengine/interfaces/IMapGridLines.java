package mil.emp3.mapengine.interfaces;

import java.util.Date;
import java.util.List;

import mil.emp3.api.interfaces.IFeature;

/**
 * This class defines the interface to a map grid line generator
 */

public interface IMapGridLines {
    /**
     * This method returns the data the grid was updated.
     * @return java.util.Date
     */
    Date getLastUpdated();

    /**
     * This method return the list of feature that must be rendered to display the grid lines.
     * @return java.util.List of {@link IFeature}
     */
    List<IFeature> getGridFeatures();
}
