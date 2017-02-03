package mil.emp3.mapengine.interfaces;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.WMSVersionEnum;

/**
 *
 * This class defines the interface to the the map engines capabilities.
 */
public interface IMapEngineCapabilities {
    public boolean canPlot(FeatureTypeEnum eFeatureType);
    public boolean wmsVersionSupported(WMSVersionEnum eVersion);
}
