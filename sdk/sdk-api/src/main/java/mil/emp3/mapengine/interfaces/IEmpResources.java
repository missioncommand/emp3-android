package mil.emp3.mapengine.interfaces;

import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

/**
 * An object that holds EMP Core resources.
 */
public interface IEmpResources {

    /**
     * If Mil Std Renderer is unable to render a symbol based on application supplied data then a default icon is displayed.
     * @return default icon for mil std symbol.
     */
    IEmpImageInfo getDefaultIconImageInfo();

    /**
     * Gets the CMAPI icon style, size, offsets x and y in pixels
     * @return icon style
     */
    IGeoIconStyle getDefaultIconStyle();

    /**
     * Gets icon image information based on android resource identifier.
     * @param resId
     * @return
     */
    IEmpImageInfo getAndroidResourceIconImageInfo(int resId);

    /**
     * Gets the stroke style that will be used to highlight the features that were selected using the 'select'
     * API supported by the Map.
     * @param mapInstance
     * @return stroke style
     */
    IGeoStrokeStyle getSelectedStrokeStyle(IMapInstance mapInstance);

    /**
     * Gets the label style that will be used to highlight the features that were selected using the 'select'
     * API supported by the Map.
     * @param mapInstance
     * @return label style
     */
    IGeoLabelStyle getSelectedLabelStyle(IMapInstance mapInstance);

    /**
     * Gets the scale that will be used to render the icons for the features that were selected using the 'select'
     * API supported by the Map.
     * @param mapInstance
     * @return double
     */
    double getSelectedIconScale(IMapInstance mapInstance);

    /**
     * Gets fill style for the buffer surrounding a feature.
     * @param mapInstance
     * @return
     */
    IGeoFillStyle getBufferFillStyle(IMapInstance mapInstance);
}
