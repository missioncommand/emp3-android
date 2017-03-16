package mil.emp3.api.utils.kml;

import android.util.Xml;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.GeoJSON;
import mil.emp3.api.KML;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Overlay;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.Text;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.abstracts.Map;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IKMLExportable;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.utils.MilStdUtilities;

/**
 * This class implements the KML export capability for EMP.
 */

public class EmpKMLExporter {
    private static String TAG = EmpKMLExporter.class.getSimpleName();

    private interface ISerializePlacemarkGeometry {
        void serializeGeometry(XmlSerializer xmlSerializer)
                throws IOException;
    }

    private EmpKMLExporter() {

    }

    private void serializeName(IContainer container, XmlSerializer xmlSerializer)
            throws IOException {
        if ((null != container.getName()) && !container.getName().isEmpty()) {
            xmlSerializer.startTag(null, "name");
            xmlSerializer.text(container.getName());
            xmlSerializer.endTag(null, "name");
        }
    }

    private void serializeDescription(IContainer container, XmlSerializer xmlSerializer)
            throws IOException {
        if ((null != container.getDescription()) && !container.getDescription().isEmpty()) {
            xmlSerializer.startTag(null, "description");
            xmlSerializer.text(container.getDescription());
            xmlSerializer.endTag(null, "description");
        }
    }

    private void serializeVisibility(IContainer container, XmlSerializer xmlSerializer)
            throws IOException {
        xmlSerializer.startTag(null, "visibility");
        xmlSerializer.text("1");
        xmlSerializer.endTag(null, "visibility");
    }

    private void serializeExtrude(IFeature feature, XmlSerializer xmlSerializer)
            throws IOException {
        if (feature.getExtrude()) {
            xmlSerializer.startTag(null, "extrude");
            xmlSerializer.text("1");
            xmlSerializer.endTag(null, "extrude");
        }
    }

    private void serializeCoordinates(List<IGeoPosition> coorList, XmlSerializer xmlSerializer)
            throws IOException {
        String temp = "";

        if (coorList.isEmpty()) {
            return;
        }

        xmlSerializer.startTag(null, "coordinates");
        for (IGeoPosition pos : coorList) {
            if (!temp.isEmpty()) {
                temp = " ";
            }
            temp += pos.getLongitude() + "," + pos.getLatitude() + "," + pos.getAltitude();
        }
        xmlSerializer.text(temp);
        xmlSerializer.endTag(null, "coordinates");

    }

    private void serializeAltitudeMode(IFeature feature, XmlSerializer xmlSerializer)
            throws IOException {
        xmlSerializer.startTag(null, "altitudeMode");
        switch (feature.getAltitudeMode()) {
            case ABSOLUTE:
                xmlSerializer.text("absolute");
                break;
            case RELATIVE_TO_GROUND:
                xmlSerializer.text("relativeToGround");
                break;
            case CLAMP_TO_GROUND:
            default:
                xmlSerializer.text("clampToGround");
                break;
        }
        xmlSerializer.endTag(null, "altitudeMode");
    }

    private void serializeIconHotSpot(IGeoIconStyle iconStyle, XmlSerializer xmlSerializer)
            throws IOException {
        if (null != iconStyle) {
            xmlSerializer.startTag(null, "hotSpot");
            xmlSerializer.attribute(null, "x", "" + iconStyle.getOffSetX());
            xmlSerializer.attribute(null, "y", "" + iconStyle.getOffSetY());
            xmlSerializer.attribute(null, "xunits", "pixels");
            xmlSerializer.attribute(null, "yunits", "pixels");
            xmlSerializer.endTag(null, "hotSpot");
        }
    }

    private void serializeHRef(String URI, XmlSerializer xmlSerializer)
            throws IOException {
        if ((null != URI) && !URI.isEmpty()) {
            xmlSerializer.startTag(null, "href");
            xmlSerializer.text(URI);
            xmlSerializer.endTag(null, "href");
        }
    }

    private void serializePlacemark(IFeature feature, XmlSerializer xmlSerializer, ISerializePlacemarkGeometry callback)
            throws IOException {
        xmlSerializer.startTag(null, "Placemark");
        if ((null != feature.getDataProviderId()) && !feature.getDataProviderId().isEmpty()) {
            xmlSerializer.attribute(null, "id", feature.getDataProviderId());
        } else {
            xmlSerializer.attribute(null, "id", feature.getGeoId().toString());
        }

        serializeName(feature, xmlSerializer);
        serializeDescription(feature, xmlSerializer);
        serializeVisibility(feature, xmlSerializer);


        if (null == callback) {
            throw new InvalidParameterException("Invalid ISerializePlacemarkGeometry");
        }
        callback.serializeGeometry(xmlSerializer);

        xmlSerializer.endTag(null, "Placemark");
    }

    private String getStyleId(IFeature feature) {
        String id = ((null != feature.getName()) ? feature.getName() : "feature") + "-style-" + feature.getGeoId();

        id = id.replaceAll("[^a-zA-Z0-9_]", "_");
        return id;
    }

    private void serializeStrokeStyle(IGeoStrokeStyle strokeStyle, XmlSerializer xmlSerializer)
            throws IOException {
        if (null == strokeStyle) {
            return;
        }

        IGeoColor color = strokeStyle.getStrokeColor();

        xmlSerializer.startTag(null, "LineStyle");
        xmlSerializer.startTag(null, "color");
        xmlSerializer.text(Integer.toHexString((int) (color.getAlpha() * 255)) + Integer.toHexString(color.getBlue()) + Integer.toHexString(color.getGreen()) + Integer.toHexString(color.getRed()));
        xmlSerializer.endTag(null, "color");

        xmlSerializer.startTag(null, "width");
        xmlSerializer.text(Integer.toString((int) strokeStyle.getStrokeWidth()));
        xmlSerializer.endTag(null, "width");
        xmlSerializer.endTag(null, "LineStyle");
    }

    private void serializeFillStyle(IGeoFillStyle fillStyle, boolean outline, XmlSerializer xmlSerializer)
            throws IOException {
        if (null == fillStyle) {
            return;
        }

        IGeoColor color = fillStyle.getFillColor();

        xmlSerializer.startTag(null, "PolyStyle");
        xmlSerializer.startTag(null, "color");
        xmlSerializer.text(Integer.toHexString((int) (color.getAlpha() * 255)) + Integer.toHexString(color.getBlue()) + Integer.toHexString(color.getGreen()) + Integer.toHexString(color.getRed()));
        xmlSerializer.endTag(null, "color");

        xmlSerializer.startTag(null, "outline");
        xmlSerializer.text(outline ? "1" : "0");
        xmlSerializer.endTag(null, "outline");
        xmlSerializer.endTag(null, "PolyStyle");
    }

    private boolean needStyle(final Point feature) {
        return (((null != feature.getIconURI()) && !feature.getIconURI().isEmpty()) || (feature.getIconScale() != 1.0));
    }

    private boolean needStyle(final Path feature) {
        return (null != feature.getStrokeStyle());
    }

    private boolean needStyle(final Text feature) {
        return (null != feature.getLabelStyle());
    }

    private boolean polygonNeedStyle(final IFeature feature) {
        return (null != feature.getStrokeStyle()) || (null != feature.getFillStyle());
    }

    private void exportStylesToKML(final Point feature, XmlSerializer xmlSerializer) throws IOException {
        if  (needStyle(feature)) {
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", getStyleId(feature));
            xmlSerializer.startTag(null, "IconStyle");

            if (feature.getIconScale() != 1.0) {
                xmlSerializer.startTag(null, "scale");
                xmlSerializer.text("" + feature.getIconScale());
                xmlSerializer.endTag(null, "scale");
            }
            if ((null != feature.getIconURI()) && !feature.getIconURI().isEmpty()) {
                xmlSerializer.startTag(null, "Icon");
                serializeHRef(feature.getIconURI(), xmlSerializer);
                xmlSerializer.endTag(null, "Icon");

                serializeIconHotSpot(feature.getIconStyle(), xmlSerializer);
            }

            xmlSerializer.endTag(null, "IconStyle");
            xmlSerializer.endTag(null, "Style");
        }
    }

    private void exportStylesToKML(final Path feature, XmlSerializer xmlSerializer) throws IOException {
        if (needStyle(feature)) {
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", getStyleId(feature));

            serializeStrokeStyle(feature.getStrokeStyle(), xmlSerializer);
            xmlSerializer.endTag(null, "Style");
        }
    }

    private void exportStylesToKML(final Text feature, XmlSerializer xmlSerializer) throws IOException {
        if  (needStyle(feature)) {
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", getStyleId(feature));


            xmlSerializer.endTag(null, "Style");
        }
    }

    private void exportPolygonStylesToKML(final IFeature feature, XmlSerializer xmlSerializer) throws IOException {
        if  (polygonNeedStyle(feature)) {
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", getStyleId(feature));

            if (null != feature.getStrokeStyle()) {
                serializeStrokeStyle(feature.getStrokeStyle(), xmlSerializer);
            }

            if (null != feature.getFillStyle()) {
                serializeFillStyle(feature.getFillStyle(), (null != feature.getStrokeStyle()), xmlSerializer);
            }
            xmlSerializer.endTag(null, "Style");
        }
    }

    private void exportStylesToKML(final GeoJSON geoJSONfeature, XmlSerializer xmlSerializer) throws IOException {
        for (IFeature feature: geoJSONfeature.getFeatureList()) {
            exportStylesToKML(feature, xmlSerializer);
        }
    }

    private void exportStylesToKML(final KML kmlfeature, XmlSerializer xmlSerializer) throws IOException {
        for (IFeature feature: kmlfeature.getFeatureList()) {
            exportStylesToKML(feature, xmlSerializer);
        }
    }

    private void exportStylesToKML(final MilStdSymbol feature, XmlSerializer xmlSerializer) throws IOException {
        if (feature.isSinglePoint()) {
            xmlSerializer.startTag(null, "Style");
            xmlSerializer.attribute(null, "id", getStyleId(feature));
            xmlSerializer.startTag(null, "IconStyle");

            if (feature.getIconScale() != 1.0) {
                xmlSerializer.startTag(null, "scale");
                xmlSerializer.text("" + feature.getIconScale());
                xmlSerializer.endTag(null, "scale");
            }
/*
            if ((null != feature.getIconURI()) && !feature.getIconURI().isEmpty()) {
                xmlSerializer.startTag(null, "Icon");
                serializeHRef(feature.getIconURI(), xmlSerializer);
                xmlSerializer.endTag(null, "Icon");

                serializeIconHotSpot(feature.getIconStyle(), xmlSerializer);
            }
*/
            xmlSerializer.endTag(null, "IconStyle");
            xmlSerializer.endTag(null, "Style");
        } else if (polygonNeedStyle(feature)) {
            exportPolygonStylesToKML(feature, xmlSerializer);
        }
    }

    private void exportStylesToKML(final IContainer container, XmlSerializer xmlSerializer) throws IOException {
        if (container instanceof Point) {
            exportStylesToKML((Point) container, xmlSerializer);
        } else if (container instanceof Path) {
            exportStylesToKML((Path) container, xmlSerializer);
        } else if (container instanceof Text) {
            exportStylesToKML((Text) container, xmlSerializer);
        } else if (container instanceof GeoJSON) {
            exportStylesToKML((GeoJSON) container, xmlSerializer);
        } else if (container instanceof KML) {
            exportStylesToKML((KML) container, xmlSerializer);
        } else if (container instanceof IFeature) {
            exportStylesToKML((IFeature) container, xmlSerializer);
        }

        for (IGeoBase geoObject : container.getChildren()) {
            if (geoObject instanceof IContainer) {
                exportStylesToKML((IContainer) geoObject, xmlSerializer);
            }
        }
    }

    private void exportEmpObjectToKML(final Point feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (needStyle(feature)){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + getStyleId(feature));
                    xmlSerializer.endTag(null, "styleUrl");
                }
                xmlSerializer.startTag(null, "Point");
                serializeExtrude(feature, xmlSerializer);
                serializeAltitudeMode(feature, xmlSerializer);
                serializeCoordinates(feature.getPositions(), xmlSerializer);
                xmlSerializer.endTag(null, "Point");
            }
        });
    }

    private void exportEmpObjectToKML(final Path feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (needStyle(feature)){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + getStyleId(feature));
                    xmlSerializer.endTag(null, "styleUrl");
                }
                xmlSerializer.startTag(null, "LineString");
                serializeExtrude(feature, xmlSerializer);
                serializeAltitudeMode(feature, xmlSerializer);
                serializeCoordinates(feature.getPositions(), xmlSerializer);
                xmlSerializer.endTag(null, "LineString");
            }
        });
    }

    private void exportEmpObjectToKML(final Text feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (needStyle(feature)){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + getStyleId(feature));
                    xmlSerializer.endTag(null, "styleUrl");
                }

                xmlSerializer.startTag(null, "Point");
                serializeExtrude(feature, xmlSerializer);
                serializeAltitudeMode(feature, xmlSerializer);
                serializeCoordinates(feature.getPositions(), xmlSerializer);
                xmlSerializer.endTag(null, "Point");
            }
        });
    }

    private void exportEmpObjectToKML(final Circle feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                List<IGeoPosition> posList = feature.getPolygonPositionList();

                // KML needs the first and last position to be equal.
                posList.add(feature.getPositions().get(0));

                if  (polygonNeedStyle(feature)){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + getStyleId(feature));
                    xmlSerializer.endTag(null, "styleUrl");
                }

                xmlSerializer.startTag(null, "Polygon");
                serializeExtrude(feature, xmlSerializer);
                serializeAltitudeMode(feature, xmlSerializer);
                xmlSerializer.startTag(null, "outerBoundaryIs");
                xmlSerializer.startTag(null, "LinearRing");
                serializeCoordinates(posList, xmlSerializer);
                xmlSerializer.endTag(null, "LinearRing");
                xmlSerializer.endTag(null, "outerBoundaryIs");
                xmlSerializer.endTag(null, "Polygon");
            }
        });
    }

    private void exportEmpObjectToKML(final Ellipse feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                List<IGeoPosition> posList = feature.getPolygonPositionList();

                // KML needs the first and last position to be equal.
                posList.add(feature.getPositions().get(0));

                if  (polygonNeedStyle(feature)){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + getStyleId(feature));
                    xmlSerializer.endTag(null, "styleUrl");
                }

                xmlSerializer.startTag(null, "Polygon");
                serializeExtrude(feature, xmlSerializer);
                serializeAltitudeMode(feature, xmlSerializer);
                xmlSerializer.startTag(null, "outerBoundaryIs");
                xmlSerializer.startTag(null, "LinearRing");
                serializeCoordinates(posList, xmlSerializer);
                xmlSerializer.endTag(null, "LinearRing");
                xmlSerializer.endTag(null, "outerBoundaryIs");
                xmlSerializer.endTag(null, "Polygon");
            }
        });
    }

    private void exportEmpObjectToKML(final Polygon feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (polygonNeedStyle(feature)){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + getStyleId(feature));
                    xmlSerializer.endTag(null, "styleUrl");
                }
                List<IGeoPosition> posList = new ArrayList<>();

                // KML needs the first and last position to be equal.
                posList.addAll(feature.getPositions());
                posList.add(feature.getPositions().get(0));

                xmlSerializer.startTag(null, "Polygon");
                serializeExtrude(feature, xmlSerializer);
                serializeAltitudeMode(feature, xmlSerializer);
                xmlSerializer.startTag(null, "outerBoundaryIs");
                xmlSerializer.startTag(null, "LinearRing");
                serializeCoordinates(posList, xmlSerializer);
                xmlSerializer.endTag(null, "LinearRing");
                xmlSerializer.endTag(null, "outerBoundaryIs");
                xmlSerializer.endTag(null, "Polygon");
            }
        });
    }

    private void exportEmpObjectToKML(final GeoJSON geoJSONFeature, XmlSerializer xmlSerializer) throws IOException {
        for (IFeature feature: geoJSONFeature.getFeatureList()) {
            exportEmpObjectToKML(feature, xmlSerializer);
        }
    }

    private void exportEmpObjectToKML(final KML kmlFeature, XmlSerializer xmlSerializer) throws IOException {
        for (IFeature feature: kmlFeature.getFeatureList()) {
            exportEmpObjectToKML(feature, xmlSerializer);
        }
    }

    private void exportEmpObjectToKML(final Rectangle feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                List<IGeoPosition> posList = feature.getCorners();

                // KML needs the first and last position to be equal.
                posList.add(feature.getPositions().get(0));

                if  (polygonNeedStyle(feature)){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + getStyleId(feature));
                    xmlSerializer.endTag(null, "styleUrl");
                }

                xmlSerializer.startTag(null, "Polygon");
                serializeExtrude(feature, xmlSerializer);
                serializeAltitudeMode(feature, xmlSerializer);
                xmlSerializer.startTag(null, "outerBoundaryIs");
                xmlSerializer.startTag(null, "LinearRing");
                serializeCoordinates(posList, xmlSerializer);
                xmlSerializer.endTag(null, "LinearRing");
                xmlSerializer.endTag(null, "outerBoundaryIs");
                xmlSerializer.endTag(null, "Polygon");
            }
        });
    }

    private void exportEmpObjectToKML(final Square feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new EmpKMLExporter.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                List<IGeoPosition> posList = feature.getCorners();

                // KML needs the first and last position to be equal.
                posList.add(feature.getPositions().get(0));

                if  (polygonNeedStyle(feature)){
                    xmlSerializer.startTag(null, "styleUrl");
                    xmlSerializer.text("#" + getStyleId(feature));
                    xmlSerializer.endTag(null, "styleUrl");
                }

                xmlSerializer.startTag(null, "Polygon");
                serializeExtrude(feature, xmlSerializer);
                serializeAltitudeMode(feature, xmlSerializer);
                xmlSerializer.startTag(null, "outerBoundaryIs");
                xmlSerializer.startTag(null, "LinearRing");
                serializeCoordinates(posList, xmlSerializer);
                xmlSerializer.endTag(null, "LinearRing");
                xmlSerializer.endTag(null, "outerBoundaryIs");
                xmlSerializer.endTag(null, "Polygon");
            }
        });
    }

    private void exportEmpObjectToKML(final IContainer container, XmlSerializer xmlSerializer) throws IOException {
        if (container instanceof Point) {
            exportEmpObjectToKML((Point) container, xmlSerializer);
        } else if (container instanceof Path) {
            exportEmpObjectToKML((Path) container, xmlSerializer);
        } else if (container instanceof Polygon) {
            exportEmpObjectToKML((Polygon) container, xmlSerializer);
        } else if (container instanceof Text) {
            exportEmpObjectToKML((Text) container, xmlSerializer);
        } else if (container instanceof GeoJSON) {
            exportEmpObjectToKML((GeoJSON) container, xmlSerializer);
        } else if (container instanceof KML) {
            exportEmpObjectToKML((KML) container, xmlSerializer);
        } else if (container instanceof Circle) {
            exportEmpObjectToKML((Circle) container, xmlSerializer);
        } else if (container instanceof Ellipse) {
            exportEmpObjectToKML((Ellipse) container, xmlSerializer);
        } else if (container instanceof Rectangle) {
            exportEmpObjectToKML((Rectangle) container, xmlSerializer);
        } else if (container instanceof Square) {
            exportEmpObjectToKML((Square) container, xmlSerializer);
        }

        if (container.getChildren().size() > 0) {
            xmlSerializer.startTag(null, "Folder");

            if (container instanceof Feature) {
                // If we are exporting a feature, this folder contains the features children.
                // We set the targetId to the id of the parent feature.
                if ((null != container.getDataProviderId()) && !container.getDataProviderId().isEmpty()) {
                    xmlSerializer.attribute(null, "targetId", container.getDataProviderId());
                } else {
                    xmlSerializer.attribute(null, "targetId", container.getGeoId().toString());
                }

                xmlSerializer.startTag(null, "name");
                if ((null != container.getName()) && !container.getName().isEmpty()) {
                    xmlSerializer.text(container.getName() + " - Children");
                } else {
                    xmlSerializer.text("EMP feature Children");
                }
                xmlSerializer.endTag(null, "name");
            } else {
                if ((null != container.getDataProviderId()) && !container.getDataProviderId().isEmpty()) {
                    xmlSerializer.attribute(null, "id", container.getDataProviderId());
                } else {
                    xmlSerializer.attribute(null, "id", container.getGeoId().toString());
                }

                xmlSerializer.startTag(null, "name");
                if ((null != container.getName()) && !container.getName().isEmpty()) {
                    xmlSerializer.text(container.getName());
                } else {
                    xmlSerializer.text("EMP Untitled Overlay");
                }
                xmlSerializer.endTag(null, "name");

                if ((null != container.getDescription()) && !container.getDescription().isEmpty()) {
                    xmlSerializer.startTag(null, "description");
                    xmlSerializer.text(container.getDescription());
                    xmlSerializer.endTag(null, "description");
                }
            }

            for (IGeoBase geoObject : container.getChildren()) {
                if (geoObject instanceof IKMLExportable) {
                    ((IKMLExportable) geoObject).exportEmpObjectToKML(xmlSerializer);
                }
            }

            xmlSerializer.endTag(null, "Folder");
        }
    }

    private String export(IMap map, XmlSerializer xmlSerializer)
            throws IOException {
        StringWriter writer = new StringWriter();

        xmlSerializer.setOutput(writer);
        // start DOCUMENT
        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.startTag(null, "kml");
        xmlSerializer.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");

        xmlSerializer.startTag(null, "Document");

        if ((null != map.getDataProviderId()) && !map.getDataProviderId().isEmpty()) {
            xmlSerializer.attribute(null, "id", map.getDataProviderId());
        } else {
            xmlSerializer.attribute(null, "id", map.getGeoId().toString());
        }

        xmlSerializer.startTag(null, "name");
        if ((null != map.getName()) && !map.getName().isEmpty()) {
            xmlSerializer.text(map.getName());
        } else {
            xmlSerializer.text("EMP Untitled Map");
        }
        xmlSerializer.endTag(null, "name");

        if ((null != map.getDescription()) && !map.getDescription().isEmpty()) {
            xmlSerializer.startTag(null, "description");
            xmlSerializer.text(map.getDescription());
            xmlSerializer.endTag(null, "description");
        }

        exportStylesToKML(map, xmlSerializer);

        for (IGeoBase geoObject : map.getChildren()) {
            if (geoObject instanceof IContainer) {
                exportEmpObjectToKML((IContainer) geoObject, xmlSerializer);
            }
        }

        xmlSerializer.endTag(null, "Document");

        xmlSerializer.endTag(null, "kml");
        xmlSerializer.endDocument();
        xmlSerializer.flush();
        return writer.toString();
    }

    private String export(IOverlay overlay, XmlSerializer xmlSerializer)
            throws IOException {

        StringWriter writer = new StringWriter();

        xmlSerializer.setOutput(writer);
        // start DOCUMENT
        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.startTag(null, "kml");
        xmlSerializer.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");

        xmlSerializer.startTag(null, "Document");

        if ((null != overlay.getDataProviderId()) && !overlay.getDataProviderId().isEmpty()) {
            xmlSerializer.attribute(null, "id", overlay.getDataProviderId());
        } else {
            xmlSerializer.attribute(null, "id", overlay.getGeoId().toString());
        }

        xmlSerializer.startTag(null, "name");
        if ((null != overlay.getName()) && !overlay.getName().isEmpty()) {
            xmlSerializer.text(overlay.getName());
        } else {
            xmlSerializer.text("EMP Untitled Overlay");
        }
        xmlSerializer.endTag(null, "name");

        if ((null != overlay.getDescription()) && !overlay.getDescription().isEmpty()) {
            xmlSerializer.startTag(null, "description");
            xmlSerializer.text(overlay.getDescription());
            xmlSerializer.endTag(null, "description");
        }

        exportStylesToKML(overlay, xmlSerializer);

        for (IGeoBase geoObject : overlay.getChildren()) {
            if (geoObject instanceof IContainer) {
                exportEmpObjectToKML((IContainer) geoObject, xmlSerializer);
            }
        }

        xmlSerializer.endTag(null, "Document");

        xmlSerializer.endTag(null, "kml");
        xmlSerializer.endDocument();
        xmlSerializer.flush();
        return writer.toString();
    }

    private String export(IFeature feature, XmlSerializer xmlSerializer)
            throws IOException {

        StringWriter writer = new StringWriter();

        xmlSerializer.setOutput(writer);
        // start DOCUMENT
        xmlSerializer.startDocument("UTF-8", true);
        xmlSerializer.startTag(null, "kml");
        xmlSerializer.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");

        xmlSerializer.startTag(null, "Document");

        if ((null != feature.getDataProviderId()) && !feature.getDataProviderId().isEmpty()) {
            xmlSerializer.attribute(null, "id", feature.getDataProviderId());
        } else {
            xmlSerializer.attribute(null, "id", feature.getGeoId().toString());
        }

        xmlSerializer.startTag(null, "name");
        if ((null != feature.getName()) && !feature.getName().isEmpty()) {
            xmlSerializer.text(feature.getName());
        } else {
            xmlSerializer.text("EMP Untitled Overlay");
        }
        xmlSerializer.endTag(null, "name");

        if ((null != feature.getDescription()) && !feature.getDescription().isEmpty()) {
            xmlSerializer.startTag(null, "description");
            xmlSerializer.text(feature.getDescription());
            xmlSerializer.endTag(null, "description");
        }

        exportStylesToKML(feature, xmlSerializer);
        exportEmpObjectToKML(feature, xmlSerializer);

        xmlSerializer.endTag(null, "Document");

        xmlSerializer.endTag(null, "kml");
        xmlSerializer.endDocument();
        xmlSerializer.flush();
        return writer.toString();
    }

    public final static String export(IMap map)
            throws IOException {

        if (null == map) {
            throw new InvalidParameterException("Parameter can't be null.");
        }

        EmpKMLExporter exporter = new EmpKMLExporter();
        return exporter.export(map, Xml.newSerializer());
    }

    public final static String export(IOverlay overlay)
            throws IOException {

        if (null == overlay) {
            throw new InvalidParameterException("Parameter can't be null.");
        }

        EmpKMLExporter exporter = new EmpKMLExporter();
        return exporter.export(overlay, Xml.newSerializer());
    }

    public final static String export(IFeature feature)
            throws IOException {

        if (null == feature) {
            throw new InvalidParameterException("Parameter can't be null.");
        }

        EmpKMLExporter exporter = new EmpKMLExporter();
        return exporter.export(feature, Xml.newSerializer());
    }
}
