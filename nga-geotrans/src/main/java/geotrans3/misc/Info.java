// CLASSIFICATION: UNCLASSIFIED

/*
 * Info.java
 *
 * Created on April 3, 2007, 10:26 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package geotrans3.misc;

/**
 *
 * @author comstam
 */
public class Info 
{
  private String code;
  private String name;
  private String datumEllipsoidCode;
  
  /** Creates a new instance of Info */
  public Info() 
  {
    code = "";
    name = "";
    datumEllipsoidCode = "";
  }
  
  
  public Info(String _code, String _name)
  {
    code = _code;
    name = _name;
    datumEllipsoidCode = "";
  }
  
  
  public Info(String _code, String _name, String _datumEllipsoidCode)
  {
    code = _code;
    name = _name;
    datumEllipsoidCode = _datumEllipsoidCode;
  }
  
  
  public String getCode()
  {
    return code;
  }
  
  
  public String getName()
  {
    return name;
  }
  
  
  public String getDatumEllipsoidCode()
  {
    return datumEllipsoidCode;
  }
}

// CLASSIFICATION: UNCLASSIFIED
