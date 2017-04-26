package mil.emp3.core.services.kml;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mil.emp3.api.exceptions.EMP_Exception;

/**
 * Unzip a kmzFile. This is purposly not embedded in KMLRequest as in future there will be a requirement to simply unzip a
 * specified KMZ file.
 */
public class KMZFile {
    private static String TAG = KMZFile.class.getSimpleName();
    private final int READ_BUFFER_SIZE = 4096;

    public interface IKMZFileRerquest {
        File getDestinationDir();
        String getSourceFilePath();
        String getKmlFilePath();
        void setKmlFilePath(String kmlFilePath);
    }

    /**
     * https://developers.google.com/kml/documentation/kmzarchives for structure of KMZ file, Important thing to note is:
     *     a KMZ file can refer to other kmz files - we don't support that
     *     There shouldn't be more than one kml file in the archive
     *     File references are relative.
     *     kml file is always in the root folder.
     * @param request
     * @throws EMP_Exception
     */
    protected void unzipKMZFile(IKMZFileRerquest request) throws EMP_Exception {

        try {
            String kmlFilePath = null;
            ZipFile zipFile = new ZipFile(request.getSourceFilePath());
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry zipEntry = entries.nextElement();
                Log.v(TAG, "zipEntry " + zipEntry.getName());

                // If it is a directory then make a new directory and continue.
                if(zipEntry.isDirectory()) {
                    File directory = new File(request.getDestinationDir() + File.separator + zipEntry.getName());
                    directory.mkdirs();
                    continue;
                }

                // Look for a kml file that needs to be parsed. We pick the first one that we find. Technically there should be only
                // one KML file in the KMZ archive.
                if(((null == kmlFilePath) || (0 == kmlFilePath.length())) && (zipEntry.getName().endsWith(".kml"))) {
                    request.setKmlFilePath(request.getDestinationDir() + File.separator + zipEntry.getName());
                    Log.d(TAG, "kmlFilePath " + request.getKmlFilePath());
                }

                // Copy the file to destination directory
                try (BufferedInputStream bis = new  BufferedInputStream(zipFile.getInputStream(zipEntry));
                     BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(request.getDestinationDir(), zipEntry.getName())))) {
                    byte[] buf = new byte[READ_BUFFER_SIZE];
                    int ii;
                    while ((ii = bis.read(buf, 0, READ_BUFFER_SIZE)) != -1) {
                        bos.write(buf, 0, ii);
                    }
                    bos.flush();
                }
            }

        } catch (IOException | SecurityException e) {
            Log.e(TAG, "KMLProcessor-unzipKMZFile " + request.getSourceFilePath(), e);
            if(request.getSourceFilePath().endsWith(".kml")) {
                // So it is a KML file not a KMZ file
                request.setKmlFilePath(request.getSourceFilePath());
                Log.d(TAG, "kmlFilePath " + request.getKmlFilePath());
                return;
            }
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getMessage());
        }
    }
}
