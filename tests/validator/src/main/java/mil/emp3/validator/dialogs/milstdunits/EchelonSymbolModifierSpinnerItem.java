package mil.emp3.validator.dialogs.milstdunits;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.validator.dialogs.utils.SpinnerWithIconItem;
import mil.emp3.validator.utils.SymbolCodeUtils;

/**
 *
 */
public class EchelonSymbolModifierSpinnerItem extends SpinnerWithIconItem {

    private final MilStdSymbol.EchelonSymbolModifier eEchelonModifier;
    private final MilStdSymbol.Echelon eEchelon;
    private final MilStdSymbol.InstalationEchelon eInstalation;
    private final MilStdSymbol.MobilityEchelonModifier eMobility;
    private final MilStdSymbol.TowedArrayEchelonModifier eTowed;
    private final String sText;

    public EchelonSymbolModifierSpinnerItem(MilStdSymbol.EchelonSymbolModifier eModifier, int iResId) {
        super(iResId);
        this.eEchelonModifier = eModifier;
        this.eEchelon = null;
        this.eInstalation = null;
        this.eMobility = null;
        this.eTowed = null;
        this.sText = SymbolCodeUtils.getDisplayText(eModifier);
    }

    public EchelonSymbolModifierSpinnerItem(MilStdSymbol.Echelon eValue, int iResId) {
        super(iResId);
        this.eEchelonModifier = null;
        this.eEchelon = eValue;
        this.eInstalation = null;
        this.eMobility = null;
        this.eTowed = null;
        this.sText = SymbolCodeUtils.getDisplayText(eValue);
    }

    public EchelonSymbolModifierSpinnerItem(MilStdSymbol.InstalationEchelon eValue, int iResId) {
        super(iResId);
        this.eEchelonModifier = MilStdSymbol.EchelonSymbolModifier.INSTALLATION;
        this.eEchelon = null;
        this.eInstalation = eValue;
        this.eMobility = null;
        this.eTowed = null;
        this.sText = SymbolCodeUtils.getDisplayText(eValue);
    }

    public EchelonSymbolModifierSpinnerItem(MilStdSymbol.MobilityEchelonModifier eValue, int iResId) {
        super(iResId);
        this.eEchelonModifier = MilStdSymbol.EchelonSymbolModifier.MOBILITY;
        this.eEchelon = null;
        this.eInstalation = null;
        this.eMobility = eValue;
        this.eTowed = null;
        this.sText = SymbolCodeUtils.getDisplayText(eValue);
    }

    public EchelonSymbolModifierSpinnerItem(MilStdSymbol.TowedArrayEchelonModifier eValue, int iResId) {
        super(iResId);
        this.eEchelonModifier = MilStdSymbol.EchelonSymbolModifier.TOWED_ARRAY;
        this.eEchelon = null;
        this.eInstalation = null;
        this.eMobility = null;
        this.eTowed = eValue;
        this.sText = SymbolCodeUtils.getDisplayText(eValue);
    }

    public EchelonSymbolModifierSpinnerItem(String sStr, int iResId) {
        super(iResId);
        this.eEchelonModifier = null;
        this.eEchelon = null;
        this.eInstalation = null;
        this.eMobility = null;
        this.eTowed = null;
        this.sText = sStr;
    }

    @Override
    public String getText() {
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
