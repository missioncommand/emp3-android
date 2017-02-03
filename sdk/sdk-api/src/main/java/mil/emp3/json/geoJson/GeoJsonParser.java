package mil.emp3.json.geoJson;

import com.eclipsesource.json.*;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.utils.EmpGeoColor;

public class GeoJsonParser {

    private final static String TAG = GeoJsonParser.class.getSimpleName();

    /**
     * Parses GeoJSON formatted string and returns a feature list
     * List may contain, zero, one or multiple features
     * @param is inputstream
     * @return List of GeoJsonFeature
     */

    public static List<GeoJsonFeature> parse (InputStream is) throws EMP_Exception {
        try {
            Reader reader = new InputStreamReader(is);
            JsonValue value = Json.parse(reader);
            return parseJsonObject(value);
        } catch (Exception ioe) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "GeoJSON input error");
        }
    }

    /**
     * Parses GeoJSON formatted string and returns a feature list
     * List may contain, zero, one or multiple features
     * @param input string
     * @return List of GeoJsonFeature
     */

    public static List<GeoJsonFeature> parse(String input) throws EMP_Exception {
        JsonValue value = Json.parse(input);
        return parseJsonObject(value);
    }

    private static List<GeoJsonFeature> parseJsonObject(JsonValue value) throws EMP_Exception {
        List<GeoJsonFeature> featureList = new ArrayList<>();
        // At this point input must be feature or feature collection
        if (value.isObject()) {
            try {
                JsonObject object = value.asObject();
                String objectType = object.get("type").asString();
                if (objectType.equals("FeatureCollection")) {
                    JsonArray featureArray = object.get("features").asArray();
                    for (JsonValue feature : featureArray) {
                        parseFeature(featureList, feature.asObject());
                    }
                } else if (objectType.equals("Feature")) {
                    parseFeature(featureList, object);
                } else if (objectType.equals("GeometryCollection")) {
                    JsonArray geometryArray = object.get("geometries").asArray();
                    for (JsonValue geometry : geometryArray) {
                        parseGeometry(featureList, geometry.asObject(), null);
                    }
                    for (GeoJsonFeature feature : featureList) {
                        feature.setProperties(new GeoJsonProperties());
                    }
                }
            } catch (ParseException e) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "GeoJSON parse error");
            }
        } else {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "GeoJSON string is not valid object");
        }
        return featureList;
    }

    private static void parseFeature(List<GeoJsonFeature> featureList, JsonObject object) throws ParseException{
        JsonObject geometry = object.get("geometry").asObject();
        JsonObject properties = object.get("properties").asObject();
        parseGeometry(featureList, geometry, properties);
    }

    private static void parseGeometry(List<GeoJsonFeature> featureList, JsonObject geometry, JsonObject properties) throws ParseException {
        String geometryType = geometry.get("type").asString();
        boolean multi = geometryType.startsWith("Multi");
        GeoJsonProperties geoJsonProperties = null;
        if (properties != null) {
            geoJsonProperties = parseProperties(properties);
        }
        if (multi) {
            String singleGeometryType = geometryType.substring(5);// Length of "Multi"
            JsonArray coordinateListList = geometry.get("coordinates").asArray();
            for (int i = 0; i < coordinateListList.size(); i++) {
                GeoJsonFeature feature = new GeoJsonFeature();
                feature.setGeometryType(GeometryEnum.fromString(singleGeometryType));
                JsonArray coordinateList = coordinateListList.get(i).asArray();
                parseCoordinates(feature, coordinateList);
                feature.setProperties(geoJsonProperties);
                featureList.add(feature);
            }
        } else {
            GeoJsonFeature feature = new GeoJsonFeature();
            feature.setGeometryType(GeometryEnum.fromString(geometryType));
            JsonArray coordinateList = geometry.get("coordinates").asArray();
            parseCoordinates(feature, coordinateList);
            feature.setProperties(geoJsonProperties);
            featureList.add(feature);
        }
    }

    private static void parseCoordinates(GeoJsonFeature feature, JsonArray coordinateList) throws ParseException {
        if (feature.getGeometryType() != GeometryEnum.POINT) {
            for (JsonValue pair : coordinateList) {
                JsonArray twoDoubles = pair.asArray();
                IGeoPosition coordinates = new GeoPosition();
                coordinates.setLatitude(twoDoubles.get(1).asDouble());
                coordinates.setLongitude(twoDoubles.get(0).asDouble());
                coordinates.setAltitude(0);
                feature.add(coordinates);
            }
        } else {
            IGeoPosition coordinates = new GeoPosition();
            coordinates.setLatitude(coordinateList.get(1).asDouble());
            coordinates.setLongitude(coordinateList.get(0).asDouble());
            coordinates.setAltitude(0);
            feature.add(coordinates);
        }
    }

    private static GeoJsonProperties parseProperties(JsonObject properties) throws ParseException {
        GeoJsonProperties geoJsonProperties = new GeoJsonProperties();
        JsonValue optional = null;
        optional = properties.get("name");
        if (optional != null) {
            String name = optional.asString();
            if (name != null && !name.isEmpty()) {
                geoJsonProperties.setName(name);
            }
        }
        optional = properties.get("id");
        if (optional != null) {
            String id = optional.asString();
            if (id != null && !id.isEmpty()) {
                geoJsonProperties.setId(id);
            }
        }
        optional = properties.get("description");
        if (optional != null) {
            String description = optional.asString();
            if (description != null && !description.isEmpty()) {
                geoJsonProperties.setDescription(description);
            }
        }
        optional = properties.get("timePrimitive");
        if (optional != null) {
            JsonObject timePrimitive = optional.asObject();
            if (timePrimitive != null) {
                JsonObject timeSpan = timePrimitive.get("timeSpan").asObject();
                if (timeSpan != null) {
                    String timeSpanBegin = timeSpan.get("begin").asString();
                    geoJsonProperties.setTimeSpanBegin(timeSpanBegin);
                    String timeSpanEnd = timeSpan.get("end").asString();
                    geoJsonProperties.setTimeSpanEnd(timeSpanEnd);
                }
                String timeStamp = timePrimitive.get("timeStamp").asString();
                if (timeStamp != null && !timeStamp.isEmpty()) {
                    geoJsonProperties.setTimeStamp(timeStamp);
                }
            }
        }
        optional = properties.get("style");
        if (optional != null) {
            JsonObject style = optional.asObject();
            if (style != null) {
                optional = style.get("lineStyle");
                if (optional != null) {
                    JsonObject lineStyle = optional.asObject();
                    if (lineStyle != null) {
                        JsonObject lineColor = lineStyle.get("color").asObject();
                        geoJsonProperties.setLineStyle(parseColor(lineColor));
                    }
                }
                optional = style.get("polyStyle");
                if (optional != null) {
                    JsonObject polyStyle = optional.asObject();
                    if (polyStyle != null) {
                        JsonObject polyColor = polyStyle.get("color").asObject();
                        geoJsonProperties.setPolyStyle(parseColor(polyColor));
                    }
                }
                optional = style.get("iconStyle");
                if (optional != null) {
                    JsonObject iconStyle = optional.asObject();
                    if (iconStyle != null) {
                        String url = iconStyle.get("url").asString();
                        geoJsonProperties.setIconStyle(url);
                    }
                }
            }
        }
        return geoJsonProperties;
    }

    private static EmpGeoColor parseColor(JsonObject color) throws ParseException {
        int r = color.get("r").asInt();
        int g = color.get("g").asInt();
        int b = color.get("b").asInt();
        double a = color.get("a").asDouble();
        EmpGeoColor empGeoColor = new EmpGeoColor(a, r, g, b);
        return empGeoColor;
    }
}
