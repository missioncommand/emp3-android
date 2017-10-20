// CLASSIFICATION: UNCLASSIFIED

/*
 * JNIEllipsoidLibrary.java
 *
 * Created on August 9, 2007, 4:14 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.jni;


///import msp.ccs.dtcc.ellipse.*;
import geotrans3.misc.Info;
import geotrans3.exception.CoordinateConversionException;


public class JNIEllipsoidLibrary
{
  private native void jniDefineEllipsoid(long _ellipsoidLibraryPtr, String ellipsoidCode, String ellipsoidName,
                                        double a, double f)throws CoordinateConversionException;
  private native void jniRemoveEllipsoid(long _ellipsoidLibraryPtr, String ellipsoidCode)throws CoordinateConversionException;
  private native long jniGetEllipsoidCount(long _ellipsoidLibraryPtr) throws CoordinateConversionException;
  private native long jniGetEllipsoidIndex(long _ellipsoidLibraryPtr, String ellipsoidCode) throws CoordinateConversionException;
  private native Info jniGetEllipsoidInfo(long _ellipsoidLibraryPtr, long index);
  
    
  // Holds the C++ EllipsoidLibrary object pointer
  private long ellipsoidLibraryPtr;
  
  
  /**
   * Assigns a pointer to the C++ EllipsoidLibrary object.
   */
  public JNIEllipsoidLibrary(long _ellipsoidLibraryPtr)
  {
    ellipsoidLibraryPtr = _ellipsoidLibraryPtr;
  }
  
  
  public synchronized void defineEllipsoid(String ellipsoidCode, String ellipsoidName,
                                            double a, double f) throws CoordinateConversionException
  {
    if (ellipsoidLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("defineEllipsoid called with null object");
    }
    
    jniDefineEllipsoid(ellipsoidLibraryPtr, ellipsoidCode, ellipsoidName, a,  f);
  }


  public synchronized void removeEllipsoid(String ellipsoidCode) throws CoordinateConversionException
  {
    if (ellipsoidLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("removeEllipsoid called with null object");
    }
    
    jniRemoveEllipsoid(ellipsoidLibraryPtr, ellipsoidCode);
  }
  

  public synchronized long getEllipsoidCount() throws CoordinateConversionException
  {
    if (ellipsoidLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("getEllipsoidCount called with null object");
    }
    
    return jniGetEllipsoidCount(ellipsoidLibraryPtr);
  }


  public synchronized long getEllipsoidIndex(String ellipsoidCode) throws CoordinateConversionException
  {
    if (ellipsoidLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("getEllipsoidIndex called with null object");
    }
    
    return jniGetEllipsoidIndex(ellipsoidLibraryPtr, ellipsoidCode);
  }


  public synchronized Info getEllipsoidInfo(long index) throws CoordinateConversionException
  {
    if (ellipsoidLibraryPtr == 0) 
    {
        throw new CoordinateConversionException("getEllipsoidInfo called with null object");
    }
    
    return jniGetEllipsoidInfo(ellipsoidLibraryPtr, index);
  }

}// CLASSIFICATION: UNCLASSIFIED
