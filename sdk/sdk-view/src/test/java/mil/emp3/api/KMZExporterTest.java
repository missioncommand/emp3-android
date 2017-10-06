package mil.emp3.api;

import android.os.Environment;
import android.util.Xml;

import com.google.common.io.Files;

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

import mil.emp3.api.interfaces.IEmpExportToTypeCallBack;
import mil.emp3.api.utils.FileUtility;
import mil.emp3.api.utils.kmz.EmpKMZExporter;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;
import static org.powermock.reflect.Whitebox.setInternalState;

/**
 * Created by jenifer.cochran@rgi-corp.local on 10/2/17.
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({Environment.class, Xml.class})
public class KMZExporterTest extends TestBaseSingleMap
{
    private final static String TAG = KMZExporterTest.class.getName();
    private static File outputDirectory = Files.createTempDir();

    @Before
    public void setUp() throws Exception
    {
        super.init();
        super.setupSingleMap(TAG);


        XmlSerializer mockSerializer = PowerMockito.mock(XmlSerializer.class);
        mockStatic(Xml.class);
        when(Xml.newSerializer()).thenReturn(mockSerializer);

        if(!outputDirectory.exists())
        {
            outputDirectory = Files.createTempDir();
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
        if(outputDirectory.exists())
        {
            FileUtility.deleteFolder(outputDirectory);
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidMapParameter()
    {
        final boolean[] processEnded        = {false};
        String  kmzFileNameWithoutExtension = "TestKmzFileName";

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
                                   outputDirectory.getAbsolutePath(),
                                   kmzFileNameWithoutExtension);
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidCallbackParameter()
    {
        final boolean[] processEnded        = {false};
        String  kmzFileNameWithoutExtension = "TestKmzFileName";

        EmpKMZExporter.exportToKMZ(this.remoteMap,
                                   false,
                                   null,       //invalid input
                                   outputDirectory.getAbsolutePath(),
                                   kmzFileNameWithoutExtension);
    }

    @Test
    public void exportKmzTest()
    {
        String  kmzFileNameWithoutExtension = "TestKmzFileName";
        final boolean[] processEnded        = {false};
        final File[]    kmzFile             = new File[1];

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
                                   outputDirectory.getAbsolutePath(),
                                   kmzFileNameWithoutExtension);

        while(processEnded[0] == false)
        {
            //wait until the thread has ended to verify success
        }

        if(kmzFile[0] != null)
        {
            kmzFile[0].delete();
        }
    }
}
