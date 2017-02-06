package mil.emp3.mapengine.interfaces;

/**
 * An object that holds capabilities required by a specific map engine.
 */
public interface IMapEngineRequirements {
    /**
     * Returns true if map engine requires WMS capabilities.
     * @return
     */
    boolean requiresWMSCapabilities();
    /**
     * Returns true if map engine requires WMTS capabilities.
     * @return
     */
    public boolean requiresWMTSCapabilities();
}
