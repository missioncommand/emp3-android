package mil.emp3.mapengine.api;

import mil.emp3.mapengine.interfaces.IMapEngineRequirements;

/**
 * This class implements the IMapEngineRequirements interface. A map engine
 * developer is free to implement its own class as long as the interface is
 * implemented.
 */
public class Requirements implements IMapEngineRequirements {
    private final boolean wmsCapabilities;
    
    public Requirements(boolean requireWMSCapabilities) {
        this.wmsCapabilities = requireWMSCapabilities;
    }

    @Override
    public boolean requiresWMSCapabilities() {
        return this.wmsCapabilities;
    }
}
