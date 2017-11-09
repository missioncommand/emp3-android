package mil.emp3.api;

import android.os.Environment;
import android.util.Xml;

import com.google.common.io.Files;

import org.cmapi.primitives.GeoPosition;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.UUID;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.IOverlay;
import mil.emp3.api.utils.FileUtility;
import mil.emp3.api.utils.kmz.EmpKMZExporter;

import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

/**
 * @author Jenifer Cochran
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Environment.class, Xml.class})
public class KMZExporterTest extends TestBaseSingleMap
{
    private final static String TAG = KMZExporterTest.class.getName();

    private static File outputDirectory;
    private static File temporaryOutputDirectory;

    @Before
    public void setUp() throws Exception
    {
        super.init();
        super.setupSingleMap(TAG);

        //Mock the xml serializer
        XmlSerializer mockSerializer = PowerMockito.mock(XmlSerializer.class);
        mockStatic(Xml.class);
        when(Xml.newSerializer()).thenReturn(mockSerializer);

        if(outputDirectory == null || !outputDirectory.exists())
        {
            outputDirectory = createTemporaryDirectory();
        }

        if(temporaryOutputDirectory == null || !temporaryOutputDirectory.exists())
        {
            temporaryOutputDirectory = createTemporaryDirectory();
        }

        mockStatic(Environment.class);
        setInternalState(Environment.class, "DIRECTORY_PICTURES", "Pictures");

        // Make the Environment class return a mocked external storage directory
        when(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
                        .thenReturn(outputDirectory);


    }

    @After
    public void tearDown()
    {
        if(outputDirectory != null && outputDirectory.exists())
        {
            FileUtility.deleteFolder(outputDirectory);
        }

        if(temporaryOutputDirectory != null && temporaryOutputDirectory.exists())
        {
            FileUtility.deleteFolder(temporaryOutputDirectory);
        }
    }

    private static File createTemporaryDirectory() throws IOException
    {
        File tempDirectory = new File(org.assertj.core.util.Files.temporaryFolderPath() + File.separator + UUID.randomUUID().toString());
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                       public void exportSuccess(File exportObject)
                                       {
                                           processEnded[0] = true;
                                       }
                                       @Override
                                       public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                                                                           public void exportSuccess(File exportObject)
                                                                           {
                                                                               processEnded[0] = true;
                                                                           }
                                                                           @Override
                                                                           public void exportFailed(Exception Ex)
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
                Assert.assertTrue(String.format("The File Name of the kmz does not match what was passed in. Expected %s.kmz Actual %s",
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
                Assert.assertTrue(String.format("The File Name of the kmz does not match what was passed in. Expected %s.kmz Actual %s",
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
                Assert.assertTrue(String.format("The File Name of the kmz does not match what was passed in. Expected %s.kmz Actual %s",
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

    private static IOverlay addOverlayToMap(IMap map) throws EMP_Exception
    {
        final Overlay   overlay      = new Overlay();
        //add overlay to map
        overlay.setName("Test Overlay");
        map.addOverlay(overlay, true);
        return overlay;
    }

    private static Point addRandomFeature(IOverlay overlay) throws EMP_Exception
    {
        Point oPoint = getRandomPoint();
        overlay.addFeature(oPoint, true);
        return oPoint;
    }

    private static Point addRandomFeature(IMap map) throws EMP_Exception
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

    private static double getRandomValueBetween(double low, double high)
    {
        return low + (high - low) * new Random().nextDouble();
    }
}
