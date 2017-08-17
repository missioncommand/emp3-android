// CLASSIFICATION: UNCLASSIFIED

/*
 * Platform.java
 *
 * Created on August 27, 2001, 4:05 PM
 */


/**
 *
 * @author  amyc
 * @version 
 */
package geotrans3.utility;

public class Platform extends Object {

    private static String os = System.getProperty("os.name");
    public static String separator = System.getProperty("file.separator");
    private static String javaVersion = System.getProperty("java.version");
    
    //Determine if operating system is Windows
    public static boolean isWindows;
    static
    {
        if ( os != null && os.startsWith("Windows")) 
            isWindows = true; 
        else 
            isWindows = false; 
    }

    //Determine if operating system is Windows
    public static boolean isWindows9x;
    static
    {
        if ( os != null && os.startsWith("Windows 9")) 
            isWindows9x = true; 
        else 
            isWindows9x = false; 
    }

    //Determine if operating system is Unix
    public static boolean isUnix;
    static
    {
        if ( (os != null) && (os.equals("SunOS") || (os.equals("Irix"))))
            isUnix = true; 
        else 
            isUnix = false; 
    } 
    
    //Determine if operating system is Unix
    public static boolean isLinux;
    static
    {
        if ( (os != null) && (os.equals("Linux")))
            isLinux = true; 
        else 
            isLinux = false; 
    } 
    
    public static boolean isJavaV1_3;
    static
    {
        if ( (javaVersion != null) && (javaVersion.startsWith("1.3")))
            isJavaV1_3 = true; 
        else 
            isJavaV1_3 = false; 
    }
        
    /** Creates new Platform */    
    public Platform() 
    {
    }    
}

// CLASSIFICATION: UNCLASSIFIED
