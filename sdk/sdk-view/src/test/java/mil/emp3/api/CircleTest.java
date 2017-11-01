package mil.emp3.api;

import android.util.Log;

import org.cmapi.primitives.GeoCircle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.Collections;
import java.util.HashMap;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.interfaces.IEmpBoundingBox;

import static mil.emp3.api.Circle.MINIMUM_RADIUS;
import static mil.emp3.api.utils.ComparisonUtils.validateCircle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by Matt.Miller on 10/17/2017.
 */
@PrepareForTest({IEmpBoundingBox.class})
public class CircleTest extends TestBase {

    private Circle c;

    @org.junit.Test
    public void constructorTest() throws Exception {
        c = new Circle();
        final GeoCircle gc = new GeoCircle();
        gc.setTimeStamp(c.getTimeStamp());
        validateCircle(c,
                        gc,
                        150.0,
                        FeatureTypeEnum.GEO_RECTANGLE,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        null,
                        Collections.EMPTY_LIST,
                        null,
                        new GeoStrokeStyle(),
                        null,
                        new GeoLabelStyle(),
                        false,
                        true,
                        0.0,
                        0.0,
                        null,
                        false,
                        Collections.EMPTY_LIST,
                        false,
                        Collections.EMPTY_LIST,
                        "",
                        null,
                        "",
                        new HashMap());
    }


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