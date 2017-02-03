package mil.emp3.dev_test_sdk.utils;

import mil.emp3.api.MilStdSymbol;

/**
 *
 */
public class SymbolCodeUtils {
    public static String setEchelonModifier(String sSymbolCode, String sEchelonModifier) {
        return sSymbolCode.substring(0, 10) + sEchelonModifier.substring(0,1) + sSymbolCode.substring(11);
    }

    public static String setEchelon(String sSymbolCode, String sEchelon) {
        return sSymbolCode.substring(0, 11) + sEchelon.substring(0,1) + sSymbolCode.substring(12);
    }

    public static String setEchelonAndModifier(String sSymbolCode, String sEchelonModifier, String sEchelon) {
        String sTemp = SymbolCodeUtils.setEchelonModifier(sSymbolCode, sEchelonModifier);
        return SymbolCodeUtils.setEchelon(sTemp, sEchelon);
    }

    public static String getDisplayText(MilStdSymbol.Echelon eValue) {
        if (eValue == null) {
            return null;
        }

        switch (eValue) {
            case UNIT:
                return "Unit";
            case TEAM_CREW:
                return "Team//Crew";
            case SQUAD:
                return "Squad";
            case SECTION:
                return "Section";
            case PLATOON_DETACHMENT:
                return "Platoon//Detachment";
            case COMPANY_BATTERY_TROOP:
                return "Company//Battery//Troop";
            case BATTALION_SQUADRON:
                return "Battalion//Squadron";
            case REGIMENT_GROUP:
                return "Regiment//Group";
            case BRIGADE:
                return "Brigate";
            case DIVISION:
                return "Division";
            case CORPS_MEF:
                return "Corps//MEF";
            case ARMY:
                return "Army";
            case ARMY_GROUP_FRONT:
                return "Army//Group//Front";
            case REGION:
                return "Region";
            case COMMAND:
                return "Command";
        }
        return null;
    }

    public static String getDisplayText(MilStdSymbol.InstalationEchelon eValue) {
        if (eValue == null) {
            return null;
        }
        switch (eValue) {
            case INSTALLATION:
                return "Instalation";
            case FEINT_DUMMY_INSTALLATION:
                return "Feint Dummy Instalation";
        }
        return null;
    }

    public static String getDisplayText(MilStdSymbol.MobilityEchelonModifier eValue) {
        if (eValue == null) {
            return null;
        }

        switch (eValue) {
            case WHEELED_LIMITED_CROSS_COUNTRY:
                return "Wheeled Limited Cross Country";
            case CROSS_COUNTRY:
                return "Cross Country";
            case TRACKED:
                return "Tracked";
            case WHEELED_AND_TRACKED_COMBINATION:
                return "Wheeled And Tracked Combination";
            case TOWED:
                return "Towed";
            case RAIL:
                return "Rail";
            case OVER_THE_SNOW:
                return "Over The Snow";
            case SLED:
                return "Sled";
            case PACK_ANIMALS:
                return "Pack Animals";
            case BARGE:
                return "Barge";
            case AMPHIBIOUS:
                return "Amphibious";
        }
        return null;
    }

    public static String getDisplayText(MilStdSymbol.TowedArrayEchelonModifier eValue) {
        if (eValue == null) {
            return null;
        }

        switch (eValue) {
            case SHORT:
                return "Short";
            case LONG:
                return "Long";
        }
        return null;
    }

    public static String getDisplayText(MilStdSymbol.EchelonSymbolModifier eValue) {
        if (eValue == null) {
            return null;
        }

        switch (eValue) {
            case UNIT:
                return "-";
            case HEADQUARTERS:
                return "Headquarters";
            case TASK_FORCE_HQ:
                return "Task Force HQ";
            case FEINT_DUMMY_HQ:
                return "Feint Dummy HQ";
            case FEINT_DUMMY_TASK_FORCE_HQ:
                return "Feint Dummy Task Force HQ";
            case TASK_FORCE:
                return "Task Force";
            case FEINT_DUMMY:
                return "Feint Dummy";
            case FEINT_DUMMY_TASK_FORCE:
                return "Feint Dummy Task Force";
            case INSTALLATION:
                return "Installation";
            case MOBILITY:
                return "Mobility";
            case TOWED_ARRAY:
                return "Towed Array";
        }
        return null;
    }
}
