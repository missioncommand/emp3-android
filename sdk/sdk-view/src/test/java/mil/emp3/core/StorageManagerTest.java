package mil.emp3.core;

import android.content.Context;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.List;

import mil.emp3.api.TestBase;
import mil.emp3.api.enums.ContainerEventEnum;
import mil.emp3.api.enums.EventListenerTypeEnum;
import mil.emp3.api.enums.FeatureEventEnum;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.IconSizeEnum;
import mil.emp3.api.enums.MapStateEnum;
import mil.emp3.api.enums.MilStdLabelSettingEnum;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.IUUIDSet;
import mil.emp3.api.listeners.EventListenerHandle;
import mil.emp3.api.listeners.IContainerEventListener;
import mil.emp3.api.listeners.IFeatureEventListener;
import mil.emp3.api.listeners.IVisibilityEventListener;
import mil.emp3.api.utils.UUIDSet;
import mil.emp3.mapengine.api.FeatureVisibilityList;
import mil.emp3.mapengine.events.MapInstanceStateChangeEvent;
import mil.emp3.mapengine.interfaces.IMapEngineCapabilities;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mapengine.listeners.MapInstanceStateChangeEventListener;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(RobolectricTestRunner.class)
public class StorageManagerTest extends TestBase {


    private Context context;

    private IMap mockMap;

    @Before
    public void setUp() throws Exception {
        init();
        mockMap = createMockedMapWithStubbedGuid();
    }

    @Test
    public void testApplyWithNullContext() throws Exception {
        testApplyWithContext(null);
    }

    @Test
    public void testApplyWithNotNullContext() throws Exception {
        testApplyWithContext("ekekeke");
    }

    @Test
    public void testSetMilStdLabelsWithNullContext() throws Exception {
        testSetMilStdLabelsWithContext(null);
    }

    @Test
    public void testSetMilStdLabelsWithNotNullContext() throws Exception {
        testSetMilStdLabelsWithContext("jhjhjhjhjh");
    }

    @Test
    public void testSetIconSizeWithNullContext() throws Exception {
        testSetIconSizeWithContext(null);
    }

    @Test
    public void testSetIconSizeWithNotNullContext() throws Exception {
        testSetIconSizeWithContext("okokok");
    }

    @Test
    public void testRedrawAllFeaturesWithNullContext() throws Exception {
        testRedrawAllFeaturesWithContext(null);
    }

    @Test
    public void testRedrawAllFeaturesWithNotNullContext() throws Exception {
        testRedrawAllFeaturesWithContext("rrttrrtt");
    }

    @Test
    public void testVisibilityOnMapWithNullContext() throws Exception {
        testVisibilityOnMapWithContext(null);
    }

    @Test
    public void testVisibilityOnMapWithNotNullContext() throws Exception {
        testVisibilityOnMapWithContext("aassddff");
    }

    @Test
    public void testClearSelectedWithNullContext() throws Exception {
        testClearSelectedWithContext(null);
    }

    @Test
    public void testClearSelectedWithNotNullContext() throws Exception {
        testClearSelectedWithContext("ffffff");
    }

    @Test
    public void testSelectDeselectFeaturesWithNullContext() throws Exception {
        testSelectDeselectFeaturesWithContext(null);
    }

    @Test
    public void testSelectDeselectFeaturesWithNotNullContext() throws Exception {
        testSelectDeselectFeaturesWithContext("zzzxxxccc");
    }

    @Test
    public void testRemoveChildrenWithContextNull() throws Exception {
        testRemoveChildrenWithContext(null);
    }
    @Test
    public void testRemoveChildrenWithContextNotNull() throws Exception {
        testRemoveChildrenWithContext("aabbcc");
    }

    @Test
    public void testAddRemoveFeaturesWithContextNull() throws Exception {
        testAddRemoveFeaturesWithContext(null);
    }

    @Test
    public void testAddRemoveFeaturesWithContextNotNull() throws Exception {
        testAddRemoveFeaturesWithContext("asdf");
    }

    @Test
    public void testAddRemoveOverlaysWithContextNull() throws Exception {
        testAddRemoveOverlaysWithContext(null);
    }

    @Test
    public void testAddRemoveOverlaysWithContextNotNull() throws Exception {
        testAddRemoveOverlaysWithContext("abc");
    }

    private void testAddRemoveOverlaysWithContext(Object contextData) throws Exception {
        IContainerEventListener mockListener;
        EventListenerHandle eventHandle;

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final List<IOverlay> mockOverlayChildren = createMockedOverlayWithStubbedGuid(1);

        // add overlay to map
        mockListener = mock(IContainerEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, mockMap, mockListener);
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), false, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, ContainerEventEnum.OBJECT_ADDED)));
        eventManager.removeEventHandler(eventHandle);

        // add overlay to overlay
        mockListener = mock(IContainerEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, mockOverlay, mockListener);
        storageManager.addOverlays(mockOverlay, mockOverlayChildren, false, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, ContainerEventEnum.OBJECT_ADDED)));
        eventManager.removeEventHandler(eventHandle);


        // remove overlay from overlay
        mockListener = mock(IContainerEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, mockOverlay, mockListener);
        storageManager.removeOverlays(mockOverlay, mockOverlayChildren, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, ContainerEventEnum.OBJECT_REMOVED)));
        eventManager.removeEventHandler(eventHandle);

        // remove overlay from map
        mockListener = mock(IContainerEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, mockMap, mockListener);
        storageManager.removeOverlays(mockMap, Arrays.asList(mockOverlay), contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, ContainerEventEnum.OBJECT_REMOVED)));
        eventManager.removeEventHandler(eventHandle);
    }

    private void testAddRemoveFeaturesWithContext(Object contextData) throws Exception {
        IContainerEventListener mockListener;
        EventListenerHandle eventHandle;

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final IFeature mockFeature = createMockedCircleWithStubbedGuid(1).get(0);
        final List<IFeature> mockFeatureChildren = createMockedCircleWithStubbedGuid(1);

        // ..because the overlay needs to be part of the map before we do anything further with it
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), false, contextData);

        // add feature to overlay
        mockListener = mock(IContainerEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, mockOverlay, mockListener);
        storageManager.addFeatures(mockOverlay, Arrays.asList(mockFeature), false, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, ContainerEventEnum.OBJECT_ADDED)));
        eventManager.removeEventHandler(eventHandle);

        // add feature to feature
        mockListener = mock(IContainerEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, mockFeature, mockListener);
        storageManager.addFeatures(mockFeature, mockFeatureChildren, false, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, ContainerEventEnum.OBJECT_ADDED)));
        eventManager.removeEventHandler(eventHandle);


        // remove feature from feature
        mockListener = mock(IContainerEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, mockFeature, mockListener);
        storageManager.removeFeatures(mockFeature, mockFeatureChildren, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, ContainerEventEnum.OBJECT_REMOVED)));
        eventManager.removeEventHandler(eventHandle);

        // remove feature from overlay
        mockListener = mock(IContainerEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, mockOverlay, mockListener);
        storageManager.removeFeatures(mockOverlay, Arrays.asList(mockFeature), contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, ContainerEventEnum.OBJECT_REMOVED)));
        eventManager.removeEventHandler(eventHandle);
    }

    private void testRemoveChildrenWithContext(Object contextData) throws Exception {
        IContainerEventListener mockListener;
        EventListenerHandle eventHandle;

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final List<IOverlay> mockOverlayChildren = createMockedOverlayWithStubbedGuid(1);

        // ..because the overlay needs to be part of the map before we do anything further with it
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), false, contextData);

        storageManager.addOverlays(mockOverlay, mockOverlayChildren, false, contextData);

        mockListener = mock(IContainerEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.CONTAINER_EVENT_LISTENER, mockOverlay, mockListener);
        storageManager.removeChildren(mockOverlay, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, ContainerEventEnum.OBJECT_REMOVED)));
        eventManager.removeEventHandler(eventHandle);
    }

    private void testSelectDeselectFeaturesWithContext(Object contextData) throws Exception {
        IFeatureEventListener mockListener;
        EventListenerHandle eventHandle;

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final IFeature mockFeature = createMockedCircleWithStubbedGuid(1).get(0);


        // ..because the overlay needs to be part of the map before we do anything further with it
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), false, contextData);

        storageManager.addFeatures(mockOverlay, Arrays.asList(mockFeature), false, contextData);


        mockListener = mock(IFeatureEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.FEATURE_EVENT_LISTENER, mockMap, mockListener);
        storageManager.selectFeatures(mockMap, Arrays.asList(mockFeature), contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, FeatureEventEnum.FEATURE_SELECTED)));
        eventManager.removeEventHandler(eventHandle);

        mockListener = mock(IFeatureEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.FEATURE_EVENT_LISTENER, mockMap, mockListener);
        storageManager.deselectFeatures(mockMap, Arrays.asList(mockFeature), contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, FeatureEventEnum.FEATURE_DESELECTED)));
        eventManager.removeEventHandler(eventHandle);
    }

    private void testClearSelectedWithContext(Object contextData) throws Exception {
        IFeatureEventListener mockListener;
        EventListenerHandle eventHandle;

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final IFeature mockFeature = createMockedCircleWithStubbedGuid(1).get(0);

        // ..because the overlay needs to be part of the map before we do anything further with it
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), false, contextData);

        // add some features
        storageManager.addFeatures(mockOverlay, Arrays.asList(mockFeature), false, contextData);

        // select some features
        storageManager.selectFeatures(mockMap, Arrays.asList(mockFeature), contextData);

        mockListener = mock(IFeatureEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.FEATURE_EVENT_LISTENER, mockMap, mockListener);
        storageManager.clearSelected(mockMap, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, FeatureEventEnum.FEATURE_DESELECTED)));
        eventManager.removeEventHandler(eventHandle);
    }

    private void testVisibilityOnMapWithContext(Object contextData) throws Exception {
        IVisibilityEventListener mockListener;
        EventListenerHandle eventHandle;

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final IFeature mockFeature = createMockedCircleWithStubbedGuid(1).get(0);

        // ..because the overlay needs to be part of the map before we do anything further with it
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), false, contextData);

        // add some features
        storageManager.addFeatures(mockOverlay, Arrays.asList(mockFeature), false, contextData);


        final IUUIDSet uuidSet = new UUIDSet();
        uuidSet.add(mockFeature.getGeoId());

        mockListener = mock(IVisibilityEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, mockMap, mockListener);
        storageManager.setVisibilityOnMap(mockMap, uuidSet, VisibilityActionEnum.SHOW_ALL, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, VisibilityActionEnum.SHOW_ALL)));
        eventManager.removeEventHandler(eventHandle);

        mockListener = mock(IVisibilityEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, mockMap, mockListener);
        storageManager.setVisibilityOnMap(mockMap, uuidSet, VisibilityActionEnum.HIDE_ALL, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, VisibilityActionEnum.HIDE_ALL)));
        eventManager.removeEventHandler(eventHandle);

        mockListener = mock(IVisibilityEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, mockMap, mockListener);
        storageManager.setVisibilityOnMap(mockMap, uuidSet, VisibilityActionEnum.TOGGLE_OFF, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, VisibilityActionEnum.TOGGLE_OFF)));
        eventManager.removeEventHandler(eventHandle);

        mockListener = mock(IVisibilityEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, mockMap, mockListener);
        storageManager.setVisibilityOnMap(mockMap, uuidSet, VisibilityActionEnum.TOGGLE_ON, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, VisibilityActionEnum.TOGGLE_ON)));
        eventManager.removeEventHandler(eventHandle);

        ////

        mockListener = mock(IVisibilityEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, mockMap, mockListener);
        storageManager.setVisibilityOnMap(mockMap, mockFeature, mockOverlay, VisibilityActionEnum.SHOW_ALL, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, VisibilityActionEnum.SHOW_ALL)));
        eventManager.removeEventHandler(eventHandle);

        mockListener = mock(IVisibilityEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, mockMap, mockListener);
        storageManager.setVisibilityOnMap(mockMap, mockFeature, mockOverlay, VisibilityActionEnum.HIDE_ALL, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, VisibilityActionEnum.HIDE_ALL)));
        eventManager.removeEventHandler(eventHandle);

        mockListener = mock(IVisibilityEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, mockMap, mockListener);
        storageManager.setVisibilityOnMap(mockMap, mockFeature, mockOverlay, VisibilityActionEnum.TOGGLE_OFF, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, VisibilityActionEnum.TOGGLE_OFF)));
        eventManager.removeEventHandler(eventHandle);

        mockListener = mock(IVisibilityEventListener.class);
        eventHandle  = eventManager.addEventHandler(EventListenerTypeEnum.VISIBILITY_EVENT_LISTENER, mockMap, mockListener);
        storageManager.setVisibilityOnMap(mockMap, mockFeature, mockOverlay, VisibilityActionEnum.TOGGLE_ON, contextData);
        verify(mockListener, times(1)).onEvent(argThat(createMatcherToMatch(contextData, VisibilityActionEnum.TOGGLE_ON)));
        eventManager.removeEventHandler(eventHandle);
    }

    private void testRedrawAllFeaturesWithContext(Object contextData) throws Exception {
        final IMapInstance mockMapInstance = mock(IMapInstance.class);
        mockMap = createMockedMapWithStubbedGuid(mockMapInstance);

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final IFeature mockFeature = createMockedCircleWithStubbedGuid(1).get(0);

        // ..because the overlay needs to be part of the map before we do anything further with it
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), true, "NA");

        // add some features
        storageManager.addFeatures(mockOverlay, Arrays.asList(mockFeature), true, "NA");


        storageManager.redrawAllFeaturesAndServices(mockMap, contextData, null);
        verify(mockMapInstance, times(1)).addFeatures(any(FeatureVisibilityList.class), eq(contextData));
    }

    private void testSetIconSizeWithContext(Object contextData) throws Exception {
        final IMapInstance mockMapInstance = mock(IMapInstance.class);
        mockMap = createMockedMapWithStubbedGuid(mockMapInstance);

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final IFeature mockFeature = createMockedPointWithStubbedGuid(1).get(0);

        // ..because the overlay needs to be part of the map before we do anything further with it
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), true, "NA");

        // add some features
        storageManager.addFeatures(mockOverlay, Arrays.asList(mockFeature), true, "NA");


        storageManager.setIconSize(mockMap, IconSizeEnum.MEDIUM, contextData);
        verify(mockMapInstance, times(1)).addFeatures(any(FeatureVisibilityList.class), eq(contextData));
    }

    private void testSetMilStdLabelsWithContext(Object contextData) throws Exception {
        final IMapInstance mockMapInstance = mock(IMapInstance.class);
        mockMap = createMockedMapWithStubbedGuid(mockMapInstance);

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final IFeature mockFeature = createMockedMilStdSymbolWithStubbedGuid(1).get(0);

        // ..because the overlay needs to be part of the map before we do anything further with it
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), true, "NA");

        // add some features
        storageManager.addFeatures(mockOverlay, Arrays.asList(mockFeature), true, "NA");


        storageManager.setMilStdLabels(mockMap, MilStdLabelSettingEnum.ALL_LABELS, contextData);
        verify(mockMapInstance, times(1)).addFeatures(any(FeatureVisibilityList.class), eq(contextData));
    }

    private void testApplyWithContext(Object contextData) throws Exception {
        final IMapEngineCapabilities capabilities = mock(IMapEngineCapabilities.class);
        when(capabilities.canPlot(any(FeatureTypeEnum.class))).thenReturn(true);

        final IMapInstance mockMapInstance = mock(IMapInstance.class);
        when(mockMapInstance.getCapabilities()).thenReturn(capabilities);

        mockMap = createMockedMapWithStubbedGuid(mockMapInstance);

        /*
         * NOTE: apply() assumes a MAP_READY state event has been triggered. We capture the
         *       stateChangeListener that gets passed to the mapInstance so that we can manually
         *       generate one.
         */
        ArgumentCaptor<MapInstanceStateChangeEventListener> stateChangeListener = ArgumentCaptor.forClass(MapInstanceStateChangeEventListener.class);
        verify(mockMapInstance).addMapInstanceStateChangeEventListener(stateChangeListener.capture());
        stateChangeListener.getValue().onEvent(new MapInstanceStateChangeEvent(mockMapInstance, MapStateEnum.MAP_READY));

        final IOverlay mockOverlay = createMockedOverlayWithStubbedGuid(1).get(0);
        final IFeature mockFeature = createMockedCircleWithStubbedGuid(1).get(0);

        // ..because the overlay needs to be part of the map before we do anything further with it
        storageManager.addOverlays(mockMap, Arrays.asList(mockOverlay), false, "NA");

        // add some features
        storageManager.addFeatures(mockOverlay, Arrays.asList(mockFeature), false, "NA");


        storageManager.apply(mockFeature, false, contextData);
        verify(mockMapInstance, times(1)).addFeatures(any(FeatureVisibilityList.class), eq(contextData));

        storageManager.apply(mockFeature, true, contextData);
        verify(mockMapInstance, timeout(500).times(1)).addFeatures(any(FeatureVisibilityList.class), eq(contextData));
    }
}
