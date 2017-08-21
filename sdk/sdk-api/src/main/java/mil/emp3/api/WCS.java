package mil.emp3.api;

import java.net.MalformedURLException;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.interfaces.IWCS;

public class WCS extends MapService implements IWCS {

    private final String coverageName;

    //nasa worldwind sectors
    private double latitudeDegree;
    private double longtitudeDegree;
    private double deltaLatitudeDegree;
    private double deltaLongitudeDegree;
    private int numberOfLayers;

    public WCS(String url, String name) throws MalformedURLException {
        super(url);
        this.latitudeDegree = -90;
        this.longtitudeDegree = -100;
        this.deltaLatitudeDegree = 100;
        this.deltaLongitudeDegree = 360;
        this.numberOfLayers = 12;

        coverageName = name;
    }

    public WCS(String url, String name, double latitudeDegree, double longtitudeDegree, double deltaLatitudeDegree, double deltaLongitudeDegree, int numberOfLayers) throws MalformedURLException {
        super(url);
        this.latitudeDegree = -latitudeDegree;
        this.longtitudeDegree = longtitudeDegree;
        this.deltaLatitudeDegree = deltaLatitudeDegree;
        this.deltaLongitudeDegree = deltaLongitudeDegree;
        this.numberOfLayers = numberOfLayers;

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

    public double getLatitudeDegree() {
        return this.latitudeDegree;
    }

    public void setLatitudeDegree(double degree){
        this.latitudeDegree = degree;
    }

    public double getLongitudeDegree() {
        return this.longtitudeDegree;
    }

    public void setLongtitudeDegree(double degree) {
        this.longtitudeDegree = degree;
    }

    public double getDeltaLatitudeDegree() {
        return this.deltaLatitudeDegree;
    }

    public void setDeltaLatitudeDegree(double degree) {
        this.deltaLatitudeDegree = degree;
    }

    public double getDeltaLongitudeDegree() {
        return this.deltaLongitudeDegree;
    }

    public void setDeltaLongitudeDegree(double degree) {
        this.deltaLongitudeDegree = degree;
    }

    public int getNumberOfLayers() {
        return this.numberOfLayers;
    }
    public void setNumberOfLayers(int num) {
        this.numberOfLayers = num;
    }
}
