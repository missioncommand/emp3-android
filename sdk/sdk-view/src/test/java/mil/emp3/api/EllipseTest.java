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

import static mil.emp3.api.utils.ComparisonUtils.EPSILON8;
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

    @Test
    public void radiusConstructorTest() throws Exception {
        el = new Ellipse(50.0, 50.0);
        final GeoEllipse gc = new GeoEllipse();
        gc.setSemiMajor(50.0);
        gc.setSemiMinor(50.0);
        gc.setTimeStamp(el.getTimeStamp());
        gc.setFillStyle(null);
        validateEllipse(el,
                        gc,
                        50.0,
                        50.0,
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
    public void radiusAzimuthConstructorTest() throws Exception {
        el = new Ellipse(50.0, 50.0, 100.0);
        final GeoEllipse gc = new GeoEllipse();
        gc.setSemiMajor(50.0);
        gc.setSemiMinor(50.0);
        gc.setAzimuth(100.0);
        gc.setTimeStamp(el.getTimeStamp());
        gc.setFillStyle(null);
        validateEllipse(el,
                        gc,
                        50.0,
                        50.0,
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
                        100.0,
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
    public void negativeSemiMajorGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setSemiMajor(-10.0);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSemiMajorGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setSemiMajor(0.0);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNSemiMajorGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setSemiMajor(Double.NaN);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSemiMajorGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setSemiMajor(0.5);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSemiMinorGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setSemiMinor(-10.0);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSemiMinorGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setSemiMinor(0.0);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNSemiMinorGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setSemiMinor(Double.NaN);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSemiMinorGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setSemiMinor(0.5);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeAzimuthMajorGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setAzimuth(-400.0);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNAzimuthGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setAzimuth(Double.NaN);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooLargeAzimuthGeoEllipseConstructor() {
        final GeoEllipse ge = new GeoEllipse();
        ge.setAzimuth(400.0);
        el = new Ellipse(ge);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallAzimuthAzimuthConstructor() {
        el = new Ellipse(100.0, 100.0, -400.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooLargeAzimuthAzimuthConstructor() {
        el = new Ellipse(100.0, 100.0, 400.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNAzimuthAzimuthConstructor() {
        el = new Ellipse(100.0, 100.0, Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSemiMajorAzimuthConstructor() {
        el = new Ellipse(0.5, 100.0, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSemiMajorAzimuthConstructor() {
        el = new Ellipse(-5.0, 100.0, -400.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSemiMajorParamterConstructor() {
        el = new Ellipse(0.0, 100.0, -400.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNSemiMajorAzimuthConstructor() {
        el = new Ellipse(Double.NaN, 100.0, -400.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSemiMinorAzimuthConstructor() {
        el = new Ellipse(100.0, 0.5, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSemiMinorAzimuthConstructor() {
        el = new Ellipse(100.0, -4.0, -400.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSemiMinorAzimuthConstructor() {
        el = new Ellipse(100.0, 0.0, -400.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNSemiMinorAzimuthConstructor() {
        el = new Ellipse(100.0, Double.NaN, -400.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSemiMajorParameterConstructor() {
        el = new Ellipse(0.5, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSemiMajorParameterConstructor() {
        el = new Ellipse(-5.0, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSemiMajorParameterConstructor() {
        el = new Ellipse(0.0, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNSemiMajorParameterConstructor() {
        el = new Ellipse(Double.NaN, 100.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSemiMinorParameterConstructor() {
        el = new Ellipse(100.0, 0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSemiMinorParameterConstructor() {
        el = new Ellipse(100.0, -4.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSemiMinorParameterConstructor() {
        el = new Ellipse(100.0, 0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNSemiMinorParameterConstructor() {
        el = new Ellipse(100.0, Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSetSemiMajor() {
        el = new Ellipse();
        el.setSemiMajor(-10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSetSemiMajor() {
        el = new Ellipse();
        el.setSemiMajor(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNSetSemiMajor() {
        el = new Ellipse();
        el.setSemiMajor(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSetSemiMajor() {
        el = new Ellipse();
        el.setSemiMajor(0.5);
    }

    @Test
    public void setSemiMajor() {
        el = new Ellipse();
        el.setSemiMajor(10.0);
        assertEquals(el.getSemiMajor(), 10.0, EPSILON8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSetSemiMinor() {
        el = new Ellipse();
        el.setSemiMinor(-10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSetSemiMinor() {
        el = new Ellipse();
        el.setSemiMinor(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void naNSetSemiMinor() {
        el = new Ellipse();
        el.setSemiMinor(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSetSemiMinor() {
        el = new Ellipse();
        el.setSemiMinor(0.5);
    }

    @Test
    public void setSemiMinor() {
        el = new Ellipse();
        el.setSemiMinor(10.0);
        assertEquals(el.getSemiMinor(), 10.0, EPSILON8);
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
        final Double northSouthCoord = 7.461048185746222 * Math.pow(10, -4);
        final Double eastWestCoord = 1.4822202186905997 * Math.pow(10, -3);

        validateBoundingBox((EmpBoundingBox) el.getFeatureBoundingBox(),
                            northSouthCoord,
                            eastWestCoord,
                            -1 * northSouthCoord,
                            -1 * eastWestCoord);
    }


    @Test
    public void noPosToString() {
        el = new Ellipse();
        final String expectedString = "Ellipse \n" +
                                      "\tsemi-major: 150.0\n" +
                                      "\tsemi-minor: 75.0\n" +
                                      "\tazimuth: 0.0\n";
        assertEquals(el.toString(), expectedString);
    }

    @Test
    public void posToString() {
        final GeoPosition gp = new GeoPosition();
        el = new Ellipse();
        el.setPosition(gp);
        final String expectedString = "Ellipse at\n" +
                                      "\tlatitude: 0.0\n" +
                                      "\tlongitude: 0.0\n" +
                                      "\taltitude: 0.0\n" +
                                      "\tsemi-major: 150.0\n" +
                                      "\tsemi-minor: 75.0\n" +
                                      "\tazimuth: 0.0\n";
        assertEquals(el.toString(), expectedString);
    }
}