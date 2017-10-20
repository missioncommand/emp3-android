// CLASSIFICATION: UNCLASSIFIED

/*
 * Precision.java
 *
 * Created on August 15, 2007, 3:26 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.enumerations;


/**
 * Defines valid precision values.
 * <pre>
 * DEGREE                      : Rounds (or truncates) to the nearest 100,000 meters (for Easting/X, Northing/Y, Z, and Ellipsoidal Height values),
 *             or to the nearest 1 degree (for Latitude and Longitude values)
 * TEN_MINUTE                  : Rounds (or truncates) to the nearest 10,000 meters (for Easting/X, Northing/Y, Z, and Ellipsoidal Height values),
 *             or to the nearest 10 minutes (for Latitude and Longitude values)
 * MINUTE                      : Rounds (or truncates) to the nearest 1,000 meters (for Easting/X, Northing/Y, Z, and Ellipsoidal Height values),
 *             or to the nearest 1 minute (for Latitude and Longitude values)
 * TEN_SECOND                  : Rounds (or truncates) to the nearest 100 meters (for Easting/X, Northing/Y, Z, and Ellipsoidal Height values),
 *             or to the nearest 10 seconds, 0.1 minutes, or 0.001 degrees, as appropriate (for Latitude and Longitude values)
 * SECOND                      : Rounds (or truncates) to the nearest 10 meters (for Easting/X, Northing/Y, Z, and Ellipsoidal Height values),
 *             or to the nearest 1 second, 0.01 minutes, or 0.0001 degrees, as appropriate (for Latitude and Longitude values)
 * TENTH_OF_SECOND             : Rounds (or truncates) to the nearest 1 meter (for Easting/X, Northing/Y, Z, and Ellipsoidal Height values),
 *             or to the nearest 0.1 second, 0.001 minutes, or 0.00001 degrees, as appropriate (for Latitude and Longitude values)
 * HUNDRETH_OF_SECOND          : Rounds (or truncates) to the nearest 0.1 meter (for Easting/X, Northing/Y, Z, and Ellipsoidal Height values),
 *             or to the nearest 0.01 second, 0.0001 minutes, or 0.000001 degrees, as appropriate (for Latitude and Longitude values)
 * THOUSANDTH_OF_SECOND        : Rounds (or truncates) to the nearest 0.01 meter (for Easting/X, Northing/Y, Z, and Ellipsoidal Height values),
 *             or to the nearest 0.001 second, 0.00001 minutes, or 0.0000001 degrees, as appropriate (for Latitude and Longitude values)
 * TEN_THOUSANDTH_OF_SECOND    : Rounds (or truncates) to the nearest 0.001 meter (for Easting/X, Northing/Y, Z, and Ellipsoidal Height values),
 *             or to the nearest 0.0001 second, 0.000001 minutes, or 0.00000001degrees, as appropriate (for Latitude and Longitude values)
 * 
 * </pre>
 * @author comstam
 */
public class Precision 
{
  public final static int DEGREE = 0;
  public final static int TEN_MINUTE = 1;
  public final static int MINUTE = 2;
  public final static int TEN_SECOND = 3;
  public final static int SECOND = 4;
  public final static int TENTH_OF_SECOND = 5;
  public final static int HUNDRETH_OF_SECOND = 6;
  public final static int THOUSANDTH_OF_SECOND = 7;
  public final static int TEN_THOUSANDTH_OF_SECOND = 8;  
}

// CLASSIFICATION: UNCLASSIFIED
