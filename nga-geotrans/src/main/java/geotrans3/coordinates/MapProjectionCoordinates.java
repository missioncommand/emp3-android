// CLASSIFICATION: UNCLASSIFIED

/*
 * MapProjectionCoordinates.java
 *
 * Created on April 6, 2007, 2:45 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.coordinates;

/**
 *
 * @author comstam
 */
public class MapProjectionCoordinates extends CoordinateTuple 
{
  private double easting;
  private double northing;
  
  
  public MapProjectionCoordinates(int coordinateType) 
  {
    super(coordinateType);
    
    easting = 0;
    northing = 0;
  }
  
  
  /** Creates a new instance of MapProjectionCoordinates */
  public MapProjectionCoordinates(int coordinateType, double _easting, double _northing) 
  {
    super(coordinateType);
    
    easting = _easting;
    northing = _northing;
  }
  
  
  public MapProjectionCoordinates(int coordinateType, String _warningMessage, double _easting, double _northing) 
  {
    super(coordinateType, _warningMessage);
    
    easting = _easting;
    northing = _northing;
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

    s = String.valueOf(this.easting) + " " 
      + String.valueOf(this.northing);

    return s;
  }

}

// CLASSIFICATION: UNCLASSIFIED
