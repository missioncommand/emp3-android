// CLASSIFICATION: UNCLASSIFIED


package geotrans3.utility;

import geotrans3.utility.Platform;
import java.io.*;
import java.util.*;

public class ReadEnv 
{
 public static Properties getEnvVars() throws Throwable 
 {
    Process p = null;
    Properties envVars = new Properties();
    Runtime r = Runtime.getRuntime();

    if (Platform.isWindows9x == true)
    {
      p = r.exec( "command.com /c set" );
    }
    else if(Platform.isWindows == true)
    {
      p = r.exec( "cmd.exe /c set" );
    }
    else 
    {
      p = r.exec( "env" );
    }
    
    BufferedReader br = new BufferedReader( new InputStreamReader( p.getInputStream() ) );
    String line;
    while( (line = br.readLine()) != null ) 
    {
      int idx = line.indexOf( '=' );
      String key = line.substring( 0, idx );
      String value = line.substring( idx+1 );
      envVars.setProperty( key, value );

   ///   System.out.println( key + " = " + value );
    }
    
    return envVars;
  }

  public static void main(String args[])
  {
    try 
    {
      Properties p = ReadEnv.getEnvVars();
      System.out.println("the current value of TEMP is : " +
      p.getProperty("TEMP"));
    }
    catch (Throwable e) 
    {
      e.printStackTrace();
    }
  }
}


// CLASSIFICATION: UNCLASSIFIED
