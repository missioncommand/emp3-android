package mil.emp3.api;

import android.graphics.Bitmap;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoPosition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import mil.emp3.api.enums.FeatureTypeEnum;

import static mil.emp3.api.utils.ComparisonUtils.validatePolygon;
import static org.junit.Assert.assertEquals;

/**
 * Created by Matt.Miller on 10/16/2017.
 */

public class PolygonTest extends TestBase {
    public static final String TAG = PolygonTest.class.getSimpleName();
    private Polygon poly;
    private GeoPolygon gp;
    private ArrayList<IGeoPosition> positions;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void defaultConstructor() throws Exception {
        poly = new Polygon();

        final GeoPolygon gp = new GeoPolygon();
        gp.setTimeStamp(poly.getTimeStamp());
        validatePolygon(poly,
                        null,
                        gp,
                        FeatureTypeEnum.GEO_POLYGON,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        poly.getTimeStamp(),
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
    public void geoPolygonConstructor() {
        gp = new GeoPolygon();
        poly = new Polygon(gp);
        validatePolygon(poly,
                        null,
                        gp,
                        FeatureTypeEnum.GEO_POLYGON,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        poly.getTimeStamp(),
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
    public void positionsConstructor() {
        final GeoPosition geoPosition = new GeoPosition();
        geoPosition.setLatitude(50);
        geoPosition.setLongitude(50);

        positions = new ArrayList<IGeoPosition>();
        positions.add(geoPosition);
        poly = new Polygon(positions);

        gp = new GeoPolygon();
        gp.setPositions(Collections.singletonList(geoPosition));
        gp.setTimeStamp(poly.getTimeStamp());
        validatePolygon(poly,
                        null,
                        gp,
                        FeatureTypeEnum.GEO_POLYGON,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        positions,
                        poly.getTimeStamp(),
                        Collections.EMPTY_LIST,
                        null,
                        new GeoStrokeStyle(),
                        new GeoFillStyle(),
                        new GeoLabelStyle(),
                        false,
                        true,
                        0.0,
                        0.0,
                        geoPosition,
                        false,
                        Collections.EMPTY_LIST,
                        false,
                        Collections.EMPTY_LIST,
                        "",
                        "",
                        "",
                        new HashMap());
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPositionsConstructor() {
        positions = null;
        poly = new Polygon(positions);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullInPositionsArrayConstructor() {
        positions = new ArrayList();
        positions.add(null);
        poly = new Polygon(positions);
    }

    @Test
    public void testPatternFillImage() throws Exception {
        final Bitmap empIcon = Mockito.mock(Bitmap.class);
        poly = new Polygon();
        poly.setPatternFillImage(empIcon);
        assertEquals(poly.getPatternFillImage(), empIcon);
    }


    @Test
    public void stringTest() throws Exception {
        final String noPointsExpectedResult = "Polygon with zero points";
        final String onePointExpectedResult = "Polygon with 1 points\n" +
                                              "\tlatitude: 0.0, " +
                                              "\tlongitude: 0.0, " +
                                              "\taltitude: 0.0\n";
        poly = new Polygon();
        assertEquals(poly.toString(), noPointsExpectedResult);
        poly.setPosition(new GeoPosition());
        assertEquals(poly.toString(), onePointExpectedResult);
    }
}