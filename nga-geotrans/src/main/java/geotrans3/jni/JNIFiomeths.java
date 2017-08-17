// CLASSIFICATION: UNCLASSIFIED

/*
 * JNIFiomeths.java
 *
 * Created on April 9, 2001, 4:22 PM
 */

package geotrans3.jni;


import geotrans3.exception.CoordinateConversionException;
import geotrans3.parameters.CoordinateSystemParameters;


/**
 *
 * @author  amyc
 * @version 
 */
public class JNIFiomeths extends Object
{
  private native long jniFiomethsCreateExampleFile() throws CoordinateConversionException;
  private native long jniFiomethsCreate(String fileName) throws CoordinateConversionException;
  private native void jniFiomethsDestroy(long _fiomethsPtr);
  private native void jniCloseInputFile(long _fiomethsPtr);
  private native void jniCloseOutputFile(long _fiomethsPtr);
  private native void jniConvertFile(long _fiomethsPtr) throws CoordinateConversionException;
  private native String jniGetDatumCode(long _fiomethsPtr) throws CoordinateConversionException;
  private native CoordinateSystemParameters jniGetCoordinateSystemParameters(long _fiomethsPtr) throws CoordinateConversionException;
  private native long jniGetNumErrors(long _fiomethsPtr);
  private native long jniGetNumProcessed(long _fiomethsPtr);
  private native long jniGetNumWarnings(long _fiomethsPtr);
  private native double jniGetElapsedTime(long _fiomethsPtr);
  private native long jniSetOutputFilename(long _fiomethsPtr, String fileName, String targetDatumCode, CoordinateSystemParameters targetParameters) throws CoordinateConversionException, Exception;
  private native void jniSetUseNSEW(long _fiomethsPtr, boolean value);
  private native void jniSetUseMinutes(long _fiomethsPtr, boolean value);
  private native void jniSetUseSeconds(long _fiomethsPtr, boolean value);
  private native void jniSetLatLongPrecision(long _fiomethsPtr, int precision);
  // BAEts29174 - Set output format for file operation
  private native void jniShowLeadingZeros(long _fiomethsPtr, boolean lz);
  private native void jniSetLonRange(long _fiomethsPtr, int longRange);
  private native void jniSetSeparator(long _fiomethsPtr, char sepChar);
  
  private native void jniWriteExampleCoord(long _fiomethsPtr);
  private native void jniSetGeodeticCoordinateOrder(long _fiomethsPtr, boolean outputLatitudeLongitudeOrder);

  // Holds the C++ Fiomeths object pointer
  private long fiomethsPtr;
  
  
  /** Creates new JNIFiomeths */
  public JNIFiomeths() throws Exception
  {
    try
    {
      fiomethsPtr = jniFiomethsCreateExampleFile();
    }
    catch(Exception e)
    {
       throw new Exception(e.getMessage());
    }
  }
  
  
  public JNIFiomeths(String fileName) throws Exception
  {
    try
    {
      fiomethsPtr = jniFiomethsCreate(fileName);
    }
    catch(Exception e)
    {
       throw new Exception(e.getMessage());
    }
  }
  
  
  public synchronized void destroy()
  {
    if(fiomethsPtr != 0)
    {
      jniFiomethsDestroy(fiomethsPtr);
      fiomethsPtr = 0;
    }
  }
  
  
  public synchronized void closeInputFile() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("closeInputFile called with null object");
    }
    
    jniCloseInputFile(fiomethsPtr);  
  }
  
  
  public synchronized void closeOutputFile() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("closeOutputFile called with null object");
    }
    
    jniCloseOutputFile(fiomethsPtr);  
  }
  
  
  public synchronized void convertFile() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("convertFile called with null object");
    }
    
    jniConvertFile(fiomethsPtr);  
  }
  
  
  /**
   * Returns the input datum code. 
   *
   * @return    datum code contained in the header of the input file
   */
  public synchronized String getDatumCode() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("getDatumCode called with null object");
    }
    
    return jniGetDatumCode(fiomethsPtr);  
  }
  
  
  /**
   * Returns the input coordinate system parameters. 
   *
   * @return    coordinate system parameters contained in the header of the input file
   * @throws   CoordinateConversionException    invalid coordinate system
   */
  public synchronized CoordinateSystemParameters getCoordinateSystemParameters() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("getCoordinateSystemParameters called with null object");
    }
    
    return jniGetCoordinateSystemParameters(fiomethsPtr);    
  }
  
  
  public synchronized long getNumErrors() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("getNumErrors called with null object");
    }
    
    return jniGetNumErrors(fiomethsPtr);  
  }
  
  
  public synchronized long getNumProcessed() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("getNumProcessed called with null object");
    }
    
    return jniGetNumProcessed(fiomethsPtr);  
  }
  
  
  public synchronized long getNumWarnings() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("getNumWarnings called with null object");
    }
    
    return jniGetNumWarnings(fiomethsPtr);  
  }
  
  
  public synchronized double getElapsedTime() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("getElapsedTime called with null object");
    }
    
    return jniGetElapsedTime(fiomethsPtr);  
  }
  
         
/*  public synchronized void setInputFilename(long _coordinateConversionPtrServicePtr, java.lang.String fileName) throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("setInputFilename called with null object");
    }
    
    return jniSetInputFilename(fiomethsPtr);  
  */
  
  
  public synchronized long setOutputFilename(String fileName, String targetDatumCode, CoordinateSystemParameters targetParameters) throws CoordinateConversionException, Exception
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("setOutputFilename called with null object");
    }
    
    return jniSetOutputFilename(fiomethsPtr, fileName, targetDatumCode, targetParameters);  
  }
  
  
  public synchronized void setUseNSEW(boolean value) throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("useNSEW called with null object");
    }
    
    jniSetUseNSEW(fiomethsPtr, value);  
  }
  
  
  public synchronized void setUseMinutes(boolean value) throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("useMinutes called with null object");
    }
    
    jniSetUseMinutes(fiomethsPtr, value);  
  }
  
  
  public synchronized void setUseSeconds(boolean value) throws CoordinateConversionException
  {
    if (fiomethsPtr == 0)
    {
        throw new CoordinateConversionException("useSeconds called with null object");
    }

    jniSetUseSeconds(fiomethsPtr, value);
  }


  public synchronized void setLatLongPrecision(int _precision) throws CoordinateConversionException
  {
    if (fiomethsPtr == 0)
    {
        throw new CoordinateConversionException("setLatLongPrecision called with null object");
    }

    jniSetLatLongPrecision(fiomethsPtr, _precision);
  }

  // BAEts29174
  public synchronized void setLeadingZeros(boolean leadingZeros) throws CoordinateConversionException
  {
    if (fiomethsPtr == 0)
    {
        throw new CoordinateConversionException("setLeadingZeros called with null object");
    }

    jniShowLeadingZeros(fiomethsPtr, leadingZeros);
  }

  public synchronized void setSeparator(char sepChar) throws CoordinateConversionException
  {
    if (fiomethsPtr == 0)
    {
        throw new CoordinateConversionException("setSeparator called with null object");
    }

    jniSetSeparator(fiomethsPtr, sepChar);
  }

  public synchronized void setLonRange(int lonRange) throws CoordinateConversionException
  {
    if (fiomethsPtr == 0)
    {
        throw new CoordinateConversionException("setLonRange called with null object");
    }

    jniSetLonRange(fiomethsPtr, lonRange);
  }

  public synchronized void writeExampleCoord() throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("writeExampleCoord called with null object");
    }
    
    jniWriteExampleCoord(fiomethsPtr);  
  }
  
  
  public synchronized void setGeodeticCoordinateOrder(boolean outputLatitudeLongitudeOrder) throws CoordinateConversionException
  {
    if (fiomethsPtr == 0) 
    {
        throw new CoordinateConversionException("setGeodeticCoordinateOrder called with null object");
    }
    
    jniSetGeodeticCoordinateOrder(fiomethsPtr, outputLatitudeLongitudeOrder);  
  }
}

// CLASSIFICATION: UNCLASSIFIED
