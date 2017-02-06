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
                double halfWidth = Square.this.getWidth() / 2;
                double azimuth = Square.this.getAzimuth();
                double distanceFromCenter = Math.sqrt((halfWidth * halfWidth) + (halfWidth * halfWidth));
                double bearingFromCenter = 45.0;
                List<IGeoPosition> posList = new ArrayList<>();
                IGeoPosition center = Square.this.getPosition();
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

                if  (Square.this.needStyle()){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + EmpKMLExporter.getStyleId(Square.this));
                    xmlSerializer.endTag(null, "styleUrl");
                }

                xmlSerializer.startTag(null, "Polygon");
                EmpKMLExporter.serializeExtrude(Square.this, xmlSerializer);
                EmpKMLExporter.serializeAltitudeMode(Square.this, xmlSerializer);
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
