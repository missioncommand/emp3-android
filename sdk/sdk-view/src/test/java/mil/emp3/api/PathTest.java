package mil.emp3.api;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.GeoRenderable;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;

import static org.junit.Assert.*;

/**
 * Created by Matt.Miller on 10/17/2017.
 */
public class PathTest extends TestBase {
    public static final String TAG = PathTest.class.getSimpleName();

    @org.junit.Test
    public void pathTest() throws Exception {
        Path p = new Path();
        p = new Path(new GeoRenderable());
        ArrayList<IGeoPosition> positions = null;
        try {
            p = new Path(positions);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "The coordinate parameter can NOT be null");
        }
        positions = new ArrayList<IGeoPosition>();
        try {
            positions.add(null);
            p = new Path(positions);
            fail();
        } catch (final IllegalArgumentException e) {
            assertEquals(e.getMessage(), "GeoPosition is invalid");
        }
        positions.remove(0);
        positions.add(new GeoPosition());
        p = new Path(positions);
        Log.i(TAG, p.toString());
        p = new Path();
        Log.i(TAG, p.toString());
    }


}