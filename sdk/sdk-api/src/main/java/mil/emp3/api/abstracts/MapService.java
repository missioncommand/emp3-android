package mil.emp3.api.abstracts;

import org.cmapi.primitives.GeoBase;

import java.net.MalformedURLException;

import mil.emp3.api.interfaces.IMapService;

/**
 * This abstract class is the base for all map service classes.
 */

public abstract class MapService extends GeoBase implements IMapService {
    private final java.net.URL mapServiceURL;

    protected MapService(String serviceURL)
            throws MalformedURLException {
        this.mapServiceURL = new java.net.URL(serviceURL);
    }

    public java.net.URL getURL() {
        return this.mapServiceURL;
    }

    @Override
    public String toString() {
        if(null != mapServiceURL) {
            return "URL: " + mapServiceURL.toString() + " ";
        } else {
            return "URL: ";
        }
    }
}
