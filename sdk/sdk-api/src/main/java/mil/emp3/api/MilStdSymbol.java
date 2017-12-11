package mil.emp3.api;

import android.util.SparseArray;

import org.cmapi.primitives.GeoColor;
import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoMilSymbol;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoLabelStyle;
import org.cmapi.primitives.IGeoMilSymbol;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoStrokeStyle;

import java.util.ArrayList;
import java.util.List;

import armyc2.c2sd.renderer.utilities.MilStdAttributes;
import armyc2.c2sd.renderer.utilities.ModifiersTG;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.SymbolDefTable;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;
import armyc2.c2sd.renderer.utilities.ModifiersUnits;
import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.FontSizeModifierEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.utils.ColorUtils;
import mil.emp3.api.utils.EmpBoundingBox;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.FontUtilities;
import mil.emp3.api.utils.GeographicLib;
import mil.emp3.api.utils.ManagerFactory;


/**
 * This class encapsulates a GeoMilSymbol. It provides additional functionality.
 */
public class MilStdSymbol extends Feature<IGeoMilSymbol> implements IGeoMilSymbol {
    static final private ICoreManager coreManager = ManagerFactory.getInstance().getCoreManager();
    private SparseArray<String> attributes;

    private static final int DEFAULT_SCALE = 1;
    private static final int DEFAULT_PIXEL_SIZE = 150;

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
            if (str == null || str.isEmpty())
                return null;
            return fromChar(str.charAt(0));
        }

        /**
         * This static method returns the affiliation enumerated value for the given string representation.
         * @param chr affiliation character
         * @return affiliation enumerated value or null if the string does not represent a valid MilStd affiliation code.
         */
        public static Affiliation fromChar(char chr) {
            Affiliation eValue = null;

            switch (chr) {
                case 'P':
                    eValue = PENDING;
                    break;
                case 'U':
                    eValue = UNKNOWN;
                    break;
                case 'F':
                    eValue = FRIEND;
                    break;
                case 'N':
                    eValue = NEUTRAL;
                    break;
                case 'H':
                    eValue = HOSTILE;
                    break;
                case 'A':
                    eValue = ASSUMED_FRIEND;
                    break;
                case 'S':
                    eValue = SUSPECT;
                    break;
                case 'G':
                    eValue = EXERCISE_PENDING;
                    break;
                case 'W':
                    eValue = EXERCISE_UNKNOWN;
                    break;
                case 'D':
                    eValue = EXERCISE_FRIEND;
                    break;
                case 'L':
                    eValue = EXERCISE_NEUTRAL;
                    break;
                case 'M':
                    eValue = EXERCISE_ASSUMED_FRIEND;
                    break;
                case 'J':
                    eValue = JOKER;
                    break;
                case 'K':
                    eValue = FAKER;
                    break;
                default:
                    eValue = UNKNOWN;
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
    private String basicSymbolCode = null;
    private Boolean isTacticalGraphic = null;
    private Boolean isSinglePoint = null;

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
     * This method sets the MilStd symbol code. If the affiliation is not set, it is set to friend.
     * @param symbolCode A valid MilStd symbol code.
     */
    @Override
    public void setSymbolCode(String symbolCode) {
        if ((symbolCode == null) || (symbolCode.length() != 15)) {
            throw new IllegalArgumentException("Invalid symbol code.");
        }
        basicSymbolCode = SymbolUtilities.getBasicSymbolID(symbolCode);
        isTacticalGraphic = SymbolUtilities.isTacticalGraphic(basicSymbolCode);
        isSinglePoint = isSinglePoint();
        char affiliation = SymbolUtilities.getAffiliation(symbolCode);

        this.getRenderable().setSymbolCode(symbolCode);
        if (null == MilStdSymbol.Affiliation.fromChar(affiliation)) {
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
        ArrayList<String> aValueList;

        if (sCommaDelimitedString == null || sCommaDelimitedString.isEmpty()) {
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

        String sValue = getModifiers().get(eModifier);

        ArrayList<String> aValueList = this.convertToArrayList(sValue);

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
        return getModifiers().get(eModifier);
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

        String sValue = getModifiers().get(eModifier);

        ArrayList<String> aValueList = this.convertToArrayList(sValue);

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
        validate();
        return SymbolUtilities.getBasicSymbolID(this.getSymbolCode());
    }

    /**
     * This method trues true if the symbol code is a tactical graphic. it returns false otherwise.
     * @throws IllegalStateException if called before symbol code is set
     * @return true if symbol is a tactical graphic
     */
    public boolean isTacticalGraphic() {
        if (isTacticalGraphic != null) {
            return isTacticalGraphic.booleanValue();
        }
        validate();
        return SymbolUtilities.isTacticalGraphic(this.getBasicSymbol());
    }

    /**
     * This method returns true if the symbol code represents a single point MilStd.
     * @throws IllegalStateException if called before symbol code is set
     * @return true if symbol code is for a single point
     */
    public boolean isSinglePoint() {
        if (isSinglePoint != null) {
            return isSinglePoint.booleanValue();
        }
        validate();
        isSinglePoint = false;
        if (basicSymbolCode == null) {
            basicSymbolCode = SymbolUtilities.getBasicSymbolID(this.getSymbolCode());
            isTacticalGraphic = SymbolUtilities.isTacticalGraphic(basicSymbolCode);
        }

        if (isTacticalGraphic) {
            int milstdVersion = (this.getSymbolStandard() == IGeoMilSymbol.SymbolStandard.MIL_STD_2525B)? 0: 1;
            SymbolDef symbolDefinition = SymbolDefTable.getInstance().getSymbolDef(basicSymbolCode, milstdVersion);

            if (symbolDefinition.getDrawCategory() == SymbolDef.DRAW_CATEGORY_POINT) {
                // This to account for TG that are icons.
                isSinglePoint = true;
            }
        } else {
            isSinglePoint = true;
        }

        return isSinglePoint.booleanValue();
    }

    /**
     * This method retrieves the affiliation value set in the symbol code.
     * @throws IllegalStateException if called before symbol code is set
     * @return {@link MilStdSymbol.Affiliation}
     */
    public MilStdSymbol.Affiliation getAffiliation() {
        validate();
        String sSymbolCode = this.getSymbolCode();

        char affiliation = SymbolUtilities.getAffiliation(sSymbolCode);
        return MilStdSymbol.Affiliation.fromChar(affiliation);
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
        validate();
        String sSymbolCode = this.getSymbolCode();
        
        sSymbolCode = SymbolUtilities.setAffiliation(sSymbolCode, eAffiliation.toString());
        this.getRenderable().setSymbolCode(sSymbolCode);
    }

    private void setEchelonField(String sStr) {
        if ((sStr == null) || (sStr.length() < 2)) {
            throw new IllegalArgumentException("Invalid echelon.");
        }
        validate();
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
        validate();
        return SymbolUtilities.isAirTrack(this.getSymbolCode());
    }

    /**
     * This method returns true if the symbol code is a space track, false otherwise.
     * @throws IllegalStateException if called before symbol code is set
     * @return true if symbol code is a space track
     */
    public boolean isSpaceTrack() {
        boolean bRet = false;
        validate();
        if (this.getSymbolCode().length() == 15) {
            if ("S*P".equals(this.getBasicSymbol().substring(0, 3))) {
                bRet = true;
            }
        }
        return bRet;
    }

    @Override
    public void validate() {
        if (getSymbolCode() == null) {
            throw new IllegalStateException("Invalid operation. Symbol code is not set");
        }
    }

    public String getMilSymServiceURL() {
        if (!this.isSinglePoint()) {
            return null;
        }

        String url = "";

        return url;
    }

    /**
     * This method returns the list of modifiers provided in the symbol that match the modifiers
     * listed in the label settings. The return list can be used to call the MilStd icon renderer.
     * @param eLabelSetting The label inclusion setting. {@link MilStdLabelSettingEnum}
     * @return A SpareArry of the modifiers and values.
     */
    public SparseArray<String> getUnitModifiers(MilStdLabelSettingEnum eLabelSetting) {
        String UniqueDesignator1 = null;
        SparseArray<String> oArray = new SparseArray<>();
        java.util.HashMap<IGeoMilSymbol.Modifier, String> geoModifiers = getModifiers();
        java.util.Set<IGeoMilSymbol.Modifier> oLabels = coreManager.getMilStdModifierLabelList(eLabelSetting);

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

        if ((getName() != null) && !getName().isEmpty()) {
            if (eLabelSetting != null) {
                switch (eLabelSetting) {
                    case REQUIRED_LABELS:
                        break;
                    case COMMON_LABELS:
                    case ALL_LABELS:
                        if ((UniqueDesignator1 == null) || UniqueDesignator1.isEmpty() || !UniqueDesignator1.toUpperCase().equals(getName().toUpperCase())) {
                            oArray.put(ModifiersUnits.CN_CPOF_NAME_LABEL, getName());
                        }
                        break;
                }
            }
        }
        return oArray;
    }

    /**
     * This method returns the list of modifiers provided in the symbol that match the modifiers
     * listed in the label settings. The return list can be used to call the MilStd renderer.
     * @param eLabelSetting The label inclusion setting. {@link MilStdLabelSettingEnum}
     * @return A SpareArry of the modifiers and values.
     */
    public SparseArray<String> getTGModifiers(MilStdLabelSettingEnum eLabelSetting) {
        String UniqueDesignator1 = null;
        SparseArray<String> oArray = new SparseArray<>();
        java.util.HashMap<IGeoMilSymbol.Modifier, String> geoModifiers = getModifiers();

        if ((geoModifiers != null) && !geoModifiers.isEmpty()) {
            java.util.Set<IGeoMilSymbol.Modifier> oModifierList = geoModifiers.keySet();

            for (IGeoMilSymbol.Modifier eModifier: oModifierList) {
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

        if ((getName() != null) && !getName().isEmpty()) {
            if (eLabelSetting != null) {
                switch (eLabelSetting) {
                    case REQUIRED_LABELS:
                        break;
                    case COMMON_LABELS:
                    case ALL_LABELS:
                        if ((UniqueDesignator1 == null) || UniqueDesignator1.isEmpty() || !UniqueDesignator1.toUpperCase().equals(getName().toUpperCase())) {
                            oArray.put(ModifiersUnits.CN_CPOF_NAME_LABEL, getName());
                        }
                        break;
                }
            }
        }

        return oArray;
    }

    /**
     * This method returns a list of attribute which can be used to call the MilStd renderer.
     * @param iIconSize              The size of the icon
     * @param selected               True if the feature is currenlt selected.
     * @param selectedStrokeColor    The stroke color for selected features.
     * @param selectedTextColor      The text color for selected features.
     * @return SparseArray of attributes.
     */
    public SparseArray<String> getAttributes(int iIconSize, boolean selected, IGeoColor selectedStrokeColor, IGeoColor selectedTextColor) {
        IGeoColor strokeColor = null;
        IGeoColor textColor = null;
        SparseArray<String> oArray = new SparseArray<>();
        IGeoFillStyle oFillStyle = getFillStyle();
        IGeoStrokeStyle oStrokeStyle = getStrokeStyle();
        IGeoLabelStyle labelStyle = getLabelStyle();


        oArray.put(MilStdAttributes.SymbologyStandard, "" + geoMilStdVersionToRendererVersion());
        oArray.put(MilStdAttributes.PixelSize, "" + iIconSize);
        oArray.put(MilStdAttributes.KeepUnitRatio, "true");
        oArray.put(MilStdAttributes.UseDashArray, "true");

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

        if (isSinglePoint()) {
            oArray.put(MilStdAttributes.FontSize, "" + FontUtilities.getTextPixelSize(labelStyle, FontSizeModifierEnum.NORMAL));
        }

        return oArray;
    }

    public void setSymbolAttributes(final SparseArray<String> attributes) {
        this.attributes = attributes;
    }

    /**
     * This method converts the features MilStd version to a value suitable for calling the MilStd renderer.
     * @return
     */
    public int geoMilStdVersionToRendererVersion() {
        int iVersion = RendererSettings.Symbology_2525C;

        switch (this.getSymbolStandard()) {
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
     * This method returns the symbol definition of a tactical graphic feature.
     * @return a armyc2.c2sd.renderer.utilities.SymbolDef object or null if the feature is NOT a tactical graphic.
     */
    public armyc2.c2sd.renderer.utilities.SymbolDef getTacticalGraphicSymbolDefinition() {
        if (!this.isTacticalGraphic()) {
            return null;
        }

        return armyc2.c2sd.renderer.utilities.SymbolDefTable.getInstance().getSymbolDef(this.getBasicSymbol(), this.geoMilStdVersionToRendererVersion());
    }

    public IEmpBoundingBox getFeatureBoundingBox() {
        if (!this.isTacticalGraphic()) {
            return null;
        }

        IEmpBoundingBox bBox = new EmpBoundingBox();
        armyc2.c2sd.renderer.utilities.SymbolDef symbolDefinition = getTacticalGraphicSymbolDefinition();

        for (IGeoPosition pos: getPositions()) {
            bBox.includePosition(pos.getLatitude(), pos.getLongitude());
        }

        if (getPositions().size() >= symbolDefinition.getMinPoints()) {
            // We need the minimum number of points.
            List<IGeoPosition> posList = getPositions();

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
                    break;
                /**
                 * TG such as:
                 * TACGRP.TSK.ISL,
                 * TACGRP.TSK.OCC,
                 * TACGRP.TSK.RTN,
                 * TACGRP.TSK.SCE
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_AUTOSHAPE: {
                    double dist = GeographicLib.computeDistanceBetween(posList.get(0), posList.get(1));
                    IGeoPosition pos = new GeoPosition();

                    // Compute north.
                    GeographicLib.computePositionAt(0.0, dist, posList.get(0), pos);
                    bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                    // Compute east.
                    GeographicLib.computePositionAt(90.0, dist, posList.get(0), pos);
                    bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                    // Compute south.
                    GeographicLib.computePositionAt(180.0, dist, posList.get(0), pos);
                    bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                    // Compute west.
                    GeographicLib.computePositionAt(270.0, dist, posList.get(0), pos);
                    bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                    break;
                }
                /**
                 * An enclosed polygon with n points
                 * 0 control points
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_POLYGON:
                    break;
                /**
                 * A graphic with n points whose last point defines the width of the graphic.
                 * 1 control point
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_ROUTE:
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
                    break;
                /**
                 * Shape is defined by a single point
                 * 0 control points
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_POINT:
                    break;
                /**
                 * An animated shape, uses the animate function to draw. Super Autoshape draw
                 * in 2 phases, usually one to define length, and one to define width.
                 * 0 control points (every point shapes symbol)
                 *
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_SUPERAUTOSHAPE:
                    break;
                /**
                 * Circle that requires 1 AM modifier value.
                 * See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_CIRCULAR_PARAMETERED_AUTOSHAPE:
                    break;
                /**
                 * Rectangle that requires 2 AM modifier values and 1 AN value.";
                 * See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_RECTANGULAR_PARAMETERED_AUTOSHAPE:
                    break;
                /**
                 * Requires 2 AM values and 2 AN values per sector.
                 * The first sector can have just one AM value although it is recommended
                 * to always use 2 values for each sector.  X values are not required
                 * as our rendering is only 2D for the Sector Range Fan symbol.
                 * See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_SECTOR_PARAMETERED_AUTOSHAPE:
                    break;
                /**
                 *  Requires at least 1 distance/AM value"
                 *  See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_CIRCULAR_RANGEFAN_AUTOSHAPE: {
                    double dist = getNumericModifier(Modifier.DISTANCE, 0);

                    if (dist != Double.NaN) {
                        IGeoPosition pos = new GeoPosition();

                        // Compute north.
                        GeographicLib.computePositionAt(0.0, dist, posList.get(0), pos);
                        bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                        // Compute east.
                        GeographicLib.computePositionAt(90.0, dist, posList.get(0), pos);
                        bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                        // Compute south.
                        GeographicLib.computePositionAt(180.0, dist, posList.get(0), pos);
                        bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                        // Compute west.
                        GeographicLib.computePositionAt(270.0, dist, posList.get(0), pos);
                        bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                    }
                    break;
                }
                /**
                 * Requires 1 AM value.
                 * See ModifiersTG.js for modifier descriptions and constant key strings.
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_TWO_POINT_RECT_PARAMETERED_AUTOSHAPE:
                    double dist = getNumericModifier(Modifier.DISTANCE, 0);

                    if (dist != Double.NaN) {
                        IGeoPosition pos = new GeoPosition();

                        // Compute north.
                        GeographicLib.computePositionAt(0.0, dist, posList.get(0), pos);
                        bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                        // Compute south.
                        GeographicLib.computePositionAt(180.0, dist, posList.get(0), pos);
                        bBox.includePosition(pos.getLatitude(), pos.getLongitude());
                    }
                    break;
                /**
                 * 3D airspace, not a milstd graphic.
                 */
                case armyc2.c2sd.renderer.utilities.SymbolDef.DRAW_CATEGORY_3D_AIRSPACE:
                    break;
            }

            // Now we need to extend the box by ~ 10%.
            double deltaLat = bBox.deltaLatitude();
            double deltaLong = bBox.deltaLongitude();

            if (deltaLat == 0.0) {
                deltaLat = 0.05;
            }
            if (deltaLong == 0.0) {
                deltaLong = 0.05;
            }

            deltaLat *= 0.05;
            deltaLong *= 0.05;

            bBox.includePosition(bBox.getNorth() + deltaLat, bBox.getWest());
            bBox.includePosition(bBox.getSouth() - deltaLat, bBox.getWest());
            bBox.includePosition(bBox.getNorth(), bBox.getWest() - deltaLong);
            bBox.includePosition(bBox.getNorth(), bBox.getEast() + deltaLong);
        }

        return bBox;
    }

    /**
     * Sets Icon color of military symbol.
     * @param color {@link IGeoColor} Color to render icon in.
     */
    public void setIconColor(final IGeoColor color) {
        this.attributes.put(MilStdAttributes.IconColor, ColorUtils.colorToString(color));
    }

    /**
     * Sets the fill color of the symbol.
     * @param color {@link IGeoColor} Color to fill icon with.
     */
    public void setFillColor(final IGeoColor color) {
        this.attributes.put(MilStdAttributes.FillColor, ColorUtils.colorToString(color));
    }

    /**
     * Sets the line stroke color of the icon.
     * @param color {@link IGeoColor} Color to render stroke with.
     */
    public void setLineColor(final IGeoColor color) {
        this.attributes.put(MilStdAttributes.LineColor, ColorUtils.colorToString(color));
    }

    /**
     * Sets text color of label.
     * @param color {@link IGeoColor} Color to render label text in.
     */
    public void setTextColor(final IGeoColor color) {
        this.attributes.put(MilStdAttributes.TextColor, ColorUtils.colorToString(color));
    }

    /**
     * Sets text background color of label.
     * @param color {@link IGeoColor} Color to render background text of label in.
     */
    public void setTextBackgroundColor(final IGeoColor color) {
        this.attributes.put(MilStdAttributes.TextBackgroundColor, ColorUtils.colorToString(color));
    }

    /**
     * Convenience method to color fill, line and icon in one call.
     * @param fillColor - Color of the fill.
     * @param lineColor - Color of the line.
     * @param iconColor - Color of the icon.
     */
    public void styleSymbol(final IGeoColor fillColor, final IGeoColor lineColor, final IGeoColor iconColor) {
        this.setFillColor(fillColor);
        this.setLineColor(lineColor);
        this.setIconColor(iconColor);
    }

    /**
     * Returns the attributes spare array. Use {@link MilStdAttributes} enum as key values.
     * @return {@link SparseArray} array of attributes.
     */
    public SparseArray<String> getAttributes() {
        return this.attributes;
    }
}
