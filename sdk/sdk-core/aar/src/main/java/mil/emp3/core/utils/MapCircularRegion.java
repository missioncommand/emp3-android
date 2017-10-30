package mil.emp3.core.utils;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;

/**
 * This class represent a region of a map based on a circle.
 */
public class MapCircularRegion {
    private static double dMaxRadius = 2.0 * Math.PI * 0.166667 * mil.emp3.api.utils.GeoLibrary.DEFAULT_EARTH_RADIUS;
    private IGeoPosition oCenterPosition = null;
    private double dRadius = 0;

    /**
     * This constructor creates a region centered at the specified with the specified radius.
     * @param oPos The center of the region.
     * @param dRegionRadius The ragius of the region.
     * @throws EMP_Exception
     */
    public MapCircularRegion(IGeoPosition oPos, double dRegionRadius) throws EMP_Exception {
        if (oPos == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "oPos can't be null.");
        }
        if (dRegionRadius == Double.NaN) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "dRegionRadius must be a number.");
        }

        this.dRadius = Math.abs(dRegionRadius);
        this.oCenterPosition = new GeoPosition();
        this.oCenterPosition.setLatitude(oPos.getLatitude());
        this.oCenterPosition.setLongitude(oPos.getLongitude());
        this.oCenterPosition.setAltitude(0.0);
    }

    /**
     * This constructor creates a region with the minimum radius that contains the coordinates on the list provided.
     * @param oPositionList A list of geographic coordinates.
     * @throws EMP_Exception
     */
    public MapCircularRegion(List<IGeoPosition> oPositionList) throws EMP_Exception {
        this.addCoordinates(oPositionList);
    }

    /**
     * This method return the center of the region.
     * @return The center coordinates.
     */
    public IGeoPosition getCenter() {
        return this.oCenterPosition;
    }

    /**
     * This method return the radius of the region.
     * @return The radius in meters.
     */
    public double getRadius() {
        return this.dRadius;
    }

    /**
     * This method adds the specified coordinate to the region by modifying its center and radius.
     * The Region will not be allowed to encompass more the a 60.0 degree arc distance.
     * @param oPos The added position
     * @throws EMP_Exception
     */
    public void addCoordinate(IGeoPosition oPos) throws EMP_Exception {
        if (oPos == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "oPos can't be null.");
        }
        // Get the bearing between the points.
        double dBearing = mil.emp3.api.utils.GeographicLib.computeBearing(this.oCenterPosition, oPos);
        //Get the distance between the points.
        double dDist = mil.emp3.api.utils.GeographicLib.computeDistanceBetween(this.oCenterPosition, oPos);
        // Calculate the point on the circle that is 180 deg from oPos.
        IGeoPosition oOpositePointOnCircle = mil.emp3.api.utils.GeographicLib.computePositionAt(((dBearing + 180.0) % 360), this.dRadius, this.oCenterPosition);
        // Compute the new center in between the center and the new position.
        this.oCenterPosition = mil.emp3.api.utils.GeographicLib.computePositionAt(dBearing, (dDist / 2.0), this.oCenterPosition);
        // Compute new radius.
        double dNewRadius = mil.emp3.api.utils.GeographicLib.computeDistanceBetween(this.oCenterPosition, oOpositePointOnCircle);

        if (dNewRadius < MapCircularRegion.dMaxRadius) {
            this.dRadius = dNewRadius;
        }
    }

    /**
     * This method adds all the coordinates on the list provided.
     * @param oPositionList The list of coordinates.
     * @throws EMP_Exception
     */
    public void addCoordinates(List<IGeoPosition> oPositionList) throws EMP_Exception {
        if (oPositionList == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "oPositionList can't be null.");
        }
        if (oPositionList.size() == 0) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "oPositionList is empty.");
        }

        IGeoPosition oPos;

        java.util.Iterator<IGeoPosition> oIterator = oPositionList.iterator();
        if (this.oCenterPosition == null) {
            oPos = oIterator.next();
            this.oCenterPosition = new GeoPosition();
            this.oCenterPosition.setLatitude(oPos.getLatitude());
            this.oCenterPosition.setLongitude(oPos.getLongitude());
            this.oCenterPosition.setAltitude(0.0);
            this.dRadius = 0.0;
        }

        while (oIterator.hasNext()) {
            oPos = oIterator.next();
            this.addCoordinate(oPos);
        }
    }

    public IGeoPosition getNorthWest() {
        double dCenterToNorthWestDist = Math.sqrt(2.0 * Math.pow(this.dRadius, 2));
        return mil.emp3.api.utils.GeographicLib.computePositionAt(315.0, dCenterToNorthWestDist, this.oCenterPosition);
    }

    public IGeoPosition getNorthEast() {
        double dCenterToNorthEastDist = Math.sqrt(2.0 * Math.pow(this.dRadius, 2));
        return mil.emp3.api.utils.GeographicLib.computePositionAt(45.0, dCenterToNorthEastDist, this.oCenterPosition);
    }

    public IGeoPosition getSouthEast() {
        double dCenterToSouthEastDist = Math.sqrt(2.0 * Math.pow(this.dRadius, 2));
        return mil.emp3.api.utils.GeographicLib.computePositionAt(135.0, dCenterToSouthEastDist, this.oCenterPosition);
    }

    public IGeoPosition getSouthWest() {
        double dCenterToSouthWestDist = Math.sqrt(2.0 * Math.pow(this.dRadius, 2));
        return mil.emp3.api.utils.GeographicLib.computePositionAt(225.0, dCenterToSouthWestDist, this.oCenterPosition);
    }

    public double getAltitudeOfConeVertexWithFOV(double dFOV) {
        double dArcRadians = Math.toRadians(mil.emp3.api.utils.GeoLibrary.getDegreesFromArcLength(this.dRadius));
        double dEarthY = mil.emp3.api.utils.GeoLibrary.DEFAULT_EARTH_RADIUS * Math.sin(dArcRadians);
        double dEarthX = mil.emp3.api.utils.GeoLibrary.DEFAULT_EARTH_RADIUS * Math.cos(dArcRadians);
        double dConeHeight = dEarthY / Math.tan(Math.toRadians(dFOV / 2.0));
        double dDiff = mil.emp3.api.utils.GeoLibrary.DEFAULT_EARTH_RADIUS - dEarthX;
        return dConeHeight - dDiff;
    }

    public boolean isInRegion(IGeoPosition oPos) {
        double dArcDist = mil.emp3.api.utils.GeographicLib.computeDistanceBetween(this.oCenterPosition, oPos);

        return (dArcDist <= this.dRadius);
    }
}
