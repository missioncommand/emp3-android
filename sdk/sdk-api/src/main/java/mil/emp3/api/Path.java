package mil.emp3.api;

import org.cmapi.primitives.IGeoPosition;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.kml.EmpKMLExporter;

import org.cmapi.primitives.GeoRenderable;
import org.cmapi.primitives.IGeoRenderable;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlSerializer;

/**
 * This class implements the Path (Multi segment line) feature. It accepts a list of coordinates
 * where each adjacent pair of coordinates identifies a line segment.
 */
public class Path extends Feature<IGeoRenderable> implements IFeature<IGeoRenderable> {
    /**
     * This constructor creates a default path feature.
     */
    public Path() {
        super(new GeoRenderable(), FeatureTypeEnum.GEO_PATH);
    }

    /**
     * This constructor creates a default path feature with the position list provided.
     * @param oPositionList
     */
    public Path(List<IGeoPosition> oPositionList) {
        super(new GeoRenderable(), FeatureTypeEnum.GEO_PATH);
        if (null == oPositionList) {
            throw new InvalidParameterException("The coordinate parameter can NOT be null");
        }
        this.getRenderable().getPositions().addAll(oPositionList);
    }

    /**
     * This constructor creates a path feature from the renderable provided.
     * @param oRenderable
     */
    public Path(IGeoRenderable oRenderable) {
        super(oRenderable, FeatureTypeEnum.GEO_PATH);
    }

    @Override
    public String exportToKML() throws IOException {
        return callKMLExporter();
    }

    private boolean needStyle() {
        return (null != this.getStrokeStyle());
    }

    @Override
    public void exportStylesToKML(XmlSerializer xmlSerializer) throws IOException {
        if (this.needStyle()) {
            IGeoStrokeStyle strokeStyle = this.getStrokeStyle();

            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", EmpKMLExporter.getStyleId(this));

            EmpKMLExporter.serializeStrokeStyle(this.getStrokeStyle(), xmlSerializer);
            xmlSerializer.endTag(null, "Style");
        }

        super.exportStylesToKML(xmlSerializer);
    }

    @Override
    public void exportEmpObjectToKML(XmlSerializer xmlSerializer) throws IOException {
        EmpKMLExporter.serializePlacemark(this, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (Path.this.needStyle()){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + EmpKMLExporter.getStyleId(Path.this));
                    xmlSerializer.endTag(null, "styleUrl");
                }
                xmlSerializer.startTag(null, "LineString");
                EmpKMLExporter.serializeExtrude(Path.this, xmlSerializer);
                EmpKMLExporter.serializeAltitudeMode(Path.this, xmlSerializer);
                EmpKMLExporter.serializeCoordinates(Path.this.getPositions(), xmlSerializer);
                xmlSerializer.endTag(null, "LineString");
            }
        });

        super.exportEmpObjectToKML(xmlSerializer);
    }

    @Override
    protected void appendGeoJSONProperties(StringBuffer buffer) {
        buffer.append("\"properties\": {");
        buffer.append("\"style\": {");
        buffer.append("\"lineStyle\": {");
        EmpGeoColor color = (EmpGeoColor)this.getStrokeStyle().getStrokeColor();
        buffer.append(color.toGeoJSON());
        buffer.append("}"); // lineStyle
        buffer.append("}"); // style
        appendGeoJSONTimes(buffer);
        buffer.append(",\"name\":");
        buffer.append("\"" + this.getName() + "\"");
        buffer.append(",\"id\":");
        buffer.append("\"" + this.getGeoId() + "\"");
        buffer.append(",\"description\":");
        buffer.append("\"" + this.getDescription() + "\"");
        buffer.append("}");// properties
    }

    @Override
    protected void appendGeoJSONGeometry(StringBuffer buffer) {
        buffer.append("\"geometry\":  {\"type\": \"LineString\",");
        buffer.append("\"coordinates\":  [");
        appendGeoJSONPositions(buffer);
        buffer.append("]");// end of coordinates
        buffer.append("}");// end of geometry
    }

    @Override
    public String toGeoJSON() {
        StringBuffer buffer = new StringBuffer("{\"type\":  \"Feature\",");
        appendGeoJSONGeometry(buffer);
        buffer.append(", ");
        appendGeoJSONProperties(buffer);
        buffer.append("}");
        return buffer.toString();
    }
}
