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
    WMSVersionEnum getWMSVersion();

    /**
     * This method retrieves the title format.
     * @return String
     */    
    String getTileFormat();

    /**
     * This method retrieves the transparency property.
     * @return String
     */    
    boolean getTransaparent();

    /**
     * This method sets the coordinate system.
     * @param coordinateSystem
     */

    void setCoordinateSystem(String coordinateSystem) throws EMP_Exception;

    /** This method returns the coordinate system.
     * @return String
     */

    String getCoordinateSystem();

    /**
     *  This method sets the styles for the map.
     */

    void setStyles(List<String> styles);

    /**
     *  This method gets the styles for the map.
     */

    List<String> getStyles();

    /**
     * This method sets the time string for the map.
     */
    void setTimeString(String timeString);

    /**
     * This method gets the time string for the map
     */
    String getTimeString();

    /**
     * This sets the resolution of the map data in meters per pixel.
     * @param resolution
     */

    void setLayerResolution(double resolution);

    /**
     * This gets the resolution of the map data in meters per pixel.
     * @return
     */

    double getLayerResolution();

    /**
     * This method sets the layer list. The updated list is sent to all maps utilizing
     * this service.
     * @param newLayers A list of string. The list is ignored if its null or empty.
     * @throws EMP_Exception
     */
    void setLayers(List<String> newLayers) throws EMP_Exception;

    /**
     * This method retrieves the current layer list.
     * @return A list of String values.
     */
    List<String> getLayers();
}
