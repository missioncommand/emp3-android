package mil.emp3.api;


import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.IGeoPolygon;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import mil.emp3.api.abstracts.Feature;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.utils.kml.EmpKMLExporter;

/**
 * This class implements an polygon. It requires 3 or more geographic coordinates.
 */
public class Polygon extends Feature<IGeoPolygon> implements IGeoPolygon {

    /**
     * This constructor creates a default geo polygon.
     */
    public Polygon() {
        super(new GeoPolygon(), FeatureTypeEnum.GEO_POLYGON);
    }

    /**
     * This constructor creates a default geo polygon with the coordinate list provided.
     * @param oPositionList
     */
    public Polygon(List<IGeoPosition> oPositionList) {
        super(new GeoPolygon(), FeatureTypeEnum.GEO_POLYGON);
        if (null == oPositionList) {
            throw new InvalidParameterException("The position list parameter can NOT be null");
        }
        this.getRenderable().getPositions().addAll(oPositionList);
    }

    /**
     * This constructor creates a polygon feature from the geo polygon provided.
     * @param oRenderable
     */
    public Polygon(IGeoPolygon oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_POLYGON);
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
                if  (Polygon.this.needStyle()){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + EmpKMLExporter.getStyleId(Polygon.this));
                    xmlSerializer.endTag(null, "styleUrl");
                }
                List<IGeoPosition> posList = new ArrayList<>();

                // KML needs the first and last position to be equal.
                posList.addAll(Polygon.this.getPositions());
                posList.add(Polygon.this.getPositions().get(0));

                xmlSerializer.startTag(null, "Polygon");
                EmpKMLExporter.serializeExtrude(Polygon.this, xmlSerializer);
                EmpKMLExporter.serializeAltitudeMode(Polygon.this, xmlSerializer);
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
