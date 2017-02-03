package mil.emp3.api.interfaces;

import mil.emp3.api.exceptions.EMP_Exception;

/**
 * This interface class defines the interface to all Map Services such as
 * WMS WMTS, etc ...
 */
public interface IMapService {
    /**
     * Set the name for this map service.
     * @param sName the new name that will be set. A value of null sets the name 
     * to an empty string.
     */
    public void setName(String sName);

    /**
     * This method retrieves the name property.
     * @return String
     */
    public String getName();

    /**
     * This method retrieves the unique identifier for the service.
     * @return java.util.UUID
     */
    public java.util.UUID getGeoId();

    /**
     * Set the name for this map service.
     * @param sDescription the new name that will be set. A value of null sets the name 
     * to an empty string.
     */
    public void setDescription(String sDescription);

    /**
     * This method retrieves the description property.
     * @return String
     */
    public String getDescription();

    /**
     * This method retrieves the URL.
     * @return java.net.URL
     */
    public java.net.URL getURL();

    /**
     * This method sets the layer list. The updated list is sent to all maps utilizing
     * this service.
     * @param newLayers A list of string. The list is ignored if its null or empty.
     * @throws EMP_Exception 
     */
    public void setLayers(java.util.List<String> newLayers) throws EMP_Exception;

    /**
     * This method retrieves the current layer list.
     * @return A list of String values.
     */
    public java.util.List<String> getLayers();
}
