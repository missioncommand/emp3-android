// CLASSIFICATION: UNCLASSIFIED

/*
 * UTMCoordinates.java
 *
 * Created on April 6, 2007, 2:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.coordinates;

/**
 *
 * @author comstam
 */
public class UTMCoordinates extends CoordinateTuple 
{
  private long zone;
  private char hemisphere;
  private double easting;
  private double northing;
  
  /** Creates a new instance of UTMCoordinates */
  public UTMCoordinates(int coordinateType) 
  {
    super(coordinateType);
    
    zone = 0;
    hemisphere = 'N';
    easting = 0;
    northing = 0;
  }
  
  public UTMCoordinates(int coordinateType, long _zone, char _hemisphere, double _easting, double _northing) 
  {
    super(coordinateType);
    
    zone = _zone;
    hemisphere = _hemisphere;
    easting = _easting;
    northing = _northing;
  }
  
  
  public UTMCoordinates(int coordinateType, String _warningMessage, long _zone, char _hemisphere, double _easting, double _northing) 
  {
    super(coordinateType, _warningMessage);
    
    zone = _zone;
    hemisphere = _hemisphere;
    easting = _easting;
    northing = _northing;
  }
  
  
  public long getZone()
  {
    return zone;
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

  public String toString() {
    String s;

    s = String.valueOf(this.zone) + " "
      + String.valueOf(this.hemisphere) + " "	
      + String.valueOf(this.easting) + " "	
      + String.valueOf(this.northing);

    return s;
  }
  
}

// CLASSIFICATION: UNCLASSIFIED
