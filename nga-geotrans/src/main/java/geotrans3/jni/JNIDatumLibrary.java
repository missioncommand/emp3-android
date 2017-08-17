// CLASSIFICATION: UNCLASSIFIED

/*
 * JNIDatumLibrary.java
 *
 * Created on August 14, 2007, 10:32 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.jni;


//import msp.ccs.dtcc.datum.*;
import geotrans3.misc.AOI;
import geotrans3.misc.Info;
import geotrans3.exception.CoordinateConversionException;


public class JNIDatumLibrary
{
  private native void jniDefineDatum(long _datumLibraryPtr, int datumType, String datumCode, String datumName,
          String ellipsoidCode,
          double deltaX, double deltaY, double deltaZ,
          double sigmaX, double sigmaY, double sigmaZ,
          double westLon, double eastLon, double southLat, double northLat,
          double rotationX, double rotationY, double rotationZ,
          double scaleFactor) throws CoordinateConversionException;

  private native void jniRemoveDatum(long _datumLibraryPtr, String datumCode) throws CoordinateConversionException;

  private native long jniGetDatumCount(long _datumLibraryPtr) throws CoordinateConversionException;
  private native long jniGetDatumIndex(long _datumLibraryPtr, String datumCode) throws CoordinateConversionException;
  private native Info jniGetDatumInfo(long _datumLibraryPtr, long index);
  private native AOI jniGetDatumValidRectangle(long _datumLibraryPtr, long index);
  
  
  // Holds the C++ DatumLibrary object pointer
  private long datumLibraryPtr;
  
  
  /**
   * Assigns a pointer to the C++ DatumLibrary object.
   */
  public JNIDatumLibrary(long _datumLibraryPtr)
  {
    datumLibraryPtr = _datumLibraryPtr;
  }
  
  
  public synchronized void defineDatum(int datumType, String datumCode, String datumName,
                                          String ellipsoidCode,
                                          double deltaX, double deltaY, double deltaZ,
                                          double sigmaX, double sigmaY, double sigmaZ,
                                          double westLon, double eastLon, double southLat, double northLat,
                                          double rotationX, double rotationY, double rotationZ,
                                          double scaleFactor) throws CoordinateConversionException
  {
    if (datumLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("defineDatum called with null object");
    }
    
    jniDefineDatum(datumLibraryPtr, datumType, datumCode, datumName, ellipsoidCode,
                   deltaX, deltaY, deltaZ, sigmaX, sigmaY, sigmaZ,
                   westLon, eastLon, southLat, northLat, rotationX, rotationY, rotationZ, scaleFactor);
  }
  
  
  public synchronized void removeDatum(String datumCode) throws CoordinateConversionException
  {
    if (datumLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("removeDatum called with null object");
    }
    
    jniRemoveDatum(datumLibraryPtr, datumCode);
  }


  public synchronized long getDatumCount() throws CoordinateConversionException
  {
    if (datumLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("getDatumCount called with null object");
    }
    
    return jniGetDatumCount(datumLibraryPtr);
  }
  
  
  public synchronized long getDatumIndex(String datumCode) throws CoordinateConversionException
  {
    if (datumLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("getDatumIndex called with null object");
    }
    
    return jniGetDatumIndex(datumLibraryPtr, datumCode);
  }


  public synchronized Info getDatumInfo(long index) throws CoordinateConversionException
  {
    if (datumLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("getDatumInfo called with null object");
    }
    
    return jniGetDatumInfo(datumLibraryPtr, index);
  }


  public synchronized AOI getDatumValidRectangle(long index) throws CoordinateConversionException
  {
    if (datumLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("getDatumValidRectangle called with null object");
    }
    
    return jniGetDatumValidRectangle(datumLibraryPtr, index);
  }
}

// CLASSIFICATION: UNCLASSIFIED
