// CLASSIFICATION: UNCLASSIFIED

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package geotrans3.misc;


import geotrans3.coordinates.Accuracy;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.exception.ErrorMessages;


/**
 * <pre>
 *    Source defines a list of source accuracy values: 
 *    circular, linear and spherical values.
 * 
 * ERROR HANDLING
 *
 *    This component checks for input file errors and input parameter errors.
 *    If an invalid value is found, an exception is thrown.
 *    The possible errors are:
 *
 *      Source Index       : Index out of valid range (less than one
 *                           or more than Source_Count)
 *      Source Name        : Source name not found
 *
 * REUSE NOTES
 *
 *    Source is intended for reuse by any application that needs access to 
 *    accuracy sources.
 *
 *    
 * REFERENCES
 *
 *    Further information on Source can be found in the Reuse Manual.
 *
 *    Source originated from :  U.S. Army Topographic Engineering Center (USATEC)
 *                              Geospatial Information Division (GID)
 *                              7701 Telegraph Road
 *                              Alexandria, VA  22310-3864
 *
 * LICENSES
 *
 *    None apply to this component.
 *
 * RESTRICTIONS
 *
 *    Source has no restrictions.
 *
 * ENVIRONMENT
 *
 *    Source was tested and certified in the following environments:
 *
 *    1. Java version 1.5.0_15
 *
 * MODIFICATIONS
 *
 *    Date              Description
 *    ----              -----------
 *    10-09-08          Original Java Code
 * </pre>
 * 
 * @author comstam
 */
public class Source
{
  private static final int SOURCE_NAME_LENGTH = 50;
  private static final int NUMBER_SOURCES = 19;
  
  
  /**
   *  Defines an accuracy source consisting of:
   *  circular error
   *  linear error
   *  spherical error
   */
  private static class AccuracySource
  {
    String name;
    double ce;        
    double le;        
    double se;        
    
    public AccuracySource(String _name, double _ce, double _le, double _se)
    {
      name = _name;
      ce = _ce;
      le = _le;
      se = _se;
    }
  } 

  /**
   *  Table of source accuracy values.
   */
  private static AccuracySource Source_Table[] = 
  { 
    new AccuracySource("Unknown", -1, -1, -1),
    new AccuracySource("User defined (1 meter)", 1, 1, 2),
    new AccuracySource("GPS PPS", 20, 20, 26),
    new AccuracySource("GPS SPS", 20, 20, 26),
    new AccuracySource("1:25,000 City Map", 50, 20, 52),
    new AccuracySource("1:50,000 Topographic Line Map (TLM)", 50, 20, 52),
    new AccuracySource("1:50,000 Combat Chart", 50, 20, 52),
    new AccuracySource("1:100,000 Topographic Line Map (TLM)", 100, 20, 100),
    new AccuracySource("1:250,000 Joint Operations Graphic (JOG)", 250, 100, 258),
    new AccuracySource("1:500,000 Tactical Pilotage Chart (TPC)", 1000, 150, 993),
    new AccuracySource("1:1,000,000 Operational Navigation Chart (ONC)", 2000, 650, 2031),
    new AccuracySource("Digital Terrain Elevation Data (DTED) Level 1", 50, 30, 55),
    new AccuracySource("Digital Terrain Elevation Data (DTED) Level 2", 23, 18, 27),
    new AccuracySource("Digital Feature Analysis Data (DFAD) Level 1", 130, -1, -1),
    new AccuracySource("Digital Feature Analysis Data (DFAD) Level 2", 130, -1, -1),
    new AccuracySource("Controlled Image Base (CIB) [>10m GSD]", 25, -1, -1),
    new AccuracySource("Vector Smart Map (VMAP) Level 1", 250, 100, 258),
    new AccuracySource("Vector Smart Map (VMAP) Level 2 (1:100,000)", 100, 20, 100),
    new AccuracySource("Vector Smart Map (VMAP) Level 2 (1:50,000)", 50, 20, 52)
  };

  
  /**
   * Returns the number of source accuracies.
   * 
   * @return    number of source accuracies
   */
  public static int count()
  { 
    return NUMBER_SOURCES;
  } 
  
  
  /**
   * Returns the index of the source accuracy with the 
   * specified name.
   * 
   * @param    name    source name being searched for
   * @return   index of the source accuracy with the specified code
   * @throws  CoordinateConversionException    invalid source name
   */
  public static int index(String name) throws CoordinateConversionException
  {
    int index = 0;
    int length = name.length();
    if(length > (SOURCE_NAME_LENGTH - 1))
      throw new CoordinateConversionException("Source: " + ErrorMessages.invalidName);
    else
    {
      String temp_name = name;

      /* Search for code */
      int i = 0;

      while(i < NUMBER_SOURCES && !temp_name.equalsIgnoreCase(Source_Table[i].name))
      {
        i++;
      }
      
      if(i == NUMBER_SOURCES || !temp_name.equalsIgnoreCase(Source_Table[i].name))
        throw new CoordinateConversionException("Source: " + ErrorMessages.invalidName);
      else
        index = i;
    }
    
    return index;
  }
  
  
  /**
   * Returns the name of the source accuracy referenced by index.
   * 
   * @param    index    index of a given source in the source table
   * @return   name of the source accuracy referenced by index
   * @throws  CoordinateConversionException    invalid index
   */
  public static String name(final int index) throws CoordinateConversionException
  {
    if(index < 0 || index >= NUMBER_SOURCES)
      throw new CoordinateConversionException("Source: " + ErrorMessages.invalidIndex);

    return Source_Table[index].name;
  }

  
  /**
   * Returns the accuracy values (CE, LE, SE) for the source referenced by index.
   * 
   * @param    index    index of a given source in the source table
   * @return   accuracy values: circular, linear and spherical errors
   * @throws  CoordinateConversionException    invalid index
   */
  public static Accuracy accuracy(final int index) throws CoordinateConversionException
  {
    if(index < 0 || index >= NUMBER_SOURCES)
      throw new CoordinateConversionException("Source: " + ErrorMessages.invalidIndex);

    return new Accuracy(Source_Table[index].ce, Source_Table[index].le, Source_Table[index].se);
  }
}

// CLASSIFICATION: UNCLASSIFIED
