package mil.emp3.json.geoJson;

import android.util.Log;

import com.eclipsesource.json.*;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoTimeSpan;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoTimeSpan;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.utils.EmpGeoColor;

public class GeoJsonParser {

    private final static String TAG = GeoJsonParser.class.getSimpleName();
    private static SimpleDateFormat zonedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    /**
     * Parses GeoJSON formatted string and returns a feature list
     * List may contain, zero, one or multiple features
     * @param is inputstream
     * @return List of IFeature
     */

    public static List<IFeature> parse (InputStream is) throws EMP_Exception {
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
     * @return List of IFeature
     */

    public static List<IFeature> parse(String input) throws EMP_Exception {
        JsonValue value = Json.parse(input);
        return parseJsonObject(value);
    }

    private static List<IFeature> parseJsonObject(JsonValue value) throws EMP_Exception {
        List<IFeature> featureList = new ArrayList<>();
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
                }
            } catch (ParseException|java.text.ParseException e) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "GeoJSON parse error");
            }
        } else {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "GeoJSON string is not valid object");
        }
        return featureList;
    }

    private static void parseFeature(List<IFeature> featureList, JsonObject object) throws ParseException,
            java.text.ParseException {
        JsonObject geometry = object.get("geometry").asObject();
        // ignore bounding box object.get("bbox").asObject();
        JsonObject properties = object.get("properties").asObject();
        parseGeometry(featureList, geometry, properties);
    }

    private static void parseGeometry(List<IFeature> featureList, JsonObject geometry, JsonObject properties)
            throws ParseException, java.text.ParseException {
        String geometryType = geometry.get("type").asString();
        boolean multi = geometryType.startsWith("Multi");
        if (multi) {
            String singleGeometryType = geometryType.substring(5);// Length of "Multi"
            JsonArray coordinateListList = geometry.get("coordinates").asArray();
            for (int i = 0; i < coordinateListList.size(); i++) {
                IFeature feature = createFeature(singleGeometryType);
                if (feature != null) {
                    JsonArray coordinateList = coordinateListList.get(i).asArray();
                    parseCoordinates(feature, coordinateList);
                    parseProperties(feature, properties);
                    featureList.add(feature);
                }
            }
        } else {
            IFeature feature = createFeature(geometryType);
            if (feature != null) {
                JsonArray coordinateList = geometry.get("coordinates").asArray();
                parseCoordinates(feature, coordinateList);
                parseProperties(feature, properties);
                featureList.add(feature);
            }
        }
    }

    private static IFeature createFeature(String geometryType) {
        IFeature newFeature = null;
        switch (geometryType) {
            case "Point":
                newFeature = new Point();
                break;
            case "LineString":
                newFeature = new Path();
                break;
            case "Polygon":
                newFeature = new Polygon();
                break;
            default:
                Log.d(TAG, "Received unknown feature type " + geometryType);
        }
        if (null != newFeature) {
            newFeature.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
            newFeature.setName("GeoJSON " + geometryType);  // default name, overwrite later
        }
        return newFeature;
    }

    private static void parseCoordinates(IFeature feature, JsonArray coordinateList) throws ParseException {
        if (!(feature instanceof Point)) {
            for (JsonValue pair : coordinateList) {
                JsonArray twoDoubles = pair.asArray();
                IGeoPosition coordinates = new GeoPosition();
                coordinates.setLatitude(twoDoubles.get(1).asDouble());
                coordinates.setLongitude(twoDoubles.get(0).asDouble());
                coordinates.setAltitude(0);
                feature.getPositions().add(coordinates);
            }
        } else {
            IGeoPosition coordinates = new GeoPosition();
            coordinates.setLatitude(coordinateList.get(1).asDouble());
            coordinates.setLongitude(coordinateList.get(0).asDouble());
            coordinates.setAltitude(0);
            ((Point)feature).setPosition(coordinates);
        }
    }

    private static void parseProperties(IFeature feature, JsonObject properties) throws ParseException,
                                                        java.text.ParseException {
        if (properties == null) {
            return;
        }
        JsonValue optional = null;
        optional = properties.get("name");
        if (optional != null) {
            String name = optional.asString();
            if (name != null && !name.isEmpty()) {
                feature.setName(name);
            }
        }
        optional = properties.get("id");
        // Ignore
//        if (optional != null) {
//            String id = optional.asString();
//            if (id != null && !id.isEmpty()) {
//                feature.setId(id);
//            }
//        }
        optional = properties.get("description");
        if (optional != null) {
            String description = optional.asString();
            if (description != null && !description.isEmpty()) {
                feature.setDescription(description);
            }
        }
        optional = properties.get("timePrimitive");
        if (optional != null) {
            JsonObject timePrimitive = optional.asObject();
            if (timePrimitive != null) {
                JsonObject timeSpan = timePrimitive.get("timeSpan").asObject();
                IGeoTimeSpan geoTimeSpan = new GeoTimeSpan();
                if (timeSpan != null) {
                    String timeSpanBegin = timeSpan.get("begin").asString();
                    geoTimeSpan.setBegin(zonedDate.parse(timeSpanBegin));
                    String timeSpanEnd = timeSpan.get("end").asString();
                    geoTimeSpan.setEnd(zonedDate.parse(timeSpanEnd));
                    feature.getTimeSpans().add(geoTimeSpan);
                }
                String timeStamp = timePrimitive.get("timeStamp").asString();
                if (timeStamp != null && !timeStamp.isEmpty()) {
                    Date ts = zonedDate.parse(timeStamp);
                    feature.setTimeStamp(ts);
                }
            }
        }
        optional = properties.get("style");
        if (optional != null) {
            JsonObject style = optional.asObject();
            if (style != null) {
                if (feature instanceof Point) {
                    optional = style.get("iconStyle");
                    if (optional != null) {
                        JsonObject iconStyle = optional.asObject();
                        if (iconStyle != null) {
                            String url = iconStyle.get("url").asString();
                            ((Point) feature).setIconURI(url);
                        }
                    }
                } else {
                    optional = style.get("lineStyle");
                    if (optional != null) {
                        JsonObject lineStyle = optional.asObject();
                        if (lineStyle != null) {
                            JsonObject lineColor = lineStyle.get("color").asObject();
                            feature.getStrokeStyle().setStrokeColor(parseColor(lineColor));
                        }
                    }
                    optional = style.get("polyStyle");
                    if (optional != null && feature instanceof Polygon) {
                        JsonObject polyStyle = optional.asObject();
                        if (polyStyle != null) {
                            JsonObject polyColor = polyStyle.get("color").asObject();
                            feature.getFillStyle().setFillColor(parseColor(polyColor));
                        }
                    }
                }
            }
        }
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
