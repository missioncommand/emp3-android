package mil.emp3.api;

import android.util.Log;
import org.cmapi.primitives.GeoCircle;
import org.cmapi.primitives.GeoPosition;
import org.powermock.core.classloader.annotations.PrepareForTest;
import mil.emp3.api.interfaces.IEmpBoundingBox;
import static mil.emp3.api.Circle.MINIMUM_RADIUS;
import static org.junit.Assert.*;

/**
 * Created by Matt.Miller on 10/17/2017.
 */
@PrepareForTest({IEmpBoundingBox.class})
public class CircleTest extends TestBase {

    public static final String TAG = CircleTest.class.getSimpleName();

    @org.junit.Test
    public void constructorTest() throws Exception {
        Circle c = new Circle();
        assertEquals(c.getRadius(), 100, 0);

        c = new Circle(50);
        assertEquals(c.getRadius(), 50, 0);

        c = new Circle(new GeoCircle());
        assertEquals(c.getRadius(), 100, 0);

        try {
            c = new Circle(null);
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Encapsulated GeoCircle must be non-null");
        }

        Log.i(TAG, c.toString());
        c.setPosition(new GeoPosition());
        Log.i(TAG, c.toString());
    }

    @org.junit.Test
    public void testRadius() throws Exception {
        Circle c = new Circle();
        try {
            c.setRadius(-5);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, -5.0 is a negative number");
        }
        try {
            c.setRadius(0);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, 0 is not a positive number");
        }
        try {
            c.setRadius(.5);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid radius, 0.5 Minimum supported " + MINIMUM_RADIUS);
        }
        try {
            c.setRadius(Double.NaN);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a positive number");
        }

        c.setRadius(100);
        assertEquals(c.getRadius(), 100, 0);
    }

    @org.junit.Test
    public void getFeatureBoundingBox() throws Exception {
        Circle c = new Circle();
        c.setPosition(new GeoPosition());
        c.getFeatureBoundingBox();

    }
}