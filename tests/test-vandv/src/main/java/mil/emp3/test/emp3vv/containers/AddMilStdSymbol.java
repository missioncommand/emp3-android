package mil.emp3.test.emp3vv.containers;

import android.app.Activity;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.test.emp3vv.common.StyleManager;
import mil.emp3.test.emp3vv.containers.dialogs.milstdtacticalgraphics.TacticalGraphicPropertiesDialog;
import mil.emp3.test.emp3vv.containers.dialogs.milstdunits.SymbolPropertiesDialog;
import mil.emp3.test.emp3vv.dialogs.utils.ErrorDialog;
import mil.emp3.test.emp3vv.utils.PositionUtility;

/**
 * Most of the code here was ported from test-basic. It shows two dialogs , one for single point mil std symbols and another for
 * tactical graphics. It process the callback to add the feature to specified overlay. It has to complete the feature with position before
 * adding it.
 */
public class AddMilStdSymbol extends AddEntityBase implements SymbolPropertiesDialog.SymbolPropertiesDialogListener, TacticalGraphicPropertiesDialog.SymbolPropertiesDialogListener {

    private static String TAG = AddMilStdSymbol.class.getSimpleName();
    public static String symbolPropertiesFragment = "fragment_symbol_properties_dialog";
    public static String tacticalGraphicPropertiesFragment = "fragment_tactical_graphic_properties_dialog";

    public AddMilStdSymbol(Activity activity, IMap map, IStatusListener statusListener, StyleManager styleManager) {
        super(activity, map, statusListener, styleManager);
    }
    
    public String showSymbolPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                SymbolPropertiesDialog featurePropertiesDlg = SymbolPropertiesDialog.newInstance("Symbol Properties", map, parentList, featureName, visible, AddMilStdSymbol.this);
                featurePropertiesDlg.show(fm, "fragment_symbol_properties_dialog");
            }
        };
        mainHandler.post(myRunnable);
        return symbolPropertiesFragment;
    }

    public String showTacticalGraphicPropertiesDialog(final List<String> parentList, final String featureName, final boolean visible) {
        Handler mainHandler = new Handler(activity.getMainLooper());

        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                FragmentManager fm = ((AppCompatActivity)activity).getSupportFragmentManager();
                TacticalGraphicPropertiesDialog tacticalGraphicPropertiesDialog = TacticalGraphicPropertiesDialog.newInstance("Symbol Properties", map, parentList, featureName, visible, AddMilStdSymbol.this);
                tacticalGraphicPropertiesDialog.show(fm, "fragment_tactical_graphic_properties_dialog");
            }
        };
        mainHandler.post(myRunnable);
        return tacticalGraphicPropertiesFragment;
    }

    @Override
    public void onSymbolPropertiesSaveClick(SymbolPropertiesDialog dialog) {

        try {
            IFeature newSymbol = initializeSinglePoint(map, dialog.getMilStdVersion(), dialog.getSymbolCode(), dialog.getFeatureName(), dialog.getCurrentUnitDef(), dialog.getPositionUtility(), dialog.getModifiers());
            applyStyle(newSymbol);
            addFeature2Map(map, dialog.getParentList(), dialog.isFeatureVisible(), newSymbol);
        } catch (Exception e) {
            Log.e(TAG, "onSymbolPropertiesSaveClick", e);
            statusListener.updateStatus(TAG, e.getMessage());
            ErrorDialog.showError(activity, "Failed to add Feature " + e.getMessage());
        }
    }

    @Override
    public void onSymbolPropertiesCancelClick(SymbolPropertiesDialog dialog) {

    }

    @Override
    public boolean onSaveClick(TacticalGraphicPropertiesDialog dialog) {
        try {
            IFeature newSymbol = initializeTacticalGraphicsSymbol(map, dialog.getMilStdVersion(), dialog.getSymbolCode(), dialog.getFeatureName(), dialog.getCurrentDef(), dialog.getPositionUtility(), dialog.getModifiers());
            applyStyle(newSymbol);
            addFeature2Map(map, dialog.getParentList(), dialog.isFeatureVisible(), newSymbol);
            return true;
        } catch (Exception e) {
            ErrorDialog.showError(activity, "Failed to add Feature " + e.getMessage());
        }
        return false;
    }

    @Override
    public void onCancelClick(TacticalGraphicPropertiesDialog dialog) {

    }

    public static MilStdSymbol initializeSinglePoint(IMap map, IGeoMilSymbol.SymbolStandard milStdVersion, String symbolCode, String featureName,
                                                     armyc2.c2sd.renderer.utilities.UnitDef symbolDef, PositionUtility positionUtility, HashMap<IGeoMilSymbol.Modifier, String> modifierMap) throws EMP_Exception{
        MilStdSymbol newSymbol = new MilStdSymbol(milStdVersion,symbolCode);
        newSymbol.setModifiers(modifierMap);
        newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, symbolDef.getDescription());
        newSymbol.getPositions().clear();
        newSymbol.getPositions().addAll(positionUtility.getPositionList());
        newSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);
        newSymbol.setName(featureName);
        return newSymbol;
    }

    public static MilStdSymbol initializeTacticalGraphicsSymbol(IMap map, IGeoMilSymbol.SymbolStandard milStdVersion, String symbolCode, String featureName,
                                                                armyc2.c2sd.renderer.utilities.SymbolDef symbolDef, PositionUtility positionUtility, HashMap<IGeoMilSymbol.Modifier, String> modifierMap ) throws EMP_Exception, IllegalStateException {
        MilStdSymbol newSymbol = new MilStdSymbol(milStdVersion, symbolCode);
        newSymbol.setModifiers(modifierMap);
        newSymbol.setSymbolCode(symbolCode);
        newSymbol.setName(featureName);
        newSymbol.setAltitudeMode(IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND);

        Log.d(TAG, "initializeTacticalGraphicsSymbol min point " + symbolDef.getMinPoints() + " max points " + symbolDef.getMaxPoints());

        if(symbolDef.getMinPoints() > positionUtility.getPositionList().size()) {
            throw new IllegalStateException("Required point " + symbolDef.getMinPoints() + " supplied positions " + positionUtility.getPositionList().size());
        }

        newSymbol.getPositions().clear();
        newSymbol.getPositions().addAll(positionUtility.getPositionList());

//        if (symbolDef.isMultiPoint() && symbolDef.getMinPoints() > 1) {
//            double startLatitude = map.getCamera().getLatitude();
//            double startLongitude = map.getCamera().getLongitude();
//            for (int ii = 1; ii < symbolDef.getMinPoints(); ii++) {
//                IGeoPosition pos = new GeoPosition();
//                pos.setLatitude(startLatitude + (.001 * ii));
//                pos.setLongitude(startLongitude + (.001 * ii));
//                newSymbol.getPositions().add(pos);
//            }
//        }
        switch (SymbolUtilities.getBasicSymbolID(symbolCode)) {
            case "G*G*ALC---****X": // AIR_CORRIDOR:
                switch (milStdVersion) {
                    case MIL_STD_2525C:
                        newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, newSymbol.getName());
                        newSymbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 0, 10000);
                        newSymbol.setModifier(IGeoMilSymbol.Modifier.ALTITUDE_DEPTH, 1, 20000);
                        newSymbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP, "201000Z");
                        newSymbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP_2, "231000Z");
                        break;
                    case MIL_STD_2525B:
                        newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, newSymbol.getName());
                        newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_2, "UD 2");
                        break;
                }
                break;
            case "G*G*GLB---****X": // BOUNDARIES
                newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, "10 Mtn Div");
                newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_2, "3 Inf Div");
                newSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.DIVISION);
                break;
            case "G*G*GLP---****X": // PHASE LINE
                newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, newSymbol.getName());
                break;
            case "G*G*OAO---****X": // OBJECTIVE
                newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, newSymbol.getName());
                break;
            case "G*G*OPP---****X": // POINT OF DEPARTURE
                newSymbol.setModifier(IGeoMilSymbol.Modifier.ADDITIONAL_INFO_1, "Info 1");
                newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, newSymbol.getName());
                newSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.COMPANY_BATTERY_TROOP);
                newSymbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP, "201000Z");
                newSymbol.setModifier(IGeoMilSymbol.Modifier.DATE_TIME_GROUP_2, "231000Z");
                break;
            case "G*S*LRM---****X": // MAIN SUPPLY ROUTE
            case "G*S*LRA---****X": // ALTERNATE SUPPLY ROUTE
            case "G*G*OAK---****X": // ATTACK POSITION
                newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, newSymbol.getName());
                break;
            case "G*G*GAA---****X": // ASSEMBLY AREA
                newSymbol.setModifier(IGeoMilSymbol.Modifier.UNIQUE_DESIGNATOR_1, newSymbol.getName());
                newSymbol.setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier.UNIT, MilStdSymbol.Echelon.COMPANY_BATTERY_TROOP);
                break;
        }
        return newSymbol;
    }

}
