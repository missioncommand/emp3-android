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

    private Circle circ;

    @Test
    public void constructorTest() throws Exception {
        circ = new Circle();
        final GeoCircle gc = new GeoCircle();
        gc.setTimeStamp(circ.getTimeStamp());
        gc.setFillStyle(null);
        validateCircle(circ,
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
        circ = new Circle(50.0);
        final GeoCircle gc = new GeoCircle();
        gc.setRadius(50.0);
        gc.setTimeStamp(circ.getTimeStamp());
        gc.setFillStyle(null);
        validateCircle(circ,
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
        circ = new Circle(gc);
        gc.setTimeStamp(circ.getTimeStamp());
        gc.setFillStyle(null);
        validateCircle(circ,
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
        circ = new Circle(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NegativeRadiusConstructor() {
        circ = new Circle(-45.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroRadiusConstructor() {
        circ = new Circle(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NaNRadiusConstructor() {
        circ = new Circle(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallRadiusConstructor() {
        circ = new Circle(0.5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void negativeSetRadius() {
        circ = new Circle();
        circ.setRadius(-10.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void zeroSetRadius() {
        circ = new Circle();
        circ.setRadius(0.0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void NaNSetRadius() {
        circ = new Circle();
        circ.setRadius(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void tooSmallSetRadius() {
        circ = new Circle();
        circ.setRadius(0.5);
    }

    @Test
    public void setRadius() {
        circ = new Circle();
        circ.setRadius(10.0);
        assertEquals(circ.getRadius(), 10.0, Epsilon);
    }

    @Test
    public void nullPositionFeatureBoundingBox() {
        circ = new Circle();
        assertNull(circ.getFeatureBoundingBox());
    }

    @Test
    public void featureBoundingBox() throws Exception {
        final GeoPosition gp = new GeoPosition();
        circ = new Circle();
        circ.setPosition(gp);
        final Double northSouthCoord = 9.948064247282673*Math.pow(10,-4);
        final Double eastWestCoord = 9.881468124603998*Math.pow(10,-4);

        validateBoundingBox((EmpBoundingBox) circ.getFeatureBoundingBox(),
                                             northSouthCoord,
                                             eastWestCoord,
                                             -1*northSouthCoord,
                                             -1*eastWestCoord);
    }

    @Test
    public void noPosToString() {
        circ = new Circle();
        final String expectedString = "Circle\n" +
                                      "\tradius: 100.0\n" +
                                      "\tazimuth: 0.0\n";
        assertEquals(circ.toString(), expectedString);
    }

    @Test
    public void posToString() {
        final GeoPosition gp = new GeoPosition();
        circ = new Circle();
        circ.setPosition(gp);
        final String expectedString = "Circle at\n" +
                                      "\tlatitude: 0.0\n" +
                                      "\tlongitude: 0.0\n" +
                                      "\taltitude: 0.0\n" +
                                      "\tradius: 100.0\n" +
                                      "\tazimuth: 0.0\n";
        assertEquals(circ.toString(), expectedString);
    }
}