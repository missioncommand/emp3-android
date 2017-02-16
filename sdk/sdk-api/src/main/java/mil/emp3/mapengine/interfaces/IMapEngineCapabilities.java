package mil.emp3.mapengine.interfaces;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.enums.WMTSVersionEnum;

/**
 * An object that holds map engine capabilities.
 */
public interface IMapEngineCapabilities {
    /**
     * Returns true if map engine can plot the specified feature type {@link FeatureTypeEnum}
     * @param eFeatureType
     * @return
     */
    boolean canPlot(FeatureTypeEnum eFeatureType);

    /**
     * Returns true if specified wms version is supported.
     * @param eVersion
     * @return
     */
    boolean wmsVersionSupported(WMSVersionEnum eVersion);

    /**
     * Returns true if specified wmts version is supported.
     * @param wmtsVersionEnum
     * @return
     */
    public boolean wmtsVersionSupported(WMTSVersionEnum wmtsVersionEnum);
}
