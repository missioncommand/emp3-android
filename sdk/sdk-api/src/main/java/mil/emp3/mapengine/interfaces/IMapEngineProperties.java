package mil.emp3.mapengine.interfaces;

/**
 * An object that holds map engine properties.
 */
public interface IMapEngineProperties {
    /**
     * Map engine name
     * @return string
     */
    String getName();

    /**
     * Map engine version
     * @return string
     */
    String getVersion();

    /**
     * Help
     * @return string
     */
    String getHelp();
}
