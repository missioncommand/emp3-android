package mil.emp3.api;

import android.util.Log;

import org.cmapi.primitives.GeoEllipse;
import org.cmapi.primitives.GeoPosition;

import static mil.emp3.api.Ellipse.MINIMUM_SEMI_MAJOR;
import static mil.emp3.api.Ellipse.MINIMUM_SEMI_MINOR;
import static org.junit.Assert.*;

/**
 * Created by Matt.Miller on 10/17/2017.
 */
public class EllipseTest extends TestBase{
    public static final String TAG = EllipseTest.class.getSimpleName();

    @org.junit.Test
    public void testConstructors() throws Exception {
        Ellipse ell = new Ellipse();
        assertEquals(ell.getSemiMajor(), 150, 0);
        assertEquals(ell.getSemiMinor(), 75, 0);

        ell = new Ellipse(50, 100);
        assertEquals(ell.getSemiMajor(), 50, 0);
        assertEquals(ell.getSemiMinor(), 100, 0);

        try {
            ell = new Ellipse(-10, 50);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Invalid Input, -10.0 is a negative number");
        }
        try {
            ell = new Ellipse(5, -10);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Invalid Input, -10.0 is a negative number");
        }

        try {
            ell = new Ellipse(0, 50);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Invalid Input, 0 is not a positive number");
        }

        try {
            ell = new Ellipse(10, 0);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Invalid Input, 0 is not a positive number");
        }

        try {
            ell = new Ellipse(.5, 50);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Invalid semimajor, 0.5 Minimum supported " + MINIMUM_SEMI_MAJOR);
        }

        try {
            ell = new Ellipse(10, .5);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Invalid semiminor, 0.5 Minimum supported " + MINIMUM_SEMI_MINOR);
        }

        try {
            ell = new Ellipse(10, 10, -400);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Value is out of range (-400.0).");
        }
        ell = new Ellipse(10, 10, 0);
        assertEquals(ell.getSemiMajor(), 10, 0);
        assertEquals(ell.getSemiMinor(), 10, 0);
        assertEquals(ell.getAzimuth(), 0, 0);

        ell = new Ellipse(new GeoEllipse());
        assertEquals(ell.getSemiMinor(), 75, 0);
        assertEquals(ell.getSemiMajor(), 150, 0);
        assertEquals(ell.getAzimuth(), 0, 0);

        try {
            ell = new Ellipse(null);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Encapsulated GeoEllipse must be non-null");
        }
    }

    @org.junit.Test
    public void testSemiMajor() throws Exception {
        final Ellipse ell = new Ellipse();
        try {
            ell.setSemiMajor(Double.NaN);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a positive number");
        }
        try {
            ell.setSemiMajor(-1);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, -1.0 is a negative number");
        }
        try {
            ell.setSemiMajor(0);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, 0 is not a positive number");
        }
        try {
            ell.setSemiMajor(.5);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid semimajor, 0.5 Minimum supported " + MINIMUM_SEMI_MAJOR);
        }

        ell.setSemiMajor(10);
        assertEquals(ell.getSemiMajor(), 10, 0);
    }

    @org.junit.Test
    public void testSemiMinor() throws Exception {
        final Ellipse ell = new Ellipse();
        try {
            ell.setSemiMinor(Double.NaN);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a positive number");
        }
        try {
            ell.setSemiMinor(-1);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, -1.0 is a negative number");
        }
        try {
            ell.setSemiMinor(0);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, 0 is not a positive number");
        }
        try {
            ell.setSemiMinor(.5);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid semiminor, 0.5 Minimum supported " + MINIMUM_SEMI_MINOR);
        }

        ell.setSemiMinor(10);
        assertEquals(ell.getSemiMinor(), 10, 0);
    }

    @org.junit.Test
    public void getFeatureBoundingBox() throws Exception {
        final Ellipse e = new Ellipse();
        e.setPosition(new GeoPosition());
        e.getFeatureBoundingBox();
    }

    @org.junit.Test
    public void stringTest() throws Exception {
        final Ellipse e = new Ellipse();
        Log.i(TAG, e.toString());
        e.setPosition(new GeoPosition());
        Log.i(TAG, e.toString());
    }

}