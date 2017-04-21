package mil.emp3.api;

import android.test.mock.MockContext;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import java.io.File;
import java.net.URL;

import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.listeners.IKMLSEventListener;

public class KMLSTest extends TestBaseSingleMap {

    private static String TAG = KMLSTest.class.getSimpleName();

    @Before
    public void setUp() throws Exception {
        setupSingleMap(TAG);
    }

    @After
    public void tearDown() throws Exception {

    }

    class MyMockContext extends MockContext {
        @Override
        public File getDir(String name, int mode) {
            Log.d(TAG, "Current Dir " + System.getProperty("user.dir"));
            return new File(System.getProperty("user.dir"));
        }
    }
    class KMLSServiceListener implements IKMLSEventListener {

        KMLSServiceListener() {
        }
        @Override
        public void onEvent(KMLSEvent event) {
            try {
                Log.d(TAG, "KMLSServiceListener-onEvent " + event.getEvent().toString() + " status ");
            } catch(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Test
    public void kmzSample_Test() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("example.kmz");
        Log.d(TAG, "url " + url.toString());
        MockContext context = new MyMockContext();
        IMapService mapService = new KMLS(context, url.toString(), true, new KMLSServiceListener());
        remoteMap.addMapService(mapService);

        Thread.sleep(10000);
    }
}
