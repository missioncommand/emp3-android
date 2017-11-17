package mil.emp3.worldwind.utils;

import android.graphics.PointF;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoPosition;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.geom.Line;
import gov.nasa.worldwind.geom.Vec3;
import gov.nasa.worldwind.globe.Globe;
import mil.emp3.api.exceptions.EMP_Exception;

/**
 * This class provides conversion utility methods between worldwind values to/from EMP values.
 */
public class Conversion {
    /**
     * Converts a screen point to the geographic coordinates on the globe.
     *
     * @param wwd WorldWindow
     * @param ray Pre-allocated Line.
     * @param screenX X coordinate
     * @param pickPoint Pre-allocated Vec3
     * @param screenY Y coordinate
     * @param result  Pre-allocated Position receives the geographic coordinates
     *
     * @return true if the screen point could be converted; false if the screen point is not on the globe
     */
    public static boolean screenPointToGroundPosition(WorldWindow wwd, Line ray, Vec3 pickPoint, float screenX, float screenY, gov.nasa.worldwind.geom.Position result) {
        if (wwd.rayThroughScreenPoint(screenX, screenY, ray)) {
            Globe globe = wwd.getGlobe();
            if (globe.intersect(ray, pickPoint)) {
                globe.cartesianToGeographic(pickPoint.x, pickPoint.y, pickPoint.z, result);
                return true;
            }
        }
        return false;
    }

    /**
     * Converts a screen point to the geographic coordinates on the globe.
     *
     * @param wwd WorldWindow
     * @param latitude
     * @param longitude
     * @param result  Pre-allocated android Point receives the geographic coordinates
     *
     * @return true if the screen point could be converted; false if the screen point is not on the globe
     */
    public static boolean groundPositionToScreenPoint(WorldWindow wwd, double latitude, double longitude, android.graphics.Point result) {
        Globe globe = wwd.getGlobe();
        Vec3 result3 = new Vec3();
        globe.geographicToCartesian(latitude, longitude, 0.0, result3);
        PointF resultF = new PointF();
        boolean valid = wwd.cartesianToScreenPoint(result3.x, result3.y, result3.z, resultF);
        if (valid) {
            result.x = (int)resultF.x;
            result.y = (int)resultF.y;
            return true;
        }
        return false;
    }

    /**
     * This method converts a list of IGeoPosition to a list of worldwind Position.
     * @param geoPositionList A list of IGeoPosition.
     * @return java.util.List<gov.nasa.worldwind.geom.Position>
     * @throws EMP_Exception If the geoPositionList is null.
     */
    public static java.util.List<gov.nasa.worldwind.geom.Position> convertToWWPositionList(java.util.List<IGeoPosition> geoPositionList) throws EMP_Exception {
        java.util.List<gov.nasa.worldwind.geom.Position> wwPositionList = new java.util.ArrayList<>();

        Conversion.convertToWWPositionList(geoPositionList, wwPositionList);
        return wwPositionList;
    }

    /**
     * This method converts a list of IGeoPosition to a list of worldwind Position.
     * @param geoPositionList A list of IGeoPosition.
     * @param resultList A list of worldwind Position where the resulting list is placed.
     * @throws EMP_Exception If the geoPositionList or resultList is null.
     */
    public static void convertToWWPositionList(java.util.List<IGeoPosition> geoPositionList, java.util.List<gov.nasa.worldwind.geom.Position> resultList) throws EMP_Exception {
        if (geoPositionList == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "geoPositionList can not be null.");
        }

        if (resultList == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "resultList can not be null.");
        }

        resultList.clear();
        for(IGeoPosition geoPosition: geoPositionList) {
            resultList.add(gov.nasa.worldwind.geom.Position.fromDegrees(geoPosition.getLatitude(), geoPosition.getLongitude(), geoPosition.getAltitude()));
        }
    }

    @WorldWind.AltitudeMode
    public static int convertIGeoAaltitudeMode(IGeoAltitudeMode.AltitudeMode altitudeMode) {
        int iRet = WorldWind.CLAMP_TO_GROUND;

        if (null != altitudeMode) {
            switch (altitudeMode) {
                case CLAMP_TO_GROUND:
                    iRet = WorldWind.CLAMP_TO_GROUND;
                    break;
                case RELATIVE_TO_GROUND:
                    iRet = WorldWind.RELATIVE_TO_GROUND;
                    break;
                case ABSOLUTE:
                    iRet = WorldWind.ABSOLUTE;
                    break;
            }
        }
        return iRet;
    }

    public static void convertAndSetIGeoAltitudeMode(gov.nasa.worldwind.shape.AbstractShape shape, IGeoAltitudeMode.AltitudeMode altitudeMode) {
        if (null == altitudeMode) {
            shape.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        } else switch (altitudeMode) {
            case CLAMP_TO_GROUND:
                shape.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                break;
            case RELATIVE_TO_GROUND:
                shape.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                break;
            case ABSOLUTE:
                shape.setAltitudeMode(WorldWind.ABSOLUTE);
                break;
        }
    }

    public static void convertAndSetIGeoAltitudeMode(gov.nasa.worldwind.shape.Label shape, IGeoAltitudeMode.AltitudeMode altitudeMode) {
        if (null == altitudeMode) {
            shape.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
        } else switch (altitudeMode) {
            case CLAMP_TO_GROUND:
                shape.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
                break;
            case RELATIVE_TO_GROUND:
                shape.setAltitudeMode(WorldWind.RELATIVE_TO_GROUND);
                break;
            case ABSOLUTE:
                shape.setAltitudeMode(WorldWind.ABSOLUTE);
                break;
        }
    }

    public static gov.nasa.worldwind.render.Color convertColor(IGeoColor geoColor) {
        gov.nasa.worldwind.render.Color wwColor = new gov.nasa.worldwind.render.Color();

        Conversion.convertColor(geoColor, wwColor);
        return wwColor;
    }

    public static void convertColor(IGeoColor fromColor, gov.nasa.worldwind.render.Color toColor) {
        if ((fromColor == null) || (toColor == null)) {
            return;
        }

        toColor.set(fromColor.getRed()/255F, fromColor.getGreen()/255F, fromColor.getBlue()/255F, (float) fromColor.getAlpha());
    }
}
