package mil.emp3.api.interfaces;

import java.util.List;
import org.cmapi.primitives.IGeoBase;

import mil.emp3.api.exceptions.EMP_Exception;

/**
 * This interface class defines the interface to all Map Services such as
 * WMS WMTS, etc ...
 */
public interface IMapService extends IGeoBase {
    /**
     * This method retrieves the URL.
     * @return java.net.URL
     */
    java.net.URL getURL();
}
