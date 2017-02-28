package mil.emp3.api;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;

import mil.emp3.api.utils.EmpBoundingBox;

/**
 * Emp Bounding box class has changed as a result of recent modification getBounds capability. For now I am
 * disabling all the tests in this file.
 */
public class EmpBoundingBoxTest {
    private static String TAG = EmpBoundingBoxTest.class.getSimpleName();

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void constructorTest() throws InterruptedException {
        EmpBoundingBox bbox = null;

        try {
            bbox = new EmpBoundingBox(Double.NaN, Double.NaN, Double.NaN, Double.NaN);
            Assert.assertTrue("Constructor didn't generate an exception with NaN values.", false);
        } catch (InvalidParameterException ex) {
            Assert.assertTrue(true);
        }

        try {
            bbox = new EmpBoundingBox(null, null);
            Assert.assertTrue("Constructor didn't generate an exception with null coordinates.", false);
        } catch (InvalidParameterException ex) {
            Assert.assertTrue(true);
        }

        try {
            bbox = new EmpBoundingBox(20, -20, -100, -110);
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.assertTrue("Constructor generate an exception with valid n/s/e/w values.", true);
        }

        try {
            IGeoPosition northEast = new GeoPosition();
            IGeoPosition southWest = new GeoPosition();

            northEast.setLatitude(20);
            northEast.setLongitude(-100);
            southWest.setLatitude(-20);
            southWest.setLongitude(-110);
            bbox = new EmpBoundingBox(southWest, northEast);
            Assert.assertTrue(true);
        } catch (Exception ex) {
            Assert.assertTrue("Constructor generate an exception with valid northEast/southWest values.", true);
        }
    }

    @Test
    public void methodsTest() throws InterruptedException {
        EmpBoundingBox bbox = new EmpBoundingBox(20, -20, -100, -110);

        Assert.assertTrue("Delta Latitude Failed.", (bbox.deltaLatitude() == 40.0));
        Assert.assertTrue("Delta Longitude Failed.", (bbox.deltaLongitude() == 10.0));
        Assert.assertTrue("north Failed.", (bbox.north() == 20.0));
        Assert.assertTrue("south Failed.", (bbox.south() == -20.0));
        Assert.assertTrue("east Failed.", (bbox.east() == -100.0));
        Assert.assertTrue("west Failed.", (bbox.west() == -110.0));
        Assert.assertTrue("containsIDL Failed.", !bbox.containsIDL());

        bbox = new EmpBoundingBox(20, -20, -170, 170);
        Assert.assertTrue("containsIDL Failed.", bbox.containsIDL());
        Assert.assertTrue("Delta Longitude with IDL Failed.", (bbox.deltaLongitude() == 20.0));
    }
}
