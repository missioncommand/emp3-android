package mil.emp3.core.utils;

import android.util.Log;
import android.util.SparseArray;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import armyc2.c2sd.graphics2d.Point2D;
import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.ShapeInfo;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.Path;
import mil.emp3.api.Polygon;
import mil.emp3.api.Text;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpBoundingArea;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.FontUtilities;
import mil.emp3.core.utils.milstd2525.MilStdUtilities;
import mil.emp3.core.utils.milstd2525.icons.BitmapCacheFactory;
import mil.emp3.core.utils.milstd2525.icons.IBitmapCache;
import mil.emp3.mapengine.interfaces.IEmpImageInfo;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import sec.web.render.SECWebRenderer;

/**
 *
 * This class implements the rendering of the MilStd features.
 */
public class MilStdRenderer implements IMilStdRenderer {
    private static final String TAG = MilStdRenderer.class.getSimpleName();

    public static final String METOC_PRESSURE_INSTABILITY_LINE = "WA-DPXIL---L---";
    public static final String METOC_PRESSURE_SHEAR_LINE = "WA-DPXSH---L---";
    public static final String METOC_BOUNDED_AREAS_OF_WEATHER_LIQUID_PRECIPITATION_NON_CONVECTIVE_CONTINUOUS_OR_INTERMITTENT = "WA-DBALPC---A--";
    public static final String METOC_ATMOSPHERIC_BOUNDED_AREAS_OF_WEATHER_THUNDERSTORMS = "WA-DBAT-----A--";
    private final static int DEFAULT_STROKE_WIDTH = 3;

    private IStorageManager storageManager;

    private static String sRendererCacheDir = null;
    private int getMemoryClass = 0;
    private static java.util.Set<IGeoMilSymbol.Modifier> oRequiredLabels = new java.util.HashSet<>();
    private static java.util.Set<IGeoMilSymbol.Modifier> oCommonLabels = new java.util.HashSet<>();
    private static IBitmapCache oBitmapCache = null;

    private boolean initialized;

    private void initCheck() {
        if (!initialized) {
            init();
            initialized = true;
        }
    }

    public void init() {
        Log.d(TAG, "init");

        // These modifiers, if provided in the MilStd feature, must be shown
        // along with the icon. They are the only modifiers displayed when the label setting is REQUIRED.
        oRequiredLabels.add(IGeoMilSymbol.Modifier.EQUIPMENT_TYPE);
        oRequiredLabels.add(IGeoMilSymbol.Modifier.SIGNATURE_EQUIPMENT);
        oRequiredLabels.add(IGeoMilSymbol.Modifier.OFFSET_INDICATOR);
        oRequiredLabels.add(IGeoMilSymbol.Modifier.SPECIAL_C2_HEADQUARTERS);
        oRequiredLabels.add(IGeoMilSymbol.Modifier.FEINT_DUMMY_INDICATOR);
        oRequiredLabels.add(IGeoMilSymbol.Modifier.INSTALLATION);

        // These modifiers, if provided in the MilStd feature, are to be displayed with the icon
        // when the label setting is COMMON.
        oCommonLabels.addAll(oRequiredLabels);

        oCommonLabels.add(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_1);
        oCommonLabels.add(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_2);
        oCommonLabels.add(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_3);
        oCommonLabels.add(IGeoMilSymbol.Modifier.HIGHER_FORMATION);
        oCommonLabels.add(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1);
        oCommonLabels.add(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_2);



        oBitmapCache = BitmapCacheFactory.instance().getBitMapCache(MilStdRenderer.sRendererCacheDir, getMemoryClass);
    }

    @Override
    public String getBitmapCacheName() {
        if(null == oBitmapCache) {
            throw new IllegalStateException("BitmapCache is not yet initialized");
        }
        Log.i(TAG, "getBitmapCacheName " + oBitmapCache.getClass().getSimpleName());
        return oBitmapCache.getClass().getSimpleName();
    }

    public void setRendererCacheDir(String sCacheDir, int getMemoryClass) {
        if (sRendererCacheDir == null) {
            sRendererCacheDir = sCacheDir;
            this.getMemoryClass = getMemoryClass;
            init();
        }
    }

    @Override
    public void setStorageManager(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * This method returns the list of modifiers provided in the symbol that match the modifiers
     * listed in the label settings.
     * @param mapInstance The map instance making the call.
     * @param symbol the feature.
     * @return
     */
    @Override
    public SparseArray<String> getTGModifiers(IMapInstance mapInstance, MilStdSymbol symbol) {
        initCheck();

        String UniqueDesignator1 = null;
        MilStdLabelSettingEnum eLabelSetting = storageManager.getMilStdLabels(mapInstance);
        SparseArray<String> oArray = new SparseArray<>();
        java.util.HashMap<IGeoMilSymbol.Modifier, String> geoModifiers = symbol.getModifiers();
/*
        java.util.Set<IGeoMilSymbol.Modifier> oLabels = null;

        if (eLabelSetting != null) {
            switch (eLabelSetting) {
                case REQUIRED_LABELS:
                    oLabels = MilStdRenderer.oRequiredLabels;
                    break;
                case COMMON_LABELS:
                    oLabels = MilStdRenderer.oCommonLabels;
                    break;
            }
        }
*/
        if ((geoModifiers != null) && !geoModifiers.isEmpty()) {
            java.util.Set<IGeoMilSymbol.Modifier> oModifierList = geoModifiers.keySet();
            
            for (IGeoMilSymbol.Modifier eModifier: oModifierList) {
/*
                if ((oLabels != null) && !oLabels.contains(eModifier)) {
                    // Its not on the list.
                    continue;
                }
*/
                switch (eModifier) {
                    case SYMBOL_ICON:
                        oArray.put(ModifiersTG.A_SYMBOL_ICON, geoModifiers.get(eModifier));
                        break;
                    case ECHELON:
                        oArray.put(ModifiersTG.B_ECHELON, geoModifiers.get(eModifier));
                        break;
                    case QUANTITY:
                        oArray.put(ModifiersTG.C_QUANTITY, geoModifiers.get(eModifier));
                        break;
                    case TASK_FORCE_INDICATOR:
                        break;
                    case FRAME_SHAPE_MODIFIER:
                        break;
                    case REDUCED_OR_REINFORCED:
                        break;
                    case STAFF_COMMENTS:
                        break;
                    case ADDITIONAL_INFO_1:
                        oArray.put(ModifiersTG.H_ADDITIONAL_INFO_1, geoModifiers.get(eModifier));
                        break;
                    case ADDITIONAL_INFO_2:
                        oArray.put(ModifiersTG.H1_ADDITIONAL_INFO_2, geoModifiers.get(eModifier));
                        break;
                    case ADDITIONAL_INFO_3:
                        oArray.put(ModifiersTG.H2_ADDITIONAL_INFO_3, geoModifiers.get(eModifier));
                        break;
                    case EVALUATION_RATING:
                        break;
                    case COMBAT_EFFECTIVENESS:
                        break;
                    case SIGNATURE_EQUIPMENT:
                        break;
                    case HIGHER_FORMATION:
                        break;
                    case HOSTILE:
                        oArray.put(ModifiersTG.N_HOSTILE, geoModifiers.get(eModifier));
                        break;
                    case IFF_SIF:
                        break;
                    case DIRECTION_OF_MOVEMENT:
                        oArray.put(ModifiersTG.Q_DIRECTION_OF_MOVEMENT, geoModifiers.get(eModifier));
                        break;
                    case MOBILITY_INDICATOR:
                        break;
                    case SIGINT_MOBILITY_INDICATOR:
                        break;
                    case OFFSET_INDICATOR:
                        break;
                    case UNIQUE_DESIGNATOR_1:
                        UniqueDesignator1 = geoModifiers.get(eModifier);
                        oArray.put(ModifiersTG.T_UNIQUE_DESIGNATION_1, UniqueDesignator1);
                        break;
                    case UNIQUE_DESIGNATOR_2:
                        oArray.put(ModifiersTG.T1_UNIQUE_DESIGNATION_2, geoModifiers.get(eModifier));
                        break;
                    case EQUIPMENT_TYPE:
                        oArray.put(ModifiersTG.V_EQUIP_TYPE, geoModifiers.get(eModifier));
                        break;
                    case DATE_TIME_GROUP:
                        oArray.put(ModifiersTG.W_DTG_1, geoModifiers.get(eModifier));
                        break;
                    case DATE_TIME_GROUP_2:
                        oArray.put(ModifiersTG.W1_DTG_2, geoModifiers.get(eModifier));
                        break;
                    case ALTITUDE_DEPTH:
                        oArray.put(ModifiersTG.X_ALTITUDE_DEPTH, geoModifiers.get(eModifier));
                        break;
                    case LOCATION:
                        oArray.put(ModifiersTG.Y_LOCATION, geoModifiers.get(eModifier));
                        break;
                    case SPEED:
                        break;
                    case SPECIAL_C2_HEADQUARTERS:
                        break;
                    case FEINT_DUMMY_INDICATOR:
                        break;
                    case INSTALLATION:
                        break;
                    case PLATFORM_TYPE:
                        break;
                    case EQUIPMENT_TEARDOWN_TIME:
                        break;
                    case COMMON_IDENTIFIER:
                        break;
                    case AUXILIARY_EQUIPMENT_INDICATOR:
                        break;
                    case AREA_OF_UNCERTAINTY:
                        break;
                    case DEAD_RECKONING:
                        break;
                    case SPEED_LEADER:
                        break;
                    case PAIRING_LINE:
                        break;
                    case OPERATIONAL_CONDITION:
                        break;
                    case DISTANCE:
                        oArray.put(ModifiersTG.AM_DISTANCE, geoModifiers.get(eModifier));
                        break;
                    case AZIMUTH:
                        oArray.put(ModifiersTG.AN_AZIMUTH, geoModifiers.get(eModifier));
                        break;
                    case ENGAGEMENT_BAR:
                        break;
                    case COUNTRY_CODE:
                        break;
                    case SONAR_CLASSIFICATION_CONFIDENCE:
                        break;
                }
            }
        }
        

        if ((symbol.getName() != null) && !symbol.getName().isEmpty()) {
            if (eLabelSetting != null) {
                switch (eLabelSetting) {
                    case REQUIRED_LABELS:
                        break;
                    case COMMON_LABELS:
                    case ALL_LABELS:
                        if ((UniqueDesignator1 == null) || UniqueDesignator1.isEmpty() || !UniqueDesignator1.toUpperCase().equals(symbol.getName().toUpperCase())) {
                            oArray.put(ModifiersUnits.CN_CPOF_NAME_LABEL, symbol.getName());
                        }
                        break;
                }
            }
        }

        return oArray;
    }

    /**
     * This method returns the list of modifiers provided in the symbol that match the modifiers
     * listed in the label settings.
     * @param mapInstance The map instance making the call.
     * @param symbol the feature.
     * @return
     */
    @Override
    public SparseArray<String> getUnitModifiers(IMapInstance mapInstance, MilStdSymbol symbol) {
        initCheck();

        if (symbol.isTacticalGraphic()) {
            return this.getTGModifiers(mapInstance, symbol);
        }

        String UniqueDesignator1 = null;
        SparseArray<String> oArray = new SparseArray<>();
        MilStdLabelSettingEnum eLabelSetting = storageManager.getMilStdLabels(mapInstance);
        java.util.HashMap<IGeoMilSymbol.Modifier, String> geoModifiers = symbol.getModifiers();
        java.util.Set<IGeoMilSymbol.Modifier> oLabels = null;

        if (eLabelSetting != null) {
            switch (eLabelSetting) {
                case REQUIRED_LABELS:
                    oLabels = MilStdRenderer.oRequiredLabels;
                    break;
                case COMMON_LABELS:
                    oLabels = MilStdRenderer.oCommonLabels;
                    break;
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
                        oArray.put(ModifiersUnits.A_SYMBOL_ICON, geoModifiers.get(eModifier));
                        break;
                    case ECHELON:
                        oArray.put(ModifiersUnits.B_ECHELON, geoModifiers.get(eModifier));
                        break;
                    case QUANTITY:
                        oArray.put(ModifiersUnits.C_QUANTITY, geoModifiers.get(eModifier));
                        break;
                    case TASK_FORCE_INDICATOR:
                        oArray.put(ModifiersUnits.D_TASK_FORCE_INDICATOR, geoModifiers.get(eModifier));
                        break;
                    case FRAME_SHAPE_MODIFIER:
                        oArray.put(ModifiersUnits.E_FRAME_SHAPE_MODIFIER, geoModifiers.get(eModifier));
                        break;
                    case REDUCED_OR_REINFORCED:
                        oArray.put(ModifiersUnits.F_REINFORCED_REDUCED, geoModifiers.get(eModifier));
                        break;
                    case STAFF_COMMENTS:
                        oArray.put(ModifiersUnits.G_STAFF_COMMENTS, geoModifiers.get(eModifier));
                        break;
                    case ADDITIONAL_INFO_1:
                        oArray.put(ModifiersUnits.H_ADDITIONAL_INFO_1, geoModifiers.get(eModifier));
                        break;
                    case ADDITIONAL_INFO_2:
                        oArray.put(ModifiersUnits.H1_ADDITIONAL_INFO_2, geoModifiers.get(eModifier));
                        break;
                    case ADDITIONAL_INFO_3:
                        oArray.put(ModifiersUnits.H2_ADDITIONAL_INFO_3, geoModifiers.get(eModifier));
                        break;
                    case EVALUATION_RATING:
                        oArray.put(ModifiersUnits.J_EVALUATION_RATING, geoModifiers.get(eModifier));
                        break;
                    case COMBAT_EFFECTIVENESS:
                        oArray.put(ModifiersUnits.K_COMBAT_EFFECTIVENESS, geoModifiers.get(eModifier));
                        break;
                    case SIGNATURE_EQUIPMENT:
                        oArray.put(ModifiersUnits.L_SIGNATURE_EQUIP, geoModifiers.get(eModifier));
                        break;
                    case HIGHER_FORMATION:
                        oArray.put(ModifiersUnits.M_HIGHER_FORMATION, geoModifiers.get(eModifier));
                        break;
                    case HOSTILE:
                        oArray.put(ModifiersUnits.N_HOSTILE, geoModifiers.get(eModifier));
                        break;
                    case IFF_SIF:
                        oArray.put(ModifiersUnits.P_IFF_SIF, geoModifiers.get(eModifier));
                        break;
                    case DIRECTION_OF_MOVEMENT:
                        oArray.put(ModifiersUnits.Q_DIRECTION_OF_MOVEMENT, geoModifiers.get(eModifier));
                        break;
                    case MOBILITY_INDICATOR:
                        oArray.put(ModifiersUnits.R_MOBILITY_INDICATOR, geoModifiers.get(eModifier));
                        break;
                    case SIGINT_MOBILITY_INDICATOR:
                        oArray.put(ModifiersUnits.R2_SIGNIT_MOBILITY_INDICATOR, geoModifiers.get(eModifier));
                        break;
                    case OFFSET_INDICATOR:
                        oArray.put(ModifiersUnits.S_HQ_STAFF_OR_OFFSET_INDICATOR, geoModifiers.get(eModifier));
                        break;
                    case UNIQUE_DESIGNATOR_1:
                        UniqueDesignator1 = geoModifiers.get(eModifier);
                        oArray.put(ModifiersUnits.T_UNIQUE_DESIGNATION_1, UniqueDesignator1);
                        break;
                    case UNIQUE_DESIGNATOR_2:
                        oArray.put(ModifiersUnits.T1_UNIQUE_DESIGNATION_2, geoModifiers.get(eModifier));
                        break;
                    case EQUIPMENT_TYPE:
                        oArray.put(ModifiersUnits.V_EQUIP_TYPE, geoModifiers.get(eModifier));
                        break;
                    case DATE_TIME_GROUP:
                        oArray.put(ModifiersUnits.W_DTG_1, geoModifiers.get(eModifier));
                        break;
                    case DATE_TIME_GROUP_2:
                        oArray.put(ModifiersUnits.W1_DTG_2, geoModifiers.get(eModifier));
                        break;
                    case ALTITUDE_DEPTH:
                        oArray.put(ModifiersUnits.X_ALTITUDE_DEPTH, geoModifiers.get(eModifier));
                        break;
                    case LOCATION:
                        oArray.put(ModifiersUnits.Y_LOCATION, geoModifiers.get(eModifier));
                        break;
                    case SPEED:
                        oArray.put(ModifiersUnits.Z_SPEED, geoModifiers.get(eModifier));
                        break;
                    case SPECIAL_C2_HEADQUARTERS:
                        oArray.put(ModifiersUnits.AA_SPECIAL_C2_HQ, geoModifiers.get(eModifier));
                        break;
                    case FEINT_DUMMY_INDICATOR:
                        oArray.put(ModifiersUnits.AB_FEINT_DUMMY_INDICATOR, geoModifiers.get(eModifier));
                        break;
                    case INSTALLATION:
                        oArray.put(ModifiersUnits.AC_INSTALLATION, geoModifiers.get(eModifier));
                        break;
                    case PLATFORM_TYPE:
                        oArray.put(ModifiersUnits.AD_PLATFORM_TYPE, geoModifiers.get(eModifier));
                        break;
                    case EQUIPMENT_TEARDOWN_TIME:
                        oArray.put(ModifiersUnits.AE_EQUIPMENT_TEARDOWN_TIME, geoModifiers.get(eModifier));
                        break;
                    case COMMON_IDENTIFIER:
                        oArray.put(ModifiersUnits.AF_COMMON_IDENTIFIER, geoModifiers.get(eModifier));
                        break;
                    case AUXILIARY_EQUIPMENT_INDICATOR:
                        oArray.put(ModifiersUnits.AG_AUX_EQUIP_INDICATOR, geoModifiers.get(eModifier));
                        break;
                    case AREA_OF_UNCERTAINTY:
                        oArray.put(ModifiersUnits.AH_AREA_OF_UNCERTAINTY, geoModifiers.get(eModifier));
                        break;
                    case DEAD_RECKONING:
                        oArray.put(ModifiersUnits.AI_DEAD_RECKONING_TRAILER, geoModifiers.get(eModifier));
                        break;
                    case SPEED_LEADER:
                        oArray.put(ModifiersUnits.AJ_SPEED_LEADER, geoModifiers.get(eModifier));
                        break;
                    case PAIRING_LINE:
                        oArray.put(ModifiersUnits.AK_PAIRING_LINE, geoModifiers.get(eModifier));
                        break;
                    case OPERATIONAL_CONDITION:
                        oArray.put(ModifiersUnits.AL_OPERATIONAL_CONDITION, geoModifiers.get(eModifier));
                        break;
                    case DISTANCE:
                        break;
                    case AZIMUTH:
                        break;
                    case ENGAGEMENT_BAR:
                        oArray.put(ModifiersUnits.AO_ENGAGEMENT_BAR, geoModifiers.get(eModifier));
                        break;
                    case COUNTRY_CODE:
                        oArray.put(ModifiersUnits.CC_COUNTRY_CODE, geoModifiers.get(eModifier));
                        break;
                    case SONAR_CLASSIFICATION_CONFIDENCE:
                        oArray.put(ModifiersUnits.SCC_SONAR_CLASSIFICATION_CONFIDENCE, geoModifiers.get(eModifier));
                        break;
                }
            }
        }
        
        if ((symbol.getName() != null) && !symbol.getName().isEmpty()) {
            if (eLabelSetting != null) {
                switch (eLabelSetting) {
                    case REQUIRED_LABELS:
                        break;
                    case COMMON_LABELS:
                    case ALL_LABELS:
                        if ((UniqueDesignator1 == null) || UniqueDesignator1.isEmpty() || !UniqueDesignator1.toUpperCase().equals(symbol.getName().toUpperCase())) {
                            oArray.put(ModifiersUnits.CN_CPOF_NAME_LABEL, symbol.getName());
                        }
                        break;
                }
            }
        }
        return oArray;
    }

    @Override
    public SparseArray<String> getAttributes(IMapInstance mapInstance, IFeature feature, boolean selected) {
        initCheck();

        SparseArray<String> oArray = new SparseArray<>();
        int iIconSize = storageManager.getIconPixelSize(mapInstance);
        IGeoFillStyle oFillStyle = feature.getFillStyle();
        IGeoStrokeStyle oStrokeStyle = feature.getStrokeStyle();
        IGeoLabelStyle labelStyle = feature.getLabelStyle();
        IGeoColor strokeColor = null;
        IGeoColor textColor = null;
        boolean isMilStd = (feature instanceof MilStdSymbol);

        if (isMilStd) {
            oArray.put(MilStdAttributes.SymbologyStandard, "" + geoMilStdVersionToRendererVersion(((MilStdSymbol) feature).getSymbolStandard()));
        }
        oArray.put(MilStdAttributes.PixelSize, "" + iIconSize);
        oArray.put(MilStdAttributes.KeepUnitRatio, "true");
        oArray.put(MilStdAttributes.UseDashArray, "true");

        if (selected) {
            strokeColor = storageManager.getSelectedStrokeStyle(mapInstance).getStrokeColor();
            textColor = storageManager.getSelectedLabelStyle(mapInstance).getColor();
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

        if (isMilStd && !((MilStdSymbol) feature).isSinglePoint()) {
            oArray.put(MilStdAttributes.FontSize, "" + FontUtilities.getTextPixelSize(labelStyle, FontSizeModifierEnum.NORMAL));
        }

        return oArray;
    }

    @Override
    public double getFarDistanceThreshold(IMapInstance mapInstance) {
        initCheck();

        return storageManager.getFarDistanceThreshold(mapInstance);
    }

    @Override
    public double getMidDistanceThreshold(IMapInstance mapInstance) {
        initCheck();

        return storageManager.getMidDistanceThreshold(mapInstance);
    }

    private String convertToStringPosition(List<IGeoPosition> posList) {
        StringBuilder Str = new StringBuilder("");

        for (IGeoPosition pos: posList) {
            if (Str.length() > 0) {
                Str.append(" ");
            }
            Str.append(pos.getLongitude());
            Str.append(",");
            Str.append(pos.getLatitude());
            Str.append(",");
            Str.append(pos.getAltitude());
        }

        return Str.toString();
    }

    private List<List<IGeoPosition>> convertListOfPointListsToListOfPositionLists(ArrayList<ArrayList<Point2D>> listOfPointList) {
        IGeoPosition position;
        List<IGeoPosition> positionList;
        List<List<IGeoPosition>> listOfPosList = new ArrayList<>();

        if (listOfPointList != null) {
            // Convert the point lists into position lists.
            for (ArrayList<armyc2.c2sd.graphics2d.Point2D> pointList : listOfPointList) {
                positionList = new ArrayList<>();
                listOfPosList.add(positionList);
                for (armyc2.c2sd.graphics2d.Point2D point : pointList) {
                    position = new GeoPosition();
                    // Y is latitude and X is longitude.
                    position.setLatitude(point.getY());
                    position.setLongitude(point.getX());
                    position.setAltitude(0);
                    positionList.add(position);
                }
            }
        }

        return listOfPosList;
    }

    private void renderShapeParser(List<IFeature> featureList,
            IMapInstance mapInstance,
            armyc2.c2sd.renderer.utilities.MilStdSymbol renderSymbol,
            IFeature renderFeature,
            boolean selected) {
        IFeature feature;
        IGeoColor geoLineColor;
        IGeoColor geoFillColor;
        armyc2.c2sd.renderer.utilities.Color fillColor;
        armyc2.c2sd.renderer.utilities.Color lineColor;
        IGeoStrokeStyle renderStrokeStyle = renderFeature.getStrokeStyle();
        //IGeoFillStyle symbolFillStyle = renderFeature.getFillStyle();
        IGeoLabelStyle symbolTextStyle = renderFeature.getLabelStyle();
        IGeoStrokeStyle currentStrokeStyle;
        IGeoFillStyle currentFillStyle;
        IGeoLabelStyle currentTextStyle;
        IGeoAltitudeMode.AltitudeMode altitudeMode = renderFeature.getAltitudeMode();
        ArrayList<ShapeInfo> shapeInfoList = renderSymbol.getSymbolShapes();
        ArrayList<ShapeInfo> modifierShapeInfoList = renderSymbol.getModifierShapes();

        // Process the list of shapes.
        for(ShapeInfo shapeInfo: shapeInfoList) {
            currentStrokeStyle = null;
            currentFillStyle = null;

            lineColor = shapeInfo.getLineColor();
            if (lineColor != null) {
                currentStrokeStyle = new GeoStrokeStyle();
                currentStrokeStyle.setStrokeWidth((renderStrokeStyle == null)? 3: renderStrokeStyle.getStrokeWidth());
                geoLineColor = new EmpGeoColor((double) lineColor.getAlpha() / 255.0, lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue());
                currentStrokeStyle.setStrokeColor(geoLineColor);
            }

            if (selected) {
                if (null == currentStrokeStyle) {
                    currentStrokeStyle = storageManager.getSelectedStrokeStyle(mapInstance);
                } else {
                    currentStrokeStyle.setStrokeColor(storageManager.getSelectedStrokeStyle(mapInstance).getStrokeColor());
                }
            }

            armyc2.c2sd.graphics2d.BasicStroke basicStroke = (armyc2.c2sd.graphics2d.BasicStroke) shapeInfo.getStroke();

            if (renderFeature instanceof MilStdSymbol) {
                MilStdSymbol symbol = (MilStdSymbol) renderFeature;
                if ((null != currentStrokeStyle) && (basicStroke != null) && (basicStroke.getDashArray() != null)) {
                    currentStrokeStyle.setStipplingPattern(this.getTGStipplePattern(symbol.getBasicSymbol()));
                    currentStrokeStyle.setStipplingFactor(this.getTGStippleFactor(symbol.getBasicSymbol(), (int) currentStrokeStyle.getStrokeWidth()));
                }
            }

            fillColor = shapeInfo.getFillColor();
            if (fillColor != null) {
                geoFillColor = new EmpGeoColor((double) fillColor.getAlpha() / 255.0, fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue());
                currentFillStyle = new GeoFillStyle();
                currentFillStyle.setFillColor(geoFillColor);
            }

            switch (shapeInfo.getShapeType()) {
                case ShapeInfo.SHAPE_TYPE_POLYLINE: {
                    List<List<IGeoPosition>> listOfPosList = this.convertListOfPointListsToListOfPositionLists(shapeInfo.getPolylines());

                    if (currentFillStyle != null) {
                        // We create the polygon feature if it has a fill style.
                        for (List<IGeoPosition> posList : listOfPosList) {
                            feature = new Polygon(posList);
                            feature.setStrokeStyle(currentStrokeStyle);
                            feature.setFillStyle(currentFillStyle);
                            feature.setAltitudeMode(altitudeMode);
                            featureList.add(feature);
                        }
                    } else {
                        // We create a path feature if it does not have a fill style.
                        for (List<IGeoPosition> posList: listOfPosList) {
                            feature = new Path(posList);
                            feature.setStrokeStyle(currentStrokeStyle);
                            feature.setAltitudeMode(altitudeMode);
                            featureList.add(feature);
                        }
                    }
                    break;
                }
                case ShapeInfo.SHAPE_TYPE_FILL: {
                    List<List<IGeoPosition>> listOfPosList = this.convertListOfPointListsToListOfPositionLists(shapeInfo.getPolylines());

                    //if ((currentStrokeStyle != null) || (currentFillStyle != null)) {
                    if (currentStrokeStyle != null) {
                        // We create the feature if it has at least one style.
                        for (List<IGeoPosition> posList: listOfPosList) {
                            feature = new Path(posList); //new Polygon(posList);
                            feature.setStrokeStyle(currentStrokeStyle);
                            //feature.setFillStyle(currentFillStyle);
                            feature.setAltitudeMode(altitudeMode);
                            featureList.add(feature);
                        }
                    }
                    break;
                }
                default:
                    Log.i(TAG, "Unhandled Shape type " + shapeInfo.getShapeType());
                    break;
            }
        }

        // All modifier text are the same color.
        armyc2.c2sd.renderer.utilities.Color renderTextColor = renderSymbol.getTextColor();
        IGeoColor textColor = new EmpGeoColor(renderTextColor.getAlpha(), renderTextColor.getRed(), renderTextColor.getGreen(), renderTextColor.getBlue());

        // Process the list of shapes.
        for(ShapeInfo shapeInfo: modifierShapeInfoList) {
            switch (shapeInfo.getShapeType()) {
                case ShapeInfo.SHAPE_TYPE_MODIFIER: {
                    Log.i(TAG, "Shape Type M<odifier.");
                    break;
                }
                case ShapeInfo.SHAPE_TYPE_MODIFIER_FILL: {
                    Text textFeature;
                    armyc2.c2sd.graphics2d.Point2D point2D = shapeInfo.getModifierStringPosition();
                    IGeoPosition textPosition = new GeoPosition();

                    textFeature = new Text(shapeInfo.getModifierString());

                    textPosition.setAltitude(0);
                    textPosition.setLatitude(point2D.getY());
                    textPosition.setLongitude(point2D.getX());

                    textFeature.setPosition(textPosition);
                    textFeature.setAltitudeMode(altitudeMode);

                    currentTextStyle = new GeoLabelStyle();
                    if ((null == symbolTextStyle) || (null == symbolTextStyle.getColor())) {
                        currentTextStyle.setColor(textColor);
                        currentTextStyle.setSize(FontUtilities.DEFAULT_FONT_POINT_SIZE);
                    } else {
                        currentTextStyle.setColor(symbolTextStyle.getColor());
                        currentTextStyle.setSize(symbolTextStyle.getSize());
                    }
                    if (selected) {
                        if (null == currentTextStyle) {
                            currentTextStyle = storageManager.getSelectedLabelStyle(mapInstance);
                        } else {
                            currentTextStyle.setColor(storageManager.getSelectedLabelStyle(mapInstance).getColor());
                        }
                    }
                    currentTextStyle.setFontFamily(RendererSettings.getInstance().getMPModifierFontName());

                    switch (shapeInfo.getTextJustify()) {
                        case ShapeInfo.justify_left:
                            currentTextStyle.setJustification(IGeoLabelStyle.Justification.LEFT);
                            break;
                        case ShapeInfo.justify_right:
                            currentTextStyle.setJustification(IGeoLabelStyle.Justification.RIGHT);
                            break;
                        case ShapeInfo.justify_center:
                        default:
                            currentTextStyle.setJustification(IGeoLabelStyle.Justification.CENTER);
                            break;
                    }

                    textFeature.setLabelStyle(currentTextStyle);
                    textFeature.setRotationAngle(shapeInfo.getModifierStringAngle());

                    featureList.add(textFeature);
                    //Log.i(TAG, "Shape Type Modifier Fill." + shapeInfo.getModifierString() + " at " + point2D.getY() + "/" + point2D.getX());
                    break;
                }
                default:
                    Log.i(TAG, "Unhandled Shape type " + shapeInfo.getShapeType());
                    break;
            }
        }
    }

    private void renderTacticalGraphic(List<IFeature> featureList, IMapInstance mapInstance, MilStdSymbol symbol, boolean selected) {
        ICamera camera = mapInstance.getCamera();
        IGeoBounds bounds = storageManager.getBounds(storageManager.getMapMapping(mapInstance).getClientMap());

        if ((camera == null) || (bounds == null)) {
            return;
        }

        // Prep the parameters in the type the renderer requires them.
        int milstdVersion = MilStdUtilities.geoMilStdVersionToRendererVersion(symbol.getSymbolStandard());
        armyc2.c2sd.renderer.utilities.SymbolDef symbolDefinition = armyc2.c2sd.renderer.utilities.SymbolDefTable.getInstance().getSymbolDef(symbol.getBasicSymbol(), milstdVersion);

        if (symbol.getPositions().size() < symbolDefinition.getMinPoints()) {
            // There is not enough positions.
            return;
        }

        String coordinateStr = this.convertToStringPosition(symbol.getPositions());
        Log.d(TAG, "Symbol Code " + symbol.getSymbolCode() + " coordinateStr " + coordinateStr);
        String boundingBoxStr;
        if(bounds instanceof IEmpBoundingArea) {
            boundingBoxStr = bounds.toString();
        } else {
            boundingBoxStr = bounds.getWest() + "," + bounds.getSouth() + "," + bounds.getEast() + "," + bounds.getNorth();
        }

        Log.d(TAG, "bounds " + boundingBoxStr);
        double scale = camera.getAltitude() * 6.36;

        SparseArray<String> modifiers = this.getTGModifiers(mapInstance, symbol);
        SparseArray<String> attributes = this.getAttributes(mapInstance, symbol, selected);
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(symbol.getAltitudeMode());

        armyc2.c2sd.renderer.utilities.MilStdSymbol renderSymbol = SECWebRenderer.RenderMultiPointAsMilStdSymbol(
                symbol.getGeoId().toString(), symbol.getName(), symbol.getDescription(),
                symbol.getSymbolCode(), coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, milstdVersion);
        Log.d(TAG, "After RenderMultiPointAsMilStdSymbol renderSymbolgetSymbolShapes().size() " + renderSymbol.getSymbolShapes().size());
        // Retrieve the list of shapes.
        this.renderShapeParser(featureList, mapInstance, renderSymbol, symbol, selected);

    }

    @Override
    public List<IFeature> getTGRenderableShapes(IMapInstance mapInstance, MilStdSymbol symbol, boolean selected) {
        initCheck();

        List<IFeature> oList = new ArrayList<>();
        String basicSC = SymbolUtilities.getBasicSymbolID(symbol.getSymbolCode());
        
        if (SymbolUtilities.isTacticalGraphic(basicSC)) {
            this.renderTacticalGraphic(oList, mapInstance, symbol, selected);
        }
        
        return oList;
    }

    @Override
    public IEmpImageInfo getMilStdIcon(String sSymbolCode, SparseArray oModifiers, SparseArray oAttr) {
        initCheck();
        
        return MilStdRenderer.oBitmapCache.getImageInfo(sSymbolCode, oModifiers, oAttr);
    }

    /**
     * This method converts a IGeoMilSymbol.SymbolStandard enumerated value to a
     * MilStd Renderer symbol version value.
     * @param eStandard see IGeoMilSymbol.SymbolStandard.
     * @return an integer value indicating the standard version.
     */
    private int geoMilStdVersionToRendererVersion(IGeoMilSymbol.SymbolStandard eStandard) {
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

    @Override
    public double getSelectedIconScale(IMapInstance mapInstance) {
        return storageManager.getSelectedIconScale(mapInstance);
    }

    private int getTGStippleFactor(String basicSymbolCode, int strokeWidth) {
        int factor = 0;

        if (strokeWidth < 1) {
            strokeWidth = 1;
        }

        switch (basicSymbolCode) {
            case METOC_PRESSURE_INSTABILITY_LINE:
            case METOC_PRESSURE_SHEAR_LINE:
                factor = 2;
                break;
            case METOC_BOUNDED_AREAS_OF_WEATHER_LIQUID_PRECIPITATION_NON_CONVECTIVE_CONTINUOUS_OR_INTERMITTENT:
            case METOC_ATMOSPHERIC_BOUNDED_AREAS_OF_WEATHER_THUNDERSTORMS:
                factor = 3;
                break;
            default:
                // Normal dashes.
                factor = 3;
                break;
        }

        return factor;
    }

    /**
     * This method is called if the MilStd renderer indicates that the graphic requires a line stippling.
     * Specific symbols require specific stippling patterns.
     * @param basicSymbolCode
     * @return
     */
    private short getTGStipplePattern(String basicSymbolCode) {
        short pattern = 0;

        switch (basicSymbolCode) {
            case METOC_PRESSURE_INSTABILITY_LINE:
                pattern = (short) 0xDFF6;
                break;
            case METOC_PRESSURE_SHEAR_LINE:
            case METOC_BOUNDED_AREAS_OF_WEATHER_LIQUID_PRECIPITATION_NON_CONVECTIVE_CONTINUOUS_OR_INTERMITTENT:
            case METOC_ATMOSPHERIC_BOUNDED_AREAS_OF_WEATHER_THUNDERSTORMS:
                pattern = (short) 0xFFF6;
                break;
            default:
                // Normal dashes.
                pattern = (short) 0xEEEE;
                break;
        }

        return pattern;
    }

    @Override
    public List<IFeature> getFeatureRenderableShapes(IMapInstance mapInstance, IFeature feature, boolean selected) {
        initCheck();

        String symbolCode = "";
        List<IFeature> oList = new ArrayList<>();
        ICamera camera = mapInstance.getCamera();
        IGeoBounds bounds = storageManager.getBounds(storageManager.getMapMapping(mapInstance).getClientMap());

        if ((camera == null) || (bounds == null)) {
            return oList;
        }

        String coordinateStr = this.convertToStringPosition(feature.getPositions());
        String boundingBoxStr;
        if(bounds instanceof IEmpBoundingArea) {
            boundingBoxStr = bounds.toString();
        } else {
            boundingBoxStr = bounds.getWest() + "," + bounds.getSouth() + "," + bounds.getEast() + "," + bounds.getNorth();
        }

        double scale = camera.getAltitude() * 6.36;
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(feature.getAltitudeMode());
        SparseArray<String> modifiers = new SparseArray<>();
        SparseArray<String> attributes;

        if (feature instanceof mil.emp3.api.Circle) {
            mil.emp3.api.Circle circleFeature = (mil.emp3.api.Circle) feature;
            symbolCode = "PBS_CIRCLE-----";
            modifiers.put(ModifiersTG.AM_DISTANCE, "" + circleFeature.getRadius());
            attributes = this.getAttributes(mapInstance, feature, selected);
        } else if (feature instanceof mil.emp3.api.Ellipse) {
            mil.emp3.api.Ellipse ellipseFeature = (mil.emp3.api.Ellipse) feature;
            symbolCode = "PBS_ELLIPSE----";
            modifiers.put(ModifiersTG.AM_DISTANCE, ellipseFeature.getSemiMinor() + "," + ellipseFeature.getSemiMajor());
            modifiers.put(ModifiersTG.AN_AZIMUTH, ellipseFeature.getAzimuth() + "");
            attributes = this.getAttributes(mapInstance, feature, selected);
        } else if (feature instanceof mil.emp3.api.Rectangle) {
            mil.emp3.api.Rectangle rectangleFeature = (mil.emp3.api.Rectangle) feature;
            symbolCode = "PBS_RECTANGLE--";
            modifiers.put(ModifiersTG.AM_DISTANCE, rectangleFeature.getWidth() + "," + rectangleFeature.getHeight());
            modifiers.put(ModifiersTG.AN_AZIMUTH, rectangleFeature.getAzimuth() + "");
            attributes = this.getAttributes(mapInstance, feature, selected);
        } else if (feature instanceof mil.emp3.api.Square) {
            mil.emp3.api.Square squareFeature = (mil.emp3.api.Square) feature;
            symbolCode = "PBS_SQUARE-----";
            modifiers.put(ModifiersTG.AM_DISTANCE, squareFeature.getWidth() + "");
            modifiers.put(ModifiersTG.AN_AZIMUTH, squareFeature.getAzimuth() + "");
            attributes = this.getAttributes(mapInstance, feature, selected);
        } else {
            return oList;
        }

        armyc2.c2sd.renderer.utilities.MilStdSymbol renderSymbol = SECWebRenderer.RenderMultiPointAsMilStdSymbol(
                feature.getGeoId().toString(), feature.getName(), feature.getDescription(),
                symbolCode, coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, 1);

        // Retrieve the list of shapes.
        this.renderBasicShapeParser(oList, mapInstance, renderSymbol, feature, selected);

        return oList;
    }

    /**
     *
     * @param featureList - This is the output feature list that will be displayed on the map
     * @param mapInstance - Underlying mapInstance
     * @param renderSymbol - This is the value returned by mission command render-er.
     * @param renderFeature - This is the Feature created by the application that needs to be rendered
     * @param selected - Is the feature in a selected state?
     */
    private void renderBasicShapeParser(List<IFeature> featureList,
                                   IMapInstance mapInstance,
                                   armyc2.c2sd.renderer.utilities.MilStdSymbol renderSymbol,
                                   IFeature renderFeature,
                                   boolean selected) {
        //
        // The mission command render-er will return two shapes in response to a request to render a
        // Circle, Ellipse, Square or a Rectangle. One shape will be POLYLINE and other will be FILL.
        // We will pick up the right shape based on the FillStyle specified by the application in the
        // 'renderFeature'. This logic is dependent on fillStyle being null by default in basic shapes.
        // The constructor of basic shapes ensures this.
        //
        int shapeTypeToUse = ShapeInfo.SHAPE_TYPE_FILL;
        if(null == renderFeature.getFillStyle()) {
            shapeTypeToUse = ShapeInfo.SHAPE_TYPE_POLYLINE;
        }

        for(ShapeInfo shapeInfo: renderSymbol.getSymbolShapes()) {
            if(shapeInfo.getShapeType() != shapeTypeToUse) {
                continue;
            }

            //
            // If line color is not specified by the application then we want to use whatever default was returned by
            // the renderer. We also need to override with 'selected' stroke color if feature is in selected state.
            //

            IGeoStrokeStyle currentStrokeStyle = renderFeature.getStrokeStyle();

            if(selected) {
                if(null == currentStrokeStyle) {
                    currentStrokeStyle = storageManager.getSelectedStrokeStyle(mapInstance);
                } else {
                    currentStrokeStyle.setStrokeColor(storageManager.getSelectedStrokeStyle(mapInstance).getStrokeColor());
                }
            }

            if((null == currentStrokeStyle) || (null == currentStrokeStyle.getStrokeColor())) {
                // Get the line/stroke color from the renderer.
                if(null != shapeInfo.getLineColor()) {
                    EmpGeoColor rendererStrokeColor = new EmpGeoColor((double) shapeInfo.getLineColor().getAlpha() / 255.0,
                            shapeInfo.getLineColor().getRed(), shapeInfo.getLineColor().getGreen(), shapeInfo.getLineColor().getBlue());
                    if(null == currentStrokeStyle) {
                        currentStrokeStyle = new GeoStrokeStyle();
                        currentStrokeStyle.setStrokeWidth(DEFAULT_STROKE_WIDTH);
                    }
                    currentStrokeStyle.setStrokeColor(rendererStrokeColor);
                }
            }

            //
            // If fill color is not specified by the application then we want to use whatever default was returned by
            // the renderer.
            //
            IGeoFillStyle currentFillStyle = renderFeature.getFillStyle();

            if((null == currentFillStyle) || (null == currentFillStyle.getFillColor())) {
                // Get the fill color from the renderer.
                if(null != shapeInfo.getFillColor()) {
                    EmpGeoColor rendererFillColor = new EmpGeoColor((double) shapeInfo.getFillColor().getAlpha() / 255.0,
                            shapeInfo.getFillColor().getRed(), shapeInfo.getFillColor().getGreen(), shapeInfo.getFillColor().getBlue());
                    if(null == currentFillStyle) {
                        currentFillStyle = new GeoFillStyle();
                    }
                    currentFillStyle.setFillColor(rendererFillColor);
                }
            }

            switch (shapeInfo.getShapeType()) {
                case ShapeInfo.SHAPE_TYPE_POLYLINE: {
                    List<List<IGeoPosition>> listOfPosList = this.convertListOfPointListsToListOfPositionLists(shapeInfo.getPolylines());

                    for (List<IGeoPosition> posList : listOfPosList) {
                        IFeature feature = new Path(posList);
                        feature.setStrokeStyle(currentStrokeStyle);
                        feature.setAltitudeMode(renderFeature.getAltitudeMode());
                        featureList.add(feature);
                    }
                    break;
                }
                case ShapeInfo.SHAPE_TYPE_FILL: {
                    List<List<IGeoPosition>> listOfPosList = this.convertListOfPointListsToListOfPositionLists(shapeInfo.getPolylines());

                    for (List<IGeoPosition> posList : listOfPosList) {
                        IFeature feature = new Polygon(posList);
                        feature.setStrokeStyle(currentStrokeStyle);
                        feature.setFillStyle(currentFillStyle);
                        feature.setAltitudeMode(renderFeature.getAltitudeMode());
                        featureList.add(feature);
                    }
                    break;
                }
                default:
                    Log.e(TAG, "Unhandled Shape type " + shapeInfo.getShapeType());
                    break;
            }
        }
    }
}
