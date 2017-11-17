package mil.emp3.api;

import org.cmapi.primitives.IGeoMilSymbol;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.enums.VisibilityStateEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;

//
// NOTE : SymbolUtilities methods are logging NullPointer Exception
//
@RunWith(RobolectricTestRunner.class)
public class BasicAddOverlayAndFeatureTest extends TestBaseSingleMap {

    private static String TAG = BasicAddOverlayAndFeatureTest.class.getSimpleName();

    @Before
    public void setUp() throws Exception {
       setupSingleMap(TAG);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void addOverlayFeature_Test() throws Exception {
        mapInstance.cleanQueues();
        java.util.List<IOverlay> overlays = remoteMap.getAllOverlays();
        Assert.assertNotNull("getAllOverlays returned NULL", overlays);
        Assert.assertEquals("getAllOverlays failed", 0, overlays.size());

        // Add overlay test
        Overlay overlay = new Overlay();
        overlay.setName("addOverlay_Test_1");
        remoteMap.addOverlay(overlay, true);
        overlays = remoteMap.getAllOverlays();
        Assert.assertNotNull("getAllOverlays returned NULL", overlays);
        Assert.assertEquals("getAllOverlays failed", 1, overlays.size());
        Assert.assertTrue("Invalid overlay name", overlays.get(0).getName().compareTo("addOverlay_Test_1") == 0);

        // Add feature test
        java.util.List<IFeature> features = remoteMap.getAllFeatures();

        Assert.assertNotNull("getAllFeatures returned NULL", features);
        Assert.assertEquals("getAllFeatures failed", 0, features.size());

        Feature milStdSymbol = new MilStdSymbol( IGeoMilSymbol.SymbolStandard.MIL_STD_2525B, "SFAPMFF--------");

        milStdSymbol.setName("addFeature_Test_1");

        overlay.addFeature(milStdSymbol, true);

        features = remoteMap.getAllFeatures();
        Assert.assertNotNull("getAllFeatures returned NULL", features);
        Assert.assertEquals("getAllFeatures failed", 1, features.size());
        Assert.assertTrue("Invalid feature name", features.get(0).getName().compareTo("addFeature_Test_1") == 0);

        features = overlay.getFeatures();
        Assert.assertNotNull(" overlay.getFeatures returned NULL", features);
        Assert.assertEquals(" overlay.getFeatures failed", 1, features.size());
        Assert.assertTrue("Invalid feature name", features.get(0).getName().compareTo("addFeature_Test_1") == 0);

        VisibilityStateEnum vse = remoteMap.getVisibility(overlay);
        Assert.assertEquals("overlay visibility on map", VisibilityStateEnum.VISIBLE, vse);

        vse = remoteMap.getVisibility(milStdSymbol);
        Assert.assertEquals("feature visibility on map", VisibilityStateEnum.VISIBLE, vse);

        vse = remoteMap.getVisibility(milStdSymbol, overlay);
        Assert.assertEquals("feature visibility on overlay", VisibilityStateEnum.VISIBLE, vse);

        Assert.assertTrue(mapInstance.validateAddFeatures(milStdSymbol));

        // Turn off overlay visibility, feature should become invisible
        remoteMap.setVisibility(overlay, VisibilityActionEnum.TOGGLE_OFF);

        vse = remoteMap.getVisibility(overlay);
        Assert.assertEquals("overlay visibility on map", VisibilityStateEnum.HIDDEN, vse);

        vse = remoteMap.getVisibility(milStdSymbol);
        Assert.assertEquals("feature visibility on map", VisibilityStateEnum.HIDDEN, vse);

        vse = remoteMap.getVisibility(milStdSymbol, overlay);
        Assert.assertEquals("feature visibility on overlay", VisibilityStateEnum.VISIBLE_ANCESTOR_HIDDEN, vse);
        Assert.assertTrue(mapInstance.validateRemoveFeatures(milStdSymbol.getGeoId()));

    }
}
