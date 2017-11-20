package mil.emp3.api;

import android.util.Log;

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
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.BasicUtilities;
@RunWith(RobolectricTestRunner.class)
public class RemoveTest extends TestBaseMultiMap {
    private static String TAG = RemoveTest.class.getSimpleName();
    double latitude = 40.2171;
    double longitude = -74.7429;
    MilStdSymbol p1, p1_1, p2, p3;
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

        // Both maps should display feature p1
        Assert.assertTrue("Feature p1 and p1_1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1, p1_1));
        Assert.assertTrue("Feature p1 and p1_1 should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(p1, p1_1));

        o1.removeFeature(p1);
        Log.d(TAG, "Feature removed from o1 visible on m2 only ");
        Assert.assertTrue("Feature p1 and p1_1 should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p1.getGeoId(), p1_1.getGeoId()));
        Assert.assertTrue("Feature p1 and p1_1 should NOT be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));

        o2.removeFeature(p1);
        Log.d(TAG, "Feature p1 removed from o2 still visible on m2 only ");
        Assert.assertTrue("Nothing should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(new java.util.ArrayList<UUID>()));
        Assert.assertTrue("Feature p1 should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(p1.getGeoId()));

        o2.removeFeature(p1_1);
        Log.d(TAG, "Feature p1_1 removed from o2 all features should be gone ");
        Assert.assertTrue("Nothing should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(new java.util.ArrayList<UUID>()));
        Assert.assertTrue("Feature p1_1 should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(p1_1.getGeoId()));

        Log.d(TAG, "Ending test0");
    }

    // At the end of the test no features should be on the screen
    @Test
    public void test1() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting Test1\n");

        remoteMap[0].addOverlay(o1, true);
        o1.addFeature(p1, true);
        p1.addFeature(p2, true);
        Assert.assertTrue("Feature p1 and p2 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1, p2));

        p1.removeFeature(p2);
        Assert.assertTrue("Feature p2 should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p2.getGeoId()));

        o1.removeFeature(p1);
        Assert.assertTrue("Feature p1 should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p1.getGeoId()));

        o1.addFeature(p1, true);
        p1.addFeature(p2, true);
        Assert.assertTrue("Feature p1 and p2 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1, p2));

        o1.removeFeature(p1);
        Assert.assertTrue("Feature p1 and p2 should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p1.getGeoId(), p2.getGeoId()));

        Log.d(TAG, "Ending Test1\n");
    }

    // A feature is added to another feature and a an overlay and then simply removed from the parent feature.
    // It should still remain visible. Issue in StorageObjectWrapper addParent was fixed.

    @Test
    public void test2() throws EMP_Exception, InterruptedException {

        Log.d(TAG, "Starting Test2\n");

        remoteMap[0].addOverlay(o1, true);
        remoteMap[0].addOverlay(o2, true);

        o1.addFeature(p1, true);
        Assert.assertTrue("Feature p1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertTrue("Nothing should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(new java.util.ArrayList<IFeature>()));

        p1.addFeature(p2, true);
        Assert.assertTrue("Feature p2 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p2));
        Assert.assertTrue("Nothing should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(new java.util.ArrayList<IFeature>()));

        o2.addFeature(p2, true);
        Assert.assertTrue("Nothing should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(new java.util.ArrayList<IFeature>()));
        Assert.assertTrue("Nothing should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(new java.util.ArrayList<IFeature>()));

        p1.removeFeature(p2);
        Assert.assertTrue("Nothing should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(new java.util.ArrayList<UUID>()));
        Assert.assertTrue("Nothing should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));

        o2.removeFeature(p2);
        Assert.assertTrue("Feature p2 should be Removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p2.getGeoId()));
        Assert.assertTrue("Nothing should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));

        o1.removeFeature(p1);
        Assert.assertTrue("Feature p1 should be Removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p1.getGeoId()));
        Assert.assertTrue("Nothing should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));

        Log.d(TAG, "Ending Test2\n");
    }

    // Remove a list of features where some in the list may have parent children relationship.

    @Test
    public void test3()  throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting Test3\n");

        remoteMap[0].addOverlay(o1, true);
        remoteMap[0].addOverlay(o2, true);

        o1.addFeature(p1, true);
        Assert.assertTrue("Feature p1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertTrue("Nothing should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(new java.util.ArrayList<IFeature>()));

        p1.addFeature(p2, true);
        Assert.assertTrue("Feature p2 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p2));
        Assert.assertTrue("Nothing should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(new java.util.ArrayList<IFeature>()));

        o2.addFeature(p2, true);
        Assert.assertTrue("Nothing should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(new java.util.ArrayList<IFeature>()));
        Assert.assertTrue("Nothing should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(new java.util.ArrayList<IFeature>()));

        List<IFeature> features = o1.getFeatures();
        Assert.assertEquals("o1 should have ywo features", features.size(), 2);
        List<String> featureNameList = new ArrayList<>();
        for(IFeature feature: features) {
            featureNameList.add(feature.getName());
        }
        Assert.assertTrue("TRUCK0", featureNameList.contains("TRUCK0"));
        Assert.assertTrue("TRUCK2", featureNameList.contains("TRUCK2"));

        List<IFeature> list = new ArrayList<>();
        list.add(p1); list.add(p2);

        boolean gotException = false;
        try {
            o1.removeFeatures(list);
        } catch (EMP_Exception e) {
            gotException = true;
        }
        Assert.assertTrue("Got EMP_exception, removing grand child?", gotException);

        list.clear();
        list.add(p1);
        o1.removeFeatures(list);
        Assert.assertTrue("Feature p1 should be Removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p1.getGeoId()));
        Assert.assertTrue("Nothing should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));

        o2.removeFeature(p2);
        Assert.assertTrue("Feature p2 should be Removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p2.getGeoId()));
        Assert.assertTrue("Nothing should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));

        Log.d(TAG, "Ending Test3\n");
    }

    /*
        m1 -> o1, o1 -> o2, o3
        m2 -> o2
        o2 -> o3
        o3 -> p1

     */
    @Test
    public void test4() throws EMP_Exception, InterruptedException {

        Log.d(TAG, "Starting test4");

        remoteMap[0].addOverlay(o1, true);
        remoteMap[1].addOverlay(o2, true);
        o1.addOverlay(o3, true);

        o1.addOverlay(o2, true);
        o2.addOverlay(o3, true);
        o3.addFeature(p1, true);
        Assert.assertTrue("Feature p1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertTrue("Feature p1 should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(p1));

        remoteMap[0].removeOverlay(o1);
        Assert.assertTrue("Feature p1 should be Removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p1.getGeoId()));
        Assert.assertTrue("Nothing should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));


        o2.removeOverlay(o3);
        Assert.assertTrue("Nothing should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(new java.util.ArrayList<UUID>()));
        Assert.assertTrue("Feature p1 should be Removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(p1.getGeoId()));

        Log.d(TAG, "Ending test4");
    }

    /*
    m1 -> o1, o1 -> o2, o3
    m2 -> o2
    o2 -> o3
    o3 -> p1

    */
    @Test
    public void test5() throws EMP_Exception, InterruptedException {

        Log.d(TAG, "Starting test4");

        remoteMap[0].addOverlay(o1, true);
        remoteMap[1].addOverlay(o2, true);
        o1.addOverlay(o3, true);
        o1.addOverlay(o2, true);
        o2.addOverlay(o3, true);

        o3.addFeature(p1, true);
        Assert.assertTrue("Feature p1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertTrue("Feature p1 should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(p1));

        o1.removeOverlay(o2);
        Assert.assertTrue("Nothing should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(new java.util.ArrayList<UUID>()));
        Assert.assertTrue("Nothing should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));

        o2.removeOverlay(o3);
        Assert.assertTrue("Nothing should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(new java.util.ArrayList<UUID>()));
        Assert.assertTrue("Feature p1 should be Removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(p1.getGeoId()));

        o1.removeOverlay(o3);
        Assert.assertTrue("Feature p1 should be Removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p1.getGeoId()));
        Assert.assertTrue("Nothing should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));

        Log.d(TAG, "Ending test5");
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
    public void test6() throws EMP_Exception, InterruptedException {
        Log.d(TAG, "Starting test6");

        remoteMap[0].addOverlay(o1, true);
        remoteMap[1].addOverlay(o2, true);
        o1.addOverlay(o3, true);
        o1.addOverlay(o2, true);
        o2.addOverlay(o3, true);

        o3.addFeature(p1, true);
        Assert.assertTrue("Feature p1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertTrue("Feature p1 should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(p1));

        o2.addFeature(p2, true);
        Assert.assertTrue("Feature p2 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p2));
        Assert.assertTrue("Feature p2 should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(p2));

        o1.addFeature(p3, true);
        Assert.assertTrue("Feature p3 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p3));
        Assert.assertTrue("Nothing should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(new java.util.ArrayList<IFeature>()));

        p1.addFeature(p1_1, true);
        Assert.assertTrue("Feature p1_1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1_1));
        Assert.assertTrue("Feature p1_1 should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(p1_1));

        o3.addFeature(p1_1, true);
        Assert.assertTrue("Nothing should be added to remoteMap[0]", mapInstance[1].validateAddFeatures(new java.util.ArrayList<IFeature>()));
        Assert.assertTrue("Nothing should be added to remoteMap[1]", mapInstance[1].validateAddFeatures(new java.util.ArrayList<IFeature>()));

        o1.clearContainer();
        Assert.assertTrue("Feature p1, p1_1, p2, p3 should be Removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p1.getGeoId(), p1_1.getGeoId(),
                p2.getGeoId(), p3.getGeoId()));
        Assert.assertTrue("Nothing should be removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(new java.util.ArrayList<UUID>()));

        o3.clearContainer();
        Assert.assertTrue("Nothing should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(new java.util.ArrayList<UUID>()));
        Assert.assertTrue("Feature p1, p1_1 should be Removed from remoteMap[1]", mapInstance[1].validateRemoveFeatures(p1.getGeoId(), p1_1.getGeoId()));

        Log.d(TAG, "Ending test6");
    }
}
