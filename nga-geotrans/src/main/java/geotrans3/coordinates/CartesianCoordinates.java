// CLASSIFICATION: UNCLASSIFIED

/*
 * CartesianCoordinates.java
 *
 * Created on April 6, 2007, 2:42 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.coordinates;

/**
 *
 * @author comstam
 */
public class CartesianCoordinates extends CoordinateTuple
{
  private double x;
  private double y;
  private double z;
  
  
  /** Creates a new instance of CartesianCoordinates */
  public CartesianCoordinates(int coordinateType) 
  {
    super(coordinateType);
    
    x = 0;
    y = 0;
    z = 0;
  }
  
  
  public CartesianCoordinates(int coordinateType, double _x, double _y, double _z) 
  {
    super(coordinateType);
    
    x = _x;
    y = _y;
    z = _z;
  }
  
  
  public CartesianCoordinates(int coordinateType, String _warningMessage, double _x, double _y, double _z) 
  {
    super(coordinateType, _warningMessage);
    
    x = _x;
    y = _y;
    z = _z;
  }
  
  
  public double getX()
  {
    return x;
  }
  
  
  public double getY()
  {
    return y;
  }
  
  
  public double getZ()
  {
    return z;
  }
}

// CLASSIFICATION: UNCLASSIFIED
