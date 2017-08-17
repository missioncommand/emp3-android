// CLASSIFICATION: UNCLASSIFIED

#ifndef __fiomeths_h__
#define __fiomeths_h__

#include <stdio.h>
#include "CoordinateConversionService.h"

#ifdef __cplusplus
extern "C" {
#endif

/***************************************************************************/
/* RSC IDENTIFIER: FIOMETHS
 *
 * ABSTRACT
 *
 *    This component provides file processing capability to MSPCCS.
 *
 *    This component depends on the following modules:  Coordinate Conversion Service,
 *    ERRHAND, COMPHACK, FTOVAL, STRTOVAL.
 *
 * ERROR HANDLING
 *
 *    This component checks for errors in the file input data and if
 *    an error is found it passes an error code back to the calling routine.
 *
 *
 * REUSE NOTES
 *
 *    FIOMETHS is not specifically intended for reuse, as it is designed
 *    to work with files of a very specific format.
 *
 *
 * REFERENCES
 *
 *    Further information on FIOMETHS can be found in the MSPCCS Programmer's
 *    Guide.
 *
 *    FIOMETHS originated from :  U.S. Army Topographic Engineering Center
 *                             Digital Concepts & Analysis Center
 *                             7701 Telegraph Road
 *                             Alexandria, VA  22310-3864
 *
 * LICENSES
 *
 *    None apply to this component.
 *
 * RESTRICTIONS
 *
 *    FIOMETHS has no restrictions.
 *
 * ENVIRONMENT
 *
 *    FIOMETHS was tested and certified in the following environments:
 *
 *    1. Solaris 2.5 with GCC, version 2.7.2.1
 *    2. Windows XP with MS Visual C++, version 6
 *
 * MODIFICATIONS
 *
 *    Date              Description
 *    ----              -----------
 *    04-22-97          Original Code
 *    06-28-99          Added new DT&CC Modules
 *    09-13-00          Added new DT&CC Modules
 *    03-29-01          Improved file formatting flexibility
 *    08-17-05          Changed Lambert_Conformal_Conic to CoordinateType::lambertConformalConic2Parallels
 *    01-18-06          Added changes for new height types
 *    04-18-07          Updated to use C++ MSPCCS
 *    11-30-11          K.Lam, BAEts29174, Set output format for file operation
 */




/***************************************************************************/
/*
 *                              DEFINES
 */


using namespace MSP::CCS;


class Fiomeths
{
public:

  Fiomeths();
  Fiomeths( const char* fileName );

  ~Fiomeths();

  void setOutputFilename( const char *filename, const char* _targetDatumCode, CoordinateSystemParameters* _targetParameters );

  void convertFile();

  void closeInputFile();
  void closeOutputFile();

  void writeExampleCoord();

  /*
   *  The function getDatumCode returns the code of the current datum
   */
  const char* getDatumCode() const;


  /*
   *  The function getCoordinateSystemParameters returns the current coordinate system
   *  parameters.
   */
  MSP::CCS::CoordinateSystemParameters* getCoordinateSystemParameters() const;

  long getNumErrors() const;
  long getNumProcessed() const;
  long getNumWarnings() const;
  double getElapsedTime() const;

  void setUseNSEW(bool value);
  void setUseMinutes(bool value);
  void setUseSeconds(bool value);
  void setLatLongPrecision(int _precision);
  void setSeparator(char value);
  void setLonRange(int value);
  void showLeadingZeros(bool value);
  
  void setGeodeticCoordinateOrder(bool _outputLatitudeLongitudeOrder);


private:

  struct TrailingHeight
  {
    bool heightPresent;
    char height[10];
  };

  CoordinateConversionService* coordinateConversionService;

  FILE* inputFile;
  FILE* outputFile;

  CoordinateType::Enum sourceProjectionType;
  CoordinateType::Enum targetProjectionType;

  char* sourceDatumCode;
  char* targetDatumCode;
  CoordinateSystemParameters* coordinateSystemParameters;
  EquidistantCylindricalParameters* equidistantCylindricalParameters;
  GeodeticParameters* geodeticParameters;
  LocalCartesianParameters* localCartesianParameters;
  MapProjection3Parameters* mapProjection3Parameters;
  MapProjection4Parameters* mapProjection4Parameters;
  MapProjection5Parameters* mapProjection5Parameters;
  MapProjection6Parameters* mapProjection6Parameters;
  MercatorStandardParallelParameters* mercatorStandardParallelParameters;
  MercatorScaleFactorParameters* mercatorScaleFactorParameters;
  NeysParameters* neysParameters;
  ObliqueMercatorParameters* obliqueMercatorParameters;
  PolarStereographicStandardParallelParameters* polarStereographicStandardParallelParameters;
  PolarStereographicScaleFactorParameters* polarStereographicScaleFactorParameters;
  UTMParameters* utmParameters;
  
  CoordinateSystemParameters* targetParameters;

  long _numErrors;
  long _numProcessed;
  long _numWarnings;
  double _elapsedTime;

  bool _useNSEW;
  bool _useMinutes;
  bool _useSeconds;
  const int invalid;
  bool inputLatitudeLongitudeOrder;
  bool outputLatitudeLongitudeOrder;


  void setInputFilename( const char *fileName );

  void getFileErrorString( long error, char *str );

  long parseInputFileHeader( FILE *file );

  void writeOutputFileHeader( const char* targetDatumCode, CoordinateSystemParameters* targetParameters );

  void setCoordinateSystemParameters( MSP::CCS::CoordinateSystemParameters* parameters );

  CoordinateTuple* readCoordinate();
  long readCoord( double *easting, double *northing );
  long readHeight( char* height );

  Accuracy* readConversionErrors( char* errors );

  void convert( std::vector<MSP::CCS::CoordinateTuple*>& sourceCoordinateCollection, std::vector<MSP::CCS::Accuracy*>& sourceAccuracyCollection, std::vector<TrailingHeight>& trailingHeightCollection, std::vector<MSP::CCS::CoordinateTuple*>& targetCoordinates, std::vector<MSP::CCS::Accuracy*>& targetAccuracy );

  CoordinateTuple* initTargetCoordinate();
  void writeTargetCoordinate( CoordinateTuple* targetCoordinate );
  void writeCoord( double easting, double northing );
  void writeHeight( char* height );

  void writeTargetAccuracy( Accuracy* accuracy );
};

#ifdef __cplusplus
}
#endif

#endif

// CLASSIFICATION: UNCLASSIFIED
