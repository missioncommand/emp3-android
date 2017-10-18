package mil.emp3.api;

import android.util.Log;

import junit.framework.AssertionFailedError;

import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import mil.emp3.api.enums.WMSVersionEnum;

import static org.junit.Assert.*;

/**
 * Created by Matt.Miller on 10/12/2017.
 */
public class WMSTest extends TestBase {
    public static final String TAG = WMSTest.class.getSimpleName();
    private WMS testWmsObject;
    private final String tileFormat = "image/png";
    private final Boolean transparent = true;

    @org.junit.Before
    public void setUp() throws Exception {
        super.init();
        testWmsObject = new WMS("http://127.0.0.1", WMSVersionEnum.VERSION_1_1, tileFormat, transparent, null);
        Log.i(TAG, "WMS Object created, toString value: " + testWmsObject.toString());
    }

    @org.junit.After
    public void tearDown() throws Exception {
        testWmsObject = new WMS("http://127.0.0.1", WMSVersionEnum.VERSION_1_1, tileFormat, transparent, Arrays.asList("layer1", "layer2"));
        Log.i(TAG, "WMS Object final state, toString value: " + testWmsObject.toString());

    }

    @org.junit.Test
    public void getWMSVersion() throws Exception {
        assertEquals(testWmsObject.getWMSVersion(), WMSVersionEnum.VERSION_1_1);
    }

    @org.junit.Test
    public void getTileFormat() throws Exception {
        assertEquals(testWmsObject.getTileFormat(), tileFormat);
    }

    @org.junit.Test
    public void getTransaparent() throws Exception {
        assertEquals(testWmsObject.getTransaparent(), transparent);
    }

    @org.junit.Test
    public void coordinateSystemTest() throws Exception {
        //should potentially throw an error since Coordinate system cannot be set to null
        assertEquals(testWmsObject.getCoordinateSystem(),null);
        try {
            testWmsObject.setCoordinateSystem(null);
            Assert.fail("Did not catch null coordinate system");
        } catch (Exception e){
        }
        try {
            testWmsObject.setCoordinateSystem("");
            Assert.fail("Did not catch emtpy coordinate system");
        } catch (Exception E){
        }
        //there should be some restrictions on the input
        //but for now this is acceptable
        testWmsObject.setCoordinateSystem("test_string");
        assertEquals(testWmsObject.getCoordinateSystem(), "test_string");
    }

    @org.junit.Test
    public void stylesTest() throws Exception {
        //should not change anything...potentially should throw an error?
        assertNull(testWmsObject.getStyles());
        testWmsObject.setStyles(Arrays.asList());
        assertEquals(testWmsObject.getStyles(), Collections.emptyList());

        testWmsObject.setStyles(Arrays.asList("value1"));
        assertEquals(testWmsObject.getStyles(), Arrays.asList("value1"));
    }

    @org.junit.Test
    public void timeStringTest() throws Exception {
        //this should have improved validation
        testWmsObject.setTimeString("test_string");
        assertEquals(testWmsObject.getTimeString(), "test_string");
    }

    @org.junit.Test
    public void layerResolutionTest() throws Exception {
        testWmsObject.setLayerResolution(99.9);
        assertEquals(testWmsObject.getLayerResolution(),99.9,0);
        //probably should have validation
        testWmsObject.setLayerResolution(-100.5);
        assertEquals(testWmsObject.getLayerResolution(),-100.5,0);
    }

    @org.junit.Test
    public void layersTest() throws Exception {
        assertEquals(testWmsObject.getLayers(), Collections.emptyList());
        testWmsObject.setLayers(Collections.emptyList());
        assertEquals(testWmsObject.getLayers(), Collections.emptyList());
        testWmsObject.setLayers(Collections.singletonList("layer1"));
        assertEquals(testWmsObject.getLayers(),Collections.singletonList("layer1"));
        try {
            testWmsObject.getLayers().add("layer2");
            Assert.fail("Should not have been able to add layer to private list variable");
        }catch (Exception e) {
            Log.i(TAG, "Success: Could not add to private List variable");
        }
        assertEquals(testWmsObject.getLayers(),Collections.singletonList("layer1"));
        testWmsObject.setLayers(Arrays.asList("layer1", "layer2"));
        assertEquals(testWmsObject.getLayers(),Arrays.asList("layer1", "layer2"));
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

    @Test
    public void invalidGeoPackageUrlTest() {
        Log.i(TAG, "Invalid GeoPackage URL Test");
        try {
            GeoPackage gpkg = new GeoPackage("zzzzz");
            Assert.fail("Bad URL not caught");
        } catch (MalformedURLException mue) {
            Log.i(TAG, "Bad URL caught, test passed");
        }
    }

}