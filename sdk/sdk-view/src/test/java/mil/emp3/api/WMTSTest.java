package mil.emp3.api;

import android.util.Log;

import org.junit.Assert;

import java.util.Arrays;
import java.util.Collections;

import mil.emp3.api.enums.WMTSVersionEnum;

import static org.junit.Assert.*;

/**
 * Created by Matt.Miller on 10/12/2017.
 */
public class WMTSTest extends TestBase {
    public static final String TAG = WMSTest.class.getSimpleName();
    private WMTS testWmtsObject;
    private final String tileFormat = "image/png";
    private final Boolean transparent = true;

    @org.junit.Before
    public void setUp() throws Exception {
        super.init();
        testWmtsObject = new WMTS("http://127.0.0.1", WMTSVersionEnum.VERSION_1_0_0, tileFormat, null);
        Log.i(TAG, "WMS Object created, toString value: " + testWmtsObject.toString());
    }

    @org.junit.After
    public void tearDown() throws Exception {
        testWmtsObject = new WMTS("http://127.0.0.1", null, tileFormat, Arrays.asList("layer1", "layer2"));
        Log.i(TAG, "WMS Object final state, toString value: " + testWmtsObject.toString());

    }
    @org.junit.Test
    public void getWMTSVersion() throws Exception {
        assertEquals(testWmtsObject.getWMTSVersion(), WMTSVersionEnum.VERSION_1_0_0);
    }

    @org.junit.Test
    public void getTileFormat() throws Exception {
        assertEquals(testWmtsObject.getTileFormat(), tileFormat);
    }

    @org.junit.Test
    public void stylesTest() throws Exception {
        //should not change anything...potentially should throw an error?
        assertNull(testWmtsObject.getStyles());
        testWmtsObject.setStyles(Arrays.asList());
        assertEquals(testWmtsObject.getStyles(), Collections.emptyList());

        testWmtsObject.setStyles(Arrays.asList("value1"));
        assertEquals(testWmtsObject.getStyles(), Arrays.asList("value1"));
    }

    @org.junit.Test
    public void dimensionsTest() throws Exception {
        //should not change anything...potentially should throw an error?
        assertNull(testWmtsObject.getDimensions());
        testWmtsObject.setDimensions(Arrays.asList());
        assertEquals(testWmtsObject.getDimensions(), Collections.emptyList());

        testWmtsObject.setDimensions(Arrays.asList("value1"));
        assertEquals(testWmtsObject.getDimensions(), Arrays.asList("value1"));
    }

    @org.junit.Test
    public void layersTest() throws Exception {
        assertEquals(testWmtsObject.getLayers(), Collections.emptyList());
        testWmtsObject.setLayers(Collections.emptyList());
        assertEquals(testWmtsObject.getLayers(), Collections.emptyList());
        testWmtsObject.setLayers(Collections.singletonList("layer1"));
        assertEquals(testWmtsObject.getLayers(),Collections.singletonList("layer1"));
        try {
            testWmtsObject.getLayers().add("layer2");
            Assert.fail("Should not have been able to add layer to private list variable");
        }catch (Exception e) {
            Log.i(TAG, "Success: Could not add to private List variable");
        }

        assertEquals(testWmtsObject.getLayers(),Collections.singletonList("layer1"));
        testWmtsObject.setLayers(Arrays.asList("layer1", "layer2"));
        assertEquals(testWmtsObject.getLayers(),Arrays.asList("layer1", "layer2"));
    }

}