package mil.emp3.api;

import android.test.mock.MockContext;
import android.util.Log;
import android.webkit.URLUtil;

import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoPoint;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by Matt.Miller on 10/16/2017.
 */
@PrepareForTest({URLUtil.class})
public class PointTest extends TestBase {
    class MyMockContext extends MockContext {
        @Override
        public File getDir(String name, int mode) {
            Log.d(TAG, "Current Dir " + System.getProperty("user.dir"));
            return new File(System.getProperty("user.dir") + File.separator + name);
        }
    }

    public static final String TAG = PointTest.class.getSimpleName();
    Point p1;
    GeoPoint gp;

    @org.junit.Before
    public void setUp() throws Exception {
        MockContext context = new PointTest.MyMockContext();
        PowerMockito.mockStatic(URLUtil.class);
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(true);
        p1 = new Point();
        String s = "127.0.0.1";
        p1 = new Point(s);
        gp = new GeoPoint();
        p1 = new Point(gp);
        p1 = new Point(50,50);
        Log.i(TAG, p1.toString());

    }

    @org.junit.Test
    public void badConstructors() throws Exception {
        final MockContext context = new PointTest.MyMockContext();
        PowerMockito.mockStatic(URLUtil.class);
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(false);
        try {
            p1 = new Point("xyz");
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid url input, xyz is not a valid url");
        }
        try {
            p1 = new Point(Double.NaN, 50);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a valid latitude");
        }
        try {
            p1 = new Point(50, Double.NaN);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, NaN is not a valid longitude");
        }
        try {
            p1 = new Point(-100, 50);
            fail();
        } catch(final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, -100.0 is not in the valid latitude range -90 to 90");
        }
        try {
            p1 = new Point(50, -300);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, -300.0 is not in the valid longitude range -180 to 180");
        }
    }

    @org.junit.Test
    public void testIconStyle() throws Exception {
        p1 = new Point();
        p1.setIconStyle(new GeoIconStyle());
        assertNotNull(p1.getIconStyle().toString());
    }

    @org.junit.Test
    public void testIconURI() throws Exception {
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(true);
        p1 = new Point();
        String s = "127.0.0.1";
        p1.setIconURI(s);
        assertEquals(p1.getIconURI(), s);
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(false);
        try {
            p1.setIconURI("xyz");
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(),"Invalid Input, xyz is not a valid URL");
        }
    }


    @org.junit.Test
    public void testIconScale() throws Exception {
        try {
            p1.setIconScale(-5);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, -5.0 is a negative number");
        }
        try {
            p1.setIconScale(0);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "Invalid Input, 0 is not a positive number");
        }
        p1.setIconScale(5);
        assertEquals(p1.getIconScale(),5,0);
    }

    @org.junit.Test
    public void testResourceId() throws Exception {
        p1.setResourceId(5);
        assertEquals(p1.getResourceId(), 5,0);
    }

}