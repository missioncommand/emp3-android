// CLASSIFICATION: UNCLASSIFIED

/*
 * CoordinateType.java
 *
 * Created on April 3, 2001, 4:18 PM
 */

package geotrans3.enumerations;

/**
 * Defines the coordinate system type.
 * <pre>
 * ALBERS           : Albers Equal Area Conic
 * BONNE            : Bonne
 * CASSINI          : Cassini 
 * CYLEQA           : Cylindrical Equal Area 
 * ECKERT4          : Eckert 4 
 * ECKERT6          : Eckert 6
 * EQDCYL           : Equidistant Cylindrical 
 * GEOCENTRIC       : Geocentric 
 * GEODETIC         : Geodetic
 * GEOREF           : Georef
 * GARS             : Global Area Reference System (GARS)
 * GNOMONIC         : Gnomonic 
 * LAMBERT_1        : Lambert Conformal Conic (1 Standard Parallel) 
 * LAMBERT_2        : Lambert Conformal Conic (2 Standard Parallel) 
 * LOCCART          : Local Cartesian 
 * MERCATOR         : Mercator (Standard Parallel)
 * MERCATOR_SF      : Mercator Scale Factor
 * MGRS             : Military Grid Reference System (MGRS) 
 * MILLER           : Miller Cylindrical 
 * MOLLWEIDE        : Mollweide 
 * NZMG             : New Zealand Map Grid 
 * NEYS             : Ney's (Modified Lambert Conformal Conic) 
 * OMERC            : Oblique Mercator 
 * ORTHOGRAPHIC     : Orthographic 
 * POLARSTEREO_SP   : Polar Stereographic (Standard Parallel)
 * POLARSTEREO_SF   : Polar Stereographic (Scale Factor)
 * POLYCONIC        : Polyconic 
 * SINUSOIDAL       : Sinusoidal 
 * STEREOGRAPHIC    : Stereographic 
 * TRCYLEQA         : Transverse Cylindrical Equal Area 
 * TRANMERC         : Transverse Mercator
 * UPS              : Universal Polar Stereographic (UPS) 
 * UTM              : Universal Transverse Mercator (UTM)
 * USNG             : United States National Grid (USNG) 
 * GRINTEN          : Van der Grinten
 * WEBMERCATOR      : Web Mercator
 * F16GRS           : F-16 Grid Reference System
 * 
 * </pre>
 * @author comstam
 */
public class CoordinateType
{
  // Indexes
  public final static int ALBERS = 0;
  public final static int AZIMUTHAL = 1;
  public final static int BONNE = 2;
  public final static int BNG = 3;
  public final static int CASSINI = 4;
  public final static int CYLEQA = 5;
  public final static int ECKERT4 = 6;
  public final static int ECKERT6 = 7;
  public final static int EQDCYL = 8;
  public final static int GEOCENTRIC = 9;
  public final static int GEODETIC = 10;
  public final static int GEOREF = 11;
  public final static int GARS = 12;
  public final static int GNOMONIC = 13;
  public final static int LAMBERT_1 = 14;
  public final static int LAMBERT_2 = 15;
  public final static int LOCCART = 16;
  public final static int MERCATOR_SP = 17;
  public final static int MERCATOR_SF = 18;
  public final static int MGRS = 19;
  public final static int MILLER = 20;
  public final static int MOLLWEIDE = 21;
  public final static int NZMG = 22;
  public final static int NEYS = 23;
  public final static int OMERC = 24;
  public final static int ORTHOGRAPHIC = 25;
  public final static int POLARSTEREO_SP = 26;
  public final static int POLARSTEREO_SF = 27;
  public final static int POLYCONIC = 28;
  public final static int SINUSOIDAL = 29;
  public final static int STEREOGRAPHIC = 30;
  public final static int TRCYLEQA = 31;
  public final static int TRANMERC = 32;
  public final static int UPS = 33;
  public final static int UTM = 34;
  public final static int USNG = 35;
  public final static int GRINTEN = 36;
  public final static int WEBMERCATOR = 37;
  public final static int F16GRS = 38;
  
  // Names
  public final static String ALBERS_STR = "Albers Equal Area Conic";
  public final static String AZIMUTHAL_STR = "Azimuthal Equidistant (S)";
  public final static String BONNE_STR = "Bonne";
  public final static String BNG_STR = "British National Grid";
  public final static String CASSINI_STR = "Cassini";
  public final static String CYLEQA_STR = "Cylindrical Equal Area";
  public final static String ECKERT4_STR = "Eckert IV (S)";
  public final static String ECKERT6_STR = "Eckert VI (S)";
  public final static String EQDCYL_STR = "Equidistant Cylindrical (S)";
  public final static String GEOCENTRIC_STR = "Geocentric";
  public final static String GEODETIC_STR = "Geodetic";
  public final static String GEOREF_STR = "GEOREF";
  public final static String GARS_STR = "Global Area Reference System (GARS)";
  public final static String GNOMONIC_STR = "Gnomonic (S)";
  public final static String LAMBERT_1_STR = "Lambert Conformal Conic (1 Standard Parallel)";
  public final static String LAMBERT_2_STR = "Lambert Conformal Conic (2 Standard Parallel)";
  public final static String LOCCART_STR = "Local Cartesian";
  public final static String MERCATOR_SP_STR = "Mercator (Standard Parallel)";
  public final static String MERCATOR_SF_STR = "Mercator (Scale Factor)";
  public final static String MGRS_STR = "Military Grid Reference System (MGRS)";
  public final static String MILLER_STR = "Miller Cylindrical (S)";
  public final static String MOLLWEIDE_STR = "Mollweide (S)";
  public final static String NZMG_STR = "New Zealand Map Grid";
  public final static String NEYS_STR = "Ney's (Modified Lambert Conformal Conic)";
  public final static String OMERC_STR = "Oblique Mercator";
  public final static String ORTHOGRAPHIC_STR = "Orthographic (S)";
  public final static String POLARSTEREO_SP_STR = "Polar Stereographic (Standard Parallel)";
  public final static String POLARSTEREO_SF_STR = "Polar Stereographic (Scale Factor)";
  public final static String POLYCONIC_STR = "Polyconic";
  public final static String SINUSOIDAL_STR = "Sinusoidal";
  public final static String STEREOGRAPHIC_STR = "Stereographic (S)";
  public final static String TRCYLEQA_STR = "Transverse Cylindrical Equal Area";
  public final static String TRANMERC_STR = "Transverse Mercator";
  public final static String UPS_STR = "Universal Polar Stereographic (UPS)";
  public final static String UTM_STR = "Universal Transverse Mercator (UTM)";
  public final static String USNG_STR = "United States National Grid (USNG)";
  public final static String GRINTEN_STR = "Van der Grinten";
  public final static String WEBMERCATOR_STR = "Web Mercator (S)";
  public final static String F16GRS_STR = "F-16 Grid Reference System";
  
  // Codes
  public final static String ALBERS_CODE = "AC";
  public final static String AZIMUTHAL_CODE = "AL";
  public final static String BONNE_CODE = "BF";
  public final static String BNG_CODE = "BN";
  public final static String CASSINI_CODE = "CS";
  public final static String CYLEQA_CODE = "LI";
  public final static String ECKERT4_CODE = "EF";
  public final static String ECKERT6_CODE = "ED";
  public final static String EQDCYL_CODE = "CP";
  public final static String GEOCENTRIC_CODE = "GC";
  public final static String GEODETIC_CODE = "GD";
  public final static String GEOREF_CODE = "GE";
  public final static String GARS_CODE = "GA";
  public final static String GNOMONIC_CODE = "GN";
  public final static String LAMBERT_1_CODE = "L1";
  public final static String LAMBERT_2_CODE = "L2";
  public final static String LOCCART_CODE = "LC";
  public final static String MERCATOR_SP_CODE = "MC";
  public final static String MERCATOR_SF_CODE = "MF";
  public final static String MGRS_CODE = "MG";
  public final static String MILLER_CODE = "MH";
  public final static String MOLLWEIDE_CODE = "MP";
  public final static String NZMG_CODE = "NT";
  public final static String NEYS_CODE = "NY";
  public final static String OMERC_CODE = "OC";
  public final static String ORTHOGRAPHIC_CODE = "OD";
  public final static String POLARSTEREO_SP_CODE = "PG";
  public final static String POLARSTEREO_SF_CODE = "PF";
  public final static String POLYCONIC_CODE = "PH";
  public final static String SINUSOIDAL_CODE = "SA";
  public final static String STEREOGRAPHIC_CODE = "SD";
  public final static String TRCYLEQA_CODE = "TX";
  public final static String TRANMERC_CODE = "TC";
  public final static String UPS_CODE = "UP";
  public final static String UTM_CODE = "UT";
  public final static String USNG_CODE = "US";
  public final static String GRINTEN_CODE = "VA";
  public final static String WEBMERCATOR_CODE = "WM";
  public final static String F16GRS_CODE = "F-16 Grid Reference System";

  
  public static int index(String name)
  {
    name = name.toUpperCase();
    
    if(name.startsWith(GEODETIC_STR.toUpperCase()))
      return GEODETIC;
    else if(name.startsWith(GEOREF_STR.toUpperCase()))
      return GEOREF;
    else if(name.startsWith(GARS_STR.toUpperCase()))
      return GARS;
    else if(name.startsWith(GEOCENTRIC_STR.toUpperCase()))
      return GEOCENTRIC;
    else if(name.startsWith(LOCCART_STR.toUpperCase()))
      return LOCCART;
    else if(name.startsWith(MGRS_STR.toUpperCase()))
      return MGRS;
    else if(name.startsWith(USNG_STR.toUpperCase()))
      return USNG;
    else if(name.startsWith(UTM_STR.toUpperCase()))
      return UTM;
    else if(name.startsWith(UPS_STR.toUpperCase()))
      return UPS;
    else if(name.startsWith(ALBERS_STR.toUpperCase()))
      return ALBERS;
    else if(name.startsWith(AZIMUTHAL_STR.toUpperCase()))
      return AZIMUTHAL;
    else if(name.startsWith(BNG_STR.toUpperCase()))
      return BNG;
    else if(name.startsWith(BONNE_STR.toUpperCase()))
      return BONNE;
    else if(name.startsWith(CASSINI_STR.toUpperCase()))
      return CASSINI;
    else if(name.startsWith(CYLEQA_STR.toUpperCase()))
      return CYLEQA;
    else if(name.startsWith(ECKERT4_STR.toUpperCase()))
      return ECKERT4;
    else if(name.startsWith(ECKERT6_STR.toUpperCase()))
      return ECKERT6;
    else if(name.startsWith(EQDCYL_STR.toUpperCase()))
      return EQDCYL;
    else if(name.startsWith(GNOMONIC_STR.toUpperCase()))
      return GNOMONIC;
    else if(name.startsWith(LAMBERT_1_STR.toUpperCase()) || name.startsWith("Lambert Conformal Conic (1 parallel)".toUpperCase()))
      return LAMBERT_1;
    else if(name.startsWith(LAMBERT_2_STR.toUpperCase()) || name.startsWith("Lambert Conformal Conic (2 parallel)".toUpperCase()))
      return LAMBERT_2;
    else if(name.startsWith(MERCATOR_SP_STR.toUpperCase()) || name.equalsIgnoreCase("Mercator"))
      return MERCATOR_SP;
    else if(name.startsWith(MERCATOR_SF_STR.toUpperCase()))
      return MERCATOR_SF;
    else if(name.startsWith(MILLER_STR.toUpperCase()))
      return MILLER;
    else if(name.startsWith(MOLLWEIDE_STR.toUpperCase()))
      return MOLLWEIDE;
    else if(name.startsWith(NEYS_STR.toUpperCase()))
      return NEYS;
    else if(name.startsWith(NZMG_STR.toUpperCase()))
      return NZMG;
    else if(name.startsWith(OMERC_STR.toUpperCase()))
      return OMERC;
    else if(name.startsWith(ORTHOGRAPHIC_STR.toUpperCase()))
      return ORTHOGRAPHIC;
    else if(name.startsWith(POLARSTEREO_SP_STR.toUpperCase()) || name.equalsIgnoreCase("Polar Stereographic"))
      return POLARSTEREO_SP;
    else if(name.startsWith(POLARSTEREO_SF_STR.toUpperCase()))
      return POLARSTEREO_SF;
    else if(name.startsWith(POLYCONIC_STR.toUpperCase()))
      return POLYCONIC;
    else if(name.startsWith(SINUSOIDAL_STR.toUpperCase()))
      return SINUSOIDAL;
    else if(name.startsWith(STEREOGRAPHIC_STR.toUpperCase()))
      return STEREOGRAPHIC;
    else if(name.startsWith(TRCYLEQA_STR.toUpperCase()))
      return TRCYLEQA;
    else if(name.startsWith(TRANMERC_STR.toUpperCase()))
      return TRANMERC;
    else if(name.startsWith(GRINTEN_STR.toUpperCase()))
      return GRINTEN;
    else if(name.startsWith(WEBMERCATOR_STR.toUpperCase()))
      return WEBMERCATOR;
    else if(name.startsWith(F16GRS_STR.toUpperCase()))
      return F16GRS;
    else
      return GEODETIC;
  }

  
  /**
   * Returns a string for the name of the given coordinate system index.
   *
   * @param    index    coordinate system
   * @return   string value representing the name of the coordinate system 
   */
  public static String name(int index)
  {
    switch(index)
    {
      case ALBERS:
        return ALBERS_STR;
      case AZIMUTHAL:
        return AZIMUTHAL_STR;
      case BONNE:
        return BONNE_STR;
      case BNG:
        return BNG_STR;
      case CASSINI:
        return CASSINI_STR;
      case CYLEQA:
        return CYLEQA_STR;
      case ECKERT4:
        return ECKERT4_STR;
      case ECKERT6:
        return ECKERT6_STR;
      case EQDCYL:
        return EQDCYL_STR;
      case GEOCENTRIC:
        return GEOCENTRIC_STR;
      case GEODETIC:
        return GEODETIC_STR;
      case GEOREF:
        return GEOREF_STR;
      case GARS:
        return GARS_STR;
      case GNOMONIC:
        return GNOMONIC_STR;
      case LAMBERT_1:
        return LAMBERT_1_STR;
      case LAMBERT_2:
        return LAMBERT_2_STR;
      case LOCCART:
        return LOCCART_STR;
      case MERCATOR_SP:
        return MERCATOR_SP_STR;
      case MERCATOR_SF:
        return MERCATOR_SF_STR;
      case MGRS:
        return MGRS_STR;
      case MILLER:
        return MILLER_STR;
      case MOLLWEIDE:
        return MOLLWEIDE_STR;
      case NZMG:
        return NZMG_STR;
      case NEYS:
        return NEYS_STR;
      case OMERC:
        return OMERC_STR;
      case ORTHOGRAPHIC:
        return ORTHOGRAPHIC_STR;
      case POLARSTEREO_SP:
        return POLARSTEREO_SP_STR;
      case POLARSTEREO_SF:
        return POLARSTEREO_SF_STR;
      case POLYCONIC:
        return POLYCONIC_STR;
      case SINUSOIDAL:
        return SINUSOIDAL_STR;
      case STEREOGRAPHIC:
        return STEREOGRAPHIC_STR;
      case TRCYLEQA:
        return TRCYLEQA_STR;
      case TRANMERC:
        return TRANMERC_STR;
      case UPS:
        return UPS_STR;
      case UTM:
        return UTM_STR;
      case USNG:
        return USNG_STR;
      case GRINTEN:
        return GRINTEN_STR;
      case WEBMERCATOR:
        return WEBMERCATOR_STR;
      case F16GRS:
        return F16GRS_STR;
      default:
        return GEODETIC_STR;
    }
  }

  
  /**
   * Returns the coordinate system index for the given coordinate system code.
   *
   * @param    code    coordinate system code
   * @return   index of the coordinate system 
   */
  public static int codeIndex(String code)
  {
    code = code.toUpperCase();
    
    if(code.startsWith(GEODETIC_CODE))
      return GEODETIC;
    else if(code.startsWith(GEOREF_CODE))
      return GEOREF;
    else if(code.startsWith(GARS_CODE))
      return GARS;
    else if(code.startsWith(GEOCENTRIC_CODE))
      return GEOCENTRIC;
    else if(code.startsWith(LOCCART_CODE))
      return LOCCART;
    else if(code.startsWith(MGRS_CODE))
      return MGRS;
    else if(code.startsWith(USNG_CODE))
      return USNG;
    else if(code.startsWith(UTM_CODE))
      return UTM;
    else if(code.startsWith(UPS_CODE))
      return UPS;
    else if(code.startsWith(ALBERS_CODE))
      return ALBERS;
    else if(code.startsWith(AZIMUTHAL_CODE))
      return AZIMUTHAL;
    else if(code.startsWith(BNG_CODE))
      return BNG;
    else if(code.startsWith(BONNE_CODE))
      return BONNE;
    else if(code.startsWith(CASSINI_CODE))
      return CASSINI;
    else if(code.startsWith(CYLEQA_CODE))
      return CYLEQA;
    else if(code.startsWith(ECKERT4_CODE))
      return ECKERT4;
    else if(code.startsWith(ECKERT6_CODE))
      return ECKERT6;
    else if(code.startsWith(EQDCYL_CODE))
      return EQDCYL;
    else if(code.startsWith(GNOMONIC_CODE))
      return GNOMONIC;
    else if(code.startsWith(LAMBERT_1_CODE))
      return LAMBERT_1;
    else if(code.startsWith(LAMBERT_2_CODE))
      return LAMBERT_2;
    else if(code.startsWith(MERCATOR_SP_CODE))
      return MERCATOR_SP;
    else if(code.startsWith(MERCATOR_SF_CODE))
      return MERCATOR_SF;
    else if(code.startsWith(MILLER_CODE))
      return MILLER;
    else if(code.startsWith(MOLLWEIDE_CODE))
      return MOLLWEIDE;
    else if(code.startsWith(NEYS_CODE))
      return NEYS;
    else if(code.startsWith(NZMG_CODE))
      return NZMG;
    else if(code.startsWith(OMERC_CODE))
      return OMERC;
    else if(code.startsWith(ORTHOGRAPHIC_CODE))
      return ORTHOGRAPHIC;
    else if(code.startsWith(POLARSTEREO_SP_CODE))
      return POLARSTEREO_SP;
    else if(code.startsWith(POLARSTEREO_SF_CODE))
      return POLARSTEREO_SF;
    else if(code.startsWith(POLYCONIC_CODE))
      return POLYCONIC;
    else if(code.startsWith(SINUSOIDAL_CODE))
      return SINUSOIDAL;
    else if(code.startsWith(STEREOGRAPHIC_CODE))
      return STEREOGRAPHIC;
    else if(code.startsWith(TRCYLEQA_CODE))
      return TRCYLEQA;
    else if(code.startsWith(TRANMERC_CODE))
      return TRANMERC;
    else if(code.startsWith(GRINTEN_CODE))
      return GRINTEN;
    else if(code.startsWith(WEBMERCATOR_CODE))
      return WEBMERCATOR;
    else if(code.startsWith(F16GRS_CODE))
      return F16GRS;
    else
      return GEODETIC;
  }

  
  /**
   * Returns the coordinate system code for the given coordinate system index.
   *
   * @param    index    coordinate system index
   * @return   code of the coordinate system 
   */
  public static String code(int index)
  {
    switch(index)
    {
      case ALBERS:
        return ALBERS_CODE;
      case AZIMUTHAL:
        return AZIMUTHAL_CODE;
      case BONNE:
        return BONNE_CODE;
      case BNG:
        return BNG_CODE;
      case CASSINI:
        return CASSINI_CODE;
      case CYLEQA:
        return CYLEQA_CODE;
      case ECKERT4:
        return ECKERT4_CODE;
      case ECKERT6:
        return ECKERT6_CODE;
      case EQDCYL:
        return EQDCYL_CODE;
      case GEOCENTRIC:
        return GEOCENTRIC_CODE;
      case GEODETIC:
        return GEODETIC_CODE;
      case GEOREF:
        return GEOREF_CODE;
      case GARS:
        return GARS_CODE;
      case GNOMONIC:
        return GNOMONIC_CODE;
      case LAMBERT_1:
        return LAMBERT_1_CODE;
      case LAMBERT_2:
        return LAMBERT_2_CODE;
      case LOCCART:
        return LOCCART_CODE;
      case MERCATOR_SP:
        return MERCATOR_SP_CODE;
      case MERCATOR_SF:
        return MERCATOR_SF_CODE;
      case MGRS:
        return MGRS_CODE;
      case MILLER:
        return MILLER_CODE;
      case MOLLWEIDE:
        return MOLLWEIDE_CODE;
      case NZMG:
        return NZMG_CODE;
      case NEYS:
        return NEYS_CODE;
      case OMERC:
        return OMERC_CODE;
      case ORTHOGRAPHIC:
        return ORTHOGRAPHIC_CODE;
      case POLARSTEREO_SP:
        return POLARSTEREO_SP_CODE;
      case POLARSTEREO_SF:
        return POLARSTEREO_SF_CODE;
      case POLYCONIC:
        return POLYCONIC_CODE;
      case SINUSOIDAL:
        return SINUSOIDAL_CODE;
      case STEREOGRAPHIC:
        return STEREOGRAPHIC_CODE;
      case TRCYLEQA:
        return TRCYLEQA_CODE;
      case TRANMERC:
        return TRANMERC_CODE;
      case UPS:
        return UPS_CODE;
      case UTM:
        return UTM_CODE;
      case USNG:
        return USNG_CODE;
      case GRINTEN:
        return GRINTEN_CODE;
      case WEBMERCATOR:
        return WEBMERCATOR_CODE;
      case F16GRS:
        return F16GRS_CODE;
      default:
        return GEODETIC_CODE;
    }
  }
}

// CLASSIFICATION: UNCLASSIFIED
