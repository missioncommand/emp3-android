// CLASSIFICATION: UNCLASSIFIED

/*
 * JNICoordinateConversionService.java
 *
 * Created on March 28, 2007, 4:40 PM
 */

package geotrans3.jni;

import java.util.Collection;
import geotrans3.coordinates.Accuracy;
import geotrans3.coordinates.ConvertResults;
import geotrans3.coordinates.ConvertCollectionResults;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.AOI;
import geotrans3.misc.CoordinateSystemInfo;
import geotrans3.misc.Info;
import geotrans3.coordinates.CoordinateTuple;
import geotrans3.parameters.CoordinateSystemParameters;

/**
 *
 * @author  amyc
 * @version 
 */
public class JNICoordinateConversionService extends Object
{
  private native long jniCreate(String sourceDatumCode, CoordinateSystemParameters sourceParameters, String targetDatumCode, CoordinateSystemParameters targetParameters) throws CoordinateConversionException;
  private native void jniDestroy(long _coordinateConversionPtrService);

  private native ConvertResults jniConvertSourceToTarget(long _coordinateConversionPtrService, CoordinateTuple sourceCoordinates, Accuracy sourceAccuracy, CoordinateTuple targetCoordinates, Accuracy targetAccuracy) throws CoordinateConversionException;
  private native ConvertResults jniConvertTargetToSource(long _coordinateConversionPtrService, CoordinateTuple targetCoordinates, Accuracy targetAccuracy, CoordinateTuple sourceCoordinates, Accuracy sourceAccuracy) throws CoordinateConversionException;

  private native void jniConvertSourceToTargetCollection(long _coordinateConversionPtrService, Collection sourceCoordinates, Collection sourceAccuracy, Collection targetCoordinates, Collection targetAccuracy) throws CoordinateConversionException;
  private native void jniConvertTargetToSourceCollection(long _coordinateConversionPtrService, Collection targetCoordinates, Collection targetAccuracy, Collection sourceCoordinates, Collection sourceAccuracy) throws CoordinateConversionException;

  private native long jniGetDatumLibrary(long _coordinateConversionPtrService);
  private native long jniGetEllipsoidLibrary(long _coordinateConversionPtrService);

  private native double jniGetServiceVersion(long _coordinateConversionPtrService);

  private native String jniGetDatum(long _coordinateConversionPtrService, int direction) throws CoordinateConversionException;
  private native CoordinateSystemParameters jniGetCoordinateSystem(long _coordinateConversionPtrService, int direction) throws CoordinateConversionException;
 
  // Holds the C++ CoordinateConversionService object pointer
  private long coordinateConversionServicePtr;
  
  
  /**
   * Creates new JNICoordinateConversionService
   */
  public JNICoordinateConversionService(String sourceDatumCode, CoordinateSystemParameters sourceParameters, String targetDatumCode, CoordinateSystemParameters targetParameters) throws Exception
  {
    try
    {
      coordinateConversionServicePtr = jniCreate(sourceDatumCode, sourceParameters, targetDatumCode, targetParameters);
    }
    catch(Exception e)
    {
       throw new Exception(e.getMessage());
    }
  }

  
  public synchronized void destroy()
  {
    if(coordinateConversionServicePtr != 0)
    {
      jniDestroy(coordinateConversionServicePtr);
      coordinateConversionServicePtr = 0;
    }
  }
  
  
  public synchronized long getCoordinateConversionServicePtr()
  {
    return coordinateConversionServicePtr;
  }
  
  
  public synchronized long getDatumLibrary() throws CoordinateConversionException
  {
    if (coordinateConversionServicePtr == 0) 
    {
        throw new CoordinateConversionException("getDatumLibrary called with null object");
    }
    
    return jniGetDatumLibrary(coordinateConversionServicePtr);
  }
  
  
  public synchronized long getEllipsoidLibrary() throws CoordinateConversionException
  {
    if (coordinateConversionServicePtr == 0) 
    {
        throw new CoordinateConversionException("getEllipsoidLibrary called with null object");
    }
    
    return jniGetEllipsoidLibrary(coordinateConversionServicePtr);
  }
  
  
  public synchronized String getDatum(int direction) throws CoordinateConversionException
  {
    if (coordinateConversionServicePtr == 0) 
    {
        throw new CoordinateConversionException("getDatum called with null object");
    }
    
    return jniGetDatum(coordinateConversionServicePtr, direction);
  }
  
  
  public synchronized CoordinateSystemParameters getCoordinateSystem(int direction) throws CoordinateConversionException
  {
    if (coordinateConversionServicePtr == 0) 
    {
        throw new CoordinateConversionException("getCoordinateSystem called with null object");
    }
    
    return jniGetCoordinateSystem(coordinateConversionServicePtr, direction);
  }
  
  
  public synchronized ConvertResults convertSourceToTarget(CoordinateTuple sourceCoordinates, Accuracy sourceAccuracy, CoordinateTuple targetCoordinates, Accuracy targetAccuracy) throws CoordinateConversionException
  {
    if (coordinateConversionServicePtr == 0) 
    {
        throw new CoordinateConversionException("convertSourceToTarget called with null object");
    }
    
    return jniConvertSourceToTarget(coordinateConversionServicePtr, sourceCoordinates, sourceAccuracy, targetCoordinates, targetAccuracy);
  }


  public synchronized ConvertResults convertTargetToSource(CoordinateTuple targetCoordinates, Accuracy targetAccuracy, CoordinateTuple sourceCoordinates, Accuracy sourceAccuracy) throws CoordinateConversionException
  {
    if (coordinateConversionServicePtr == 0) 
    {
        throw new CoordinateConversionException("convertTargetToSource called with null object");
    }
    
    return jniConvertTargetToSource(coordinateConversionServicePtr, targetCoordinates, targetAccuracy, sourceCoordinates, sourceAccuracy);
  }


/*  public synchronized ConvertCollectionResults convertVector(java.lang.String sourceDatumCode, CoordinateSystemParameters sourceCoordSysParams, 
                                       java.lang.String targetDatumCode, CoordinateSystemParameters targetCoordSysParams,
                                       java.util.Vector sourceCoordinatesVector, java.util.Vector sourceAccuracyVector) throws CoordinateConversionException
  {
    if (coordinateConversionServicePtr == 0) 
    {
        throw new CoordinateConversionException("convertVector called with null object");
    }
    
    return jniConvertVector(coordinateConversionServicePtr, sourceDatumCode, sourceCoordSysParams, 
                            targetDatumCode, targetCoordSysParams,
                            sourceCoordinatesVector, sourceAccuracyVector);
  }*/
  
  
  public synchronized double getServiceVersion() throws CoordinateConversionException
  {
    if (coordinateConversionServicePtr == 0) 
    {
        throw new CoordinateConversionException("getServiceVersion called with null object");
    }
    
    return jniGetServiceVersion(coordinateConversionServicePtr);
  }
}

// CLASSIFICATION: UNCLASSIFIED
