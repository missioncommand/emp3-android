// CLASSIFICATION: UNCLASSIFIED

/*
 * CoordinateSystemParameters.java
 *
 * Created on April 3, 2007, 1:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.parameters;

import geotrans3.coordinates.*;
import geotrans3.enumerations.CoordinateType;


/**
 *
 * @author comstam
 */
public class CoordinateSystemParameters 
{
  protected int coordinateType = CoordinateType.GEODETIC;
  
  
  /** Creates a new instance of CoordinateSystemParameters */
  public CoordinateSystemParameters(int _coordinateType) 
  {
    coordinateType = _coordinateType;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * CoordinateSystemParameters object.
   * .
   * @param    parameters    CoordinateSystemParameters object to compare 
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(CoordinateSystemParameters parameters)
  {
    if(coordinateType == parameters.getCoordinateType())
      return true;
    else
      return false;
  }
  
  
  public int getCoordinateType()
  {
    return coordinateType;
  }  
}

// CLASSIFICATION: UNCLASSIFIED
