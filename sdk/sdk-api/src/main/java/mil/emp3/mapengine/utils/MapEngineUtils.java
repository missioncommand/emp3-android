package mil.emp3.mapengine.utils;

import org.cmapi.primitives.IGeoStrokeStyle;

/**
 * This class provides utility functions for the use of the map engines.
 */
public class MapEngineUtils {

    public static final String METOC_PRESSURE_INSTABILITY_LINE = "WA-DPXIL---L---";
    public static final String METOC_PRESSURE_SHEAR_LINE = "WA-DPXSH---L---";
    public static final String METOC_BOUNDED_AREAS_OF_WEATHER_LIQUID_PRECIPITATION_NON_CONVECTIVE_CONTINUOUS_OR_INTERMITTENT = "WA-DBALPC---A--";
    public static final String METOC_ATMOSPHERIC_BOUNDED_AREAS_OF_WEATHER_THUNDERSTORMS = "WA-DBAT-----A--";

/*
    public enum TacticalGraphicFillPattern {
        NONE,
        NBC_BIO_FILL,
        NBC_CCML_FILL,
        SONOBY_FILL_FILL,
        REF_PNT_FILL,
        DEC_PNT_FILL,
        CHK_PNT_FILL,
        CPO_INT_FILL,
        NBC_NUC_FILL
    }

    private static final String NBC_BIO_FILL = "NBCBIOFILL****X";
    private static final String NBC_CCML_FILL = "NBCCMLFILL****X";
    private static final String SONOBY_FILL_FILL = "SONOBYFILL****X";
    private static final String REF_PNT_FILL = "REFPNTFILL****X";
    private static final String DEC_PNT_FILL = "DECPNTFILL****X";
    private static final String CHK_PNT_FILL = "CHKPNTFILL****X";
    private static final String CPO_INT_FILL = "CPOINTFILL****X";
    private static final String NBC_NUC_FILL = "NBCNUCFILL****X";
*/


    /**
     * This method returns a stripple pattern for the given stroke stype pattern.
     * @param strokeStyle
     * @return
     */
    public static short getStipplePattern(IGeoStrokeStyle strokeStyle) {
        short pattern = 0;

        switch (strokeStyle.getStrokePattern()) {
            case solid:
                pattern = 0;
                break;
            case dashed:
                pattern = (short) 0xE73C;
                break;
            case dotted:
                pattern = (short) 0xAAAA;
                break;
        }

        return pattern;
    }

    public static int getStippleFactor(IGeoStrokeStyle strokeStyle) {
        int factor = 0;

        switch (strokeStyle.getStrokePattern()) {
            case solid:
                factor = 0;
                break;
            case dashed:
            case dotted:
                factor = (int) strokeStyle.getStrokeWidth();
                break;
        }

        return factor;
    }
/*
    public static TacticalGraphicFillPattern getTGFillPattern(String symbolCode) {
        TacticalGraphicFillPattern eRet = TacticalGraphicFillPattern.NONE;
        String rendererFillPattern = SymbolUtilities.getTGFillSymbolCode(symbolCode);

        switch (rendererFillPattern) {
            case NBC_BIO_FILL:
                eRet = TacticalGraphicFillPattern.NBC_BIO_FILL;
                break;
            case NBC_CCML_FILL:
                eRet = TacticalGraphicFillPattern.NBC_CCML_FILL;
                break;
            case SONOBY_FILL_FILL:
                eRet = TacticalGraphicFillPattern.SONOBY_FILL_FILL;
                break;
            case REF_PNT_FILL:
                eRet = TacticalGraphicFillPattern.REF_PNT_FILL;
                break;
            case DEC_PNT_FILL:
                eRet = TacticalGraphicFillPattern.DEC_PNT_FILL;
                break;
            case CHK_PNT_FILL:
                eRet = TacticalGraphicFillPattern.CHK_PNT_FILL;
                break;
            case CPO_INT_FILL:
                eRet = TacticalGraphicFillPattern.CPO_INT_FILL;
                break;
            case NBC_NUC_FILL:
                eRet = TacticalGraphicFillPattern.NBC_NUC_FILL;
                break;
        }

        return eRet;
    }
*/

    public static int getTGStippleFactor(String basicSymbolCode, IGeoStrokeStyle strokeStyle) {
        int factor = 0;

        switch (strokeStyle.getStrokePattern()) {
            case solid:
                factor = 0;
                break;
            case dashed:
            case dotted: {
                switch (basicSymbolCode) {
                    case METOC_PRESSURE_INSTABILITY_LINE:
                        factor = 2 * (int) strokeStyle.getStrokeWidth();
                        break;
                    case METOC_PRESSURE_SHEAR_LINE:
                        factor = 2 * (int) strokeStyle.getStrokeWidth();
                        break;
                    case METOC_BOUNDED_AREAS_OF_WEATHER_LIQUID_PRECIPITATION_NON_CONVECTIVE_CONTINUOUS_OR_INTERMITTENT:
                    case METOC_ATMOSPHERIC_BOUNDED_AREAS_OF_WEATHER_THUNDERSTORMS:
                        factor = 3 * (int) strokeStyle.getStrokeWidth();
                        break;
                    default:
                        // Normal dashes.
                        factor = 3 * (int) strokeStyle.getStrokeWidth();
                        break;
                }
                factor = (int) strokeStyle.getStrokeWidth();
                break;
            }
        }

        return factor;
    }

    public static short getTGStipplePattern(String basicSymbolCode, IGeoStrokeStyle strokeStyle) {
        short pattern = 0;

        switch (strokeStyle.getStrokePattern()) {
            case solid:
                pattern = 0;
                break;
            case dashed:
            case dotted: {
                switch (basicSymbolCode) {
                    case METOC_PRESSURE_INSTABILITY_LINE:
                        pattern = (short) 0xDFF6;
                        break;
                    case METOC_PRESSURE_SHEAR_LINE:
                        pattern = (short) 0xFFF6;
                        break;
                    case METOC_BOUNDED_AREAS_OF_WEATHER_LIQUID_PRECIPITATION_NON_CONVECTIVE_CONTINUOUS_OR_INTERMITTENT:
                    case METOC_ATMOSPHERIC_BOUNDED_AREAS_OF_WEATHER_THUNDERSTORMS:
                        pattern = (short) 0xFFF6;
                        break;
                    default:
                        // Normal dashes.
                        pattern = (short) 0xEEEE;
                        break;
                }
                break;
            }
        }

        return pattern;
    }
}
