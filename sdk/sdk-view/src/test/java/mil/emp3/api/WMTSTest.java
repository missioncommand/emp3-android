package mil.emp3.api;

import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import mil.emp3.api.enums.WMTSVersionEnum;

import static mil.emp3.api.utils.ComparisonUtils.validateWMTS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Created by Matt.Miller on 10/12/2017.
 */
public class WMTSTest extends TestBase {
    private WMTS wmts1;
    private final String url = "http://127.0.0.1";
    private final WMTSVersionEnum ver1 = WMTSVersionEnum.VERSION_1_0_0;
    private final String tileFormat = "image/png";
    private final List<String> testList = Collections.singletonList("test");

    @Before
    public void setUp() throws Exception {
        wmts1 = new WMTS(url, null, null, null);

    }

    @Test(expected = MalformedURLException.class)
    public void nullURLConstructor() throws Exception{
        wmts1 = new WMTS(null, null, null, null);
    }

    @Test
    public void nonNullURLConstructor() throws Exception{
        validateWMTS(wmts1,
                     ver1,
                     tileFormat,
                     null,
                     null,
                     new ArrayList<String>(),
                     url,
                     "",
                     null,
                     "",
                     new HashMap());
    }

    @Test
    public void allParamConstructor() throws Exception{
        final String altTileFormat = "image/jpg";
        wmts1 = new WMTS(url, ver1, altTileFormat, testList);
        validateWMTS(wmts1,
                     ver1,
                     altTileFormat,
                     null,
                     null,
                testList,
                     url,
                     "",
                     null,
                     "",
                     new HashMap());
    }

    @Test
    public void getWMTSVersion() {
        assertEquals(wmts1.getWMTSVersion(), ver1);
    }

    @Test
    public void getTileFormat() throws Exception {
        assertEquals(wmts1.getTileFormat(), tileFormat);
    }

    @Test
    public void setNullStyles() {
        wmts1.setStyles(null);
        assertNull(wmts1.getStyles());
    }

    @Test
    public void setNonNullStyles() {
        wmts1.setStyles(testList);
        assertEquals(wmts1.getStyles(), testList);
    }

    @Test
    public void setNullDimensions() {
        wmts1.setDimensions(null);
        assertNull(wmts1.getDimensions());
    }

    @Test
    public void setNonNullDimensions() {
        wmts1.setDimensions(testList);
        assertEquals(wmts1.getDimensions(), testList);
    }

    @Test
    public void setNullLayers() throws Exception {
        wmts1.setLayers(null);
        assertEquals(wmts1.getLayers(), new ArrayList());
    }

    @Test
    public void setEmptyLayers() throws Exception {
        wmts1.setLayers(Collections.emptyList());
        assertEquals(wmts1.getLayers(), new ArrayList());
    }

    @Test
    public void setLayers() throws Exception {
        wmts1.setLayers(testList);
        assertEquals(wmts1.getLayers(), testList);
    }

    @Test
    public void noLayersToString() {
        final String expectedString = "URL: " + url + "\n" +
                                      "WMTS Version: " + ver1 + "\n" +
                                      "Tile format: " + tileFormat + "\n";
        assertEquals(wmts1.toString(), expectedString);
    }

    @Test
    public void layersToString() throws Exception{
        wmts1.setLayers(testList);
        final String expectedString = "URL: " + url + "\n" +
                                      "WMTS Version: " + ver1 + "\n" +
                                      "Tile format: " + tileFormat + "\n" +
                                      "Layers: \n" +
                                      "\ttest\n";
        assertEquals(wmts1.toString(), expectedString);

    }

}