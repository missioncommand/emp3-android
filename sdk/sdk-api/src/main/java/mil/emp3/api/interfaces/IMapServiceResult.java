package mil.emp3.api.interfaces;

import java.util.UUID;

/**
 * Returns the result of a map service creation call
 * NASA WorldWind map engine makes async calls, so the result
 * can only be returned in a callback.
 */

public interface IMapServiceResult {

    /**
     *
     * @param success true if service was created
     * @param geoId geoId of the service created, null if creation failed
     * @param t exception thrown, if any when creation failed, null otherwise
     */

    void result(boolean success, UUID geoId, Throwable t);
}
