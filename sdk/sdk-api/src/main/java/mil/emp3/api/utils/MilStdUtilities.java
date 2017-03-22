package mil.emp3.api.utils;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoMilSymbol;
import armyc2.c2sd.renderer.utilities.RendererSettings;
import armyc2.c2sd.renderer.utilities.SymbolUtilities;

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
}
