package mil.emp3.api;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoSquare;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoSquare;

import static mil.emp3.api.Square.MINIMUM_WIDTH;
import static org.junit.Assert.*;

/**
 * Created by Matt.Miller on 10/16/2017.
 */
public class SquareTest extends TestBase{
    public static final String TAG = SquareTest.class.getSimpleName();
    private Square s1;
    private Square s2;
    private Square s3;
    private IGeoPosition igp;
    private IGeoSquare igs;


    @org.junit.Before
    public void setUp() throws Exception {
        s1 = new Square();
        igp = new GeoPosition();
        s1 = new Square(igp);
        s1 = new Square(igp, 5.4);
        igs = new GeoSquare();
        s1 = new Square(igs);
    }

    @org.junit.Test
    public void invalidConstructors() throws Exception {
        try {
            s2 = new Square(igp, .5);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid width. " + .5 + " Minimum supported " + MINIMUM_WIDTH);
        }
        igp = null;
        try {
            s2 = new Square(igp);
            fail();
        } catch (IllegalArgumentException e) {
        }
        try {
            s2 = new Square(igp, 5);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The coordinate can NOT be null and must have valid values.");
        }
        igs.setWidth(Double.NaN);
        try {
            s2 = new Square(igs);
            fail();
        } catch(Exception e ){
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a positive number");
        }
        igs.setWidth(0.45);
        try {
            s2 = new Square(igs);
            fail();
        } catch (Exception e) {
            assertEquals(e.getMessage(), "Invalid width. 0.45 Minimum supported " + MINIMUM_WIDTH);
        }
        igs = null;
        try {
            s2 = new Square(igs);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Encapsulated Square must be non-null");
        }

    }

    @org.junit.Test
    public void widthTest() throws Exception {
        try {
            s1.setWidth(Double.NaN);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a positive number");
        }
        try {
            s1.setWidth(.45);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid width. 0.45 Minimum supported " + MINIMUM_WIDTH);
        }
        try {
            s1.setWidth(-3.45);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, -3.45 is a negative number");
        }
        s1.setWidth(10);
        assertEquals(s1.getWidth(), 10,0);
    }


    @org.junit.Test
    public void getFeatureBoundingBox() throws Exception {
        s1 = new Square();
        s1.getFeatureBoundingBox();
        igp = new GeoPosition();
        s1.setPosition(igp);
        s1.setAzimuth(0.0);
        s1.getFeatureBoundingBox();
        s1.setAzimuth(10.0);
        s1.getFeatureBoundingBox();
    }

    @org.junit.Test
    public void stringTest() throws Exception {
        s1 = new Square();
        Log.i(TAG, s1.toString());
        igs = new GeoSquare();
        igp = new GeoPosition();
        s1.setPosition(igp);
        Log.i(TAG, s1.toString());
    }

}