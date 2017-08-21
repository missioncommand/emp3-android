package mil.emp3.api.interfaces;

public interface IWCS extends IMapService {

    /**
     * This method gets the url of the WCS service
     * @return string
     */

    String getServiceURL();

    /**
     * This method gets the coverage summary name
     * @return string
     */

    String getCoverageName();

    double getLatitudeDegree();
    double getLongitudeDegree();
    double getDeltaLatitudeDegree();
    double getDeltaLongitudeDegree();
    int getNumberOfLevels();

}
