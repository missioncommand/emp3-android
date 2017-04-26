package mil.emp3.core.services.kml;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IKML;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.IMap;

public class KMLSRequest implements KMZFile.IKMZFileRerquest{
    private static String TAG = KMLSRequest.class.getSimpleName();

    private final int READ_BUFFER_SIZE = 4096;

    private final String KMLS_ROOT = "KMLS";

    private static boolean directoryBuilt = false;
    private static File kmlsRoot;

    private IMap map;
    private IKMLS service;
    private String kmzFilePath = null;
    private File kmzDirectory = null;
    private String kmlFilePath = null;
    private IKML feature;

    KMLSRequest(IMap map, IKMLS service) {
        this.map = map;
        this.service = service;
    }

    @Override
    public File getDestinationDir() {
        return kmzDirectory;
    }

    @Override
    public String getSourceFilePath() {
        return kmzFilePath;
    }

    @Override
    public String getKmlFilePath() {
        return kmlFilePath;
    }

    @Override
    public void setKmlFilePath(String kmlFilePath) {
        this.kmlFilePath = kmlFilePath;
    }

    public IMap getMap() {
        return map;
    }

    public void setMap(IMap map) {
        this.map = map;
    }

    public IKMLS getService() {
        return service;
    }

    public void setService(IKMLS service) {
        this.service = service;
    }

    public File getKmzDirectory() { return kmzDirectory; }

    public String getKmzFilePath() { return kmzFilePath; }

    public IKML getFeature() { return feature; }

    public void setFeature(IKML feature) { this.feature = feature; }

    private void buildDirectory(Context context) {
        try {
            if (!directoryBuilt) {
                Log.d(TAG, "buildDirectory");
                kmlsRoot = context.getDir(KMLS_ROOT, Context.MODE_PRIVATE);
                this.kmlsRoot.mkdirs();
                cleanDirectory(this.kmlsRoot);   // Cleanup the space used by volatile service from previous run. This will remove the root itself.
                this.kmlsRoot.mkdirs();
                directoryBuilt = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to build directory ", e);
        }
    }

    /**
     * Copy the KMZ file from the specified URL.
     * @return
     */
    void copyKMZ() throws EMP_Exception {

        // Following would be much more efficient but it is truncating the file. If we have to support
        // Large file sizes then we should investigate the code below.

        // URL website = new URL("http://www.website.com/information.asp");
        // ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        // FileOutputStream fos = new FileOutputStream("information.html");
        // fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        try {
            buildDirectory(service.getContext());
            char forwardSlashChar = '/';

            Log.d(TAG, "protocol " + service.getURL().getProtocol() + " HOST " + service.getURL().getHost()
                    + " File " + service.getURL().getFile());
            Log.d(TAG, "File.separator " + File.separator);
            URLConnection connection = service.getURL().openConnection();
            String path = service.getURL().getPath();

            String hostName = service.getURL().getHost();
            if(null == hostName) {
                hostName = "file";
            }

            File directory;
            String fileName;
            String targetFilePath;

            // Figure out the destination directory and destination file path.
            if (service.getURL().getProtocol().equals("file")) {
                String file = service.getURL().getFile();
                if (file.contains(":")) {
                    String split[] = file.split(":");
                    file = split[split.length - 1];
                }
                fileName = file.substring(file.lastIndexOf(forwardSlashChar) + 1);
                directory = new File(kmlsRoot + File.separator + file.replace(forwardSlashChar, '.'));
            } else {
                directory = new File(kmlsRoot + File.separator + hostName.replace(File.separatorChar, '.') + path.replace(File.separatorChar, '.'));
                fileName = path.substring(path.lastIndexOf(File.separatorChar) + 1);
            }

            targetFilePath = directory + File.separator + fileName;

            Log.v(TAG, "File Path from URL " + path + " target directory " + directory + " target fileName " + fileName);

            if(!directory.exists()) {
                directory.mkdirs();
            }
            File targetFile = new File(targetFilePath);
            if(targetFile.exists() && (targetFile.length() > 0) && (targetFile.canRead())) {
                Log.i(TAG, "file " + targetFilePath + " exists, no need to fetch");
            } else {
                cleanDirectory(directory);

                try (InputStream input = connection.getInputStream();
                     OutputStream output = new FileOutputStream(targetFilePath)) {
                    byte[] buffer = new byte[READ_BUFFER_SIZE];
                    int n;
                    int total = 0;
                    while ((n = input.read(buffer)) != -1) {
                        output.write(buffer, 0, n);
                        total += n;
                    }
                    Log.v(TAG, "transferred " + total);
                }
            }

            kmzFilePath = targetFilePath;
            kmzDirectory = directory;
        } catch (IOException ioe) {
            Log.e(TAG, "KMLSProcessor-copyKMZ failed " + service.getURL(), ioe);
            throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, ioe.getMessage());
        }
    }

    /**
     * Cleans the directory. Avoiding get apache io-commons?
     * @param directory
     */
    private void cleanDirectory(File directory) {
        File[] listOfFiles = directory.listFiles();
        if(listOfFiles != null) {
            for(File file: listOfFiles) {
                if(file.isDirectory()) {
                    cleanDirectory(file);
                    file.delete();
                } else {
                    file.delete();
                }
            }
        }
    }

    /**
     * Recursively lists files in a directory.
     * @param directory
     */
    static void listFiles(File directory) {
        if(null != directory) {
            Log.v(TAG, "listFiles for " + directory.getAbsolutePath());
            File[] files = directory.listFiles();
            if (null != files) {
                for (File file : files) {
                    if(file.isDirectory()) {
                        listFiles(file);
                    } else {
                        Log.v(TAG, file.getName() + " " + file.length());
                    }
                }
            }
        }
    }

    /**
     * This is used in debug mode only
     */
    public static void listKmlsRoot() {
        if(null != kmlsRoot) {
            listFiles(kmlsRoot);
        }
    }
}
