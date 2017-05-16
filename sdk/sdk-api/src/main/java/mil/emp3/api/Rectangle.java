package mil.emp3.api;


import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoRectangle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRectangle;

import java.util.List;

import mil.emp3.api.abstracts.Feature;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeoLibrary;

/**
 * This class implements the EMP rectangle feature. It accepts one (1) geographic coordinate that places the
 * center of the rectangle. The rectangle's width and height are centered at the coordinate.
 */
public class Rectangle extends Feature<IGeoRectangle> implements IGeoRectangle {

    public final static double MINIMUM_WIDTH = global.MINIMUM_DISTANCE;
    public final static double MINIMUM_HEIGHT = global.MINIMUM_DISTANCE;

    /**
     * This constructor creates a default rectangle feature.
     */
    public Rectangle() {
        super(new GeoRectangle(), FeatureTypeEnum.GEO_RECTANGLE);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a default rectangle feature centered at the coordinate provided. If specified coordinate is null
     * or has invalid members then an exception is thrown.
     * @param oCenter
     */
    public Rectangle(IGeoPosition oCenter) {
        super(new GeoRectangle(), FeatureTypeEnum.GEO_RECTANGLE);

        if ((null == oCenter) || (!EmpGeoPosition.validate(oCenter))) {
            throw new IllegalArgumentException("The coordinate can NOT be null and must have valid values");
        }
        this.getRenderable().getPositions().add(oCenter);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a rectangle feature centered at the coordinate provided with the width and height provided.
     * If coordinates for the center, width or height is invalid an exception is thrown. The width and height must be greater
     * than 1 meter.
     * @param oCenter {@link IGeoPosition}
     * @param dWidth The width in meters.
     * @param dHeight The height in meters.
     */
    public Rectangle(IGeoPosition oCenter, double dWidth, double dHeight) {
        super(new GeoRectangle(), FeatureTypeEnum.GEO_RECTANGLE);

        if (null == oCenter || !EmpGeoPosition.validate(oCenter)) {
            throw new IllegalArgumentException("The coordinate can NOT be null and must have valid values");
        }
        dWidth = makePositive(dWidth, "Width is invalid. NaN");
        dHeight = makePositive(dHeight, "Height is invalid. NaN");

        if(dWidth < MINIMUM_WIDTH || dHeight < MINIMUM_HEIGHT) {
            throw new IllegalArgumentException("Invalid height or width. " + dHeight + " " +  dWidth + " Minimum supported " +
                MINIMUM_HEIGHT + " " + MINIMUM_WIDTH);
        }

        this.getRenderable().getPositions().add(oCenter);
        this.setWidth(dWidth);
        this.setHeight(dHeight);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a rectangle feature from the geo rectangle provided. If provided geo rectangle is null or has
     * invalid members then an exception is thrown.
     * @param oRenderable {@link IGeoRectangle}
     */
    public Rectangle(IGeoRectangle oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_RECTANGLE);

        if(null == oRenderable) {
            throw new IllegalArgumentException("Encapsulated Rectangle must be non-null");
        }
        this.setWidth(makePositive(this.getWidth(), "Invalid Width. NaN"));
        this.setHeight(makePositive(this.getHeight(), "Invalid Height. NaN"));

        if(this.getWidth() < MINIMUM_WIDTH || this.getHeight() < MINIMUM_HEIGHT) {
            throw new IllegalArgumentException("Invalid height or width. " + this.getHeight() + " " +  this.getWidth() + " Minimum supported " +
                    MINIMUM_HEIGHT + " " + MINIMUM_WIDTH);
        }

        this.setAzimuth(this.getAzimuth());  // For validation.
    }

    /**
     * This method overrides the default width of a rectangular feature. If width is invalid then an exception is thrown. Width
     * must be greater than 1.0 meter.
     * @param dWidth The width in meters.
     */
    @Override
    public void setWidth(double dWidth) {
        dWidth = makePositive(dWidth, "Invalid Width. NaN");
        if (dWidth < MINIMUM_WIDTH) {
            throw new IllegalArgumentException("Invalid width.");
        }
        this.getRenderable().setWidth(Math.abs(dWidth));
    }

    /**
     * This method retrieves the width of the rectangle.
     * @return The width in meters.
     */
    @Override
    public double getWidth() {
        return this.getRenderable().getWidth();
    }

    /**
     * This method overrides the default height of a rectangular feature.
     * @param dHeight The height in meters must be greater than 1 meter.
     */
    @Override
    public void setHeight(double dHeight) {
        dHeight = makePositive(dHeight, "Invalid Height. NaN");
        if (dHeight < MINIMUM_HEIGHT) {
            throw new IllegalArgumentException("Invalid height.");
        }
        this.getRenderable().setHeight(Math.abs(dHeight));
    }

    /**
     * This method retrieves the height of the rectangle.
     * @return The height in meters or 0 if its not applicable.
     */
    @Override
    public double getHeight() {
        return this.getRenderable().getHeight();
    }

    public IEmpBoundingBox getFeatureBoundingBox() {
        double halfHeightE2;
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

        halfHeightE2 = this.getHeight() / 2.0;
        halfWidthE2 = this.getWidth() / 2.0;

        bearingToTopRightCorner = Math.toDegrees(Math.atan2(halfWidthE2, halfHeightE2));
        if (azimuth != 0.0) {
            bearing = ((((bearingToTopRightCorner + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        } else {
            bearing = bearingToTopRightCorner;
        }

        halfHeightE2 = halfHeightE2 * halfHeightE2;
        halfWidthE2 = halfWidthE2 * halfWidthE2;

        distanceToCorner = Math.sqrt(halfHeightE2 + halfWidthE2);

        // Calculate the top right position.
        GeoLibrary.computePositionAt(bearing, distanceToCorner, this.getPosition(), pos);
        bBox.includePosition(pos.getLatitude(), pos.getLongitude());

        // Calculate the bottom right position.
        bearing = (bearingToTopRightCorner * -1.0) + 180.0;
        if (azimuth != 0.0) {
            bearing = ((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        }

        GeoLibrary.computePositionAt(bearing, distanceToCorner, this.getPosition(), pos);
        bBox.includePosition(pos.getLatitude(), pos.getLongitude());

        // Calculate the bottom left position.
        bearing = bearingToTopRightCorner - 180.0;
        if (azimuth != 0.0) {
            bearing = ((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        }

        GeoLibrary.computePositionAt(bearing, distanceToCorner, this.getPosition(), pos);
        bBox.includePosition(pos.getLatitude(), pos.getLongitude());

        // Calculate the top left position.
        bearing = bearingToTopRightCorner * -1.0;
        if (azimuth != 0.0) {
            bearing = ((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        }

        GeoLibrary.computePositionAt(bearing, distanceToCorner, this.getPosition(), pos);
        bBox.includePosition(pos.getLatitude(), pos.getLongitude());

        return bBox;
    }

    /**
     *
     * @return String gives rectangle parameters
     */

    @Override
    public String toString() {
        if (getPosition() != null) {
            return "Rectangle at\n" +
                    "\tlatitude: " + getPosition().getLatitude() + "\n" +
                    "\tlongitude: " + getPosition().getLongitude() + "\n" +
                    "\taltitude: " + getPosition().getAltitude() + "\n" +
                    "\theight: " + getHeight() + "\n" +
                    "\twidth: " + getWidth() + "\n" +
                    "\tazimuth: " + getAzimuth() + "\n";
        } else {
            return "Rectangle\n" +
                    "\theight: " + getHeight() + "\n" +
                    "\twidth: " + getWidth() + "\n" +
                    "\tazimuth: " + getAzimuth() + "\n";
        }
    }
}
