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
    public static void deleteFolder(File folder)
    {
        File[] files = folder.listFiles();
        if (files != null)
        {
            for (File file : files)
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

    public static boolean isChildDirectory(File possibleParentDirectory, File possibleChildDirectory)
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
        catch (IOException e)
        {
            return false;
        }
    }

    /**
     * Creates the a directory
     *
     * @param outputDirectory the location of the directory
     */
    public static void createOutputDirectory(String outputDirectory)
    {
        File directory = new File(outputDirectory);
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
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }


    /**
     * Checks if the location is a directory as
     * well as if it exists.
     *
     * @param directoryPath the location of the directory to check
     * @return true if is a directory and it exists, false otherwise
     */
    public static boolean directoryExists(String directoryPath)
    {
        File dir = new File(directoryPath);
        return dir.exists() && dir.isDirectory();
    }

}
