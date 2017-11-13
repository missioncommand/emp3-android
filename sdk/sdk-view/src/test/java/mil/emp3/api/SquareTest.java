package mil.emp3.api;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoSquare;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoSquare;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.utils.EmpBoundingBox;

import static mil.emp3.api.utils.ComparisonUtils.Epsilon;
import static mil.emp3.api.utils.ComparisonUtils.validateBoundingBox;
import static mil.emp3.api.utils.ComparisonUtils.validateSquare;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Matt.Miller on 10/16/2017.
 */
public class SquareTest extends TestBase {
    public static final String TAG = SquareTest.class.getSimpleName();
    private Square square1;
    private GeoPosition geoPos;

    @Test
    public void defaultConstructor() {
        square1 = new Square();
        final GeoSquare geoSquare = new GeoSquare();
        geoSquare.setFillStyle(null);
        geoSquare.setTimeStamp(square1.getTimeStamp());
        validateSquare(square1,
                       geoSquare,
                       100.0,
                       FeatureTypeEnum.GEO_SQUARE,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       geoSquare.getTimeStamp(),
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
    public void geoPosConstructor() {
        geoPos = new GeoPosition();
        final GeoSquare geoSquare = new GeoSquare();
        geoSquare.setPositions(Collections.singletonList(this.geoPos));
        geoSquare.setFillStyle(null);
        square1 = new Square(this.geoPos);
        geoSquare.setTimeStamp(square1.getTimeStamp());
        validateSquare(square1,
                       geoSquare,
                       100.0,
                       FeatureTypeEnum.GEO_SQUARE,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       Collections.singletonList(geoPos),
                       geoSquare.getTimeStamp(),
                       Collections.EMPTY_LIST,
                       null,
                       new GeoStrokeStyle(),
                       null,
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
                       null,
                       "",
                       new HashMap());
    }

    @Test
    public void geoPosWidthConstructor() {
        geoPos = new GeoPosition();
        square1 = new Square(geoPos, 5.4);
        final GeoSquare geoSquare = new GeoSquare();
        geoSquare.setPositions(Collections.singletonList(geoPos));
        geoSquare.setWidth(5.4);
        geoSquare.setFillStyle(null);
        geoSquare.setTimeStamp(square1.getTimeStamp());
        validateSquare(square1,
                       geoSquare,
                       5.4,
                       FeatureTypeEnum.GEO_SQUARE,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       Collections.singletonList(geoPos),
                       geoSquare.getTimeStamp(),
                       Collections.EMPTY_LIST,
                       null,
                       new GeoStrokeStyle(),
                       null,
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
                       null,
                       "",
                       new HashMap());
    }

    @Test
    public void copyConstructor() {
        square1 = new Square();
        final Square square2 = new Square(square1);
        validateSquare(square2,
                       (GeoSquare) square1.getRenderable(),
                       100.0,
                       square1.getFeatureType(),
                       square1.getChildFeatures(),
                       square1.getParentOverlays(),
                       square1.getParentFeatures(),
                       square1.getPositions(),
                       square1.getTimeStamp(),
                       square1.getTimeSpans(),
                       square1.getAltitudeMode(),
                       square1.getStrokeStyle(),
                       square1.getFillStyle(),
                       square1.getLabelStyle(),
                       square1.getExtrude(),
                       square1.getTessellate(),
                       square1.getBuffer(),
                       square1.getAzimuth(),
                       (GeoPosition) square1.getPosition(),
                       square1.getReadOnly(),
                       square1.getParents(),
                       square1.hasChildren(),
                       square1.getChildren(),
                       square1.getName(),
                       square1.getDataProviderId(),
                       square1.getDescription(),
                       square1.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidWidthConstructor() {
        square1 = new Square(new GeoPosition(), .5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoPositionConstructor() {
        square1 = new Square((GeoPosition) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoSquareConstructor() {
        square1 = new Square((GeoSquare) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoPositionWidthHeightConstructor() {
        square1 = new Square(null, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void geoSquareNaNWidthConstructor() {
        final IGeoSquare gs = new GeoSquare();
        gs.setWidth(Double.NaN);
        square1 = new Square(gs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void geoSquareInvalidWidthConstructor() {
        final IGeoSquare gs = new GeoSquare();
        gs.setWidth(.5);
        square1 = new Square(gs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthNaNTest() {
        square1 = new Square();
        square1.setWidth(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthDecimalTest() {
        square1 = new Square();
        square1.setWidth(.45);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthNegativeTest() {
        square1 = new Square();
        square1.setWidth(-3.45);
    }

    @Test
    public void widthTest() {
        square1 = new Square();
        square1.setWidth(10);
        assertEquals(square1.getWidth(), 10, Epsilon);
    }

    @Test
    public void noPositionFeatureBoundingBox() {
        square1 = new Square();
        assertNull(square1.getFeatureBoundingBox());
    }

    @Test
    public void zeroAzimuthFeatureBoundingBox() {
        geoPos = new GeoPosition();
        square1 = new Square(geoPos);
        square1.setAzimuth(0.0);
        final Double eastWestCoord = 4.4915764203778963 * Math.pow(10, -4);
        final Double northSouthCoord = 4.5218473849217844 * Math.pow(10, -4);
        validateBoundingBox((EmpBoundingBox) square1.getFeatureBoundingBox(),
                            northSouthCoord,
                            eastWestCoord,
                            -1 * northSouthCoord,
                            -1 * eastWestCoord);
    }

    @Test
    public void FeatureBoundingBox() {
        geoPos = new GeoPosition();
        square1 = new Square(geoPos);
        square1.setAzimuth(10.0);
        final Double eastWestCoord = 5.203293342219695 * Math.pow(10, -4);
        final Double northSouthCoord = 5.238360920998275 * Math.pow(10, -4);
        validateBoundingBox((EmpBoundingBox) square1.getFeatureBoundingBox(),
                            northSouthCoord,
                            eastWestCoord,
                            -1 * northSouthCoord,
                            -1 * eastWestCoord);
    }

    @Test
    public void defaultStringTest() {
        square1 = new Square();
        assertEquals(square1.toString(),
                     "Square\n" +
                             "\twidth: 100.0\n" +
                             "\tazimuth: 0.0\n");
    }

    @Test
    public void geoPositionStringTest() {
        geoPos = new GeoPosition();
        square1 = new Square(geoPos);
        assertEquals(square1.toString(),
                     "Square at\n" +
                             "\tlatitude: 0.0\n" +
                             "\tlongitude: 0.0\n" +
                             "\taltitude: 0.0\n" +
                             "\twidth: 100.0\n" +
                             "\tazimuth: 0.0\n");
    }
}