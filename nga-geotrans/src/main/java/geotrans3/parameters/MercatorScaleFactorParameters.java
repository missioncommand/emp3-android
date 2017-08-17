// CLASSIFICATION: UNCLASSIFIED

/*
 * MercatorParameters.java
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
public class MercatorScaleFactorParameters extends CoordinateSystemParameters
{
  private double centralMeridian;
  private double scaleFactor;
  private double falseEasting;
  private double falseNorthing;

  
  /** Creates a new instance of MercatorParameters */
  public MercatorScaleFactorParameters(int coordinateType, double _centralMeridian, double _scaleFactor, double _falseEasting, double _falseNorthing) 
  {
    super(coordinateType);
    
    centralMeridian = _centralMeridian;
    scaleFactor = _scaleFactor;
    falseEasting = _falseEasting;
    falseNorthing = _falseNorthing;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * MercatorParameters object.
   * .
   * @param    parameters    MercatorParameters object to compare 
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(MercatorScaleFactorParameters parameters)
  {
    if(super.equal(parameters) && centralMeridian == parameters.getCentralMeridian() && scaleFactor == parameters.getScaleFactor() && falseEasting == parameters.getFalseEasting() && falseNorthing == parameters.getFalseNorthing())
      return true;
    else
      return false;
  }

  
  public double getCentralMeridian()
  {
    return centralMeridian;
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
	return "MercatorScaleFactorParameters: CentralMeridian = " + centralMeridian + 
	" ScaleFactor = " + scaleFactor +
	" FalseEasting = " + falseEasting +
	" FalseNorthing = " + falseNorthing;
  }
}

// CLASSIFICATION: UNCLASSIFIED
