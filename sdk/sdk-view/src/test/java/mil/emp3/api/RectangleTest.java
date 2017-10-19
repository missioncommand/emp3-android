package mil.emp3.api;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoRectangle;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoRectangle;

import static mil.emp3.api.Rectangle.MINIMUM_HEIGHT;
import static mil.emp3.api.Rectangle.MINIMUM_WIDTH;
import static org.junit.Assert.*;

/**
 * Created by Matt.Miller on 10/16/2017.
 */
public class RectangleTest extends TestBase{
    public static final String TAG = RectangleTest.class.getSimpleName();
    private Rectangle r1;
    private Rectangle r2;
    private Rectangle r3;
    private IGeoPosition igp;
    private IGeoRectangle igr;


    @org.junit.Before
    public void setUp() throws Exception {
        r1 = new Rectangle();
        igp = new GeoPosition();
        r1 = new Rectangle(igp);
        r1 = new Rectangle(igp, 5.4, 5.4);
        igr = new GeoRectangle();
        r1 = new Rectangle(igr);
    }

    @org.junit.Test
    public void invalidConstructors() throws Exception {
        try {
            r2 = new Rectangle(igp, .5, .5);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid width. " + .5 + " Minimum supported " + MINIMUM_WIDTH);
        }
        try {
            r2 = new Rectangle(igp, 2, .5);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid height. " + .5 + " Minimum supported " + MINIMUM_HEIGHT);
        }
        igp = null;
        try {
            r2 = new Rectangle(igp);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The coordinate can NOT be null and must have valid values");
        }
        try {
            r2 = new Rectangle(igp, 5, 5);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The coordinate can NOT be null and must have valid values");
        }
        igr.setWidth(Double.NaN);
        try {
            r2 = new Rectangle(igr);
            fail();
        } catch(final Exception e ){
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a positive number");
        }
        igr.setWidth(0.45);
        try {
            r2 = new Rectangle(igr);
            fail();
        } catch (final Exception e) {
            assertEquals(e.getMessage(), "Invalid width. 0.45 Minimum supported " + MINIMUM_WIDTH);
        }
        igr.setWidth(5);
        igr.setHeight(Double.NaN);
        try {
            r2 = new Rectangle(igr);
            fail();
        } catch(final Exception e ){
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a positive number");
        }
        igr.setWidth(5);
        igr.setHeight(.45);
        try {
            r2 = new Rectangle(igr);
            fail();
        } catch (final Exception e) {
            assertEquals(e.getMessage(), "Invalid height. 0.45 Minimum supported " + MINIMUM_HEIGHT);
        }
        igr = null;
        try {
            r2 = new Rectangle(igr);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Encapsulated Rectangle must be non-null");
        }

    }

    @org.junit.Test
    public void widthTest() throws Exception {
        try {
            r1.setWidth(Double.NaN);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a positive number");
        }
        try {
            r1.setWidth(.45);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid width. 0.45 Minimum supported " + MINIMUM_WIDTH);
        }
        try {
            r1.setWidth(-3.45);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, -3.45 is a negative number");
        }
        r1.setWidth(10);
        assertEquals(r1.getWidth(), 10,0);
    }

    @org.junit.Test
    public void heightTest() throws Exception {
        try {
            r1.setHeight(Double.NaN);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a positive number");
        }
        try {
            r1.setHeight(.45);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid height. 0.45 Minimum supported " + MINIMUM_HEIGHT);
        }
        try {
            r1.setHeight(-3.45);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, -3.45 is a negative number");
        }
        r1.setHeight(10);
        assertEquals(r1.getHeight(), 10,0);
    }

    @org.junit.Test
    public void getFeatureBoundingBox() throws Exception {
        r1 = new Rectangle();
        r1.getFeatureBoundingBox();
        igp = new GeoPosition();
        r1.setPosition(igp);
        r1.setAzimuth(0.0);
        r1.getFeatureBoundingBox();
        r1.setAzimuth(10.0);
        r1.getFeatureBoundingBox();
    }

    @org.junit.Test
    public void stringTest() throws Exception {
        r1 = new Rectangle();
        Log.i(TAG, r1.toString());
        igp = new GeoPosition();
        r1.setPosition(igp);
        Log.i(TAG, r1.toString());
    }

}