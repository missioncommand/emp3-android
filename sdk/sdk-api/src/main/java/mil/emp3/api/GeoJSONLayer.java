package mil.emp3.api;

import java.net.MalformedURLException;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.interfaces.IGeoJSONLayer;

/**
 * This class implements a GeoJSON Layer map service.
 */

public class GeoJSONLayer extends MapService implements IGeoJSONLayer {

    private GeoJSON geoJSONFeature;

    protected GeoJSONLayer(String serviceURL) throws MalformedURLException {
        super(serviceURL);
    }
}
