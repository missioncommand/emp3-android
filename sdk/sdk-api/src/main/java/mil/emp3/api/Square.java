package mil.emp3.api;


import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoSquare;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoSquare;

import java.util.List;

import mil.emp3.api.abstracts.Feature;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeographicLib;

/**
 * This class implements the EMP square feature. It accepts one (1) geographic coordinate that places the
 * center of the square. The square's length of all sides is set with the setWidth method. And the square
 * is centered at the coordinate.
 */
public class Square extends Feature<IGeoSquare> implements IGeoSquare {
    public final static double MINIMUM_WIDTH = global.MINIMUM_DISTANCE;
    /**
     * This constructor creates a default square feature.
     */
    public Square() {
        super(new GeoSquare(), FeatureTypeEnum.GEO_SQUARE);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a default square feature centered at the coordinate provided.
     * If specified coordinates are null or invalid an exception is thrown.
     * @param oCenter
     */
    public Square(IGeoPosition oCenter) {
        super(new GeoSquare(), FeatureTypeEnum.GEO_SQUARE);
        if (null == oCenter || !EmpGeoPosition.validate(oCenter)) {
            throw new IllegalArgumentException("The coordinate can NOT be null and must have valid values");
        }
        this.getRenderable().getPositions().add(oCenter);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a square feature centered at the coordinate provided with the width provided.
     * If specified coordinates are null or invalid an exception is thrown. If width is invalid an exception is thrown.
     * minimum value for width is 1.0 meters.
     * @param oCenter {@link IGeoPosition}
     * @param dWidth The width in meters.
     */
    public Square(IGeoPosition oCenter, double dWidth) {
        super(new GeoSquare(), FeatureTypeEnum.GEO_SQUARE);
        if (null == oCenter || !EmpGeoPosition.validate(oCenter)) {
            throw new IllegalArgumentException("The coordinate can NOT be null and must have valid values.");
        }

        dWidth = makePositive(dWidth, "Invalid Width. NaN");
        if(dWidth < MINIMUM_WIDTH) {
            throw new IllegalArgumentException("Invalid width. " + dWidth + " Minimum supported " + MINIMUM_WIDTH);
        }
        this.getRenderable().getPositions().add(oCenter);
        this.setWidth(dWidth);
        this.setFillStyle(null);
    }


    /**
     * This constructor creates a square feature from the geo square provided. If provided geo square is null or has
     * invalid members then an exception is thrown.
     * @param oRenderable {@link IGeoSquare}
     */
    public Square(IGeoSquare oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_SQUARE);
        if(null == oRenderable) {
            throw new IllegalArgumentException("Encapsulated Square must be non-null");
        }
        this.setWidth(this.getWidth());
        this.setAzimuth(this.getAzimuth());  // For validation.
    }

    /**
     * This method overrides the default width of a square feature. If width is invalid an exception is thrown.
     * @param fValue The width in meters.
     */
    @Override
    public void setWidth(final double fValue) {
        validateWidth(fValue);
        this.getRenderable().setWidth(fValue);
    }

    /**
     * Validates whether or not a given input is a valid Width.
     * Throws an exception if invalid in order to inform user what the issue was.
     * @param dValue The width to be checked
     */
    private void validateWidth(final double dValue) {
        validatePositive(dValue);
        if(dValue < MINIMUM_WIDTH) {
            throw new IllegalArgumentException("Invalid width, " + String.valueOf(dValue) + " is not greater than " + MINIMUM_WIDTH);
        }
    }

    /**
     * This method retrieves the width of the rectangle.
     * @return The width in meters.
     */
    @Override
    public double getWidth() {
        return this.getRenderable().getWidth();
    }

    public IEmpBoundingBox getFeatureBoundingBox() {
        double halfWidthE2;
        double distanceToCorner;
        double bearingToTopRightCorner;
        double bearing;
        double azimuth = this.getAzimuth();
        List<IGeoPosition> posList = this.getPositions();

        if ((null == posList) || posList.isEmpty()) {
            return null;
        }

        IEmpBoundingBox bBox = new EmpBoundingBox();
        IGeoPosition pos = new GeoPosition();
        halfWidthE2 = this.getWidth() / 2.0;

        bearingToTopRightCorner = 45.0;
        if (azimuth != 0.0) {
            bearing = ((((bearingToTopRightCorner + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        } else {
            bearing = bearingToTopRightCorner;
        }

        halfWidthE2 = halfWidthE2 * halfWidthE2;

        distanceToCorner = Math.sqrt(2.0 * halfWidthE2);

        // Calculate the top right position.
        GeographicLib.computePositionAt(bearing, distanceToCorner, this.getPosition(), pos);
        bBox.includePosition(pos.getLatitude(), pos.getLongitude());

        // Calculate the bottom right position.
        bearing = (bearingToTopRightCorner * -1.0) + 180.0;
        if (azimuth != 0.0) {
            bearing = ((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        }

        GeographicLib.computePositionAt(bearing, distanceToCorner, this.getPosition(), pos);
        bBox.includePosition(pos.getLatitude(), pos.getLongitude());

        // Calculate the bottom left position.
        bearing = bearingToTopRightCorner - 180.0;
        if (azimuth != 0.0) {
            bearing = ((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        }

        GeographicLib.computePositionAt(bearing, distanceToCorner, this.getPosition(), pos);
        bBox.includePosition(pos.getLatitude(), pos.getLongitude());

        // Calculate the top left position.
        bearing = bearingToTopRightCorner * -1.0;
        if (azimuth != 0.0) {
            bearing = ((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        }

        GeographicLib.computePositionAt(bearing, distanceToCorner, this.getPosition(), pos);
        bBox.includePosition(pos.getLatitude(), pos.getLongitude());

        return bBox;
    }
    /**
     *
     * @return String gives square parameters
     */

    @Override
    public String toString() {
        if (getPosition() != null) {
            return "Square at\n" +
                    "\tlatitude: " + getPosition().getLatitude() + "\n" +
                    "\tlongitude: " + getPosition().getLongitude() + "\n" +
                    "\taltitude: " + getPosition().getAltitude() + "\n" +
                    "\twidth: " + getWidth() + "\n" +
                    "\tazimuth: " + getAzimuth() + "\n";
        } else {
            return "Square\n" +
                    "\twidth: " + getWidth() + "\n" +
                    "\tazimuth: " + getAzimuth() + "\n";
        }
    }
}
