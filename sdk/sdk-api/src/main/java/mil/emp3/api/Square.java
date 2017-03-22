package mil.emp3.api;


import org.cmapi.primitives.GeoSquare;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoSquare;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.abstracts.Feature;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.utils.kml.EmpKMLExporter;

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
            throw new InvalidParameterException("The coordinate can NOT be null and must have valid values");
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
            throw new InvalidParameterException("The coordinate can NOT be null and must have valid values.");
        }

        dWidth = makePositive(dWidth, "Invalid Width. NaN");
        if(dWidth < MINIMUM_WIDTH) {
            throw new InvalidParameterException("Invalid width. " + dWidth + " Minimum supported " + MINIMUM_WIDTH);
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
            throw new InvalidParameterException("Encapsulated Square must be non-null");
        }
        this.setWidth(makePositive(this.getWidth(), "Invalid Width. NaN"));

        if(this.getWidth() < MINIMUM_WIDTH) {
            throw new InvalidParameterException("Invalid width. " + this.getWidth() + " Minimum supported " + MINIMUM_WIDTH);
        }
        this.setAzimuth(this.getAzimuth());  // For validation.
    }

    /**
     * This method overrides the default width of a square feature. If width is invalid an exception is thrown.
     * @param fValue The width in meters.
     */
    @Override
    public void setWidth(double fValue) {
        fValue = makePositive(fValue, "Invalid width. NaN");
        if(this.getWidth() < MINIMUM_WIDTH) {
            throw new InvalidParameterException("Invalid width. " + fValue + " Minimum supported " + MINIMUM_WIDTH);
        }
        this.getRenderable().setWidth(fValue);
    }

    /**
     * This method retrieves the width of the rectangle.
     * @return The width in meters.
     */
    @Override
    public double getWidth() {
        return this.getRenderable().getWidth();
    }

    public List<IGeoPosition> getCorners() {
        IGeoPosition pos;
        double halfWidthE2;
        double distanceToCorner;
        double bearingToTopRightCorner;
        double bearing;
        double azimuth = this.getAzimuth();
        List<IGeoPosition> posList = new ArrayList<>();

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
        pos = GeoLibrary.computePositionAt(bearing, distanceToCorner, this.getPosition());
        posList.add(pos);

        // Calculate the bottom right position.
        bearing = (bearingToTopRightCorner * -1.0) + 180.0;
        if (azimuth != 0.0) {
            bearing = ((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        }

        pos = GeoLibrary.computePositionAt(bearing, distanceToCorner, this.getPosition());
        posList.add(pos);

        // Calculate the bottom left position.
        bearing = bearingToTopRightCorner - 180.0;
        if (azimuth != 0.0) {
            bearing = ((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        }

        pos = GeoLibrary.computePositionAt(bearing, distanceToCorner, this.getPosition());
        posList.add(pos);

        // Calculate the top left position.
        bearing = bearingToTopRightCorner * -1.0;
        if (azimuth != 0.0) {
            bearing = ((((bearing + azimuth + 180.0) % 360.0) + 360.0) % 360.0) - 180.0;
        }

        pos = GeoLibrary.computePositionAt(bearing, distanceToCorner, this.getPosition());
        posList.add(pos);

        return posList;
    }
}
