package mil.emp3.json.geoJson;

import android.util.Log;

import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoTimeSpan;

import java.text.SimpleDateFormat;
import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.GeoJSON;
import mil.emp3.api.KML;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpGeoColor;

import static mil.emp3.api.enums.FeatureTypeEnum.*;

public class GeoJsonExporter {

    private static final String TAG = GeoJsonExporter.class.getSimpleName();
    private static SimpleDateFormat zonedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");

    private void appendGeoJSONPositions(IFeature feature, StringBuffer buffer) {
        List<IGeoPosition> positions = null;
        if (feature.getFeatureType() == GEO_CIRCLE) {
            positions = ((Circle)feature).getPolygonPositionList();
        } else if (feature.getFeatureType() == GEO_ELLIPSE) {
            positions = ((Ellipse)feature).getPolygonPositionList();
        } else {
            positions = feature.getPositions();
        }
        int i = 0;
        while (true) {
            IGeoPosition position = positions.get(i++);
            buffer.append("[");
            buffer.append(position.getLatitude());
            buffer.append(", ");
            buffer.append(position.getLatitude());
            buffer.append("]");
            if (i == positions.size() - 1) break;
            buffer.append(", ");
        }
    }

    private void appendGeoJSONOtherProperties(IFeature feature, StringBuffer buffer) {
        boolean haveTimeSpan = false;
        if (feature.getTimeSpans().size() > 0) {
            haveTimeSpan = true;
            buffer.append(",\"timePrimitive\": {");
            buffer.append("\"timeSpan\": {");
            IGeoTimeSpan timeSpan = feature.getTimeSpans().get(0);
            buffer.append("\"begin\": {");
            buffer.append(zonedDate.format(timeSpan.getBegin()));
            buffer.append("}");
            buffer.append("\"end\": {");
            buffer.append(zonedDate.format(timeSpan.getEnd()));
            buffer.append("}");
            buffer.append("}");// time span
        }
        if (feature.getTimeStamp() != null) {
            if (!haveTimeSpan) {
                buffer.append(",\"timePrimitive\": {");
                haveTimeSpan = true;
            }
            buffer.append("\"timeStamp\": {");
            buffer.append(zonedDate.format(feature.getTimeStamp()));
            buffer.append("}");// time stamp
        }
        if (haveTimeSpan) {
            buffer.append("}");// time primitive
        }
        buffer.append(",\"name\":");
        buffer.append("\"" + feature.getName() + "\"");
        buffer.append(",\"id\":");
        buffer.append("\"" + feature.getGeoId() + "\"");
        buffer.append(",\"description\":");
        buffer.append("\"" + feature.getDescription() + "\"");
        buffer.append("}");
    }

    private void appendGeoJSONPolygon(IFeature feature, StringBuffer buffer) {
        buffer.append("\"geometry\":  {\"type\": \"Polygon\",");
        buffer.append("\"coordinates\":  [[");
        appendGeoJSONPositions(feature, buffer);
        buffer.append("]]");// end of coordinates
        buffer.append("}");// end of geometry
        buffer.append(", ");
        buffer.append("\"properties\": {");
        buffer.append("\"style\": {");
        buffer.append("\"lineStyle\": {");
        EmpGeoColor color = (EmpGeoColor) feature.getStrokeStyle().getStrokeColor();
        buffer.append(color.toGeoJSON());
        buffer.append("}"); // end of lineStyle
        buffer.append(",\"polyStyle\": {");
        color = (EmpGeoColor) feature.getStrokeStyle().getStrokeColor();
        buffer.append(color.toGeoJSON());
        buffer.append("}"); // end of polyStyle
        buffer.append("}"); // end of style
        appendGeoJSONOtherProperties(feature, buffer);
        buffer.append("}");
    }

    private void appendGeoJSONPath(IFeature feature, StringBuffer buffer) {
        buffer.append("\"geometry\":  {\"type\": \"LineString\",");
        buffer.append("\"coordinates\":  [");
        appendGeoJSONPositions(feature, buffer);
        buffer.append("]");// end of coordinates
        buffer.append("}");// end of geometry
        buffer.append(",\"properties\": {");
        buffer.append("\"style\": {");
        buffer.append("\"lineStyle\": {");
        EmpGeoColor color = (EmpGeoColor)feature.getStrokeStyle().getStrokeColor();
        buffer.append(color.toGeoJSON());
        buffer.append("}"); // lineStyle
        buffer.append("}"); // style
        appendGeoJSONOtherProperties(feature, buffer);
        buffer.append("}");
    }

    private void appendGeoJSONPoint(IFeature feature, StringBuffer buffer) {
        buffer.append("\"geometry\":  {\"type\": \"Point\",");
        buffer.append("\"coordinates\":  ");
        IGeoPosition position = feature.getPositions().get(0);
        buffer.append("[");
        buffer.append(position.getLatitude());
        buffer.append(", ");
        buffer.append(position.getLatitude());
        buffer.append("]");
        buffer.append("}");// end of geometry
        buffer.append(",\"properties\": {");
        buffer.append("\"style\": {");
        buffer.append("\"iconStyle\": {");
        buffer.append("\"url\": {");
        buffer.append("\""+ ((Point)feature).getIconURI() + "\"");
        buffer.append("}"); // url
        buffer.append("}"); // iconStyle
        buffer.append("}"); // style
        appendGeoJSONOtherProperties(feature, buffer);
        buffer.append("}");
    }

    /**
     * Converts this object to a geoJSON string
     * Recursive method goes down through child features till
     * it finds a node
     */

    public void appendFeature(IFeature feature, StringBuffer buffer) {

        buffer.append("{\"type\":  \"Feature\",");
        switch (feature.getFeatureType()) {
            case GEO_POLYGON:
            case GEO_RECTANGLE:
            case GEO_SQUARE:
            case GEO_CIRCLE:
            case GEO_ELLIPSE:
                appendGeoJSONPolygon(feature, buffer);
                break;
            case GEO_PATH:
                appendGeoJSONPath(feature, buffer);
                break;
            case GEO_POINT:
                appendGeoJSONPoint(feature, buffer);
                break;
            case GEOJSON:
            case KML:
                Log.i(TAG, "Child feature can't be GEOJSON type");
                break;
            case GEO_ACM:
            case GEO_TEXT:
            case GEO_MIL_SYMBOL:
            default:
                buffer.append("}");
        }
    }

    private void appendFeatureList(List<IFeature> featureList, StringBuffer buffer) {
        buffer.append("{\"type\":  \"FeatureCollection\",");
        buffer.append("\"Features\":[{");
        for (IFeature feature : featureList) {
            appendFeature(feature, buffer);
        }
        buffer.append("}]");
    }

    private void export(IFeature feature, StringBuffer buffer) {
        List<IFeature> featureList = null;
        if (feature.getFeatureType() == GEOJSON) {
            featureList = ((GeoJSON) feature).getFeatureList();
            appendFeatureList(featureList, buffer);
        } else if (feature.getFeatureType() == KML) {
            featureList = ((mil.emp3.api.KML) feature).getFeatureList();
            appendFeatureList(featureList, buffer);
        } else if (feature.getChildFeatures().size() > 0) {
            featureList = feature.getChildFeatures();
            appendFeatureList(featureList, buffer);
        } else {
            appendFeature(feature, buffer);
        }
    }

    public final static String export(IFeature feature) {
        GeoJsonExporter exporter = new GeoJsonExporter();
        StringBuffer buffer = new StringBuffer();
        exporter.export(feature, buffer);
        return buffer.toString();
    }
}
