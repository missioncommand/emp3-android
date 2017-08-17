// CLASSIFICATION: UNCLASSIFIED

/******************************************************************************
* Filename: StringHandler.java
*
* Copyright BAE Systems Inc. 2012 ALL RIGHTS RESERVED
*
* MODIFICATION HISTORY
*
* DATE      NAME        DR#          DESCRIPTION
*
* 08/13/10  S Gillis    BAEts27457   Update to GeoTrans 3.1
* 05/31/11  K. Lam      BAEts28657   Update version to 3.2
* 07/13/11  K. Swanson  BAEts27157   Add ability to set leading
*                                    zeros, separator, lon range.
* 11/18/11  K. Lam      MSP_029475   Update version to 3.3 
* 07/18/12  S. Gillis   MSP_00029550 Updated exception handling 
* 01/12/16  K. Chen     MSP_00030518 Update version to 3.7 and add US Survey Feet Support
*****************************************************************************/

package geotrans3.misc;

import geotrans3.misc.StringToVal;
import geotrans3.exception.CoordinateConversionException;
import geotrans3.enumerations.CoordinateType;
import geotrans3.enumerations.SourceOrTarget;
import geotrans3.utility.Constants;

public class StringHandler extends Object 
{
  private final String title = "MSP GEOTRANS 3.7";
  
  private String errorMsg[];
  private boolean ccsError = false;
  private int numErrors = 0;
  private StringToVal stringToVal;
  private final int maxErrors = 20;

  
  /** Creates new StringHandler */
  public StringHandler() 
  {
    errorMsg = new String[maxErrors];
    stringToVal = new StringToVal();
    initialize();
  }
    
    
  public StringHandler(StringToVal _stringToVal) 
  {
    errorMsg = new String[maxErrors];
    stringToVal = _stringToVal;
    initialize();
  }


  public void setPrecision(int _precision)
  {
    stringToVal.setPrecision(_precision);
  }

  public void setLeadingZeros (boolean leadingZeros )
  {
    stringToVal.showLeadingZeros(leadingZeros);
  }

  public void setSeparator (char sepChar )
  {
    stringToVal.setSeparator(sepChar);
  }
  
  public void setLonRange (int lonRange )
  {
    stringToVal.setLongRange(lonRange);
  }

    
  public String longitudeToString(final double longitude, boolean use_NSEW, boolean use_Minutes, boolean use_Seconds) throws CoordinateConversionException
  {
    return stringToVal.longitudeToString(longitude, use_NSEW, use_Minutes, use_Seconds);
  }


  public String latitudeToString(final double latitude, boolean use_NSEW, boolean use_Minutes, boolean use_Seconds) throws CoordinateConversionException
  {
    return stringToVal.latitudeToString(latitude, use_NSEW, use_Minutes, use_Seconds);
  }


  public String meterToString(final double meters)
  {
    return stringToVal.meterToString(meters);
  }

  public String surveyFeetToString(final double feet)
  {
	  return stringToVal.surveyFeetToString(feet);
  }

  public double stringToLatitude(String str, String msg)
  {
    double lat = 0;

    try
    {
      lat = stringToVal.stringToLatitude(str) * Constants.PI_OVER_180;
    }   
    catch(CoordinateConversionException e)
    {
      errorMsg[numErrors] = msg + ": " + e.getMessage();
      numErrors++;
      ccsError = true;
    }
    
    return lat;
  }
    
  public double stringToLongitude(String str, String msg)
  {
    double lon = 0;

    try
    {
      lon = stringToVal.stringToLongitude(str) * Constants.PI_OVER_180;
    }   
    catch(CoordinateConversionException e)
    {
      errorMsg[numErrors] = msg + ": " + e.getMessage();
      numErrors++;
      ccsError = true;
    }
    
    return lon;
  }
  
  
  public double stringToDouble(String str, String msg)
  {
    double num = 0;

    try
    {
      num = stringToVal.stringToDouble(str);
    }   
    catch(CoordinateConversionException e)
    {
      errorMsg[numErrors] = msg + ": " + e.getMessage();
      numErrors++;            
      ccsError = true;
    }
    
    return num;
  }

  
  public int stringToInt(String str, String msg)
  {
    int num = 0;

    try
    {
        num = stringToVal.stringToInt(str);
    }
    catch (CoordinateConversionException e)
    {
        errorMsg[numErrors] = msg + ": " + e.getMessage();
        numErrors++;
        ccsError = true;
    }

    return num;
  }
    
/*
  public void displayErrorMsg(java.awt.Component parent)
  {
    javax.swing.JOptionPane.showMessageDialog(parent, errorMsg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
    initialize();
  }  
    
  
  public void displayErrorMsg(java.awt.Component parent, String msg)
  {
    javax.swing.JOptionPane.showMessageDialog(parent, "Error: \n" + msg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
  }  

  
  public void displayErrorMsg(java.awt.Component parent, int dir, int projType)
  {
    errorMsg[0] = "Error: \n" + SourceOrTarget.name(dir) + " " + CoordinateType.name(projType) + ": ";
    errorMsg[1] = " ";
    javax.swing.JOptionPane.showMessageDialog(parent, errorMsg, title, javax.swing.JOptionPane.ERROR_MESSAGE);
    initialize();
  }  

  
  public void displayWarningMsg(java.awt.Component parent, String msg)
  {
    javax.swing.JOptionPane.showMessageDialog(parent, "Warning: \n" + msg, title, javax.swing.JOptionPane.WARNING_MESSAGE);
  }  

  
  public void displayPlainMsg(java.awt.Component parent, String msg)
  {
    javax.swing.JOptionPane.showMessageDialog(parent, msg, title, javax.swing.JOptionPane.PLAIN_MESSAGE);
  }  

*/
  
  public boolean getError()
  {
    return ccsError;
  }
  
  
  public void setErrorMessage(boolean error, String msg)
  {
    ccsError = error;
    errorMsg[numErrors] = msg;
    numErrors++;
  }
    
  
  private void initialize()
  {
    ccsError = false;
    numErrors = 2;
    for (int i = 0; i < maxErrors; i++)
        errorMsg[i] = "";
  }

 /*
  public void setNumberFormat(javax.swing.JTextField textField, double num, int digits)
  {
    java.text.NumberFormat nf = java.text.NumberFormat.getNumberInstance();
    nf.setMaximumFractionDigits(digits);
    nf.setMinimumFractionDigits(digits);
    textField.setText(nf.format(num));
  }
  */
  
  public String getTitle()
  {
    return title;
  }
}

// CLASSIFICATION: UNCLASSIFIED
