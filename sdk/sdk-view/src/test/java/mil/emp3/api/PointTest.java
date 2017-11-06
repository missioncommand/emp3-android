package mil.emp3.api;

import android.webkit.URLUtil;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoIconStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPoint;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.Collections;
import java.util.HashMap;

import mil.emp3.api.enums.FeatureTypeEnum;

import static mil.emp3.api.utils.ComparisonUtils.validatePoint;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by Matt.Miller on 10/16/2017.
 */
@PrepareForTest({URLUtil.class})
public class PointTest  TestBase {

    public static final String TAG = PointTest.class.getSimpleName();
    private static final double Epsilon = 1e-8;
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
        GeoPoint p2 = new GeoPoint();
        p2.setTimeStamp(p1.getTimeStamp());
        validatePoint(p1,
                      1.0,
                      0,
                      new GeoIconStyle(),
                      "",
                      p2,
                      FeatureTypeEnum.GEO_POINT,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      p2.getTimeStamp(),
                      Collections.EMPTY_LIST,
                      null,
                      new GeoStrokeStyle(),
                      new GeoFillStyle(),
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
                      "",
                      "",
                      new HashMap());
    }

    @Test
    public void stringConstructor() {
        final String sampleURL = "127.0.0.1";
        p1 = new Point(sampleURL);
        final GeoPoint gp = new GeoPoint();
        gp.setTimeStamp(p1.getTimeStamp());
        gp.setIconURI(sampleURL);
        validatePoint(p1,
                      1.0,
                      0,
                      new GeoIconStyle(),
                      sampleURL,
                      gp,
                      FeatureTypeEnum.GEO_POINT,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      gp.getTimeStamp(),
                      Collections.EMPTY_LIST,
                      null,
                      new GeoStrokeStyle(),
                      new GeoFillStyle(),
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
                      "",
                      "",
                      new HashMap());
    }

    @Test
    public void geoPointConstructor() {
        gp = new GeoPoint();
        p1 = new Point(gp);
        validatePoint(p1,
                      1.0,
                      0,
                      new GeoIconStyle(),
                      "",
                      gp,
                      FeatureTypeEnum.GEO_POINT,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      gp.getTimeStamp(),
                      Collections.EMPTY_LIST,
                      null,
                      new GeoStrokeStyle(),
                      new GeoFillStyle(),
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
                      "",
                      "",
                      new HashMap());
    }

    @Test
    public void latLongConstructor() {
        p1 = new Point(50,50);
        final GeoPosition geoPos = new GeoPosition();
        geoPos.setLatitude(50);
        geoPos.setLongitude(50);
        final GeoPoint geoPoint = new GeoPoint();
        geoPoint.setTimeStamp(p1.getTimeStamp());
        geoPoint.setPositions(Collections.singletonList(geoPos));
        validatePoint(p1,
                      1.0,
                      0,
                      new GeoIconStyle(),
                      "",
                      geoPoint,
                      FeatureTypeEnum.GEO_POINT,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      Collections.EMPTY_LIST,
                      Collections.singletonList(geoPos),
                      geoPoint.getTimeStamp(),
                      Collections.EMPTY_LIST,
                      null,
                      new GeoStrokeStyle(),
                      new GeoFillStyle(),
                      new GeoLabelStyle(),
                      false,
                      true,
                      0.0,
                      0.0,
                      geoPos,
                      false,
                      Collections.EMPTY_LIST,
                      false,
                      Collections.EMPTY_LIST,
                      "",
                      "",
                      "",
                      new HashMap());
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
        final int scale = 5;
        p1.setIconScale(scale);
        assertEquals(p1.getIconScale(), scale, Epsilon);
    }

    @Test
    public void testResourceId() {
        final int resourceID = 5;
        p1.setResourceId(resourceID);
        assertEquals(p1.getResourceId(), resourceID, Epsilon);
    }
}