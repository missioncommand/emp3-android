// CLASSIFICATION: UNCLASSIFIED

/******************************************************************************
* Filename: ErrorMessages.java
*
* Copyright BAE Systems Inc. 2012 ALL RIGHTS RESERVED
*
* MODIFICATION HISTORY
*
* DATE      NAME        DR#          DESCRIPTION
*
* 07/18/12  S. Gillis   MSP_00029550 Added errors
*****************************************************************************/

package geotrans3.exception;


/**
 * Defines all error messages which may be returned by the ccs. 
 */
public class ErrorMessages
{  
  public static final String geoidFileOpenError = "Unable to locate geoid data file";
  public static final String geoidFileParseError = "Unable to read geoid file";
  
  public static final String ellipsoidFileOpenError = "Unable to locate ellipsoid data file: ellips.dat";
  public static final String ellipsoidFileCloseError = "Unable to close ellipsoid file: ellips.dat";
  public static final String ellipsoidFileParseError = "Unable to read ellipsoid file: ellips.dat";
  public static final String ellipsoidOverflow = "Ellipsoid table overflow";
  public static final String ellipse = "Ellipsoid library not initialized\n";
  public static final String invalidEllipsoidCode = "Invalid ellipsoid code\n";
  
  public static final String datumFileOpenError = "Unable to locate datum data file";
  public static final String datumFileCloseError = "Unable to close datum file";
  public static final String datumFileParseError = "Unable to read datum file";
  public static final String datumOverflow = "Datum table overflow";
  public static final String datumType = "Invalid datum type\n";
  public static final String invalidDatumCode = "Invalid datum code\n";
   
  // Parameter error messages
  public static final String semiMajorAxis = "Ellipsoid semi-major axis must be greater than zero\n";
  public static final String ellipsoidFlattening = "Inverse flattening must be between 250 and 350\n";
  public static final String orientation = "Orientation out of range\n";
  public static final String originLatitude = "Origin Latitude (or Latitude of True Scale) out of range\n";
  public static final String originLongitude = "Origin Longitude (or Longitude Down from Pole) out of range\n";
  public static final String centralMeridian = "Central Meridian out of range\n";
  public static final String scaleFactor = "Scale Factor out of range\n";
  public static final String zoneOverride = "Invalid Zone Override\n";
  public static final String standardParallel1 = "Invalid 1st Standard Parallel\n";
  public static final String standardParallel2 = "Invalid 2nd Standard Parallel\n";
  public static final String standardParallel1_2 = "1st & 2nd Standard Parallels cannot both be zero\n";
  public static final String standardParallelHemisphere = "Standard Parallels cannot be equal and opposite latitudes\n";
  public static final String precision = "Precision must be between 0 and 5\n";
  
  // Coordinate error messages
  public static final String latitude = "Latitude out of range\n";
  public static final String longitude = "Longitude out of range\n";
  public static final String easting = "Easting/X out of range\n";
  public static final String northing = "Northing/Y out of range\n";
  
  public static final String utmZoneRow = "Invalid UTM Zone Row";
  public static final String zone = "Zone out of range (1-60)\n";
  
  public static final String mgrsString = "Invalid MGRS String\n";

  public static final String radius = "Easting/Northing too far from center of projection\n";
  
  public static final String hemisphere = "Invalid Hemisphere\n";
  public static final String signHemisphere = "Mismatched sign and hemisphere \n";
  public static final String degrees = "Degrees value out of range\n";
  public static final String minutes = "Minutes value out of range\n";
  public static final String seconds = "Seconds value out of range\n";
  
  public static final String invalidIndex = "Index value outside of valid range\n";
  public static final String invalidName = "Invalid name\n";
  public static final String invalidType = "Invalid coordinate system type\n";

  // Number/String errors
  public static final String numericError = "Entry must be numeric\n";
  public static final String noEntryError = "A value must be entered\n"; 
}

// CLASSIFICATION: UNCLASSIFIED
