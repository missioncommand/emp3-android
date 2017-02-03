package mil.emp3.mapengine.interfaces;

import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoStrokeStyle;

/**
 * This class defines the interface to EMP core resources.
 */
public interface IEmpResources {
    IEmpImageInfo getDefaultIconImageInfo();
    IGeoIconStyle getDefaultIconStyle();
    IEmpImageInfo getAndroidResourceIconImageInfo(int resId);
    IGeoStrokeStyle getSelectedStrokeStyle(IMapInstance mapInstance);
    IGeoLabelStyle getSelectedLabelStyle(IMapInstance mapInstance);
    double getSelectedIconScale(IMapInstance mapInstance);
}
