package mil.emp3.api;

import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;

import mil.emp3.api.enums.WMSVersionEnum;

public class WMSTest extends TestBase {
    public static final String TAG = WMSTest.class.getSimpleName();

    @Before
    public void setUp() throws Exception{
        super.init();
    }

    @After
    public void cleanUp() throws Exception{

    }

    @Test
    public void invalidUrlTest() {
        Log.i(TAG, "Invalid URL Test");
        try {
            WMS wms = new WMS("xxxx", null, null, false, null);
            Assert.fail("Bad URL not caught");
        } catch (MalformedURLException mue) {
            Log.i(TAG, "Bad URL caught, test passed");
        }
    }

    @Test
    public void defaultSettingsTest() {
        Log.i(TAG, "Default Settings Test");
        try {
            WMS wms = new WMS("http://127.0.0.1", null, null, false, null);
            Assert.assertEquals(wms.getWMSVersion(), WMSVersionEnum.VERSION_1_3_0);
            Assert.assertEquals(wms.getLayers().size(), 0);
        } catch (Exception mue) {
            Assert.fail("Exception while setting defaults");
        }
    }

    @Test
    public void nullCoordinateSystemTest(){
        Log.i(TAG, "Null Coordinate System Test");
        try {
            WMS wms = new WMS("http://127.0.0.1", null, null, false, null);
            wms.setCoordinateSystem(null);
            Assert.fail("Null coordinate system not caught");
        } catch (Exception e) {
            Log.i(TAG, "Null coordinate system caught, test passed");
        }
    }
}
