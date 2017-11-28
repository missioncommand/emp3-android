package mil.emp3.api;

import junit.framework.Assert;

import org.cmapi.primitives.IGeoAltitudeMode;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.abstracts.Feature;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.BasicUtilities;

/**
 * NOTE/CAUTION: These tests were written by simply taking the results and putting them back in the test. This was done after
 * results were verified visually on a display. Value of these tests is when some calculation is changed we can make sure that
 * what worked prior to the change continues to work.
 */
@RunWith(RobolectricTestRunner.class)
public class ZoomInTest  extends TestBaseSingleMap {
    private static String TAG = ZoomInTest.class.getSimpleName();

    @Before
    public void setUp() throws Exception {
        setupSingleMap(TAG);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void zoomInTo_singleFeature() throws Exception {
        mapInstance.cleanQueues();

        Overlay overlay = new Overlay();
        overlay.setName("zoomInTo_singleFeature");
        remoteMap.addOverlay(overlay, true);

        Feature milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK0", UUID.randomUUID(), 40.5, -70.5, 0);
        overlay.addFeature(milStdSymbol, true);

        remoteMap.zoomTo(milStdSymbol, false);
        Assert.assertTrue(mapInstance.validateCamera(3709.7837257077094, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 40.5, -70.5, 0.0, 0.0, 0.0));
    }

    @Test
    public void zoomInTo_multipleFeature() throws Exception {
        mapInstance.cleanQueues();

        Overlay overlay = new Overlay();
        overlay.setName("zoomInTo_singleFeature");
        remoteMap.addOverlay(overlay, true);

        Feature milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK0", UUID.randomUUID(), 40.5, -70.5, 0);
        overlay.addFeature(milStdSymbol, true);

        milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK1", UUID.randomUUID(), 40.5, -75.5, 0);
        overlay.addFeature(milStdSymbol, true);

        remoteMap.zoomTo(overlay, false);
        Assert.assertTrue(mapInstance.validateCamera(561281.4843786797, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 40.5269455751329, -72.99999999999999, 0.0, 0.0, 0.0));

        milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK2", UUID.randomUUID(), 35.5, -75.5, 0);
        overlay.addFeature(milStdSymbol, true);
        remoteMap.zoomTo(overlay, false);

        Assert.assertTrue(mapInstance.validateCamera(906907.3383294166, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 38.85721322480032, -73.87215733997421, 0.0, 0.0, 0.0));

        List<IFeature> features = overlay.getFeatures();
        remoteMap.zoomTo(features, false);
        Assert.assertTrue(mapInstance.validateCamera(906907.3383294166, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 38.85721322480032, -73.87215733997421, 0.0, 0.0, 0.0));

    }

    @Test
    public void zoomInTo_FeatureWithNonZeroAltitude() throws Exception {
        mapInstance.cleanQueues();

        Overlay overlay = new Overlay();
        overlay.setName("zoomInTo_FeatureWithNonZeroAltitude");
        remoteMap.addOverlay(overlay, true);

        Feature milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK0", UUID.randomUUID(), 40.5, -70.5, 0);
        overlay.addFeature(milStdSymbol, true);

        milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK1", UUID.randomUUID(), 40.5, -75.5, 0);
        overlay.addFeature(milStdSymbol, true);

        milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK2", UUID.randomUUID(), 35.5, -75.5, 0);
        overlay.addFeature(milStdSymbol, true);
        remoteMap.zoomTo(overlay, false);

        Assert.assertTrue(mapInstance.validateCamera(906907.3383294166, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 38.85721322480032, -73.87215733997421, 0.0, 0.0, 0.0));
        BasicUtilities.updateMilStdSymbolAltitude(milStdSymbol, 300000.0);

        remoteMap.zoomTo(overlay, false);
        Assert.assertTrue(mapInstance.validateCamera(1206907.3383294167, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 38.85721322480032, -73.87215733997421, 0.0, 0.0, 0.0));
    }

    @Test
    public void invalidParameters() throws Exception {
        IOverlay overlay = null;
        remoteMap.zoomTo(overlay, false);
        ICamera expectedCamera = null;
        Assert.assertTrue(mapInstance.validateSetCamera(expectedCamera));

        IFeature feature = null;
        remoteMap.zoomTo(feature, false);
        Assert.assertTrue(mapInstance.validateSetCamera(expectedCamera));

        List<IFeature> features = null;
        remoteMap.zoomTo(features, false);
        Assert.assertTrue(mapInstance.validateSetCamera(expectedCamera));

        overlay = new Overlay();
        overlay.setName("zoomInTo_FeatureWithNonZeroAltitude");
        remoteMap.addOverlay(overlay, true);
        remoteMap.zoomTo(overlay, false);
        Assert.assertTrue(mapInstance.validateSetCamera(expectedCamera));

        features = new ArrayList<>();
        remoteMap.zoomTo(features, false);
        Assert.assertTrue(mapInstance.validateSetCamera(expectedCamera));

        Feature milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK0", UUID.randomUUID(), 40.5, -70.5, 0);
        overlay.addFeature(milStdSymbol, true);

        milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK1", UUID.randomUUID(), 40.5, -75.5, 0);
        overlay.addFeature(milStdSymbol, true);

        remoteMap.zoomTo(overlay, false);
        Assert.assertTrue(mapInstance.validateCamera(561281.4843786797, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 40.5269455751329, -72.99999999999999, 0.0, 0.0, 0.0));
    }

    @Test
    public void zoomInTo_DateLine() throws Exception {
        IOverlay overlay = new Overlay();
        overlay.setName("zoomInTo_FeatureWithNonZeroAltitude");
        remoteMap.addOverlay(overlay, true);

        Feature milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK0", UUID.randomUUID(), 40.1, -179.5, 0);
        overlay.addFeature(milStdSymbol, true);

        milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK1", UUID.randomUUID(), 40.5, 179.5, 0);
        overlay.addFeature(milStdSymbol, true);

        remoteMap.zoomTo(overlay, false);
        Assert.assertTrue(mapInstance.validateCamera(127742.63869044282, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 40.301076190857295, -179.99851980956896, 0.0, 0.0, 0.0));
    }

    @Test
    public void zoomInTo_Pole() throws Exception {
        IOverlay overlay = new Overlay();
        overlay.setName("zoomInTo_FeatureWithNonZeroAltitude");
        remoteMap.addOverlay(overlay, true);

        Feature milStdSymbol = BasicUtilities.generateMilStdSymbol("TRUCK0", UUID.randomUUID(), 90.0, 0.0, 0);
        overlay.addFeature(milStdSymbol, true);
        remoteMap.zoomTo(milStdSymbol, false);
        Assert.assertTrue(mapInstance.validateCamera(2952.931299695943, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, 90.0, 0.0, 0.0, 0.0, 0.0));

        BasicUtilities.updateMilStdSymbolLatLong(milStdSymbol, -90.0, 0.0);
        remoteMap.zoomTo(milStdSymbol, false);
        Assert.assertTrue(mapInstance.validateCamera(2952.931299695943, IGeoAltitudeMode.AltitudeMode.CLAMP_TO_GROUND, -90.0, 0.0, 0.0, 0.0, 0.0));
    }
}
