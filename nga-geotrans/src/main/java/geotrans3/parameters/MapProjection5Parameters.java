// CLASSIFICATION: UNCLASSIFIED

/*
 * MapProjection5Parameters.java
 *
 * Created on April 4, 2007, 8:50 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.parameters;


/**
 *
 * @author comstam
 */
public class MapProjection5Parameters extends CoordinateSystemParameters
{
  private double centralMeridian;
  private double originLatitude;
  private double scaleFactor;
  private double falseEasting;
  private double falseNorthing;

  
  /** Creates a new instance of MapProjection5Parameters */
  public MapProjection5Parameters(int coordinateType, double _centralMeridian, double _originLatitude, double _scaleFactor, double _falseEasting, double _falseNorthing) 
  {
    super(coordinateType);
    
    centralMeridian = _centralMeridian;
    originLatitude = _originLatitude;
    scaleFactor = _scaleFactor;
    falseEasting = _falseEasting;
    falseNorthing = _falseNorthing;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * MapProjection5Parameters object.
   * .
   * @param    parameters    MapProjection5Parameters object to compare 
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(MapProjection5Parameters parameters)
  {
    if(super.equal(parameters) && centralMeridian == parameters.getCentralMeridian() && originLatitude == parameters.getOriginLatitude() && scaleFactor == parameters.getScaleFactor() && falseEasting == parameters.getFalseEasting() && falseNorthing == parameters.getFalseNorthing())
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


  public double getScaleFactor()
  {
    return scaleFactor;
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
	return "MapProjection5Parameters: CentralMeridian = " + centralMeridian +
	" OriginLatitude = " + originLatitude +
	" ScaleFactor = " + scaleFactor +
	" FalseEasting = " + falseEasting +
	" FalseNorthing = " + falseNorthing;
  }
}
// CLASSIFICATION: UNCLASSIFIED
