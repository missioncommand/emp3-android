package mil.emp3.api;

import android.content.Context;
import android.content.res.Resources;

import org.junit.Ignore;
import org.mockito.Mock;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEvent;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.mock.MockEventListener;
import mil.emp3.api.mock.MockMapInstance;

@Ignore
public class TestBaseSingleMap extends TestBase {
    private static String TAG;

    @Mock
    private Context context;
    @Mock
    private Resources res;

    protected RemoteMap remoteMap;

    protected MockMapInstance mapInstance;
    private BlockingQueue<IFeature> addFeatureQueue;
    private BlockingQueue<UUID> removeFeatureQueue;
    private BlockingQueue<ICamera> setCameraQueue;

    BlockingQueue<IEvent> receivedEventQueue;
    protected MockEventListener eventListener;

    @Override
    protected void setupSingleMap(String TAG) throws Exception {
        super.setupSingleMap(TAG);

        this.TAG = TAG;

        addFeatureQueue = new LinkedBlockingDeque<>();
        removeFeatureQueue = new LinkedBlockingDeque<>();
        setCameraQueue = new LinkedBlockingDeque<>();
        mapInstance = new MockMapInstance(addFeatureQueue, removeFeatureQueue, setCameraQueue );
        receivedEventQueue = new LinkedBlockingDeque<>();
        eventListener = new MockEventListener(receivedEventQueue);

        remoteMap = new RemoteMap(UUID.randomUUID().toString(), context, MirrorCacheModeEnum.DISABLED, mapInstance);
        About.getVersionInformation(remoteMap);
        System.err.println("RemoteMap initialized");
    }
}
