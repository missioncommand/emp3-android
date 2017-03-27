package mil.emp3.api;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;

import mil.emp3.api.interfaces.IEmpBoundingBox;
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
        IEmpBoundingBox bbox = new EmpBoundingBox(20, -20, -100, -110);

        Assert.assertTrue("Delta Latitude Failed.", (bbox.deltaLatitude() == 40.0));
        Assert.assertTrue("Delta Longitude Failed.", (bbox.deltaLongitude() == 10.0));
        Assert.assertTrue("north Failed.", (bbox.north() == 20.0));
        Assert.assertTrue("south Failed.", (bbox.south() == -20.0));
        Assert.assertTrue("east Failed.", (bbox.east() == -100.0));
        Assert.assertTrue("west Failed.", (bbox.west() == -110.0));
        Assert.assertTrue("containsIDL Failed.", !bbox.containsIDL());

        IEmpBoundingBox bbox2 = new EmpBoundingBox(20, -20, -170, 170);
        Assert.assertTrue("containsIDL Failed.", bbox2.containsIDL());
        Assert.assertTrue("Delta Longitude with IDL Failed.", (bbox2.deltaLongitude() == 20.0));

        IEmpBoundingBox result = bbox2.intersection(bbox);
        Assert.assertTrue("EmpBoundingBox intersection when they don't intersect.", result == null);

        bbox.setNorth(40);
        bbox.setSouth(-40);
        bbox.setWest(175);
        bbox.setEast(-180);
        Assert.assertTrue("setEast failed to change -180 to 180.", bbox.getEast() == 180);

        Assert.assertTrue("EmpBoundingBox intersects", bbox2.intersects(bbox));

        result = bbox2.intersection(bbox);
        Assert.assertTrue("EmpBoundingBox intersection test 1 failed when they do intersect.", result != null);

        Assert.assertTrue("EmpBoundingBox intersection test 1 failed north setting.", result.getNorth() == 20);
        Assert.assertTrue("EmpBoundingBox intersection test 1 failed south setting.", result.getSouth() == -20);
        Assert.assertTrue("EmpBoundingBox intersection test 1 failed west setting.", result.getWest() == 175);
        Assert.assertTrue("EmpBoundingBox intersection test 1 failed east setting.", result.getEast() == 180);

        result = bbox.intersection(bbox2);
        Assert.assertTrue("EmpBoundingBox intersection test 2 failed when they do intersect.", result != null);

        Assert.assertTrue("EmpBoundingBox intersection test 2 failed north setting.", result.getNorth() == 20);
        Assert.assertTrue("EmpBoundingBox intersection test 2 failed south setting.", result.getSouth() == -20);
        Assert.assertTrue("EmpBoundingBox intersection test 2 failed west setting.", result.getWest() == 175);
        Assert.assertTrue("EmpBoundingBox intersection test 2 failed east setting.", result.getEast() == 180);

        bbox.setEast(-175);

        result = bbox2.intersection(bbox);
        Assert.assertTrue("EmpBoundingBox intersection test 3 failed when they do intersect.", result != null);

        Assert.assertTrue("EmpBoundingBox intersection test 3 failed north setting.", result.getNorth() == 20);
        Assert.assertTrue("EmpBoundingBox intersection test 3 failed south setting.", result.getSouth() == -20);
        Assert.assertTrue("EmpBoundingBox intersection test 3 failed west setting.", result.getWest() == 175);
        Assert.assertTrue("EmpBoundingBox intersection test 3 failed east setting.", result.getEast() == -175);

        result = bbox.intersection(bbox2);
        Assert.assertTrue("EmpBoundingBox intersection test 4 failed when they do intersect.", result != null);

        Assert.assertTrue("EmpBoundingBox intersection test 4 failed north setting.", result.getNorth() == 20);
        Assert.assertTrue("EmpBoundingBox intersection test 4 failed south setting.", result.getSouth() == -20);
        Assert.assertTrue("EmpBoundingBox intersection test 4 failed west setting.", result.getWest() == 175);
        Assert.assertTrue("EmpBoundingBox intersection test 4 failed east setting.", result.getEast() == -175);

        bbox.setNorth(20);
        bbox.setSouth(-20);
        bbox.setWest(170);
        bbox.setEast(-170);

        bbox2.setNorth(40);
        bbox2.setSouth(-40);
        bbox2.setWest(165);
        bbox2.setEast(175);

        result = bbox2.intersection(bbox);
        Assert.assertTrue("EmpBoundingBox intersection test 5 failed when they do intersect.", result != null);

        Assert.assertTrue("EmpBoundingBox intersection test 5 failed north setting.", result.getNorth() == 20);
        Assert.assertTrue("EmpBoundingBox intersection test 5 failed south setting.", result.getSouth() == -20);
        Assert.assertTrue("EmpBoundingBox intersection test 5 failed west setting.", result.getWest() == 170);
        Assert.assertTrue("EmpBoundingBox intersection test 5 failed east setting.", result.getEast() == 175);

        result = bbox.intersection(bbox2);
        Assert.assertTrue("EmpBoundingBox intersection test 6 failed when they do intersect.", result != null);

        Assert.assertTrue("EmpBoundingBox intersection test 6 failed north setting.", result.getNorth() == 20);
        Assert.assertTrue("EmpBoundingBox intersection test 6 failed south setting.", result.getSouth() == -20);
        Assert.assertTrue("EmpBoundingBox intersection test 6 failed west setting.", result.getWest() == 170);
        Assert.assertTrue("EmpBoundingBox intersection test 6 failed east setting.", result.getEast() == 175);

        bbox2.setNorth(40);
        bbox2.setSouth(-40);
        bbox2.setWest(-175);
        bbox2.setEast(165);

        result = bbox2.intersection(bbox);
        Assert.assertTrue("EmpBoundingBox intersection test 7 failed when they do intersect.", result != null);

        Assert.assertTrue("EmpBoundingBox intersection test 7 failed north setting.", result.getNorth() == 20);
        Assert.assertTrue("EmpBoundingBox intersection test 7 failed south setting.", result.getSouth() == -20);
        Assert.assertTrue("EmpBoundingBox intersection test 7 failed west setting.", result.getWest() == -175);
        Assert.assertTrue("EmpBoundingBox intersection test 7 failed east setting.", result.getEast() == -170);

        result = bbox.intersection(bbox2);
        Assert.assertTrue("EmpBoundingBox intersection test 8 failed when they do intersect.", result != null);

        Assert.assertTrue("EmpBoundingBox intersection test 8 failed north setting.", result.getNorth() == 20);
        Assert.assertTrue("EmpBoundingBox intersection test 8 failed south setting.", result.getSouth() == -20);
        Assert.assertTrue("EmpBoundingBox intersection test 8 failed west setting.", result.getWest() == -175);
        Assert.assertTrue("EmpBoundingBox intersection test 8 failed east setting.", result.getEast() == -170);
    }
}
