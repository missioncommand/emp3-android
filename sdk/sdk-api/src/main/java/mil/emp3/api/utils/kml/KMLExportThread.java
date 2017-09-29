package mil.emp3.api.utils.kml;

import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.util.Xml;

import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.IGeoBase;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.GeoJSON;
import mil.emp3.api.KML;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Path;
import mil.emp3.api.Point;
import mil.emp3.api.Polygon;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.Text;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.global;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ColorUtils;
import mil.emp3.api.utils.FontUtilities;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.api.utils.MilStdUtilities;
import sec.web.render.SECWebRenderer;

/**
 * This class implements the KML export capability in a separate thread.
 */
public class KMLExportThread extends java.lang.Thread {
    private final static String TAG = KMLExportThread.class.getSimpleName();

    static final private ICoreManager coreManager = ManagerFactory.getInstance().getCoreManager();

    private static final String TEMP_DATAURL_STRING = "temp.dataURL";

    private final boolean addExtendedData;
    private final IMap map;
    private final IOverlay overlay;
    private final IFeature feature;
    private final IEmpExportToStringCallback callback;
    private final MilStdLabelSettingEnum eLabelSetting;
    private final java.util.Set<IGeoMilSymbol.Modifier> oLabels;
    private final static MilStdIconRenderer oIconRenderer = MilStdIconRenderer.getInstance();
    private final static SparseArray<String> emptyArray = new SparseArray<>();

    // These were added to dump log into a file. It can be removed when the KML issues are resolved.
    private final boolean dumpToFile = false;
    private FileOutputStream dump = null;

    private interface ISerializePlacemarkGeometry {
        void serializeGeometry(XmlSerializer xmlSerializer)
                throws IOException;
    }

    private void serializeName(IContainer container, XmlSerializer xmlSerializer)
            throws IOException {
        if ((null != container.getName()) && !container.getName().isEmpty()) {
            xmlSerializer.startTag(null, "name");
            xmlSerializer.cdsect(container.getName());
            xmlSerializer.endTag(null, "name");
        }
    }

    private void serializeDescription(IContainer container, XmlSerializer xmlSerializer)
            throws IOException {
        if ((null != container.getDescription()) && !container.getDescription().isEmpty()) {
            xmlSerializer.startTag(null, "description");
            xmlSerializer.cdsect(container.getDescription());
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

    private String convertPositionsToString(List<IGeoPosition> posList) {
        String temp = "";

        if (posList.isEmpty()) {
            return temp;
        }

        for (IGeoPosition pos : posList) {
            if (!temp.isEmpty()) {
                temp += " ";
            }
            temp += pos.getLongitude() + "," + pos.getLatitude() + "," + pos.getAltitude();
        }

        return temp;
    }

    private void serializeCoordinates(List<IGeoPosition> posList, XmlSerializer xmlSerializer)
            throws IOException {
        String temp = "";

        if (posList.isEmpty()) {
            return;
        }

        xmlSerializer.startTag(null, "coordinates");
        xmlSerializer.text(convertPositionsToString(posList));
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
            xmlSerializer.attribute(null, "x", "" + (int) iconStyle.getOffSetX());
            xmlSerializer.attribute(null, "y", "" + (int) iconStyle.getOffSetY());
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

    private void serializePlacemark(IFeature feature, XmlSerializer xmlSerializer, KMLExportThread.ISerializePlacemarkGeometry callback)
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
            throw new IllegalArgumentException("Invalid ISerializePlacemarkGeometry");
        }
        callback.serializeGeometry(xmlSerializer);

        xmlSerializer.endTag(null, "Placemark");
    }

    private String getStyleId(IFeature feature) {
        String id = ((null != feature.getName()) ? feature.getName() : "feature") + "-style-" + feature.getGeoId();

        id = id.replaceAll("[^a-zA-Z0-9_]", "_");
        return id;
    }

    private void serializeColor(IGeoColor color, XmlSerializer xmlSerializer)
            throws IOException {
        xmlSerializer.startTag(null, "color");
        if (null != null) {
            xmlSerializer.text(String.format("%02H%02H%02H%02H", (int) (color.getAlpha() * 255), color.getBlue(), color.getGreen(), color.getRed()));
        } else {
            xmlSerializer.text("FF000000");
        }
        xmlSerializer.endTag(null, "color");
    }

    private void serializeStrokeStyle(IGeoStrokeStyle strokeStyle, XmlSerializer xmlSerializer)
            throws IOException {
        if (null == strokeStyle) {
            return;
        }

        IGeoColor color = strokeStyle.getStrokeColor();

        xmlSerializer.startTag(null, "LineStyle");
        serializeColor(color, xmlSerializer);
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
        serializeColor(color, xmlSerializer);

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
            xmlSerializer.startTag(null, "IconStyle");

            if (feature.getAzimuth() != 0.0) {
                double heading = global.modulus(feature.getAzimuth() + 360.0, 360.0);
                xmlSerializer.startTag(null, "heading");
                xmlSerializer.text("" + heading);
                xmlSerializer.endTag(null, "heading");
            }

            xmlSerializer.startTag(null, "Icon");
            xmlSerializer.startTag(null, "href");
            xmlSerializer.endTag(null, "href");
            xmlSerializer.endTag(null, "Icon");
            xmlSerializer.endTag(null, "IconStyle");

            xmlSerializer.startTag(null, "LabelStyle");

            if (null != feature.getLabelStyle()) {
                IGeoColor color = feature.getLabelStyle().getColor();
                serializeColor(color, xmlSerializer);
            } else {
                serializeColor(null, xmlSerializer);
            }

            xmlSerializer.startTag(null, "scale");
            xmlSerializer.text("1.0");
            xmlSerializer.endTag(null, "scale");
            xmlSerializer.endTag(null, "LabelStyle");

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

            if (feature.getAzimuth() != 0.0) {
                double heading = global.modulus(feature.getAzimuth() + 360.0, 360.0);
                xmlSerializer.startTag(null, "heading");
                xmlSerializer.text("" + heading);
                xmlSerializer.endTag(null, "heading");
            }

            //String iconURL = "[MIL_SYM_SERVICE_URL]/mil-sym-service/renderer/image/" + feature.getSymbolCode();
            String iconURL;

            ImageInfo oImageInfo = null;
            int iconSize = 35;
            SparseArray<String> saModifiers = feature.getUnitModifiers(this.map.getMilStdLabels());
            SparseArray<String> saAttr = feature.getAttributes(iconSize,
                    this.map.isSelected(feature),
                    this.map.getSelectedStrokeStyle().getStrokeColor(),
                    this.map.getSelectedLabelStyle().getColor());

            saAttr.put(MilStdAttributes.UseDashArray, "false");
            saAttr.delete(MilStdAttributes.FontSize); //.put(MilStdAttributes.FontSize, "13"); // 10 pts * 1/72 inch / pts * 96 pixel / inch

            iconURL = getMilStdSinglePointIconURL(feature, eLabelSetting, oLabels, saAttr);

            oImageInfo = this.oIconRenderer.RenderIcon(feature.getSymbolCode(), ((saModifiers == null) ? this.emptyArray : saModifiers), ((saAttr == null) ? this.emptyArray : saAttr));

            xmlSerializer.startTag(null, "Icon");
            serializeHRef(iconURL, xmlSerializer);
            xmlSerializer.endTag(null, "Icon");

            if (null != oImageInfo) {
                if (this.addExtendedData) {
                    //Bitmap iconBitmap = oImageInfo.getImage();
                    //ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    //iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                    //byte[] byteArray = byteArrayOutputStream.toByteArray();

                    //String encoded = "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);

                    //feature.setProperty(TEMP_DATAURL_STRING, encoded);
                }

                xmlSerializer.startTag(null, "hotSpot");
                xmlSerializer.attribute(null, "x", "" + (float) oImageInfo.getCenterPoint().x / (float) oImageInfo.getImageBounds().width());
                xmlSerializer.attribute(null, "y", "" + (1.0 - ((float) oImageInfo.getCenterPoint().y / (float) oImageInfo.getImageBounds().height())));
                xmlSerializer.attribute(null, "xunits", "fraction");
                xmlSerializer.attribute(null, "yunits", "fraction");
                xmlSerializer.endTag(null, "hotSpot");
            }


            xmlSerializer.endTag(null, "IconStyle");
            xmlSerializer.endTag(null, "Style");
        }
    }

    protected String getMilStdSinglePointIconURL(final MilStdSymbol                    feature,
                                                 MilStdLabelSettingEnum                eLabelSetting,
                                                 java.util.Set<IGeoMilSymbol.Modifier> labelSet,
                                                 SparseArray<String>                   attributes)
    {
        return  MilStdUtilities.getMilStdSinglePointIconURL(feature, eLabelSetting, labelSet, attributes);
    }

    private void exportEmpObjectToKML(final Point feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new KMLExportThread.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (needStyle(feature)){
                    exportStylesToKML(feature, xmlSerializer);
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
        serializePlacemark(feature, xmlSerializer, new KMLExportThread.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (needStyle(feature)){
                    exportStylesToKML(feature, xmlSerializer);
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
        serializePlacemark(feature, xmlSerializer, new KMLExportThread.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (needStyle(feature)){
                    exportStylesToKML(feature, xmlSerializer);
                }

                xmlSerializer.startTag(null, "Point");
                serializeExtrude(feature, xmlSerializer);
                serializeAltitudeMode(feature, xmlSerializer);
                serializeCoordinates(feature.getPositions(), xmlSerializer);
                xmlSerializer.endTag(null, "Point");
            }
        });
    }

    public SparseArray<String> getAttributes(final IFeature feature, boolean selected, IGeoColor selectedStrokeColor, IGeoColor selectedTextColor) {
        IGeoColor strokeColor = null;
        IGeoColor textColor = null;
        SparseArray<String> oArray = new SparseArray<>();
        IGeoFillStyle oFillStyle = feature.getFillStyle();
        IGeoStrokeStyle oStrokeStyle = feature.getStrokeStyle();
        IGeoLabelStyle labelStyle = feature.getLabelStyle();

        oArray.put(MilStdAttributes.KeepUnitRatio, "true");
        oArray.put(MilStdAttributes.UseDashArray, "false");

        if (selected) {
            strokeColor = selectedStrokeColor;
            textColor = selectedTextColor;
        } else {
            if (oStrokeStyle != null) {
                strokeColor = oStrokeStyle.getStrokeColor();
            }
            if (labelStyle != null) {
                textColor = labelStyle.getColor();
            }
        }

        if (oFillStyle != null) {
            oArray.put(MilStdAttributes.FillColor, "#" + ColorUtils.colorToString(oFillStyle.getFillColor()));
        }

        if (oStrokeStyle != null) {
            oArray.put(MilStdAttributes.LineColor, "#" + ColorUtils.colorToString(oStrokeStyle.getStrokeColor()));
            oArray.put(MilStdAttributes.LineWidth, "" + (int) oStrokeStyle.getStrokeWidth());
        }

        if (strokeColor != null) {
            oArray.put(MilStdAttributes.LineColor, "#" + ColorUtils.colorToString(strokeColor));
        }

        if (textColor != null) {
            oArray.put(MilStdAttributes.TextColor, "#" + ColorUtils.colorToString(textColor));
            // There is currently no way to change the font.
        }

        return oArray;
    }

    private void exportEmpObjectToKML(final Circle feature, XmlSerializer xmlSerializer) throws IOException {
        IEmpBoundingBox bBox = feature.getFeatureBoundingBox();
        String boundingBoxStr = bBox.getWest() + "," + bBox.getSouth() + "," + bBox.getEast() + "," + bBox.getNorth();
        String coordinateStr = convertPositionsToString(feature.getPositions());

        double scale = 636000.0;

        SparseArray<String> modifiers = new SparseArray<>();
        SparseArray<String> attributes = getAttributes(feature,
                KMLExportThread.this.map.isSelected(feature), KMLExportThread.this.map.getSelectedStrokeStyle().getStrokeColor(),
                KMLExportThread.this.map.getSelectedLabelStyle().getColor());
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());

        modifiers.put(ModifiersTG.AM_DISTANCE, feature.getRadius() + "");

        String kml = SECWebRenderer.RenderSymbol(
                feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                "PBS_CIRCLE-----", coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, 0, 0);

        //Log.i(TAG, kmlTG);
        int iIndex = kml.indexOf("<Folder");

        if (iIndex == -1) {
            //xmlSerializer.comment("ERROR: " + kmlTG);
            Log.e(TAG, feature.getName() + " Renderer Error: " + kml);
        } else {
            try {
                insertXMLString(feature, kml, xmlSerializer);
            } catch (Exception e) {
                throw new IOException("Failed to insert renderer KML output. " + kml, e);
            }
        }
    }

    private void exportEmpObjectToKML(final Ellipse feature, XmlSerializer xmlSerializer) throws IOException {
        IEmpBoundingBox bBox = feature.getFeatureBoundingBox();
        String boundingBoxStr = bBox.getWest() + "," + bBox.getSouth() + "," + bBox.getEast() + "," + bBox.getNorth();
        String coordinateStr = convertPositionsToString(feature.getPositions());

        double scale = 636000.0;

        SparseArray<String> modifiers = new SparseArray<>();
        SparseArray<String> attributes = getAttributes(feature,
                KMLExportThread.this.map.isSelected(feature), KMLExportThread.this.map.getSelectedStrokeStyle().getStrokeColor(),
                KMLExportThread.this.map.getSelectedLabelStyle().getColor());
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());

        modifiers.put(ModifiersTG.AM_DISTANCE, feature.getSemiMinor() + "," + feature.getSemiMajor());
        modifiers.put(ModifiersTG.AN_AZIMUTH, feature.getAzimuth() + "");

        String kml = SECWebRenderer.RenderSymbol(
                feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                "PBS_ELLIPSE----", coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, 0, 0);

        //Log.i(TAG, kmlTG);
        int iIndex = kml.indexOf("<Folder");

        if (iIndex == -1) {
            //xmlSerializer.comment("ERROR: " + kmlTG);
            Log.e(TAG, feature.getName() + " Renderer Error: " + kml);
        } else {
            try {
                insertXMLString(feature, kml, xmlSerializer);
            } catch (Exception e) {
                throw new IOException("Failed to insert renderer KML output. " + kml, e);
            }
        }
    }

    private void exportEmpObjectToKML(final Polygon feature, XmlSerializer xmlSerializer) throws IOException {
        serializePlacemark(feature, xmlSerializer, new KMLExportThread.ISerializePlacemarkGeometry() {
            @Override
            public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                if  (polygonNeedStyle(feature)){
                    exportPolygonStylesToKML(feature, xmlSerializer);
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
        IEmpBoundingBox bBox = feature.getFeatureBoundingBox();
        String boundingBoxStr = bBox.getWest() + "," + bBox.getSouth() + "," + bBox.getEast() + "," + bBox.getNorth();
        String coordinateStr = convertPositionsToString(feature.getPositions());

        double scale = 636000.0;

        SparseArray<String> modifiers = new SparseArray<>();
        SparseArray<String> attributes = getAttributes(feature,
                KMLExportThread.this.map.isSelected(feature), KMLExportThread.this.map.getSelectedStrokeStyle().getStrokeColor(),
                KMLExportThread.this.map.getSelectedLabelStyle().getColor());
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());

        modifiers.put(ModifiersTG.AM_DISTANCE, feature.getWidth() + "," + feature.getHeight());
        modifiers.put(ModifiersTG.AN_AZIMUTH, feature.getAzimuth() + "");

        String kml = SECWebRenderer.RenderSymbol(
                feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                "PBS_RECTANGLE--", coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, 0, 0);

        //Log.i(TAG, kmlTG);
        int iIndex = kml.indexOf("<Folder");

        if (iIndex == -1) {
            //xmlSerializer.comment("ERROR: " + kmlTG);
            Log.e(TAG, feature.getName() + " Renderer Error: " + kml);
        } else {
            try {
                insertXMLString(feature, kml, xmlSerializer);
            } catch (Exception e) {
                throw new IOException("Failed to insert renderer KML output. " + kml, e);
            }
        }
    }

    private void exportEmpObjectToKML(final Square feature, XmlSerializer xmlSerializer) throws IOException {
        IEmpBoundingBox bBox = feature.getFeatureBoundingBox();
        String boundingBoxStr = bBox.getWest() + "," + bBox.getSouth() + "," + bBox.getEast() + "," + bBox.getNorth();
        String coordinateStr = convertPositionsToString(feature.getPositions());

        double scale = 636000.0;

        SparseArray<String> modifiers = new SparseArray<>();
        SparseArray<String> attributes = getAttributes(feature,
                KMLExportThread.this.map.isSelected(feature), KMLExportThread.this.map.getSelectedStrokeStyle().getStrokeColor(),
                KMLExportThread.this.map.getSelectedLabelStyle().getColor());
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());

        modifiers.put(ModifiersTG.AM_DISTANCE, feature.getWidth() + "");
        modifiers.put(ModifiersTG.AN_AZIMUTH, feature.getAzimuth() + "");

        String kml = SECWebRenderer.RenderSymbol(
                feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                "PBS_SQUARE-----", coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, 0, 0);

        //Log.i(TAG, kmlTG);
        int iIndex = kml.indexOf("<Folder");

        if (iIndex == -1) {
            //xmlSerializer.comment("ERROR: " + kmlTG);
            Log.e(TAG, feature.getName() + " Renderer Error: " + kml);
        } else {
            try {
                insertXMLString(feature, kml, xmlSerializer);
            } catch (Exception e) {
                throw new IOException("Failed to insert renderer KML output. " + kml, e);
            }
        }
    }

    private void addMilStdModifierExtendedData(final MilStdSymbol feature, XmlSerializer xmlSerializer) throws IOException {
        java.util.HashMap<IGeoMilSymbol.Modifier, String> geoModifiers = feature.getModifiers();
        java.util.Set<IGeoMilSymbol.Modifier> oModifierList = geoModifiers.keySet();

        for (IGeoMilSymbol.Modifier eModifier: oModifierList) {
            xmlSerializer.startTag(null, "Data");
            xmlSerializer.attribute(null, "name", "modifier:" + eModifier.valueOf());
            xmlSerializer.startTag(null, "value");
            xmlSerializer.text("" + geoModifiers.get(eModifier));
            xmlSerializer.endTag(null, "value");
            xmlSerializer.endTag(null, "Data");
        }
    }

    private void addPositionsExtendedData(final List<IGeoPosition> coordList, XmlSerializer xmlSerializer) throws IOException {
        String temp = "";

        if (coordList.isEmpty()) {
            return;
        }

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "positionList");
        for (IGeoPosition pos : coordList) {
            if (!temp.isEmpty()) {
                temp += " ";
            }
            temp += pos.getLongitude() + "," + pos.getLatitude() + "," + pos.getAltitude();
        }
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text(temp);
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");
    }

    private void addColorExtendedData(String prefix, final IGeoColor color, XmlSerializer xmlSerializer) throws IOException {
        if (null == color) {
            return;
        }

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", prefix + ".color.alpha");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (float) color.getAlpha());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", prefix + ".color.red");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + color.getRed());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", prefix + ".color.green");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + color.getGreen());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", prefix + ".color.blue");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + color.getGreen());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");
    }

    private void addStrokeStyleExtendedData(final IGeoStrokeStyle strokeStyle, XmlSerializer xmlSerializer) throws IOException {
        if (null == strokeStyle) {
            return;
        }

        addColorExtendedData("strokeStyle", strokeStyle.getStrokeColor(), xmlSerializer);

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "strokeStyle.strokeWidth");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) strokeStyle.getStrokeWidth());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "strokeStyle.stipplingFactor");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + strokeStyle.getStipplingFactor());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "strokeStyle.stipplingPattern");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + strokeStyle.getStipplingPattern());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");
    }

    private void addFillStyleExtendedData(final IGeoFillStyle fillStyle, XmlSerializer xmlSerializer) throws IOException {
        if (null == fillStyle) {
            return;
        }

        addColorExtendedData("fillStyle", fillStyle.getFillColor(), xmlSerializer);

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "fillStyle.fillPattern");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text(fillStyle.getFillPattern().name());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");
    }

    private void addLabelStyleExtendedData(final IGeoLabelStyle labelStyle, XmlSerializer xmlSerializer) throws IOException {
        if (null == labelStyle) {
            return;
        }

        addColorExtendedData("labelStyle", labelStyle.getColor(), xmlSerializer);
        addColorExtendedData("labelStyle.outline", labelStyle.getOutlineColor(), xmlSerializer);

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "labelStyle.fontFamily");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text(labelStyle.getFontFamily());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "labelStyle.justification");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text(labelStyle.getJustification().name());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "labelStyle.typeFace");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text(labelStyle.getTypeface().name());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "labelStyle.pointSize");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) labelStyle.getSize());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");
    }

    private void addIconStyleExtendedData(final IGeoIconStyle iconStyle, XmlSerializer xmlSerializer) throws IOException {
        if (null == iconStyle) {
            return;
        }

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "iconStyle.size");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) iconStyle.getSize());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "iconStyle.offset.x");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) iconStyle.getOffSetX());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "iconStyle.offset.y");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) iconStyle.getOffSetY());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");
    }

    private void addExtendedData(final MilStdSymbol feature, XmlSerializer xmlSerializer) throws IOException {
        if (!KMLExportThread.this.addExtendedData) {
            return;
        }

        xmlSerializer.startTag(null, "ExtendedData");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "empType");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("milStdSymbol");
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addStrokeStyleExtendedData(feature.getStrokeStyle(), xmlSerializer);
        addFillStyleExtendedData(feature.getFillStyle(), xmlSerializer);
        addLabelStyleExtendedData(feature.getLabelStyle(), xmlSerializer);

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "symbolCode");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text(feature.getSymbolCode());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addMilStdModifierExtendedData(feature, xmlSerializer);
        addPositionsExtendedData(feature.getPositions(), xmlSerializer);

        xmlSerializer.endTag(null, "ExtendedData");
    }

    private void addExtendedData(final Circle feature, XmlSerializer xmlSerializer) throws IOException {
        if (!KMLExportThread.this.addExtendedData) {
            return;
        }

        xmlSerializer.startTag(null, "ExtendedData");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "empType");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("circle");
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addStrokeStyleExtendedData(feature.getStrokeStyle(), xmlSerializer);
        addFillStyleExtendedData(feature.getFillStyle(), xmlSerializer);

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "radius");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) feature.getRadius());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addPositionsExtendedData(feature.getPositions(), xmlSerializer);

        xmlSerializer.endTag(null, "ExtendedData");
    }

    private void addExtendedData(final Ellipse feature, XmlSerializer xmlSerializer) throws IOException {
        if (!KMLExportThread.this.addExtendedData) {
            return;
        }

        xmlSerializer.startTag(null, "ExtendedData");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "empType");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("ellipse");
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addStrokeStyleExtendedData(feature.getStrokeStyle(), xmlSerializer);
        addFillStyleExtendedData(feature.getFillStyle(), xmlSerializer);

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "azimuth");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (float) feature.getAzimuth());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "semiMajor");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) feature.getSemiMajor());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "semiMinor");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) feature.getSemiMinor());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addPositionsExtendedData(feature.getPositions(), xmlSerializer);

        xmlSerializer.endTag(null, "ExtendedData");
    }

    private void addExtendedData(final Rectangle feature, XmlSerializer xmlSerializer) throws IOException {
        if (!KMLExportThread.this.addExtendedData) {
            return;
        }

        xmlSerializer.startTag(null, "ExtendedData");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "empType");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("rectangle");
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addStrokeStyleExtendedData(feature.getStrokeStyle(), xmlSerializer);
        addFillStyleExtendedData(feature.getFillStyle(), xmlSerializer);

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "azimuth");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (float) feature.getAzimuth());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "height");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) feature.getHeight());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "width");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) feature.getWidth());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addPositionsExtendedData(feature.getPositions(), xmlSerializer);

        xmlSerializer.endTag(null, "ExtendedData");
    }

    private void addExtendedData(final Square feature, XmlSerializer xmlSerializer) throws IOException {
        if (!KMLExportThread.this.addExtendedData) {
            return;
        }

        xmlSerializer.startTag(null, "ExtendedData");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "empType");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("rectangle");
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addStrokeStyleExtendedData(feature.getStrokeStyle(), xmlSerializer);
        addFillStyleExtendedData(feature.getFillStyle(), xmlSerializer);

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "azimuth");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (float) feature.getAzimuth());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        xmlSerializer.startTag(null, "Data");
        xmlSerializer.attribute(null, "name", "width");
        xmlSerializer.startTag(null, "value");
        xmlSerializer.text("" + (int) feature.getWidth());
        xmlSerializer.endTag(null, "value");
        xmlSerializer.endTag(null, "Data");

        addPositionsExtendedData(feature.getPositions(), xmlSerializer);

        xmlSerializer.endTag(null, "ExtendedData");
    }

    private void parseTag(final IFeature feature, XmlPullParser parser, XmlSerializer xmlSerializer)
            throws IOException, XmlPullParserException {
        int eventType = parser.getEventType();
        String tagName = parser.getName();

        xmlSerializer.startTag(null, tagName);
        if (parser.getAttributeCount() > 0) {
            // Transfer all the tags attributes.
            for (int iIndex = 0; iIndex < parser.getAttributeCount(); iIndex++) {
                xmlSerializer.attribute(null, parser.getAttributeName(iIndex), parser.getAttributeValue(iIndex));
            }
        }

        if (tagName.equals("Folder")) {
            if (feature instanceof MilStdSymbol) {
                addExtendedData((MilStdSymbol) feature, xmlSerializer);
            } else if (feature instanceof Circle) {
                addExtendedData((Circle) feature, xmlSerializer);
            } else if (feature instanceof Ellipse) {
                addExtendedData((Ellipse) feature, xmlSerializer);
            } else if (feature instanceof Rectangle) {
                addExtendedData((Rectangle) feature, xmlSerializer);
            } else if (feature instanceof Square) {
                addExtendedData((Square) feature, xmlSerializer);
            }
        }

        eventType = parser.nextToken();
        while (!(eventType == XmlPullParser.END_TAG && parser.getName().equals(tagName))) {
            if (eventType == XmlPullParser.START_TAG) {
                parseTag(feature, parser, xmlSerializer);
            } else if (eventType == XmlPullParser.CDSECT) {
                xmlSerializer.cdsect(parser.getText());
            } else if (eventType == XmlPullParser.TEXT) {
                if (!parser.isWhitespace()) {
                    xmlSerializer.text(parser.getText());
                }
            }
            eventType = parser.nextToken();
        }

        xmlSerializer.endTag(null, tagName);
    }

    private void insertXMLString(final IFeature feature, String xmlString, XmlSerializer xmlSerializer)
            throws XmlPullParserException, IOException {
        XmlPullParser parser;

        java.io.StringReader reader = new java.io.StringReader(xmlString);
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();

        factory.setNamespaceAware(true);
        parser = factory.newPullParser();
        parser.setInput(reader);

        int eventType = parser.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                parseTag(feature, parser, xmlSerializer);
            }
            eventType = parser.nextToken();
        }
    }

    private void exportEmpObjectToKML(final MilStdSymbol feature, XmlSerializer xmlSerializer) throws IOException {

        if (feature.isSinglePoint()) {
            serializePlacemark(feature, xmlSerializer, new KMLExportThread.ISerializePlacemarkGeometry() {
                @Override
                public void serializeGeometry(XmlSerializer xmlSerializer) throws IOException {
                    exportStylesToKML(feature, xmlSerializer);

                    addExtendedData(feature, xmlSerializer);

                    xmlSerializer.startTag(null, "Point");
                    serializeExtrude(feature, xmlSerializer);
                    serializeAltitudeMode(feature, xmlSerializer);
                    serializeCoordinates(feature.getPositions(), xmlSerializer);
                    xmlSerializer.endTag(null, "Point");
                }
            });
        } else {
            IEmpBoundingBox bBox = feature.getFeatureBoundingBox();
            String boundingBoxStr = bBox.getWest() + "," + bBox.getSouth() + "," + bBox.getEast() + "," + bBox.getNorth();
            String coordinateStr = convertPositionsToString(feature.getPositions());

            double scale = 636000.0;

            SparseArray<String> modifiers = feature.getTGModifiers(KMLExportThread.this.eLabelSetting);
            SparseArray<String> attributes = feature.getAttributes(KMLExportThread.this.map.getIconPixelSize(),
                    KMLExportThread.this.map.isSelected(feature), KMLExportThread.this.map.getSelectedStrokeStyle().getStrokeColor(),
                    KMLExportThread.this.map.getSelectedLabelStyle().getColor());
            String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());

            attributes.put(MilStdAttributes.UseDashArray, "false");


            String kmlTG = SECWebRenderer.RenderSymbol(
                    feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                    feature.getSymbolCode(), coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                    modifiers, attributes, 0, feature.geoMilStdVersionToRendererVersion());

            if (this.dumpToFile) {
                dump.write(("\nID:" + feature.getGeoId().toString() + "\nName:" + feature.getName() + "\nDescription:" + feature.getDescription() + "\nSymbolCode:" + feature.getSymbolCode() + "\nCoordinate:" + coordinateStr + "\nAltitudeMode:" + altitudeModeStr + "\nScale:" + scale + "\nBBox:" + boundingBoxStr + "\nModifiers:" + modifiers.toString() + "\nAttributes:" + attributes.toString() + "\nformat:0" + "\nVersion:" + feature.geoMilStdVersionToRendererVersion()).getBytes());
                dump.write(("\nKML output (" + kmlTG.length() + "):\n" + kmlTG).getBytes());
            }

            int iIndex = kmlTG.indexOf("<Folder");

            if (iIndex == -1) {
                //xmlSerializer.comment("ERROR: " + kmlTG);
                Log.e(TAG, feature.getName() + " Renderer Error: " + kmlTG);
            } else {
                try {
                    insertXMLString(feature, kmlTG, xmlSerializer);
                } catch (Exception e) {
                    throw new IOException("Failed to insert renderer KML output. " + kmlTG, e);
                }
            }
        }
    }

    private void exportEmpObjectToKML(final IContainer container, XmlSerializer xmlSerializer) throws IOException {
        if (container instanceof Point) {
            exportEmpObjectToKML((Point) container, xmlSerializer);
        } else if (container instanceof Path) {
            exportEmpObjectToKML((Path) container, xmlSerializer);
        } else if (container instanceof Polygon) {
            exportEmpObjectToKML((Polygon) container, xmlSerializer);
        } else if (container instanceof MilStdSymbol) {
            exportEmpObjectToKML((MilStdSymbol) container, xmlSerializer);
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
                if (geoObject instanceof IContainer) {
                    exportEmpObjectToKML((IContainer) geoObject, xmlSerializer);
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

        xmlSerializer.comment("This KML document has been generated by the Extensible Mapping Platform V3.");

        xmlSerializer.startTag(null, "name");
        if ((null != map.getName()) && !map.getName().isEmpty()) {
            xmlSerializer.cdsect(map.getName());
        } else {
            xmlSerializer.text("EMP Untitled Map");
        }
        xmlSerializer.endTag(null, "name");

        if ((null != map.getDescription()) && !map.getDescription().isEmpty()) {
            xmlSerializer.startTag(null, "description");
            xmlSerializer.cdsect(map.getDescription());
            xmlSerializer.endTag(null, "description");
        }

        //exportStylesToKML(map, xmlSerializer);

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

        xmlSerializer.comment("This KML document has been generated by the Extensible Mapping Platform V3.");

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

        //exportStylesToKML(overlay, xmlSerializer);

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

        xmlSerializer.comment("This KML document has been generated by the Extensible Mapping Platform V3.");

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

        //exportStylesToKML(feature, xmlSerializer);
        exportEmpObjectToKML(feature, xmlSerializer);

        xmlSerializer.endTag(null, "Document");

        xmlSerializer.endTag(null, "kml");
        xmlSerializer.endDocument();
        xmlSerializer.flush();
        return writer.toString();
    }

    @Override
    public void run() {
        try {
            String kmlString = "";

            XmlSerializer xmlSerializer = Xml.newSerializer();

            if (null != this.overlay) {
                kmlString = export(this.overlay, xmlSerializer);
            } else if (null != this.feature) {
                kmlString = export(this.feature, xmlSerializer);
            } else if (null != this.map) {
                kmlString = export(this.map, xmlSerializer);
            }
            try {
                this.callback.exportSuccess(kmlString);
            } catch (Exception Ex) {
                Log.e(TAG, "Exception raised in export callback.", Ex);
            }
        } catch (Exception Ex) {
            this.callback.exportFailed(Ex);
        }

        if (this.dumpToFile && (null != dump)) {
            try {
                dump.flush();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (dump != null) {
                        dump.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            dump = null;
        }
    }

    protected KMLExportThread(IMap map, boolean extendedData, IEmpExportToStringCallback callback) {
        this.map = map;
        this.overlay = null;
        this.feature = null;
        this.callback = callback;
        this.addExtendedData = extendedData;
        this.eLabelSetting = this.map.getMilStdLabels();
        this.oLabels = coreManager.getMilStdModifierLabelList(this.eLabelSetting);
        this.setUp();
    }

    protected KMLExportThread(IMap map, IOverlay overlay, boolean extendedData, IEmpExportToStringCallback callback) {
        this.map = map;
        this.overlay = overlay;
        this.feature = null;
        this.callback = callback;
        this.addExtendedData = extendedData;
        this.eLabelSetting = this.map.getMilStdLabels();
        this.oLabels = coreManager.getMilStdModifierLabelList(this.eLabelSetting);
        this.setUp();
    }

    protected KMLExportThread(IMap map, IFeature feature, boolean extendedData, IEmpExportToStringCallback callback) {
        this.map = map;
        this.overlay = null;
        this.feature = feature;
        this.callback = callback;
        this.addExtendedData = extendedData;
        this.eLabelSetting = this.map.getMilStdLabels();
        this.oLabels = coreManager.getMilStdModifierLabelList(this.eLabelSetting);
        this.setUp();
    }

    private void setUp() {
        this.setName("KML Export Thread");

        if (dumpToFile) {
            File sd = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
            File dest = new File(sd, "tg-kml-gen.txt");
            if (dest.exists()) {
                dest.delete();
            }
            try {
                dump = new FileOutputStream(dest);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                dump = null;
            }
        }
    }
}
