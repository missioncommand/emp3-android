// CLASSIFICATION: UNCLASSIFIED

/*
 * BNGCoordinates.java
 *
 * Created on April 6, 2007, 2:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.coordinates;


/**
 * Contains fields and access functions for an MGRS coordinate
 * string. 
 * 
 * @author comstam
 */
public class BNGCoordinates extends StringCoordinates 
{
  /**
   * Initializes the coordinate type and sets the 
   * coordinate string to a default value.
   *
   * @param    coordinateType    type of coordinate this class represents  
   * @see      ccs.enumerations.CoordinateType	      
   */
  public BNGCoordinates(int coordinateType) 
  {
    super(coordinateType);
    
    coordinateString = "SV 0000000000";
  }
  
  
  /**
   * Initializes the coordinate type and precision and sets the 
   * coordinate string to a default value.
   *
   * @param    coordinateType    type of coordinate this class represents  
   * @param    _precision        precision value  
   * @see      ccs.enumerations.CoordinateType	      
   */
  public BNGCoordinates(int coordinateType, int _precision) 
  {
    super(coordinateType, _precision);
    
    coordinateString = "SV 0000000000";
  }
  
  
  /**
   * Initializes the coordinate type, coordinate string and precision values.
   *
   * @param    coordinateType        type of coordinate this class represents 
   * @param    _coordinateString     MGRS coordinate string
   * @see      ccs.enumerations.CoordinateType	      
   */
  public BNGCoordinates(int coordinateType, String _coordinateString, int _precision)
  {
    super(coordinateType, _coordinateString, _precision);
  }
  /**
   * Initializes the coordinate type, coordinate string and precision values.
   *
   * @param    coordinateType        type of coordinate this class represents 
   * @param    _coordinateString     MGRS coordinate string
   * @see      ccs.enumerations.CoordinateType	      
   */
  public BNGCoordinates(int coordinateType, String _warningMessage, String _coordinateString, int _precision)
  {
    super(coordinateType, _warningMessage, _coordinateString, _precision);
  }
}

// CLASSIFICATION: UNCLASSIFIED
