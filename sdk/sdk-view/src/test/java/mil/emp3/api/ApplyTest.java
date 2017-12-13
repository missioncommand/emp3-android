package mil.emp3.api;

import android.util.Log;

import org.cmapi.primitives.IGeoPosition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import armyc2.c2sd.renderer.utilities.UnitDefTable;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.shadows.ShadowTestRunner;
import mil.emp3.api.shadows.ShadowUnitDefTable;
import mil.emp3.api.utils.BasicUtilities;
@RunWith(ShadowTestRunner.class)
@Config (shadows = {ShadowUnitDefTable.class})
public class ApplyTest extends TestBaseMultiMap {
    private static String TAG = ApplyTest.class.getSimpleName();
    double latitude = 40.2171;
    double longitude = -74.7429;
    MilStdSymbol p1, p1_1, p2, p3;
    IOverlay o1, o2, o3;

    @Before
    public void setUp() throws Exception {
        UnitDefTable.getInstance().init();
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

    private void updateMilStdSymbolPosition(MilStdSymbol symbol, double latitude, double longitude) {
        IGeoPosition oPosition = symbol.getPositions().get(0);
        oPosition.setLatitude(latitude);
        oPosition.setLongitude(longitude);
    }

    @Test
    public void test6() throws EMP_Exception, InterruptedException {

        Log.d(TAG, "Starting test6");

        remoteMap[0].addOverlay(o1, true);
        o1.addFeature(p1,true);
        Assert.assertTrue("Feature p1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));

        updateMilStdSymbolPosition(p1, latitude + .01, longitude + .01);
        p1.apply();
        Thread.sleep(1000, 0);
        Assert.assertTrue("Feature p1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));

        remoteMap[0].setIconSize(IconSizeEnum.TINY);
        Assert.assertTrue("Feature p1 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));

        List<IFeature> list = BasicUtilities.generateMilStdSymbolList(10, latitude, longitude);
        o1.addFeatures(list, true);
        List<IFeature> saveList = new ArrayList<>();
        saveList.addAll(list);
        Assert.assertTrue("list of features should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(list));

        remoteMap[0].setIconSize(IconSizeEnum.LARGE);
        saveList.add(p1);
        Assert.assertTrue("list of features should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(saveList));

        Log.d(TAG, "Ending test6");
    }
}
