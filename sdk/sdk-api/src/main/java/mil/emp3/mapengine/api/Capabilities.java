package mil.emp3.mapengine.api;

import java.util.Arrays;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.enums.WMTSVersionEnum;
import mil.emp3.mapengine.interfaces.IMapEngineCapabilities;

/**
 * EMP3 Core queries this class to determine the capabilities supported by a specific map engine.
 * Currently this includes: shapes and symbols that can be plotted, version of supported WMS.
 */
public class Capabilities implements IMapEngineCapabilities {
    private final java.util.Set<FeatureTypeEnum> canPlotSet;
    private final java.util.Set<WMSVersionEnum> supportedWMSVersion;
    private final java.util.Set<WMTSVersionEnum> supportedWMTSVersion;
    
    public Capabilities(FeatureTypeEnum[] aFeatureTypes, WMSVersionEnum[] aVersions,
                        WMTSVersionEnum[] aWMTSVersions) {
        this.canPlotSet = new java.util.HashSet<>();
        this.supportedWMSVersion = new java.util.HashSet<>();
        this.supportedWMTSVersion = new java.util.HashSet<>();
        
        if (aFeatureTypes != null) {
            this.canPlotSet.addAll(Arrays.asList(aFeatureTypes));
        }
        
        if (aVersions != null) {
            this.supportedWMSVersion.addAll(Arrays.asList(aVersions));
        }

        if (aWMTSVersions != null) {
            this.supportedWMTSVersion.addAll(Arrays.asList((aWMTSVersions)));
        }
    }

    @Override
    public boolean canPlot(FeatureTypeEnum eFeatureType) {
        return this.canPlotSet.contains(eFeatureType);
    }

    @Override
    public boolean wmsVersionSupported(WMSVersionEnum eVersion) {
        return this.supportedWMSVersion.contains(eVersion);
    }

    @Override
    public boolean wmtsVersionSupported(WMTSVersionEnum eVersion) {
        return this.supportedWMTSVersion.contains(eVersion);
    }

}
