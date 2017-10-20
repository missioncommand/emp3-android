// CLASSIFICATION: UNCLASSIFIED

/*
 * ConvertResults.java
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
public class ConvertResults
{  
  private CoordinateTuple coordinateTuple;
  private Accuracy accuracy;
  
  
  /** Creates a new instance of ConvertResults */
  public ConvertResults() 
  {
    coordinateTuple = new CoordinateTuple();
    accuracy = new Accuracy();
  }
  
  public ConvertResults(CoordinateTuple _coordinateTuple, Accuracy _accuracy) 
  {
    coordinateTuple = _coordinateTuple;
    accuracy = _accuracy;
 }
  
  
  public CoordinateTuple getCoordinateTuple()
  {
    return coordinateTuple;
  }
  
  
  public Accuracy getAccuracy()
  {
    return accuracy;
  }
}

// CLASSIFICATION: UNCLASSIFIED
