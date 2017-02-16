package mil.emp3.api.interfaces;

import java.util.List;

import mil.emp3.api.enums.WMTSVersionEnum;

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


}
