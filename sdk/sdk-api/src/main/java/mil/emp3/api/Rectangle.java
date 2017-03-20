package mil.emp3.api;


import org.cmapi.primitives.GeoRectangle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRectangle;
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
 * This class implements the EMP rectangle feature. It accepts one (1) geographic coordinate that places the
 * center of the rectangle. The rectangle's width and height are centered at the coordinate.
 */
public class Rectangle extends Feature<IGeoRectangle> implements IGeoRectangle {

    /**
     * This constructor creates a default rectangle feature.
     */
    public Rectangle() {
        super(new GeoRectangle(), FeatureTypeEnum.GEO_RECTANGLE);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a default rectangle feature centered at the coordinate provided.
     * @param oCenter
     */
    public Rectangle(IGeoPosition oCenter) {
        super(new GeoRectangle(), FeatureTypeEnum.GEO_RECTANGLE);

        if (null == oCenter) {
            throw new InvalidParameterException("The coordiante can NOT be null.");
        }
        this.getRenderable().getPositions().add(oCenter);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a rectangle feature centered at the coordinate provided with the width and height provided.
     * @param oCenter {@link IGeoPosition}
     * @param dWidth The width in meters.
     * @param dHeight The height in meters.
     */
    public Rectangle(IGeoPosition oCenter, double dWidth, double dHeight) {
        super(new GeoRectangle(), FeatureTypeEnum.GEO_RECTANGLE);

        if (null == oCenter) {
            throw new InvalidParameterException("The coordiante can NOT be null.");
        }

        this.getRenderable().getPositions().add(oCenter);
        this.setWidth(dWidth);
        this.setHeight(dHeight);
        this.setFillStyle(null);
    }

    /**
     * This constructor creates a rectangle feature from the geo rectangle provided.
     * @param oRenderable {@link IGeoRectangle}
     */
    public Rectangle(IGeoRectangle oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_RECTANGLE);
    }

    /**
     * This method overrides the default width of a rectangular feature.
     * @param fValue The width in meters.
     */
    @Override
    public void setWidth(double fValue) {
        if ((fValue == 0.0) || (Double.isNaN(fValue))) {
            throw new InvalidParameterException("Invalid width.");
        }
        this.getRenderable().setWidth(Math.abs(fValue));
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
     * @param fValue The width in meters.
     */
    @Override
    public void setHeight(double fValue) {
        if ((fValue == 0.0) || (Double.isNaN(fValue))) {
            throw new InvalidParameterException("Invalid height.");
        }
        this.getRenderable().setHeight(Math.abs(fValue));
    }

    /**
     * This method retrieves the height of the rectangle.
     * @return The height in meters or 0 if its not applicable.
     */
    @Override
    public double getHeight() {
        return this.getRenderable().getHeight();
    }

    public List<IGeoPosition> getCorners() {
        IGeoPosition pos;
        double halfHeightE2;
        double halfWidthE2;
        double distanceToCorner;
        double bearingToTopRightCorner;
        double bearing;
        double azimuth = this.getAzimuth();
        List<IGeoPosition> posList = new ArrayList<>();

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
