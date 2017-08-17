// CLASSIFICATION: UNCLASSIFIED

/*
 * jOptions.java
 * Created in September 2000;  
 *
 * @author  Carol Finn
 * This class exists to store values from the FormatOptionsDlg class, 
 * a dialog box, and use them to format output coordinate Strings.
 */

package geotrans3.misc;


import geotrans3.exception.CoordinateConversionException;
import geotrans3.misc.StringToVal;


public class FormatOptions 
{    
    // Geodetic Units
    public static final int DMS = 0;
    public static final int DM  = 1;
    public static final int DEG = 2;

    // Geodetic Separator
    public static final char SPACE = ' ';
    public static final char SLASH = '/';
    public static final char COLON = ':';

    // Sign/Hemisphere (+/- or N/S)
    public static final int SIGN = 0;
    public static final int HEMI = 1;

    // Longitude Range 
    public static final int _180_180 = 0;
    public static final int _0_360 = 1;

    // Precision
    // Note: The '_' character stands for a decimal point. 
    public static final int MET100000 = 0;
    public static final int MET10000  = 1;
    public static final int MET1000   = 2;
    public static final int MET100    = 3;
    public static final int MET10     = 4;
    public static final int MET1    = 5;
    public static final int MET_1   = 6;
    public static final int MET_01  = 7;
    public static final int MET_001 = 8;
    public static final int MET_SCI = 9;        // Scientific notation

    //LSC-2325
    public static final int METER = 0;
    public static final int SURVEY_FEET = 1;

    private int units = DMS;  
    private char separator = SPACE;
    private int signHemi = HEMI;
    private int range = _180_180;
    private int precision = MET1;
    private boolean leadingZeros = false;
    
    //LSC-2325
    private int heightUnit = METER;

    private String unitsString = "DMS";
    private int separatorIndex = 0;
    private String signHemiString = "N/S,E/W";
    private String rangeString = "(-180,180)";
    private String precisionString = "1m";  
    private StringToVal stringToVal = new StringToVal();
    
    
    public FormatOptions() 
    {
    }

    public FormatOptions(FormatOptions formatOptions) 
    {
        setOptions(formatOptions);
    }
    
   
  public StringToVal getStringToVal()
  {
    return stringToVal;
  }


    // Get/set geodetic units
    public int getUnits()
    {
        return units;
    }

    public String getUnitsString()
    {
        return unitsString;
    }

    public void setUnits(int unit)
    {
        units = unit;
        switch(units)
        {
          case DMS:
            unitsString = "DMS";
            break;
          case DM:
            unitsString = "DM";
            break;
          case DEG:
            unitsString = "D";
            break;
        }
    }

    // Get/set geodetic separator
    public char getSeparator()
    {
        return separator;
    }

    public int getSeparatorIndex()
    {
        return separatorIndex;
    }

    public void setSeparator(char separ)
    {
        separator = separ;
        stringToVal.setSeparator(separ);
        switch(separator)
        {
          case SPACE:
            separatorIndex = 0;
            break;
          case SLASH:
            separatorIndex = 1;
            break;
          case COLON:
            separatorIndex = 2;
            break;
        }
    }
    
    public void setSeparator(int separ)
    {
        separatorIndex = separ;
        switch(separatorIndex)
        {
          case 0:
            separator = SPACE;
            break;
          case 1:
            separator = SLASH;
            break;
          case 2:
            separator = COLON;
            break;
        }
        stringToVal.setSeparator(separator);
    }

    // Get/set sign/hemisphere
    public int getSignHemi()
    {
        return signHemi;
    }

    public String getSignHemiString()
    {
        return signHemiString;
    }

    public void setSignHemi(int signhemi)
    {
        signHemi = signhemi;
        switch(signHemi)
        {
          case SIGN:
            signHemiString = "+/-";
            break;
          case HEMI:
            signHemiString = "N/S,E/W";
            break;
        }      
    }

    // Get/set longitude range
    public int getRange()
    {
        return range;
    }
  
    public String getRangeString()
    {
        return rangeString;
    }
  
    public void setRange(int lonRange)
    {
        range = lonRange;
        stringToVal.setLongRange(lonRange);
        switch(range)
        {
          case SIGN:
            rangeString = "(-180,180)";
            break;
          case HEMI:
            rangeString = "(0,360)";
            break;
        }
    }
  
    // Get/set precision
    public int getPrecision()
    {
        return precision;
    }

    //LSC-2325
    public int getHeightUnit()
    {
        return heightUnit;
    }

    public void setHeightUnit(int h)
    {
        heightUnit = h;
    }

    public String getPrecisionString()
    {
        return precisionString;
    }

    public void setPrecision(int prec)
    {
        precision = prec;
        stringToVal.setPrecision(prec);
        switch(precision)
        {
          case MET100000:
            precisionString = "100,000m";
            break;
          case MET10000:
            precisionString = "10,000m";
            break;
          case MET1000:
            precisionString = "1,000m";
            break;
          case MET100:
            precisionString = "100m";
            break;
          case MET10:
            precisionString = "10m";
            break;
          case MET1:
            precisionString = "1m";
            break;
          case MET_1:
            precisionString = "0.1m";
            break;
          case MET_01:
            precisionString = "0.01m";
            break;
          case MET_001:
            precisionString = "0.001m";
            break;
          case MET_SCI:
            precisionString = "Scientific Notation";
            break;        }
    }

  // Get/set leading zeros
    public boolean getLeadingZeros()
    {
        return leadingZeros;
    }
  
    public void setLeadingZeros(boolean lz)
    {
        leadingZeros = lz;
        stringToVal.showLeadingZeros(lz);
    }
    
    // Check if current sign/hemisphere setting uses hemisphere
    public boolean getUseNSEW()
    {
        if (signHemi == HEMI)
            return true;
        else
            return false;
    }
    
    // Check if current geodetic units setting uses minutes
    public boolean getUseMinutes()
    {
        if (units != DEG)
            return true;
        else 
            return false;
    }
    
    // Check if current geodetic units setting uses seconds
    public boolean getUseSeconds()
    {
        if (units == DMS)
            return true;
        else 
            return false;
    }  
    
    // Reset the formatting options
    public void reset(FormatOptions formatOptions)
    {
        setOptions(formatOptions);
        stringToVal.setPrecision(formatOptions.precision);
        stringToVal.setSeparator(formatOptions.separator);
        stringToVal.setLongRange(formatOptions.range);
        stringToVal.showLeadingZeros(formatOptions.leadingZeros);
    }
    
    // Set the formatting options
    private void setOptions(FormatOptions formatOptions)
    {
        units = formatOptions.units;
        signHemi = formatOptions.signHemi;
        precision = formatOptions.precision;
        separator = formatOptions.separator;
        range = formatOptions.range;
        leadingZeros = formatOptions.leadingZeros;
        heightUnit = formatOptions.heightUnit;
    }            
} 

// CLASSIFICATION: UNCLASSIFIED
