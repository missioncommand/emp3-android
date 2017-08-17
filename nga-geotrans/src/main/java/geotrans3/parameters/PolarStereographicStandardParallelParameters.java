// CLASSIFICATION: UNCLASSIFIED

/*
 * PolarStereographicStandardParallelParameters.java
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
public class PolarStereographicStandardParallelParameters extends CoordinateSystemParameters
{
  private double centralMeridian;
  private double standardParallel;
  private double falseEasting;
  private double falseNorthing;
  
  
  /** Creates a new instance of PolarStereographicStandardParallelParameters */
  public PolarStereographicStandardParallelParameters(int coordinateType, double _centralMeridian, double _standardParallel, double _falseEasting, double _falseNorthing)
  {
    super(coordinateType);
    
    centralMeridian = _centralMeridian;
    standardParallel = _standardParallel;
    falseEasting = _falseEasting;
    falseNorthing = _falseNorthing;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * PolarStereographicStandardParallelParameters object.
   * .
   * @param    parameters    PolarStereographicStandardParallelParameters object to compare
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(PolarStereographicStandardParallelParameters parameters)
  {
    if(super.equal(parameters) && centralMeridian == parameters.getCentralMeridian() && standardParallel == parameters.getStandardParallel() && falseEasting == parameters.getFalseEasting() && falseNorthing == parameters.getFalseNorthing())
      return true;
    else
      return false;
  }
  
  
  public double getCentralMeridian()
  {
    return centralMeridian;
  }


  public double getStandardParallel()
  {
    return standardParallel;
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
	return "PolarStereographicStandardParallelParameters: CentralMeridian = " + centralMeridian +
	" StandardParallel = " + standardParallel +
	" FalseEasting = " + falseEasting +
	" FalseNorthing = " + falseNorthing;
  }
}

// CLASSIFICATION: UNCLASSIFIED
