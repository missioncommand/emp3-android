package mil.emp3.api.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Deque;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * Zip utility to perform zipping operations on Files and Directories.
 *
 * @author Jenifer Cochran
 * Resource: https://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
 */

public final class ZipUtility
{
    /**
     * Private Constructor to avoid instantiation of the class
     */
    private ZipUtility()
    {

    }

    /**
     * Zips a file at a location and places the resulting zip file at the toLocation
     * Example: (where the strings indicate the file paths) ZipUtility.zip("downloads/myfolder", "downloads/myFolder.zip");
     * Will maintain the directory structure.
     *
     * @param directory the location of the file/folder that needs to be zipped
     * @param zipFile the location where the file/folder will be zipped to
     */
    public static void zip(File  directory,
                           final File zipFile) throws IOException
    {
        final URI         base  = directory.toURI();
        final Deque<File> queue = new LinkedList<>();
        queue.push(directory);

        try (final OutputStream    fileOutputStream = new FileOutputStream(zipFile);
             final ZipOutputStream zipOutputStream  = new ZipOutputStream(fileOutputStream))
        {
            while (!queue.isEmpty())
            {
                directory = queue.pop();
                for (final File child : directory.listFiles())
                {
                    String name = base.relativize(child.toURI()).getPath();
                    if (child.isDirectory())
                    {
                        queue.push(child);
                        name = name.endsWith(File.separator) ? name : name + File.separator;
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
     * UnZips a file at a location and places the resulting zip file at the toLocation
     * Example: (where the strings indicate the file paths) ZipUtility.zip("downloads/myfolder", "downloads/myFolder.zip");
     * Will maintain the directory structure.
     *
     * @param directory the location of the folder to extract the files to
     * @param zipFile the location where the folder is zipped
     */
    public static void unzip(final File zipFile,
                             final File directory) throws IOException
    {
        final ZipFile                         zfile   = new ZipFile(zipFile);
        final Enumeration<? extends ZipEntry> entries = zfile.entries();

        while (entries.hasMoreElements())
        {
            final ZipEntry entry = entries.nextElement();
            final File     file  = new File(directory, entry.getName());

            if (entry.isDirectory())
            {
                file.mkdirs();
            }
            else
            {
                file.getParentFile().mkdirs();

                try (InputStream in = zfile.getInputStream(entry))
                {
                    copy(in, file);
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
    private static void copy(final InputStream  inputStream,
                             final OutputStream outputStream) throws IOException
    {
        final byte[] buffer = new byte[1024];
        while (true)
        {
            final int readCount = inputStream.read(buffer);
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
    private static void copy(final File         file,
                             final OutputStream outputStream) throws IOException
    {
        try (InputStream in = new FileInputStream(file))
        {
            copy(in, outputStream);
        }
    }


    /**
     * Copies the inputStream to the File location
     *
     * @param file the File to copy to
     * @param inputStream the inputStream to copy from
     * @throws IOException Reading or Writing exception
     */
    private static void copy(final InputStream inputStream,
                             final File        file) throws IOException
    {

        try(OutputStream out = new FileOutputStream(file))
        {
            copy(inputStream, out);
        }
    }
}
