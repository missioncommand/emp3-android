package mil.emp3.api;

import android.util.Log;

import junit.framework.Assert;

import org.cmapi.primitives.GeoStrokeStyle;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.events.FeatureEditEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.listeners.IFeatureEditEventListener;
import mil.emp3.api.utils.BasicUtilities;
import mil.emp3.api.utils.FeatureUtils;

/**
 * TODO add tests similar to DrawFeatureTest
 */

public class EditFeatureTest extends TestBaseMultiMap {
    private static String TAG = EditFeatureTest.class.getSimpleName();
    double latitude = 40.2171;
    double longitude = -74.7429;
    MilStdSymbol p1;
    IOverlay o1;
    double delta = .00001;
    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        setupMultipleMaps(TAG, 1);
    }

    @After
    public void tearDown() throws Exception {
        if(remoteMap[0].getEditorMode() == EditorMode.EDIT_MODE) {
            remoteMap[0].cancelEdit();
        }
    }

    public class FeatureEditorListener implements IEditEventListener {

        IFeature feature;
        List<String> eventReceived = new ArrayList<>();
        boolean testingExceptions = false;

        public FeatureEditorListener(IFeature feature) {
            this.feature = feature;
        }
        public FeatureEditorListener(IFeature feature, boolean testingExceptions) {
            this.feature = feature;
            this.testingExceptions = testingExceptions;
        }

        String getNextEventReceived() {

            if((null == eventReceived) || (0 == eventReceived.size())) {
                return null;
            }
            String nextEvent = eventReceived.get(0);
            eventReceived.remove(0);
            return nextEvent;
        }

        int getEventReceivedCount() {
            if(null == eventReceived) {
                return 0;
            } else {
                return eventReceived.size();
            }
        }

        @Override
        public void onEditStart(IMap map) {
            Log.d(TAG, "Edit Start.");
            eventReceived.add("onEditStart");
            if(testingExceptions) {
                throw new IllegalStateException("onEditStart");
            }
        }

        @Override
        public void onEditUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "Edit Update.");
            // eventReceived.add("onEditUpdate");
            int[] aIndexes;
            String temp;

            for (IEditUpdateData updateData: updateList) {
                eventReceived.add("onEditUpdate." + updateData.getUpdateType().toString());
                switch (updateData.getUpdateType()) {
                    case COORDINATE_ADDED:
                    case COORDINATE_MOVED:
                    case COORDINATE_DELETED:
                        aIndexes = updateData.getCoordinateIndexes();
                        temp = "";
                        for (int index = 0; index < aIndexes.length; index++) {
                            if (temp.length() > 0) {
                                temp += ",";
                            }
                            temp += aIndexes[index];
                        }
                        Log.i(TAG, "   Edit Update " + updateData.getUpdateType().name() + " indexes:{" + temp + "}");
                        break;
                    case MILSTD_MODIFIER_UPDATED:
                        Log.i(TAG, "   Edit Update " + updateData.getUpdateType().name() + " modifier: " + updateData.getChangedModifier().name() + " {" + ((MilStdSymbol) oFeature).getStringModifier(updateData.getChangedModifier()) + "}");
                        break;
                    case ACM_ATTRIBUTE_UPDATED:
                        Log.i(TAG, "   Edit Update " + updateData.getUpdateType().name() + " ACM attribute:{" + updateData.getChangedModifier().name() + "}");
                        break;
                }
            }
            if(testingExceptions) {
                throw new IllegalStateException("onEditUpdate");
            }
        }

        @Override
        public void onEditComplete(IMap map, IFeature feature) {
            Log.d(TAG, "Edit Complete.");
            eventReceived.add("onEditComplete");
//            updaterThread.interrupt();
            if(testingExceptions) {
                throw new IllegalStateException("onEditComplete");
            }

        }

        @Override
        public void onEditCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "Edit Canceled.");
            eventReceived.add("onEditCancel");
            if(testingExceptions) {
                throw new IllegalStateException("onEditCancel");
            }
        }

        @Override
        public void onEditError(IMap map, String errorMessage) {
            Log.d(TAG, "Edit Error.");
            eventReceived.add("onEditError");
            if(testingExceptions) {
                throw new IllegalStateException("onEditError");
            }
        }
    }

    private void addFeatureToMap(IFeature feature) throws Exception {

        o1 = new Overlay();
        o1.setName("o1");

        remoteMap[0].addOverlay(o1, true);
        o1.addFeature(feature, true);

        Assert.assertTrue("Feature feature should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(feature));
    }

    /**
     * Verifies state of the map after editFeature is executed, verifies that onEditStart was received and feature was added
     * to MapInstance.
     *
     * @param fel
     * @throws Exception
     */
    private void postEditFeature(FeatureEditorListener fel) throws Exception {
        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be DRAW_MODE", EditorMode.EDIT_MODE, remoteMap[0].getEditorMode());

        Assert.assertEquals("Expecting one event after editFeature", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditStart after editFeature", "onEditStart", fel.getNextEventReceived());
    }

    /**
     * Testing illegal operations in Draw Mode. Not using JUNIT Exception feature as message matching is not working, additionally
     * I can do this all in one test if I do it manually.
     * @throws Exception
     */

    @Test
    public void illegalOperationInEditMode() throws Exception {
        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + 0, new UUID(1, 1), latitude + (0 * .001), longitude + (0 * .001));
        addFeatureToMap(p1);
        remoteMap[0].editFeature(p1);

        boolean exceptionGood = false;
        try {
            Polygon polygon = FeatureUtils.setupDrawPolygon();
            remoteMap[0].drawFeature(polygon);
        } catch (EMP_Exception e) {
            if(e.getMessage().equals("Map already in edit/draw mode or locked UNLOCKED")) {
                exceptionGood = true;
            }
        } finally {
            Assert.assertTrue("drawFeature Expected EM_Exception Map already in edit/draw mode or locked UNLOCKED", exceptionGood);
        }

        exceptionGood = false;
        try {
            remoteMap[0].drawFreehand(new GeoStrokeStyle());
        } catch (EMP_Exception e) {
            if(e.getMessage().equals("Map already in edit/draw mode or locked UNLOCKED")) {
                exceptionGood = true;
            }
        } finally {
            Assert.assertTrue("drawFreehand Expected EM_Exception Map already in edit/draw mode or locked UNLOCKED", exceptionGood);
        }

        exceptionGood = false;
        try {
            remoteMap[0].setMotionLockMode(MapMotionLockEnum.LOCKED);
        } catch (EMP_Exception e) {
            if(e.getMessage().equals("setLockMode is not allowed when in Draw/Edit mode")) {
                exceptionGood = true;
            }
        } finally {
            Assert.assertTrue("setMotionLockMode LOCKED Expected EM_Exception setLockMode is not allowed when in Draw/Edit mode", exceptionGood);
        }

        exceptionGood = false;
        try {
            remoteMap[0].setMotionLockMode(MapMotionLockEnum.UNLOCKED);
        } catch (EMP_Exception e) {
            if(e.getMessage().equals("setLockMode is not allowed when in Draw/Edit mode")) {
                exceptionGood = true;
            }
        } finally {
            Assert.assertTrue("setMotionLockMode UNLOCKED Expected EM_Exception setLockMode is not allowed when in Draw/Edit mode", exceptionGood);
        }

        exceptionGood = false;
        try {
            remoteMap[0].setMotionLockMode(MapMotionLockEnum.SMART_LOCK);
        } catch (EMP_Exception e) {
            if(e.getMessage().equals("setLockMode is not allowed when in Draw/Edit mode")) {
                exceptionGood = true;
            }
        } finally {
            Assert.assertTrue("setMotionLockMode SMART_LOCK Expected EM_Exception setLockMode is not allowed when in Draw/Edit mode", exceptionGood);
        }
    }

    /**
     * If feature under edit is removed then edit mode needs to be cancelled.
     * @throws Exception
     */

    @Test
    public void removeWhileInEditMode() throws Exception {
        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + 0, new UUID(1, 1), latitude + (0 * .001), longitude + (0 * .001));
        FeatureEditorListener fel = new FeatureEditorListener(p1);

        addFeatureToMap(p1);

        remoteMap[0].editFeature(p1, fel);
        Thread.sleep(10);
        postEditFeature(fel);

        o1.removeFeature(p1);

        Assert.assertEquals("Expecting one event after removeFeature", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawStart after onEditCancel", "onEditCancel", fel.getNextEventReceived());

        org.junit.Assert.assertTrue("Feature p1 should be removed from remoteMap[0]", mapInstance[0].validateRemoveFeatures(p1.getGeoId()));

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be INACTIVE", EditorMode.INACTIVE, remoteMap[0].getEditorMode());
    }

    @Test
    public void userDragsFeatureAndCompletes() throws Exception {
        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + 0, new UUID(1, 1), latitude + (0 * .001), longitude + (0 * .001));
        FeatureEditorListener fel = new FeatureEditorListener(p1);

        addFeatureToMap(p1);

        remoteMap[0].editFeature(p1, fel);
        Thread.sleep(10);
        postEditFeature(fel);

        Log.d(TAG, "simulateFeatureDrag");
        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG, p1, latitude, longitude, 0, latitude+.1, longitude-.1, 0);
        Log.d(TAG, "done simulateFeatureDrag");

        Thread.sleep(1000,0);
        Assert.assertTrue("Feature feature should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertEquals("Expecting one event after DRAG FEATURE", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditUpdate after DRAG FEATURE", "onEditUpdate." + FeatureEditUpdateTypeEnum.COORDINATE_MOVED, fel.getNextEventReceived());

        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG_COMPLETE, p1, 40.1, -40.1, 0, 40.2, -40.2, 0);
        Assert.assertEquals("Expecting one event after dragFeature", 0, fel.getEventReceivedCount());

        remoteMap[0].completeEdit();

        Assert.assertEquals("Expecting one event after completeEdit", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditComplete after completeEdit", "onEditComplete", fel.getNextEventReceived());

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be INACTIVE", EditorMode.INACTIVE, remoteMap[0].getEditorMode());

        Assert.assertEquals("Expected Altitude ", latitude+.1, p1.getPosition().getLatitude(), delta);
        Assert.assertEquals("Expected Longitude ", longitude-.1, p1.getPosition().getLongitude(), delta);
    }

    @Test
    public void userDragsFeatureAndCancels() throws Exception {
        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + 0, new UUID(1, 1), latitude + (0 * .001), longitude + (0 * .001));
        FeatureEditorListener fel = new FeatureEditorListener(p1);

        addFeatureToMap(p1);

        remoteMap[0].editFeature(p1, fel);
        Thread.sleep(10);
        postEditFeature(fel);

        Log.d(TAG, "simulateFeatureDrag");
        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG, p1, latitude, longitude, 0, latitude+.1, longitude-.1, 0);
        Log.d(TAG, "done simulateFeatureDrag");

        Thread.sleep(1000,0);
        Assert.assertTrue("Feature feature should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertEquals("Expecting one event after DRAG FEATURE", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditUpdate after DRAG FEATURE", "onEditUpdate." + FeatureEditUpdateTypeEnum.COORDINATE_MOVED, fel.getNextEventReceived());

        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG_COMPLETE, p1, 40.1, -40.1, 0, 40.2, -40.2, 0);
        Assert.assertEquals("Expecting one event after dragFeature", 0, fel.getEventReceivedCount());

        remoteMap[0].cancelEdit();

        Assert.assertEquals("Expecting one event after completeEdit", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditCancel after cancelEdit", "onEditCancel", fel.getNextEventReceived());

        Thread.sleep(1000,0);
        Assert.assertTrue("Feature feature should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be INACTIVE", EditorMode.INACTIVE, remoteMap[0].getEditorMode());

        Assert.assertEquals("Expected Altitude ", latitude, p1.getPosition().getLatitude(), delta);
        Assert.assertEquals("Expected Longitude ", longitude, p1.getPosition().getLongitude(), delta);
    }

    @Test
    public void userClicksOnMapAndCompletes() throws Exception {
        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + 0, new UUID(1, 1), latitude + (0 * .001), longitude + (0 * .001));
        FeatureEditorListener fel = new FeatureEditorListener(p1);

        addFeatureToMap(p1);

        remoteMap[0].editFeature(p1, fel);
        Thread.sleep(10);
        postEditFeature(fel);

        Log.d(TAG, "simulate CLICK on Map");
        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.CLICKED, null, latitude+1.0, longitude-1.0,0);
        Log.d(TAG, "done simulate CLICK on Map");

        Thread.sleep(1000,0);
        Assert.assertTrue("Feature feature should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));

        Assert.assertEquals("Expecting one event after CLICK on Map", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditUpdate after CLICK on Map", "onEditUpdate." + FeatureEditUpdateTypeEnum.COORDINATE_MOVED, fel.getNextEventReceived());

        remoteMap[0].completeEdit();

        Assert.assertEquals("Expecting one event after completeEdit", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditComplete after completeEdit", "onEditComplete", fel.getNextEventReceived());

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be INACTIVE", EditorMode.INACTIVE, remoteMap[0].getEditorMode());

        Assert.assertEquals("Expected Altitude ", latitude+1.0, p1.getPosition().getLatitude(), delta);
        Assert.assertEquals("Expected Longitude ", longitude-1.0, p1.getPosition().getLongitude(), delta);
    }

    /**
     * This is a copy of userDragsFeatureAndCompletes but aplication trows exceptions within the callbacks.
     * @throws Exception
     */
    @Test
    public void applicationThrowsException() throws Exception {
        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + 0, new UUID(1, 1), latitude + (0 * .001), longitude + (0 * .001));
        FeatureEditorListener fel = new FeatureEditorListener(p1, true);

        addFeatureToMap(p1);

        remoteMap[0].editFeature(p1, fel);
        Thread.sleep(10);
        postEditFeature(fel);

        Log.d(TAG, "simulateFeatureDrag");
        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG, p1, latitude, longitude, 0, latitude+.1, longitude-.1, 0);
        Log.d(TAG, "done simulateFeatureDrag");

        Thread.sleep(1000,0);
        Assert.assertTrue("Feature feature should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertEquals("Expecting one event after DRAG FEATURE", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditUpdate after DRAG FEATURE", "onEditUpdate." + FeatureEditUpdateTypeEnum.COORDINATE_MOVED, fel.getNextEventReceived());

        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG_COMPLETE, p1, 40.1, -40.1, 0, 40.2, -40.2, 0);
        Assert.assertEquals("Expecting one event after dragFeature", 0, fel.getEventReceivedCount());

        remoteMap[0].completeEdit();

        Assert.assertEquals("Expecting one event after completeEdit", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditComplete after completeEdit", "onEditComplete", fel.getNextEventReceived());

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be INACTIVE", EditorMode.INACTIVE, remoteMap[0].getEditorMode());

        Assert.assertEquals("Expected Altitude ", latitude+.1, p1.getPosition().getLatitude(), delta);
        Assert.assertEquals("Expected Longitude ", longitude-.1, p1.getPosition().getLongitude(), delta);
    }

    class FeatureEditEventListener implements IFeatureEditEventListener {
        int featureEditEventListener = 0;
        @Override
        public void onEvent(FeatureEditEvent event) {
            featureEditEventListener++;
            throw new IllegalStateException("IFeatureEditEventListener onEvent");
        }
    }

    /**
     * This is a copy of userDragsFeatureAndCompletes, but IFeatureEditEventListener is also invoked.
     * Listener also throws an exception, which should be simply caught and logged.
     * @throws Exception
     */
    @Test
    public void testFeatureEditEventListener() throws Exception {
        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + 0, new UUID(1, 1), latitude + (0 * .001), longitude + (0 * .001));
        FeatureEditorListener fel = new FeatureEditorListener(p1);
        FeatureEditEventListener feel = new FeatureEditEventListener();
        remoteMap[0].addFeatureEditEventListener(feel);

        addFeatureToMap(p1);

        remoteMap[0].editFeature(p1, fel);
        Thread.sleep(10);
        postEditFeature(fel);

        Log.d(TAG, "simulateFeatureDrag");
        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG, p1, latitude, longitude, 0, latitude+.1, longitude-.1, 0);
        Log.d(TAG, "done simulateFeatureDrag");

        Thread.sleep(1000,0);
        Assert.assertTrue("Feature feature should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(p1));
        Assert.assertEquals("Expecting one event after DRAG FEATURE", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditUpdate after DRAG FEATURE", "onEditUpdate." + FeatureEditUpdateTypeEnum.COORDINATE_MOVED, fel.getNextEventReceived());

        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG_COMPLETE, p1, 40.1, -40.1, 0, 40.2, -40.2, 0);
        Assert.assertEquals("Expecting one event after dragFeature", 0, fel.getEventReceivedCount());

        remoteMap[0].completeEdit();

        Assert.assertEquals("Expecting one event after completeEdit", 1, fel.getEventReceivedCount());
        Assert.assertEquals("Expecting onEditComplete after completeEdit", "onEditComplete", fel.getNextEventReceived());

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be INACTIVE", EditorMode.INACTIVE, remoteMap[0].getEditorMode());

        Assert.assertEquals("Expected Altitude ", latitude+.1, p1.getPosition().getLatitude(), delta);
        Assert.assertEquals("Expected Longitude ", longitude-.1, p1.getPosition().getLongitude(), delta);

        Log.d(TAG, "FeatureEditEventListener invoked " + feel.featureEditEventListener);
        Assert.assertEquals("FeatureEditEventListener invoked", 3, feel.featureEditEventListener );
    }
}
