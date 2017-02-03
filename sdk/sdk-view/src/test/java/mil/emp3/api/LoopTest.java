package mil.emp3.api;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.BasicUtilities;

public class LoopTest extends TestBaseMultiMap {
    private static String TAG = GetOverlayFeatureTest.class.getSimpleName();
    double latitude = 40.2171;
    double longitude = -74.7429;
    MilStdSymbol p1, p1_1, p2, p3;
    IOverlay o1, o2, o3, o4;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        setupMultipleMaps(TAG, 2);
        int count = 0;
        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
        o1 = new Overlay();
        o1.setName("o1");
        o2 = new Overlay();
        o2.setName("o2");
        o3 = new Overlay();
        o3.setName("o3");
        o4 = new Overlay();
        o4.setName("o4");
        count++;
        p1_1 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
        count++;
        p2 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
        count++;
        p3 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void parentFeatureNotYetAdded() throws Exception {
        exception.expect(EMP_Exception.class);
        p1.addFeature(p1_1, true);
    }

    @Test
    public void ParentOverlayNotYetAddedAddFeature() throws EMP_Exception {
        exception.expect(EMP_Exception.class);
        o1.addFeature(p1, true);
    }

    @Test
    public void ParentOverlayNotYetAddedAddOverlay() throws EMP_Exception {
        exception.expect(EMP_Exception.class);
        o1.addOverlay(o2, true);
    }

    @Test
    public void selfFeatureLoop() throws EMP_Exception {
        remoteMap[0].addOverlay(o1, true);
        o1.addFeature(p1, true);
        exception.expect(EMP_Exception.class);
        p1.addFeature(p1, true);
    }
    @Test
    public void simpleFeatureLoop() throws EMP_Exception {
        remoteMap[0].addOverlay(o1, true);
        o1.addFeature(p1, true);
        p1.addFeature(p1_1, true);
        exception.expect(EMP_Exception.class);
        p1_1.addFeature(p1, true);
    }

    @Test
    public void indirectFeatureLoop() throws EMP_Exception {
        remoteMap[0].addOverlay(o1, true);
        o1.addFeature(p1, true);
        p1.addFeature(p1_1, true);
        o1.addFeature(p2, true);
        p2.addFeature(p1, true);
        exception.expect(EMP_Exception.class);
        p1_1.addFeature(p2, true);
    }

    @Test
    public void selfOverlayLoop() throws EMP_Exception {
        remoteMap[0].addOverlay(o1, true);
        exception.expect(EMP_Exception.class);
        o1.addOverlay(o1, true);
    }

    @Test
    public void simpleOverlayLoop() throws EMP_Exception {
        remoteMap[0].addOverlay(o1, true);
        o1.addOverlay(o2, true);
        exception.expect(EMP_Exception.class);
        o2.addOverlay(o1, true);
    }

    @Test
    public void indirectOverlayLoop() throws EMP_Exception {
        remoteMap[0].addOverlay(o1, true);
        remoteMap[1].addOverlay(o2, true);
        o2.addOverlay(o3, true);
        o1.addOverlay(o2, true);
        exception.expect(EMP_Exception.class);
        o3.addOverlay(o1, true);
    }
}
