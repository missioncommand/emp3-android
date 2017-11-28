package mil.emp3.api;

import android.util.Log;

import junit.framework.Assert;

import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoPosition;
import org.cmapi.primitives.IGeoPositionGroup;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.enums.MapMotionLockEnum;
import mil.emp3.api.enums.UserInteractionEventEnum;
import mil.emp3.api.events.MapFreehandEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IFreehandEventListener;
import mil.emp3.api.listeners.IMapFreehandEventListener;
import mil.emp3.api.utils.BasicUtilities;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.FeatureUtils;

/**
 * Some basic FreehandDraw scenarios are tested.
 */
@RunWith(RobolectricTestRunner.class)
public class FreehandDrawTest extends TestBaseMultiMap {
    private static String TAG = FreehandDrawTest.class.getSimpleName();
    double latitude = 40.2171;
    double longitude = -74.7429;
    MilStdSymbol p1;
    IOverlay o1;

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        setupMultipleMaps(TAG, 1);
    }

    @After
    public void tearDown() throws Exception {
        if(remoteMap[0].getEditorMode() == EditorMode.FREEHAND_MODE) {
            remoteMap[0].drawFreehandExit();
        }
    }
    class FreehandDrawListener implements IFreehandEventListener {
        private int iPosCnt = 0;

        List<String> eventReceived = new ArrayList<>();
        boolean testingExceptions = false;

        FreehandDrawListener() { }
        FreehandDrawListener(boolean testingExceptions) {
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
        public void onEnterFreeHandDrawMode(IMap map) {
            Log.d(TAG, "Enter Freehand Draw Mode.");
            eventReceived.add("onEnterFreeHandDrawMode");
            if(testingExceptions) {
                throw new IllegalStateException("onEnterFreeHandDrawMode");
            }
        }

        @Override
        public void onFreeHandLineDrawStart(IMap map, IGeoPositionGroup positionList) {
            Log.d(TAG, "Freehand Draw Start.");
            IGeoPosition oPos;
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("Start Draw Points.\n");
            iPosCnt = 0;
            int iCurrentSize = positionList.getPositions().size();
            for (int iIndex = iPosCnt; iIndex < iCurrentSize; iIndex++) {
                oPos = positionList.getPositions().get(iIndex);
                stringBuilder.append(String.format("[%d] Lat: %2$8.5f Lon: %3$8.5f\n", iIndex, oPos.getLatitude(), oPos.getLongitude()));
            }

            iPosCnt = iCurrentSize;
            eventReceived.add("onFreeHandLineDrawStart." + iPosCnt);
            if(testingExceptions) {
                throw new IllegalStateException("onFreeHandLineDrawStart");
            }
        }

        @Override
        public void onFreeHandLineDrawUpdate(IMap map, IGeoPositionGroup positionList) {
            Log.d(TAG, "Freehand Draw Update.");
            IGeoPosition oPos;
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("");
            int iCurrentSize = positionList.getPositions().size();
            for (int iIndex = iPosCnt; iIndex < iCurrentSize; iIndex++) {
                oPos = positionList.getPositions().get(iIndex);
                stringBuilder.append(String.format("[%d] Lat: %2$8.5f Lon: %3$8.5f\n", iIndex, oPos.getLatitude(), oPos.getLongitude()));
            }

            iPosCnt = iCurrentSize;
            eventReceived.add("onFreeHandLineDrawUpdate." + iPosCnt);
            if(testingExceptions) {
                throw new IllegalStateException("onFreeHandLineDrawUpdate");
            }
        }

        @Override
        public void onFreeHandLineDrawEnd(IMap map, IGeoStrokeStyle style, IGeoPositionGroup positionList) {
            Log.d(TAG, "Freehand Draw Complete.");
            IGeoPosition oPos;
            StringBuilder stringBuilder = new StringBuilder();

            stringBuilder.append("");
            int iCurrentSize = positionList.getPositions().size();
            for (int iIndex = iPosCnt; iIndex < iCurrentSize; iIndex++) {
                oPos = positionList.getPositions().get(iIndex);
                stringBuilder.append(String.format("[%d] Lat: %2$8.5f Lon: %3$8.5f\n", iIndex, oPos.getLatitude(), oPos.getLongitude()));
            }

            iPosCnt = iCurrentSize;
            eventReceived.add("onFreeHandLineDrawEnd." + iPosCnt);
            if(testingExceptions) {
                throw new IllegalStateException("onFreeHandLineDrawEnd");
            }
        }

        @Override
        public void onExitFreeHandDrawMode(IMap map) {
            Log.d(TAG, "Exit Freehand Draw mode.");
            eventReceived.add("onExitFreeHandDrawMode");
            if(testingExceptions) {
                throw new IllegalStateException("onExitFreeHandDrawMode");
            }
        }

        @Override
        public void onDrawError(IMap map, String errorMessage) {
            Log.d(TAG, "Error Freehand Draw..");
            eventReceived.add("onDrawError");
            if(testingExceptions) {
                throw new IllegalStateException("onDrawError");
            }
        }
    }

    private void startFreehandDraw(FreehandDrawListener fdl) throws Exception {

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be INACTIVE", EditorMode.INACTIVE, remoteMap[0].getEditorMode());

        IGeoStrokeStyle strokeStyle = new GeoStrokeStyle();
        IGeoColor geoColor = new EmpGeoColor(1.0, 255, 255, 0);

        strokeStyle.setStrokeColor(geoColor);
        strokeStyle.setStrokeWidth(5);

        remoteMap[0].drawFreehand(strokeStyle, fdl);

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.SMART_LOCK, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be DRAW_MODE", EditorMode.FREEHAND_MODE, remoteMap[0].getEditorMode());

        Assert.assertEquals("Expecting one event after startFreehandDraw", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onEnterFreeHandDrawMode after startFreehandDraw", "onEnterFreeHandDrawMode", fdl.getNextEventReceived());

    }

    private void endFreehandDraw(FreehandDrawListener fdl) throws Exception {

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.SMART_LOCK, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be DRAW_MODE", EditorMode.FREEHAND_MODE, remoteMap[0].getEditorMode());

        remoteMap[0].drawFreehandExit();

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be INACTIVE", EditorMode.INACTIVE, remoteMap[0].getEditorMode());

        Assert.assertEquals("Expecting one event after drawFreehandExit", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onExitFreeHandDrawMode after drawFreehandExit", "onExitFreeHandDrawMode", fdl.getNextEventReceived());
    }

    private void addFeatureToMap(IFeature feature) throws Exception {

        o1 = new Overlay();
        o1.setName("o1");

        remoteMap[0].addOverlay(o1, true);
        o1.addFeature(feature, true);

        Assert.assertTrue("Feature feature should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(feature));
    }
    /**
     * Testing illegal operations in Freehand Draw Mode. Not using JUNIT Exception feature as message matching is not working, additionally
     * I can do this all in one test if I do it manually.
     * @throws Exception
     */

    @Test
    public void illegalOperationInEditMode() throws Exception {

        FreehandDrawListener fdl = new FreehandDrawListener();
        startFreehandDraw(fdl);

        boolean exceptionGood = false;
        try {
            Polygon polygon = FeatureUtils.setupDrawPolygon();
            remoteMap[0].drawFeature(polygon);
        } catch (EMP_Exception e) {
            if(e.getMessage().equals("Map already in edit/draw mode or locked SMART_LOCK")) {
                exceptionGood = true;
            }
        } finally {
            Assert.assertTrue("drawFeature Expected EM_Exception Map already in edit/draw mode or locked SMART_LOCK", exceptionGood);
        }

        exceptionGood = false;
        try {
            p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + 0, new UUID(1, 1), latitude + (0 * .001), longitude + (0 * .001));
            addFeatureToMap(p1);
            remoteMap[0].editFeature(p1);
        } catch (EMP_Exception e) {
            if(e.getMessage().equals("Map already in edit/draw mode or locked SMART_LOCK")) {
                exceptionGood = true;
            }
        } finally {
            Assert.assertTrue("editFeature Expected EM_Exception Map already in edit/draw mode or locked SMART_LOCK", exceptionGood);
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

        endFreehandDraw(fdl);
    }

    /**
     * Testing start and Exit of FreehandDraw without any additional user actions.
     * @throws Exception
     */
    @Test
    public void basicFreehandDraw() throws Exception {
        FreehandDrawListener fdl = new FreehandDrawListener();
        startFreehandDraw(fdl);

        endFreehandDraw(fdl);
    }

    /**
     * First two points generate different events than any following points
     * @param fdl
     * @throws Exception
     */
    private void firstTwoPointsOfLine(FreehandDrawListener fdl) throws Exception {
        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG, 100, 100, 40.0, -40.0, 0.0);
        Assert.assertEquals("Expecting one event after DRAG", 0, fdl.getEventReceivedCount());

        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG, 150, 150, 40.1, -40.1, 0.0);
        Assert.assertEquals("Expecting one event after DRAG", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onFreeHandLineDrawStart after DRAG", "onFreeHandLineDrawStart." + 2, fdl.getNextEventReceived());
    }

    /**
     * Beyond two points same event is generated.
     * @param fdl
     * @param x
     * @param y
     * @param lat
     * @param lon
     * @param alt
     * @throws Exception
     */
    private void beyondFirstTwoPointsOfLine(FreehandDrawListener fdl, int x, int y, double lat, double lon, double alt) throws Exception {
        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG, x, y, lat, lon, alt);
        Assert.assertEquals("Expecting one event after DRAG", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onFreeHandLineDrawUpdate after DRAG", "onFreeHandLineDrawUpdate." + 3, fdl.getNextEventReceived());
    }

    @Test
    public void addTwoPointsToFreehandDraw() throws Exception {
        FreehandDrawListener fdl = new FreehandDrawListener();
        startFreehandDraw(fdl);

        firstTwoPointsOfLine(fdl);

        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE, 100, 100, 40.0, -40.0, 0.0);
        Assert.assertEquals("Expecting one event after DRAG_COMPLETE", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onFreeHandLineDrawEnd after DRAG_COMPLETE", "onFreeHandLineDrawEnd." + 2, fdl.getNextEventReceived());

        endFreehandDraw(fdl);
    }

    @Test
    public void addThreePointsToFreehandDraw() throws Exception {
        FreehandDrawListener fdl = new FreehandDrawListener();
        startFreehandDraw(fdl);

        firstTwoPointsOfLine(fdl);
        beyondFirstTwoPointsOfLine(fdl, 160, 170, 40.2, -40.3, 0);

        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE, 100, 100, 40.0, -40.0, 0.0);
        Assert.assertEquals("Expecting one event after DRAG_COMPLETE", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onFreeHandLineDrawEnd after DRAG_COMPLETE", "onFreeHandLineDrawEnd." + 3, fdl.getNextEventReceived());

        endFreehandDraw(fdl);
    }

    /**
     * User can draw multiple disconnected segments by lifting the finger and putting it back down and dragging again
     * @throws Exception
     */
    @Test
    public void addMultipleLineSegments() throws Exception {
        FreehandDrawListener fdl = new FreehandDrawListener();
        startFreehandDraw(fdl);

        firstTwoPointsOfLine(fdl);

        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE, 100, 100, 40.0, -40.0, 0.0);
        Assert.assertEquals("Expecting one event after DRAG_COMPLETE", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onFreeHandLineDrawEnd after DRAG_COMPLETE", "onFreeHandLineDrawEnd." + 2, fdl.getNextEventReceived());

        firstTwoPointsOfLine(fdl);
        beyondFirstTwoPointsOfLine(fdl, 160, 170, 40.2, -40.3, 0);

        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE, 100, 100, 40.0, -40.0, 0.0);
        Assert.assertEquals("Expecting one event after DRAG_COMPLETE", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onFreeHandLineDrawEnd after DRAG_COMPLETE", "onFreeHandLineDrawEnd." + 3, fdl.getNextEventReceived());

        endFreehandDraw(fdl);
    }

    /**
     * Draws the third point near the edge. This should cause the Editor to change the camera position and pan the map.
     * @throws Exception
     */
    @Test
    public void testSmartLockFreehandDraw() throws Exception {

        ICamera camera = new Camera();
        camera.setLatitude(40.0);
        camera.setLongitude(-40.0);
        remoteMap[0].setCamera(camera, false);
        Assert.assertTrue("Validate setCameraCount on initialization ", mapInstance[0].validateSetCameraCount(1));

        FreehandDrawListener fdl = new FreehandDrawListener();
        startFreehandDraw(fdl);

        firstTwoPointsOfLine(fdl);
        beyondFirstTwoPointsOfLine(fdl, 480, 560, 40.2, -40.3, 0);
        Assert.assertTrue("Validate setCameraCount on initialization ", mapInstance[0].validateSetCameraCount(1));

        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE, 100, 100, 40.0, -40.0, 0.0);
        Assert.assertEquals("Expecting one event after DRAG_COMPLETE", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onFreeHandLineDrawEnd after DRAG_COMPLETE", "onFreeHandLineDrawEnd." + 3, fdl.getNextEventReceived());

        endFreehandDraw(fdl);
    }

    class MapFreehandEventListener implements IMapFreehandEventListener {
        int mapFreehandEventListener = 0;
        @Override
        public void onEvent(MapFreehandEvent event) {
            mapFreehandEventListener++;
            throw new IllegalStateException("IMapFreehandEventListener onEvent");
        }
    }
    /**
     * This is a copy of addMultipleLineSegments but it tests a scenario where application throws an exception in th eevent handler.
     * It also verifies that IMapFreehandEventListener is fired.
     * @throws Exception
     */

    @Test
    public void addMultipleLineSegmentsWithExceptions() throws Exception {
        FreehandDrawListener fdl = new FreehandDrawListener(true);
        MapFreehandEventListener mfel = new MapFreehandEventListener();
        remoteMap[0].addMapFreehandEventListener(mfel);

        startFreehandDraw(fdl);

        firstTwoPointsOfLine(fdl);

        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE, 100, 100, 40.0, -40.0, 0.0);
        Assert.assertEquals("Expecting one event after DRAG_COMPLETE", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onFreeHandLineDrawEnd after DRAG_COMPLETE", "onFreeHandLineDrawEnd." + 2, fdl.getNextEventReceived());

        firstTwoPointsOfLine(fdl);
        beyondFirstTwoPointsOfLine(fdl, 160, 170, 40.2, -40.3, 0);

        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DRAG_COMPLETE, 100, 100, 40.0, -40.0, 0.0);
        Assert.assertEquals("Expecting one event after DRAG_COMPLETE", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onFreeHandLineDrawEnd after DRAG_COMPLETE", "onFreeHandLineDrawEnd." + 3, fdl.getNextEventReceived());

        endFreehandDraw(fdl);

        Log.d(TAG, "IMapFreehandEventListener invoked " + mfel.mapFreehandEventListener);
        Assert.assertEquals("IMapFreehandEventListener invoked", 7, mfel.mapFreehandEventListener);
    }
}
