// CLASSIFICATION: UNCLASSIFIED

/*
 * GeodeticParameters.java
 *
 * Created on April 3, 2007, 2:35 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.parameters;


/**
 *
 * @author comstam
 */
public class GeodeticParameters extends CoordinateSystemParameters
{
  private int heightType;
  
  
  /** Creates a new instance of GeodeticParameters */
  public GeodeticParameters(int coordinateType, int _heightType) 
  {
    super(coordinateType);
    
    heightType = _heightType;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * GeodeticParameters object.
   * .
   * @param    parameters    GeodeticParameters object to compare 
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(GeodeticParameters parameters)
  {
    if(super.equal(parameters) && heightType == parameters.getHeightType())
      return true;
    else
      return false;
  }
  
  
  public int getHeightType()
  {
    return heightType;
  }
  
  public String toString()
  {
	return "GeodeticParameters: Height = " + getHeightType();
  }
}

// CLASSIFICATION: UNCLASSIFIED
