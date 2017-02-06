package mil.emp3.api.utils.kml;

import android.util.Xml;

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
import java.util.List;

import mil.emp3.api.Overlay;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.abstracts.Map;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IKMLExportable;

/**
 * This class implements the KML export capability for EMP.
 */

public class EmpKMLExporter {
    private static String TAG = EmpKMLExporter.class.getSimpleName();

    public interface ISerializePlacemarkGeometry {
        void serializeGeometry(XmlSerializer xmlSerializer)
                throws IOException;
    }

    public final static String export(IKMLExportable exportable)
            throws IOException {

        if (null == exportable) {
            throw new InvalidParameterException("Parameter can't be null.");
        }

        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter writer = new StringWriter();

        try {
            xmlSerializer.setOutput(writer);
            // start DOCUMENT
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.startTag(null, "kml");
            xmlSerializer.attribute(null, "xmlns", "http://www.opengis.net/kml/2.2");

            xmlSerializer.startTag(null, "Document");

            if ((null != exportable.getDataProviderId()) && !exportable.getDataProviderId().isEmpty()) {
                xmlSerializer.attribute(null, "id", exportable.getDataProviderId());
            } else {
                xmlSerializer.attribute(null, "id", exportable.getGeoId().toString());
            }

            if (exportable instanceof Overlay) {
                xmlSerializer.startTag(null, "name");
                if ((null != exportable.getName()) && !exportable.getName().isEmpty()) {
                    xmlSerializer.text(exportable.getName());
                } else {
                    xmlSerializer.text("EMP Untitled Overlay");
                }
                xmlSerializer.endTag(null, "name");
            } else {
                xmlSerializer.startTag(null, "name");
                xmlSerializer.text("Extensible Mapping Platform KML Export");
                xmlSerializer.endTag(null, "name");
            }

            if ((null != exportable.getDescription()) && !exportable.getDescription().isEmpty()) {
                xmlSerializer.startTag(null, "description");
                xmlSerializer.text(exportable.getDescription());
                xmlSerializer.endTag(null, "description");
            }

            exportable.exportStylesToKML(xmlSerializer);
            if ((exportable instanceof Overlay) || (exportable instanceof Map)) {
                for (IGeoBase geoObject : exportable.getChildren()) {
                    if (geoObject instanceof IKMLExportable) {
                        ((IKMLExportable) geoObject).exportEmpObjectToKML(xmlSerializer);
                    }
                }
            } else {
                exportable.exportEmpObjectToKML(xmlSerializer);
            }

            xmlSerializer.endTag(null, "Document");

            xmlSerializer.endTag(null, "kml");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            return writer.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return "";
    }

    public static void serializeName(IContainer container, XmlSerializer xmlSerializer)
            throws IOException {
        if ((null != container.getName()) && !container.getName().isEmpty()) {
            xmlSerializer.startTag(null, "name");
            xmlSerializer.text(container.getName());
            xmlSerializer.endTag(null, "name");
        }
    }

    public static void serializeDescription(IContainer container, XmlSerializer xmlSerializer)
            throws IOException {
        if ((null != container.getDescription()) && !container.getDescription().isEmpty()) {
            xmlSerializer.startTag(null, "description");
            xmlSerializer.text(container.getDescription());
            xmlSerializer.endTag(null, "description");
        }
    }

    public static void serializeVisibility(IContainer container, XmlSerializer xmlSerializer)
            throws IOException {
        xmlSerializer.startTag(null, "visibility");
        xmlSerializer.text("1");
        xmlSerializer.endTag(null, "visibility");
    }

    public static void serializeExtrude(IFeature feature, XmlSerializer xmlSerializer)
            throws IOException {
        if (feature.getExtrude()) {
            xmlSerializer.startTag(null, "extrude");
            xmlSerializer.text("1");
            xmlSerializer.endTag(null, "extrude");
        }
    }

    public static void serializeCoordinates(List<IGeoPosition> coorList, XmlSerializer xmlSerializer)
            throws IOException {
        boolean firstAdded = false;
        String temp = "";

        if (coorList.isEmpty()) {
            return;
        }

        xmlSerializer.startTag(null, "coordinates");
        for (IGeoPosition pos : coorList) {
            if (firstAdded) {
                temp = " ";
            } else {
                temp = "";
            }
            temp += pos.getLongitude() + "," + pos.getLatitude() + "," + pos.getAltitude();
        }
        xmlSerializer.text(temp);
        xmlSerializer.endTag(null, "coordinates");

    }

    public static void serializeAltitudeMode(IFeature feature, XmlSerializer xmlSerializer)
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

    public static void serializeIconHotSpot(IGeoIconStyle iconStyle, XmlSerializer xmlSerializer)
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

    public static void serializeHRef(String URI, XmlSerializer xmlSerializer)
            throws IOException {
        if ((null != URI) && !URI.isEmpty()) {
            xmlSerializer.startTag(null, "href");
            xmlSerializer.text(URI);
            xmlSerializer.endTag(null, "href");
        }
    }

    public static void serializePlacemark(IFeature feature, XmlSerializer xmlSerializer, ISerializePlacemarkGeometry callback)
            throws IOException {
        xmlSerializer.startTag(null, "Placemark");
        if ((null != feature.getDataProviderId()) && !feature.getDataProviderId().isEmpty()) {
            xmlSerializer.attribute(null, "id", feature.getDataProviderId());
        } else {
            xmlSerializer.attribute(null, "id", feature.getGeoId().toString());
        }

        EmpKMLExporter.serializeName(feature, xmlSerializer);
        EmpKMLExporter.serializeDescription(feature, xmlSerializer);
        EmpKMLExporter.serializeVisibility(feature, xmlSerializer);


        if (null == callback) {
            throw new InvalidParameterException("Invalid ISerializePlacemarkGeometry");
        }
        callback.serializeGeometry(xmlSerializer);

        xmlSerializer.endTag(null, "Placemark");
    }

    public static String getStyleId(IFeature feature) {
        String id = ((null != feature.getName()) ? feature.getName() : "feature") + "-style-" + feature.getGeoId();

        id = id.replaceAll("[^a-zA-Z0-9_]", "_");
        return id;
    }

    public static void serializeStrokeStyle(IGeoStrokeStyle strokeStyle, XmlSerializer xmlSerializer)
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

    public static void serializeFillStyle(IGeoFillStyle fillStyle, boolean outline, XmlSerializer xmlSerializer)
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
}
