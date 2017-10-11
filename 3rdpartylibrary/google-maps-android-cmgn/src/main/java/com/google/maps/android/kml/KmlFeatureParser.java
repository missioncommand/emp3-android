package com.google.maps.android.kml;

import android.webkit.URLUtil;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Parses the feature of a given KML file into a KmlPlacemark or KmlGroundOverlay object
 *
 * NOTE: This file has been modified from its initial content to account for Common Map Geospatial Notation.
 */
class KmlFeatureParser {

    private final static String GEOMETRY_REGEX = "Point|LineString|Polygon|MultiGeometry";

    private final static int LONGITUDE_INDEX = 0;

    private final static int LATITUDE_INDEX = 1;

    private final static int ALTITUDE_INDEX = 2;

    private final static String PROPERTY_REGEX = "name|description|drawOrder|visibility|open|address|phoneNumber|altitudeMode|extrude";

    private final static String BOUNDARY_REGEX = "outerBoundaryIs|innerBoundaryIs";

    private final static String EXTENDED_DATA = "ExtendedData";

    private final static String STYLE_URL_TAG = "styleUrl";

    private final static String STYLE_TAG = "Style";

    private final static String COMPASS_REGEX = "north|south|east|west";

    /**
     * Creates a new Placemark object (created if a Placemark start tag is read by the
     * XmlPullParser and if a Geometry tag is contained within the Placemark tag)
     * and assigns specific elements read from the parser to the Placemark.
     */
    /* package */
    static KmlPlacemark createPlacemark(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        String styleId = null;
        KmlStyle inlineStyle = null;
        HashMap<String, String> properties = new HashMap<String, String>();
        KmlGeometry geometry = null;
        int eventType = parser.getEventType();
        String placemarkId = null;

        if (parser.getAttributeCount() > 0) {
            for (int iIndex = 0; iIndex < parser.getAttributeCount(); iIndex++) {
                if (parser.getAttributeName(iIndex).toLowerCase().equals("id")) {
                    placemarkId = parser.getAttributeValue(iIndex);
                }
            }
        }
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Placemark"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals(STYLE_URL_TAG)) {
                    styleId = parser.nextText();
                } else if (parser.getName().matches(GEOMETRY_REGEX)) {
                    geometry = createGeometry(parser, parser.getName(), properties);
                } else if (parser.getName().matches(PROPERTY_REGEX)) {
                    properties.put(parser.getName(), parser.nextText());
                } else if (parser.getName().equals(EXTENDED_DATA)) {
                    properties.putAll(setExtendedDataProperties(parser));
                } else if (parser.getName().equals(STYLE_TAG)) {
                    inlineStyle = KmlStyleParser.createStyle(parser);
                }
            }
            eventType = parser.next();
        }
        return new KmlPlacemark(placemarkId, geometry, styleId, inlineStyle, properties);
    }

    /**
     * Creates a new GroundOverlay object (created if a GroundOverlay tag is read by the
     * XmlPullParser) and assigns specific elements read from the parser to the GroundOverlay
     * @param documentBase Path leading to kml file directory to generate full path file url for map service.
     *                     For example if the file was in C:/Documents/MyKMZ.kmz/test.kml this value would be C:/Documents/MyKMZ.kmz
     */
    /* package */
    static KmlGroundOverlay createGroundOverlay(XmlPullParser parser, String documentBase)
            throws IOException, XmlPullParserException {
        float drawOrder = 0.0f;
        float rotation = 0.0f;
        int visibility = 1;
        String imageUrl = null;
        IGeoBounds bBox;
        HashMap<String, String> properties = new HashMap<String, String>();
        HashMap<String, Double> compassPoints = new HashMap<String, Double>();

        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("GroundOverlay"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("Icon")) {
                    imageUrl = getImageUrl(parser, documentBase);
                } else if (parser.getName().equals("drawOrder")) {
                    drawOrder = Float.parseFloat(parser.nextText());
                } else if (parser.getName().equals("visibility")) {
                    visibility = Integer.parseInt(parser.nextText());
                } else if (parser.getName().equals("ExtendedData")) {
                    properties.putAll(setExtendedDataProperties(parser));
                } else if (parser.getName().equals("rotation")) {
                    rotation = getRotation(parser);
                } else if (parser.getName().matches(PROPERTY_REGEX) || parser.getName().equals("color")) {
                    properties.put(parser.getName(), parser.nextText());
                } else if (parser.getName().matches(COMPASS_REGEX)) {
                    compassPoints.put(parser.getName(), Double.parseDouble(parser.nextText()));
                }
            }
            eventType = parser.next();
        }
        bBox = createLatLngBounds(compassPoints.get("north"), compassPoints.get("south"),
                compassPoints.get("east"), compassPoints.get("west"));
        return new KmlGroundOverlay(imageUrl, bBox, drawOrder, visibility, properties, rotation);
    }

    private static float getRotation(XmlPullParser parser)
            throws IOException, XmlPullParserException {
        return -Float.parseFloat(parser.nextText());
    }

    /**
     * Retrieves a url from the "href" tag nested within an "Icon" tag, read by
     * the XmlPullParser.
     * If the image is a local reference, get the absolute path from the documentBase
     * and then cast it into a File URl.
     *
     * @param documentBase Path leading to kml file directory to generate full path file url for map service.
     *                     For example if the file was in C:/Documents/MyKMZ.kmz/test.kml this value would be C:/Documents/MyKMZ.kmz
     * @return An image url
     */
    private static String getImageUrl(XmlPullParser parser, String documentBase)
            throws IOException, XmlPullParserException {
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Icon"))) {
            if (eventType == XmlPullParser.START_TAG && parser.getName().equals("href")) {
                String imageUrl =  parser.nextText();
                if(!URLUtil.isValidUrl(imageUrl)) {
                    return new File(documentBase+File.separator+imageUrl).toURI().toURL().toString();
                }
                return imageUrl;
            }
            eventType = parser.next();
        }
        return null;
    }

    /**
     * Creates a new KmlGeometry object (Created if "Point", "LineString", "Polygon" or
     * "MultiGeometry" tag is detected by the XmlPullParser)
     *
     * @param geometryType Type of geometry object to create
     */
    private static KmlGeometry createGeometry(XmlPullParser parser, String geometryType, HashMap<String, String> properties)
            throws IOException, XmlPullParserException {
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(geometryType))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("Point")) {
                    return createPoint(parser);
                } else if (parser.getName().equals("LineString")) {
                    return createLineString(parser, properties);
                } else if (parser.getName().equals("Polygon")) {
                    return createPolygon(parser, properties);
                } else if (parser.getName().equals("MultiGeometry")) {
                    return createMultiGeometry(parser, properties);
                }
            }
            eventType = parser.next();
        }
        return null;
    }

    /**
     * Adds untyped name value pairs parsed from the ExtendedData
     */
    private static HashMap<String, String> setExtendedDataProperties(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        HashMap<String, String> properties = new HashMap<String, String>();
        String propertyKey = null;
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(EXTENDED_DATA))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().equals("Data")) {
                    propertyKey = parser.getAttributeValue(null, "name");
                } else if (parser.getName().equals("value") && propertyKey != null) {
                    properties.put(propertyKey, parser.nextText());
                    propertyKey = null;
                }
            }
            eventType = parser.next();
        }
        return properties;
    }

    /**
     * Creates a new KmlPoint object
     *
     * @return KmlPoint object
     */
    private static KmlPoint createPoint(XmlPullParser parser)
            throws XmlPullParserException, IOException {
        IGeoPosition coordinate = null;
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Point"))) {
            if (eventType == XmlPullParser.START_TAG && parser.getName().equals("coordinates")) {
                coordinate = convertToLatLng(parser.nextText());
            }
            eventType = parser.next();
        }
        return new KmlPoint(coordinate);
    }

    /**
     * Creates a new KmlLineString object
     *
     * @return KmlLineString object
     */
    private static KmlLineString createLineString(XmlPullParser parser, HashMap<String, String> properties)
            throws XmlPullParserException, IOException {
        List<IGeoPosition> coordinates = new ArrayList<>();
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("LineString"))) {
            if (eventType == XmlPullParser.START_TAG && parser.getName().equals("coordinates")) {
                coordinates = convertToLatLngArray(parser.nextText());
            }
            eventType = parser.next();
        }
        return new KmlLineString(coordinates);
    }

    /**
     * Creates a new KmlPolygon object. Parses only one outer boundary and no or many inner
     * boundaries containing the coordinates.
     *
     * @return KmlPolygon object
     */
    private static KmlPolygon createPolygon(XmlPullParser parser, HashMap<String, String> properties)
            throws XmlPullParserException, IOException {
        // Indicates if an outer boundary needs to be defined
        Boolean isOuterBoundary = false;
        List<IGeoPosition> outerBoundary = new ArrayList<>();
        List<List<IGeoPosition>> innerBoundaries = new ArrayList<>();
        int eventType = parser.getEventType();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("Polygon"))) {
            if (eventType == XmlPullParser.START_TAG) {
                if (parser.getName().matches(PROPERTY_REGEX)) {
                    properties.put(parser.getName(), parser.nextText());
                } else if (parser.getName().matches(BOUNDARY_REGEX)) {
                    isOuterBoundary = parser.getName().equals("outerBoundaryIs");
                } else if (parser.getName().equals("coordinates")) {
                    if (isOuterBoundary) {
                        outerBoundary = convertToLatLngArray(parser.nextText());
                    } else {
                        innerBoundaries.add(convertToLatLngArray(parser.nextText()));
                    }
                }
            }
            eventType = parser.next();
        }
        return new KmlPolygon(outerBoundary, innerBoundaries);
    }

    /**
     * Creates a new KmlMultiGeometry object
     *
     * @return KmlMultiGeometry object
     */
    private static KmlMultiGeometry createMultiGeometry(XmlPullParser parser, HashMap<String, String> properties)
            throws XmlPullParserException, IOException {
        ArrayList<KmlGeometry> geometries = new ArrayList<KmlGeometry>();
        // Get next otherwise have an infinite loop
        int eventType = parser.next();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals("MultiGeometry"))) {
            if (eventType == XmlPullParser.START_TAG && parser.getName().matches(GEOMETRY_REGEX)) {
                geometries.add(createGeometry(parser, parser.getName(), properties));
            }
            eventType = parser.next();
        }
        return new KmlMultiGeometry(geometries);
    }

    /**
     * Convert a string of coordinates into an array of LatLngs
     *
     * @param coordinatesString coordinates string to convert from
     * @return array of LatLng objects created from the given coordinate string array
     */
    private static List<IGeoPosition> convertToLatLngArray(String coordinatesString) {
        ArrayList<IGeoPosition> coordinatesArray = new ArrayList<IGeoPosition>();
        // Need to trim to avoid whitespace around the coordinates such as tabs
        String[] coordinates = coordinatesString.trim().split("(\\s+)");
        for (String coordinate : coordinates) {
            coordinatesArray.add(convertToLatLng(coordinate));
        }
        return coordinatesArray;
    }

    /**
     * Convert a string coordinate from a string into a LatLng object
     *
     * @param coordinateString coordinate string to convert from
     * @return LatLng object created from given coordinate string
     */
    private static IGeoPosition convertToLatLng(String coordinateString) {
        // Lat and Lng are separated by a ,
        String[] coordinate = coordinateString.split(",");
        Double lat = Double.parseDouble(coordinate[LATITUDE_INDEX]);
        Double lon = Double.parseDouble(coordinate[LONGITUDE_INDEX]);
        IGeoPosition pos = new GeoPosition();
        pos.setLatitude(lat);
        pos.setLongitude(lon);
        if (coordinate.length > 2) {
            Double alt = Double.parseDouble(coordinate[ALTITUDE_INDEX]);
            pos.setAltitude(alt);
        }
        return pos;
    }

    /**
     * Given a set of four latLng coordinates, creates a LatLng Bound
     *
     * @param north North coordinate of the bounding box
     * @param south South coordinate of the bounding box
     * @param east  East coordinate of the bounding box
     * @param west  West coordinate of the bounding box
     */
    private static IGeoBounds createLatLngBounds(Double north, Double south, Double east,
            Double west) {
        IGeoBounds gBounds = new GeoBounds();

        gBounds.setNorth(north);
        gBounds.setWest(west);
        gBounds.setSouth(south);
        gBounds.setEast(east);

        return gBounds;
    }
}
