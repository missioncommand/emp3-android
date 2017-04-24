package mil.emp3.api;

import java.net.MalformedURLException;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.interfaces.IWCS;

public class WCS extends MapService implements IWCS {

    private final String serviceURL;
    private final String coverageName;

    public WCS(String url, String name) throws MalformedURLException {
        super(url);
        serviceURL = url;
        coverageName = name;
    }

    @Override
    public String getServiceURL() {
        return serviceURL;
    }

    @Override
    public String getCoverageName() {
        return coverageName;
    }
}
