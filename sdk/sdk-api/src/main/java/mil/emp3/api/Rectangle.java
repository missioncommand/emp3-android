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
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.utils.kml.EmpKMLExporter;

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
            throw new InvalidParameterException("The coordinate can NOT be null and must have valid values");
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
            throw new InvalidParameterException("The coordinate can NOT be null and must have valid values");
        }
        dWidth = makePositive(dWidth, "Width is invalid. NaN");
        dHeight = makePositive(dHeight, "Height is invalid. NaN");

        if(dWidth < MINIMUM_WIDTH || dHeight < MINIMUM_HEIGHT) {
            throw new InvalidParameterException("Invalid height or width. " + dHeight + " " +  dWidth + " Minimum supported " +
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
            throw new InvalidParameterException("Encapsulated Rectangle must be non-null");
        }
        this.setWidth(makePositive(this.getWidth(), "Invalid Width. NaN"));
        this.setHeight(makePositive(this.getHeight(), "Invalid Height. NaN"));

        if(this.getWidth() < MINIMUM_WIDTH || this.getHeight() < MINIMUM_HEIGHT) {
            throw new InvalidParameterException("Invalid height or width. " + this.getHeight() + " " +  this.getWidth() + " Minimum supported " +
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
            throw new InvalidParameterException("Invalid width.");
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
            throw new InvalidParameterException("Invalid height.");
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

    @Override
    public String exportToKML() throws IOException {
        return callKMLExporter();
    }

    private boolean needStyle() {
        return (null != this.getStrokeStyle()) || (null != this.getFillStyle());
    }

    @Override
    public void exportStylesToKML(XmlSerializer xmlSerializer) throws IOException {
        if (this.needStyle()) {
            IGeoStrokeStyle strokeStyle = this.getStrokeStyle();

            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", EmpKMLExporter.getStyleId(this));

            if (null != this.getStrokeStyle()) {
                EmpKMLExporter.serializeStrokeStyle(this.getStrokeStyle(), xmlSerializer);
            }

            if (null != this.getFillStyle()) {
                EmpKMLExporter.serializeFillStyle(this.getFillStyle(), (null != this.getStrokeStyle()), xmlSerializer);
            }
            xmlSerializer.endTag(null, "Style");
        }

        super.exportStylesToKML(xmlSerializer);
    }

    @Override
    public void exportEmpObjectToKML(XmlSerializer xmlSerializer) throws IOException {
        EmpKMLExporter.serializePlacemark(this, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                double halfHeight = Rectangle.this.getHeight() / 2;
                double halfWidth = Rectangle.this.getWidth() / 2;
                double azimuth = Rectangle.this.getAzimuth();
                double distanceFromCenter = Math.sqrt((halfHeight * halfHeight) + (halfWidth * halfWidth));
                double bearingFromCenter = Math.toDegrees(Math.atan2(halfWidth, halfHeight));
                List<IGeoPosition> posList = new ArrayList<>();
                IGeoPosition center = Rectangle.this.getPosition();
                IGeoPosition pos1, pos;

                // Generate the 4 corners of the rectangle.
                pos1 = GeoLibrary.computePositionAt(bearingFromCenter + azimuth, distanceFromCenter, center);
                posList.add(pos1);
                pos = GeoLibrary.computePositionAt(180.0 - bearingFromCenter + azimuth, distanceFromCenter, center);
                posList.add(pos);
                pos = GeoLibrary.computePositionAt(180.0 + bearingFromCenter + azimuth, distanceFromCenter, center);
                posList.add(pos);
                pos = GeoLibrary.computePositionAt(azimuth - bearingFromCenter, distanceFromCenter, center);
                posList.add(pos);
                // KML needs the first and last position to be equal.
                posList.add(pos1);

                if  (Rectangle.this.needStyle()){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + EmpKMLExporter.getStyleId(Rectangle.this));
                    xmlSerializer.endTag(null, "styleUrl");
                }

                xmlSerializer.startTag(null, "Polygon");
                EmpKMLExporter.serializeExtrude(Rectangle.this, xmlSerializer);
                EmpKMLExporter.serializeAltitudeMode(Rectangle.this, xmlSerializer);
                xmlSerializer.startTag(null, "outerBoundaryIs");
                xmlSerializer.startTag(null, "LinearRing");
                EmpKMLExporter.serializeCoordinates(posList, xmlSerializer);
                xmlSerializer.endTag(null, "LinearRing");
                xmlSerializer.endTag(null, "outerBoundaryIs");
                xmlSerializer.endTag(null, "Polygon");
            }
        });

        super.exportEmpObjectToKML(xmlSerializer);
    }
}
