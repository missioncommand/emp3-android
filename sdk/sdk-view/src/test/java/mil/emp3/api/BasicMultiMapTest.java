package mil.emp3.api;

import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.BasicUtilities;

public class BasicMultiMapTest extends TestBaseMultiMap {

    private static String TAG = BasicAddOverlayAndFeatureTest.class.getSimpleName();
    double latitude = 40.2171;
    double longitude = -74.7429;

    @Before
    public void setUp() throws Exception {
        setupMultipleMaps(TAG, 2);
    }

    @After
    public void tearDown() throws Exception {

    }

    /*
       m1 -> o1, o1 -> o2, o3
       m2 -> o2
       o2 -> o3
       o3 -> p1
       o2 -> p2
       o1 -> p3
       p1 -> p1_1
    */
    @Test
    public void test1() throws EMP_Exception, InterruptedException {

        Log.d(TAG, "Starting test1");
        int count = 0;
        MilStdSymbol p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
        IOverlay o1 = new Overlay();
        o1.setName("o1");
        IOverlay o2 = new Overlay();
        o2.setName("o2");
        IOverlay o3 = new Overlay();
        o3.setName("o3");
        count++;
        MilStdSymbol p1_1 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
        count++;
        MilStdSymbol p2 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
        count++;
        MilStdSymbol p3 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));


        remoteMap[0].addOverlay(o1, true);
        remoteMap[1].addOverlay(o2, true);
        o1.addOverlay(o3, true);

        // o1.addOverlay(o2, true);
        o2.addOverlay(o3, true);
        o3.addFeature(p1, true);

        // Both maps should display feature p1
        Assert.assertTrue("Feature p1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertTrue("Feature p1 should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(p1));

        o2.addFeature(p2, true);
        List<IFeature> emptyFeatureList = new ArrayList<>();
        Assert.assertTrue("Feature p2 should not be added to remoteMap[0]", mapInstance[0].validateAddFeatures(emptyFeatureList));
        Assert.assertTrue("Feature p2 should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(p2));

        o1.addOverlay(o2, true);
        Assert.assertTrue("Feature p2 should not be added to remoteMap[1], it is already there", mapInstance[1].validateAddFeatures(emptyFeatureList));
        List<IFeature> featureList = new ArrayList<>();
        featureList.add(p1); featureList.add(p2); // p1 is getting re-added, this is inefficient, may need to be fixed
        Assert.assertTrue("Feature p1 (again), p2 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(featureList));

        o1.addFeature(p3, true);
        p1.addFeature(p1_1, true);
        o3.addFeature(p1_1, true);

        List<IFeature> features = remoteMap[0].getAllFeatures();
        Assert.assertEquals("remoteMap[0] should have four features ", 4, features.size());

        features = remoteMap[1].getAllFeatures();
        Assert.assertEquals("remoteMap[1] should have three features ", 3, features.size());

        features = o1.getFeatures();
        Assert.assertEquals("o1 should have four features ", 4, features.size());

        features = o2.getFeatures();
        Assert.assertEquals("o2 should have three features ", 3, features.size());

        features = o3.getFeatures();
        Assert.assertEquals("o3 should have two features ", 2, features.size());

        java.util.List<IOverlay> overlays = remoteMap[0].getAllOverlays();
        Assert.assertEquals("remoteMap[0] should have three overlays ", 3, overlays.size());

        overlays = remoteMap[1].getAllOverlays();
        Assert.assertEquals("remoteMap[1] should have two overlays ", 2, overlays.size());

        overlays = o1.getOverlays();
        Assert.assertEquals("o1 should have two overlays ", 2, overlays.size());

        overlays = o3.getOverlays();
        Assert.assertEquals("o3 should have zero overlays ", 0, overlays.size());

        java.util.List<IContainer> containers = p1_1.getParents();
        Assert.assertEquals("p1_1 should have two parents one overlay and one feature ", 2, containers.size());
        IOverlay parentOverlay = null;
        IFeature parentFeature = null;
        if (containers.get(0) instanceof IFeature) parentFeature = (IFeature) containers.get(0);
        if (containers.get(0) instanceof IOverlay) parentOverlay = (IOverlay) containers.get(0);
        if (containers.get(1) instanceof IFeature) parentFeature = (IFeature) containers.get(1);
        if (containers.get(1) instanceof IOverlay) parentOverlay = (IOverlay) containers.get(1);
        Assert.assertNotNull("parentOverlay should be non-null", parentOverlay);
        Assert.assertNotNull("parentFeature should be non-null", parentFeature);

        overlays = p1_1.getParentOverlays();
        Assert.assertEquals("p1_1 should have one parent overlay ", 1, overlays.size());
        Assert.assertTrue("object should IOverlay ", overlays.get(0) instanceof IOverlay);

        features = p1_1.getParentFeatures();
        Assert.assertEquals("p1_1 should have one parent feature ", 1, features.size());
        Assert.assertTrue("object should IFeature ", features.get(0) instanceof IFeature);

        Log.d(TAG, "Ending test1");
    }
}
