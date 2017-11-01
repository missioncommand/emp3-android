package mil.emp3.api;

import org.cmapi.primitives.GeoEllipse;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.utils.EmpBoundingBox;

import static mil.emp3.api.utils.ComparisonUtils.Epsilon;
import static mil.emp3.api.utils.ComparisonUtils.validateBoundingBox;
import static mil.emp3.api.utils.ComparisonUtils.validateEllipse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Matt.Miller on 10/17/2017.
 */
public class EllipseTest extends TestBase {

    private Ellipse el;

    @Test
    public void constructorTest() throws Exception {
        el = new Ellipse();
        final GeoEllipse gc = new GeoEllipse();
        gc.setTimeStamp(el.getTimeStamp());
        gc.setFillStyle(null);
        validateEllipse(el,
                        gc,
                        150.0,
                        75.0
                        FeatureTypeEnum.GEO_ELLIPSE,
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
        el = new Ellipse(50.0, 50.0);
        final GeoEllipse gc = new GeoEllipse();
        gc.setSemiMajor(50.0);
        gc.setSemiMajor(50.0);
        gc.setTimeStamp(el.getTimeStamp());
        gc.setFillStyle(null);
        validateEllipse(el,
                gc,
                50.0,
                50.0
                FeatureTypeEnum.GEO_ELLIPSE,
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
    public void geoEllipseConstructorTest() throws Exception {
        final GeoEllipse gc = new GeoEllipse();
        el = new Ellipse(gc);
        gc.setTimeStamp(el.getTimeStamp());
        gc.setFillStyle(null);
        validateEllipse(el,
                gc,
                150.0,
                75.0,
                FeatureTypeEnum.GEO_ELLIPSE,
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
    public void nullGeoEllipseConstructor() {
        el = new Ellipse(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NegativeRadiusConstructor() {
        el = new Ellipse(-45.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroRadiusConstructor() {
        el = new Ellipse(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NaNRadiusConstructor() {
        el = new Ellipse(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallRadiusConstructor() {
        el = new Ellipse(0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSetRadius() {
        el = new Ellipse();
        el.setRadius(-10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSetRadius() {
        el = new Ellipse();
        el.setRadius(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NaNSetRadius() {
        el = new Ellipse();
        el.setRadius(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSetRadius() {
        el = new Ellipse();
        el.setRadius(0.5);
    }

    @Test
    public void setRadius() {
        el = new Ellipse();
        el.setRadius(10.0);
        assertEquals(el.getRadius(), 10.0, Epsilon);
    }

    @Test
    public void nullPositionFeatureBoundingBox() {
        el = new Ellipse();
        assertNull(el.getFeatureBoundingBox());
    }

    @Test
    public void featureBoundingBox() throws Exception {
        final GeoPosition gp = new GeoPosition();
        el = new Ellipse();
        el.setPosition(gp);
        final Double coord = 9.881468125740867*Math.pow(10,-4);
//
//        final EmpBoundingBox empBoundingBoxMock = PowerMockito.mock(EmpBoundingBox.class);
//        when(empBoundingBoxMock.deltaLatitude()).thenReturn(0.0);
//        when(empBoundingBoxMock.deltaLongitude()).thenReturn(0.0);
//        PowerMockito.whenNew(EmpBoundingBox.class).withNoArguments().thenReturn(empBoundingBoxMock);

        validateBoundingBox((EmpBoundingBox) el.getFeatureBoundingBox(),
                                             coord,
                                             coord,
                                             -1*coord,
                                             -1*coord);
    }
}