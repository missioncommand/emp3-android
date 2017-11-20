package mil.emp3.api;

import android.content.Context;

import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import mil.emp3.api.enums.MirrorCacheModeEnum;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEvent;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.mock.MockEventListener;
import mil.emp3.api.mock.MockMapInstance;

@Ignore
public class TestBaseMultiMap extends TestBase {
    private static String TAG;

    private Context context;

    protected RemoteMap[] remoteMap;

    protected MockMapInstance[] mapInstance;
    private BlockingQueue<IFeature>[] addFeatureQueue;
    private BlockingQueue<UUID>[] removeFeatureQueue;
    private BlockingQueue<IFeature>[] selectFeatureQueue;
    private BlockingQueue<UUID>[] deselectFeatureQueue;
    private BlockingQueue<ICamera>[] setCameraQueue;
    private BlockingQueue<String>[] userContextQueue;

    BlockingQueue<IEvent>[] receivedEventQueue;
    protected MockEventListener[] eventListener;

    protected void setupMultipleMaps(String TAG, int mapCount) throws Exception {
        init();

        this.TAG = TAG;

        remoteMap = new RemoteMap[mapCount];
        mapInstance = new MockMapInstance[mapCount];
        addFeatureQueue = new BlockingQueue[mapCount];
        removeFeatureQueue = new BlockingQueue[mapCount];
        setCameraQueue = new BlockingQueue[mapCount];
        selectFeatureQueue = new BlockingQueue[mapCount];
        deselectFeatureQueue = new BlockingQueue[mapCount];
        userContextQueue = new BlockingQueue[mapCount];

        receivedEventQueue = new BlockingQueue[mapCount];
        eventListener = new MockEventListener[mapCount];

        for(int ii = 0; ii < mapCount; ii++) {
            addFeatureQueue[ii] = new LinkedBlockingDeque<>();
            removeFeatureQueue[ii] = new LinkedBlockingDeque<>();
            selectFeatureQueue[ii] = new LinkedBlockingDeque<>();
            deselectFeatureQueue[ii] = new LinkedBlockingDeque<>();
            userContextQueue[ii] = new LinkedBlockingDeque<>();
            setCameraQueue[ii] = new LinkedBlockingQueue<>();
            mapInstance[ii] = new MockMapInstance(addFeatureQueue[ii], removeFeatureQueue[ii], setCameraQueue[ii], selectFeatureQueue[ii],
                    deselectFeatureQueue[ii], userContextQueue[ii]);
            receivedEventQueue[ii] = new LinkedBlockingDeque<>();
            eventListener[ii] = new MockEventListener(receivedEventQueue[ii]);

            remoteMap[ii] = new RemoteMap(UUID.randomUUID().toString(), context, MirrorCacheModeEnum.DISABLED, mapInstance[ii]);
            About.getVersionInformation(remoteMap[ii]);

        }
        System.err.println("RemoteMaps initialized");
    }
}
