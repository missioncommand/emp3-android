// CLASSIFICATION: UNCLASSIFIED

/******************************************************************************
* Filename        : strtoval.h
*
* MODIFICATION HISTORY:
*
* DATE      NAME        DR#               DESCRIPTION
*
* 03/29/11  S. Gillis   BAEts28564        Fixed Windows memory crash
******************************************************************************/

#ifndef __strtoval_h__
#define __strtoval_h__

///#include "comphack.h"
#include "CoordinateConversionService.h"

   
#ifdef __cplusplus
extern "C" {
#endif


using namespace MSP::CCS;


/* Begin enum declarations */

enum Range
{
  _180_180,
  _0_360
};

/* SVC_Status is the status-reporting code used by functions in this module. */

typedef enum String_to_Value_Conversion_Status
{
   /* Success */
   SVC_Success = 0,

      /* String_to_<Type> and errors */
      SVC_String_to_Long = -1,
      SVC_String_to_Double = -2,

      /* String_to_Longitude errors */
      SVC_Degrees_out_of_Bounds = -11,
      SVC_Minutes_out_of_Bounds = -12,
      SVC_Seconds_out_of_Bounds = -13,
      SVC_Minutes_or_Seconds_Overflow = -14,
      SVC_Inappropriate_Hemisphere = -15,
      SVC_Sign_and_Hemisphere = -16,
      SVC_Misplaced_Hemisphere = -17,

      /* <Degrees>_to_String errors */

      SVC_DMS_to_String = -21,
      SVC_DM_to_String = -22,
      SVC_D_to_String = -23,

      /* <Parameter>_to_String errors */

      SVC_Easting_to_String = -31,
      SVC_Northing_to_String = -32,
      SVC_False_Easting_to_String = -33,
      SVC_False_Northing_to_String = -34,
      SVC_Central_Meridian_to_String = -35,
      SVC_Standard_Parallel_to_String = -36,
      SVC_Standard_Parallel_Two_to_String = -37,
      SVC_Zone_to_String = -38,
      SVC_Ellipsoidal_Height_to_String = -39,
      SVC_Origin_Latitude_to_String = -40,
      SVC_Scale_Factor_to_String = -41,
      SVC_X_to_String = -42,
      SVC_Y_to_String = -43,
      SVC_Z_to_String = -44,
      SVC_Meter_to_String = -45,

      /* Parameter Specific errors */

      SVC_Zone_out_of_Bounds = -71,
      SVC_Scale_Factor_out_of_Bounds = -72,

      /* Projection Specific errors */
      SVC_Invalid_Projection_String = -81,
      SVC_Invalid_Projection_Value = -82,

   /* General error */
   SVC_Error = -1000

} SVC_Status;

/* End enum declarations */

/********/

/* Begin function declarations */
void Show_Leading_Zeros(int lz);

void Set_Separator(char sep);

void Set_Long_Range(Range range);

char Get_Separator();

Range Get_Long_Range();                                                

void Set_Lat_Long_Precision(Precision::Enum precis);

void Degrees_to_String(double degrees, char *str, bool use_Minutes, bool use_Seconds, long Type);

SVC_Status String_to_Projection(const char *str, MSP::CCS::CoordinateType::Enum *val);
SVC_Status Projection_to_String(const CoordinateType::Enum val, char str[32]);

SVC_Status String_to_Double(const char *str, double *val);

long Valid_Number(const char *str);

SVC_Status Meter_to_String(const double meters, char str[17]);

SVC_Status Long_Meter_to_String(const double meters, char str[17]);

SVC_Status String_to_Long(const char *str, long *val);

SVC_Status String_to_Longitude(const char *str, double *longitude);

/* Force usage of fixed length strings to alleviate checkless C. */
SVC_Status Longitude_to_String(const double longitude, char str[17], bool use_NSEW,  
                         bool use_Minutes, bool use_Seconds);

SVC_Status String_to_Latitude(const char *str, double *latitude);
SVC_Status Latitude_to_String(const double latitude, char str[17], bool use_NSEW, 
                        bool use_Minutes, bool use_Seconds);

SVC_Status Zone_to_String(const long zone, char str[3]);

SVC_Status Ellipsoidal_Height_to_String(const double ellipsoidal_Height, char str[17]);

SVC_Status Scale_Factor_to_String(const double scale_Factor, char str[8]);

void Error_Prefix ( const SourceOrTarget::Enum Direction,
  const CoordinateType::Enum System, char *Separator, char *String);

void Error_Append ( char *New_String, char *Separator, char *String);

#ifdef __cplusplus
}
#endif

#endif

// CLASSIFICATION: UNCLASSIFIED
