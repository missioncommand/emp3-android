package mil.emp3.api.interfaces;

import java.util.List;

import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.exceptions.EMP_Exception;

/**
 *
 */
public interface IWMS extends IMapService {
    /**
     * This method retrieves the wms version.
     * @return WMSVersionEnum
     */    
    public WMSVersionEnum getWMSVersion();

    /**
     * This method retrieves the title format.
     * @return String
     */    
    public String getTileFormat();

    /**
     * This method retrieves the transparency property.
     * @return String
     */    
    public boolean getTransaparent();

    /**
     * This method sets the coordinate system.
     * @param coordinateSystem
     */

    public void setCoordinateSystem(String coordinateSystem) throws EMP_Exception;

    /** This method returns the coordinate system.
     * @return String
     */

    public String getCoordinateSystem();

    /**
     *  This method sets the styles for the map.
     */

    public void setStyles(List<String> styles);

    /**
     *  This method gets the styles for the map.
     */

    public List<String> getStyles();

    /**
     * This method sets the time string for the map.
     */
    public void setTimeString(String timeString);

    /**
     * This method gets the time string for the map
     */
    public String getTimeString();

    /**
     * This sets the resolution of the map data in meters per pixel.
     * @param resolution
     */

    public void setLayerResolution(double resolution);

    /**
     * This gets the resolution of the map data in meters per pixel.
     * @return
     */

    public double getLayerResolution();

}
