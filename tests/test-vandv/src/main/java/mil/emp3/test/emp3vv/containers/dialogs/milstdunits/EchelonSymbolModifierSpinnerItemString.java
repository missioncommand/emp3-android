package mil.emp3.test.emp3vv.containers.dialogs.milstdunits;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.test.emp3vv.utils.SymbolCodeUtils;

public class EchelonSymbolModifierSpinnerItemString {
    private final MilStdSymbol.EchelonSymbolModifier eEchelonModifier;
    private final MilStdSymbol.Echelon eEchelon;
    private final MilStdSymbol.InstalationEchelon eInstalation;
    private final MilStdSymbol.MobilityEchelonModifier eMobility;
    private final MilStdSymbol.TowedArrayEchelonModifier eTowed;
    private final String sText;

    public EchelonSymbolModifierSpinnerItemString(MilStdSymbol.EchelonSymbolModifier eModifier) {
        this.eEchelonModifier = eModifier;
        this.eEchelon = null;
        this.eInstalation = null;
        this.eMobility = null;
        this.eTowed = null;
        this.sText = SymbolCodeUtils.getDisplayText(eModifier);
    }

    public EchelonSymbolModifierSpinnerItemString(MilStdSymbol.Echelon eValue) {
        this.eEchelonModifier = null;
        this.eEchelon = eValue;
        this.eInstalation = null;
        this.eMobility = null;
        this.eTowed = null;
        this.sText = SymbolCodeUtils.getDisplayText(eValue);
    }

    public EchelonSymbolModifierSpinnerItemString(MilStdSymbol.InstalationEchelon eValue) {
        this.eEchelonModifier = MilStdSymbol.EchelonSymbolModifier.INSTALLATION;
        this.eEchelon = null;
        this.eInstalation = eValue;
        this.eMobility = null;
        this.eTowed = null;
        this.sText = SymbolCodeUtils.getDisplayText(eValue);
    }

    public EchelonSymbolModifierSpinnerItemString(MilStdSymbol.MobilityEchelonModifier eValue) {
        this.eEchelonModifier = MilStdSymbol.EchelonSymbolModifier.MOBILITY;
        this.eEchelon = null;
        this.eInstalation = null;
        this.eMobility = eValue;
        this.eTowed = null;
        this.sText = SymbolCodeUtils.getDisplayText(eValue);
    }

    public EchelonSymbolModifierSpinnerItemString(MilStdSymbol.TowedArrayEchelonModifier eValue) {
        this.eEchelonModifier = MilStdSymbol.EchelonSymbolModifier.TOWED_ARRAY;
        this.eEchelon = null;
        this.eInstalation = null;
        this.eMobility = null;
        this.eTowed = eValue;
        this.sText = SymbolCodeUtils.getDisplayText(eValue);
    }

    public EchelonSymbolModifierSpinnerItemString(String sStr) {
        this.eEchelonModifier = null;
        this.eEchelon = null;
        this.eInstalation = null;
        this.eMobility = null;
        this.eTowed = null;
        this.sText = sStr;
    }

    @Override
    public String toString() {
        return sText;
    }

    public MilStdSymbol.EchelonSymbolModifier getEcheclonSymbolModifier() {
        return this.eEchelonModifier;
    }

    public MilStdSymbol.Echelon getEchelon() {
        return this.eEchelon;
    }

    public MilStdSymbol.InstalationEchelon getInstallationModifier() {
        return this.eInstalation;
    }

    public MilStdSymbol.MobilityEchelonModifier getMobilityModifier() {
        return this.eMobility;
    }

    public MilStdSymbol.TowedArrayEchelonModifier getTowedEchelonSymbolModifier() {
        return this.eTowed;
    }
}
