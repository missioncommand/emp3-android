package mil.emp3.api;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.abstracts.MapService;
import mil.emp3.api.enums.WMTSVersionEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IWMTS;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;

/**
 * This feature is not ready for use.  It is waiting on NASA WorldWind to complete the
 * implementation of WMTS.
 */
public class WMTS extends MapService implements IWMTS {
    final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();

    private WMTSVersionEnum eVersion = null;
    private String sTileFormat = "image/png";
    private List<String> oLayers;
    private List<String> oStyles = null;
    private List<String> oDimensions = null;

    /**
     * This constructor creates a WMTS
     * @param sURL This parameter set the url for the service.
     * @param eWMTSVersion This parameter set the WMTS version for the service.
     * @param sTileFormat This parameter sets the format of the tiles requested.
     * @param oLayers This is a list of layers that must be displayed.
     * @throws java.net.MalformedURLException If the url string is invalid.
     */
    public WMTS(String sURL,
               WMTSVersionEnum eWMTSVersion,
               String sTileFormat,
               List<String> oLayers)
            throws java.net.MalformedURLException {
        super(sURL);

        if (oLayers != null) {
            this.oLayers = oLayers;
        } else {
            this.oLayers = new ArrayList<>();
        }
        if ((sTileFormat != null) && !sTileFormat.isEmpty()) {
            this.sTileFormat = sTileFormat;
        }
        if (eWMTSVersion == null) {
            this.eVersion = WMTSVersionEnum.VERSION_1_0_0;
        } else {
            this.eVersion = eWMTSVersion;
        }
    }

    /**
     * This method retrieves the wmts version.
     * @return WMTSVersionEnum
     */
    public WMTSVersionEnum getWMTSVersion() {
        return this.eVersion;
    }

    /**
     * This method retrieves the title format.
     * @return String
     */
    public String getTileFormat() {
        return this.sTileFormat;
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
    public void setDimensions(List<String> dimensions) {
        if (dimensions != null)
            oDimensions = dimensions;
    }

    @Override
    public List<String> getDimensions() {
        return oDimensions;
    }

    /**
     * This method sets the layer list. The updated list is sent to all maps utilizing
     * this service.
     * @param newLayers A list of string. The list is ignored if its null or empty.
     * @throws EMP_Exception
     */

    @Override
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
    @Override
    public List<String> getLayers() {
        return this.oLayers;
    }
}
