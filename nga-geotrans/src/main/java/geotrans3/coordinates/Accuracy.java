// CLASSIFICATION: UNCLASSIFIED

/*
 * Accuracy.java
 *
 * Created on April 24, 2001, 11:54 AM
 */

package geotrans3.coordinates;

/**
 *
 * @author  amyc
 * @version 
 */
public class Accuracy extends Object {

    private double ce90;
    private double le90;
    private double se90;
    
    /** Creates new Accuracy */
    public Accuracy() 
    {
        ce90 = 0;
        le90 = 0;
        se90 = 0;
    }
    
    public Accuracy(double _ce90,double _le90,double _se90)
    {
        ce90 = _ce90;
        le90 = _le90;
        se90 = _se90;
    }
    
    public double getCE90()
    {
        return ce90;
    }
    
    public double getLE90()
    {
        return le90;
    }
    
    public double getSE90()
    {
        return se90;
    }
}

// CLASSIFICATION: UNCLASSIFIED
