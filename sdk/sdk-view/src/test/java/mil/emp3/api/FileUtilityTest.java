package mil.emp3.api;


import org.assertj.core.util.Files;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import mil.emp3.api.utils.FileUtility;

/**
 * @author Jenifer Cochran
 */
@RunWith(RobolectricTestRunner.class)
public class FileUtilityTest
{
    @Test
    public void isChildDirectoryTestFalseNonExistant()
    {
        File nonExistantDirectory = new File(getTemporaryDirectoryPath());
        File nonExistantSubDirectory = new File(getTemporaryDirectoryPath() + getRandomDirectoryString());

        Assert.assertFalse("FileUtility.isChildDirectory returned true instead of false when the directories did not exist.",
                           FileUtility.isChildDirectory(nonExistantDirectory, nonExistantSubDirectory));
    }

    @Test
    public void isChildDirectoryTestFalseNotADirectory() throws IOException
    {
        File tempFile = File.createTempFile("file", ".txt");
        File childDirectory = createTemporaryDirectory();

        Assert.assertFalse("FileUtility.isChildDirectory returned true when passed in a File instead of a Directory as a possible parent directory.",
                           FileUtility.isChildDirectory(tempFile, childDirectory));

        FileUtility.deleteFolder(childDirectory);
        tempFile.delete();
    }

    @Test
    public void isChildDirectoryTestTrue() throws IOException
    {
        File tempParentDirectory = createTemporaryDirectory();
        File tempSubDirectory    = createTemporarySubDirectory(tempParentDirectory); //create subdirectory

        Assert.assertTrue(String.format("FileUtility returned false instead of true when testing if directory: %s is a child directory of directory: %s.",
                                        tempSubDirectory.getAbsolutePath(),
                                        tempParentDirectory.getAbsolutePath()),
                          FileUtility.isChildDirectory(tempParentDirectory, tempSubDirectory));

        FileUtility.deleteFolder(tempParentDirectory);
        FileUtility.deleteFolder(tempSubDirectory);
    }

    @Test
    public void isChildDirectoryTestFalse() throws IOException
    {
        File tempParentDirectory = createTemporaryDirectory(); //these will both be tmp/Somecrazyrandomname
        File tempSubDirectory    = createTemporaryDirectory();

        Assert.assertFalse(String.format("FileUtility returned true instead of false when testing if directory: %s is a child directory of directory: %s.",
                                         tempSubDirectory.getAbsolutePath(),
                                         tempParentDirectory.getAbsolutePath()),
                           FileUtility.isChildDirectory(tempParentDirectory, tempSubDirectory));

        FileUtility.deleteFolder(tempParentDirectory);
        FileUtility.deleteFolder(tempSubDirectory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createOutputDirectoryTestIllegalArgumentException() throws IOException
    {
        File tempFile = File.createTempFile("File", ".txt");
        //since this is a file and not a directory, should throw IAE
        FileUtility.createOutputDirectory(tempFile.getAbsolutePath());
        tempFile.delete();
    }

    @Test
    public void createOutputDirectoryTestExists() throws IOException
    {
        String newDirectoryPath = getTemporaryDirectoryPath();
        FileUtility.createOutputDirectory(newDirectoryPath);

        Assert.assertTrue("FileUtility did not create a directory in the given path.",
                          new File(newDirectoryPath).exists());

        FileUtility.deleteFolder(new File(newDirectoryPath));
    }

    @Test
    public void testDirectoryExistsTrue() throws IOException
    {
        //should pass since it is a directory and it exists
        File tempDirectory = createTemporaryDirectory();
        Assert.assertTrue("FileUtility.directoryExists returned false instead of true.",
                          FileUtility.directoryExists(tempDirectory.getAbsolutePath()));
        FileUtility.deleteFolder(tempDirectory);
    }

    @Test
    public void testDirectoryExistsFalse() throws IOException
    {
        //should fail since it isn't a directory
        File tempFile = File.createTempFile("File", ".txt");
        Assert.assertFalse("FileUtility.directoryExists returned true instead of false, when given a path to a file.",
                           FileUtility.directoryExists(tempFile.getAbsolutePath()));

        tempFile.delete();
    }

    @Test
    public void testDirectoryExistsFalse2()
    {
        //Should fail since directory doesn't exist
        String nonExistantPath = Files.temporaryFolderPath() + getRandomDirectoryString();
        //ensure the path doesn't exist
        while(new File(nonExistantPath).exists())
        {
            nonExistantPath = nonExistantPath + UUID.randomUUID();
        }

        Assert.assertFalse("FileUtility.directoryExists returned true when given a path to a directory that doesn't exist.",
                           FileUtility.directoryExists(nonExistantPath));
    }

    @Test
    public void testDeleteFolder() throws IOException
    {
        //create a tempfolder with files and folders within
        //and ensure that everything is deleted
        //including the folder itself
        File tempFolder = createTemporaryDirectory();
        File tempFile = File.createTempFile("Test", ".png", tempFolder);
        File tempSubDirectory = createTemporarySubDirectory(tempFolder);

        File tempFileInSubDirectory = File.createTempFile("Test2", ".txt", tempSubDirectory);

        FileUtility.deleteFolder(tempFolder);

        Assert.assertFalse("FileUtility.deleteFolder did not delete the top level directory.",
                           tempFolder.exists());
        Assert.assertFalse("FileUtility.deleteFolder did not delete a file within the top level directory. (did not remove its contents).",
                           tempFile.exists());
        Assert.assertFalse("FileUtility.deleteFolder did not delete a subfolder within the folder it pointed to. (did not remove a folder in its contents)",
                           tempSubDirectory.exists());
        Assert.assertFalse("FileUtility.deleteFolder did not delete a subfolder's contents.",
                           tempFileInSubDirectory.exists());
    }

    private static File createTemporarySubDirectory(File baseDirectory)
    {
        File tempSubDirectory = new File(baseDirectory.getAbsolutePath() + getRandomDirectoryString());
        tempSubDirectory.mkdirs();
        return tempSubDirectory;
    }

    private static String getRandomDirectoryString()
    {
        return File.separator + UUID.randomUUID().toString();
    }

    private static String getTemporaryDirectoryPath()
    {
        return Files.temporaryFolderPath() + getRandomDirectoryString();
    }

    private static File createTemporaryDirectory() throws IOException
    {
        File tempDirectory = new File(getTemporaryDirectoryPath());
        tempDirectory.mkdirs();
        return tempDirectory;
    }
}
