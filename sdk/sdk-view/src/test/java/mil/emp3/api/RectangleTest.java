package mil.emp3.api;

import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoRectangle;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoRectangle;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.utils.EmpBoundingBox;

import static mil.emp3.api.utils.ComparisonUtils.Epsilon;
import static mil.emp3.api.utils.ComparisonUtils.validateBoundingBox;
import static mil.emp3.api.utils.ComparisonUtils.validateRectangle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Matt.Miller on 10/16/2017.
 */
public class RectangleTest extends TestBase{
    public static final String TAG = RectangleTest.class.getSimpleName();
    private Rectangle r1;
    private GeoPosition gp;

    @Test
    public void defaultConstructor() {
        r1 = new Rectangle();
        final GeoRectangle gr = new GeoRectangle();
        gr.setFillStyle(null);
        gr.setTimeStamp(r1.getTimeStamp());
        validateRectangle(r1,
                          gr,
                          150.0,
                          75.0,
                          FeatureTypeEnum.GEO_RECTANGLE,
                          Collections.EMPTY_LIST,
                          Collections.EMPTY_LIST,
                          Collections.EMPTY_LIST,
                          Collections.EMPTY_LIST,
                          gr.getTimeStamp(),
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
    public void positionConstructor() {
        gp = new GeoPosition();
        r1 = new Rectangle(gp);
        final GeoRectangle gr = new GeoRectangle();
        gr.setFillStyle(null);
        gr.setPositions(Collections.singletonList(gp));
        gr.setTimeStamp(r1.getTimeStamp());
        validateRectangle(r1,
                          gr,
                          150.0,
                          75.0,
                          FeatureTypeEnum.GEO_RECTANGLE,
                          Collections.EMPTY_LIST,
                          Collections.EMPTY_LIST,
                          Collections.EMPTY_LIST,
                          Collections.singletonList(gp),
                          gr.getTimeStamp(),
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
                          null,
                          "",
                          new HashMap());
    }

    @Test
    public void positionWidthHeightConstructor() {
        final Double dimensions = 5.4;
        gp = new GeoPosition();
        r1 = new Rectangle(gp, dimensions, dimensions);
        final GeoRectangle gr = new GeoRectangle();
        gr.setFillStyle(null);
        gr.setPositions(Collections.singletonList(gp));
        gr.setHeight(dimensions);
        gr.setWidth(dimensions);
        gr.setTimeStamp(r1.getTimeStamp());
        validateRectangle(r1,
                          gr,
                          5.4,
                          5.4,
                          FeatureTypeEnum.GEO_RECTANGLE,
                          Collections.EMPTY_LIST,
                          Collections.EMPTY_LIST,
                          Collections.EMPTY_LIST,
                          Collections.singletonList(gp),
                          gr.getTimeStamp(),
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
                          null,
                          "",
                          new HashMap());
    }

    @Test
    public void copyConstructor() {
        r1 = new Rectangle();
        final Rectangle r2 = new Rectangle(r1);
        validateRectangle(r2,
                          (GeoRectangle) r1.getRenderable(),
                          150.0,
                          75.0,
                          r1.getFeatureType(),
                          r1.getChildFeatures(),
                          r1.getParentOverlays(),
                          r1.getParentFeatures(),
                          r1.getPositions(),
                          r1.getTimeStamp(),
                          r1.getTimeSpans(),
                          r1.getAltitudeMode(),
                          r1.getStrokeStyle(),
                          r1.getFillStyle(),
                          r1.getLabelStyle(),
                          r1.getExtrude(),
                          r1.getTessellate(),
                          r1.getBuffer(),
                          r1.getAzimuth(),
                          (GeoPosition) r1.getPosition(),
                          r1.getReadOnly(),
                          r1.getParents(),
                          r1.hasChildren(),
                          r1.getChildren(),
                          r1.getName(),
                          r1.getDataProviderId(),
                          r1.getDescription(),
                          r1.getProperties());
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidWidthConstructor() {
        r1 = new Rectangle(gp, .5, .5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidHeightConstructor() {
        r1 = new Rectangle(gp, 2, .5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoPositionConstructor() {
        r1 = new Rectangle((GeoPosition)null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoPositionWidthHeightConstructor() {
        r1 = new Rectangle(null, 5, 5);
    }

    @Test(expected = IllegalArgumentException.class)
    public void geoRectangleNaNWidthConstructor() {
        final IGeoRectangle gr = new GeoRectangle();
        gr.setWidth(Double.NaN);
        r1 = new Rectangle(gr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void geoRectangleInvalidWidthConstructor() {
        final IGeoRectangle gr = new GeoRectangle();
        gr.setWidth(.5);
        r1 = new Rectangle(gr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void geoRectangleNaNHeightConstructor() {
        final IGeoRectangle gr = new GeoRectangle();
        gr.setHeight(Double.NaN);
        r1 = new Rectangle(gr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void geoRectangleInvalidHeightConstructor() {
        final IGeoRectangle gr = new GeoRectangle();
        gr.setWidth(5);
        gr.setHeight(.5);
        r1 = new Rectangle(gr);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullGeoRectangleConstructor() {
        r1 = new Rectangle((GeoRectangle) null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthNaNTest() {
        r1 = new Rectangle();
        r1.setWidth(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthDecimalTest() {
        r1 = new Rectangle();
        r1.setWidth(.45);
    }

    @Test(expected = IllegalArgumentException.class)
    public void widthNegativeTest() {
        r1 = new Rectangle();
        r1.setWidth(-3.45);
    }

    @Test
    public void widthTest() {
        r1 = new Rectangle();
        r1.setWidth(10);
        assertEquals(r1.getWidth(), 10, Epsilon);
    }


    @Test(expected = IllegalArgumentException.class)
    public void heightNaNTest() {
        r1 = new Rectangle();
        r1.setHeight(Double.NaN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void heightDecimalTest() {
        r1 = new Rectangle();
        r1.setHeight(.45);
    }

    @Test(expected = IllegalArgumentException.class)
    public void heightNegativeTest() {
        r1 = new Rectangle();
        r1.setHeight(-3.45);
    }

    @Test
    public void heightTest() {
        r1 = new Rectangle();
        r1.setHeight(10);
        assertEquals(r1.getHeight(), 10, Epsilon);
    }

    @Test
    public void noPositionFeatureBoundingBox() {
        r1 = new Rectangle();
        assertNull(r1.getFeatureBoundingBox());
    }

    @Test
    public void zeroAzimuthFeatureBoundingBox() {
        gp = new GeoPosition();
        r1 = new Rectangle(gp);
        r1.setAzimuth(0.0);
        final Double eastWestCoord = 6.737364631135279*Math.pow(10,-4);
        final Double northSouthCoord = 3.3913855389755554*Math.pow(10,-4);
        validateBoundingBox((EmpBoundingBox) r1.getFeatureBoundingBox(),
                                             northSouthCoord,
                                             eastWestCoord,
                                             -1*northSouthCoord,
                                             -1*eastWestCoord);
    }
    @Test
    public void FeatureBoundingBox() {
        gp = new GeoPosition();
        r1 = new Rectangle(gp);
        r1.setAzimuth(10.0);
        final Double northSouthCoord = 4.5176786090905807*Math.pow(10,-4);
        final Double eastWestCoord =  7.219974468171131*Math.pow(10,-4);
        validateBoundingBox((EmpBoundingBox) r1.getFeatureBoundingBox(),
                                             northSouthCoord,
                                             eastWestCoord,
                                             -1*northSouthCoord,
                                             -1*eastWestCoord);
    }

    @Test
    public void defaultStringTest() {
        r1 = new Rectangle();
        assertEquals(r1.toString(),
                        "Rectangle\n" +
                        "\theight: 75.0\n" +
                        "\twidth: 150.0\n" +
                        "\tazimuth: 0.0\n");
    }

    @Test
    public void geoPositionStringTest() {
        gp = new GeoPosition();
        r1 = new Rectangle(gp);
        assertEquals(r1.toString(),
                        "Rectangle at\n" +
                        "\tlatitude: 0.0\n" +
                        "\tlongitude: 0.0\n" +
                        "\taltitude: 0.0\n" +
                        "\theight: 75.0\n" +
                        "\twidth: 150.0\n" +
                        "\tazimuth: 0.0\n");
    }
}