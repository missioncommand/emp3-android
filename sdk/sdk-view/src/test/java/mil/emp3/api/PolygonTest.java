package mil.emp3.api;

import android.graphics.Bitmap;
import android.util.Log;

import org.cmapi.primitives.GeoPolygon;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoRectangle;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Matt.Miller on 10/16/2017.
 */
public class PolygonTest extends TestBase{
    public static final String TAG = PolygonTest.class.getSimpleName();
    private Polygon p;
    private GeoPolygon gp;
    private ArrayList<IGeoPosition> positions;

    @org.junit.Before
    public void setUp() throws Exception {
        p = new Polygon();
        gp = new GeoPolygon();
        p = new Polygon(gp);
        positions = new ArrayList<IGeoPosition>();
        positions.add(new GeoPosition());
        p = new Polygon(positions);
    }

    @org.junit.Test
    public void invalidConstructors() throws Exception{
        positions = null;
        try {
            p = new Polygon(positions);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The position list parameter can NOT be null");
        }
        positions = new ArrayList<IGeoPosition>();
        positions.add(null);
        try {
            p = new Polygon(positions);
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
        p = new Polygon();
        p.setPatternFillImage(p.getPatternFillImage());
    }

    @org.junit.Test
    public void getPatternFillImage() throws Exception {
    }

    @org.junit.Test
    public void stringTest() throws Exception {
        p = new Polygon();
        Log.i(TAG, p.toString());
        p.setPosition(new GeoPosition());
        Log.i(TAG, p.toString());
    }

}