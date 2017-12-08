package mil.emp3.api;

import android.os.Looper;

import org.junit.Rule;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mil.emp3.api.enums.ContainerEventEnum;
import mil.emp3.api.enums.FeatureEventEnum;
import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.VisibilityActionEnum;
import mil.emp3.api.events.ContainerEvent;
import mil.emp3.api.events.FeatureEvent;
import mil.emp3.api.events.VisibilityEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.core.CoreManager;
import mil.emp3.core.EventManager;
import mil.emp3.core.storage.StorageManager;
import mil.emp3.core.utils.MilStdRenderer;
import mil.emp3.mapengine.interfaces.IMapInstance;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;

import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@RunWith(RobolectricTestRunner.class)
@PrepareForTest({ManagerFactory.class, Looper.class})
@SuppressStaticInitializationFor("mil.emp3.api.utils.ManagerFactory")
abstract public class TestBase {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    static final protected IStorageManager storageManager = new StorageManager();
    static final protected IEventManager eventManager     = new EventManager();
    static final protected ICoreManager coreManager       = new CoreManager();
    static final protected IMilStdRenderer milStdRenderer = new MilStdRenderer();

    static {
        storageManager.setEventManager(eventManager);
        eventManager.setStorageManager(storageManager);
        coreManager.setStorageManager(storageManager);
        coreManager.setEventManager(eventManager);

        milStdRenderer.setStorageManager(storageManager);
    }

    protected void init() throws Exception {
        //
        // ManagerFactory
        //
//        PowerMockito.mockStatic(ManagerFactory.class);
//        final ManagerFactory managerFactory = PowerMockito.mock(ManagerFactory.class);
//
//        PowerMockito.when(ManagerFactory.getInstance()).thenReturn(managerFactory);
//
//        PowerMockito.when(managerFactory.getStorageManager()).thenReturn(storageManager);
//        PowerMockito.when(managerFactory.getEventManager()).thenReturn(eventManager);
//        PowerMockito.when(managerFactory.getCoreManager()).thenReturn(coreManager);
//        PowerMockito.when(managerFactory.getMilStdRenderer()).thenReturn(milStdRenderer);

        //
        // Looper
        //
//        PowerMockito.mockStatic(Looper.class);
//        final Looper looper = PowerMockito.mock(Looper.class);
//
//        PowerMockito.when(Looper.getMainLooper()).thenReturn(looper);
//        PowerMockito.when(Looper.myLooper()).thenReturn(looper);
    }

    static public List<IFeature> createMockedMilStdSymbolWithStubbedGuid(int count) {
        final List<IFeature> mockChildren = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final MilStdSymbol mockChild = mock(MilStdSymbol.class);
            final UUID childUuid = UUID.randomUUID();

            when(mockChild.getGeoId()).thenReturn(childUuid);
            when(mockChild.getFeatureType()).thenReturn(FeatureTypeEnum.GEO_MIL_SYMBOL);

            mockChildren.add(mockChild);
        }

        return mockChildren;
    }

    static public List<IFeature> createMockedPointWithStubbedGuid(int count) {
        final List<IFeature> mockChildren = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final Point mockChild = mock(Point.class);
            final UUID childUuid = UUID.randomUUID();

            when(mockChild.getGeoId()).thenReturn(childUuid);
            when(mockChild.getFeatureType()).thenReturn(FeatureTypeEnum.GEO_POINT);

            mockChildren.add(mockChild);
        }

        return mockChildren;
    }

    static public List<IFeature> createMockedCircleWithStubbedGuid(int count) {
        final List<IFeature> mockChildren = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final Circle mockChild = mock(Circle.class);
            final UUID childUuid = UUID.randomUUID();

            when(mockChild.getGeoId()).thenReturn(childUuid);
            when(mockChild.getFeatureType()).thenReturn(FeatureTypeEnum.GEO_CIRCLE);

            mockChildren.add(mockChild);
        }

        return mockChildren;
    }

    static public List<IOverlay> createMockedOverlayWithStubbedGuid(int count) {
        final List<IOverlay> mockChildren = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            final Overlay mockChild = mock(Overlay.class);
            final UUID childUuid = UUID.randomUUID();
            when(mockChild.getGeoId()).thenReturn(childUuid);

            mockChildren.add(mockChild);
        }

        return mockChildren;
    }

    static public IMap createMockedMapWithStubbedGuid() throws EMP_Exception {
        return createMockedMapWithStubbedGuid(mock(IMapInstance.class));
    }

    static public IMap createMockedMapWithStubbedGuid(IMapInstance mapInstance) throws EMP_Exception {
        final IMap mockMap = mock(IMap.class);
        when(mockMap.getGeoId()).thenReturn(UUID.randomUUID());

        storageManager.swapMapInstance(mockMap, mapInstance);

        return mockMap;
    }

    static public ArgumentMatcher<ContainerEvent> createMatcherToMatch(final Object userContext, final ContainerEventEnum eventType) {
        return new ArgumentMatcher<ContainerEvent>() {
            @Override
            public boolean matches(Object argument) {
                final ContainerEvent event = (ContainerEvent) argument;

                return (eventType == event.getEvent())
                        && (userContext == event.getUserContext());
            }
        };
    }

    static public ArgumentMatcher<FeatureEvent> createMatcherToMatch(final Object userContext, final FeatureEventEnum eventType) {
        return new ArgumentMatcher<FeatureEvent>() {
            @Override
            public boolean matches(Object argument) {
                final FeatureEvent event = (FeatureEvent) argument;

                return (eventType == event.getEvent())
                        && (userContext == event.getUserContext());
            }
        };
    }

    static public ArgumentMatcher<VisibilityEvent> createMatcherToMatch(final Object userContext, final VisibilityActionEnum eventType) {
        return new ArgumentMatcher<VisibilityEvent>() {
            @Override
            public boolean matches(Object argument) {
                final VisibilityEvent event = (VisibilityEvent) argument;

                return (eventType == event.getEvent())
                        && (userContext == event.getUserContext());
            }
        };
    }
}
