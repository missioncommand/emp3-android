package mil.emp3.api;

import android.util.Log;

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
import java.util.UUID;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.utils.ComparisonUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

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
        ComparisonUtil.validatePolygon(poly,
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
                new UUID(0,0),
                null,
                "",
                new HashMap());
    }

    @Test
    public void geoPolygonConstructor() {
        gp = new GeoPolygon();
        poly = new Polygon(gp);
    }

    @Test
    public void positionsConstructor() {
        positions = new ArrayList<IGeoPosition>();
        positions.add(new GeoPosition());
        poly = new Polygon(positions);
    }

    @org.junit.Test
    public void invalidConstructors() throws Exception{
        positions = null;
        try {
            poly = new Polygon(positions);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The position list parameter can NOT be null");
        }
        positions = new ArrayList<IGeoPosition>();
        positions.add(null);
        try {
            poly = new Polygon(positions);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "GeoPosition is invalid");
        }
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
        poly = new Polygon();
        Log.i(TAG, poly.toString());
        poly.setPosition(new GeoPosition());
        Log.i(TAG, poly.toString());
    }

}