// CLASSIFICATION: UNCLASSIFIED

/*
 * HeightType.java
 *
 * Created on April 10, 2001, 12:14 PM
 * 
 * MODIFICATION HISTORY:
 *
 * DATE        NAME              DR#               DESCRIPTION
 * 
 * 05/12/10    S Gillis          BAEts26542        MSP TS MSL-HAE conversion 
 *                                                 should use CCS 
 * 01/04/11    J Chelos          BAEts26267        Added EGM2008 BiCubicSpline
 */

package geotrans3.enumerations;

/**
 *
 * @author  amyc
 * @version 
 */
public class HeightType extends Object {

    public final static int NO_HEIGHT = 0;
    public final static int ELLIPSOID_HEIGHT = 1;
    public final static int MSL_EGM96_15M_BL_HEIGHT = 2;
    public final static int MSL_EGM96_VG_NS_HEIGHT = 3;
    public final static int MSL_EGM84_10D_BL_HEIGHT = 4;
    public final static int MSL_EGM84_10D_NS_HEIGHT = 5;
    public final static int MSL_EGM84_30M_BL_HEIGHT = 6;
    public final static int MSL_EGM2008_TWOPOINTFIVEM_BCS_HEIGHT = 7;

}

// CLASSIFICATION: UNCLASSIFIED
