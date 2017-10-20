// CLASSIFICATION: UNCLASSIFIED

/*
 * AOI.java
 *
 * Created on April 3, 2007, 10:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.misc;

/**
 *
 * @author comstam
 */
public class AOI 
{
  private double westLongitude;
  private double eastLongitude;
  private double southLatitude;
  private double northLatitude;
  
  /** Creates a new instance of AOI */
  public AOI() 
  {
    westLongitude = -180;
    eastLongitude = 180;
    southLatitude = -90;
    northLatitude = 90;
  }
  
  
  public AOI(double _westLongitude, double _eastLongitude, double _southLatitude, double _northLatitude) 
  {
    westLongitude = _westLongitude;
    eastLongitude = _eastLongitude;
    southLatitude = _southLatitude;
    northLatitude = _northLatitude;
  }
  
  
  public double getWestLongitude()
  {
    return westLongitude;
  }
  
  
  public double getEastLongitude()
  {
    return eastLongitude;
  }
  
  
  public double getSouthLatitude()
  {
    return southLatitude;
  }
  
  
  public double getNorthLatitude()
  {
    return northLatitude;
  }
}

// CLASSIFICATION: UNCLASSIFIED
