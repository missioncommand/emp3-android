// CLASSIFICATION: UNCLASSIFIED

/*
 * ConvertCollectionResults.java
 *
 * Created on April 19, 2007, 4:05 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.coordinates;

/**
 *
 * @author comstam
 */
public class ConvertCollectionResults
{  
  private java.util.Vector coordinateTuple;
  private java.util.Vector accuracy;
  private java.util.Vector warningStr;
  
  /** Creates a new instance of ConvertCollectionResults */
  public ConvertCollectionResults() 
  {
    coordinateTuple = new java.util.Vector();
    accuracy = new java.util.Vector();
    warningStr = new java.util.Vector();
  }
  
  public ConvertCollectionResults(java.util.Vector _coordinateTuple, java.util.Vector _accuracy) 
  {
    coordinateTuple = _coordinateTuple;
    accuracy = _accuracy;
 }
  
  
  
  public ConvertCollectionResults(java.util.Vector _coordinateTuple, java.util.Vector _accuracy, java.util.Vector _warningStr) 
  {
    coordinateTuple = _coordinateTuple;
    accuracy = _accuracy;
    warningStr = _warningStr;
  }
  
  
  public java.util.Vector getCoordinateTuple()
  {
    return coordinateTuple;
  }
  
  
  public java.util.Vector getAccuracy()
  {
    return accuracy;
  }
  
  
  public java.util.Vector getWarningStr()
  {
    return warningStr;
  }
}

// CLASSIFICATION: UNCLASSIFIED
