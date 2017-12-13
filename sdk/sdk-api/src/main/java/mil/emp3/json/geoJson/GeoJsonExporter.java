package mil.emp3.json.geoJson;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.cmapi.primitives.IGeoTimeSpan;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.GeoJSON;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Point;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ColorUtils;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.api.utils.MilStdUtilities;
import sec.web.render.SECWebRenderer;

import static mil.emp3.api.enums.FeatureTypeEnum.*;

public class GeoJsonExporter extends Thread{

    private static final String TAG = GeoJsonExporter.class.getSimpleName();
    private static final int RENDER_JSON = 1;
    private static SimpleDateFormat zonedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
    static final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    static final private ICoreManager coreManager = ManagerFactory.getInstance().getCoreManager();

    private static final String TEMP_DATAURL_STRING = "temp.dataURL";

    private final boolean addExtendedData;
    private final IMap map;
    private final IFeature feature;
    private final List<IFeature> featureList;
    private final IOverlay overlay;
    private final IEmpExportToStringCallback callback;
    private final MilStdLabelSettingEnum eLabelSetting;
    private final java.util.Set<IGeoMilSymbol.Modifier> oLabels;
    private final static MilStdIconRenderer oIconRenderer = MilStdIconRenderer.getInstance();
    private final static SparseArray<String> emptyArray = new SparseArray<>();
    private IGeoIconStyle tempIconStyle = new GeoIconStyle();

    public void appendGeoJSONColor(IGeoColor color, StringBuffer buffer) {
        buffer.append("\"color\": {\"r\":");
        buffer.append(color.getRed());
        buffer.append(", \"g\":");
        buffer.append(color.getGreen());
        buffer.append(", \"b\":");
        buffer.append(color.getBlue());
        buffer.append(", \"a\":");
        buffer.append(color.getAlpha());
        buffer.append("}");
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

    private void appendCircle(final Circle feature, StringBuffer buffer) throws IOException {
        IEmpBoundingBox bBox = feature.getFeatureBoundingBox();
        String boundingBoxStr = bBox.getWest() + "," + bBox.getSouth() + "," + bBox.getEast() + "," + bBox.getNorth();
        String coordinateStr = convertPositionsToString(feature.getPositions());

        double scale = 636000.0;

        SparseArray<String> modifiers = new SparseArray<>();
        SparseArray<String> attributes = getAttributes(feature,
                this.map.isSelected(feature), this.map.getSelectedStrokeStyle().getStrokeColor(),
                this.map.getSelectedLabelStyle().getColor());
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());

        modifiers.put(ModifiersTG.AM_DISTANCE, feature.getRadius() + "");

        String geoJSON = SECWebRenderer.RenderSymbol(
                feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                "PBS_CIRCLE-----", coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, RENDER_JSON, 0);
        buffer.append(geoJSON);
    }

    private void appendEllipse(final Ellipse feature, StringBuffer buffer) throws IOException {
        IEmpBoundingBox bBox = feature.getFeatureBoundingBox();
        String boundingBoxStr = bBox.getWest() + "," + bBox.getSouth() + "," + bBox.getEast() + "," + bBox.getNorth();
        String coordinateStr = convertPositionsToString(feature.getPositions());

        double scale = 636000.0;

        SparseArray<String> modifiers = new SparseArray<>();
        SparseArray<String> attributes = getAttributes(feature,
                this.map.isSelected(feature), this.map.getSelectedStrokeStyle().getStrokeColor(),
                this.map.getSelectedLabelStyle().getColor());
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());

        modifiers.put(ModifiersTG.AM_DISTANCE, feature.getSemiMinor() + "," + feature.getSemiMajor());
        modifiers.put(ModifiersTG.AN_AZIMUTH, feature.getAzimuth() + "");

        String geoJSON = SECWebRenderer.RenderSymbol(
                feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                "PBS_ELLIPSE----", coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, RENDER_JSON, 0);
        buffer.append(geoJSON);
    }

    private void appendRectangle(final Rectangle feature, StringBuffer buffer) throws IOException {
        IEmpBoundingBox bBox = feature.getFeatureBoundingBox();
        String boundingBoxStr = bBox.getWest() + "," + bBox.getSouth() + "," + bBox.getEast() + "," + bBox.getNorth();
        String coordinateStr = convertPositionsToString(feature.getPositions());

        double scale = 636000.0;

        SparseArray<String> modifiers = new SparseArray<>();
        SparseArray<String> attributes = getAttributes(feature,
                this.map.isSelected(feature), this.map.getSelectedStrokeStyle().getStrokeColor(),
                this.map.getSelectedLabelStyle().getColor());
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());

        modifiers.put(ModifiersTG.AM_DISTANCE, feature.getWidth() + "," + feature.getHeight());
        modifiers.put(ModifiersTG.AN_AZIMUTH, feature.getAzimuth() + "");

        String geoJSON = SECWebRenderer.RenderSymbol(
                feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                "PBS_RECTANGLE--", coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, RENDER_JSON, 0);
        buffer.append(geoJSON);
    }

    private void appendSquare(final Square feature, StringBuffer buffer) throws IOException {
        IEmpBoundingBox bBox = feature.getFeatureBoundingBox();
        String boundingBoxStr = bBox.getWest() + "," + bBox.getSouth() + "," + bBox.getEast() + "," + bBox.getNorth();
        String coordinateStr = convertPositionsToString(feature.getPositions());

        double scale = 636000.0;

        SparseArray<String> modifiers = new SparseArray<>();
        SparseArray<String> attributes = getAttributes(feature,
                this.map.isSelected(feature), this.map.getSelectedStrokeStyle().getStrokeColor(),
                this.map.getSelectedLabelStyle().getColor());
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());

        modifiers.put(ModifiersTG.AM_DISTANCE, feature.getWidth() + "");
        modifiers.put(ModifiersTG.AN_AZIMUTH, feature.getAzimuth() + "");

        String geoJSON = SECWebRenderer.RenderSymbol(
                feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                "PBS_SQUARE-----", coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, RENDER_JSON, 0);
        buffer.append(geoJSON);
    }


    private void appendGeoJSONPositions(IFeature feature, StringBuffer buffer) {
        boolean addComma = false;
        for (IGeoPosition position : feature.getPositions()) {
            if (addComma) {
                buffer.append(",\n");
            } else {
                addComma = true;
            }
            buffer.append("[");
            buffer.append(position.getLongitude());
            buffer.append(", ");
            buffer.append(position.getLatitude());
            buffer.append("]");
        }
    }

    private void appendGeoJSONOtherProperties(IFeature feature, StringBuffer buffer) {
        boolean haveTimeSpan = false;
        if (feature.getTimeSpans().size() > 0) {
            haveTimeSpan = true;
            buffer.append(",\n\"timePrimitive\": {");
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
        buffer.append(",\n\"name\":");
        buffer.append("\"" + feature.getName() + "\"");
        buffer.append(",\n\"id\":");
        buffer.append("\"" + feature.getGeoId() + "\"");
        buffer.append(",\n\"description\":");
        buffer.append("\"" + feature.getDescription() + "\"");
        buffer.append("}");
    }

    private void appendGeoJSONPolygon(IFeature feature, StringBuffer buffer) {
        buffer.append("\"geometry\":  {\"type\": \"Polygon\",");
        buffer.append("\"coordinates\":  [[");
        appendGeoJSONPositions(feature, buffer);
        buffer.append("]]");// end of coordinates
        buffer.append("}");// end of geometry
        buffer.append(",\n\"properties\": {");
        buffer.append("\"style\": {");
        buffer.append("\"lineStyle\": {");
        IGeoColor color = feature.getStrokeStyle().getStrokeColor();
        appendGeoJSONColor(color, buffer);
        buffer.append("}"); // end of lineStyle
        if (feature.getFillStyle() != null) {
            buffer.append(",\"polyStyle\": {");
            color = feature.getFillStyle().getFillColor();
            appendGeoJSONColor(color, buffer);
            buffer.append("}"); // end of polyStyle
        }
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
        buffer.append(",\n\"properties\": {");
        buffer.append("\"style\": {");
        buffer.append("\"lineStyle\": {");
        IGeoColor color = feature.getStrokeStyle().getStrokeColor();
        appendGeoJSONColor(color, buffer);
        buffer.append("}"); // lineStyle
        buffer.append("}"); // style
        appendGeoJSONOtherProperties(feature, buffer);
        buffer.append("}");
    }

    private void appendDataURL(MilStdSymbol feature, StringBuffer buffer) {
        //String iconURL = "[MIL_SYM_SERVICE_URL]/mil-sym-service/renderer/image/" + feature.getSymbolCode();
        String iconURL;

        ImageInfo oImageInfo = null;
        int iconSize = 35; //(int) (this.map.getIconPixelSize() * ratioStdScreenToDeviceDPI);
        SparseArray<String> saModifiers = feature.getUnitModifiers(this.map.getMilStdLabels());
        SparseArray<String> saAttr = feature.getAttributes(iconSize,
                this.map.isSelected(feature),
                this.map.getSelectedStrokeStyle().getStrokeColor(),
                this.map.getSelectedLabelStyle().getColor(),
                this.map.getSelectedLabelStyle().getOutlineColor());

        saAttr.put(MilStdAttributes.UseDashArray, "false");

        iconURL = MilStdUtilities.getMilStdSinglePointIconURL(feature, eLabelSetting, oLabels, saAttr);

        oImageInfo = this.oIconRenderer.RenderIcon(feature.getSymbolCode(), ((saModifiers == null) ? this.emptyArray : saModifiers), ((saAttr == null) ? this.emptyArray : saAttr));

        if (null != oImageInfo) {
            if (this.addExtendedData) {
                Bitmap iconBitmap = oImageInfo.getImage();
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                iconBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();

                String encoded = "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.DEFAULT);

                feature.setProperty(TEMP_DATAURL_STRING, encoded);
            }

            buffer.append("\"url\": {");
            buffer.append(iconURL);
            buffer.append("}"); // url
            buffer.append(",");
            tempIconStyle.setOffSetX(oImageInfo.getCenterPoint().x);
            tempIconStyle.setOffSetY(oImageInfo.getImageBounds().height() - oImageInfo.getCenterPoint().y);
            buffer.append("\"offsetX\": {");
            buffer.append(tempIconStyle.getOffSetX());
            buffer.append("}");
            buffer.append(",");
            buffer.append("\"offsetY\": {");
            buffer.append(tempIconStyle.getOffSetY());
            buffer.append("}");
        }
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
        buffer.append(",\n\"properties\": {");
        buffer.append("\"style\": {");
        buffer.append("\"iconStyle\": {");
        if (feature.getFeatureType() == GEO_POINT) {
            buffer.append("\"url\": {");
            buffer.append("\"" + ((Point) feature).getIconURI() + "\"");
            buffer.append("}"); // url
        } else {
            // must be single point milstd symbol
            appendDataURL((MilStdSymbol)feature, buffer);
        }
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

    public void appendFeature(IFeature feature, StringBuffer buffer) throws IOException {

        buffer.append("{\"type\":  \"Feature\",\n");
        switch (feature.getFeatureType()) {
            case GEO_RECTANGLE:
                appendRectangle((Rectangle)feature, buffer);
                break;
            case GEO_SQUARE:
                appendSquare((Square)feature, buffer);
                break;
            case GEO_CIRCLE:
                appendCircle((Circle)feature, buffer);
                break;
            case GEO_ELLIPSE:
                appendEllipse((Ellipse)feature, buffer);
                break;
            case GEO_POLYGON:
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
                if(((MilStdSymbol)feature).isSinglePoint()) {
                    appendGeoJSONPoint(feature, buffer);
                }
                break;
            default:
                buffer.append("}");
        }
    }

    private void appendFeatureList(List<IFeature> featureList, StringBuffer buffer) throws IOException {
        if (featureList.isEmpty()) {
            return;
        }
        buffer.append("{\"type\":  \"FeatureCollection\",\n");
        buffer.append("\"features\":[\n");
        boolean addComma = false;
        for (IFeature feature : featureList) {
            if (addComma) {
                buffer.append(",\n");
            } else {
                addComma = true;
            }
            appendFeature(feature, buffer);
        }
        buffer.append("]\n}\n");
    }

    private void getAllFeatures(IOverlay overlay, List<IFeature> featureList) {
        for (IOverlay ovl : overlay.getOverlays()) {
            getAllFeatures(ovl, featureList);  // recursive
            featureList.addAll(ovl.getFeatures());
        }
    }

    private void export(IFeature feature, StringBuffer buffer) throws IOException {
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

    private void export(List<IFeature> featureList, StringBuffer buffer) throws IOException {
        appendFeatureList(featureList, buffer);
    }

    private void export(IOverlay overlay, StringBuffer buffer) throws IOException {
        List<IFeature> featureList = new ArrayList<>();
        getAllFeatures(overlay, featureList); // flatten it out
        appendFeatureList(featureList, buffer);
    }

    private void export(IMap map, StringBuffer buffer) throws IOException {
        List<IFeature> featureList = map.getAllFeatures();
        appendFeatureList(featureList, buffer);
    }

    @Override
    public void run() {
        try {
            StringBuffer buffer = new StringBuffer();
            if (feature != null) {
                export(feature, buffer);
            } else if (featureList != null){
                export(featureList, buffer);
            } else if (overlay != null) {
                export(overlay, buffer);
            } else {
                export(map, buffer);
            }
            this.callback.exportSuccess(buffer.toString());
        } catch (Exception Ex) {
            this.callback.exportFailed(Ex);
        }
    }

    protected GeoJsonExporter(IMap map, IFeature feature, boolean extendedData, IEmpExportToStringCallback callback) {
        this.map = map;
        this.feature = feature;
        this.featureList = null;
        this.overlay = null;
        this.callback = callback;
        this.addExtendedData = extendedData;
        this.eLabelSetting = this.map.getMilStdLabels();
        this.oLabels = coreManager.getMilStdModifierLabelList(this.eLabelSetting);
    }

    protected GeoJsonExporter(IMap map, List<IFeature> featureList, boolean extendedData, IEmpExportToStringCallback callback) {
        this.map = map;
        this.featureList = featureList;
        this.overlay = null;
        this.feature = null;
        this.callback = callback;
        this.addExtendedData = extendedData;
        this.eLabelSetting = this.map.getMilStdLabels();
        this.oLabels = coreManager.getMilStdModifierLabelList(this.eLabelSetting);
    }

    protected GeoJsonExporter(IMap map, boolean extendedData, IEmpExportToStringCallback callback) {
        this.map = map;
        this.overlay = null;
        this.featureList = null;
        this.feature = null;
        this.callback = callback;
        this.addExtendedData = extendedData;
        this.eLabelSetting = this.map.getMilStdLabels();
        this.oLabels = coreManager.getMilStdModifierLabelList(this.eLabelSetting);
    }

    protected GeoJsonExporter(IMap map, IOverlay overlay, boolean extendedData, IEmpExportToStringCallback callback) {
        this.map = map;
        this.overlay = overlay;
        this.featureList = null;
        this.feature = null;
        this.callback = callback;
        this.addExtendedData = extendedData;
        this.eLabelSetting = this.map.getMilStdLabels();
        this.oLabels = coreManager.getMilStdModifierLabelList(this.eLabelSetting);
    }

}
