package mil.emp3.core.utils;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.core.editors.AbstractEditor;
import mil.emp3.core.editors.milstd.MilStdAutoShapeEditor;
import mil.emp3.core.editors.milstd.MilStdDCCircularParamAutoShapeEditor;
import mil.emp3.core.editors.milstd.MilStdDCCircularRangeFanEditor;
import mil.emp3.core.editors.milstd.MilStdDCLineEditor;
import mil.emp3.core.editors.milstd.MilStdDCRectangularParamAutoShape;
import mil.emp3.core.editors.milstd.MilStdDCRouteEditor;
import mil.emp3.core.editors.milstd.MilStdDCSectorRangeFanEditor;
import mil.emp3.core.editors.milstd.MilStdDCSuperAutoShapeEditor;
import mil.emp3.core.editors.milstd.MilStdDCTwoPointEditor;
import mil.emp3.core.editors.milstd.MilStdDCTwoPointRectangularParamAutoShape;
import mil.emp3.core.editors.milstd.MilStdPolygonEditor;
import mil.emp3.core.editors.milstd.MilStdSinglePointEditor;
import mil.emp3.api.utils.MilStdUtilities;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class provides the core with utility functions
 */
public class CoreMilStdUtilities {

    // These constance are used in the editors to allpy slight differences for the actual symbols.
    public static final String AIR_CORRIDOR = "G*G*ALC---****X";
    public static final String STANDARD_ARMY_AIRCRAFT_FLIGHT_ROUTE = "G*G*ALS---****X";
    public static final String MINIMUM_RISK_ROUTE = "G*G*ALM---****X";
    public static final String UBMANNED_AERIAL_VEHICLE_ROUTE = "G*G*ALU---****X";
    public static final String LOW_LEVEL_TRANSIT_ROUTE = "G*G*ALL---****X";
    public static final String AIRSPACE_COORDINATION_AREA_IRREGULAR = "G*F*ACAI--****X";
    public static final String AIRSPACE_COORDINATION_AREA_RECTANGULAR = "G*F*ACAR--****X";
    public static final String AIRSPACE_COORDINATION_AREA_CIRCULAR = "G*F*ACAC--****X";
    public static final String RESTRICTED_OPERATIONS_ZONE = "G*G*AAR---****X";
    public static final String HIGH_DENSITY_AIRSPACE_CONTROL_ZONE = "G*G*AAH---****X";
    public static final String MISSILE_ENGAGEMENT_ZONE = "G*G*AAM---****X";
    public static final String MISSILE_ENGAGEMENT_ZONE_LOW_ALTITUDE = "G*G*AAML--****X";
    public static final String MISSILE_ENGAGEMENT_ZONE_HIGH_ALTITUDE = "G*G*AAMH--****X";

    public static final String CIRCULAR_RANGE_FAN = "G*F*AXC---****X";
    public static final String SECTOR_RANGE_FAN = "G*F*AXS---****X";

    public static final String RECTANGULAR_TARGET = "G*F*ATR---****X";
    public static final String CIRCULAR_TARGET = "G*FPATC---****X";

    public static final String FORD_EASY = "G*M*BCE---****X";
    public static final String FORD_DIFFICULT = "G*M*BCD---****X";

    public static final String ROADBLOCK_COMPLETE_EXECUTED = "G*M*ORC---****X";
    public static final String MS_NBC_MINIMUM_SAFE_DISTANCE_ZONES = "G*M*NM----****X";

    public static final String AMBUSH = "G*G*SLA---****X";
    public static final String BLOCK_TASK = "G*T*B-----****X";
    public static final String TASK_CLEAR = "G*T*X-----****X";
    public static final String TASK_CONTAIN = "G*T*J-----****X";
    public static final String TASK_PENETRATE = "G*T*P-----****X";
    public static final String TASK_RELIEF_IN_PLACE = "G*T*R-----****X";
    public static final String TASK_DELAY = "G*T*L-----****X";
    public static final String TASK_DISRUPT = "G*T*T-----****X";
    public static final String TASK_RETIREMENT = "G*T*M-----****X";
    public static final String TASK_SEIZE = "G*T*Z-----****X";
    public static final String TASK_WITHDRAW = "G*T*W-----****X";
    public static final String TASK_WITHDRAW_UNDER_PRESURE = "G*T*WP----****X";
    public static final String TASK_ISOLATE = "G*T*E-----****X";
    public static final String TASK_OCCUPY = "G*T*O-----****X";
    public static final String TASK_RETIAN = "G*T*Q-----****X";
    public static final String TASK_SECURE = "G*T*S-----****X";
    public static final String TASK_CORDON_SEARCH = "G*T*V-----****X";
    public static final String TASK_CORDON_KNOCK = "G*T*2-----****X";

    public static final String CCGM_INFILTRATION_LANE = "G*G*OLI---****X";
    public static final String CCGM_OFFENCE_SUPPORT_BY_FIRE_POSITION = "G*G*OAS---****X";

    public static final String MS_MINEFIELD_GAP = "G*M*OFG---****X";
    public static final String MS_OBSTACLES_EFFECT_BLOCK = "G*M*OEB---****X";
    public static final String MS_OBSTACLES_EFFECT_TURN = "G*M*OET---****X";
    public static final String MS_OBSTACLES_RCBB_PLANNED = "G*M*ORP---****X";
    public static final String MS_OBSTACLES_RCBB_ESR1_SAFE = "G*M*ORS---****X";
    public static final String MS_OBSTACLES_RCBB_ESR2_ARMED_PASSABLE = "G*M*ORA---****X";
    public static final String MS_OBSTACLES_RCBB_COMPLETE_EXECUTED = "G*M*ORC---****X";
    public static final String MS_OBSTACLES_TRIP_WIRE = "G*M*OT----****X";
    public static final String MS_OBSTACLES_BYPASS_ASSAULT_CROSSING_AREA = "G*M*BCA---****X";
    public static final String MS_OBSTACLES_BYPASS_BRIDGE_OR_GAP = "G*M*BCB---****X";
    public static final String MS_OBSTACLES_BYPASS_FORD_EASY = "G*M*BCE---****X";
    public static final String MS_OBSTACLES_BYPASS_FORD_DIFFICULT = "G*M*BCD---****X";

    // Only in 2525B
    public static final String FORWARD_AREA_AIR_DEFENSE_ZONE = "G*G*AAF---****X";
    // Same Symbol Code with diff name in 2525C
    public static final String SHORT_RANGE_AIR_DEFENSE_ENGAGEMENT_ZONE = "G*G*AAF---****X";

    // Only in 2525B
    public static final String KILL_BOX_PURPLE_CIRCULAR = "G*F*AKPC--****X";
    public static final String KILL_BOX_PURPLE_RECTANGULAR = "G*F*AKPR--****X";
    public static final String KILL_BOX_PURPLE_IRREGULAR = "G*F*AKPI--****X";


    public static AbstractEditor getMultiPointEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener) throws EMP_Exception {
        AbstractEditor editor = null;
        armyc2.c2sd.renderer.utilities.SymbolDef symbolDef;
        int milstdVersion = MilStdUtilities.geoMilStdVersionToRendererVersion(feature.getSymbolStandard());
        String basicSymbolCode = feature.getBasicSymbol();
        armyc2.c2sd.renderer.utilities.SymbolDef symbolDefinition = armyc2.c2sd.renderer.utilities.SymbolDefTable.getInstance().getSymbolDef(basicSymbolCode, milstdVersion);

        switch (symbolDefinition.getDrawCategory()) {
            /**
             * A polyline, a line with n number of points.
             * 0 control points
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_LINE:
                /**
                 * A polyline with n points (entered in reverse order)
                 * 0 control points
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_ARROW:
                editor = new MilStdDCLineEditor(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             * TG such as:
             * TACGRP.TSK.ISL,
             * TACGRP.TSK.OCC,
             * TACGRP.TSK.RTN,
             * TACGRP.TSK.SCE
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_AUTOSHAPE:
                editor = new MilStdAutoShapeEditor(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             * An enclosed polygon with n points
             * 0 control points
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_POLYGON:
                editor = new MilStdPolygonEditor(map, feature, oEventListener);
                break;
            /**
             * A graphic with n points whose last point defines the width of the graphic.
             * 1 control point
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_ROUTE:
                editor = new MilStdDCRouteEditor(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             * A line defined only by 2 points, and cannot have more.
             * 0 control points
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_TWOPOINTLINE:
                /**
                 * A polyline with 2 points (entered in reverse order).
                 * 0 control points
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_TWOPOINTARROW:
                editor = new MilStdDCTwoPointEditor(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             * Shape is defined by a single point
             * 0 control points
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_POINT:
                editor = new MilStdSinglePointEditor(map, feature, oEventListener);
                break;
            /**
             * An animated shape, uses the animate function to draw. Super Autoshape draw
             * in 2 phases, usually one to define length, and one to define width.
             * 0 control points (every point shapes symbol)
             *
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_SUPERAUTOSHAPE:
                editor = new MilStdDCSuperAutoShapeEditor(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             * Circle that requires 1 AM modifier value.
             * See ModifiersTG.js for modifier descriptions and constant key strings.
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_CIRCULAR_PARAMETERED_AUTOSHAPE:
                editor = new MilStdDCCircularParamAutoShapeEditor(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             * Rectangle that requires 2 AM modifier values and 1 AN value.";
             * See ModifiersTG.js for modifier descriptions and constant key strings.
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_RECTANGULAR_PARAMETERED_AUTOSHAPE:
                editor = new MilStdDCRectangularParamAutoShape(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             * Requires 2 AM values and 2 AN values per sector.
             * The first sector can have just one AM value although it is recommended
             * to always use 2 values for each sector.  X values are not required
             * as our rendering is only 2D for the Sector Range Fan symbol.
             * See ModifiersTG.js for modifier descriptions and constant key strings.
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_SECTOR_PARAMETERED_AUTOSHAPE:
                editor = new MilStdDCSectorRangeFanEditor(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             *  Requires at least 1 distance/AM value"
             *  See ModifiersTG.js for modifier descriptions and constant key strings.
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_CIRCULAR_RANGEFAN_AUTOSHAPE:
                editor = new MilStdDCCircularRangeFanEditor(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             * Requires 1 AM value.
             * See ModifiersTG.js for modifier descriptions and constant key strings.
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_TWO_POINT_RECT_PARAMETERED_AUTOSHAPE:
                editor = new MilStdDCTwoPointRectangularParamAutoShape(map, feature, oEventListener, symbolDefinition);
                break;
            /**
             * 3D airspace, not a milstd graphic.
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_3D_AIRSPACE:
                throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Not Supported. Editing this tactical graphics is not yet supported.");
                //break;
        }

        return editor;
    }

    public static AbstractEditor getMultiPointEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, boolean newFeature) throws EMP_Exception {
        AbstractEditor editor = null;
        armyc2.c2sd.renderer.utilities.SymbolDef symbolDef;
        int milstdVersion = MilStdUtilities.geoMilStdVersionToRendererVersion(feature.getSymbolStandard());
        String basicSymbolCode = feature.getBasicSymbol();
        armyc2.c2sd.renderer.utilities.SymbolDef symbolDefinition = armyc2.c2sd.renderer.utilities.SymbolDefTable.getInstance().getSymbolDef(basicSymbolCode, milstdVersion);

        switch (symbolDefinition.getDrawCategory()) {
            /**
             * A polyline, a line with n number of points.
             * 0 control points
             */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_LINE:
                /**
                 * A polyline with n points (entered in reverse order)
                 * 0 control points
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_ARROW:
                editor = new MilStdDCLineEditor(map, feature, oEventListener, symbolDefinition, newFeature);
                break;
                /**
                 * An animated shape, uses the animate function to draw.
                 * 0 control points (every point shapes symbol)
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_AUTOSHAPE:
                editor = new MilStdAutoShapeEditor(map, feature, oEventListener, symbolDefinition, newFeature);
                break;
                /**
                 * An enclosed polygon with n points
                 * 0 control points
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_POLYGON:
                editor = new MilStdPolygonEditor(map, feature, oEventListener, newFeature);
                break;
                /**
                 * A graphic with n points whose last point defines the width of the graphic.
                 * 1 control point
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_ROUTE:
                editor = new MilStdDCRouteEditor(map, feature, oEventListener, symbolDefinition, newFeature);
                break;
                /**
                 * A line defined only by 2 points, and cannot have more.
                 * 0 control points
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_TWOPOINTLINE:
                /**
                 * A polyline with 2 points (entered in reverse order).
                 * 0 control points
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_TWOPOINTARROW:
                editor = new MilStdDCTwoPointEditor(map, feature, oEventListener, symbolDefinition, newFeature);
                break;
                /**
                 * Shape is defined by a single point
                 * 0 control points
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_POINT:
                editor = new MilStdSinglePointEditor(map, feature, oEventListener, newFeature);
                break;
                /**
                 * An animated shape, uses the animate function to draw. Super Autoshape draw
                 * in 2 phases, usually one to define length, and one to define width.
                 * 0 control points (every point shapes symbol)
                 *
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_SUPERAUTOSHAPE:
                 editor = new MilStdDCSuperAutoShapeEditor(map, feature, oEventListener, symbolDefinition, newFeature);
                 break;
                /**
                 * Circle that requires 1 AM modifier value.
                 * See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_CIRCULAR_PARAMETERED_AUTOSHAPE:
                 editor = new MilStdDCCircularParamAutoShapeEditor(map, feature, oEventListener, symbolDefinition, newFeature);
                 break;
                /**
                 * Rectangle that requires 2 AM modifier values and 1 AN value.";
                 * See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_RECTANGULAR_PARAMETERED_AUTOSHAPE:
                 editor = new MilStdDCRectangularParamAutoShape(map, feature, oEventListener, symbolDefinition, newFeature);
                 break;
                /**
                 * Requires 2 AM values and 2 AN values per sector.
                 * The first sector can have just one AM value although it is recommended
                 * to always use 2 values for each sector.  X values are not required
                 * as our rendering is only 2D for the Sector Range Fan symbol.
                 * See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_SECTOR_PARAMETERED_AUTOSHAPE:
                editor = new MilStdDCSectorRangeFanEditor(map, feature, oEventListener, symbolDefinition, newFeature);
                break;
                /**
                 *  Requires at least 1 distance/AM value"
                 *  See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_CIRCULAR_RANGEFAN_AUTOSHAPE:
                editor = new MilStdDCCircularRangeFanEditor(map, feature, oEventListener, symbolDefinition, newFeature);
                break;
                /**
                 * Requires 1 AM value.
                 * See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_TWO_POINT_RECT_PARAMETERED_AUTOSHAPE:
                editor = new MilStdDCTwoPointRectangularParamAutoShape(map, feature, oEventListener, symbolDefinition, newFeature);
                break;
                /**
                 * 3D airspace, not a milstd graphic.
                 */
            case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_3D_AIRSPACE:
                throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Not Supported. Editing this tactical graphics is not yet supported.");
                //break;
            default:
                throw new EMP_Exception(EMP_Exception.ErrorDetail.NOT_SUPPORTED, "Invalid Draw Category.");
        }

        return editor;
    }
}
