package mil.emp3.api;


import android.test.mock.MockContext;
import android.util.Log;
import android.webkit.URLUtil;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;

import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import mil.emp3.api.enums.KMLSEventEnum;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.IMapService;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by Matt.Miller on 10/12/2017.
 */
public class WCSTest extends TestBaseSingleMap{
    private WCS testWcsObject;
    final private String url = "https://127.0.0.1";
    final private String name = "test";

    @Before
    public void setUp() throws Exception {
        testWcsObject = new WCS(url, name);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void getServiceURLTest() throws Exception {
        assertEquals(testWcsObject.getServiceURL(),url);
    }

    @Test
    public void getCoverageNameTest() throws Exception {
        assertEquals(name, testWcsObject.getCoverageName());
    }

    @Test
    public void toStringTest() throws Exception {
        final String expectedtoString = "URL: " + url + "\nCoverage: "+name;
        assertEquals(expectedtoString,testWcsObject.toString());
    }

    public void addLayerTest() throws Exception {
        final IMapService mapService = new WCS("https://", "test_name");
        remoteMap.addMapService(mapService);

        final List<IMapService> services = remoteMap.getMapServices();
        Assert.assertNotNull("services should not be null", services);
        IKMLS foundService = null;
        for(final IMapService s: services) {
            if(s instanceof IKMLS && s.getName().equals("kmlSample_Test")) {
                foundService = (IKMLS) s;
                break;
            }
        }
        Assert.assertNotNull("It should be KML Service kmlSample_Test", foundService);

        remoteMap.removeMapService(mapService);
        Assert.assertTrue("Removed features GeoId must match ", mapInstance.validateRemoveKmlService());
    }

}