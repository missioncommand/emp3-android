// CLASSIFICATION: UNCLASSIFIED

/*
 * CoordinateTuple.java
 *
 * Created on April 3, 2007, 1:17 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.coordinates;


import geotrans3.enumerations.CoordinateType;
/**
 *
 * @author comstam
 */
public class CoordinateTuple 
{
  protected  int _id;
  protected int coordinateType;
  protected String errorMessage;
  protected String warningMessage;

  
  /** Creates a new instance of CoordinateTuple */
  public CoordinateTuple() 
  {
    coordinateType = CoordinateType.GEODETIC;
    errorMessage = "";
    warningMessage = "";
  }
  
  
  public CoordinateTuple(int _coordinateType) 
  {
    coordinateType = _coordinateType;
    errorMessage = "";
    warningMessage = "";
  }
  
  
  /**
   * Initializes the coordinate type and
   * warning message.
   *
   * @param    coordinateType    type of coordinate this class represents  
   * @param    warningMessage    warning information returned by a coordinate conversion  
   * @see      msp.ccs.enumerations.CoordinateType	      
   */
  public CoordinateTuple(int _coordinateType, String _warningMessage) 
  {
    coordinateType = _coordinateType;
    warningMessage = _warningMessage;
    errorMessage = "";
  }
  
  
  public int getCoordinateType()
  {
    return coordinateType;
  }
  
  
  /**
   * Sets the error message.
   *
   * @param    errorMessage    error information returned by a coordinate conversion  
   */
  public void setErrorMessage(String _errorMessage)
  {
    errorMessage = _errorMessage;
  }
  
  
  /**
   * Returns the error message.
   *
   * @return   error information returned by a coordinate conversion 
   */
  public String getErrorMessage()
  {
    return errorMessage;
  }
  
  
  /**
   * Sets the warning message.
   *
   * @param    warningMessage    warning information returned by a coordinate conversion  
   */
  public void setWarningMessage(String _warningMessage)
  {
    warningMessage = _warningMessage;
  }
  
  
  /**
   * Returns the warning message.
   *
   * @return   warning information returned by a coordinate conversion 
   */
  public String getWarningMessage()
  {
    return warningMessage;
  }
  
  
  public void set_id( int i )
  {
    _id = i; 
  }
  
  
  public int id()
  { 
    return _id; 
  }
}

// CLASSIFICATION: UNCLASSIFIED
