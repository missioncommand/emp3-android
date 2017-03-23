package mil.emp3.json.geoJson;

import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.util.StringBuilderPrinter;
import android.util.Xml;

import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoIconStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoTimeSpan;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import mil.emp3.api.Circle;
import mil.emp3.api.Ellipse;
import mil.emp3.api.GeoJSON;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Point;
import mil.emp3.api.Rectangle;
import mil.emp3.api.Square;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.interfaces.IEmpExportToStringCallback;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;

import static mil.emp3.api.enums.FeatureTypeEnum.*;

public class GeoJsonExporter extends Thread{

    private static final String TAG = GeoJsonExporter.class.getSimpleName();
    private static SimpleDateFormat zonedDate = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZZZZZ");
    static final private IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    static final private ICoreManager coreManager = ManagerFactory.getInstance().getCoreManager();


    private static final String TEMP_DATAURL_STRING = "temp.dataURL";

    private final boolean addExtendedData;
    private final IMap map;
    private final IFeature feature;
    private final List<IFeature> featureList;
    private final IEmpExportToStringCallback callback;
    private final MilStdLabelSettingEnum eLabelSetting;
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

    private void appendGeoJSONPositions(IFeature feature, StringBuffer buffer) {
        List<IGeoPosition> positions = null;
        if (feature.getFeatureType() == GEO_CIRCLE) {
            positions = ((Circle)feature).getPolygonPositionList();
        } else if (feature.getFeatureType() == GEO_ELLIPSE) {
            positions = ((Ellipse)feature).getPolygonPositionList();
        } else if (feature.getFeatureType() == GEO_RECTANGLE) {
            positions = ((Rectangle)feature).getCorners();
        } else if (feature.getFeatureType() == GEO_SQUARE) {
            positions = ((Square)feature).getCorners();
        } else {
            positions = feature.getPositions();
        }
        boolean addComma = false;
        for (IGeoPosition position : positions) {
            if (addComma) {
                buffer.append(",\n");
            } else {
                addComma = true;
            }
            buffer.append("[");
            buffer.append(position.getLatitude());
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

    private String getIconURLParameters(final MilStdSymbol feature, SparseArray<String> saAttr) {
        int iKey;
        String value;
        String UniqueDesignator1 = null;
        String params = "";
        java.util.HashMap<IGeoMilSymbol.Modifier, String> geoModifiers = feature.getModifiers();
        java.util.Set<IGeoMilSymbol.Modifier> oLabels = coreManager.getMilStdModifierLabelList(this.eLabelSetting);

        if (null != saAttr) {
            for (int iIndex = 0; iIndex < saAttr.size(); iIndex++) {
                iKey = saAttr.keyAt(iIndex);
                value = saAttr.valueAt(iIndex);
                switch (iKey) {
                    case MilStdAttributes.SymbologyStandard:
                        if (!params.isEmpty()) {
                            params += "&";
                        }
                        switch (value) {
                            case "0":
                                params += "symStd=2525B";
                                break;
                            case "1":
                            default:
                                params += "symStd=2525C";
                                break;
                        }
                        break;
                    case MilStdAttributes.PixelSize:
                        if (!params.isEmpty()) {
                            params += "&";
                        }
                        params += "Size=" + value;
                        break;
                    case MilStdAttributes.FillColor:
                        if (!params.isEmpty()) {
                            params += "&";
                        }
                        params += "fillColor=" + value;
                        break;
                    case MilStdAttributes.LineColor:
                        if (!params.isEmpty()) {
                            params += "&";
                        }
                        params += "lineColor=" + value;
                        break;
                    case MilStdAttributes.IconColor:
                        if (!params.isEmpty()) {
                            params += "&";
                        }
                        params += "lineColor=" + value;
                        break;
                    case MilStdAttributes.TextColor:
                        if (!params.isEmpty()) {
                            params += "&";
                        }
                        params += "textColor=" + value;
                        break;
                    case MilStdAttributes.FontSize:
                        if (!params.isEmpty()) {
                            params += "&";
                        }
                        params += "tfontSize=" + value;
                        break;
                }
            }
        }
        if ((geoModifiers != null) && !geoModifiers.isEmpty()) {
            java.util.Set<IGeoMilSymbol.Modifier> oModifierList = geoModifiers.keySet();

            for (IGeoMilSymbol.Modifier eModifier: oModifierList) {
                if ((oLabels != null) && !oLabels.contains(eModifier)) {
                    // Its not on the list.
                    continue;
                }
                switch (eModifier) {
                    case SYMBOL_ICON:
                    case ECHELON:
                    case QUANTITY:
                    case TASK_FORCE_INDICATOR:
                    case FRAME_SHAPE_MODIFIER:
                    case REDUCED_OR_REINFORCED:
                    case STAFF_COMMENTS:
                    case ADDITIONAL_INFO_1:
                    case ADDITIONAL_INFO_2:
                    case ADDITIONAL_INFO_3:
                    case EVALUATION_RATING:
                    case COMBAT_EFFECTIVENESS:
                    case SIGNATURE_EQUIPMENT:
                    case HIGHER_FORMATION:
                    case HOSTILE:
                    case IFF_SIF:
                    case DIRECTION_OF_MOVEMENT:
                    case MOBILITY_INDICATOR:
                    case SIGINT_MOBILITY_INDICATOR:
                    case OFFSET_INDICATOR:
                    case UNIQUE_DESIGNATOR_2:
                    case EQUIPMENT_TYPE:
                    case DATE_TIME_GROUP:
                    case DATE_TIME_GROUP_2:
                    case ALTITUDE_DEPTH:
                    case LOCATION:
                    case SPEED:
                    case SPECIAL_C2_HEADQUARTERS:
                    case FEINT_DUMMY_INDICATOR:
                    case INSTALLATION:
                    case PLATFORM_TYPE:
                    case EQUIPMENT_TEARDOWN_TIME:
                    case COMMON_IDENTIFIER:
                    case AUXILIARY_EQUIPMENT_INDICATOR:
                    case AREA_OF_UNCERTAINTY:
                    case DEAD_RECKONING:
                    case SPEED_LEADER:
                    case PAIRING_LINE:
                    case OPERATIONAL_CONDITION:
                    case ENGAGEMENT_BAR:
                    case COUNTRY_CODE:
                    case SONAR_CLASSIFICATION_CONFIDENCE:
                        if (!params.isEmpty()) {
                            params += "&";
                        }
                        params += eModifier.valueOf() + "=" + geoModifiers.get(eModifier);
                        break;
                    case UNIQUE_DESIGNATOR_1:
                        UniqueDesignator1 = geoModifiers.get(eModifier);
                        if (!params.isEmpty()) {
                            params += "&";
                        }
                        params += eModifier.valueOf() + "=" + geoModifiers.get(eModifier);
                        break;
                    case DISTANCE:
                    case AZIMUTH:
                        break;
                }
            }
        }

        if ((getName() != null) && !getName().isEmpty()) {
            if (eLabelSetting != null) {
                switch (eLabelSetting) {
                    case REQUIRED_LABELS:
                        break;
                    case COMMON_LABELS:
                    case ALL_LABELS:
                        if ((UniqueDesignator1 == null) || UniqueDesignator1.isEmpty() || !UniqueDesignator1.toUpperCase().equals(getName().toUpperCase())) {
                            if (!params.isEmpty()) {
                                params += "&";
                            }
                            params += "CN=" + UniqueDesignator1;
                        }
                        break;
                }
            }
        }

        return "?" + params;
    }

    private void appendDataURL(MilStdSymbol feature, StringBuffer buffer) {
        String iconURL = "[MIL_SYM_SERVICE_URL]/mil-sym-service/renderer/image/" + feature.getSymbolCode();

        ImageInfo oImageInfo = null;
        SparseArray<String> saModifiers = feature.getUnitModifiers(this.map.getMilStdLabels());
        SparseArray<String> saAttr = feature.getAttributes(this.map.getIconPixelSize(),
                this.map.isSelected(feature),
                this.map.getSelectedStrokeStyle().getStrokeColor(),
                this.map.getSelectedLabelStyle().getColor());

        iconURL += getIconURLParameters(feature, saAttr);

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

            buffer.append(iconURL);

            // Don't know yet where this should go
            tempIconStyle.setOffSetX(oImageInfo.getCenterPoint().x);
            tempIconStyle.setOffSetY(oImageInfo.getImageBounds().height() - oImageInfo.getCenterPoint().y);
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

        buffer.append("{\"type\":  \"Feature\",\n");
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
                if(((MilStdSymbol)feature).isSinglePoint()) {
                    appendGeoJSONPoint(feature, buffer);
                }
                break;
            default:
                buffer.append("}");
        }
    }

    private void appendFeatureList(List<IFeature> featureList, StringBuffer buffer) {
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

    private void export(List<IFeature> featureList, StringBuffer buffer) {
        appendFeatureList(featureList, buffer);
    }

    @Override
    public void run() {
        try {
            StringBuffer buffer = new StringBuffer();
            if (feature != null) {
                export(feature, buffer);
            } else {
                export(featureList, buffer);
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
        this.callback = callback;
        this.addExtendedData = extendedData;
        this.eLabelSetting = storageManager.getMilStdLabels(this.map);
    }

    protected GeoJsonExporter(IMap map, List<IFeature> featureList, boolean extendedData, IEmpExportToStringCallback callback) {
        this.map = map;
        this.featureList = featureList;
        this.feature = null;
        this.callback = callback;
        this.addExtendedData = extendedData;
        this.eLabelSetting = storageManager.getMilStdLabels(this.map);
    }
}
