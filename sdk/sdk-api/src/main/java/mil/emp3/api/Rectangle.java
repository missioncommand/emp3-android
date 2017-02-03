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
