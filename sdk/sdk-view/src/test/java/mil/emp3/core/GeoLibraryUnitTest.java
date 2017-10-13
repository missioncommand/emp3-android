package mil.emp3.core;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.utils.GeographicLib;

/**
 * This class tests the Geo Library method.
 */
public class GeoLibraryUnitTest {
    private static final String TAG = GeoLibraryUnitTest.class.getSimpleName();
    private static final double NAUTICAL_MILE_IN_METERS = 1855.325;
    private static final double METERS_1_DEGREE = NAUTICAL_MILE_IN_METERS * 60;
    private static final double DISTANCE_ERROR_PER_DEGREE = 200.0; // meters
    private static final double BEARING_DELTA = 0.3;// arbitrary

    @Before
    public void setUp() throws Exception {

    }

    @After
    public void cleanUp() throws Exception {

    }

    @Test
    public void computeDistanceTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting computeDistanceTest.");
        IGeoPosition oPos1 = new GeoPosition();
        IGeoPosition oPos2 = new GeoPosition();

        oPos1.setLatitude(0.0);
        oPos1.setLongitude(0.0);
        oPos1.setAltitude(0.0);

        oPos2.setLatitude(0.0);
        oPos2.setLongitude(1.0 / 60.0);
        oPos2.setAltitude(0.0);

        oPos2.setLongitude(1.0 / 60.0);
        double dDist = GeographicLib.computeDistanceBetween(oPos1, oPos1);
        Log.d(TAG, "    Testing 0 degree.");
        Assert.assertEquals(0.0, dDist, 0.0);

        oPos2.setLongitude(1.0 / 60.0);
        dDist = GeographicLib.computeDistanceBetween(oPos1, oPos2);
        Log.d(TAG, "    Testing 1 min.");
        Assert.assertEquals(NAUTICAL_MILE_IN_METERS, dDist, DISTANCE_ERROR_PER_DEGREE / 60.0);

        oPos2.setLongitude(1.0);
        dDist = GeographicLib.computeDistanceBetween(oPos1, oPos2);
        Log.d(TAG, "    Testing 1 degree.");
        Assert.assertEquals(METERS_1_DEGREE, dDist, DISTANCE_ERROR_PER_DEGREE);

        oPos2.setLongitude(10.0);
        dDist = GeographicLib.computeDistanceBetween(oPos1, oPos2);
        Log.d(TAG, "    Testing 10 degree.");
        Assert.assertEquals(METERS_1_DEGREE * 10, dDist, DISTANCE_ERROR_PER_DEGREE * 10.0);

        oPos2.setLongitude(100.0);
        dDist = GeographicLib.computeDistanceBetween(oPos1, oPos2);
        Log.d(TAG, "    Testing 100 degree.");
        Assert.assertEquals(METERS_1_DEGREE * 100, dDist, DISTANCE_ERROR_PER_DEGREE * 100.0);

        Log.d(TAG, "End computeDistanceTest.");
    }

    @Test
    public void computeBearingTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting computeBearingTest.");
        IGeoPosition oPos1 = new GeoPosition();
        IGeoPosition oPos2 = new GeoPosition();
        double dAzimuth;

        oPos1.setLatitude(0.0);
        oPos1.setLongitude(0.0);
        oPos1.setAltitude(0.0);

        oPos2.setAltitude(0.0);

        oPos2.setLatitude(1.0);
        oPos2.setLongitude(0.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 0 degrees.");
        Assert.assertEquals(0.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(1.0);
        oPos2.setLongitude(1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 45 degrees.");
        Assert.assertEquals(45.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(0.0);
        oPos2.setLongitude(1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 90 degrees.");
        Assert.assertEquals(90.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(-1.0);
        oPos2.setLongitude(1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 135 degrees.");
        Assert.assertEquals(135.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(-1.0);
        oPos2.setLongitude(0.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 180 degrees.");
        Assert.assertEquals(180.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(-1.0);
        oPos2.setLongitude(-1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 225 degrees.");
        Assert.assertEquals(225.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(0.0);
        oPos2.setLongitude(-1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 270 degrees.");
        Assert.assertEquals(270.0, dAzimuth, BEARING_DELTA);

        oPos2.setLatitude(1.0);
        oPos2.setLongitude(-1.0);
        dAzimuth = GeographicLib.computeBearing(oPos1, oPos2);
        Log.d(TAG, "    Testing 315 degrees.");
        Assert.assertEquals(315.0, dAzimuth, BEARING_DELTA);

        Log.d(TAG, "End computeBearingTest.");
    }

    private void positionAtTest(double dBearing, double dDegreeDist, IGeoPosition oPos1) throws EMP_Exception, InterruptedException {
        IGeoPosition oPos2;
        double dDist = METERS_1_DEGREE * dDegreeDist;
        double dCalculateDist;
        double dCalculateBearing;

        oPos2 = GeographicLib.computePositionAt(dBearing, dDist, oPos1);
        Log.d(TAG, "    Testing " + dBearing + "degrees Dist: " + dDist + " m.");
        Log.d(TAG, "            Bearing.");
        dCalculateBearing = GeographicLib.computeBearing(oPos1, oPos2);
        Assert.assertEquals(dBearing, dCalculateBearing, BEARING_DELTA);
        Log.d(TAG, "            Distance.");
        dCalculateDist = GeographicLib.computeDistanceBetween(oPos1, oPos2);
        Assert.assertEquals(dDist, dCalculateDist, DISTANCE_ERROR_PER_DEGREE * dDegreeDist);
    }

    @Test
    public void computePositionAtTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting computePositionAtTest.");
        IGeoPosition oPos1 = new GeoPosition();

        oPos1.setLatitude(0.0);
        oPos1.setLongitude(0.0);
        oPos1.setAltitude(0.0);
        this.positionAtTest(0.0, 0, oPos1);

        this.positionAtTest(0.0, 1, oPos1);
        this.positionAtTest(45.0, 1, oPos1);
        this.positionAtTest(90.0, 1, oPos1);
        this.positionAtTest(135.0, 1, oPos1);
        this.positionAtTest(180.0, 1, oPos1);
        this.positionAtTest(225.0, 1, oPos1);
        this.positionAtTest(270.0, 1, oPos1);
        this.positionAtTest(315.0, 1, oPos1);

        this.positionAtTest(0.0, 10, oPos1);
        this.positionAtTest(45.0, 10, oPos1);
        this.positionAtTest(90.0, 10, oPos1);
        this.positionAtTest(135.0, 10, oPos1);
        this.positionAtTest(180.0, 10, oPos1);
        this.positionAtTest(225.0, 10, oPos1);
        this.positionAtTest(270.0, 10, oPos1);
        this.positionAtTest(315.0, 10, oPos1);

        this.positionAtTest(0.0, 100, oPos1);
        this.positionAtTest(45.0, 100, oPos1);
        this.positionAtTest(90.0, 100, oPos1);
        this.positionAtTest(135.0, 100, oPos1);
        this.positionAtTest(180.0, 100, oPos1);
        this.positionAtTest(225.0, 100, oPos1);
        this.positionAtTest(270.0, 100, oPos1);
        this.positionAtTest(315.0, 100, oPos1);

        Log.d(TAG, "End computePositionAtTest.");
    }

    @Test
    public void midPointBetweenTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting midPointBetweenTest.");
        IGeoPosition oTopLeft = new GeoPosition();
        IGeoPosition oTopRight = new GeoPosition();
        IGeoPosition oBottomLeft = new GeoPosition();
        IGeoPosition oBottomRight = new GeoPosition();
        IGeoPosition oCenter1, oCenter2;

        oTopLeft.setLatitude(1.0);
        oTopLeft.setLongitude(-1.0);
        oTopLeft.setAltitude(0.0);

        oTopRight.setLatitude(1.0);
        oTopRight.setLongitude(1.0);
        oTopRight.setAltitude(0.0);

        oBottomLeft.setLatitude(-1.0);
        oBottomLeft.setLongitude(-1.0);
        oBottomLeft.setAltitude(0.0);

        oBottomRight.setLatitude(-1.0);
        oBottomRight.setLongitude(1.0);
        oBottomRight.setAltitude(0.0);

        oCenter1 = GeographicLib.midPointBetween(oTopLeft, oBottomRight);
        oCenter2 = GeographicLib.midPointBetween(oBottomLeft, oTopRight);
        Log.d(TAG, "  Testing Top Left to Bottom Right latitude.");
        Assert.assertEquals(0.0, oCenter1.getLatitude(), 0.1);
        Log.d(TAG, "  Testing Top Left to Bottom Right longitude.");
        Assert.assertEquals(0.0, oCenter1.getLongitude(), 0.1);
        Log.d(TAG, "  Testing Bottom Left to top Right latitude.");
        Assert.assertEquals(0.0, oCenter2.getLatitude(), 0.1);
        Log.d(TAG, "  Testing Bottom Left to top Right longitude.");
        Assert.assertEquals(0.0, oCenter2.getLongitude(), 0.1);

        Log.d(TAG, "End midPointBetweenTest.");
    }

    @Test
    public void computeRhumbDistanceTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting computeRhumbDistanceTest.");
        IGeoPosition oPos1 = new GeoPosition();
        IGeoPosition oPos2 = new GeoPosition();

        oPos1.setLatitude(0.0);
        oPos1.setLongitude(0.0);
        oPos1.setAltitude(0.0);

        oPos2.setLatitude(0.0);
        oPos2.setLongitude(1.0);
        oPos2.setAltitude(0.0);

        double dDist = GeographicLib.computeRhumbDistance(oPos1, oPos2);
        Log.d(TAG, "    Testing 1 degree.");
        Assert.assertEquals(METERS_1_DEGREE, dDist, DISTANCE_ERROR_PER_DEGREE);

        oPos2.setLongitude(10.0);
        dDist = GeographicLib.computeRhumbDistance(oPos1, oPos2);
        Log.d(TAG, "    Testing 10 degree.");
        Assert.assertEquals(METERS_1_DEGREE * 10, dDist, DISTANCE_ERROR_PER_DEGREE * 10.0);

        oPos2.setLongitude(100.0);
        dDist = GeographicLib.computeRhumbDistance(oPos1, oPos2);
        Log.d(TAG, "    Testing 100 degree.");
        Assert.assertEquals(METERS_1_DEGREE * 100, dDist, DISTANCE_ERROR_PER_DEGREE * 100.0);

        Log.d(TAG, "End computeRhumbDistanceTest.");
    }
}
