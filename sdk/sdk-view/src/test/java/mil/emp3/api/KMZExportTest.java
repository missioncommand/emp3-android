package mil.emp3.api;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;

import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.BasicUtilities;

/**
 * Created by matt.miller@rgi-corp.local on 11/8/17.
 */

public class KMZExportTest extends TestBaseSingleMap {
    private static String TAG = SelectFeatureTest.class.getSimpleName();

    private MilStdSymbol p1, p2, p3;
    private IOverlay o1;
    private int featureCount;

    @Before
    public void setUp() throws Exception {
        double latitude = 40.2171;
        double longitude = -74.7429;

        setupSingleMap(TAG);

        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + featureCount++, new UUID(featureCount, featureCount),
                                                 latitude + (featureCount * .001), longitude + (featureCount * .001));
        p2 = BasicUtilities.generateMilStdSymbol("TRUCK" + featureCount++, new UUID(featureCount, featureCount),
                                                 latitude + (featureCount * .001), longitude + (featureCount * .001));
        p3 = BasicUtilities.generateMilStdSymbol("TRUCK" + featureCount++, new UUID(featureCount, featureCount),
                                                 latitude + (featureCount * .001), longitude + (featureCount * .001));

        o1 = new Overlay();
        remoteMap.addOverlay(o1, true);
    }

    @Test
    public void export() {

    }
}
