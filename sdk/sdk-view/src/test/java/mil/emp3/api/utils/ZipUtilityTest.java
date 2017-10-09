package mil.emp3.api.utils;

import org.assertj.core.util.Files;
import org.codehaus.plexus.util.IOUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by jenifer.cochran@rgi-corp.local on 10/9/17.
 */

public class ZipUtilityTest
{

    @Test(expected = NullPointerException.class)
    public void testZipNullPointerExceptionDirectory() throws IOException
    {
        final File tempFile = Files.newTemporaryFile();
        try
        {
            ZipUtility.zip(null, tempFile);
        }
        finally
        {
            tempFile.delete();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testZipNullPointerExceptionZipFile() throws IOException
    {
        final File tempDirectory = Files.newTemporaryFolder();
        try
        {
            ZipUtility.zip(tempDirectory, null);
        }
        finally
        {
            FileUtility.deleteFolder(tempDirectory);
        }
    }

    @Test(expected = NullPointerException.class)
    public void testUnzipNullPointerExceptionDirectory() throws IOException
    {
        final File tempFile = Files.newTemporaryFile();
        try
        {
            ZipUtility.unzip(tempFile, null);
        }
        finally
        {
            tempFile.delete();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testUnzipNullPointerExceptionZipFile() throws IOException
    {
        final File tempDirectory = Files.newTemporaryFolder();
        try
        {
            ZipUtility.unzip(null, tempDirectory);
        }
        finally
        {
            FileUtility.deleteFolder(tempDirectory);
        }
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
        final File topDirectory = Files.newTemporaryFolder();
        final File childFileA   = new File(topDirectory.getAbsolutePath() + File.separator + "ChildFileA.txt");
        final File childFileB   = new File(topDirectory.getAbsolutePath() + File.separator + "ChildFileB.png");
        final File subdirectory = new File(topDirectory.getAbsolutePath() + File.separator + "SubDirectory");
        final File childFileC   = new File(subdirectory.getAbsolutePath() + File.separator + "ChildFileC.jpg");

        childFileA.createNewFile();
        childFileB.createNewFile();
        subdirectory.mkdirs();
        childFileC.createNewFile();

        final File unzippedOutputDirectory = Files.newTemporaryFolder();
        final File zipTempFile             = File.createTempFile("testZip", ".zip");

        try
        {
            ZipUtility.zip(topDirectory, zipTempFile);
            Assert.assertTrue("The zip utility did not create the zip file in the expected location.",zipTempFile.exists());

            ZipUtility.unzip(zipTempFile, unzippedOutputDirectory);

            //Check Directory Structure
            File[] unzippedFiles = unzippedOutputDirectory.listFiles();

            //Check ChildFileA location
            Assert.assertEquals(unzippedOutputDirectory.getAbsolutePath() + File.separator + childFileA.getName(),
                                unzippedFiles[0].getAbsolutePath());

            //Check ChildFileB location
            Assert.assertEquals(unzippedOutputDirectory.getAbsolutePath() + File.separator + childFileB.getName(),
                                unzippedFiles[1].getAbsolutePath());

            //Check Subdirectory location
            Assert.assertEquals(unzippedOutputDirectory.getAbsolutePath() + File.separator + subdirectory.getName(),
                                unzippedFiles[2].getAbsolutePath());

            //Check subdirectory if it contains the the childFileC
            File[] subDirectoryFiles = unzippedFiles[2].listFiles();
            Assert.assertEquals(unzippedOutputDirectory.getAbsolutePath()  +
                                 File.separator                            +
                                 subdirectory.getName()                    +
                                 File.separator                            +
                                 childFileC.getName(),
                                subDirectoryFiles[0].getAbsolutePath());

        }
        finally
        {
            FileUtility.deleteFolder(topDirectory);
            FileUtility.deleteFolder(unzippedOutputDirectory);
            zipTempFile.delete();
        }
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


        final File unzippedOutputDirectory = Files.newTemporaryFolder();
        final File zipTempFile             = Files.newTemporaryFile();

        try(final InputStream      inputStream = this.getClass().getClassLoader().getResourceAsStream("test.zip");
            final FileOutputStream out         = new FileOutputStream(zipTempFile);)
        {
            IOUtil.copy(inputStream, out);
            ZipUtility.unzip(zipTempFile, unzippedOutputDirectory);

            //Check Directory Structure
            File[] unzippedFiles = unzippedOutputDirectory.listFiles();
            File thumbsDirectory = new File(unzippedOutputDirectory.getAbsolutePath() + File.separator + "thumbs");

            File image1 = new File(thumbsDirectory.getAbsolutePath() + File.separator + "IMG1.JPG");
            File image2 = new File(thumbsDirectory.getAbsolutePath() + File.separator + "IMG2.JPG");
            File image3 = new File(thumbsDirectory.getAbsolutePath() + File.separator + "IMG3.JPG");

            File imageDirectory = new File(unzippedOutputDirectory.getAbsolutePath() + File.separator + "images");

            File imageA = new File(imageDirectory.getAbsolutePath() + File.separator + "IMGA.JPG");
            File imageB = new File(imageDirectory.getAbsolutePath() + File.separator + "IMGB.JPG");
            File imageC = new File(imageDirectory.getAbsolutePath() + File.separator + "IMGC.JPG");

            File diaryKml   = new File(unzippedOutputDirectory.getAbsolutePath() + File.separator + "diary.kml");
            File kmlSamples = new File(unzippedOutputDirectory.getAbsolutePath() + File.separator + "kml_samples.kml");

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
        finally
        {
            FileUtility.deleteFolder(unzippedOutputDirectory);
            zipTempFile.delete();
        }
    }
}
