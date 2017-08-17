// CLASSIFICATION: UNCLASSIFIED

/*
 * StringCoordinates.java
 *
 * Created on April 6, 2007, 2:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.coordinates;


import geotrans3.enumerations.Precision;


/**
 *
 * @author comstam
 */
public class StringCoordinates extends CoordinateTuple 
{
  protected String coordinateString;
  protected int precision;
 
 
  /**
   * Initializes the coordinate type and sets the 
   * string value to a default value.
   *
   * @param    coordinateType    type of coordinate this class represents  
   * @see      msp.ccs.enumerations.CoordinateType	      
   */
  public StringCoordinates(int coordinateType) 
  {
    super(coordinateType);
    
    coordinateString = "";
    precision = Precision.TENTH_OF_SECOND;
  }
  
  
  /**
   * Initializes the coordinate type and sets the 
   * string value to a default value.
   *
   * @param    coordinateType    type of coordinate this class represents  
   * @param    _precision        precision value  
   * @see      msp.ccs.enumerations.CoordinateType	      
   * @see      msp.ccs.enumerations.Precision	      
   */
  public StringCoordinates(int coordinateType, int _precision) 
  {
    super(coordinateType);
    
    coordinateString = "";
    precision = _precision;
  }
  
  
  /**
   * Initializes the coordinate type and sets the 
   * string value to a default value.
   *
   * @param    coordinateType    type of coordinate this class represents  
   * @param    _precision        precision value  
   * @see      msp.ccs.enumerations.CoordinateType	      
   * @see      msp.ccs.enumerations.Precision	      
   */
  public StringCoordinates(int coordinateType, int _precision, String _warningMessage) 
  {
    super(coordinateType, _warningMessage);
    
    coordinateString = "";
    precision = _precision;
  }
  
  
  /**
   * Initializes the coordinate type, 
   * and string value.
   *
   * @param    coordinateType        type of coordinate this class represents 
   * @param    _coordinateString     coordinate string value
   * @see      msp.ccs.enumerations.CoordinateType	      
   */
  public StringCoordinates(int coordinateType, String _coordinateString)
  {
    super(coordinateType);
    
    coordinateString = _coordinateString;
    precision = Precision.TENTH_OF_SECOND;
  }
  
  
  /**
   * Initializes the coordinate type, 
   * and string value.
   *
   * @param    coordinateType        type of coordinate this class represents 
   * @param    _coordinateString     coordinate string value
   * @param    _precision            precision value  
   * @see      msp.ccs.enumerations.CoordinateType	      
   * @see      msp.ccs.enumerations.Precision	      
   */
  public StringCoordinates(int coordinateType, String _coordinateString, int _precision)
  {
    super(coordinateType);
    
    coordinateString = _coordinateString;
    precision = _precision;
  }
  
  
  /**
   * Initializes the coordinate type, 
   * and string value.
   *
   * @param    coordinateType        type of coordinate this class represents 
   * @param    _coordinateString     coordinate string value
   * @param    _precision            precision value  
   * @see      msp.ccs.enumerations.CoordinateType	      
   * @see      msp.ccs.enumerations.Precision	      
   */
  public StringCoordinates(int coordinateType, String _warningMessage, String _coordinateString, int _precision)
  {
    super(coordinateType, _warningMessage);
    
    coordinateString = _coordinateString;
    precision = _precision;
  }
  
  
  /**
   * Sets the coordinate string to a new value.
   *
   * @param    _coordinateString   coordinate string value 
   */
  public void set(String _coordinateString)
  {
    coordinateString = _coordinateString;
  }
  
  
  /**
   * Returns the coordinate string value.
   *
   * @return   coordinate string value
   */
  public String getCoordinateString()
  {
    return coordinateString;
  }
  
  
  /**
   * Returns the precision value.
   *
   * @return   precision value
   */
  public int getPrecision()
  {
    return precision;
  }

  public String toString() {
    return this.coordinateString;
  }

}

// CLASSIFICATION: UNCLASSIFIED
