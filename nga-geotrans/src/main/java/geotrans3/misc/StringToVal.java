// CLASSIFICATION: UNCLASSIFIED

/******************************************************************************
* Filename: StringToVal.java
*
* Copyright BAE Systems Inc. 2012 ALL RIGHTS RESERVED
*
* MODIFICATION HISTORY
*
* DATE      NAME        DR#          DESCRIPTION
*
* 07/18/12  S. Gillis   MSP_00029550 Updated exception handling 
* 01/12/16  K. Chen     MSP_00030518 Add US Survey Feet Support
*****************************************************************************/

package geotrans3.misc;


import geotrans3.enumerations.CoordinateType;
import geotrans3.enumerations.Precision;
import geotrans3.enumerations.Range;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.exception.ErrorMessages;


/**
 * Provides string conversion functionality.
 * 
 * @author comstam
 */
public class StringToVal 
{
  private final int Lat_String = 1;
  private final int Long_String = 2;
  
  private int lonRange;
  private boolean leadingZeros;
  private char latLonSeparator;
  private int precision;
  
  
  /** 
   * Creates a new instance of StringToVal 
   */
  public StringToVal() 
  {
    lonRange = Range._180_180;
    leadingZeros = false;
    latLonSeparator = ' ';
    precision = Precision.TENTH_OF_SECOND;
  }
  
  
  /**
   * Display leading zeros in latitude and longitude values.
   *
   * @param    _leadingZeros    set to true if leading zeros should be displayed, otherwise false  
   */
  public void showLeadingZeros(boolean _leadingZeros)
  {
    leadingZeros = _leadingZeros;
  }

  
  /**
   * Sets the latitude and longitude degrees, minutes, seconds separator. 
   * Valid characters are space, forward slash or colon
   *
   * @param    _leadingZeros    valid characters are ' ', '/' or ':'  
   */
  public void setSeparator(char _latLonSeparator)
  {
    latLonSeparator = _latLonSeparator;
  }

  
  /**
   * Returns the latitude and longitude degrees, minutes, seconds separator. 
   *
   * @return    either a space, forward slash or colon
   */
  public char getSeparator()
  {
    return (latLonSeparator);
  }

  
  /**
   * Sets the longitude range. 
   *
   * @param    _lonRange    range of longitude, 0 to 360 or -180 to 180   
   * @see      ccs.enumerations.Range	      
   */
  public void setLongRange(int _lonRange)
  {
    lonRange = _lonRange;
  }

  
  /**
   * Returns the longitude range. 
   *
   * @return    0 to 360 or -180 to 180
   * @see       ccs.enumerations.Range	      
   */
  public int getLongRange()
  {
    return (lonRange);
  }

  
  /**
   * Sets the coordinate precision. 
   *
   * @param    _precision    coordinate precision  
   * @see      ccs.enumerations.Precision	      
   */
  public void setPrecision(int _precision)
  {
    precision = _precision;
  }


  /**
   * Converts a double longitude value to a string. 
   *
   * @param    inLongitude    longitude value  
   * @param    useNSEW        true if longitude string should use 'E' or 'W' for hemisphere, false if it should use '+' or '-'
   * @param    useMinutes     true if longitude string should be in degrees minutes seconds or degrees minutes format
   * @param    useSeconds     true if longitude string should be in degrees minutes seconds format
   * @return   string representation of the longitude double value
   * @exception  CoordinateConversionException      invalid longitude value
   * @see      ccs.enumerations.Precision	      
   */
  public String longitudeToString(final double inLongitude, boolean useNSEW, boolean useMinutes, boolean useSeconds) throws CoordinateConversionException
  {
    try
    {
      double degrees = 0.0;

      double longitude = inLongitude;
      if ((longitude > -0.00000001) && (longitude < 0.00000001))
      {
        longitude = 0.0;
      }

      switch (lonRange)
      {
        case Range._180_180:
        {
          if (longitude > 180)
            degrees = Math.abs(longitude - 360);
          else
            degrees = Math.abs(longitude);
          break;
        }
        case Range._0_360:
        {
          if (longitude < 0)
            degrees = longitude + 360;
          else
            degrees = longitude;
          break;
        }
      }

      String degrees_As_String = degreesToString(degrees, useMinutes, useSeconds, Long_String);

      switch (lonRange)
      {
        case Range._180_180:
        {
          if (useNSEW)
          {
            if ((longitude > 180) || (longitude < 0))
              degrees_As_String  += 'W';
            else
              degrees_As_String += 'E';
          }
          else
          {
            if ((longitude > 180) || (longitude < 0))
            {
              String temp = degrees_As_String;
              degrees_As_String = '-' + temp;
            }
          }
          break;
        }
        case Range._0_360:
        {
            if (useNSEW)
              degrees_As_String += 'E';
            break;
        }
      }

      return degrees_As_String;
    }
    catch(Exception e)
    {
      throw new CoordinateConversionException(ErrorMessages.longitude);
    }           
  }


  /**
   * Converts a double latitude value to a string. 
   *
   * @param    inLatitude     latitude value  
   * @param    useNSEW        true if latitude string should use 'N' or 'S'for hemisphere, false if it should use '+' or '-'
   * @param    useMinutes     true if latitude string should be in degrees minutes seconds or degrees minutes format
   * @param    useSeconds     true if latitude string should be in degrees minutes seconds format
   * @return   string representation of the latitude double value
   * @exception  CoordinateConversionException      invalid latitude value
   * @see      ccs.enumerations.Precision	      
   */
  public String latitudeToString(final double in_latitude, boolean useNSEW, boolean useMinutes, boolean useSeconds) throws CoordinateConversionException
  {
    try
    {
      double degrees = Math.abs(in_latitude);
      double latitude;

      latitude = in_latitude;
      if ((latitude > -0.00000001) && (latitude < 0.00000001))
      {
        latitude = 0.0;
      }

      String degrees_As_String = degreesToString(degrees, useMinutes, useSeconds, Lat_String);

      if (useNSEW)
      {
        if (latitude < 0)
        {
          degrees_As_String += 'S';
        }
        else
        {
          degrees_As_String += 'N';
        }
      }
      else
      {
        if (latitude < 0)
        {
          String temp = degrees_As_String;
          degrees_As_String = '-' + temp;
        }
      }

      return degrees_As_String;
    }
    catch(Exception e)
    {
      throw new CoordinateConversionException(ErrorMessages.latitude);
    }           
  }


  /**
   * Converts a latitude or longitude value in degrees to a string. 
   *
   * @param    degrees       latitude or longitude value to convert
   * @param    useMinutes    true if output string should be in degrees minutes seconds or degrees minutes format
   * @param    useSeconds    true if output string should be in degrees minutes seconds format
   * @param    type          indicates if the value represents a latitude or longitude string
   * @return   string representation of the latitude or longitude double value
   */
  private String degreesToString(double degrees, boolean useMinutes, boolean useSeconds, int type)
  {
    double minutes = 0.0;
    double seconds = 0.0;
    int integer_Degrees = 0;
    int integer_Minutes = 0;
    int integer_Seconds = 0;
    String degreesString = "";
    String minutesString = "";
    String secondsString = "";

    if ((!useMinutes) || (precision == 0))
    { /* Decimal Degrees */
      degrees = roundDMS(degrees, precision);
      if(leadingZeros)
      {
        if(type == Lat_String)
        {
          if(Math.abs(degrees) < 10)
            degreesString = "0" + Double.toString(degrees);
          else
            degreesString = Double.toString(degrees);
        }
        else
        {
          if(Math.abs(degrees) < 10)
            degreesString = "00" + Double.toString(degrees);
          else if(Math.abs(degrees) < 100)
            degreesString = "0" + Double.toString(degrees);
          else
            degreesString = Double.toString(degrees);
        }
      }
      else
      {
        degreesString = Double.toString(degrees);
      }
             
      // Prevent the degrees string from being displayed in scientific notation
      java.text.DecimalFormat df = new java.text.DecimalFormat();
      df.setMinimumFractionDigits(precision);
      degreesString = df.format(Double.parseDouble(degreesString));
      
      return addFractionZeros(degreesString, precision);
    }
    else if((useMinutes && !useSeconds) || (precision <= 2))
    { /* Degrees & Minutes */
      integer_Degrees = (int)degrees;
      minutes = (degrees - integer_Degrees) * 60.0;
      minutes = roundDMS(minutes, precision - 2);
      integer_Minutes = (int)minutes;
      if (integer_Minutes >= 60)
      {
        integer_Minutes -= 60;
        integer_Degrees += 1;
      }
      if (minutes >= 60)
        minutes -= 60;
      if(leadingZeros)
      {
        if(type == Lat_String)
        {
          if(Math.abs(integer_Degrees) < 10)
            degreesString = "0" + Integer.toString(integer_Degrees) + latLonSeparator;
          else
            degreesString = Integer.toString(integer_Degrees) + latLonSeparator;
        }
        else
        {
          if(Math.abs(integer_Degrees) < 10)
            degreesString = "00" + Integer.toString(integer_Degrees) + latLonSeparator;
          else if(Math.abs(integer_Degrees) < 100)
            degreesString = "0" + Integer.toString(integer_Degrees) + latLonSeparator;
          else
            degreesString = Integer.toString(integer_Degrees) + latLonSeparator;
        }
        
        if(integer_Minutes < 10)
          minutesString = "0" + this.doubleToString(minutes, (precision-2));
        else
          minutesString = this.doubleToString(minutes, (precision-2));
      }
      else
      {
        degreesString = Integer.toString(integer_Degrees) + latLonSeparator;
        minutesString = this.doubleToString(minutes, (precision-2));
      }
      
      if(precision <= 2)
        return degreesString + Integer.toString(integer_Minutes);    
      else
        return addFractionZeros(degreesString + minutesString, precision-2);    
    }
    else
    { /* Degrees, Minutes, & Seconds */
      integer_Degrees = (int)degrees;
      minutes = (degrees - integer_Degrees) * 60.0;
      integer_Minutes = (int)minutes;
      seconds = (minutes - integer_Minutes) * 60.0;
      seconds = roundDMS(seconds, precision - 4);
      integer_Seconds = (int)seconds;
      if (integer_Seconds >= 60)
      {
        integer_Seconds -= 60;
        integer_Minutes += 1;
        if (integer_Minutes >= 60)
        {
          integer_Degrees += 1;
          integer_Minutes -= 60;
        }
      }

      if (precision <= 4)
      {
        if(leadingZeros)
        {
          if(type == Lat_String)
          {
            if(Math.abs(integer_Degrees) < 10)
              degreesString = "0" + Integer.toString(integer_Degrees) + latLonSeparator;
            else
              degreesString = Integer.toString(integer_Degrees) + latLonSeparator;
          }
          else
          {
            if(Math.abs(integer_Degrees) < 10)
              degreesString = "00" + Integer.toString(integer_Degrees) + latLonSeparator;
            else if(Math.abs(integer_Degrees) < 100)
              degreesString = "0" + Integer.toString(integer_Degrees) + latLonSeparator;
            else
              degreesString = Integer.toString(integer_Degrees) + latLonSeparator;
          }

          if(integer_Minutes < 10)
            minutesString = "0" + Integer.toString(integer_Minutes) + latLonSeparator;
          else
            minutesString = Integer.toString(integer_Minutes) + latLonSeparator;
          
          if(integer_Seconds < 10)
            secondsString = "0" + Integer.toString(integer_Seconds);
          else
            secondsString = Integer.toString(integer_Seconds);
        }
        else
        {
          degreesString = Integer.toString(integer_Degrees) + latLonSeparator;
          minutesString = Integer.toString(integer_Minutes) + latLonSeparator;
          secondsString = Integer.toString(integer_Seconds);
        }
        
        return degreesString + minutesString + secondsString;    
      }
      else
      {
        if (seconds >= 60)
        {
          seconds -= 60;
        }

        if(leadingZeros)
        {
          if(type == Lat_String)
          {
            if(Math.abs(integer_Degrees) < 10)
              degreesString = "0" + Long.toString(integer_Degrees) + latLonSeparator;
            else
              degreesString = Long.toString(integer_Degrees) + latLonSeparator;
          }
          else
          {
            if(Math.abs(integer_Degrees) < 10)
              degreesString = "00" + Long.toString(integer_Degrees) + latLonSeparator;
            else if(Math.abs(integer_Degrees) < 100)
              degreesString = "0" + Long.toString(integer_Degrees) + latLonSeparator;
            else
              degreesString = Long.toString(integer_Degrees) + latLonSeparator;
          }

          if(integer_Minutes < 10)
            minutesString = "0" + Long.toString(integer_Minutes) + latLonSeparator;
          else
            minutesString = Long.toString(integer_Minutes) + latLonSeparator;

          if(integer_Seconds < 10)
            secondsString = "0" + this.doubleToString(seconds, (precision-4));
          else
            secondsString = this.doubleToString(seconds, (precision-4));
        }
        else
        {
          degreesString = Long.toString(integer_Degrees) + latLonSeparator;
          minutesString = Long.toString(integer_Minutes) + latLonSeparator;
          secondsString = this.doubleToString(seconds, (precision-4));
        }

        return addFractionZeros(degreesString + minutesString + secondsString, precision-4);    
      }
    }
  }


  /**
   * Converts a DMS, DM or D longitude string value to a double value. 
   *
   * @param str Longitude string to parse.
   * @return Double representation of the longitude string value.
   * @exception CoordinateConversionException if invalid longitude string.
   */
  public double stringToLongitude(String str) throws CoordinateConversionException
  {
    try
    {
      double val = 0.0;
      double degrees = 0.0;
      double minutes = 0.0;
      double seconds = 0.0;
      int sign = 1;
      char [] reference_Pointer = new char[str.length() + 1];
      String parse_String;
      String [] next_Str;
      /* Longitudes may have the following format :

         PDDD/MM/SS.FFH
         PDDD/MM.FFFFH
         PDDD.FFFFFFH

         where these are defined as follows
         P = optional plus/minus
         D = degrees (up to three places)
         M = minutes (up to two places)
         S = seconds (up to two places)
         F = floating-point precision (up to 6 places)
         H = optional hemisphere (NSEW)
         / = separator character, one of ':' , '/' , ' '
      */
      if (str != null && (str.length() > 0))
      {
        reference_Pointer = str.toCharArray();

        parse_String = str;
        if (validCoord(reference_Pointer, Long_String))
        {
          if (str.charAt(0) == '-')
          {
            sign = -1;
          }

          java.util.StringTokenizer token = new java.util.StringTokenizer(str, " :/WwEeNnSs");
          int count = token.countTokens();
          String nextStr = token.nextToken();
          if(nextStr != null)
            degrees =  Double.parseDouble(nextStr);
          if(token.hasMoreTokens())
          {
            nextStr = token.nextToken();
            if(nextStr != null)
              minutes =  Double.parseDouble(nextStr);
            if(token.hasMoreTokens())
            {
              nextStr = token.nextToken();
              if(nextStr != null)
                seconds =  Double.parseDouble(nextStr);
            }        
          }

          int indexOfN = str.indexOf('N');
          int indexOfn = str.indexOf('n');
          int indexOfS = str.indexOf('S');
          int indexOfs = str.indexOf('s');
          if ((indexOfN != -1) || (indexOfn != -1) ||
              (indexOfS != -1) || (indexOfs != -1))
          {
            throw new CoordinateConversionException(ErrorMessages.hemisphere);
          }

          int indexOfE = str.indexOf('E');
          int indexOfe = str.indexOf('e');
          if ((indexOfE != -1) ||
              (indexOfe != -1))
          {
            if (sign == -1)
            {
              throw new CoordinateConversionException(ErrorMessages.signHemisphere);
            }
          }

          int indexOfW = str.indexOf('W');
          int indexOfw = str.indexOf('w');
          if ((indexOfW != -1) ||
              (indexOfw != -1))
          {
            if (sign == -1)
            {
              throw new CoordinateConversionException(ErrorMessages.signHemisphere);
            }
            else
              sign = -1;
          }

          if (seconds >= 60 || seconds < 0)
          {
            throw new CoordinateConversionException(ErrorMessages.seconds);
          }

          if (minutes >= 60 || minutes < 0)
          {
            throw new CoordinateConversionException(ErrorMessages.minutes);
          }

          if ((degrees == -180 || degrees == 360) &&
              ((minutes != 0) || (seconds != 0)))
          {
            throw new CoordinateConversionException(ErrorMessages.longitude);
          }

          // Convert DMS to fractional degrees
          val = ( Math.abs(degrees) + (minutes / 60.0) + (seconds / 3600.0) ) * sign;

          // Convert longitude to be between -180 and 180
          if (val > 180)
            val -= 360;

          if (val < -180)
            val += 360;

          if ((val > 180) || (val < -180))
          {
            throw new CoordinateConversionException(ErrorMessages.degrees);
          }
        }
        else
          throw new CoordinateConversionException(ErrorMessages.longitude);
      }
      else
      {
        throw new CoordinateConversionException(ErrorMessages.noEntryError);
      }
      return val;
    }
    catch(Exception e)
    {
      throw new CoordinateConversionException(e.getMessage());      
    }
  }


  /**
   * Converts a DMS, DM or D latitude string value to a double value. 
   *
   * @param str Latitude string to parse.
   * @return Double representation of the latitude string value.
   * @exception CoordinateConversionException if invalid latitude string.
   */
  public double stringToLatitude(String str) throws CoordinateConversionException
  {
    try
    {
    double val = 0.0;
    double degrees = 0.0;
    double minutes = 0.0;
    double seconds = 0.0;
    int sign = 1;
    char [] reference_Pointer = new char[str.length() + 1];
    String parse_String;
    String [] next_Str;
    /* Latitudes may have the following format :

       DDMMSSFFFH

       where these are defined as follows
       D = degrees (up to two places)
       M = minutes (up to two places)
       S = seconds (up to tw
     o places)
       F = floating-point precision (up to 6 places)
       H = optional hemisphere (NSEW)
       / = separator character, one of / : sp
    */
    if (str != null && (str.length() > 0))
    {
      reference_Pointer = str.toCharArray();
      parse_String = str;
      if (validCoord(reference_Pointer, Lat_String))
      {
        if (str.charAt(0) == '-')
        {
          sign = -1;
        }
        
        java.util.StringTokenizer token = new java.util.StringTokenizer(str, " :/WwEeNnSs");
        int count = token.countTokens();
        String nextStr = token.nextToken();
        if(nextStr != null)
          degrees =  Double.parseDouble(nextStr);
        if(token.hasMoreTokens())
        {
          nextStr = token.nextToken();
          if(nextStr != null)
            minutes =  Double.parseDouble(nextStr);
          if(token.hasMoreTokens())
          {
            nextStr = token.nextToken();
            if(nextStr != null)
              seconds =  Double.parseDouble(nextStr);
          }        
        }

        int indexOfW = str.indexOf('W');
        int indexOfw = str.indexOf('w');
        int indexOfE = str.indexOf('E');
        int indexOfe = str.indexOf('e');
        if ((indexOfW != -1) || (indexOfw != -1) ||
            (indexOfE != -1) || (indexOfe != -1))
        {
          throw new CoordinateConversionException(ErrorMessages.hemisphere);
        }

        int indexOfN = str.indexOf('N');
        int indexOfn = str.indexOf('n');
        if ((indexOfN != -1) ||
            (indexOfn != -1))
        {
          if (sign == -1)
          {
            throw new CoordinateConversionException(ErrorMessages.signHemisphere);
          }
        }

        int indexOfS = str.indexOf('S');
        int indexOfs = str.indexOf('s');
        if ((indexOfS != -1) ||
            (indexOfs != -1))
        {
          if (sign == -1)
          {
            throw new CoordinateConversionException(ErrorMessages.signHemisphere);
          }
          else
            sign = -1;
        }

        if (seconds >= 60 || seconds < 0)
        {
          throw new CoordinateConversionException(ErrorMessages.seconds);
        }

        if (minutes >= 60 || minutes < 0)
        {
          throw new CoordinateConversionException(ErrorMessages.minutes);
        }

        if (degrees < -90 || degrees > 90)
        {
          throw new CoordinateConversionException(ErrorMessages.degrees);
        }

        if ((degrees == -90 || degrees == 90) &&
            ((minutes != 0) || (seconds != 0)))
        {
          throw new CoordinateConversionException(ErrorMessages.latitude);
        }

          // Convert DMS to fractional degrees
          val = (double)( Math.abs(degrees) + (minutes / 60) + (seconds / 3600) ) * sign;
      }
      else
        throw new CoordinateConversionException(ErrorMessages.latitude);
    }
    else
    {
      throw new CoordinateConversionException(ErrorMessages.noEntryError);
    }
    
    return val;
    }
    catch(Exception e)
    {
      throw new CoordinateConversionException(e.getMessage());      
    }
  }

  public String surveyFeetToString(final double feet)
  {
	  return meterToString(feet);
  }

  /**
   * Converts a meter value to a string using the current precision setting. 
   *
   * @param    meters    meter value to convert to a string
   * @return   string representation of the meter value
   * @see      ccs.enumerations.Precision	      
   */
  public String meterToString(final double meters)
  {
    double meter_Value = roundMeter(meters);

    java.text.DecimalFormat df = new java.text.DecimalFormat();
    df.setGroupingUsed(false);

    if (precision > 4)
    {
      df.setMinimumFractionDigits(precision - 5);
      df.setMaximumFractionDigits(precision - 5);
    }
    else
    {
      df.setMinimumFractionDigits(0);
      df.setMaximumFractionDigits(0);
    }
    
    return df.format(meter_Value);      
  }


  /**
   * Converts a double value to a string using the specified number of decimal places. 
   *
   * @param    value          value to convert to a string
   * @param    numDecimals    number of decimal places the string should display
   * @return   string representation of the double value
   */
  public String doubleToString(final double value, int numDecimals)
  {
    java.text.DecimalFormat df = new java.text.DecimalFormat();
    df.setGroupingUsed(false);

    df.setMinimumFractionDigits(numDecimals);
    df.setMaximumFractionDigits(numDecimals);
    
    return df.format(value);      
  }


  /**
   * Converts a string value to a double value. 
   *
   * @param str string to convert to a double.
   * @return Double representation of the input string.
   * @exception CoordinateConversionException if invalid string.
   */
  public double stringToDouble(String str) throws CoordinateConversionException
  {
      double returnDouble = 0.0;

      if (str != null && (str.length() > 0))
      {
          try
          {
              returnDouble = Double.parseDouble(str);
          }
          catch (NumberFormatException e)
          {
              throw new CoordinateConversionException(ErrorMessages.numericError);
          }
      }
      else
      {
          throw new CoordinateConversionException(ErrorMessages.noEntryError);
      }

      return returnDouble; 
  }


  /**
   * Converts a string value to an integer value. 
   *
   * @param str String to convert to an integer.
   * @return Integer representation of the input string.
   * @exception CoordinateConversionException if invalid string.
   */
  public int stringToInt(String str) throws CoordinateConversionException
  {
    int returnInt = 0;

    if (str != null && (str.length() > 0))
    {
        try
        {
            returnInt = Integer.parseInt(str);
        }
        catch (NumberFormatException e)
        {
            throw new CoordinateConversionException(ErrorMessages.numericError);
        }
    }
    else
    {
        throw new CoordinateConversionException(ErrorMessages.noEntryError);
    }

    return returnInt; 
  }

  /**
   * Checks if the string represents a valid latitude or longitude string. 
   *
   * @param    str     string to validate
   * @param    type    indicates if the value represents a latitude or longitude string
   * @return   true if the string is valid, false if the string is invalid
   */
  private boolean validCoord(char[] str, int type)
  {
    boolean Decimal = false;
    boolean Signed = false;
    int Separators = 0;
    boolean Valid = true;
    int Length;
    int Pos = 0;

    if (str != null)
    {
      Length = str.length;
      if ((Pos<Length) && ((str[Pos] == '-') || (str[Pos] == '+')))
      {
        Signed = true;
        Pos ++;
      }
      while ((Pos < Length) && Valid)
      {
        if (str[Pos] == '.')
        {
          if (Decimal)
            Valid = false;
          else
          {
            Decimal = true;
            Pos++;
          }
        }
        else if (Character.isDigit(str[Pos]))
        {
          Pos++;
        }
        else if ((str[Pos] == ' ') || (str[Pos] == '/') || (str[Pos] == ':'))
        {
          if (Separators >= 3)
            Valid = false;
          else
          {
            Pos++;
            Separators++;
          }
        }
        else if (Character.isLetter(str[Pos]))
        {
          String letter = "";
          letter = letter.valueOf(str[Pos]);

          if (((letter.equalsIgnoreCase("N") || letter.equalsIgnoreCase("S")) && (type == Lat_String))
              || ((letter.equalsIgnoreCase("W") || letter.equalsIgnoreCase("E")) && (type == Long_String)))
          {
            if (Signed)
              Valid = false;
            Pos++;
            if (Pos != Length)
              Valid = false;
          }
          else
            Valid = false;
        }
        else
          Valid = false;
      }
    }
    return (Valid);
  }


  /**
   *  Rounds the specified value, in meters, according to
   *  the current precision level.
   *
   * @param    value    value to be rounded
   * @return   the rounded value
   * @see      ccs.enumerations.Precision	      
   */
  private double roundMeter(final double value)
  { 
    double avalue;
    double divisor = 1.0;
    double fraction;
    double ivalue;
    double result;
    long ival = 0;
    int sign = 1;

    switch(precision)
    {
      case Precision.DEGREE:
      {
        divisor = 100000.0;
        break;
      }
      case Precision.TEN_MINUTE:
      {
        divisor = 10000.0;
        break;
      }
      case Precision.MINUTE:
      {
        divisor = 1000.0;
        break;
      }
      case Precision.TEN_SECOND:
      {
        divisor = 100.0;
        break;
      }
      case Precision.SECOND:
      {
        divisor = 10.0;
        break;
      }
      case Precision.TENTH_OF_SECOND:
      {
        divisor = 1.0;
        break;
      }
      case Precision.HUNDRETH_OF_SECOND:
      {
        divisor = 0.1;
        break;
      }
      case Precision.THOUSANDTH_OF_SECOND:
      {
        divisor = 0.01;
        break;
      }
      case Precision.TEN_THOUSANDTH_OF_SECOND:
      {
        divisor = 0.001;
        break;
      }
    }
    
    if (value < 0.0)
      sign = -1;
    avalue = Math.abs (value / divisor);
    ivalue = (long)avalue;
    ival = (long)avalue;
    fraction = avalue - ivalue;

    if ((fraction > 0.5) || ((fraction == 0.5) && (ival%2 == 1)))
      ivalue++;
    result = (double)(ivalue * divisor * sign);
    
    return result;
  } 


  /**
   *  Rounds the specified  value according to
   *  the input precision level.
   *
   * @param    value    value to be rounded
   * @param    place    precision level
   * @return   the rounded value
   */
  private double roundDMS(double val, int place)
  {
    double temp = 0;
    double fraction;
    double int_temp;

    temp = val * Math.pow(10, (double)place);

    int_temp = (long)temp;
    fraction = temp - int_temp;

    if (((temp - int_temp) > 0.5) ||
        (((temp - int_temp) == 0.5) && ((int_temp % 2.0) == 1.0)))
      return (int_temp + 1.0) / Math.pow(10, (double)place);
    else
      return int_temp / Math.pow(10, (double)place);
  }
  
  
  /**
   *  Adds zeros to the end of the decimal portion of the input string
   *
   * @param    _string    string to append
   * @param    length     number of required decimal values
   * @return   the appended string
   */
  private String addFractionZeros(String _string, int length)
  {
    int strLength = _string.length();
    int indexOfDecimalPoint = _string.indexOf(".", 0) + 1;
    int fractionLength = strLength - indexOfDecimalPoint;

    while(fractionLength < length)
    {
      _string += "0";
      fractionLength++;
    }
    
    return _string; 
  }  
}

// CLASSIFICATION: UNCLASSIFIED
