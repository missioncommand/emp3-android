// CLASSIFICATION: UNCLASSIFIED

/*
 * LocalCartesianParameters.java
 *
 * Created on April 6, 2007, 3:10 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.parameters;

/**
 *
 * @author comstam
 */
public class LocalCartesianParameters extends CoordinateSystemParameters 
{
  private double longitude;
  private double latitude;
  private double height;
  private double orientation;

      
  /** Creates a new instance of LocalCartesianParameters */
  public LocalCartesianParameters(int coordinateType, double _longitude, double _latitude, double _height, double _orientation) 
  {
     super(coordinateType);
    
    longitude = _longitude;
    latitude = _latitude;
    height = _height;
    orientation = _orientation;
 }
  
  
  /**
   * Tests if this object contains the same information as another  
   * LocalCartesianParameters object.
   * .
   * @param    parameters    LocalCartesianParameters object to compare 
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(LocalCartesianParameters parameters)
  {
    if(super.equal(parameters) && longitude == parameters.getLongitude() && latitude == parameters.getLatitude() && height == parameters.getHeight() && orientation == parameters.getOrientation())
      return true;
    else
      return false;
  }
  
  
  public double getLongitude()
  {
    return longitude;
  }


  public double getLatitude()
  {
    return latitude;
  }


  public double getHeight()
  {
    return height;
  }


  public double getOrientation()
  {
    return orientation;
  }  
}

// CLASSIFICATION: UNCLASSIFIED
