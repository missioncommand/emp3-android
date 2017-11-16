package mil.emp3.api;

import junit.framework.Assert;

import org.cmapi.primitives.GeoBounds;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoBounds;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/**
 * NOTE/CAUTION: These tests were written by simply taking the results and putting them back in the test. This was done after
 * results were verified visually on a display. Value of these tests is when some calculation is changed we can make sure that
 * what worked prior to the change continues to work.
 *
 */
@RunWith(RobolectricTestRunner.class)
public class SetExtentTest extends TestBaseSingleMap {
    private static String TAG = SetExtentTest.class.getSimpleName();

    @Before
    public void setUp() throws Exception {
        setupSingleMap(TAG);
    }

    @After
    public void tearDown() throws Exception {
        mapInstance.cleanQueues();
    }

    private IGeoBounds buildGeoBounds(double south, double north, double west, double east) {
        IGeoBounds bounds = new GeoBounds();
        bounds.setSouth(south);
        bounds.setNorth(north);
        bounds.setWest(west);
        bounds.setEast(east);
        return bounds;
    }

    @Test
    public void general_Test() throws Exception {
        mapInstance.cleanQueues();
        IGeoBounds bounds = buildGeoBounds(40.0, 41.0, -70.0, -71.0);
        remoteMap.setBounds(bounds, false);
        Assert.assertTrue(mapInstance.validateCamera(185988.70858009756, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 40.50107741772915, -70.5, 0.0, 0.0, 0.0));
    }

    /**
     * When near the pole and trying to include the pole in the extent current logic doesn't work properly. This needs to be re-visited.
     * @throws Exception
     */
    @Test
    public void setExtent_pole() throws Exception {
        mapInstance.cleanQueues();
        IGeoBounds bounds = buildGeoBounds(89.0, 89.0, -1.0, -1.0);
        remoteMap.setBounds(bounds, false);
        Assert.assertTrue(mapInstance.validateCamera(51.53575696206052, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 89.00000152277744, -1.0000000000000002, 0.0, 0.0, 0.0));
    }
}
