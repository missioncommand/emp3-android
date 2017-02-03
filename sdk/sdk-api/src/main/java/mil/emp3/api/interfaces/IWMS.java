package mil.emp3.api.interfaces;

import mil.emp3.api.enums.WMSVersionEnum;

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
}
