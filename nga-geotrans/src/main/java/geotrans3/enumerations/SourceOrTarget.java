// CLASSIFICATION: UNCLASSIFIED

/*
 * SourceOrTarget.java
 *
 * Created on April 4, 2001, 10:13 AM
 */

package geotrans3.enumerations;

/**
 * Defines valid coordinate conversion direction values, source and target. 
 * 
 * @author comstam
 */
public class SourceOrTarget
{
  public final static String SOURCE_STR = "Input";
  public final static String TARGET_STR = "Output";
  public final static int SOURCE = 0;
  public final static int TARGET = 1;

  
  /**
   * Returns a string for the name of the given direction.
   *
   * @param    index    direction
   * @return   string value representing teh name of the direction 
   */
  public static String name(int index)
  {
    switch(index)
    {
      case SOURCE:
        return SOURCE_STR;
      case TARGET:
        return TARGET_STR;
      default:
        return SOURCE_STR;
    }
  }

}// CLASSIFICATION: UNCLASSIFIED
