package mil.emp3.api;

import android.graphics.Color;
import android.os.Looper;
import android.test.mock.MockContext;
import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.net.URL;

import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.interfaces.IMapService;
import mil.emp3.api.listeners.IKMLSEventListener;

//@RunWith(RobolectricTestRunner.class)
//@Config(constants = mil.emp3.view.BuildConfig.class)
@PrepareForTest({Color.class})
public class KMLSTest extends TestBaseSingleMap {

    private static String TAG = KMLSTest.class.getSimpleName();
//    private Context context;
    @Before
    public void setUp() throws Exception {
//        context = RuntimeEnvironment.application;
        setupSingleMap(TAG);
//        PowerMockito.mock(Color.class);
//        PowerMockito.when(Color.class, "parseColor", Mockito.any(String.class)).thenReturn(0);
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
