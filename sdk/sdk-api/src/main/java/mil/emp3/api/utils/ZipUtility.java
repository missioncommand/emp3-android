package mil.emp3.api.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Zip utility to perform zipping operations on Files and Directories.
 *
 * @author Jenifer Cochran
 * Resource: https://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
 */

public class ZipUtility
{
    /**
     * Private Constructor to avoid instantiation of the class
     */
    private ZipUtility()
    {

    }

    /**
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: zipFileAtPath("downloads/myfolder", "downloads/myFolder.zip");
     * Will maintain the directory structure.
     *
     * @param directory the location of the file/folder that needs to be zipped
     * @param zipFile the location where the file/folder will be zipped to
     * @return true if the zipping was successful, false otherwise
     */
    public static void zip(File directory,
                           File zipFile) throws IOException
    {
        URI         base  = directory.toURI();
        Deque<File> queue = new LinkedList<>();
        queue.push(directory);

        try (OutputStream    fileOutputStream = new FileOutputStream(zipFile);
             ZipOutputStream zipOutputStream  = new ZipOutputStream(fileOutputStream);)
        {
            while (!queue.isEmpty())
            {
                directory = queue.pop();
                for (File child : directory.listFiles())
                {
                    String name = base.relativize(child.toURI()).getPath();
                    if (child.isDirectory())
                    {
                        queue.push(child);
                        name = name.endsWith(File.pathSeparator) ? name : name + File.pathSeparator;
                        zipOutputStream.putNextEntry(new ZipEntry(name));
                    }
                    else
                    {
                        zipOutputStream.putNextEntry(new ZipEntry(name));
                        copy(child, zipOutputStream);
                        zipOutputStream.closeEntry();
                    }
                }
            }
        }
    }

    /**
     * Copies the inputStream to the OutputStream
     *
     * @param inputStream the stream of data to read from
     * @param outputStream the outputStream to write the data to
     * @throws IOException Reading or Writing exception
     */
    private static void copy(InputStream  inputStream,
                             OutputStream outputStream) throws IOException
    {
        byte[] buffer = new byte[1024];
        while (true)
        {
            int readCount = inputStream.read(buffer);
            if (readCount < 0)
            {
                break;
            }
            outputStream.write(buffer, 0, readCount);
        }
    }


    /**
     * Copies the inputStream to the OutputStream
     *
     * @param file the data to read from
     * @param outputStream the outputStream to write the data to
     * @throws IOException Reading or Writing exception
     */
    private static void copy(File         file,
                             OutputStream outputStream) throws IOException
    {
        try (InputStream in = new FileInputStream(file))
        {
            copy(in, outputStream);
        }
    }
}
