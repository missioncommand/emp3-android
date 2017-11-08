package mil.emp3.api.utils;

import android.util.SparseArray;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoMilSymbol;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import armyc2.c2sd.renderer.utilities.ImageInfo;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.utils.kml.KMLExportThread;

/**
 * This static class implements many mil std utility functions.
 */
public class MilStdUtilities {
    /**
     * This method converts a IGeoMilSymbol.SymbolStandard enumerated value to a
     * MilStd Renderer symbol version value.
     * @param eStandard see IGeoMilSymbol.SymbolStandard.
     * @return an integer value indicating the standard version.
     */
    public static int geoMilStdVersionToRendererVersion(IGeoMilSymbol.SymbolStandard eStandard) {
        int iVersion = RendererSettings.Symbology_2525Bch2_USAS_13_14;
        
        switch (eStandard) {
            case MIL_STD_2525C:
                iVersion = RendererSettings.Symbology_2525C;
                break;
            case MIL_STD_2525B :
                iVersion = RendererSettings.Symbology_2525Bch2_USAS_13_14;
                break;
        }
        
        return iVersion;
    }

    /**
     * This method converts a Geo Altitude mode enum value to a MilStd renderer altitude mode.
     * @param mode
     * @return
     */
    public static String geoAltitudeModeToString(IGeoAltitudeMode.AltitudeMode mode) {
        switch (mode) {
            case CLAMP_TO_GROUND:
                return "clampToGround";
            case RELATIVE_TO_GROUND:
                return "relativeToGround";
            case ABSOLUTE:
                return "absolute";
        }
        return "";
    }

    public static String getMilStdSinglePointParams(final MilStdSymbol feature,
            MilStdLabelSettingEnum eLabelSetting, java.util.Set<IGeoMilSymbol.Modifier> labelSet,
            SparseArray<String> attributes) {
        int iKey;
        String value;
        String UniqueDesignator1 = null;
        String params = "";
        java.util.HashMap<IGeoMilSymbol.Modifier, String> geoModifiers = feature.getModifiers();

        if (null != attributes) {
            for (int iIndex = 0; iIndex < attributes.size(); iIndex++) {
                iKey = attributes.keyAt(iIndex);
                value = attributes.valueAt(iIndex);
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
                        params += "fontSize=" + value;
                        break;
                }
            }
        }
        if ((geoModifiers != null) && !geoModifiers.isEmpty()) {
            java.util.Set<IGeoMilSymbol.Modifier> oModifierList = geoModifiers.keySet();

            for (IGeoMilSymbol.Modifier eModifier: oModifierList) {
                if ((labelSet != null) && !labelSet.contains(eModifier)) {
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

        if ((feature.getName() != null) && !feature.getName().isEmpty()) {
            if (eLabelSetting != null) {
                switch (eLabelSetting) {
                    case REQUIRED_LABELS:
                        break;
                    case COMMON_LABELS:
                    case ALL_LABELS:
                        if ((UniqueDesignator1 == null) || UniqueDesignator1.isEmpty() || !UniqueDesignator1.toUpperCase().equals(feature.getName().toUpperCase())) {
                            if (!params.isEmpty()) {
                                params += "&";
                            }
                            params += "CN=" + feature.getName();
                        }
                        break;
                }
            }
        }
        return params;
    }

    public static String getMilStdSinglePointIconURL(final MilStdSymbol feature,
            MilStdLabelSettingEnum eLabelSetting, java.util.Set<IGeoMilSymbol.Modifier> labelSet,
            SparseArray<String> attributes)
    {
        return "http://localhost:8080/mil-sym-service/renderer/image/" + feature.getSymbolCode() + "?" + getMilStdSinglePointParams(feature, eLabelSetting, labelSet, attributes);
    }
}
