package mil.emp3.json.geoJson;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Looper;
import android.test.InstrumentationTestCase;
import android.test.InstrumentationTestRunner;
import android.test.InstrumentationTestSuite;
import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.InputStream;
import java.util.List;

import mil.emp3.api.enums.FeatureTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.core.ICoreManager;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.core.CoreManager;
import mil.emp3.core.EventManager;
import mil.emp3.core.storage.StorageManager;
import mil.emp3.core.utils.MilStdRenderer;
import mil.emp3.mapengine.interfaces.IMilStdRenderer;
import mil.emp3.view.R;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ManagerFactory.class, Looper.class})
@SuppressStaticInitializationFor("mil.emp3.api.utils.ManagerFactory")
public class GeoJsonParserTest {
    private static final String TAG = GeoJsonParser.class.getSimpleName();

    private static final String invalidJsonString = "xxxxxxxxxxxx";
    private ClassLoader classLoader = null;
    private static final IStorageManager storageManager = new StorageManager();
    private static final IEventManager eventManager     = new EventManager();
    private static final ICoreManager coreManager       = new CoreManager();
    private static final IMilStdRenderer milStdRenderer = new MilStdRenderer();

    static {
        storageManager.setEventManager(eventManager);
        eventManager.setStorageManager(storageManager);
        coreManager.setStorageManager(storageManager);
        coreManager.setEventManager(eventManager);
        milStdRenderer.setStorageManager(storageManager);
    }



    @Before
    public void setUp() throws Exception {
        classLoader = GeoJsonParser.class.getClassLoader();
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

    @After
    public void cleanUp() throws Exception {

    }

    @Test
    public void invalidJsonTest() {
        Log.d(TAG, "Invalid json test");
        try {
            GeoJsonParser.parse(invalidJsonString);
            Assert.fail("Invalid json gave no error");
        } catch (Exception e) {
            Log.d(TAG, "Invalid json threw exception, test passed");
        }
    }

    @Test
    public void validJsonTest() {
        Log.d(TAG, "Valid json test");
        InputStream stream = null;
        try {
            stream = classLoader.getResourceAsStream("valid.json");
        } catch (Exception e) {
            Assert.fail("Could not get resource stream for parsing");
        }
        try {
            List<IFeature> featureList = GeoJsonParser.parse(stream);
            Assert.fail("Valid json but not geoJson gave no error");
        } catch (Exception e) {
            Log.d(TAG, "Valid json but not geoJson threw exception, test passed");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {

                }
            }
        }
    }

    @Test
    public void invalidGeoJsonTest() {
        Log.d(TAG, "Invalid geojson test");
        InputStream stream = null;
        try {
            stream = classLoader.getResourceAsStream("cmapi.json");
        } catch (Exception e) {
            Assert.fail("Could not get resource stream for parsing");
        }
        List<IFeature> featureList = null;
        IFeature feature = null;
        try {
            featureList = GeoJsonParser.parse(stream);
            Assert.assertEquals(2, featureList.size());
            feature = featureList.get(0);
            Assert.assertEquals(feature.getFeatureType(), FeatureTypeEnum.GEO_POLYGON);
            Assert.assertEquals(feature.getPositions().size(), 5);
            Assert.assertNotEquals(feature.getDescription(), "polygon  pop-up  text");
        } catch (Exception e) {
            Assert.fail("invalid geojson threw exception, should have ignored error");
        }
        try {
            feature = featureList.get(1);
            Assert.assertEquals(feature.getFeatureType(), FeatureTypeEnum.GEO_PATH);
            Assert.assertEquals(feature.getPositions().size(), 4);
            Assert.assertEquals(feature.getPositions().get(2).getLatitude(), 5.0, 0.001);
            Assert.assertNotEquals(feature.getName(), "crossingLine");
        } catch (Exception e) {
            Assert.fail("invalid geojson threw exception, should have ignored error");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {

                }
            }
        }
    }

    @Test
    public void validGeoJsonTest() {
        Log.d(TAG, "Valid geojson test");
        InputStream stream = null;
        try {
            stream = classLoader.getResourceAsStream("cmapi-fixed.json");
        } catch (Exception e) {
            Assert.fail("Could not get resource stream for parsing");
        }
        try {
            List<IFeature> featureList = GeoJsonParser.parse(stream);
            Assert.assertEquals(2, featureList.size());
            IFeature feature = featureList.get(0);
            Assert.assertEquals(feature.getFeatureType(), FeatureTypeEnum.GEO_POLYGON);
            Assert.assertEquals(feature.getPositions().size(), 5);
            Assert.assertEquals(feature.getDescription(), "polygon  pop-up  text");
            feature = featureList.get(1);
            Assert.assertEquals(feature.getFeatureType(), FeatureTypeEnum.GEO_PATH);
            Assert.assertEquals(feature.getPositions().size(), 4);
            Assert.assertEquals(feature.getPositions().get(2).getLatitude(), 5.0, 0.001);
            Assert.assertEquals(feature.getName(), "crossingLine");
        } catch (Exception e) {
            Assert.fail("valid geojson threw exception");
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (Exception e) {

                }
            }
        }
    }
}
