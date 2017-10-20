// CLASSIFICATION: UNCLASSIFIED

/*
 * MercatorStandardParallelParameters.java
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
public class MercatorStandardParallelParameters extends CoordinateSystemParameters
{
  private double centralMeridian;
  private double standardParallel;
  private double scaleFactor;
  private double falseEasting;
  private double falseNorthing;

  
  /** Creates a new instance of MercatorStandardParallelParameters */
  public MercatorStandardParallelParameters(int coordinateType, double _centralMeridian, double _standardParallel, double _scaleFactor, double _falseEasting, double _falseNorthing)
  {
    super(coordinateType);
    
    centralMeridian = _centralMeridian;
    standardParallel = _standardParallel;
    scaleFactor = _scaleFactor;
    falseEasting = _falseEasting;
    falseNorthing = _falseNorthing;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * MercatorParameters object.
   * .
   * @param    parameters    MercatorStandardParallelParameters object to compare
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(MercatorStandardParallelParameters parameters)
  {
    if(super.equal(parameters) && centralMeridian == parameters.getCentralMeridian() && standardParallel == parameters.getStandardParallel() && scaleFactor == parameters.getScaleFactor() && falseEasting == parameters.getFalseEasting() && falseNorthing == parameters.getFalseNorthing())
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
	return "MercatorStandardParallelParameters: CentralMeridian = " + centralMeridian +
	" StandardParallel = " + standardParallel +
	" ScaleFactor = " + scaleFactor +
	" FalseEasting = " + falseEasting +
	" FalseNorthing = " + falseNorthing;
  }

}

// CLASSIFICATION: UNCLASSIFIED
