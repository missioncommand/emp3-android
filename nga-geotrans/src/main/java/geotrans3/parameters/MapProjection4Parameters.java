// CLASSIFICATION: UNCLASSIFIED

/*
 * MapProjection4Parameters.java
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
public class MapProjection4Parameters extends CoordinateSystemParameters 
{
  private double centralMeridian;
  private double originLatitude;
  private double falseEasting;
  private double falseNorthing;
  
  
  /** Creates a new instance of MapProjection4Parameters */
  public MapProjection4Parameters(int coordinateType, double _centralMeridian, double _originLatitude, double _falseEasting, double _falseNorthing) 
  {
    super(coordinateType);
    
    centralMeridian = _centralMeridian;
    originLatitude = _originLatitude;
    falseEasting = _falseEasting;
    falseNorthing = _falseNorthing;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * MapProjection4Parameters object.
   * .
   * @param    parameters    MapProjection4Parameters object to compare 
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(MapProjection4Parameters parameters)
  {
    if(super.equal(parameters) && centralMeridian == parameters.getCentralMeridian() && originLatitude == parameters.getOriginLatitude() && falseEasting == parameters.getFalseEasting() && falseNorthing == parameters.getFalseNorthing())
      return true;
    else
      return false;
  }
  
  
  public double getCentralMeridian()
  {
    return centralMeridian;
  }


  public double getOriginLatitude()
  {
    return originLatitude;
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
	return "MapProjection4Parameters: CentralMeridian = " + centralMeridian +
	" OriginLatitude = " + originLatitude +
	" FalseEasting = " + falseEasting +
	" FalseNorthing = " + falseNorthing;
  }
}

// CLASSIFICATION: UNCLASSIFIED
