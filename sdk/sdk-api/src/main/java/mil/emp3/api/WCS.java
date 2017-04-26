package mil.emp3.api;

import java.net.MalformedURLException;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.interfaces.IWCS;

public class WCS extends MapService implements IWCS {

    private final String coverageName;

    public WCS(String url, String name) throws MalformedURLException {
        super(url);
        coverageName = name;
    }

    @Override
    public String getServiceURL() {
        return getURL().toString();
    }

    @Override
    public String getCoverageName() {
        return coverageName;
    }

    /**
     * Prints all configuration parameters
     */
    @Override
    public String toString() {
        return "URL: " + getURL() + "\n" + "Coverage: " + coverageName;
    }
}
