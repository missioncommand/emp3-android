package mil.emp3.core.utils;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;

/**
 * This class implements utility functions for features.
 */
public class FeatureUtils {
    public static IGeoPosition getFeaturePosition(IFeature oFeature) {
        if (oFeature == null) {
            return null;
        }

        IGeoPosition oPos = null;
        int iCnt = oFeature.getPositions().size();

        if (iCnt == 1) {
            oPos = oFeature.getPositions().get(0);
        } else {
            try {
                MapCircularRegion oRegion = new MapCircularRegion(oFeature.getPositions());
                oPos = oRegion.getCenter();
            } catch (EMP_Exception Ex) {
                Ex.printStackTrace();
            }
        }

        return oPos;
    }

    public static boolean isOnIDL(java.util.List<IGeoPosition> oPositionList) {
        boolean bCrossIDL = false;

        if ((oPositionList == null) || (oPositionList.size() == 0) || (oPositionList.size() == 1)) {
            return bCrossIDL;
        }

        try {
            MapCircularRegion oRegion = new MapCircularRegion(oPositionList);
            IGeoPosition oPosIDLAtLatitude = new GeoPosition();
            oPosIDLAtLatitude.setLatitude(oRegion.getCenter().getLatitude());
            oPosIDLAtLatitude.setLongitude(180.0);
            bCrossIDL = oRegion.isInRegion(oPosIDLAtLatitude);
        } catch (EMP_Exception e) {
            e.printStackTrace();
        }
        return bCrossIDL;
    }
}
