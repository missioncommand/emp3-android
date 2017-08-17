// CLASSIFICATION: UNCLASSIFIED

/*
 * UPSCoordinates.java
 *
 * Created on April 6, 2007, 2:51 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.coordinates;

/**
 *
 * @author comstam
 */
public class UPSCoordinates extends CoordinateTuple
{
  private char hemisphere;
  private double easting;
  private double northing;
  
  
  /** Creates a new instance of UPSCoordinates */
  public UPSCoordinates(int coordinateType) 
  {
    super(coordinateType);
    
    hemisphere = 'N';
    easting = 0;
    northing = 0;
  }
  
  
  public UPSCoordinates(int coordinateType, char _hemisphere, double _easting, double _northing) 
  {
    super(coordinateType);
    
    hemisphere = _hemisphere;
    easting = _easting;
    northing = _northing;
  }
  
  
  public UPSCoordinates(int coordinateType, String _warningMessage, char _hemisphere, double _easting, double _northing) 
  {
    super(coordinateType, _warningMessage);
    
    hemisphere = _hemisphere;
    easting = _easting;
    northing = _northing;
  }
  
  
  public char getHemisphere()
  {
    return hemisphere;
  }
  
  
  public double getEasting()
  {
    return easting;
  }
  
  
  public double getNorthing()
  {
    return northing;
  }
}

// CLASSIFICATION: UNCLASSIFIED
