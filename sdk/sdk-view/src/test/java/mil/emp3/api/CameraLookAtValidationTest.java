package mil.emp3.api;

import junit.framework.Assert;

import org.cmapi.primitives.GeoCamera;
import org.cmapi.primitives.GeoLookAt;
import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoCamera;
import org.cmapi.primitives.IGeoLookAt;
import org.cmapi.primitives.IGeoPosition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;
import mil.emp3.api.utils.EmpGeoPosition;
@RunWith(RobolectricTestRunner.class)
public class CameraLookAtValidationTest extends TestBaseSingleMap {

    private static String TAG = CameraLookAtValidationTest.class.getSimpleName();
    private final static double delta = 0.0000001;

    @Before
    public void setUp() throws Exception {
        setupSingleMap(TAG);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidGeoCamera() {
        IGeoCamera geoCamera = new GeoCamera();
        geoCamera.setLatitude(global.LATITUDE_MAXIMUM + 1.0);
        ICamera camera = new Camera(geoCamera);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFromCamera() {
        ICamera from = null;
        ICamera camera = new Camera(from);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLongitude() {
        ICamera camera = new Camera(0, global.LONGITUDE_MINIMUM-1, 0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPosition() {
        IGeoPosition position = null;
        ICamera camera = new Camera(position);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPosition() {
        IGeoPosition position = new GeoPosition();
        position.setLatitude(global.LATITUDE_MINIMUM - 1);
        ICamera camera = new Camera(position);
    }

    @Test
    public void validPosition() {
        EmpGeoPosition position = new EmpGeoPosition(global.LATITUDE_MINIMUM, global.LONGITUDE_MAXIMUM, 1000);
        ICamera camera = new Camera(position);
        Assert.assertEquals("Latitude ", global.LATITUDE_MINIMUM, camera.getLatitude(), delta);
        Assert.assertEquals("Longitude ", global.LONGITUDE_MAXIMUM, camera.getLongitude(), delta);
        Assert.assertEquals("Altitude ", 1000.0, camera.getAltitude(), delta);
        Assert.assertEquals("Tilt ", 0, camera.getTilt(), delta);
        Assert.assertEquals("Roll ", 0, camera.getRoll(), delta);
        Assert.assertEquals("Heading ", 0, camera.getHeading(), delta);
        Assert.assertEquals("AltitudeMode ", IGeoAltitudeMode.AltitudeMode.ABSOLUTE, camera.getAltitudeMode());
    }

    @Test
    public void validLatLong() {
        ICamera camera = new Camera(global.LATITUDE_MINIMUM, global.LONGITUDE_MAXIMUM, 1000, IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
        Assert.assertEquals("Latitude ", global.LATITUDE_MINIMUM, camera.getLatitude(), delta);
        Assert.assertEquals("Longitude ", global.LONGITUDE_MAXIMUM, camera.getLongitude(), delta);
        Assert.assertEquals("Altitude ", 1000.0, camera.getAltitude(), delta);
        Assert.assertEquals("Tilt ", 0, camera.getTilt(), delta);
        Assert.assertEquals("Roll ", 0, camera.getRoll(), delta);
        Assert.assertEquals("Heading ", 0, camera.getHeading(), delta);
        Assert.assertEquals("AltitudeMode ", IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND, camera.getAltitudeMode());
    }

    // LookAt

    @Test(expected = IllegalArgumentException.class)
    public void invalidGeoLookAt() {
        IGeoLookAt geoLookAt = new GeoLookAt();
        geoLookAt.setLatitude(global.LATITUDE_MAXIMUM + 1.0);
        ILookAt camera = new LookAt(geoLookAt);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullFromLookAt() {
        ILookAt from = null;
        ILookAt lookAt = new LookAt(from);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidLongitudeLookAt() {
        ILookAt lookAt = new LookAt(0, global.LONGITUDE_MINIMUM-1, 0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullPositionForLookAt() {
        IGeoPosition position = null;
        ILookAt lookAt = new LookAt(position);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidPositionLookAt() {
        IGeoPosition position = new GeoPosition();
        position.setLatitude(global.LATITUDE_MINIMUM - 1);
        ILookAt lookAt = new LookAt(position);
    }

    @Test
    public void validPositionLookAt() {
        EmpGeoPosition position = new EmpGeoPosition(global.LATITUDE_MINIMUM, global.LONGITUDE_MAXIMUM, 1000);
        ILookAt lookAt = new LookAt(position);
        Assert.assertEquals("Latitude ", global.LATITUDE_MINIMUM, lookAt.getLatitude(), delta);
        Assert.assertEquals("Longitude ", global.LONGITUDE_MAXIMUM, lookAt.getLongitude(), delta);
        Assert.assertEquals("Altitude ", 1000.0, lookAt.getAltitude(), delta);
        Assert.assertEquals("Tilt ", 0, lookAt.getTilt(), delta);
        Assert.assertEquals("Range ", 1000000, lookAt.getRange(), delta);
        Assert.assertEquals("Heading ", 0, lookAt.getHeading(), delta);
        Assert.assertEquals("AltitudeMode ", IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND, lookAt.getAltitudeMode());
    }

    @Test
    public void validLatLongLookAt() {
        ILookAt lookAt = new LookAt(global.LATITUDE_MINIMUM, global.LONGITUDE_MAXIMUM, 1000, IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND);
        Assert.assertEquals("Latitude ", global.LATITUDE_MINIMUM, lookAt.getLatitude(), delta);
        Assert.assertEquals("Longitude ", global.LONGITUDE_MAXIMUM, lookAt.getLongitude(), delta);
        Assert.assertEquals("Altitude ", 1000.0, lookAt.getAltitude(), delta);
        Assert.assertEquals("Tilt ", 0, lookAt.getTilt(), delta);
        Assert.assertEquals("Range ", 1000000, lookAt.getRange(), delta);
        Assert.assertEquals("Heading ", 0, lookAt.getHeading(), delta);
        Assert.assertEquals("AltitudeMode ", IGeoAltitudeMode.AltitudeMode.RELATIVE_TO_GROUND, lookAt.getAltitudeMode());
    }
}
