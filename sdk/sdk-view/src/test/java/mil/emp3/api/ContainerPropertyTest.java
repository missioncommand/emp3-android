package mil.emp3.api;

import android.util.Log;

import org.junit.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;

/**
 * This class performs the container property API tests.
 */

public class ContainerPropertyTest extends TestBaseSingleMap {
    private static final String TAG = ContainerPropertyTest.class.getSimpleName();

    private static final String BOOLEAN_PROPERTY_NAME = "boolean property";
    private static final String INTEGER_PROPERTY_NAME = "integer property";
    private static final String FLOAT_PROPERTY_NAME = "float property";
    private static final String DOUBLE_PROPERTY_NAME = "double property";
    private static final String STRING_PROPERTY_NAME = "string property";
    private static final String BOUGUS_PROPERTY_NAME = "bougus property";

    private static final boolean BOOLEAN_VALUE = false;
    private static final int INTEGER_VALUE = 123;
    private static final float FLOAT_VALUE = 123.4f;
    private static final double DOUBLE_VALUE = 1.0e-7;
    private static final String STRING_VALUE = "This is a string value";

    private IFeature feature;
    @Before
    public void setUp() throws Exception {
        this.feature = new mil.emp3.api.Polygon();
    }

    @After
    public void cleanUp() throws Exception {
        this.feature = null;
    }

    @Test
    public void setPropertyTest() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting setPropertyTest.");

        try {
            feature.setProperty(BOOLEAN_PROPERTY_NAME, BOOLEAN_VALUE + "");
        } catch (Exception ex) {
            Assert.assertTrue("setProperty Boolean failed.", false);
        }

        try {
            feature.setProperty(INTEGER_PROPERTY_NAME, INTEGER_VALUE + "");
        } catch (Exception ex) {
            Assert.assertTrue("setProperty Integer failed.", false);
        }

        try {
            feature.setProperty(FLOAT_PROPERTY_NAME, FLOAT_VALUE + "");
        } catch (Exception ex) {
            Assert.assertTrue("setProperty float failed.", false);
        }

        try {
            feature.setProperty(DOUBLE_PROPERTY_NAME, DOUBLE_VALUE + "");
        } catch (Exception ex) {
            Assert.assertTrue("setProperty double failed.", false);
        }

        try {
            feature.setProperty(STRING_PROPERTY_NAME, STRING_VALUE);
        } catch (Exception ex) {
            Assert.assertTrue("setProperty string failed.", false);
        }

        Log.d(TAG, "Starting containsPropertyTest.");

        Assert.assertTrue("containsProperty Boolean failed.", feature.containsProperty(BOOLEAN_PROPERTY_NAME));
        Assert.assertTrue("containsProperty Integer failed.", feature.containsProperty(INTEGER_PROPERTY_NAME));
        Assert.assertTrue("containsProperty Float failed.", feature.containsProperty(FLOAT_PROPERTY_NAME));
        Assert.assertTrue("containsProperty Double failed.", feature.containsProperty(DOUBLE_PROPERTY_NAME));
        Assert.assertTrue("containsProperty String failed.", feature.containsProperty(STRING_PROPERTY_NAME));

        Object testObject;

        Log.d(TAG, "Starting getPropertyTest.");

        try {
            Assert.assertTrue("getBooleanProperty failed.", feature.getBooleanProperty(BOOLEAN_PROPERTY_NAME) == BOOLEAN_VALUE);
        } catch (Exception ex) {
            Assert.assertTrue("getBooleanProperty failed.", false);
        }

        try {
            Assert.assertTrue("getIntegerProperty failed.", feature.getIntegerProperty(INTEGER_PROPERTY_NAME) == INTEGER_VALUE);
        } catch (Exception ex) {
            Assert.assertTrue("getIntegerProperty failed.", false);
        }

        try {
            Assert.assertTrue("getFloatProperty failed.", feature.getFloatProperty(FLOAT_PROPERTY_NAME) == FLOAT_VALUE);
        } catch (Exception ex) {
            Assert.assertTrue("getFloatProperty failed.", false);
        }

        try {
            Assert.assertTrue("getDoubleProperty failed.", feature.getDoubleProperty(DOUBLE_PROPERTY_NAME) == DOUBLE_VALUE);
        } catch (Exception ex) {
            Assert.assertTrue("getDoubleProperty failed.", false);
        }

        try {
            Assert.assertTrue("getProperty failed.", feature.getProperty(STRING_PROPERTY_NAME).equals(STRING_VALUE));
        } catch (Exception ex) {
            Assert.assertTrue("getProperty failed.", false);
        }

        try {
            Assert.assertFalse("getBooleanProperty failed.", feature.getBooleanProperty(STRING_PROPERTY_NAME) == BOOLEAN_VALUE);
        } catch (Exception ex) {
            Assert.assertTrue("getBooleanProperty failed.", true);
        }

        try {
            Assert.assertFalse("getIntegerProperty failed.", feature.getIntegerProperty(STRING_PROPERTY_NAME) == INTEGER_VALUE);
        } catch (Exception ex) {
            Assert.assertTrue("getIntegerProperty failed.", true);
        }

        try {
            Assert.assertFalse("getFloatProperty failed.", feature.getFloatProperty(STRING_PROPERTY_NAME) == FLOAT_VALUE);
        } catch (Exception ex) {
            Assert.assertTrue("getFloatProperty failed.", true);
        }

        try {
            Assert.assertFalse("getDoubleProperty failed.", feature.getDoubleProperty(STRING_PROPERTY_NAME) == DOUBLE_VALUE);
        } catch (Exception ex) {
            Assert.assertTrue("getDoubleProperty failed.", true);
        }

        try {
            Assert.assertFalse("getProperty failed.", feature.getProperty(DOUBLE_PROPERTY_NAME).equals(STRING_VALUE));
        } catch (Exception ex) {
            Assert.assertTrue("getProperty failed.", true);
        }

        try {
            Assert.assertFalse("getProperty failed.", feature.getProperty(BOUGUS_PROPERTY_NAME).equals(STRING_VALUE));
        } catch (Exception ex) {
            Assert.assertTrue("getProperty failed.", true);
        }

        Log.d(TAG, "Starting removePropertyTest.");

        feature.removeProperty(BOOLEAN_PROPERTY_NAME);
        Assert.assertTrue("removeProperty Boolean failed.", !feature.containsProperty(BOOLEAN_PROPERTY_NAME));

        feature.removeProperty(INTEGER_PROPERTY_NAME);
        Assert.assertTrue("removeProperty Integer failed.", !feature.containsProperty(INTEGER_PROPERTY_NAME));

        feature.removeProperty(FLOAT_PROPERTY_NAME);
        Assert.assertTrue("removeProperty Float failed.", !feature.containsProperty(FLOAT_PROPERTY_NAME));

        feature.removeProperty(DOUBLE_PROPERTY_NAME);
        Assert.assertTrue("removeProperty Double failed.", !feature.containsProperty(DOUBLE_PROPERTY_NAME));

        feature.removeProperty(STRING_PROPERTY_NAME);
        Assert.assertTrue("removeProperty String failed.", !feature.containsProperty(STRING_PROPERTY_NAME));
    }
}
