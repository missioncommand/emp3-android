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
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.utils.kml.EmpKMLExporter;

/**
 * This class implements the EMP square feature. It accepts one (1) geographic coordinate that places the
 * center of the square. The square's length of all sides is set with the setWidth method. And the square
 * is centered at the coordinate.
 */
public class Square extends Feature<IGeoSquare> implements IGeoSquare {

    /**
     * This constructor creates a default square feature.
     */
    public Square() {
        super(new GeoSquare(), FeatureTypeEnum.GEO_SQUARE);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a default square feature centered at the coordinate provided.
     * @param oCenter
     */
    public Square(IGeoPosition oCenter) {
        super(new GeoSquare(), FeatureTypeEnum.GEO_SQUARE);
        if (null == oCenter) {
            throw new InvalidParameterException("The coordiante can NOT be null.");
        }
        this.getRenderable().getPositions().add(oCenter);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a square feature centered at the coordinate provided with the width provided.
     * @param oCenter {@link IGeoPosition}
     * @param dWidth The width in meters.
     */
    public Square(IGeoPosition oCenter, double dWidth) {
        super(new GeoSquare(), FeatureTypeEnum.GEO_SQUARE);
        if (null == oCenter) {
            throw new InvalidParameterException("The center coordiante can NOT be null.");
        }

        this.getRenderable().getPositions().add(oCenter);
        this.setWidth(dWidth);
        this.setFillStyle(null);
    }


    /**
     * This constructor creates a square feature from the geo square provided.
     * @param oRenderable {@link IGeoSquare}
     */
    public Square(IGeoSquare oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_SQUARE);
    }

    /**
     * This method overrides the default width of a square feature.
     * @param fValue The width in meters.
     */
    @Override
    public void setWidth(double fValue) {
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
