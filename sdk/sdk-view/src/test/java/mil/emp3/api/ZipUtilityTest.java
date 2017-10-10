package mil.emp3.api;

import org.assertj.core.util.Files;
import org.codehaus.plexus.util.IOUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import mil.emp3.api.utils.FileUtility;
import mil.emp3.api.utils.ZipUtility;

/**
 * @author Jenifer Cochran
 */

public class ZipUtilityTest
{
    private static File tempFile;
    private static File tempDirectory;
    private static File tempDirectory2;
    private static File tempZipFile;

    @Before
    public void setUp() throws IOException
    {
        if(tempZipFile == null || !tempZipFile.exists())
        {
            tempZipFile = File.createTempFile("testZip", ".zip");
        }

        if(tempFile == null || !tempFile.exists())
        {
            tempFile = Files.newTemporaryFile();
        }

        if(tempDirectory == null || !tempDirectory.exists())
        {
            tempDirectory = createTemporaryDirectory();
        }

        if(tempDirectory2 == null || !tempDirectory2.exists())
        {
            tempDirectory2 = createTemporaryDirectory();
        }
    }

    private static File createTemporaryDirectory() throws IOException
    {
        File tempDirectory = new File(Files.temporaryFolderPath() + File.separator + UUID.randomUUID().toString());
        tempDirectory.mkdirs();
        return tempDirectory;
    }

    @After
    public void tearDown()
    {
        if(tempFile != null && tempFile.exists())
        {
            tempFile.delete();
        }

        if(tempDirectory != null && tempDirectory.exists())
        {
            FileUtility.deleteFolder(tempDirectory);
        }

        if(tempDirectory2 != null && tempDirectory2.exists())
        {
            FileUtility.deleteFolder(tempDirectory2);
        }


        if(tempZipFile != null && tempZipFile.exists())
        {
            tempZipFile.delete();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testZipNullPointerExceptionDirectory() throws IOException
    {
        ZipUtility.zip(null, tempFile);
    }

    @Test(expected = NullPointerException.class)
    public void testZipNullPointerExceptionZipFile() throws IOException
    {
        ZipUtility.zip(tempDirectory, null);
    }

    @Test(expected = NullPointerException.class)
    public void testUnzipNullPointerExceptionZipFile() throws IOException
    {
        ZipUtility.unzip(null, tempDirectory);
    }

    @Test
    public void testZipOutput() throws IOException
    {
        //Folder Structure to test
        // TopDirectory
        //    ChildFileA.txt
        //    ChildFileB.png
        //    SubDirectory
        //       ChildFileC.jpg

        final File childFileA   = new File(tempDirectory.getAbsolutePath() + File.separator + "ChildFileA.txt");
        final File childFileB   = new File(tempDirectory.getAbsolutePath() + File.separator + "ChildFileB.png");
        final File subdirectory = new File(tempDirectory.getAbsolutePath() + File.separator + "SubDirectory");
        final File childFileC   = new File(subdirectory.getAbsolutePath() + File.separator + "ChildFileC.jpg");

        childFileA.createNewFile();
        childFileB.createNewFile();
        subdirectory.mkdir();
        childFileC.createNewFile();

        //zip the directory created
        ZipUtility.zip(tempDirectory, tempZipFile);

        Assert.assertTrue("The zip utility did not create the zip file in the expected location.", tempZipFile.exists());

        //unzip the directory zipped and check its contents
        ZipUtility.unzip(tempZipFile, tempDirectory2);

        //Check Directory Structure
        File[] unzippedFiles = tempDirectory2.listFiles();

        //Check ChildFileA location
        Assert.assertEquals(tempDirectory2.getAbsolutePath() + File.separator + childFileA.getName(),
                unzippedFiles[0].getAbsolutePath());

        //Check ChildFileB location
        Assert.assertEquals(tempDirectory2.getAbsolutePath() + File.separator + childFileB.getName(),
                unzippedFiles[1].getAbsolutePath());

        //Check Subdirectory location
        Assert.assertEquals(tempDirectory2.getAbsolutePath() + File.separator + subdirectory.getName(),
                unzippedFiles[2].getAbsolutePath());

        //Check subdirectory if it contains the the childFileC
        File[] subDirectoryFiles = unzippedFiles[2].listFiles();
        Assert.assertEquals(tempDirectory2.getAbsolutePath() +
                        File.separator +
                        subdirectory.getName() +
                        File.separator +
                        childFileC.getName(),
                subDirectoryFiles[0].getAbsolutePath());
    }

    @Test
    public void testUnzipOutput() throws IOException
    {
        //Expected Directory Structure
        //test
        //  images
        //       IMGA.JPG
        //       IMGB.JPG
        //       IMGC.JPG
        //  thumbs
        //       IMG1.JPG
        //       IMG2.JPG
        //       IMG3.JPG
        //  diary.kml
        //  kml_samples.kml

        try(final InputStream      inputStream = this.getClass().getClassLoader().getResourceAsStream("test.zip");
            final FileOutputStream out         = new FileOutputStream(tempZipFile);)
        {
            IOUtil.copy(inputStream, out);
            ZipUtility.unzip(tempZipFile, tempDirectory);

            //Check Directory Structure
            File[] unzippedFiles = tempDirectory.listFiles();
            File thumbsDirectory = new File(tempDirectory.getAbsolutePath() + File.separator + "thumbs");

            File image1 = new File(thumbsDirectory.getAbsolutePath() + File.separator + "IMG1.JPG");
            File image2 = new File(thumbsDirectory.getAbsolutePath() + File.separator + "IMG2.JPG");
            File image3 = new File(thumbsDirectory.getAbsolutePath() + File.separator + "IMG3.JPG");

            File imageDirectory = new File(tempDirectory.getAbsolutePath() + File.separator + "images");

            File imageA = new File(imageDirectory.getAbsolutePath() + File.separator + "IMGA.JPG");
            File imageB = new File(imageDirectory.getAbsolutePath() + File.separator + "IMGB.JPG");
            File imageC = new File(imageDirectory.getAbsolutePath() + File.separator + "IMGC.JPG");

            File diaryKml   = new File(tempDirectory.getAbsolutePath() + File.separator + "diary.kml");
            File kmlSamples = new File(tempDirectory.getAbsolutePath() + File.separator + "kml_samples.kml");

            Assert.assertTrue(String.format("ZipUtility.unzip did not unzip the test.zip file properly.  Did not create %s in the correct location.",
                                            thumbsDirectory.getName()),
                              thumbsDirectory.exists());

            Assert.assertTrue(String.format("ZipUtility.unzip did not unzip the test.zip file properly.  Did not create %s in the correct location.",
                                            image1.getName()),
                              image1.exists());

            Assert.assertTrue(String.format("ZipUtility.unzip did not unzip the test.zip file properly.  Did not create %s in the correct location.",
                                            image2.getName()),
                              image2.exists());

            Assert.assertTrue(String.format("ZipUtility.unzip did not unzip the test.zip file properly.  Did not create %s in the correct location.",
                                            image3.getName()),
                              image3.exists());

            Assert.assertTrue(String.format("ZipUtility.unzip did not unzip the test.zip file properly.  Did not create %s in the correct location.",
                                            imageA.getName()),
                              imageA.exists());

            Assert.assertTrue(String.format("ZipUtility.unzip did not unzip the test.zip file properly.  Did not create %s in the correct location.",
                                            imageB.getName()),
                              imageB.exists());

            Assert.assertTrue(String.format("ZipUtility.unzip did not unzip the test.zip file properly.  Did not create %s in the correct location.",
                                            imageC.getName()),
                              imageC.exists());

            Assert.assertTrue(String.format("ZipUtility.unzip did not unzip the test.zip file properly.  Did not create %s in the correct location.",
                                            diaryKml.getName()),
                              diaryKml.exists());

            Assert.assertTrue(String.format("ZipUtility.unzip did not unzip the test.zip file properly.  Did not create %s in the correct location.",
                                            kmlSamples.getName()),
                              kmlSamples.exists());
        }
    }
}
