package mil.emp3.api;

import android.test.mock.MockContext;
import android.util.Log;
import android.webkit.URLUtil;

import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoPoint;
import org.cmapi.primitives.IGeoIconStyle;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.when;
import org.junit.*;

/**
 * Created by Matt.Miller on 10/16/2017.
 */
@PrepareForTest({URLUtil.class})
public class PointTest extends TestBase {

    public static final String TAG = PointTest.class.getSimpleName();
    private static final double Epsilon = 1e-8;    //With nanometer precision
    Point p1;
    private GeoPoint gp;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(URLUtil.class);
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(true);
        p1 = new Point();
    }

    @Test
    public void defaultConstructor() {
        p1 = new Point();
    }

    @Test
    public void stringConstructor() {
        final String sampleURL = "127.0.0.1";
        p1 = new Point(sampleURL);
    }

    @Test
    public void geoPointConstructor() {
        gp = new GeoPoint();
        p1 = new Point(gp);
    }

    @Test
    public void latLongConstructor() {
        p1 = new Point(50,50);
    }

    @Test
    public void testToString() {
        p1 = new Point(50,50);
        final String expectedResult = "Point at\n" +
                                "\tlatitude: 50.0\n" +
                                "\tlongitude: 50.0\n" +
                                "\taltitude: 0.0";
        assertEquals(p1.toString(), expectedResult);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidURLConstructor() {
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(false);
        p1 = new Point("xyz");
    }

    @Test(expected = IllegalArgumentException.class)
    public void nanLatConstructor() {
        p1 = new Point(Double.NaN, 50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nanLongConstructor() {
        p1 = new Point(50, Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLatConstructor() {
        p1 = new Point(-100, 50);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLongConstructor() {
        p1 = new Point(50, -300);
    }

    @Test
    public void testIconStyle() throws Exception {
        final GeoIconStyle style = new GeoIconStyle();
        p1.setIconStyle(style);
        assertEquals(style, p1.getIconStyle());
    }

    @Test
    public void testIconURISuccess() throws Exception {
        final String sampleURL = "127.0.0.1";
        p1.setIconURI(sampleURL);
        assertEquals(p1.getIconURI(), sampleURL);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIconURIInvalidURL() {
        when(URLUtil.isValidUrl(any(String.class))).thenReturn(false);
        p1.setIconURI("xyz");
    }


    @Test(expected = IllegalArgumentException.class)
    public void testIconScaleNegavie() {
        p1.setIconScale(-5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIconScaleZero() {
        p1.setIconScale(0);
    }

    @Test
    public void testSetIconScaleSuccess() {
        p1.setIconScale(5);
        assertEquals(p1.getIconScale(),5,Epsilon);
    }

    @Test
    public void testResourceId() {
        final int resourceID = 5;
        p1.setResourceId(resourceID);
        assertEquals(p1.getResourceId(), resourceID,Epsilon);
    }
}