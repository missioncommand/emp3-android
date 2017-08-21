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
    private int numberOfLevels;

    public WCS(String url, String name) throws MalformedURLException {
        super(url);
        this.latitudeDegree = -90;
        this.longtitudeDegree = -180;
        this.deltaLatitudeDegree = 180;
        this.deltaLongitudeDegree = 360;
        this.numberOfLevels = 12;

        coverageName = name;
    }

    public WCS(String url, String name, double latitudeDegree, double longtitudeDegree, double deltaLatitudeDegree, double deltaLongitudeDegree, int numberOfLevels) throws MalformedURLException {
        super(url);
        this.latitudeDegree = -latitudeDegree;
        this.longtitudeDegree = longtitudeDegree;
        this.deltaLatitudeDegree = deltaLatitudeDegree;
        this.deltaLongitudeDegree = deltaLongitudeDegree;
        this.numberOfLevels = numberOfLevels;

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
        return "URL: " + getURL() + "\n" + "Coverage: " + coverageName + "\nBounding Box\n" +
        "Latitude Degree: " + getLatitudeDegree() + "\n" +
        "Delta Latitude Degree: " + getDeltaLatitudeDegree() + "\n" +
        "Longitude Degree: " + getLongitudeDegree() + "\n" +
        "Delta Longitude Degree: " + getDeltaLongitudeDegree() + "\n" +
        "Number of Levels: " + getNumberOfLevels();

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

    public int getNumberOfLevels() {
        return this.numberOfLevels;
    }
    public void setNumberOfLevels(int num) {
        this.numberOfLevels = num;
    }
}
