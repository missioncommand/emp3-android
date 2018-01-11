package mil.emp3.api;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Jenifer Cochran
 */
@RunWith(RobolectricTestRunner.class)
public class WMTSTest extends TestBase
{
    public static final String TAG = WMTSTest.class.getSimpleName();
    public static final String validUrl = "http://opencache.statkart.no/gatekeeper/gk/gk.open_wmts";
    public static final String layerNameForValidUrl = "matrikkel_bakgrunn";

    @Before
    public void setUp() throws Exception
    {
        super.init();
    }

    @After
    public void cleanUp() throws Exception
    {

    }

    @Test(expected = MalformedURLException.class)
    public void invalidUrlTest() throws MalformedURLException
    {
        ArrayList<String> layers = new ArrayList<>();
        layers.add(layerNameForValidUrl);
        new WMTS("xxxx", null, null, layers);
        Assert.fail("Bad URL not caught");
    }

    @Test
    public void nullLayers() throws MalformedURLException
    {
        //checks to make sure that the Layers list is never set to null,
        //even if it is passed in as null to the constructor
        final WMTS wmts = new WMTS(validUrl, null, null, null);
        Assert.assertTrue(wmts.getLayers() != null);
    }

    @Test
    public void layersListMatch() throws MalformedURLException
    {
        final ArrayList<String> expectedLayers = new ArrayList<>(Arrays.asList(layerNameForValidUrl, "Other Layer", "Layer 3"));

        final WMTS wmts = new WMTS(validUrl, null, null, expectedLayers);

        Assert.assertTrue(String.format("The layers list returned a different size than expected.  Expected: %d returned:%d",
                                        expectedLayers.size(),
                                        wmts.getLayers().size()),
                        expectedLayers.size() == wmts.getLayers().size());

        //check to see if there are any missing layers
        final ArrayList<String> missingLayers = new ArrayList<>();
        for (String expectedLayer : expectedLayers)
        {
            if(!wmts.getLayers().contains(expectedLayer))
            {
                missingLayers.add(expectedLayer);
            }
        }
        //create the error message
        final StringBuilder missingLayerMessage = new StringBuilder();
        for(String missingLayer : missingLayers)
        {
            missingLayerMessage.append(String.format("\texpected Layer: %s\n", missingLayer));
        }

        Assert.assertTrue(String.format("The following layers were missing from the layers list: %s", missingLayerMessage.toString()),missingLayers.isEmpty());
    }

}
