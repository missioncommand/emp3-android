package mil.emp3.api;

import org.cmapi.primitives.GeoMilSymbol;
import org.cmapi.primitives.IGeoMilSymbol;

import java.util.ArrayList;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDefTable;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;


/**
 * This class encapsulates a GeoMilSymbol. It provides additional functionality.
 */
public class MilStdSymbol extends Feature<IGeoMilSymbol> implements IGeoMilSymbol {

    /**
     * This enumeration class defines the MilStd unit affiliation values.
     */
    public enum Affiliation {
        PENDING,
        UNKNOWN,
        FRIEND,
        NEUTRAL,
        HOSTILE,
        ASSUMED_FRIEND,
        SUSPECT,
        EXERCISE_PENDING,
        EXERCISE_UNKNOWN,
        EXERCISE_FRIEND,
        EXERCISE_NEUTRAL,
        EXERCISE_ASSUMED_FRIEND,
        JOKER,
        FAKER;

        @Override
        public String toString() {
            switch (this) {
                case PENDING:
                    return "P";
                case UNKNOWN:
                    return "U";
                case FRIEND:
                    return "F";
                case NEUTRAL:
                    return "N";
                case HOSTILE:
                    return "H";
                case ASSUMED_FRIEND:
                    return "A";
                case SUSPECT:
                    return "S";
                case EXERCISE_PENDING:
                    return "G";
                case EXERCISE_UNKNOWN:
                    return "W";
                case EXERCISE_FRIEND:
                    return "D";
                case EXERCISE_NEUTRAL:
                    return "L";
                case EXERCISE_ASSUMED_FRIEND:
                    return "M";
                case JOKER:
                    return "J";
                case FAKER:
                    return "K";
            }

            return "*";
        }

        /**
         * This static method returns the affiliation enumerated value for the given string representation.
         * @param str
         * @return affiliation enumerated value or null if the string does not represent a valid MilStd affiliation code.
         */
        public static Affiliation fromString(String str) {
            Affiliation eValue = null;

            if ((str == null) || (str.length() < 1)) {
                return eValue;
            }

            switch (str.substring(0, 1)) {
                case "P":
                    eValue = PENDING;
                    break;
                case "U":
                    eValue = UNKNOWN;
                    break;
                case "F":
                    eValue = FRIEND;
                    break;
                case "N":
                    eValue = NEUTRAL;
                    break;
                case "H":
                    eValue = HOSTILE;
                    break;
                case "A":
                    eValue = ASSUMED_FRIEND;
                    break;
                case "S":
                    eValue = SUSPECT;
                    break;
                case "G":
                    eValue = EXERCISE_PENDING;
                    break;
                case "W":
                    eValue = EXERCISE_UNKNOWN;
                    break;
                case "D":
                    eValue = EXERCISE_FRIEND;
                    break;
                case "L":
                    eValue = EXERCISE_NEUTRAL;
                    break;
                case "M":
                    eValue = EXERCISE_ASSUMED_FRIEND;
                    break;
                case "J":
                    eValue = JOKER;
                    break;
                case "K":
                    eValue = FAKER;
                    break;
            }

            return eValue;
        }
    }

    /**
     * This enumeration class defines the echelon symbol modifier of the symbol code position 11.
     */
    public enum EchelonSymbolModifier {
        UNIT,
        HEADQUARTERS,
        TASK_FORCE_HQ,
        FEINT_DUMMY_HQ,
        FEINT_DUMMY_TASK_FORCE_HQ,
        TASK_FORCE,
        FEINT_DUMMY,
        FEINT_DUMMY_TASK_FORCE,
        INSTALLATION,
        MOBILITY,
        TOWED_ARRAY;

        @Override
        public String toString() {
            switch (this) {
                case UNIT:
                    return "-";
                case HEADQUARTERS:
                    return "A";
                case TASK_FORCE_HQ:
                    return "B";
                case FEINT_DUMMY_HQ:
                    return "C";
                case FEINT_DUMMY_TASK_FORCE_HQ:
                    return "D";
                case TASK_FORCE:
                    return "E";
                case FEINT_DUMMY:
                    return "F";
                case FEINT_DUMMY_TASK_FORCE:
                    return "G";
                case INSTALLATION:
                    return "H";
                case MOBILITY:
                    return "M";
                case TOWED_ARRAY:
                    return "N";
            }
            return "-";
        }

        public static EchelonSymbolModifier fromString(String str) {
            EchelonSymbolModifier eValue = null;

            if ((str == null) || (str.length() < 1)) {
                return eValue;
            }

            switch (str.substring(0,1)) {
                case "-":
                case "*":
                    eValue = UNIT;
                    break;
                case "A":
                    eValue = HEADQUARTERS;
                    break;
                case "B":
                    eValue = TASK_FORCE_HQ;
                    break;
                case "C":
                    eValue = FEINT_DUMMY_HQ;
                    break;
                case "D":
                    eValue = FEINT_DUMMY_TASK_FORCE_HQ;
                    break;
                case "E":
                    eValue = TASK_FORCE;
                    break;
                case "F":
                    eValue = FEINT_DUMMY;
                    break;
                case "G":
                    eValue = FEINT_DUMMY_TASK_FORCE;
                    break;
                case "H":
                    eValue = INSTALLATION;
                    break;
                case "M":
                    eValue = MOBILITY;
                    break;
                case "N":
                    eValue = TOWED_ARRAY;
                    break;
            }
            return eValue;
        }
    }

    /**
     * This enumeration class defines the values for the MilStd echelon component of the symbol code (position 12)
     */
    public enum Echelon {
        UNIT,
        TEAM_CREW,
        SQUAD,
        SECTION,
        PLATOON_DETACHMENT,
        COMPANY_BATTERY_TROOP,
        BATTALION_SQUADRON,
        REGIMENT_GROUP,
        BRIGADE,
        DIVISION,
        CORPS_MEF,
        ARMY,
        ARMY_GROUP_FRONT,
        REGION,
        COMMAND;

        @Override
        public String toString() {
            switch (this) {
                case UNIT:
                    return "-";
                case TEAM_CREW:
                    return "A";
                case SQUAD:
                    return "B";
                case SECTION:
                    return "C";
                case PLATOON_DETACHMENT:
                    return "D";
                case COMPANY_BATTERY_TROOP:
                    return "E";
                case BATTALION_SQUADRON:
                    return "F";
                case REGIMENT_GROUP:
                    return "G";
                case BRIGADE:
                    return "H";
                case DIVISION:
                    return "I";
                case CORPS_MEF:
                    return "J";
                case ARMY:
                    return "K";
                case ARMY_GROUP_FRONT:
                    return "L";
                case REGION:
                    return "M";
                case COMMAND:
                    return "N";
            }

            return "-";
        }

        public static Echelon fromString(String str) {
            Echelon eValue = Echelon.UNIT;

            if ((str == null) || (str.length() < 1)) {
                return eValue;
            }

            switch (str.substring(0,1)) {
                case "-":
                    eValue = UNIT;
                    break;
                case "A":
                    eValue = TEAM_CREW;
                    break;
                case "B":
                    eValue = SQUAD;
                    break;
                case "C":
                    eValue = SECTION;
                    break;
                case "D":
                    eValue = PLATOON_DETACHMENT;
                    break;
                case "E":
                    eValue = COMPANY_BATTERY_TROOP;
                    break;
                case "F":
                    eValue = BATTALION_SQUADRON;
                    break;
                case "G":
                    eValue = REGIMENT_GROUP;
                    break;
                case "H":
                    eValue = BRIGADE;
                    break;
                case "I":
                    eValue = DIVISION;
                    break;
                case "J":
                    eValue = CORPS_MEF;
                    break;
                case "K":
                    eValue = ARMY;
                    break;
                case "L":
                    eValue = ARMY_GROUP_FRONT;
                    break;
                case "M":
                    eValue = REGION;
                    break;
                case "N":
                    eValue = COMMAND;
                    break;
            }
            return eValue;
        }
    }

    /**
     * This enumerated class defines the symbol modifier position 12 for installations.
     */
    public enum InstalationEchelon {
        INSTALLATION,
        FEINT_DUMMY_INSTALLATION;

        @Override
        public String toString() {
            switch (this) {
                case FEINT_DUMMY_INSTALLATION:
                    return "B";
            }

            return "-";
        }

        public static InstalationEchelon fromString(String str) {
            InstalationEchelon eValue = null;

            if ((str == null) || (str.length() < 1)) {
                return eValue;
            }

            switch (str.substring(0,1)) {
                case "-":
                    eValue = INSTALLATION;
                    break;
                case "B":
                    eValue = FEINT_DUMMY_INSTALLATION;
                    break;
            }
            return eValue;
        }
    }

    /**
     * This enumeration class defines the symbol modifier of the symbol position 12 for modility.
     */
    public enum MobilityEchelonModifier {
        WHEELED_LIMITED_CROSS_COUNTRY,
        CROSS_COUNTRY,
        TRACKED,
        WHEELED_AND_TRACKED_COMBINATION,
        TOWED,
        RAIL,
        OVER_THE_SNOW,
        SLED,
        PACK_ANIMALS,
        BARGE,
        AMPHIBIOUS;

        @Override
        public String toString() {
            switch (this) {
                case WHEELED_LIMITED_CROSS_COUNTRY:
                    return "O";
                case CROSS_COUNTRY:
                    return "P";
                case TRACKED:
                    return "Q";
                case WHEELED_AND_TRACKED_COMBINATION:
                    return "R";
                case TOWED:
                    return "S";
                case RAIL:
                    return "T";
                case OVER_THE_SNOW:
                    return "U";
                case SLED:
                    return "V";
                case PACK_ANIMALS:
                    return "W";
                case BARGE:
                    return "X";
                case AMPHIBIOUS:
                    return "Y";
            }

            return "-";
        }

        public static MobilityEchelonModifier fromString(String str) {
            MobilityEchelonModifier eValue = null;

            if ((str == null) || (str.length() < 1)) {
                return eValue;
            }

            switch (str.substring(0,1)) {
                case "O":
                    eValue = WHEELED_LIMITED_CROSS_COUNTRY;
                    break;
                case "P":
                    eValue = CROSS_COUNTRY;
                    break;
                case "Q":
                    eValue = TRACKED;
                    break;
                case "R":
                    eValue = WHEELED_AND_TRACKED_COMBINATION;
                    break;
                case "S":
                    eValue = TOWED;
                    break;
                case "T":
                    eValue = RAIL;
                    break;
                case "U":
                    eValue = OVER_THE_SNOW;
                    break;
                case "V":
                    eValue = SLED;
                    break;
                case "W":
                    eValue = PACK_ANIMALS;
                    break;
                case "X":
                    eValue = BARGE;
                    break;
                case "Y":
                    eValue = AMPHIBIOUS;
                    break;
            }
            return eValue;
        }
    }

    /**
     * This enumeration class defines the symbol modifier of the symbol position 12 for towed arrays.
     */
    public enum TowedArrayEchelonModifier {
        SHORT,
        LONG;

        @Override
        public String toString() {
            switch (this) {
                case SHORT:
                    return "S";
                case LONG:
                    return "L";
            }

            return "S";
        }

        public static TowedArrayEchelonModifier fromString(String str) {
            TowedArrayEchelonModifier eValue = null;

            if ((str == null) || (str.length() < 1)) {
                return eValue;
            }

            switch (str.substring(0,1)) {
                case "S":
                    eValue = SHORT;
                    break;
                case "L":
                    eValue = LONG;
                    break;
            }
            return eValue;
        }
    }

    private double dIconScale = 1.0;

    /**
     * This is the default constructor for the class. It creates a GeoMilSymbol that is rendered using
     * the standard MilStd 2525 coloring scheme. Setting the stroke, fill, and or label styles will
     * alter the colors the symbol is rendered in.
     */
    public MilStdSymbol() {
        super(new GeoMilSymbol(), FeatureTypeEnum.GEO_MIL_SYMBOL);
        this.getRenderable().setSymbolCode(null);
        setStrokeStyle(null);
        setFillStyle(null);
        setLabelStyle(null);
    }
    
    /**
     * This constructor creates MilStdSymbol, and sets the
     * MilStd version and symbol code to the values provided.  It creates a GeoMilSymbol that is rendered using
     * the standard MilStd 2525 coloring scheme. Setting the stroke, fill, and or label styles will
     * alter the colors the symbol is rendered in.

     * @param eStandard {@link IGeoMilSymbol.SymbolStandard}
     * @param sSymbolCode A valid MilStd symbol code.
     * @throws mil.emp3.api.exceptions.EMP_Exception
     */
    public MilStdSymbol(IGeoMilSymbol.SymbolStandard eStandard, String sSymbolCode) throws EMP_Exception {
        super(new GeoMilSymbol(), FeatureTypeEnum.GEO_MIL_SYMBOL);
        
        if ((sSymbolCode == null) || (sSymbolCode.length() < 15)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid symbol code.");
        }

        if (eStandard == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid symbol standard.");
        }

        this.setSymbolStandard(eStandard);
        this.setSymbolCode(sSymbolCode);
        setStrokeStyle(null);
        setFillStyle(null);
        setLabelStyle(null);
    }

    /**
     * This constructor creates MilStdSymbol from the the IGeoMilSymbol object provided.
     * If the standard version is not set, it is set to the default value. If the modifiers are
     * null it is set to an empty modifier list.
     * @param oRenderable an object that implements the IGeoMilSymbol interface.
     * @throws mil.emp3.api.exceptions.EMP_Exception This exception is raised if the geo renderable
     * provided has an invalid symbol code.
     */
    public MilStdSymbol(IGeoMilSymbol oRenderable) throws EMP_Exception {
        super(oRenderable, FeatureTypeEnum.GEO_MIL_SYMBOL);

        if (this.getRenderable().getSymbolStandard() == null) {
            this.setSymbolStandard(SymbolStandard.MIL_STD_2525C);
        }

        if (this.getRenderable().getModifiers() == null) {
            this.getRenderable().setModifiers(new java.util.HashMap<IGeoMilSymbol.Modifier, String>());
        }
        
        if ((oRenderable.getSymbolCode() == null) || (oRenderable.getSymbolCode().length() != 15)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "The geo Renderable has an invalid symbol code.");
        }

        if (null == this.getAffiliation()) {
            this.setAffiliation(Affiliation.FRIEND);
        }
    }

    /**
     * This method sets the MilStd 2525 version.
     * @param eStandard See {@link IGeoMilSymbol.SymbolStandard}
     */
    @Override
    public void setSymbolStandard(IGeoMilSymbol.SymbolStandard eStandard) {
        if (eStandard == null) {
            this.getRenderable().setSymbolStandard(SymbolStandard.MIL_STD_2525C);
        } else {
            this.getRenderable().setSymbolStandard(eStandard);
        }
    }

    /**
     * This method retrieves the current MilStd 2525 version.
     * @return {@link IGeoMilSymbol.SymbolStandard}
     */
    @Override
    public IGeoMilSymbol.SymbolStandard getSymbolStandard() {
        return this.getRenderable().getSymbolStandard();
    }

    /**
     * This method sets the MilStd symbol code. If the afiliation is not set, it is set to friend.
     * @param symbolCode A valid MilStd symbol code.
     */
    @Override
    public void setSymbolCode(String symbolCode) {
        if ((symbolCode == null) || (symbolCode.length() != 15)) {
            throw new IllegalArgumentException("Invalid symbol code.");
        }
        char affiliation = SymbolUtilities.getAffiliation(symbolCode);

        this.getRenderable().setSymbolCode(symbolCode);
        if (null == MilStdSymbol.Affiliation.fromString(Character.toString(affiliation))) {
            this.setAffiliation(Affiliation.FRIEND);
        }
    }

    /**
     * This method retries the current MilStd 2525 symbol code.
     * @return String
     */
    @Override
    public String getSymbolCode() {
        return this.getRenderable().getSymbolCode();
    }


    /**
     * This method overrides the symbol's modifier list of the symbol. If the parameter
     * is null the list is cleared.
     * @param modifiers java.util.HashMap<IGeoMilSymbol.Modifier, String>
     */
    @Override
    public void setModifiers(java.util.HashMap<IGeoMilSymbol.Modifier, String> modifiers) {
        if (modifiers == null) {
            this.getRenderable().getModifiers().clear();
        } else {
            this.getRenderable().setModifiers(modifiers);
        }
    }

    /**
     * This method retrieves the list of MilStd 2525 modifiers.
     * @return java.util.HashMap<IGeoMilSymbol.Modifier, String>
     */
    @Override
    public java.util.HashMap<IGeoMilSymbol.Modifier, String> getModifiers() {
        return this.getRenderable().getModifiers();
    }

    /**
     * This method set a single modifier to the value provided. This method overrides the modifier value
     * with the value provided. If the sValue is null the modifier is removed from the list.
     * @param eModifier {@link IGeoMilSymbol.Modifier}
     * @param sValue A proper string value for the modifier indicated. Or null to remove the modifier.
     */
    public void setModifier(IGeoMilSymbol.Modifier eModifier, String sValue) {
        java.util.HashMap<IGeoMilSymbol.Modifier, String> oModifierList =
                this.getRenderable().getModifiers();

        if (sValue == null) {
            oModifierList.remove(eModifier);
        } else {
            oModifierList.put(eModifier, sValue);
        }
    }

    /**
     * This method set a single modifier to the value provided. This method overrides the modifier value
     * with the value provided. If the value is not a number (Float.NaN) the modifier is removed.
     * @param eModifier {@link IGeoMilSymbol.Modifier}
     * @param fValue A proper float value for the modifier indicated.
     */
    public void setModifier(IGeoMilSymbol.Modifier eModifier, float fValue) {
        java.util.HashMap<IGeoMilSymbol.Modifier, String> oModifierList =
                this.getRenderable().getModifiers();

        if (fValue == Float.NaN) {
            oModifierList.remove(eModifier);
        } else {
            oModifierList.put(eModifier, Float.toString(fValue));
        }
    }

    private ArrayList<String> convertToArrayList(String sCommaDelimitedString) {
        ArrayList<String> aValueList = new ArrayList<>(java.util.Arrays.asList(sCommaDelimitedString.split("\\s*,\\s*")));

        if (sCommaDelimitedString.isEmpty()) {
            aValueList = new ArrayList<>();
        } else {
            aValueList = new ArrayList<>(java.util.Arrays.asList(sCommaDelimitedString.split("\\s*,\\s*")));
        }

        return aValueList;
    }

    /**
     * This method inserts the numeric value of a comma delimited modifier at the specified index.
     * @param eModifier See IGeoMilModifier.milModifierName. Not all modifier values are comma delimited values.
     * @param iIndex The index of the value to insert. The first value is 0.
     * @param fValue The numeric value to set. or Float.NaN to remove the value.
     */
    public void setModifier(IGeoMilSymbol.Modifier eModifier, int iIndex , float fValue) {
        java.util.HashMap<IGeoMilSymbol.Modifier, String> oModifierList =
                this.getRenderable().getModifiers();
        String sValue = "";
        ArrayList<String> aValueList;

        if (oModifierList.containsKey(eModifier)) {
            sValue = oModifierList.get(eModifier);
        }

        aValueList = this.convertToArrayList(sValue);

        if ((iIndex >= 0) && (iIndex < aValueList.size())) {
            if (Float.isNaN(fValue)) {
                aValueList.remove(iIndex);
            } else {
                aValueList.add(iIndex, Float.toString(fValue));
            }
        } else if (!Float.isNaN(fValue)) {
            // Add it to the end.
            aValueList.add(Float.toString(fValue));
        }

        sValue = aValueList.toString(); // This operation encloses the string in "[]" we need to remove them.
        if (sValue.length() > 0) {
            sValue = sValue.substring(1, sValue.length() - 1);
        }
        this.setModifier(eModifier, sValue);
    }

    /**
     * This method retrieves the value of the specified modifier.
     * @param eModifier See IGeoMilModifier.milModifierName
     * @return A String value or null if it does not exists.
     */
    public String getStringModifier(IGeoMilSymbol.Modifier eModifier) {
        String sValue = null;
        java.util.HashMap<IGeoMilSymbol.Modifier, String> oModifierList =
                this.getModifiers();

        if (oModifierList.containsKey(eModifier)) {
            sValue = oModifierList.get(eModifier);
        }
        return sValue;
    }

    /**
     * This Method retrieves the numeric value of a specific modifier.
     * @param eModifier See IGeoMilModifier.milModifierName
     * @return The numeric value or NaN if it does not exists or it is not a number.
     */
    public float getNumericModifier(IGeoMilSymbol.Modifier eModifier) {
        String sValue = this.getStringModifier(eModifier);

        if (sValue != null) {
            try {
                return Float.parseFloat(sValue);
            } catch (Exception Ex) {
            }
        }
        return Float.NaN;
    }


    /**
     * This Method retrieves the numeric value of a specific comma delimited modifier at the specified index.
     * @param eModifier See IGeoMilModifier.milModifierName
     * @param iIndex the index of the value requested.
     * @return The numeric value or NaN if it does not exists or it is not a number.
     */
    public float getNumericModifier(IGeoMilSymbol.Modifier eModifier, int iIndex) {
        java.util.HashMap<IGeoMilSymbol.Modifier, String> oModifierList =
                this.getModifiers();
        String sValue = "";
        ArrayList<String> aValueList;

        if (oModifierList.containsKey(eModifier)) {
            sValue = oModifierList.get(eModifier);
        }

        aValueList = this.convertToArrayList(sValue);

        if ((iIndex >= 0) && (iIndex < aValueList.size())) {
            try {
                return Float.parseFloat(aValueList.get(iIndex));
            } catch (Exception Ex) {
            }
        }
        return Float.NaN;
    }

    /**
     * This method retrieves the basic symbol code with no modifiers.
     * @throws IllegalStateException if called before symbol code is set
     * @return basic symbol code
     */
    public String getBasicSymbol() {
        if (getSymbolCode() == null) {
            throw new IllegalStateException("Can't call this method when symbol code is null");
        }
        return SymbolUtilities.getBasicSymbolID(this.getSymbolCode());
    }

    /**
     * This method trues true if the symbol code is a tactical graphic. it returns false otherwise.
     * @throws IllegalStateException if called before symbol code is set
     * @return true if symbol is a tactical graphic
     */
    public boolean isTacticalGraphic() {
        if (getSymbolCode() == null) {
            throw new IllegalStateException("Can't call this method when symbol code is null");
        }
        return SymbolUtilities.isTacticalGraphic(this.getBasicSymbol());
    }

    /**
     * This method returns true if the symbol code represents a single point MilStd.
     * @throws IllegalStateException if called before symbol code is set
     * @return true if symbol code is for a single point
     */
    public boolean isSinglePoint() {
        if (getSymbolCode() == null) {
            throw new IllegalStateException("Can't call this method when symbol code is null");
        }
        boolean ret = false;
        String basicSymbolCode = this.getBasicSymbol();

        if (SymbolUtilities.isTacticalGraphic(basicSymbolCode)) {
            int milstdVersion = (this.getSymbolStandard() == IGeoMilSymbol.SymbolStandard.MIL_STD_2525B)? 0: 1;
            SymbolDef symbolDefinition = SymbolDefTable.getInstance().getSymbolDef(basicSymbolCode, milstdVersion);

            if (symbolDefinition.getDrawCategory() == SymbolDef.DRAW_CATEGORY_POINT) {
                // This to account for TG that are icons.
                ret = true;
            }
        } else {
            ret = true;
        }

        return ret;
    }

    /**
     * This method retrieves the affiliation value set in the symbol code.
     * @throws IllegalStateException if called before symbol code is set
     * @return {@link MilStdSymbol.Affiliation}
     */
    public MilStdSymbol.Affiliation getAffiliation() {
        if (getSymbolCode() == null) {
            throw new IllegalStateException("Can't call this method when symbol code is null");
        }
        String sSymbolCode = this.getSymbolCode();

        char affiliation = SymbolUtilities.getAffiliation(sSymbolCode);
        return MilStdSymbol.Affiliation.fromString(Character.toString(affiliation));
    }

    /**
     * This method set the symbol code affiliation of the symbol code.
     * @throws IllegalStateException if called before symbol code is set
     * @param eAffiliation {@link MilStdSymbol.Affiliation}
     * @throws IllegalArgumentException if eAffiliation is null
     */
    public void setAffiliation(MilStdSymbol.Affiliation eAffiliation) {
        if (eAffiliation == null) {
            throw new IllegalArgumentException("Affiliation can not be null.");
        }
        if (getSymbolCode() == null) {
            throw new IllegalStateException("Can't call this method when symbol code is null");
        }
        String sSymbolCode = this.getSymbolCode();
        
        sSymbolCode = SymbolUtilities.setAffiliation(sSymbolCode, eAffiliation.toString());
        this.getRenderable().setSymbolCode(sSymbolCode);
    }

    private void setEchelonField(String sStr) {
        if ((sStr == null) || (sStr.length() < 2)) {
            throw new IllegalArgumentException("Invalid echelon.");
        }
        if (getSymbolCode() == null) {
            throw new IllegalStateException("Can't call this method when symbol code is null");
        }
        String sSymbolCode = this.getSymbolCode();

        sSymbolCode = sSymbolCode.substring(0, 10) + sStr.substring(0,2) + sSymbolCode.substring(12);
        this.getRenderable().setSymbolCode(sSymbolCode);
    }

    /**
     * This method sets the symbol code echelon modifier (position 11, 12)
     * @param eModifier {@link MilStdSymbol.EchelonSymbolModifier}
     * @param eEchelon {@link MilStdSymbol.Echelon}
     * @throws EMP_Exception
     */
    public void setEchelonSymbolModifier(MilStdSymbol.EchelonSymbolModifier eModifier, MilStdSymbol.Echelon eEchelon)
            throws EMP_Exception {
        if (eModifier == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "eModifier can not be null.");
        }

        if (eEchelon == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "eEchelon can not be null.");
        }

        switch (eModifier) {
            case INSTALLATION:
            case MOBILITY:
            case TOWED_ARRAY:
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "eModifier can not be Installation, Mobility, nor Towed Array.");
        }

        this.setEchelonField(eModifier.toString() + eEchelon.toString());
    }

    /**
     * This method set the symbol code echelon modifier for installation symbols.
     * @param eValue {@link InstalationEchelon}
     * @throws EMP_Exception
     */
    public void setEchelonSymbolModifier(InstalationEchelon eValue)
            throws EMP_Exception {
        if (eValue == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "eValue can not be null.");
        }

        this.setEchelonField(EchelonSymbolModifier.INSTALLATION.toString() + eValue.toString());
    }

    /**
     * This method sets the symbol code echelon modifier (position 11,12) for mobility.
     * @param eValue {@link MobilityEchelonModifier}
     * @throws EMP_Exception
     */
    public void setEchelonSymbolModifier(MobilityEchelonModifier eValue)
            throws EMP_Exception {
        if (eValue == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "eValue can not be null.");
        }

        this.setEchelonField(EchelonSymbolModifier.MOBILITY.toString() + eValue.toString());
    }

    /**
     * This method sets the symbol code echelon modifier (position 11,12) of the symbol code of towed arrays.
     * @param eValue {@link TowedArrayEchelonModifier}
     * @throws EMP_Exception
     */
    public void setEchelonSymbolModifier(TowedArrayEchelonModifier eValue)
            throws EMP_Exception {
        if (eValue == null) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "eValue can not be null.");
        }

        this.setEchelonField(EchelonSymbolModifier.TOWED_ARRAY.toString() + eValue.toString());
    }

    /**
     * This method sets the icon scale. It only applies to single point MilStd symbols. The size of the
     * icon will be modified by this factor when rendered.
     * @param dScale A value smaller than 1.0 decreases the size. A value larger than 1.0 increases the size.
     */
    public void setIconScale(double dScale) {
        if (Double.isNaN(dScale)) {
            throw new IllegalArgumentException("Invalid scale value.");
        }
        this.dIconScale = Math.abs(dScale);
    }

    /**
     * This method returns the current scale value.
     * @return
     */
    public double getIconScale() {
        return this.dIconScale;
    }

    /**
     * This method returns true if the symbol is an air track.
     * @throws IllegalStateException if called before symbol code is set
     * @return true if symbol code is for an air track
     */
    public boolean isAirTrack() {
        if (getSymbolCode() == null) {
            throw new IllegalStateException("Can't call this method when symbol code is null");
        }
        return SymbolUtilities.isAirTrack(this.getSymbolCode());
    }

    /**
     * This method returns true if the symbol code is a space track, false otherwise.
     * @throws IllegalStateException if called before symbol code is set
     * @return true if symbol code is a space track
     */
    public boolean isSpaceTrack() {
        boolean bRet = false;
        if (getSymbolCode() == null) {
            throw new IllegalStateException("Can't call this method when symbol code is null");
        }
        if (this.getSymbolCode().length() == 15) {
            if ("S*P".equals(this.getBasicSymbol().substring(0, 3))) {
                bRet = true;
            }
        }
        return bRet;
    }
}
