package mil.emp3.api;

import java.net.MalformedURLException;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.interfaces.IKMLLayer;

/**
 * This class implements a KML Layer map service.
 */

public class KMLLayer extends MapService implements IKMLLayer {

    private KML kmlFeature;

    protected KMLLayer(String serviceURL) throws MalformedURLException {
        super(serviceURL);
    }
}
