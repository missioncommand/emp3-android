package mil.emp3.api.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
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
     * @param directory the location of the file/folder that needs to be zipped
     * @param zipfile the location where the file/folder will be zipped to
     * @return true if the zipping was successful, false otherwise
     */
    public static void zip(File directory, File zipfile) throws IOException
    {
        URI         base  = directory.toURI();
        Deque<File> queue = new LinkedList<File>();
        queue.push(directory);

        try (OutputStream out = new FileOutputStream(zipfile);
             ZipOutputStream zout = new ZipOutputStream(out);)
        {
            while (!queue.isEmpty())
            {
                directory = queue.pop();
                for (File kid : directory.listFiles())
                {
                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory())
                    {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    }
                    else
                    {
                        zout.putNextEntry(new ZipEntry(name));
                        copy(kid, zout);
                        zout.closeEntry();
                    }
                }
            }
        }
    }

    private static void copy(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        while (true)
        {
            int readCount = in.read(buffer);
            if (readCount < 0)
            {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    private static void copy(File file, OutputStream out) throws IOException
    {
        try (InputStream in = new FileInputStream(file))
        {
            copy(in, out);
        }
    }
}
