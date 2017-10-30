package mil.emp3.api;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoLabelStyle;
import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoPosition;
import org.junit.Test;

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

    @Test
    public void defaultConstructor() throws Exception {
        poly = new Polygon();
        validatePolygon(poly,
                        null,
                        new GeoPolygon(),
                        FeatureTypeEnum.GEO_POLYGON,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        null,
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
                        null,
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
                        null,
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
                        null,
                        "",
                        new HashMap());
    }

    @Test
    public void positionsConstructor() {
        final GeoPosition geoPosition = new GeoPosition();
        geoPosition.setLatitude(50);
        geoPosition.setLongitude(50);
        gp = new GeoPolygon();
        gp.setPositions(Collections.singletonList(geoPosition));

        positions = new ArrayList<IGeoPosition>();
        positions.add(geoPosition);
        poly = new Polygon(positions);
        validatePolygon(poly,
                        null,
                        gp,
                        FeatureTypeEnum.GEO_POLYGON,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        Collections.EMPTY_LIST,
                        positions,
                        null,
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
                        null,
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

    //this test doesn't really test anything since the image file is always null
    //TODO: Incorporate an actual image file
    //TODO: Add validation of image files
    @org.junit.Test
    public void setPatternFillImage() throws Exception {
        poly = new Polygon();
        poly.setPatternFillImage(poly.getPatternFillImage());
    }


    @org.junit.Test
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