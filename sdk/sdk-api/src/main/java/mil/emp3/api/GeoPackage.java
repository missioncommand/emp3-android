package mil.emp3.api;

import java.net.MalformedURLException;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.interfaces.IGeoPackage;

/**
 * Class to make use of GeoPackage SQLite databases
 */

public class GeoPackage extends MapService implements IGeoPackage {
    public GeoPackage(String serviceURL) throws MalformedURLException {
        super(serviceURL);
    }
}
