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
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.EmpGeoColor;
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

    // These constants are added here because the renderer lib does not define them as constance and
    // therefore cant be used in a switch case
    public static final int SHAPE_TYPE_POLYLINE = 0;
    public static final int SHAPE_TYPE_FILL = 1;
    public static final int SHAPE_TYPE_MODIFIER = 2;
    public static final int SHAPE_TYPE_MODIFIER_FILL = 3;

    private IStorageManager storageManager;

    private static String sRendererCacheDir = null;
    private int getMemoryClass = 0;
    private static java.util.Set<IGeoMilSymbol.Modifier> oRequiredLabels = new java.util.HashSet<>();
    private static java.util.Set<IGeoMilSymbol.Modifier> oCommonLabels = new java.util.HashSet<>();
    //private static java.util.Set<IGeoMilSymbol.Modifier> oTGRequiredLabels = new java.util.HashSet<>();
    //private static java.util.Set<IGeoMilSymbol.Modifier> oTGCommonLabels = new java.util.HashSet<>();
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

        MilStdLabelSettingEnum eLabelSetting = storageManager.getMilStdLabels(mapInstance);
        SparseArray<String> oArray = new SparseArray<>();
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
                        oArray.put(ModifiersTG.T_UNIQUE_DESIGNATION_1, geoModifiers.get(eModifier));
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
        
        if (symbol.getName().length() > 0) {
            if (eLabelSetting != null) {
                switch (eLabelSetting) {
                    case REQUIRED_LABELS:
                        break;
                    case COMMON_LABELS:
                    case ALL_LABELS:
                        oArray.put(ModifiersUnits.CN_CPOF_NAME_LABEL, symbol.getName());
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
                        oArray.put(ModifiersUnits.T_UNIQUE_DESIGNATION_1, geoModifiers.get(eModifier));
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
                        oArray.put(ModifiersUnits.CN_CPOF_NAME_LABEL, symbol.getName());
                        break;
                }
            }
        }
        return oArray;
    }

    @Override
    public SparseArray<String> getAttributes(IMapInstance mapInstance, MilStdSymbol symbol, boolean selected) {
        initCheck();

        SparseArray<String> oArray = new SparseArray<>();
        int iIconSize = storageManager.getIconPixelSize(mapInstance);
        IGeoFillStyle oFillStyle = symbol.getFillStyle();
        IGeoStrokeStyle oStrokeStyle = symbol.getStrokeStyle();
        IGeoLabelStyle labelStyle = symbol.getLabelStyle();

        oArray.put(MilStdAttributes.SymbologyStandard, "" + geoMilStdVersionToRendererVersion(symbol.getSymbolStandard()));
        oArray.put(MilStdAttributes.PixelSize, "" + iIconSize);
        oArray.put(MilStdAttributes.KeepUnitRatio, "true");
        oArray.put(MilStdAttributes.UseDashArray, "true");

        if (selected) {
            oStrokeStyle = storageManager.getSelectedStrokeStyle(mapInstance);
            labelStyle = storageManager.getSelectedLabelStyle(mapInstance);
        }

        if (oFillStyle != null) {
            oArray.put(MilStdAttributes.FillColor, "#" + ColorUtils.colorToString(oFillStyle.getFillColor()));
        }

        if (oStrokeStyle != null) {
            oArray.put(MilStdAttributes.LineColor, "#" + ColorUtils.colorToString(oStrokeStyle.getStrokeColor()));
            oArray.put(MilStdAttributes.LineWidth, "" + (int) oStrokeStyle.getStrokeWidth());
        }

        if (labelStyle != null) {
            if (null != labelStyle.getColor()) {
                oArray.put(MilStdAttributes.TextColor, "#" + ColorUtils.colorToString(labelStyle.getColor()));
            }
            oArray.put(MilStdAttributes.FontSize, "" + symbol.getFontSize());
            // There is currently no way to change the font.
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

    private String convertToStringPosition(java.util.List<IGeoPosition> posList) {
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

    private java.util.List<java.util.List<IGeoPosition>> convertListOfPointListsToListOfPositionLists(java.util.ArrayList<java.util.ArrayList<armyc2.c2sd.graphics2d.Point2D>> listOfPointList) {
        IGeoPosition position;
        java.util.List<IGeoPosition> positionList;
        java.util.List<java.util.List<IGeoPosition>> listOfPosList = new java.util.ArrayList<>();

        if (listOfPointList != null) {
            // Convert the point lists into position lists.
            for (java.util.ArrayList<armyc2.c2sd.graphics2d.Point2D> pointList : listOfPointList) {
                positionList = new java.util.ArrayList<>();
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

    private void renderTacticalGraphic(java.util.List<IFeature> featureList, IMapInstance mapInstance, MilStdSymbol symbol, boolean selected) {
        IFeature feature;
        IGeoColor geoLineColor;
        IGeoColor geoFillColor;
        armyc2.c2sd.renderer.utilities.Color fillColor;
        armyc2.c2sd.renderer.utilities.Color lineColor;
        ICamera camera = mapInstance.getCamera();
        IGeoStrokeStyle symbolStrokeStyle = symbol.getStrokeStyle();
        IGeoFillStyle symbolFillStyle = symbol.getFillStyle();
        IGeoLabelStyle symbolTextStyle = symbol.getLabelStyle();
        IGeoStrokeStyle currentStrokeStyle;
        IGeoFillStyle currentFillStyle;
        IGeoLabelStyle currentTextStyle;
        IGeoBounds bounds = mapInstance.getMapBounds();
        IGeoAltitudeMode.AltitudeMode altitudeMode = symbol.getAltitudeMode();

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
        String boundingBoxStr = bounds.getWest() + "," + bounds.getSouth() + "," + bounds.getEast() + "," + bounds.getNorth();
        double scale = camera.getAltitude() * 6.36;
        SparseArray<String> modifiers = this.getTGModifiers(mapInstance, symbol);
        SparseArray<String> attributes = this.getAttributes(mapInstance, symbol, selected);
        String altitudeModeStr = MilStdUtilities.geoAltitudeModeToString(symbol.getAltitudeMode());

        armyc2.c2sd.renderer.utilities.MilStdSymbol renderSymbol = SECWebRenderer.RenderMultiPointAsMilStdSymbol(
                symbol.getGeoId().toString(), symbol.getName(), symbol.getDescription(),
                symbol.getSymbolCode(), coordinateStr, altitudeModeStr, scale, boundingBoxStr,
                modifiers, attributes, milstdVersion);
        // Retrieve the list of shapes.
        java.util.ArrayList<ShapeInfo> shapeInfoList = renderSymbol.getSymbolShapes();

        // Process the list of shapes.
        for(ShapeInfo shapeInfo: shapeInfoList) {
            currentStrokeStyle = null;
            currentFillStyle = null;
            if (symbolStrokeStyle == null) {
                lineColor = shapeInfo.getLineColor();
                if (lineColor != null) {
                    geoLineColor = new EmpGeoColor((double) lineColor.getAlpha() / 255.0, lineColor.getRed(), lineColor.getGreen(), lineColor.getBlue());
                    currentStrokeStyle = new GeoStrokeStyle();
                    currentStrokeStyle.setStrokeColor(geoLineColor);
                    //currentStrokeStyle.setStrokeWidth(().getLineWidth());
                    currentStrokeStyle.setStrokeWidth(3);
                }
            } else {
                currentStrokeStyle = symbolStrokeStyle;
            }
            armyc2.c2sd.graphics2d.BasicStroke basicStroke = (armyc2.c2sd.graphics2d.BasicStroke) shapeInfo.getStroke();

            if ((currentStrokeStyle != null ) && (basicStroke != null) && (basicStroke.getDashArray() != null)) {
                currentStrokeStyle.setStrokePattern(IGeoStrokeStyle.StrokePattern.dotted);
            }

            if (symbolFillStyle == null) {
                fillColor = shapeInfo.getFillColor();
                if (fillColor != null) {
                    geoFillColor = new EmpGeoColor((double) fillColor.getAlpha() / 255.0, fillColor.getRed(), fillColor.getGreen(), fillColor.getBlue());
                    currentFillStyle = new GeoFillStyle();
                    currentFillStyle.setFillColor(geoFillColor);
                }
            } else {
                currentFillStyle = symbolFillStyle;
            }

            switch (shapeInfo.getShapeType()) {
                case MilStdRenderer.SHAPE_TYPE_POLYLINE: {
                    java.util.List<java.util.List<IGeoPosition>> listOfPosList = this.convertListOfPointListsToListOfPositionLists(shapeInfo.getPolylines());

                    for (java.util.List<IGeoPosition> posList: listOfPosList) {
                        if ((currentStrokeStyle != null) || (currentFillStyle != null)) {
                            // We create the feature if it has at least on style.
                            feature = new Path(posList);
                            feature.setStrokeStyle(currentStrokeStyle);
                            feature.setFillStyle(currentFillStyle);
                            feature.setAltitudeMode(altitudeMode);
                            featureList.add(feature);
                        }
                    }
                    break;
                }
                case MilStdRenderer.SHAPE_TYPE_FILL: {
                    java.util.List<java.util.List<IGeoPosition>> listOfPosList = this.convertListOfPointListsToListOfPositionLists(shapeInfo.getPolylines());

                    for (java.util.List<IGeoPosition> posList: listOfPosList) {
                        if ((currentStrokeStyle != null) || (currentFillStyle != null)) {
                            // We create the feature if it has at least on style.
                            feature = new Polygon(posList);
                            feature.setStrokeStyle(currentStrokeStyle);
                            feature.setFillStyle(currentFillStyle);
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

        shapeInfoList = renderSymbol.getModifierShapes();

        // All modifier text are the same color.
        armyc2.c2sd.renderer.utilities.Color renderTextColor = renderSymbol.getTextColor();
        IGeoColor textColor = new EmpGeoColor(renderTextColor.getAlpha(), renderTextColor.getRed(), renderTextColor.getGreen(), renderTextColor.getBlue());

        // Process the list of shapes.
        for(ShapeInfo shapeInfo: shapeInfoList) {
            switch (shapeInfo.getShapeType()) {
                case MilStdRenderer.SHAPE_TYPE_MODIFIER: {
                    Log.i(TAG, "Shape Type M<odifier.");
                    break;
                }
                case MilStdRenderer.SHAPE_TYPE_MODIFIER_FILL: {
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
                        currentTextStyle.setScale(1.0);
                    } else {
                        currentTextStyle.setColor(symbolTextStyle.getColor());
                        currentTextStyle.setScale(symbolTextStyle.getScale());
                    }

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
                    textFeature.setFontFamily(RendererSettings.getInstance().getMPModifierFontName());
                    textFeature.setFontSize(symbol.getFontSize());

                    featureList.add(textFeature);
                    Log.i(TAG, "Shape Type Modifier Fill." + shapeInfo.getModifierString() + " at " + point2D.getY() + "/" + point2D.getX());
                    break;
                }
                default:
                    Log.i(TAG, "Unhandled Shape type " + shapeInfo.getShapeType());
                    break;
            }
        }
    }

    @Override
    public java.util.List<IFeature> getTGRenderableShapes(IMapInstance mapInstance, MilStdSymbol symbol, boolean selected) {
        initCheck();

        java.util.List<IFeature> oList = new java.util.ArrayList<>();
        IStorageManager sMgr = storageManager;
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
    public int geoMilStdVersionToRendererVersion(IGeoMilSymbol.SymbolStandard eStandard) {
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
}
