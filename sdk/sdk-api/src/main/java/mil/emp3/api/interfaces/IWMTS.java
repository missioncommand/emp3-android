package mil.emp3.api.interfaces;

import java.util.List;

import mil.emp3.api.enums.WMTSVersionEnum;
import mil.emp3.api.exceptions.EMP_Exception;

public interface IWMTS extends IMapService{

    /**
     * This method retrieves the wmts version.
     * @return WMTSVersionEnum
     */
    WMTSVersionEnum getWMTSVersion();

    /**
     * This method retrieves the title format.
     * @return String
     */
    String getTileFormat();

    /**
     * This method sets a list of dimensions to support
     * Internally converted to a comma separated string
     * Example: dimensions=Elevation,Wavelength,Time
     */

    void setDimensions(List<String> dimensions);

    /**
     *  This method gets the dimensions for the map.
     *  @return list of strings
     */

    List<String> getDimensions();

    /**
     * This method sets the styles for the map.
     * Internally converted to a comma separated string
     * Example: styles=thickblue,thickred
     */

    void setStyles(List<String> styles);

    /**
     *  This method gets the styles for the map.
     *  @return list of strings
     */

    List<String> getStyles();

    /**
     * This method sets the layer list. The updated list is sent to all maps utilizing
     * this service.
     * @param newLayers A list of string. The list is ignored if its null or empty.
     * @throws EMP_Exception
     */
    public void setLayers(List<String> newLayers) throws EMP_Exception;

    /**
     * This method retrieves the current layer list.
     * @return A list of String values.
     */
    public List<String> getLayers();

}
