// CLASSIFICATION: UNCLASSIFIED

/*
 * PolarStereographicScaleFactorParameters.java
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
public class PolarStereographicScaleFactorParameters extends CoordinateSystemParameters
{
  private double centralMeridian;
  private double scaleFactor;
  private char hemisphere;
  private double falseEasting;
  private double falseNorthing;
  
  
  /** Creates a new instance of PolarStereographicScaleFactorParameters */
  public PolarStereographicScaleFactorParameters(int coordinateType, double _centralMeridian, double _scaleFactor, char _hemisphere, double _falseEasting, double _falseNorthing)
  {
    super(coordinateType);
    
    centralMeridian = _centralMeridian;
    scaleFactor = _scaleFactor;
    hemisphere = _hemisphere;
    falseEasting = _falseEasting;
    falseNorthing = _falseNorthing;
  }
  
  
  /**
   * Tests if this object contains the same information as another  
   * PolarStereographicScaleFactorParameters object.
   * .
   * @param    parameters    PolarStereographicScaleFactorParameters object to compare
   * @return   true if the information is the same, otherwise false 
   */
  public boolean equal(PolarStereographicScaleFactorParameters parameters)
  {
    if(super.equal(parameters) && centralMeridian == parameters.getCentralMeridian() && scaleFactor == parameters.getScaleFactor() && hemisphere == parameters.getHemisphere() && falseEasting == parameters.getFalseEasting() && falseNorthing == parameters.getFalseNorthing())
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


  public char getHemisphere()
  {
    return hemisphere;
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
	return "PolarStereographicScaleFactorParameters: CentralMeridian = " + centralMeridian +
	" ScaleFactor = " + scaleFactor +
	" Hemisphere = " + hemisphere +
	" FalseEasting = " + falseEasting +
	" FalseNorthing = " + falseNorthing;
  }
  
}

// CLASSIFICATION: UNCLASSIFIED
