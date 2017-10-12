package mil.emp3.api.utils;

import android.os.Environment;

import java.io.File;
import java.io.IOException;

/**
 * File Utility class to perform File and Directory operations.
 *
 * @author Jenifer Cochran
 */

public final class FileUtility
{
    /**
     * Prevents instantiation of the class
     */
    private FileUtility()
    {

    }

    /**
     * Deletes all of the Folder's contents
     *
     * @param folder the folder to delete
     */
    public static void deleteFolder(final File folder)
    {
        final File[] files = folder.listFiles();
        if (files != null)
        {
            for (final File file : files)
            {
                if (file.isDirectory())
                {
                    deleteFolder(file);
                } else
                {
                    file.delete();
                }
            }
        }
        folder.delete();
    }

    /***
     * Returns true if the possible child directory shares a directory with the possibleParentDirectory
     * (i.e. ParentDirectory : directoryA/directoryB/directoryC </br>
     *       childDirectory :  directoryA/directoryB/directoryC/directoryD </br>
     *       would return true and </br>
     *       ParentDirectory : directoryB/directoryD </br>
     *       childDirectory :  directoryB/directoryE
     *       would return false)
     *
     * @param possibleParentDirectory the possible parent directory of the possibleChildDirectory passed in
     * @param possibleChildDirectory the possible child directory of the possibleParentDirectory passed in
     * @return true if the child shares a directory with its parent, false otherwise.
     */
    public static boolean isChildDirectory(final File possibleParentDirectory,
                                           final File possibleChildDirectory)
    {
        try
        {
            final File parent = possibleParentDirectory.getCanonicalFile();
            if (!parent.exists() || !parent.isDirectory())
            {
                // this cannot possibly be the parent
                return false;
            }

            File child = possibleChildDirectory.getCanonicalFile();
            while (child != null)
            {
                if (child.equals(parent))
                {
                    return true;
                }
                child = child.getParentFile();
            }
            // No match found, and we've hit the root directory
            return false;
        }
        catch (final IOException e)
        {
            return false;
        }
    }


    /**
     * Creates the a directory
     *
     * @param outputDirectory the location of the directory
     */
    public static void createOutputDirectory(final String outputDirectory)
    {
        final File directory = new File(outputDirectory);
        directory.mkdirs();
        if(!directory.isDirectory())
        {
            throw new IllegalArgumentException(String.format("The temporaryDirectory must be a path to a Directory. %s is not a directory.",
                                                             outputDirectory));
        }
    }

    /**
     * Checks if external storage is available for read and write
     * @return true if the external storage is available for read and write,
     *         false otherwise
     */
    public static boolean isExternalStorageWritable()
    {
        final String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /**
     * Checks if the location is a directory as
     * well as if it exists.
     *
     * @param directoryPath the location of the directory to check
     * @return true if is a directory and it exists, false otherwise
     */
    public static boolean directoryExists(final String directoryPath)
    {
        final File dir = new File(directoryPath);
        return dir.exists() && dir.isDirectory();
    }

}
