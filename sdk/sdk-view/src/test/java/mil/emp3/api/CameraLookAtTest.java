package mil.emp3.api;

import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.ILookAt;

public class CameraLookAtTest extends TestBase {
    public static final String TAG = CameraLookAtTest.class.getSimpleName();

    @Rule
    public TestName name = new TestName();

    private ICamera camera;
    private ILookAt lookAt;

    @Before
    public void setUp() throws Exception{
        init();
        camera = new Camera();
        lookAt = new LookAt();
    }

    @After
    public void cleanUp() throws Exception{

    }

    @Test
    public void cameraTests() {
        invalidLatitudeCameraTest(Double.NaN);
        invalidLatitudeCameraTest(global.LATITUDE_MINIMUM - 10);
        invalidLatitudeCameraTest(global.LATITUDE_MAXIMUM + 10);
        invalidLongitudeCameraTest(Double.NaN);
        invalidLongitudeCameraTest(global.LONGITUDE_MINIMUM - 10);
        invalidLongitudeCameraTest(global.LONGITUDE_MAXIMUM + 10);
        invalidRollCameraTest(Double.NaN);
        invalidRollCameraTest(global.CAMERA_ROLL_MINIMUM - 10);
        invalidRollCameraTest(global.CAMERA_ROLL_MAXIMUM + 10);
        invalidTiltCameraTest(Double.NaN);
        invalidTiltCameraTest(global.CAMERA_TILT_MINIMUM - 10);
        invalidTiltCameraTest(global.CAMERA_TILT_MAXIMUM + 10);
        invalidHeadingCameraTest(Double.NaN);
        invalidHeadingCameraTest(global.HEADING_MINIMUM - 10);
        invalidHeadingCameraTest(global.HEADING_MAXIMUM + 10);

        validLatitudeCameraTest(global.LATITUDE_MINIMUM);
        validLatitudeCameraTest(global.LATITUDE_MAXIMUM);
        validLongitudeCameraTest(global.LONGITUDE_MINIMUM);
        validLongitudeCameraTest(global.LONGITUDE_MAXIMUM);
        validRollCameraTest(global.CAMERA_ROLL_LEVEL);
        validTiltCameraTest(global.CAMERA_TILT_TO_GROUND);
        validTiltCameraTest(global.CAMERA_TILT_TO_HORIZON);
        validTiltCameraTest(global.CAMERA_TILT_TO_SKY);
        validHeadingCameraTest(global.HEADING_MINIMUM);
        validHeadingCameraTest(global.HEADING_MAXIMUM);
        camera.apply(false);
        camera.apply(true);
        camera.apply(false, null);
        camera.apply(true, null);
        camera.apply(false, "JUnit test, no animate");
        camera.apply(true, "JUnit test, animate");
    }

    @Test
    public void LookAtTests() {
        invalidLatitudeLookAtTest(Double.NaN);
        invalidLatitudeLookAtTest(global.LATITUDE_MINIMUM - 10);
        invalidLatitudeLookAtTest(global.LATITUDE_MAXIMUM + 10);
        invalidLongitudeLookAtTest(Double.NaN);
        invalidLongitudeLookAtTest(global.LONGITUDE_MINIMUM - 10);
        invalidLongitudeLookAtTest(global.LONGITUDE_MAXIMUM + 10);
        invalidTiltLookAtTest(Double.NaN);
        invalidTiltLookAtTest(global.CAMERA_TILT_MINIMUM - 10);
        invalidTiltLookAtTest(global.CAMERA_TILT_MAXIMUM + 10);
        invalidHeadingLookAtTest(Double.NaN);
        invalidHeadingLookAtTest(global.HEADING_MINIMUM - 10);
        invalidHeadingLookAtTest(global.HEADING_MAXIMUM + 10);

        validLatitudeLookAtTest(global.LATITUDE_MINIMUM);
        validLatitudeLookAtTest(global.LATITUDE_MAXIMUM);
        validLongitudeLookAtTest(global.LONGITUDE_MINIMUM);
        validLongitudeLookAtTest(global.LONGITUDE_MAXIMUM);
        validHeadingLookAtTest(global.HEADING_MINIMUM);
        validHeadingLookAtTest(global.HEADING_MAXIMUM);
    }

    private void invalidLatitudeCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setLatitude(value);
            Assert.fail("Exception not caught, test failed");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Exception caught, test passed");
        }
    }

    private void invalidLongitudeCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setLongitude(value);
            Assert.fail("Exception not caught, test failed");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Exception caught, test passed");
        }
    }

    private void invalidRollCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setRoll(value);
            Assert.fail("Exception not caught, test failed");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Exception caught, test passed");
        }
    }

    private void invalidTiltCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setTilt(value);
            Assert.fail("Exception not caught, test failed");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Exception caught, test passed");
        }
    }

    private void invalidHeadingCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setHeading(value);
            Assert.fail("Exception not caught, test failed");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Exception caught, test passed");
        }
    }

    private void validLatitudeCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setLatitude(value);
            Log.i(TAG, "Test passed");
        } catch (IllegalArgumentException e) {
            Assert.fail("Exception caught, test failed");
        }
    }

    private void validLongitudeCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setLongitude(value);
            Log.i(TAG, "Test passed");
        } catch (IllegalArgumentException e) {
            Assert.fail("Exception caught, test failed");
        }
    }

    private void validRollCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setRoll(value);
            Log.i(TAG, "Test passed");
        } catch (IllegalArgumentException e) {
            Assert.fail("Exception caught, test failed");
        }
    }

    private void validTiltCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setTilt(value);
            Log.i(TAG, "Test passed");
        } catch (IllegalArgumentException e) {
            Assert.fail("Exception caught, test failed");
        }
    }

    private void validHeadingCameraTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            camera.setHeading(value);
            Log.i(TAG, "Test passed");
        } catch (IllegalArgumentException e) {
            Assert.fail("Exception caught, test failed");
        }
    }

    private void invalidLatitudeLookAtTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            lookAt.setLatitude(value);
            Assert.fail("Exception not caught, test failed");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Exception caught, test passed");
        }
    }

    private void invalidLongitudeLookAtTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            lookAt.setLongitude(value);
            Assert.fail("Exception not caught, test failed");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Exception caught, test passed");
        }
    }

    private void invalidTiltLookAtTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            lookAt.setTilt(value);
            Assert.fail("Exception not caught, test failed");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Exception caught, test passed");
        }
    }

    private void invalidHeadingLookAtTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            lookAt.setHeading(value);
            Assert.fail("Exception not caught, test failed");
        } catch (IllegalArgumentException e) {
            Log.i(TAG, "Exception caught, test passed");
        }
    }

    private void validLatitudeLookAtTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            lookAt.setLatitude(value);
            Log.i(TAG, "Test passed");
        } catch (IllegalArgumentException e) {
            Assert.fail("Exception caught, test failed");
        }
    }

    private void validLongitudeLookAtTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            lookAt.setLongitude(value);
            Log.i(TAG, "Test passed");
        } catch (IllegalArgumentException e) {
            Assert.fail("Exception caught, test failed");
        }
    }

    private void validTiltLookAtTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            lookAt.setTilt(value);
            Log.i(TAG, "Test passed");
        } catch (IllegalArgumentException e) {
            Assert.fail("Exception caught, test failed");
        }
    }

    private void validHeadingLookAtTest(double value) {
        Log.i(TAG, "Executing " + name.getMethodName());
        try {
            lookAt.setHeading(value);
            Log.i(TAG, "Test passed");
        } catch (IllegalArgumentException e) {
            Assert.fail("Exception caught, test failed");
        }
    }

}
