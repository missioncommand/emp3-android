package mil.emp3.api;

import org.cmapi.primitives.GeoCircle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.util.Collections;
import java.util.HashMap;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.utils.EmpBoundingBox;

import static mil.emp3.api.utils.ComparisonUtils.Epsilon;
import static mil.emp3.api.utils.ComparisonUtils.validateBoundingBox;
import static mil.emp3.api.utils.ComparisonUtils.validateCircle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Matt.Miller on 10/17/2017.
 */
@PrepareForTest({EmpBoundingBox.class})
public class CircleTest extends TestBase {

    private Circle c;

    @Test
    public void constructorTest() throws Exception {
        c = new Circle();
        final GeoCircle gc = new GeoCircle();
        gc.setTimeStamp(c.getTimeStamp());
        gc.setFillStyle(null);
        validateCircle(c,
                        gc,
                        100.0,
                        FeatureTypeEnum.GEO_CIRCLE,
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

    @Test
    public void radiusConstructorTest() throws Exception {
        c = new Circle(50.0);
        final GeoCircle gc = new GeoCircle();
        gc.setRadius(50.0);
        gc.setTimeStamp(c.getTimeStamp());
        gc.setFillStyle(null);
        validateCircle(c,
                gc,
                50.0,
                FeatureTypeEnum.GEO_CIRCLE,
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

    @Test
    public void geoCircleConstructorTest() throws Exception {
        final GeoCircle gc = new GeoCircle();
        c = new Circle(gc);
        gc.setTimeStamp(c.getTimeStamp());
        gc.setFillStyle(null);
        validateCircle(c,
                gc,
                100.0,
                FeatureTypeEnum.GEO_CIRCLE,
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

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoCircleConstructor() {
        c = new Circle(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NegativeRadiusConstructor() {
        c = new Circle(-45.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroRadiusConstructor() {
        c = new Circle(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NaNRadiusConstructor() {
        c = new Circle(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallRadiusConstructor() {
        c = new Circle(0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSetRadius() {
        c = new Circle();
        c.setRadius(-10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSetRadius() {
        c = new Circle();
        c.setRadius(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NaNSetRadius() {
        c = new Circle();
        c.setRadius(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSetRadius() {
        c = new Circle();
        c.setRadius(0.5);
    }

    @Test
    public void setRadius() {
        c = new Circle();
        c.setRadius(10.0);
        assertEquals(c.getRadius(), 10.0, Epsilon);
    }

    @Test
    public void nullPositionFeatureBoundingBox() {
        c = new Circle();
        assertNull(c.getFeatureBoundingBox());
    }

    @Test
    public void featureBoundingBox() throws Exception {
        final GeoPosition gp = new GeoPosition();
        c = new Circle();
        c.setPosition(gp);
        final Double coord = 9.881468125740867*Math.pow(10,-4);
//
//        final EmpBoundingBox empBoundingBoxMock = PowerMockito.mock(EmpBoundingBox.class);
//        when(empBoundingBoxMock.deltaLatitude()).thenReturn(0.0);
//        when(empBoundingBoxMock.deltaLongitude()).thenReturn(0.0);
//        PowerMockito.whenNew(EmpBoundingBox.class).withNoArguments().thenReturn(empBoundingBoxMock);

        validateBoundingBox((EmpBoundingBox) c.getFeatureBoundingBox(),
                                             coord,
                                             coord,
                                             -1*coord,
                                             -1*coord);
    }
}