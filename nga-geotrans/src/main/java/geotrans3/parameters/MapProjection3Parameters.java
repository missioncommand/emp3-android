// CLASSIFICATION: UNCLASSIFIED

/*
 * MapProjection3Parameters.java
 *
 * Created on April 4, 2007, 9:16 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.parameters;


/**
 *
 * @author comstam
 */
public class MapProjection3Parameters extends CoordinateSystemParameters 
{
  private double centralMeridian;
  private double falseEasting;
  private double falseNorthing;
  
  
  /** Creates a new instance of MapProjection3Parameters */
  public MapProjection3Parameters(int coordinateType, double _centralMeridian, double _falseEasting, double _falseNorthing) 
  {
    super(coordinateType);
    
    centralMeridian = _centralMeridian;
    falseEasting = _falseEasting;
    falseNorthing = _falseNorthing;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * MapProjection3Parameters object.
   * .
   * @param    parameters    MapProjection3Parameters object to compare 
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(MapProjection3Parameters parameters)
  {
    if(super.equal(parameters) && centralMeridian == parameters.getCentralMeridian() && falseEasting == parameters.getFalseEasting() && falseNorthing == parameters.getFalseNorthing())
      return true;
    else
      return false;
  }
  
  
  public double getCentralMeridian()
  {
    return centralMeridian;
  }


  public double getFalseEasting()
  {
    return falseEasting;
  }


  public double getFalseNorthing()
  {
    return falseNorthing;
  }
  
  public String toString()
  {
	return "MapProjection3Parameters: CentralMeridian = " + centralMeridian +
	" FalseEasting = " + falseEasting +
	" FalseNorthing = " + falseNorthing;
  }
}

// CLASSIFICATION: UNCLASSIFIED
