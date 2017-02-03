package mil.emp3.api;

import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.IWMS;
import mil.emp3.api.utils.ManagerFactory;

/**
 * This class defines a WMS service.
 */
public class WMS implements IWMS {

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    private final java.util.UUID uniqueId;
    private String name = "";
    private String description = "";
    private WMSVersionEnum eVersion = WMSVersionEnum.VERSION_1_3;
    private boolean bTranparent = true;
    private String sTileFormat = "image/png";
    private java.util.List<String> oLayers;
    private final java.net.URL url;

    /**
     * This constructor creates a WMS
     * @param sURL This parameter set the url for the service.
     * @param eWMSVersion This parameter set the WMS version for the service.
     * @param sTileFormat This parameter sets the format of the tiles requested.
     * @param bTransparent this parameter set the transparency property.
     * @param oLayers This is a list of layers that must be displayed.
     * @throws java.net.MalformedURLException If the url string is invalid.
     */
    public WMS(String sURL, 
            WMSVersionEnum eWMSVersion, 
            String sTileFormat, 
            boolean bTransparent, 
            java.util.List<String> oLayers) 
            throws java.net.MalformedURLException {
        
        uniqueId = java.util.UUID.randomUUID();
        this.bTranparent = bTransparent;
        if (oLayers != null) {
            this.oLayers = oLayers;
        } else {
            this.oLayers = new java.util.ArrayList<>();
        }
        if ((sTileFormat != null) && !sTileFormat.isEmpty()) {
            this.sTileFormat = sTileFormat;
        }
        if (eWMSVersion != null) {
            this.eVersion = eWMSVersion;
        }

        this.url = new java.net.URL(sURL);
    }

    /**
     * Set the name for this WMS service.
     * @param sName the new name that will be set. A value of null sets the name 
     * to an empty string.
     */
    @Override
    public void setName(String sName) {
        if (sName == null) {
            this.name = "";
        } else {
            this.name = sName;
        }
    }

    /**
     * This method retrieves the name property.
     * @return String
     */
    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public java.util.UUID getGeoId() {
        return this.uniqueId;
    }

    /**
     * Set the name for this WMS service.
     * @param sDescription the new name that will be set. A value of null sets the name 
     * to an empty string.
     */
    @Override
    public void setDescription(String sDescription) {
        if (sDescription == null) {
            this.description = "";
        } else {
            this.description = sDescription;
        }
    }

    /**
     * This method retrieves the description property.
     * @return String
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * This method retrieves the URL.
     * @return java.net.URL
     */
    @Override
    public java.net.URL getURL() {
        return this.url;
    }

    /**
     * This method retrieves the wms version.
     * @return WMSVersionEnum
     */    
    public WMSVersionEnum getWMSVersion() {
        return this.eVersion;
    }

    /**
     * This method retrieves the title format.
     * @return String
     */    
    public String getTileFormat() {
        return this.sTileFormat;
    }

    /**
     * This method retrieves the transparency property.
     * @return String
     */    
    public boolean getTransaparent() {
        return this.bTranparent;
    }

    /**
     * This method sets the layer list. The updated list is sent to all maps utilizing
     * this service.
     * @param newLayers A list of string. The list is ignored if its null or empty.
     * @throws EMP_Exception 
     */
    @Override
    public void setLayers(java.util.List<String> newLayers) throws EMP_Exception {
        if ((newLayers != null) && !newLayers.isEmpty()) {
            this.oLayers = newLayers;
            storageManager.MapServiceUpdated(this);
        }
    }

    /**
     * This method retrieves the current layer list.
     * @return A list of String values.
     */
    @Override
    public java.util.List<String> getLayers() {
        return this.oLayers;
    }
}
