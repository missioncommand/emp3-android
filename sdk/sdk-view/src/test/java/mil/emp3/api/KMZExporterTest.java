package mil.emp3.api;

import android.os.Environment;
import android.test.mock.MockContext;
import android.util.Log;
import android.util.Xml;
import android.webkit.URLUtil;

import org.cmapi.primitives.GeoPosition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.annotation.Config;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import armyc2.c2sd.renderer.MilStdIconRenderer;
import mil.emp3.api.enums.KMLSEventEnum;
import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.listeners.IKMLSEventListener;
import mil.emp3.api.shadows.ShadowKMLExportThread;
import mil.emp3.api.shadows.ShadowTestRunner;
import mil.emp3.api.utils.BasicUtilities;
import mil.emp3.api.utils.FileUtility;
import mil.emp3.api.utils.kmz.EmpKMZExporter;

import static org.junit.Assert.assertTrue;

/**
 * @author Jenifer Cochran
 */

@RunWith(ShadowTestRunner.class)
@Config(shadows = {ShadowKMLExportThread.class})
@PrepareForTest({Environment.class, Xml.class, MilStdSymbol.class, MilStdIconRenderer.class, FileUtility.class, URLUtil.class})
public class KMZExporterTest extends TestBaseSingleMap
{
    private final static String TAG = KMZExporterTest.class.getName();

    private static File outputDirectory;
    private static File temporaryOutputDirectory;

    class MyMockContext extends MockContext {
        @Override
        public File getDir(final String name, final int mode) {
            Log.d(TAG, "Current Dir " + System.getProperty("user.dir"));
            return new File(System.getProperty("user.dir") + File.separator + name);
        }
    }

    class KMLSServiceListener implements IKMLSEventListener {

        BlockingQueue<KMLSEventEnum> queue;

        KMLSServiceListener(final BlockingQueue<KMLSEventEnum> queue) {
            this.queue = queue;
        }

        @Override
        public void onEvent(final KMLSEvent event) {
            try {
                Log.d(TAG, "KMLSServiceListener-onEvent " + event.getEvent().toString() + " status ");
                queue.put(event.getEvent());
            } catch (final Exception e) {
                Log.e(TAG, e.getMessage(), e);
            }
        }
    }


    @Before
    public void setUp() throws Exception
    {
        super.init();
        super.setupSingleMap(TAG);


        if(outputDirectory == null || !outputDirectory.exists())
        {
            outputDirectory = createTemporaryDirectory();
        }

        if(temporaryOutputDirectory == null || !temporaryOutputDirectory.exists())
        {
            temporaryOutputDirectory = createTemporaryDirectory();
        }

    }

    @After
    public void tearDown()
    {
//        if(outputDirectory != null && outputDirectory.exists())
//        {
//            FileUtility.deleteFolder(outputDirectory);
//        }
//
//        if(temporaryOutputDirectory != null && temporaryOutputDirectory.exists())
//        {
//            FileUtility.deleteFolder(temporaryOutputDirectory);
//        }
    }

    private static File createTemporaryDirectory() throws IOException
    {
        final File tempDirectory = new File(org.assertj.core.util.Files.temporaryFolderPath() + File.separator + UUID.randomUUID().toString());
        tempDirectory.mkdirs();
        return tempDirectory;
    }

    // Tests the Map Constructor

    @Test(expected = IllegalArgumentException.class)
    public void invalidMapParameter()
    {
        final boolean[] processEnded                = {false};
        final String    kmzFileNameWithoutExtension = "TestKmzFileName";

        EmpKMZExporter.exportToKMZ(null,  //invalid input
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCallbackParameter()
    {
        final String  kmzFileNameWithoutExtension = "TestKmzFileName";

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   false,
                                   null,       //invalid input
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   kmzFileNameWithoutExtension);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidNullTemporaryDirectoryParameter()
    {
        final boolean[] processEnded                = {false};
        final String    kmzFileNameWithoutExtension = "TestKmzFileName";

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   null, //invalid input
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEmptyStringTemporaryDirectoryParameter()
    {
        final boolean[] processEnded                = {false};
        final String    kmzFileNameWithoutExtension = "TestKmzFileName";

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   "", //invalid input
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidNullKmlFileName()
    {
        final boolean[] processEnded                = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   null);//invalid input

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEmptyStringKmlFileName()
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   "");//invalid input

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    // Tests the overlay constructor

    @Test(expected = IllegalArgumentException.class)
    public void invalidTemporaryDirectoryLocationOverlay() throws EMP_Exception
    {
        final boolean[] processEnded                = {false};
        final String    kmzFileNameWithoutExtension = "TestKmzFileName";

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addOverlayToMap(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   outputDirectory.getParentFile().getAbsolutePath(),//invalid input
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMapParameterOverlayConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(null,//invalid input
                                   addOverlayToMap(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   "kmzFileName");

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidOverlayParameterOverlayConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   (IOverlay) null,//invalid input
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   "kmzFileName");

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCallbackParameterOverlayConstructor() throws EMP_Exception
    {
        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addOverlayToMap(this.remoteMap),
                                   false,
                                   null, //invalid input
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   "kmzFileName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidNullTemporaryDirectoryParameterOverlayConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addOverlayToMap(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   null,//invalid input
                                   "kmzFileName");

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEmptyStringTemporaryDirectoryParameterOverlayConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addOverlayToMap(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   "",//invalid input
                                   "kmzFileName");

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidNullKmzFileNameParameterOverlayConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addOverlayToMap(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   null);//invalid input

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEmptyStringKmzFileNameParameterOverlayConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addOverlayToMap(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   "");//invalid input

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    //Tests the feature Constructor

    @Test(expected = IllegalArgumentException.class)
    public void invalidTemporaryDirectoryLocationFeature() throws EMP_Exception
    {
        final boolean[] processEnded                = {false};
        final String    kmzFileNameWithoutExtension = "TestKmzFileName";

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addRandomFeature(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   outputDirectory.getParentFile().getAbsolutePath(),//invalid input
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMapParameterFeatureConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};


        EmpKMZExporter.exportToKMZ(null,//invalid input
                                   addRandomFeature(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   "kmzFileName");

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidOverlayParameterFeatureConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   (IFeature) null,//invalid input
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   "kmzFileName");

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCallbackParameterFeatureConstructor() throws EMP_Exception
    {
        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addRandomFeature(this.remoteMap),
                                   false,
                                   null, //invalid input
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   "kmzFileName");
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidNullTemporaryDirectoryParameterFeatureConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addRandomFeature(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                       @Override
                                       public void exportSuccess(final File exportObject)
                                       {
                                           processEnded[0] = true;
                                       }
                                       @Override
                                       public void exportFailed(final Exception Ex)
                                       {
                                           processEnded[0] = true;
                                           Assert.fail(Ex.getMessage());
                                       }
                                   },
                                   null,//invalid input
                                   "kmzFileName");

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEmptyStringTemporaryDirectoryParameterFeatureConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addRandomFeature(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   "",//invalid input
                                   "kmzFileName");

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidNullKmzFileNameParameterFeatureConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addRandomFeature(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   null);//invalid input

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidEmptyStringKmzFileNameParameterFeatureConstructor() throws EMP_Exception
    {
        final boolean[] processEnded = {false};


        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addRandomFeature(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   "");//invalid input

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.fail();
    }

    //Tests if the exports a file

    @Test
    public void exportKmzTest()
    {
        final String    kmzFileNameWithoutExtension = "TestKmzFileName1";
        final boolean[] processEnded                = {false};
        final File[]    kmzFile                     = new File[1];

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               kmzFile[0] = exportObject;
                                                                               processEnded[0] = true;
                                                                           }

                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                                processEnded[0] = true;
                                                                                Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        if(kmzFile[0] != null)
        {
            if(!kmzFile[0].exists())
            {
                Assert.fail("The KMZ export did not return a file that exists. Did not create the file.");
            }
            else
            {
                assertTrue(String.format("The File Name of the kmz does not match what was passed in. Expected %s.kmz Actual %s",
                                                kmzFileNameWithoutExtension,
                                                kmzFile[0].getName()),
                                 (kmzFileNameWithoutExtension+ ".kmz").equals(kmzFile[0].getName()));
                //TODO: test if the file is in correct format
            }
        }
        else
        {
            Assert.fail("The KMZ export did not return a valid file.  Returned null.");
        }
    }

    @Test
    public void exportKmzOverlayTest() throws EMP_Exception
    {
        final String    kmzFileNameWithoutExtension = "TestKmzFileName2";
        final boolean[] processEnded                = {false};
        final File[]    kmzFile                     = new File[1];

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addOverlayToMap(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               kmzFile[0] = exportObject;
                                                                               processEnded[0] = true;
                                                                           }

                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        if(kmzFile[0] != null)
        {
            if(!kmzFile[0].exists())
            {
                Assert.fail("The KMZ export did not return a file that exists. Did not create the file.");
            }
            else
            {
                assertTrue(String.format("The File Name of the kmz does not match what was passed in. Expected %s.kmz Actual %s",
                                                kmzFileNameWithoutExtension,
                                                kmzFile[0].getName()),
                                 (kmzFileNameWithoutExtension+ ".kmz").equals(kmzFile[0].getName()));
                //TODO: test if the file is in correct format
            }
        }
        else
        {
            Assert.fail("The KMZ export did not return a valid file.  Returned null.");
        }
    }

    @Test
    public void exportKmzFeatureTest() throws EMP_Exception
    {
        final String    kmzFileNameWithoutExtension = "TestKmzFileName3";
        final boolean[] processEnded                = {false};
        final File[]    kmzFile                     = new File[1];

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addRandomFeature(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               kmzFile[0] = exportObject;
                                                                               processEnded[0] = true;
                                                                           }

                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        if(kmzFile[0] != null)
        {
            if(!kmzFile[0].exists())
            {
                Assert.fail("The KMZ export did not return a file that exists. Did not create the file.");
            }
            else
            {
                assertTrue(String.format("The File Name of the kmz does not match what was passed in. Expected %s.kmz Actual %s",
                                                kmzFileNameWithoutExtension,
                                                kmzFile[0].getName()),
                                 (kmzFileNameWithoutExtension+ ".kmz").equals(kmzFile[0].getName()));
                //TODO: test if the file is in correct format
            }
        }
        else
        {
            Assert.fail("The KMZ export did not return a valid file.  Returned null.");
        }
    }

    @Test
    public void exportKmzMilSymbolTest() throws EMP_Exception, MalformedURLException, InterruptedException, IOException {
        final String    kmzFileNameWithoutExtension = "TestKmzFileName";
        final boolean[] processEnded                = {false};
        final File[]    kmzFile                     = new File[1];

        final MilStdSymbol milSymbol = BasicUtilities.generateMilStdSymbol("TRUCK", UUID.randomUUID(),
                                                                           40, -75);
        final IOverlay overlay = addOverlayToMap(this.remoteMap);
        overlay.addFeature(milSymbol, true);
        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   milSymbol,
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               kmzFile[0] = exportObject;
                                                                               processEnded[0] = true;
                                                                           }

                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   temporaryOutputDirectory.getAbsolutePath(),
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        if(kmzFile[0] != null)
        {
            if(!kmzFile[0].exists())
            {
                Assert.fail("The KMZ export did not return a file that exists. Did not create the file.");
            }
            else
            {
                assertTrue(String.format("The File Name of the kmz does not match what was passed in. Expected %s.kmz Actual %s",
                                                kmzFileNameWithoutExtension,
                                                kmzFile[0].getName()),
                                 (kmzFileNameWithoutExtension+ ".kmz").equals(kmzFile[0].getName()));
                //TODO: test if the file is in correct format
            }
        }
        else
        {
            Assert.fail("The KMZ export did not return a valid file.  Returned null.");
        }

        final int READ_BUFFER_SIZE = 4096;
        final String sourceFilePath = kmzFile[0].getAbsolutePath();
        final String destinationFilePath = kmzFile[0].getParent();
        try {
            final ZipFile zipFile = new ZipFile(sourceFilePath);
            final Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                final ZipEntry zipEntry = entries.nextElement();
                Log.v(TAG, "zipEntry " + zipEntry.getName());

                // If it is a directory then make a new directory and continue.
                if(zipEntry.isDirectory()) {
                    final File directory = new File(destinationFilePath + File.separator + zipEntry.getName());
                    directory.mkdirs();
                    continue;
                    //It is possible for the zip to not contain the directory but files under the
                    //directory.  This case generates the directory in that case
                } else if(zipEntry.getName().contains(File.separator)) {
                    final File fileInZip = new File(zipEntry.getName());
                    final File parent = fileInZip.getParentFile();
                    final File zipParent = new File(destinationFilePath + File.separator + parent.getPath());
                    if (!zipParent.exists()) {
                        zipParent.mkdirs();
                    }
                }

                // Copy the file to destination directory
                try (BufferedInputStream bis = new  BufferedInputStream(zipFile.getInputStream(zipEntry));
                     BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(destinationFilePath, zipEntry.getName())))) {
                    byte[] buf = new byte[READ_BUFFER_SIZE];
                    int ii;
                    while ((ii = bis.read(buf, 0, READ_BUFFER_SIZE)) != -1) {
                        bos.write(buf, 0, ii);
                    }
                    bos.flush();
                }
            }

        } catch (IOException | SecurityException e) {
            Log.e(TAG, "KMLProcessor-unzipKMZFile " + sourceFilePath, e);
        }

        final File imageFile = new File(destinationFilePath+"/Image/0.PNG");
        assertTrue(imageFile.exists());
    }

    @Test
    public void exportKmzFeatureWithOutputLocation() throws EMP_Exception
    {

        final String    kmzFileName  = "TestKmzFileName3.kmz";
        final boolean[] processEnded = {false};
        final File[]    kmzFile      = new File[1];


        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addRandomFeature(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>(){
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               kmzFile[0] = exportObject;

                                                                               processEnded[0] = true;
                                                                           }

                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   new File(outputDirectory, kmzFileName),
                                   temporaryOutputDirectory);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.assertTrue(new File(outputDirectory, kmzFileName).exists());
    }

    @Test
    public void exportKmzOverlayWithOutputLocation() throws EMP_Exception
    {

        final String    kmzFileName  = "TestKmzFileName4.kmz";
        final boolean[] processEnded = {false};
        final File[]    kmzFile      = new File[1];


        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   addOverlayToMap(this.remoteMap),
                                   false,
                                   new IEmpExportToTypeCallBack<File>()
                                                                       {
                                                                           @Override
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               kmzFile[0] = exportObject;

                                                                               processEnded[0] = true;
                                                                           }

                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   new File(outputDirectory, kmzFileName),
                                   temporaryOutputDirectory);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.assertTrue(new File(outputDirectory, kmzFileName).exists());
    }

    @Test
    public void exportKmzMapWithOutputLocation() throws EMP_Exception
    {
        final String    kmzFileName  = "TestKmzFileName5.kmz";
        final boolean[] processEnded = {false};
        final File[]    kmzFile      = new File[1];

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   false,
                                   new IEmpExportToTypeCallBack<File>()
                                                                       {
                                                                           @Override
                                                                           public void exportSuccess(final File exportObject)
                                                                           {
                                                                               kmzFile[0] = exportObject;

                                                                               processEnded[0] = true;
                                                                           }

                                                                           @Override
                                                                           public void exportFailed(final Exception Ex)
                                                                           {
                                                                               processEnded[0] = true;
                                                                               Assert.fail(Ex.getMessage());
                                                                           }
                                                                       },
                                   new File(outputDirectory, kmzFileName),
                                   temporaryOutputDirectory);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        Assert.assertTrue(new File(outputDirectory, kmzFileName).exists());
    }

    private static IOverlay addOverlayToMap(final IMap map) throws EMP_Exception
    {
        final Overlay   overlay      = new Overlay();
        //add overlay to map
        overlay.setName("Test Overlay");
        map.addOverlay(overlay, true);
        return overlay;
    }

    private static Point addRandomFeature(final IOverlay overlay) throws EMP_Exception
    {
        final Point oPoint = getRandomPoint();
        overlay.addFeature(oPoint, true);
        return oPoint;
    }

    private static Point addRandomFeature(final IMap map) throws EMP_Exception
    {
        final IOverlay overlay = addOverlayToMap(map);
        return addRandomFeature(overlay);
    }

    private static Point getRandomPoint() throws EMP_Exception
    {
        final Point oPoint = new Point();
        final GeoPosition location = new GeoPosition();
        location.setLatitude(getRandomValueBetween(-90.0,90.0));
        location.setLongitude(getRandomValueBetween(-180.0, 180.0));
        oPoint.setPosition(location);

        return oPoint;
    }

    private static double getRandomValueBetween(final double low, final double high)
    {
        return low + (high - low) * new Random().nextDouble();
    }
}
