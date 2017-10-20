// CLASSIFICATION: UNCLASSIFIED

/*
 * MapProjection6Parameters.java
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
public class MapProjection6Parameters extends CoordinateSystemParameters
{
  private double centralMeridian;
  private double originLatitude;
  private double standardParallel1;
  private double standardParallel2;
  private double falseEasting;
  private double falseNorthing;

  
  /** Creates a new instance of MapProjection6Parameters */
  public MapProjection6Parameters(int coordinateType, double _centralMeridian, double _originLatitude, double _standardParallel1, double _standardParallel2, double _falseEasting, double _falseNorthing) 
  {
    super(coordinateType);
    
    centralMeridian = _centralMeridian;
    originLatitude = _originLatitude;
    standardParallel1 = _standardParallel1;
    standardParallel2 = _standardParallel2;
    falseEasting = _falseEasting;
    falseNorthing = _falseNorthing;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * MapProjection6Parameters object.
   * .
   * @param    parameters    MapProjection6Parameters object to compare 
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(MapProjection6Parameters parameters)
  {
    if(super.equal(parameters) && centralMeridian == parameters.getCentralMeridian() && originLatitude == parameters.getOriginLatitude() && standardParallel1 == parameters.getStandardParallel1() && standardParallel2 == parameters.getStandardParallel2() && falseEasting == parameters.getFalseEasting() && falseNorthing == parameters.getFalseNorthing())
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


  public double getStandardParallel1()
  {
    return standardParallel1;
  }


  public double getStandardParallel2()
  {
    return standardParallel2;
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
	return "MapProjection6Parameters: CentralMeridian = " + centralMeridian +
	" OriginLatitude = " + originLatitude +
	" StandardParallel1 = " + standardParallel1 +
	" StandardParallel2 = " + standardParallel2 +
	" FalseEasting = " + falseEasting +
	" FalseNorthing = " + falseNorthing;
  }
}

// CLASSIFICATION: UNCLASSIFIED
