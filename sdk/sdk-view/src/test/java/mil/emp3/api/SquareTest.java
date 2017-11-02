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
public class SquareTest extends TestBase{
    public static final String TAG = SquareTest.class.getSimpleName();
    private Square s1;
    private GeoPosition gp;

    @Test
    public void defaultConstructor() {
        s1 = new Square();
        final GeoSquare gs = new GeoSquare();
        gs.setFillStyle(null);
        gs.setTimeStamp(s1.getTimeStamp());
        validateSquare(s1,
                       gs,
                       100.0,
                       FeatureTypeEnum.GEO_SQUARE,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       Collections.EMPTY_LIST,
                       gs.getTimeStamp(),
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
                       "",
                       "",
                       new HashMap());
    }

    @Test
    public void geoPosConstructor() {
        final GeoPosition gp = new GeoPosition();
        final GeoSquare gs = new GeoSquare();
        gs.setPositions(Collections.singletonList(gp));
        gs.setFillStyle(null);
        s1 = new Square(gp);
        gs.setTimeStamp(s1.getTimeStamp());
        validateSquare(s1,
                gs,
                100.0,
                FeatureTypeEnum.GEO_SQUARE,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.singletonList(gp),
                gs.getTimeStamp(),
                Collections.EMPTY_LIST,
                null,
                new GeoStrokeStyle(),
                null,
                new GeoLabelStyle(),
                false,
                true,
                0.0,
                0.0,
                gp,
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
    public void geoPosWidthConstructor() {
        final GeoPosition gp = new GeoPosition();
        s1 = new Square(gp, 5.4);
        final GeoSquare gs = new GeoSquare();
        gs.setPositions(Collections.singletonList(gp));
        gs.setWidth(5.4);
        gs.setFillStyle(null);
        gs.setTimeStamp(s1.getTimeStamp());
        validateSquare(s1,
                gs,
                5.4,
                FeatureTypeEnum.GEO_SQUARE,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.EMPTY_LIST,
                Collections.singletonList(gp),
                gs.getTimeStamp(),
                Collections.EMPTY_LIST,
                null,
                new GeoStrokeStyle(),
                null,
                new GeoLabelStyle(),
                false,
                true,
                0.0,
                0.0,
                gp,
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
    public void copyConstructor() {
        s1 = new Square();
        final Square s2 = new Square(s1);
        validateSquare(s2,
                (GeoSquare) s1.getRenderable(),
                100.0,
                s1.getFeatureType(),
                s1.getChildFeatures(),
                s1.getParentOverlays(),
                s1.getParentFeatures(),
                s1.getPositions(),
                s1.getTimeStamp(),
                s1.getTimeSpans(),
                s1.getAltitudeMode(),
                s1.getStrokeStyle(),
                s1.getFillStyle(),
                s1.getLabelStyle(),
                s1.getExtrude(),
                s1.getTessellate(),
                s1.getBuffer(),
                s1.getAzimuth(),
                (GeoPosition) s1.getPosition(),
                s1.getReadOnly(),
                s1.getParents(),
                s1.hasChildren(),
                s1.getChildren(),
                s1.getName(),
                s1.getDataProviderId(),
                s1.getDescription(),
                s1.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidWidthConstructor() {
        s1 = new Square(new GeoPosition(), .5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoPositionConstructor() {
        s1 = new Square((GeoPosition)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoSquareConstructor() {
        s1 = new Square((GeoSquare)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoPositionWidthHeightConstructor() {
        s1 = new Square(null, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void geoSquareNaNWidthConstructor() {
        final IGeoSquare gs = new GeoSquare();
        gs.setWidth(Double.NaN);
        s1 = new Square(gs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void geoSquareInvalidWidthConstructor() {
        final IGeoSquare gs = new GeoSquare();
        gs.setWidth(.5);
        s1 = new Square(gs);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthNaNTest() {
        s1 = new Square();
        s1.setWidth(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthDecimalTest() {
        s1 = new Square();
        s1.setWidth(.45);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthNegativeTest() {
        s1 = new Square();
        s1.setWidth(-3.45);
    }

    @Test
    public void widthTest() {
        s1 = new Square();
        s1.setWidth(10);
        assertEquals(s1.getWidth(), 10, Epsilon);
    }


    @Test
    public void noPositionFeatureBoundingBox() {
        s1 = new Square();
        assertNull(s1.getFeatureBoundingBox());
    }

    @Test
    public void zeroAzimuthFeatureBoundingBox() {
        gp = new GeoPosition();
        s1 = new Square(gp);
        s1.setAzimuth(0.0);
        final Double eastWestCoord = 4.4915764203778963*Math.pow(10,-4);
        final Double northSouthCoord = 4.5218473849217844*Math.pow(10,-4);
        validateBoundingBox((EmpBoundingBox) s1.getFeatureBoundingBox(),
                northSouthCoord,
                eastWestCoord,
                -1*northSouthCoord,
                -1*eastWestCoord);
    }
    @Test
    public void FeatureBoundingBox() {
        gp = new GeoPosition();
        s1 = new Square(gp);
        s1.setAzimuth(10.0);
        final Double eastWestCoord = 5.203293342219695*Math.pow(10,-4);
        final Double northSouthCoord = 5.238360920998275*Math.pow(10,-4);
        validateBoundingBox((EmpBoundingBox) s1.getFeatureBoundingBox(),
                northSouthCoord,
                eastWestCoord,
                -1*northSouthCoord,
                -1*eastWestCoord);
    }

    @Test
    public void defaultStringTest() {
        s1 = new Square();
        assertEquals(s1.toString(),
                "Square\n" +
                        "\twidth: 100.0\n" +
                        "\tazimuth: 0.0\n");
    }

    @Test
    public void geoPositionStringTest() {
        gp = new GeoPosition();
        s1 = new Square(gp);
        assertEquals(s1.toString(),
                "Square at\n" +
                        "\tlatitude: 0.0\n" +
                        "\tlongitude: 0.0\n" +
                        "\taltitude: 0.0\n" +
                        "\twidth: 100.0\n" +
                        "\tazimuth: 0.0\n");
    }
}