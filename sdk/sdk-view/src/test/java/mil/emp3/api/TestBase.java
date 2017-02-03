package mil.emp3.api;

import android.os.Looper;

import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.core.CoreManager;
import mil.emp3.core.EventManager;
import mil.emp3.core.storage.StorageManager;
import mil.emp3.core.utils.MilStdRenderer;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ManagerFactory.class, Looper.class})
@SuppressStaticInitializationFor("mil.emp3.api.utils.ManagerFactory")
abstract public class TestBase {

    static final IStorageManager storageManager = new StorageManager();
    static final IEventManager eventManager     = new EventManager();
    static final ICoreManager coreManager       = new CoreManager();
    static final IMilStdRenderer milStdRenderer = new MilStdRenderer();

    static {
        storageManager.setEventManager(eventManager);
        eventManager.setStorageManager(storageManager);
        coreManager.setStorageManager(storageManager);
        coreManager.setEventManager(eventManager);

        milStdRenderer.setStorageManager(storageManager);
    }

    private void init() throws Exception {
        //
        // ManagerFactory
        //
        PowerMockito.mockStatic(ManagerFactory.class);
        final ManagerFactory managerFactory = PowerMockito.mock(ManagerFactory.class);

        PowerMockito.when(ManagerFactory.getInstance()).thenReturn(managerFactory);

        PowerMockito.when(managerFactory.getStorageManager()).thenReturn(storageManager);
        PowerMockito.when(managerFactory.getEventManager()).thenReturn(eventManager);
        PowerMockito.when(managerFactory.getCoreManager()).thenReturn(coreManager);
        PowerMockito.when(managerFactory.getMilStdRenderer()).thenReturn(milStdRenderer);

        //
        // Looper
        //
        PowerMockito.mockStatic(Looper.class);
        final Looper looper = PowerMockito.mock(Looper.class);

        PowerMockito.when(Looper.getMainLooper()).thenReturn(looper);
        PowerMockito.when(Looper.myLooper()).thenReturn(looper);
    }

    protected void setupMultipleMaps(String tag, int mapCount) throws Exception {
        init();
    }
    protected void setupSingleMap(String tag) throws Exception {
        init();
    }
}
