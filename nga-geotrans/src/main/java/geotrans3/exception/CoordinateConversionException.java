// CLASSIFICATION: UNCLASSIFIED






/*
 * CoordinateConversionException.java
 *
 * Created on April 18, 2007, 1:34 PM
 */

package geotrans3.exception;

/**
 *
 * @author  amyc
 * @version 
 */
public class CoordinateConversionException extends Exception 
{

    /**
 * Creates new CoordinateConversionException without detail message.
     */
    public CoordinateConversionException() 
    {
    }


    /**
 * Constructs an CoordinateConversionException with the specified detail message.
     * @param msg the detail message.
     */
    public CoordinateConversionException(String message) 
    {
       super(message);
      ///   super("Error: \n" + msg);
    }
}



// CLASSIFICATION: UNCLASSIFIED
