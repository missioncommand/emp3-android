package mil.emp3.api;

import android.util.Log;

import org.cmapi.primitives.IGeoBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IContainer;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.BasicUtilities;

@RunWith(RobolectricTestRunner.class)
public class GetOverlayFeatureTest extends TestBaseMultiMap {
    private static String TAG = GetOverlayFeatureTest.class.getSimpleName();
    double latitude = 40.2171;
    double longitude = -74.7429;
    MilStdSymbol p1, p1_1, p2, p3, p4;
    IOverlay o1, o2, o3;

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
        count++;
        p1_1 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
        count++;
        p2 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
        count++;
        p3 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
        count++;
        p4 = BasicUtilities.generateMilStdSymbol("TRUCK" + count, new UUID(count, count), latitude + (count * .001), longitude + (count * .001));
    }

    @After
    public void tearDown() throws Exception {

    }

    /*
        m1 -> o1 -> p1 -> p1_1
        m2 -> o2 -> p1 -> p1_1
              o2 -> p1_1
     */
    @Test
    public void test0()  throws EMP_Exception, InterruptedException {

        Log.d(TAG, "Starting test0");

        remoteMap[0].addOverlay(o1, true);
        remoteMap[1].addOverlay(o2, true);
        o1.addFeature(p1, true);
        o2.addFeature(p1, true);
        p1.addFeature(p1_1, true);
        o2.addFeature(p1_1, true);

        List<IFeature> features = o1.getFeatures();
        Assert.assertEquals("o1 should have two features", features.size(), 2);
        List<String> featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK0", featureNameList.contains("TRUCK0"));
        Assert.assertTrue("TRUCK1", featureNameList.contains("TRUCK1"));

        features = o2.getFeatures();
        Assert.assertEquals("o2 should have two features", features.size(), 2);
        featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK0", featureNameList.contains("TRUCK0"));
        Assert.assertTrue("TRUCK1", featureNameList.contains("TRUCK1"));

        features = p1.getChildFeatures();
        Assert.assertEquals("p1 should have one features", features.size(), 1);
        featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK1", featureNameList.contains("TRUCK1"));

        features = remoteMap[0].getAllFeatures();
        Assert.assertEquals("remoteMap[0] should have two features", features.size(), 2);
        featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK0", featureNameList.contains("TRUCK0"));
        Assert.assertTrue("TRUCK1", featureNameList.contains("TRUCK1"));

        List<IOverlay> overlays = remoteMap[0].getAllOverlays();
        Assert.assertEquals("remoteMap[0] should have one overlays", overlays.size(), 1);
        List<String> overlayNameList = new ArrayList<>();
        for(IOverlay overlay: overlays) {
            overlayNameList.add(overlay.getName());
        }
        Assert.assertTrue("o1", overlayNameList.contains("o1"));

        overlays = o1.getOverlays();
        Assert.assertEquals("o1 should have zero overlays", overlays.size(), 0);

        Log.d(TAG, "Ending test0");
    }

    /*
       m1 -> o1, o1 -> o2, o3
       m2 -> o2
       o1 -> o3
       o2 -> o3
       o3 -> p1
       o2 -> p2
       o1 -> p3
       p1 -> p1_1

    */
    @Test
    public void test1()   throws EMP_Exception, InterruptedException {

        Log.d(TAG, "Starting test1");

        remoteMap[0].addOverlay(o1, true);
        remoteMap[1].addOverlay(o2, true);
        o1.addOverlay(o3, true);
        o1.addOverlay(o2, true);
        o2.addOverlay(o3, true);
        o3.addFeature(p1, true);
        o2.addFeature(p2, true);
        o1.addFeature(p3, true);
        p1.addFeature(p1_1, true);
        o3.addFeature(p1_1, true);

        List<IFeature> features = remoteMap[0].getAllFeatures();
        Assert.assertEquals("remoteMap[0] should have four features", features.size(), 4);
        List<String> featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK0", featureNameList.contains("TRUCK0"));
        Assert.assertTrue("TRUCK1", featureNameList.contains("TRUCK1"));
        Assert.assertTrue("TRUCK2", featureNameList.contains("TRUCK2"));
        Assert.assertTrue("TRUCK3", featureNameList.contains("TRUCK3"));


        features = remoteMap[1].getAllFeatures();
        Assert.assertEquals("remoteMap[1] should have three features", features.size(), 3);
        featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK0", featureNameList.contains("TRUCK0"));
        Assert.assertTrue("TRUCK1", featureNameList.contains("TRUCK1"));
        Assert.assertTrue("TRUCK2", featureNameList.contains("TRUCK2"));

        features = o1.getFeatures();
        Assert.assertEquals("o1 should have four features", features.size(), 4);
        featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK0", featureNameList.contains("TRUCK0"));
        Assert.assertTrue("TRUCK1", featureNameList.contains("TRUCK1"));
        Assert.assertTrue("TRUCK2", featureNameList.contains("TRUCK2"));
        Assert.assertTrue("TRUCK3", featureNameList.contains("TRUCK3"));

        features = o2.getFeatures();
        Assert.assertEquals("o2 should have three features", features.size(), 3);
        featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK0", featureNameList.contains("TRUCK0"));
        Assert.assertTrue("TRUCK1", featureNameList.contains("TRUCK1"));
        Assert.assertTrue("TRUCK2", featureNameList.contains("TRUCK2"));

        features = o3.getFeatures();
        Assert.assertEquals("o3 should have two features", features.size(), 2);
        featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK0", featureNameList.contains("TRUCK0"));
        Assert.assertTrue("TRUCK1", featureNameList.contains("TRUCK1"));

        java.util.List<IOverlay> overlays = remoteMap[0].getAllOverlays();
        Assert.assertEquals("remoteMap[0] should have three overlays", overlays.size(), 3);
        List<String> overlayNameList = new ArrayList<>();
        for(IOverlay overlay: overlays) {
            overlayNameList.add(overlay.getName());
        }
        Assert.assertTrue("o1", overlayNameList.contains("o1"));
        Assert.assertTrue("o2", overlayNameList.contains("o2"));
        Assert.assertTrue("o3", overlayNameList.contains("o3"));

        overlays = remoteMap[1].getAllOverlays();
        Assert.assertEquals("remoteMap[1] should have two overlays", overlays.size(), 2);
        overlayNameList = new ArrayList<>();
        for(IOverlay overlay: overlays) {
            overlayNameList.add(overlay.getName());
        }
        Assert.assertTrue("o2", overlayNameList.contains("o2"));
        Assert.assertTrue("o3", overlayNameList.contains("o3"));

        overlays = o1.getOverlays();
        Assert.assertEquals("o1 should have two overlays", overlays.size(), 2);
        overlayNameList = new ArrayList<>();
        for(IOverlay overlay: overlays) {
            overlayNameList.add(overlay.getName());
        }
        Assert.assertTrue("o2", overlayNameList.contains("o2"));
        Assert.assertTrue("o3", overlayNameList.contains("o3"));

        overlays = o3.getOverlays();
        Assert.assertEquals("o3 should have zero overlays", overlays.size(), 0);

        java.util.List<IContainer> containers = p1_1.getParents();
        Log.d(TAG, "p1_1 should have two parents one overlay and one feature");
        Assert.assertEquals("p1_1 should have two parents one overlay and one feature", containers.size(), 2);
        boolean foundFeature = false;
        boolean foundOverlay = false;

        for(IContainer container: containers) {
            if(container instanceof IFeature) foundFeature = true;
            if(container instanceof IOverlay) foundOverlay = true;
        }
        Assert.assertTrue("Feature and Overlay as parents", foundFeature & foundOverlay);

        overlays = p1_1.getParentOverlays();
        Assert.assertEquals("p1_1 should have one parent overlay", overlays.size(), 1);
        Assert.assertTrue("Should be instance of IOverlay", overlays.get(0) instanceof IOverlay);

        features = p1_1.getParentFeatures();
        Assert.assertEquals("p1_1 should have one parent feature", features.size(), 1);
        Assert.assertTrue("Should be instance of IFeature", features.get(0) instanceof IFeature);

        Log.d(TAG, "Ending test1");
    }

    @Test
    public void test2()   throws EMP_Exception, InterruptedException {

        Log.d(TAG, "Starting test2");

        remoteMap[0].addOverlay(o1, true);
        remoteMap[1].addOverlay(o2, true);
        o1.addOverlay(o3, true);
        o1.addOverlay(o2, true);
        o2.addOverlay(o3, true);
        o3.addFeature(p1, true);
        o2.addFeature(p2, true);
        o1.addFeature(p3, true);
        p1.addFeature(p1_1, true);
        o3.addFeature(p1_1, true);

        Assert.assertTrue("o1 has children", o1.hasChildren());
        List<IGeoBase> o1Children = o1.getChildren();
        Assert.assertEquals("o1 should have three children", o1Children.size(), 3);
        boolean foundO3 = false;
        boolean foundO2 = false;
        boolean foundP3 = false;
        for(IGeoBase geoBase: o1Children) {
            if(geoBase instanceof IFeature) {
                IFeature f = (IFeature) geoBase;
                if(f.getName().equals("TRUCK3")) foundP3 = true;
            }
            if(geoBase instanceof IOverlay) {
                IOverlay o = (IOverlay) geoBase;
                if(o.getName().equals("o2")) foundO2 = true;
                if(o.getName().equals("o3")) foundO3 = true;
            }
        }
        Assert.assertTrue("foundO3, foundO2, foundP3", foundO2 & foundO3 & foundP3);

        p1_1.addFeature(p4, true);

        List<IFeature> features = p1.getChildFeatures();
        Assert.assertEquals("p1 should have two child features", features.size(), 2);
        boolean foundP1_1 = false;
        boolean foundP4 = false;
        for(IFeature f: features) {
            if(f.getName().equals("TRUCK1")) foundP1_1 = true;
            if(f.getName().equals("TRUCK4")) foundP4 = true;
        }
        Assert.assertTrue("foundP1_1, foundP4", foundP1_1 & foundP4);

        Assert.assertTrue("p1 has children", p1.hasChildren());
        foundP1_1 = false;
        List<IGeoBase> p1Children = p1.getChildren();
        Assert.assertEquals("p1 should have one child", p1Children.size(), 1);
        for(IGeoBase g: p1Children) {
            if(g instanceof IFeature) {
                IFeature f = (IFeature) g;
                if (f.getName().equals("TRUCK1")) foundP1_1 = true;
            }
        }
        Assert.assertTrue("foundP1_1", foundP1_1);

        Assert.assertTrue("remoteMap[0] has children", remoteMap[0].hasChildren());
        p1Children = remoteMap[0].getChildren();
        Assert.assertEquals("remoteMap[0] should have", p1Children.size(), 1);
        boolean foundO1 = false;
        for(IGeoBase g: p1Children) {
            if(g instanceof IOverlay) {
                IOverlay o = (IOverlay) g;
                if (o.getName().equals("o1")) foundO1 = true;
            }
        }
        Assert.assertTrue("foundO1", foundO1);
        Log.d(TAG, "Ending test2");

    }
}
