package mil.emp3.mapengine.api;

import mil.emp3.mapengine.interfaces.IMapEngineRequirements;

/**
 * This class implements the IMapEngineRequirements interface. A map engine
 * developer is free to implement its own class as long as the interface is
 * implemented.
 */
public class Requirements implements IMapEngineRequirements {
    private final boolean wmsCapabilities;
    private final boolean wmtsCapabilities;

    /**
     * Initialize the required capabilities.
     * @param requireWMSCapabilities is true if map depends on WMS capabilities.
     * @param requireWMTSCapabilities is true if map depends on WMS capabilities.
     */
    public Requirements(boolean requireWMSCapabilities,
                        boolean requireWMTSCapabilities) {
        this.wmsCapabilities = requireWMSCapabilities;
        this.wmtsCapabilities = requireWMTSCapabilities;
    }

    @Override
    public boolean requiresWMSCapabilities() {
        return this.wmsCapabilities;
    }

    @Override
    public boolean requiresWMTSCapabilities() {
        return this.wmtsCapabilities;
    }
}
