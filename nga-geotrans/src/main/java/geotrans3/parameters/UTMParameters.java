// CLASSIFICATION: UNCLASSIFIED

/*
 * UTMParameters.java
 *
 * Created on April 3, 2007, 3:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.parameters;


/**
 *
 * @author comstam
 */
public class UTMParameters extends CoordinateSystemParameters
{
  private long zone;
  private long override;
  
  
  /** Creates a new instance of UTMParameters */
  public UTMParameters(int coordinateType, long _zone, long _override) 
  {
    super(coordinateType); 
    
    zone = _zone;
    override = _override;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * UTMParameters object.
   * .
   * @param    parameters    UTMParameters object to compare 
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(UTMParameters parameters)
  {
    if(super.equal(parameters) && zone == parameters.getZone() && override == parameters.getOverride())
      return true;
    else
      return false;
  }
  
  
  public long getZone()
  {
    return zone;
  }
  
  
  public long getOverride()
  {
    return override;
  }

  public String toString()
  {
	return "PolarStereographicStandardParallelParameters: Zone = " + zone +
	" Override = " + override;
  }

}

// CLASSIFICATION: UNCLASSIFIED
