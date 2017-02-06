package mil.emp3.api;

import android.util.Log;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import mil.emp3.api.enums.FeatureEventEnum;
import mil.emp3.api.events.FeatureEvent;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.api.utils.BasicUtilities;

/**
 * Test for selectFeature, selectFeatures, deselectFeature, deselectFeatures, clearSelected and IFeatureEventListener
 */
public class SelectFeatureTest extends TestBaseMultiMap {
    private static String TAG = SelectFeatureTest.class.getSimpleName();

    private MilStdSymbol p1, p2, p3;
    private IOverlay o1;
    private int featureCount;

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        double latitude = 40.2171;
        double longitude = -74.7429;

        setupMultipleMaps(TAG, 1);

        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + featureCount++, new UUID(featureCount, featureCount),
                latitude + (featureCount * .001), longitude + (featureCount * .001));
        p2 = BasicUtilities.generateMilStdSymbol("TRUCK" + featureCount++, new UUID(featureCount, featureCount),
                latitude + (featureCount * .001), longitude + (featureCount * .001));
        p3 = BasicUtilities.generateMilStdSymbol("TRUCK" + featureCount++, new UUID(featureCount, featureCount),
                latitude + (featureCount * .001), longitude + (featureCount * .001));

        o1 = new Overlay();
        remoteMap[0].addOverlay(o1, true);
    }

    @After
    public void tearDown() throws Exception {
    }

    class FeatureEventListener implements IFeatureEventListener {
        Map<UUID, FeatureEvent> targetMap = new HashMap<>();

        boolean testingExceptions = false;

        int getEventReceivedCount() {
            if(null == targetMap) {
                return 0;
            } else {
                return targetMap.size();
            }
        }

        boolean validateEvent(IFeature target, FeatureEventEnum eventEnum) {

            if(targetMap.containsKey(target.getGeoId())) {
                FeatureEvent targetEvent = targetMap.remove(target.getGeoId());
                if(targetEvent.getEvent().equals(eventEnum)) {
                    return true;
                }
            }
            return false;
        }
        FeatureEventListener() { }
        FeatureEventListener(boolean testingExceptions) {
            this.testingExceptions = testingExceptions;
        }

        @Override
        public void onEvent(FeatureEvent event) {
            Log.d(TAG, "onEvent " + event.getEvent().toString());
            targetMap.put(event.getTarget().getGeoId(), event);

            if(testingExceptions) {
                throw new IllegalStateException(event.getEvent().toString());
            }
        }
    }

    @Test
    public void selectSingleTest() throws Exception {
        o1.addFeature(p1, true);
        o1.addFeature(p2, true);
        o1.addFeature(p3, true);
        Thread.sleep(1000, 0);

        Assert.assertTrue("Feature p1, p2, p3 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1, p2, p3));

        FeatureEventListener fel = new FeatureEventListener();
        remoteMap[0].addFeatureEventListener(fel);

        remoteMap[0].selectFeature(p1);
        mapInstance[0].validateSelectedFeatures(p1);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after selectFeature", 1, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_SELECTED));

        List<IFeature> listSelected = remoteMap[0].getSelected();
        Assert.assertEquals("Select features count", 1, listSelected.size());
        Assert.assertTrue("Check for p1 in selected list", isInList(p1, listSelected));

        remoteMap[0].deselectFeature(p1);
        mapInstance[0].validateDeselectedFeatures(p1);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after deselectFeature", 1, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_DESELECTED after deselectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_DESELECTED));

        listSelected = remoteMap[0].getSelected();
        Assert.assertTrue("Selected features should be 0", ((null == listSelected) || (0 == listSelected.size())));

    }

    private boolean isInList(IFeature feature, List<IFeature> list) {
        for(IFeature aFeature: list) {
            if(aFeature.getGeoId().equals(feature.getGeoId())) {
                return true;
            }
        }
        return false;
    }

    @Test
    public void selectMultipleTest() throws Exception {
        o1.addFeature(p1, true);
        o1.addFeature(p2, true);
        o1.addFeature(p3, true);
        Thread.sleep(1000, 0);

        Assert.assertTrue("Feature p1, p2, p3 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1, p2, p3));

        FeatureEventListener fel = new FeatureEventListener();
        remoteMap[0].addFeatureEventListener(fel);

        List<IFeature> list = new ArrayList<>();
        list.add(p1); list.add(p2); list.add(p3);
        remoteMap[0].selectFeatures(list);
        mapInstance[0].validateSelectedFeatures(p1, p2, p3);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting three events after selectFeature", 3, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_SELECTED));
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p2, FeatureEventEnum.FEATURE_SELECTED));
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p3, FeatureEventEnum.FEATURE_SELECTED));

        List<IFeature> listSelected = remoteMap[0].getSelected();
        Assert.assertEquals("Select features count", 3, listSelected.size());
        Assert.assertTrue("Check for p1 in selected list", isInList(p1, listSelected));
        Assert.assertTrue("Check for p2 in selected list", isInList(p2, listSelected));
        Assert.assertTrue("Check for p3 in selected list", isInList(p3, listSelected));

        remoteMap[0].deselectFeature(p1);
        mapInstance[0].validateDeselectedFeatures(p1);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after deselectFeature", 1, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_DESELECTED after deselectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_DESELECTED));

        list.clear();
        list.add(p2);
        list.add(p3);

        remoteMap[0].deselectFeatures(list);
        mapInstance[0].validateDeselectedFeatures(p2);
        mapInstance[0].validateDeselectedFeatures(p3);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after deselectFeature", 2, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_DESELECTED after deselectFeature p2", fel.validateEvent(p2, FeatureEventEnum.FEATURE_DESELECTED));
        Assert.assertTrue("Expecting FEATURE_DESELECTED after deselectFeature p3", fel.validateEvent(p3, FeatureEventEnum.FEATURE_DESELECTED));

        list = remoteMap[0].getSelected();
        Assert.assertTrue("Selected features should be 0", ((null == list) || (0 == list.size())));
    }

    /**
     * Deselect feature that was previously not selected, this should result in a noop.
     * @throws Exception
     */
    @Test
    public void deselectNotSelectedTest() throws Exception {
        o1.addFeature(p1, true);
        o1.addFeature(p2, true);
        o1.addFeature(p3, true);
        Thread.sleep(1000, 0);

        Assert.assertTrue("Feature p1, p2, p3 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1, p2, p3));

        FeatureEventListener fel = new FeatureEventListener();
        remoteMap[0].addFeatureEventListener(fel);

        remoteMap[0].selectFeature(p1);
        mapInstance[0].validateSelectedFeatures(p1);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after selectFeature", 1, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_SELECTED));

        List<IFeature> listSelected = remoteMap[0].getSelected();
        Assert.assertEquals("Select features count", 1, listSelected.size());
        Assert.assertTrue("Check for p1 in selected list", isInList(p1, listSelected));

        remoteMap[0].deselectFeature(p2);
        mapInstance[0].validateDeselectedFeatures();
        Assert.assertEquals("Expecting no event after deselectFeature", 0, fel.getEventReceivedCount());

        remoteMap[0].deselectFeature(p1);
        mapInstance[0].validateDeselectedFeatures(p1);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after deselectFeature", 1, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_DESELECTED after deselectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_DESELECTED));

        listSelected = remoteMap[0].getSelected();
        Assert.assertTrue("Selected features should be 0", ((null == listSelected) || (0 == listSelected.size())));
    }

    @Test
    public void clearSelectedTest() throws Exception {
        o1.addFeature(p1, true);
        o1.addFeature(p2, true);
        o1.addFeature(p3, true);

        Assert.assertTrue("Feature p1, p2, p3 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1, p2, p3));

        FeatureEventListener fel = new FeatureEventListener();
        remoteMap[0].addFeatureEventListener(fel);

        List<IFeature> list = new ArrayList<>();
        list.add(p1); list.add(p2); list.add(p3);
        remoteMap[0].selectFeatures(list);
        mapInstance[0].validateSelectedFeatures(p1, p2, p3);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting three events after selectFeature", 3, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_SELECTED));
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p2, FeatureEventEnum.FEATURE_SELECTED));
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p3, FeatureEventEnum.FEATURE_SELECTED));

        List<IFeature> listSelected = remoteMap[0].getSelected();
        Assert.assertEquals("Select features count", 3, listSelected.size());
        Assert.assertTrue("Check for p1 in selected list", isInList(p1, listSelected));
        Assert.assertTrue("Check for p2 in selected list", isInList(p2, listSelected));
        Assert.assertTrue("Check for p3 in selected list", isInList(p3, listSelected));

        Log.d(TAG, "B4 clearSelected");
        remoteMap[0].clearSelected();
        Log.d(TAG, "AFT clearSelected");

        mapInstance[0].validateDeselectedFeatures(p1);
        mapInstance[0].validateDeselectedFeatures(p2);
        mapInstance[0].validateDeselectedFeatures(p3);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting three events after clearSelected", 3, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_DESELECTED after clearSelected p1", fel.validateEvent(p1, FeatureEventEnum.FEATURE_DESELECTED));
        Assert.assertTrue("Expecting FEATURE_DESELECTED after clearSelected p2", fel.validateEvent(p2, FeatureEventEnum.FEATURE_DESELECTED));
        Assert.assertTrue("Expecting FEATURE_DESELECTED after clearSelected p3", fel.validateEvent(p3, FeatureEventEnum.FEATURE_DESELECTED));

        list = remoteMap[0].getSelected();
        Assert.assertTrue("Selected features should be 0", ((null == list) || (0 == list.size())));
    }

    @Test
    public void isSelectedTest() throws Exception {
        o1.addFeature(p1, true);
        o1.addFeature(p2, true);
        o1.addFeature(p3, true);
        Thread.sleep(1000, 0);

        Assert.assertTrue("Feature p1, p2, p3 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1, p2, p3));

        FeatureEventListener fel = new FeatureEventListener();
        remoteMap[0].addFeatureEventListener(fel);

        remoteMap[0].selectFeature(p1);
        mapInstance[0].validateSelectedFeatures(p1);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after selectFeature", 1, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_SELECTED));

        Assert.assertTrue("Feature p1 is selected", remoteMap[0].isSelected(p1));
        Assert.assertFalse("Feature p2 is not selected", remoteMap[0].isSelected(p2));

        remoteMap[0].deselectFeature(p1);
        mapInstance[0].validateDeselectedFeatures(p1);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after deselectFeature", 1, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_DESELECTED after deselectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_DESELECTED));
    }

    /**
     * Event listener throws an exception.
     * @throws Exception
     */
    @Test
    public void selectSingleTestWithException() throws Exception {
        o1.addFeature(p1, true);
        o1.addFeature(p2, true);
        o1.addFeature(p3, true);
        Thread.sleep(1000, 0);

        Assert.assertTrue("Feature p1, p2, p3 should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1, p2, p3));

        FeatureEventListener fel = new FeatureEventListener(true); // Event listener will throw exceptions.
        remoteMap[0].addFeatureEventListener(fel);

        remoteMap[0].selectFeature(p1);
        mapInstance[0].validateSelectedFeatures(p1);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after selectFeature", 1, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_SELECTED after selectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_SELECTED));

        List<IFeature> listSelected = remoteMap[0].getSelected();
        Assert.assertEquals("Select features count", 1, listSelected.size());
        Assert.assertTrue("Check for p1 in selected list", isInList(p1, listSelected));

        remoteMap[0].deselectFeature(p1);
        mapInstance[0].validateDeselectedFeatures(p1);
        Thread.sleep(10);// Let validate finish asynchronously
        Assert.assertEquals("Expecting one event after deselectFeature", 1, fel.getEventReceivedCount());
        Assert.assertTrue("Expecting FEATURE_DESELECTED after deselectFeature", fel.validateEvent(p1, FeatureEventEnum.FEATURE_DESELECTED));

        listSelected = remoteMap[0].getSelected();
        Assert.assertTrue("Selected features should be 0", ((null == listSelected) || (0 == listSelected.size())));

    }
}
