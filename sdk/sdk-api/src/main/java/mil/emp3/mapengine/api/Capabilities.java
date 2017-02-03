package mil.emp3.mapengine.api;

import java.util.Arrays;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.mapengine.interfaces.IMapEngineCapabilities;

/**
 * This class implements the IMapEngineCapabilities interface. It represent the 
 * capabilities of a map engine which the core will constantly query. A map engine
 * developer is free to implement its own class as long as the interface is implemented.
 */
public class Capabilities implements IMapEngineCapabilities {
    private final java.util.Set<FeatureTypeEnum> canPlotSet;
    private final java.util.Set<WMSVersionEnum> supportedWMSVersion;
    
    public Capabilities(FeatureTypeEnum[] aFeatureTypes, WMSVersionEnum[] aVersions) {
        this.canPlotSet = new java.util.HashSet<>();
        this.supportedWMSVersion = new java.util.HashSet<>();
        
        if (aFeatureTypes != null) {
            this.canPlotSet.addAll(Arrays.asList(aFeatureTypes));
        }
        
        if (aVersions != null) {
            this.supportedWMSVersion.addAll(Arrays.asList(aVersions));
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

}
