package mil.emp3.api.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author Jenifer Cochran
 * Resource: https://stackoverflow.com/questions/6683600/zip-compress-a-folder-full-of-files-on-android
 */

public class ZipUtility
{
    /**
     * Private Constructor to avoid instantiation of the class
     */
    private ZipUtility()
    {

    }

    private final static int BUFFER = 2048;

    /**
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     *
     * @param sourcePath the location of the file/folder that needs to be zipped
     * @param toLocation the location where the file/folder will be zipped to
     * @return true if the zipping was successful, false otherwise
     */

    public boolean zipFileAtPath(String sourcePath, String toLocation)
    {
        File sourceFile = new File(sourcePath);

        try (FileOutputStream dest = new FileOutputStream(toLocation);
             ZipOutputStream  out = new ZipOutputStream(new BufferedOutputStream(dest)))
        {

            if (sourceFile.isDirectory())
            {
                zipSubFolder(out, sourceFile, sourceFile.getParent().length());
            }
            else
            {
                addZipFileEntry(sourcePath, sourcePath, out);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Zips a Subfolder
     *
     * @param out    ZipOutputStream to write to
     * @param folder  the folder to zip
     * @param basePathLength  the length of the base path
     * @throws IOException if read/write error or if the file doesn't exist
     */
    private void zipSubFolder(final ZipOutputStream out,
                              final File            folder,
                              final int             basePathLength) throws IOException
    {
        File[] fileList = folder.listFiles();
        for (File file : fileList)
        {
            if (file.isDirectory())
            {
                zipSubFolder(out, file, basePathLength);
            }
            else
            {
                String unmodifiedFilePath = file.getPath();
                String relativePath       = unmodifiedFilePath.substring(basePathLength);

                addZipFileEntry(unmodifiedFilePath, relativePath, out);
            }
        }
    }

    /**
     * Zips a file into the ZipOutputStream
     *
     * @param filePath  the location of the file to zip
     * @param zipEntryPath the location of where the zip entry needs to be stored
     * @param out  the ZipOutputStream to write the data to
     * @throws IOException if there are file reading/writing exceptions or if the file doesn't exist
     */
    private static void addZipFileEntry(final String          filePath,
                                        final String          zipEntryPath,
                                        final ZipOutputStream out) throws IOException
    {

        byte data[] = new byte[BUFFER];

        try (FileInputStream     fi     = new FileInputStream(filePath);
             BufferedInputStream origin = new BufferedInputStream(fi, BUFFER))
        {
            ZipEntry entry = new ZipEntry(zipEntryPath);

            out.putNextEntry(entry);
            int count;

            while ((count = origin.read(data, 0, BUFFER)) != -1)
            {
                out.write(data, 0, count);
            }
        }
    }
}
