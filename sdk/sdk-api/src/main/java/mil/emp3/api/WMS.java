package mil.emp3.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.enums.WMSVersionEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.IWMS;
import mil.emp3.api.utils.ManagerFactory;

/**
 * This class defines a WMS service.
 */
public class WMS extends MapService implements IWMS {

    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    private WMSVersionEnum eVersion = null;
    private boolean bTransparent = true;
    private String sTileFormat = "image/png";
    private List<String> oLayers;
    private String coordinateSystem = null;
    private List<String> oStyles = null;
    private String oTimeString = null;
    private double oResolution = 0;
    private double oControl = 0;
    private final static String NULL_LAYER = "WMS layer name can't be null or empty";

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
            List<String> oLayers)
            throws java.net.MalformedURLException {
        super(sURL);

        this.bTransparent = bTransparent;
        if (oLayers != null) {
            this.oLayers = oLayers;
        } else {
            this.oLayers = new ArrayList<>();
        }
        if ((sTileFormat != null) && !sTileFormat.isEmpty()) {
            this.sTileFormat = sTileFormat;
        }
        if (eWMSVersion == null) {
            this.eVersion = WMSVersionEnum.VERSION_1_3_0;
        } else {
            this.eVersion = eWMSVersion;
        }
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
        return this.bTransparent;
    }

    @Override
    public void setCoordinateSystem(String coordinateSystem) throws EMP_Exception {
        if (coordinateSystem != null && !coordinateSystem.isEmpty()) {
            this.coordinateSystem = coordinateSystem;
        } else {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER,
                    "Coordinate system can't be null or empty");
        }
    }

    @Override
    public String getCoordinateSystem() {
        return coordinateSystem;
    }

    @Override
    public void setStyles(List<String> styles) {
        if (styles != null)
            oStyles = styles;
    }

    @Override
    public List<String> getStyles() {
        return oStyles;
    }

    @Override
    public void setTimeString(String timeString) {
        if (timeString != null && !timeString.isEmpty()) {
            oTimeString = timeString;
        }
    }

    @Override
    public String getTimeString() {
        return oTimeString;
    }

    @Override
    public void setLayerResolution(double resolution) {
        oResolution = resolution;
    }

    @Override
    public double getLayerResolution() {
        return oResolution;
    }

    /**
     * This method sets the layer list. The updated list is sent to all maps utilizing
     * this service.
     * @param newLayers A list of string. The list is ignored if its null or empty.
     * @throws EMP_Exception 
     */
    public void setLayers(List<String> newLayers) throws EMP_Exception {
        if ((newLayers != null) && !newLayers.isEmpty()) {
            this.oLayers = newLayers;
            storageManager.MapServiceUpdated(this);
        }
    }

    /**
     * This method retrieves the current layer list.
     * @return A list of String values.
     */
    public List<String> getLayers() {
        return this.oLayers;
    }

    /**
     * Prints all configuration parameters
     */
    @Override
    public String toString() {
        String str = "URL: " + getURL() + "\n"
                + "WMS Version: " + eVersion.toString() + "\n"
                + "Transparent: " + bTransparent + "\n"
                + "Tile format: " + sTileFormat + "\n";
        if (oLayers != null && oLayers.size() > 0) {
            str += "Layers: \n";
            for (String layer : oLayers) {
                str += "\t" + layer + "\n";
            }
        }
        return str;
    }
}
