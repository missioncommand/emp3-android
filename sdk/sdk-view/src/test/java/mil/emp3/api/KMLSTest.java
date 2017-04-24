package mil.emp3.api;

import android.graphics.Color;
import android.os.Looper;
import android.test.mock.MockContext;
import android.util.Log;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.enums.KMLSEventEnum;
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
        PowerMockito.mockStatic(Color.class);
        PowerMockito.when(Color.class, "parseColor", Mockito.any(String.class)).thenReturn(0);
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

        BlockingQueue<KMLSEventEnum> queue;
        KMLSServiceListener(BlockingQueue<KMLSEventEnum> queue) {
            this.queue = queue;
        }
        @Override
        public void onEvent(KMLSEvent event) {
            try {
                Log.d(TAG, "KMLSServiceListener-onEvent " + event.getEvent().toString() + " status ");
                queue.put(event.getEvent());
            } catch(Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }

    @Test (expected=IllegalArgumentException.class)
    public void null_context_Test() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("example.kmz");
        Log.d(TAG, "url " + url.toString());
        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();
        mapInstance.cleanKmls();
        new KMLS(null, url.toString(), true, new KMLSServiceListener(queue));
    }

    @Test (expected=IllegalArgumentException.class)
    public void null_listener_Test() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("example.kmz");
        Log.d(TAG, "url " + url.toString());
        MockContext context = new MyMockContext();
        mapInstance.cleanKmls();
        new KMLS(context, url.toString(), true, null);
    }

    @Test (expected=IllegalArgumentException.class)
    public void null_geoid_Test() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("example.kmz");
        Log.d(TAG, "url " + url.toString());
        MockContext context = new MyMockContext();
        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();
        mapInstance.cleanKmls();
        new KMLS(context, url.toString(), true, new KMLSServiceListener(queue), null);
    }

    @Test (expected=MalformedURLException.class)
    public void bad_url_Test() throws Exception {
        MockContext context = new MyMockContext();
        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();
        mapInstance.cleanKmls();
        new KMLS(context, "example.kmz", true, new KMLSServiceListener(queue));
    }

    @Test
    public void kmzSample_Test() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("example.kmz");
        Log.d(TAG, "url " + url.toString());
        MockContext context = new MyMockContext();
        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();
        mapInstance.cleanKmls();

        IMapService mapService = new KMLS(context, url.toString(), true, new KMLSServiceListener(queue));
        remoteMap.addMapService(mapService);

        KMLSEventEnum eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED", KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_EXPLODED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_EXPLODED", KMLSEventEnum.KML_SERVICE_FILE_EXPLODED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_PARSED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_PARSED", KMLSEventEnum.KML_SERVICE_FILE_PARSED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FEATURES_DRAWN ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_PARSED", KMLSEventEnum.KML_SERVICE_FEATURES_DRAWN, eventEnum);

        Assert.assertTrue("Service Add GEO_POINT Count ", mapInstance.validateAddKmlsFeatureCount(FeatureTypeEnum.GEO_POINT, 12));
        Assert.assertTrue("Service Add GEO_PATH Count ", mapInstance.validateAddKmlsFeatureCount(FeatureTypeEnum.GEO_PATH, 3));
        Assert.assertTrue("Service Add Image Count ", mapInstance.validateAddKmlsImageCount(0));
        mapInstance.cleanKmls();

    }

    @Test
    public void kmlSample_Test() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("kml_samples.kml");
        Log.d(TAG, "url " + url.toString());
        MockContext context = new MyMockContext();
        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();
        mapInstance.cleanKmls();

        IMapService mapService = new KMLS(context, url.toString(), true, new KMLSServiceListener(queue));
        remoteMap.addMapService(mapService);

        KMLSEventEnum eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED", KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_EXPLODED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_EXPLODED", KMLSEventEnum.KML_SERVICE_FILE_EXPLODED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_PARSED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_PARSED", KMLSEventEnum.KML_SERVICE_FILE_PARSED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FEATURES_DRAWN ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_PARSED", KMLSEventEnum.KML_SERVICE_FEATURES_DRAWN, eventEnum);

        Assert.assertTrue("Service Add GEO_POINT Count ", mapInstance.validateAddKmlsFeatureCount(FeatureTypeEnum.GEO_POINT, 4));
        Assert.assertTrue("Service Add GEO_POLYGON Count ", mapInstance.validateAddKmlsFeatureCount(FeatureTypeEnum.GEO_POLYGON, 9));
        Assert.assertTrue("Service Add GEO_PATH Count ", mapInstance.validateAddKmlsFeatureCount(FeatureTypeEnum.GEO_PATH, 6));
        Assert.assertTrue("Service Add Image Count ", mapInstance.validateAddKmlsImageCount(1));
        mapInstance.cleanKmls();

    }


    @Test
    public void File_Not_Exist_Test() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("example.kmz");
        Log.d(TAG, "url " + url.toString());

        MockContext context = new MyMockContext();
        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();
        mapInstance.cleanKmls();


        IMapService mapService = new KMLS(context, url.toString().replace("example.kmz", "not_exist.kmz"), true, new KMLSServiceListener(queue));
        remoteMap.addMapService(mapService);

        KMLSEventEnum eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_RETRIEVAL_FAILED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_RETRIEVAL_FAILED", KMLSEventEnum.KML_SERVICE_FILE_RETRIEVAL_FAILED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_INSTALL_FAILED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_INSTALL_FAILED", KMLSEventEnum.KML_SERVICE_INSTALL_FAILED, eventEnum);
    }

    @Test
    public void UnZip_Fail_Test() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("cmapi.json");
        Log.d(TAG, "url " + url.toString());

        MockContext context = new MyMockContext();
        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();
        mapInstance.cleanKmls();


        IMapService mapService = new KMLS(context, url.toString(), true, new KMLSServiceListener(queue));
        remoteMap.addMapService(mapService);

        KMLSEventEnum eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED", KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_INVALID ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_INVALID", KMLSEventEnum.KML_SERVICE_FILE_INVALID, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_INSTALL_FAILED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_INSTALL_FAILED", KMLSEventEnum.KML_SERVICE_INSTALL_FAILED, eventEnum);
    }

    @Test
    public void Invalid_KML_Test() throws Exception {
        URL url = this.getClass().getClassLoader().getResource("kml_samples_invalid.kml");
        Log.d(TAG, "url " + url.toString());

        MockContext context = new MyMockContext();
        BlockingQueue<KMLSEventEnum> queue = new LinkedBlockingQueue<>();
        mapInstance.cleanKmls();


        IMapService mapService = new KMLS(context, url.toString(), true, new KMLSServiceListener(queue));
        remoteMap.addMapService(mapService);

        KMLSEventEnum eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED", KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_FILE_EXPLODED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_FILE_EXPLODED", KMLSEventEnum.KML_SERVICE_FILE_EXPLODED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_PARSE_FAILED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_PARSE_FAILED", KMLSEventEnum.KML_SERVICE_PARSE_FAILED, eventEnum);

        eventEnum = queue.poll(1, TimeUnit.SECONDS);
        Assert.assertNotNull("Not Received KMLSEventEnum.KML_SERVICE_INSTALL_FAILED ", eventEnum);
        Assert.assertEquals("Expected KMLSEventEnum.KML_SERVICE_INSTALL_FAILED", KMLSEventEnum.KML_SERVICE_INSTALL_FAILED, eventEnum);
    }
}
