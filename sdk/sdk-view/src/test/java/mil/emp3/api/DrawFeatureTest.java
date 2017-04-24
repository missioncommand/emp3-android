package mil.emp3.api;

import android.util.Log;

import junit.framework.Assert;

import org.cmapi.primitives.GeoFillStyle;
import org.cmapi.primitives.GeoStrokeStyle;
import org.cmapi.primitives.IGeoAltitudeMode;
import org.cmapi.primitives.IGeoColor;
import org.cmapi.primitives.IGeoFillStyle;
import org.cmapi.primitives.IGeoStrokeStyle;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.StringContains;
import org.hamcrest.core.SubstringMatcher;
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
import mil.emp3.api.events.FeatureDrawEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IFeatureDrawEventListener;
import mil.emp3.api.utils.BasicUtilities;
import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.FeatureUtils;
import mil.emp3.core.editors.ControlPoint;

public class DrawFeatureTest extends TestBaseMultiMap {
    private static String TAG = DrawFeatureTest.class.getSimpleName();
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
        if(remoteMap[0].getEditorMode() == EditorMode.DRAW_MODE) {
            remoteMap[0].cancelDraw();
        }
    }

    /**
     * This Feature DrawListener is common to all tests. It stores the received events. Unit tests retrieve the stored events for verification.
     */
    public class FeatureDrawListener implements IDrawEventListener {

        IFeature feature;
        List<String> eventReceived = new ArrayList<>();
        boolean testingExceptions = false;

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
        public FeatureDrawListener(IFeature feature) {
            this.feature = feature;
        }
        public FeatureDrawListener(IFeature feature, boolean testingExceptions) {
            this.feature = feature;
            this.testingExceptions = testingExceptions;
        }

        @Override
        public void onDrawStart(IMap map) {
            Log.d(TAG, "onDrawStart " + feature.getName());
            eventReceived.add("onDrawStart");
            if(testingExceptions) {
                throw new IllegalStateException("onDrawStart");
            }
        }

        @Override
        public void onDrawUpdate(IMap map, IFeature oFeature, List<IEditUpdateData> updateList) {
            Log.d(TAG, "onDrawUpdate " + feature.getName());
            int[] aIndexes;
            String temp;

            for (IEditUpdateData updateData: updateList) {
                eventReceived.add("onDrawUpdate." + updateData.getUpdateType().toString());
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
                        Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " indexes:{" + temp + "}");
                        break;
                    case MILSTD_MODIFIER_UPDATED:
                        Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " modifier: " + updateData.getChangedModifier().name() + " {" + ((MilStdSymbol) oFeature).getStringModifier(updateData.getChangedModifier()) + "}");
                        break;
                    case ACM_ATTRIBUTE_UPDATED:
                        Log.i(TAG, "   Draw Update " + updateData.getUpdateType().name() + " ACM attribute:{" + updateData.getChangedModifier().name() + "}");
                        break;
                }
            }

            if(testingExceptions) {
                throw new IllegalStateException("onDrawUpdate");
            }
        }

        @Override
        public void onDrawComplete(IMap map, IFeature feature) {
            Log.d(TAG, "onDrawComplete " + feature.getName());
            eventReceived.add("onDrawComplete");

            for(int ii = 0; ii < feature.getPositions().size(); ii++ ) {
                Log.d(TAG, "pos " + ii + " lat/lon " + feature.getPositions().get(ii).getLatitude() + "/" + feature.getPositions().get(ii).getLongitude());
            }
            if(testingExceptions) {
                throw new IllegalStateException("onDrawComplete");
            }
        }

        @Override
        public void onDrawCancel(IMap map, IFeature originalFeature) {
            Log.d(TAG, "onDrawCancel " + originalFeature);
            eventReceived.add("onDrawCancel");
            if(testingExceptions) {
                throw new IllegalStateException("onDrawCancel");
            }
        }

        @Override
        public void onDrawError(IMap map, String errorMessage) {
            Log.d(TAG, "onDrawError ");
            eventReceived.add("onDrawError");
            if(testingExceptions) {
                throw new IllegalStateException("onDrawError");
            }
        }
    }

    /**
     * Setup a polygon to draw and verify current state of the map.
     * @return
     * @throws Exception
     */
    Polygon initializeDraw() throws Exception {
        Polygon polygon = FeatureUtils.setupDrawPolygon();

        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be INACTIVE", EditorMode.INACTIVE, remoteMap[0].getEditorMode());

        return polygon;
    }

    /**
     * Verifies state of the map after drawFeature is executed, verifies that onDrawStart was received and feature was added
     * to MapInstance.
     *
     * @param fdl
     * @param polygon
     * @throws Exception
     */
    private void postDrawFeature(FeatureDrawListener fdl, Polygon polygon) throws Exception {
        Thread.sleep(1000, 0);
        Assert.assertEquals("Map should be UNLOCKED", MapMotionLockEnum.UNLOCKED, remoteMap[0].getMotionLockMode());
        Assert.assertEquals("Editor Mode should be DRAW_MODE", EditorMode.DRAW_MODE, remoteMap[0].getEditorMode());

        Assert.assertEquals("Expecting one event after drawFeature", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawStart after drawFeature", "onDrawStart", fdl.getNextEventReceived());

        Assert.assertTrue("Feature should be added to the map", mapInstance[0].validateAddFeatures(polygon));
    }

    private void addFeatureToMap(IFeature feature) throws Exception {

        o1 = new Overlay();
        o1.setName("o1");

        remoteMap[0].addOverlay(o1, true);
        o1.addFeature(feature, true);

        Assert.assertTrue("Feature feature should be added to remoteMap[0]", mapInstance[0].validateAddFeatures(feature));
    }

    /**
     * Testing illegal operations in Draw Mode. Not using JUNIT Exception feature as message matching is not working, additionally
     * I can do this all in one test if I do it manually.
     * @throws Exception
     */

    @Test
    public void illegalOperationInDraw() throws Exception {
        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon);

        remoteMap[0].drawFeature(polygon, fdl);
        postDrawFeature(fdl, polygon);

        p1 = BasicUtilities.generateMilStdSymbol("TRUCK" + 0, new UUID(1, 1), latitude + (0 * .001), longitude + (0 * .001));
        addFeatureToMap(p1);

        boolean exceptionGood = false;
        try {
            remoteMap[0].editFeature(p1);
        } catch (EMP_Exception e) {
            if(e.getMessage().equals("Map already in edit/draw mode or locked UNLOCKED")) {
                exceptionGood = true;
            }
        } finally {
            Assert.assertTrue("editFeature Expected EM_Exception Map already in edit/draw mode or locked UNLOCKED", exceptionGood);
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
     * Testst cancelDraw interface.
     * @throws Exception
     */
    @Test
    public void drawCancel() throws Exception {

        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon);

        remoteMap[0].drawFeature(polygon, fdl);
        postDrawFeature(fdl, polygon);

        remoteMap[0].cancelDraw();
        Thread.sleep(100);

        Assert.assertEquals("Expecting one event after cancelDraw", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawCancel after cancelDraw", "onDrawCancel", fdl.getNextEventReceived());

        Assert.assertTrue("Feature should be removed from the map", mapInstance[0].validateRemoveFeatures(polygon.getGeoId()));
    }

    /**
     * Tests drawComplete interface.
     * @throws Exception
     */
    @Test
    public void drawComplete() throws Exception {

        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon);

        remoteMap[0].drawFeature(polygon, fdl);
        postDrawFeature(fdl, polygon);

        remoteMap[0].completeDraw();
        Thread.sleep(100);

        Assert.assertEquals("Expecting one event after completeDraw", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawCancel after completeDraw", "onDrawComplete", fdl.getNextEventReceived());

        Assert.assertTrue("Feature should be removed from the map", mapInstance[0].validateRemoveFeatures(polygon.getGeoId()));
    }

    /**
     * Builds a list of class names. This is used to verify that specific class names (features) were added to the
     * map. In case of drawFeature we won't know the geoId of the ControlPoints ahead of time so we only check that specific
     * number of features with class name of ControlPoint were added.
     * @param includePolygon
     * @param countControlPoints
     * @return
     */
    private List<String> buildFeatureTypesList(boolean includePolygon, int countControlPoints) {
        List<String> featureTypes = new ArrayList<>();
        if(includePolygon) {
            featureTypes.add(Polygon.class.getName());
        }
        for(int ii = 0; ii < countControlPoints; ii++) {
            featureTypes.add(ControlPoint.class.getName());
        }
        return featureTypes;
    }

    private List<String> buildFeatureTypesList(int countPolygons, int countControlPoints) {
        List<String> featureTypes = new ArrayList<>();

        for(int ii = 0; ii < countPolygons; ii++) {
            featureTypes.add(Polygon.class.getName());
        }
        for(int ii = 0; ii < countControlPoints; ii++) {
            featureTypes.add(ControlPoint.class.getName());
        }
        return featureTypes;
    }

    /**
     * Simulates a CLICKED gesture which then should result in a COORDINATE_ADDED event.
     *
     * @param fdl
     * @param lat
     * @param lon
     * @param alt
     * @throws Exception
     */
    private void addCoordinate(FeatureDrawListener fdl, double lat, double lon, double alt) throws Exception {
        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.CLICKED, null, lat, lon,alt);
        Thread.sleep(10);
        Assert.assertEquals("Expecting one event after simulateUserInteractionEvent", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawUpdate after simulateUserInteractionEvent", "onDrawUpdate." + FeatureEditUpdateTypeEnum.COORDINATE_ADDED.toString(), fdl.getNextEventReceived());
    }

    /**
     * Add three coordinates to build a triangle. Along the way verifies that all the proper events were generated and proper features were added
     * to the MapInstance.
     *
     * @param fdl
     * @return
     * @throws Exception
     */
    private List<IFeature> buildTriangle(FeatureDrawListener fdl) throws Exception {
        addCoordinate(fdl, 40.0, -40.0, 0.0);

        List<String> featureTypes = buildFeatureTypesList(true, 1);
        Thread.sleep(1000,0);
        Assert.assertTrue(mapInstance[0].validateAddTypes(featureTypes));

        addCoordinate(fdl, 40.0, -41.0, 0.0);
        featureTypes = buildFeatureTypesList(true, 3);
        Thread.sleep(1000,0);
        Assert.assertTrue(mapInstance[0].validateAddTypes(featureTypes));

        addCoordinate(fdl, 40.5, -40.5, 0.0);
        List<IFeature> cpList = mapInstance[0].getControlPoints();

        featureTypes = buildFeatureTypesList(true, 6);
        Thread.sleep(1000,0);
        Assert.assertTrue(mapInstance[0].validateAddTypes(featureTypes));

        return cpList;
    }

    /**
     * Tests drawing of a triangle on the map.
     * @throws Exception
     */
    @Test
    public void drawTriangle() throws Exception {

        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon);

        remoteMap[0].drawFeature(polygon, fdl);
        postDrawFeature(fdl, polygon);

        buildTriangle(fdl);

        remoteMap[0].completeDraw();
        Thread.sleep(100);

        Assert.assertEquals("Expecting one event after completeDraw", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawCancel after completeDraw", "onDrawComplete", fdl.getNextEventReceived());

        Assert.assertTrue("Feature should be removed from the map", mapInstance[0].validateRemoveCount(7));
    }

    /**
     * Simulates a DOUBLE_CLICKED gesture and verifies that onDrawUpdate.COORDINATE_DELETED is received and ControlPoint(s) are removed from
     * the map.
     * @param fdl
     * @param cpList
     * @throws Exception
     */
    private void deleteCoordinate(FeatureDrawListener fdl, List<IFeature> cpList) throws Exception {

        ControlPoint cpToDelete = null;

        if((cpList != null) && (cpList.size() != 0)) {
            for(IFeature f: cpList) {
                if(f instanceof ControlPoint) {
                    ControlPoint cp = (ControlPoint) f;
                    if(cp.getCPType().compareTo(ControlPoint.CPTypeEnum.POSITION_CP) == 0) {
                        cpToDelete = cp;
                        break;
                    }
                }
            }
        }

        Assert.assertNotNull("No CP to delete found", cpToDelete);

        List<IFeature> pickList = new ArrayList<>();
        pickList.add(cpToDelete);
        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DOUBLE_CLICKED, pickList, cpToDelete.getPosition().getLatitude(), cpToDelete.getPosition().getLongitude(), 0.0);
        Thread.sleep(10);
        Assert.assertEquals("Expecting one event after simulateUserInteractionEvent", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawUpdate after simulateUserInteractionEvent", "onDrawUpdate." + FeatureEditUpdateTypeEnum.COORDINATE_DELETED.toString(), fdl.getNextEventReceived());

        Assert.assertTrue("Three control points will be removed.", mapInstance[0].validateRemoveCount(3));

        List<String> featureTypes = buildFeatureTypesList(true, 6);
        Thread.sleep(1000,0);
        Assert.assertTrue("Reconnect/Redraw the polygon", mapInstance[0].validateAddTypes(featureTypes));
    }

    /**
     * Builds a triangle and adds a coordinate to make it a quadrilateral.
     * Verifies that all the proper events were generated and control points were added.
     * @param fdl
     * @return
     * @throws Exception
     */
    private List<IFeature> buildQuadrilateral(FeatureDrawListener fdl) throws Exception {

        buildTriangle(fdl);
        addCoordinate(fdl, 40.5, -41.5, 0.0);

        List<String> featureTypes = buildFeatureTypesList(true, 8);

        List<IFeature> cpList = mapInstance[0].getControlPoints();

        Thread.sleep(1000,0);
        Assert.assertTrue("buildQuadrilateral ", mapInstance[0].validateAddTypes(featureTypes));


        return cpList;
    }

    /**
     * Tests delition of coordinate by double tapping a coordinate.
     * @throws Exception
     */
    @Test
    public void editCoordinates() throws Exception {

        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon);

        remoteMap[0].drawFeature(polygon, fdl);
        postDrawFeature(fdl, polygon);

        List<IFeature> cpList = buildQuadrilateral(fdl);

        deleteCoordinate(fdl, cpList);

        remoteMap[0].completeDraw();
        Thread.sleep(100);

        Assert.assertEquals("Expecting one event after completeDraw", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawCancel after completeDraw", "onDrawComplete", fdl.getNextEventReceived());

        Assert.assertTrue("Feature should be removed from the map", mapInstance[0].validateRemoveCount(7));
    }

    /**
     * Simulates deletion of a coordinate when there are exactly three ControlPoints on the map.
     * @param fdl
     * @param cpList
     * @throws Exception
     */
    private void deleteCoordinateFromTriangle(FeatureDrawListener fdl, List<IFeature> cpList) throws Exception {

        ControlPoint cpToDelete = null;

        if((cpList != null) && (cpList.size() != 0)) {
            for(IFeature f: cpList) {
                if(f instanceof ControlPoint) {
                    ControlPoint cp = (ControlPoint) f;
                    if(cp.getCPType().compareTo(ControlPoint.CPTypeEnum.POSITION_CP) == 0) {
                        cpToDelete = cp;
                        break;
                    }
                }
            }
        }

        Assert.assertNotNull("No CP to delete found", cpToDelete);

        List<IFeature> pickList = new ArrayList<>();
        pickList.add(cpToDelete);
        mapInstance[0].simulateUserInteractionEvent(UserInteractionEventEnum.DOUBLE_CLICKED, pickList, cpToDelete.getPosition().getLatitude(), cpToDelete.getPosition().getLongitude(), 0.0);
        Thread.sleep(10);
        Assert.assertEquals("Expecting one event after simulateUserInteractionEvent", 0, fdl.getEventReceivedCount());

    }

    /**
     * Verifies that user is not allowed to delete any coordinate when there are only three coordinates on the map for the feature being drawn.
     * @throws Exception
     */
    @Test
    public void attemptToRemoveTriangleCoordinate() throws Exception {

        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon);

        remoteMap[0].drawFeature(polygon, fdl);
        postDrawFeature(fdl, polygon);

        List<IFeature> cpList = buildTriangle(fdl);

        deleteCoordinateFromTriangle(fdl, cpList);

        remoteMap[0].completeDraw();
        Thread.sleep(100);

        Assert.assertEquals("Expecting one event after completeDraw", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawCancel after completeDraw", "onDrawComplete", fdl.getNextEventReceived());

        Assert.assertTrue("Feature should be removed from the map", mapInstance[0].validateRemoveCount(7));
    }

    /**
     * Simulates dragging of a feature that is being drawn. Verifies DRAG_COMPLETE does nothing.
     * @throws Exception
     */
    @Test
    public void dragTheFeature() throws Exception {

        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon);

        remoteMap[0].drawFeature(polygon, fdl);
        postDrawFeature(fdl, polygon);

        buildTriangle(fdl);

        Assert.assertTrue("List should be null/empty ", mapInstance[0].validateAddTypes(null));

        Log.d(TAG, "simulateFeatureDrag");
        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG, polygon, 40.1, -40.1, 0, 40.2, -40.2, 0);
        Log.d(TAG, "done simulateFeatureDrag");
        Assert.assertEquals("Expecting one event after dragFeature", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawStart after dragFeature", "onDrawUpdate." + FeatureEditUpdateTypeEnum.COORDINATE_MOVED.toString(), fdl.getNextEventReceived());

        mapInstance[0].simulateFeatureDrag(UserInteractionEventEnum.DRAG_COMPLETE, polygon, 40.1, -40.1, 0, 40.2, -40.2, 0);
        Assert.assertEquals("Expecting one event after dragFeature", 0, fdl.getEventReceivedCount());

        List<String> featureTypes = buildFeatureTypesList(1, 6);
        Thread.sleep(1000,0);
        Assert.assertTrue("drag Triangle ", mapInstance[0].validateAddTypes(featureTypes));

        remoteMap[0].completeDraw();

        Thread.sleep(100);

        Assert.assertEquals("Expecting one event after completeDraw", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawCancel after completeDraw", "onDrawComplete", fdl.getNextEventReceived());

        Assert.assertTrue("Feature should be removed from the map", mapInstance[0].validateRemoveCount(7));
    }

    /**
     * Invoke drawFeature on a feature that is already on the map.
     */

    @Test
    public void drawFeatureAlreadyOnMap() throws Exception {
        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon);

        addFeatureToMap(polygon);

        boolean exceptionGood = true;
        try {
            remoteMap[0].drawFeature(polygon, fdl);
        } catch (EMP_Exception e) {
            exceptionGood = false;
        } finally {
            Assert.assertTrue("drawFeature should not fail event if the feature is on the map.", exceptionGood);
        }
    }

    /**
     * Tests drawing of a triangle on the map. Application throws exception and that should be ignored by EMP software.
     * Exception throwing is enabled by adding a flag to the constructor.
     * @throws Exception
     */
    @Test
    public void drawTriangleTestException() throws Exception {

        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon, true);

        remoteMap[0].drawFeature(polygon, fdl);
        postDrawFeature(fdl, polygon);

        buildTriangle(fdl);

        remoteMap[0].completeDraw();
        Thread.sleep(100);

        Assert.assertEquals("Expecting one event after completeDraw", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawCancel after completeDraw", "onDrawComplete", fdl.getNextEventReceived());

        Assert.assertTrue("Feature should be removed from the map", mapInstance[0].validateRemoveCount(7));
    }

    class FeatureDrawEventListener implements IFeatureDrawEventListener {
        int featureDrawEventListener = 0;
        @Override
        public void onEvent(FeatureDrawEvent event) {
            featureDrawEventListener++;
        }
    }

    /**
     * Tests drawing of a triangle on the map. Application throws exception and that should be ignored by EMP software.
     * Exception throwing is enabled by adding a flag to the constructor. It also verifies that IFeatureDrawEventListener is invoked if
     * application has registered it.
     * @throws Exception
     */
    @Test
    public void drawTriangleTestFeatureDrawEventListener() throws Exception {

        Polygon polygon = initializeDraw();
        FeatureDrawListener fdl = new FeatureDrawListener(polygon, true);
        FeatureDrawEventListener fdel = new FeatureDrawEventListener();
        remoteMap[0].addFeatureDrawEventListener(fdel);

        remoteMap[0].drawFeature(polygon, fdl);
        postDrawFeature(fdl, polygon);

        buildTriangle(fdl);

        remoteMap[0].completeDraw();
        Thread.sleep(100);

        Assert.assertEquals("Expecting one event after completeDraw", 1, fdl.getEventReceivedCount());
        Assert.assertEquals("Expecting onDrawCancel after completeDraw", "onDrawComplete", fdl.getNextEventReceived());

        Assert.assertTrue("Feature should be removed from the map", mapInstance[0].validateRemoveCount(7));

        Log.d(TAG, "featureDrawEventListener invoked " + fdel.featureDrawEventListener);
        Assert.assertEquals("FeatureDrawEventListener should be invoked", 5, fdel.featureDrawEventListener );
    }
}
