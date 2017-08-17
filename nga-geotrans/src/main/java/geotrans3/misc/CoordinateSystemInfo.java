// CLASSIFICATION: UNCLASSIFIED

/*
 * CoordinateSystemInfo.java
 *
 * Created on April 19, 2007, 9:13 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.misc;

/**
 *
 * @author comstam
 */
public class CoordinateSystemInfo extends Info
{
  private int coordinateSystem;

  
  /** Creates a new instance of CoordinateSystemInfo */
  public CoordinateSystemInfo() 
  {
    coordinateSystem = 0;
  }
  
  
  public CoordinateSystemInfo(String _code, String _name, int _coordinateSystem)
  {
    super(_code, _name);
    
    coordinateSystem = _coordinateSystem;
  }
  
  
  public int getCoordinateSystem()
  {
    return coordinateSystem;
  }
}

// CLASSIFICATION: UNCLASSIFIED
