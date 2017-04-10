package mil.emp3.core.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import mil.emp3.api.KML;
import mil.emp3.api.KMLS;
import mil.emp3.api.enums.KMLSEventEnum;
import mil.emp3.api.enums.KMLSStatusEnum;
import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.listeners.IKMLSEventListener;
import mil.emp3.core.storage.ClientMapToMapInstance;

/**
 * Processes KML service requests from the client application. Basic interfaces for the application are
 * addMapService and removeMapService. Request is queued to a Thread and application call is returned.
 *
 * Some design decisions (also listed in the issue)
 *
 * 1. Features created by KMZ are treated as a special layer in Map Instance. They are not added to any overlay within the core.
 * 2. Features created via KMZ are not returned when getAllMapFeatures is executed.
 * 3. No event is generated when KMZ processing is complete or fails.
 * 4. KMZ referring to another KMZ is not supported as our parser skips over network links.
 */

public class KMLSProvider {
    private static String TAG = KMLSProvider.class.getSimpleName();
    private static KMLSProvider instance = null;
    private final static String KMLS_ROOT = "KMLS";
    private final static String PERSISTENT_ROOT = "PERSISTENT";
    private final static String VOLATILE_ROOT = "VOLATILE";

    private final IStorageManager storageManager;

    private BlockingQueue<KMLSRequest> queue = new LinkedBlockingQueue<>();

    private KMLSProcessor processor;
    private Thread processorThread;

    private KMLSReporter reporter;

    private boolean directoryBuilt = false;
    private File persistentRoot;
    private File volatileRoot;

    /**
     * KMLProvider is singleton.
     * @param storageManager
     * @return
     */
    public static KMLSProvider create(IStorageManager storageManager) {
        if(null == instance) {
            synchronized(KMLSProvider.class) {
                if(null == instance) {
                    instance = new KMLSProvider(storageManager);
                    instance.init();
                }
            }
        }
        return instance;
    }

    private KMLSProvider(IStorageManager storageManager) {
        this.storageManager = storageManager;
    }

    /**
     * Initialize the processing thread.
     */
    private void init() {
        if(null == processor) {
            processor = new KMLSProcessor();
            processorThread = new Thread(processor);
            processorThread.start();
        }

        if(null == reporter) {
            reporter = new KMLSReporter();
        }
    }

    private void buildDirectory(Context context) {
        try {
            if (!directoryBuilt) {
                File kmlsRoot = context.getDir(KMLS_ROOT, Context.MODE_PRIVATE);
                persistentRoot = new File(kmlsRoot + File.separator + PERSISTENT_ROOT);
                persistentRoot.mkdirs();
                volatileRoot = new File(kmlsRoot + File.separator + VOLATILE_ROOT);
                volatileRoot.mkdirs();
                directoryBuilt = true;
            }
        } catch (Exception e) {
            Log.e(TAG, "failed to build directory ", e);
        }
    }
    /**
     * Queue up the request for KMLProcessor as we don't want to execute it on the UI thread.
     * @param map
     * @param mapService
     * @return
     * @throws EMP_Exception
     */
    public boolean addMapService(IMap map, IKMLS mapService) throws EMP_Exception
    {
        try {
            ClientMapToMapInstance mapMapping = (ClientMapToMapInstance) storageManager.getMapMapping(map);
            if (mapMapping.serviceExists(mapService.getGeoId())) {
                Log.i(TAG, "Attempting to add same KML Service that already exists");
                return false;
            }
            mapMapping.addMapService(mapService);
            queue.put(new KMLSRequest(map, mapService));

            if(mapService instanceof KMLS) {
                KMLS kmls = (KMLS) mapService;
                kmls.setStatus(map, KMLSStatusEnum.QUEUED);
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "addMapService ", e);
        }
        return false;
    }

    /**
     * Holder for KML Request.
     */
    class KMLSRequest {
        IMap map;
        IKMLS service;
        String kmzFilePath = null;
        File kmzDirectory = null;
        String kmlFilePath = null;
        KMLSRequest(IMap map, IKMLS service) {
            this.map = map;
            this.service = service;
        }
    }

    /**
     * Waits on the queue and serially process incoming KML Service Requests.
     */
    class KMLSProcessor implements Runnable {
        private int READ_BUFFER_SIZE = 4096;
        @Override
        public void run() {
            while(!Thread.interrupted()) {
                try {
                    KMLSRequest request = queue.take();
                    Log.d(TAG, "KMLSProcessor processing " + request.service.getURL());

                    ((KMLS) request.service).setStatus(request.map, KMLSStatusEnum.FETCHING);
                    copyKMZ(request);  // Fetch the KMZ either file URL or network URL
                    reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED);
                    listFiles(request.kmzDirectory); // This is just for debugging

                    // File could be either a KMZ file or KML file.
                    boolean isKMZFile = false;
                    try {
                        ((KMLS) request.service).setStatus(request.map, KMLSStatusEnum.EXPLODING);
                        unzipKMZFile(request);
                        reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FILE_EXPLODED);
                        listFiles(request.kmzDirectory);
                        isKMZFile = true;
                    } catch (EMP_Exception e) {
                        Log.i(TAG, "KMLProcessor-run " + e.getMessage(), e);
                    }

                    if(!isKMZFile) {
                        // Process as KML file??
                    }

                    // Parse the KML file and build a KML Feature and pass it on to the MapInstance for drawing.
                    if((null != request.kmlFilePath) && (0 != request.kmlFilePath.length())) {
                        ((KMLS) request.service).setStatus(request.map, KMLSStatusEnum.PARSING);
                        KML kmlFeature = new KML(new File(request.kmlFilePath).toURI().toURL(), request.kmzDirectory.getAbsolutePath());
                        Log.d(TAG, "kmlFeature created " + request.kmlFilePath);
                        request.service.setFeature(kmlFeature);
                        reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FILE_PARSED);

                        ClientMapToMapInstance mapMapping = (ClientMapToMapInstance) storageManager.getMapMapping(request.map);
                        if((null != mapMapping) && (null != mapMapping.getMapInstance())) {
                            mapMapping.getMapInstance().addMapService(request.service);
                            ((KMLS) request.service).setStatus(request.map, KMLSStatusEnum.DRAWN);
                            reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FEATURES_DRAWN);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch(Exception e) {
                    Log.e(TAG, "KMLSProcessor run", e);
                }
            }
        }

        /**
         * Copy the KMZ file from the specified URL.
         * @param request
         * @return
         */
        private void copyKMZ(KMLSRequest request) throws EMP_Exception {

            // Following would be much more efficient but it is truncating the file. If we have to support
            // Large file sizes then we should investigate the code below.

            // URL website = new URL("http://www.website.com/information.asp");
            // ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            // FileOutputStream fos = new FileOutputStream("information.html");
            // fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

            try {
                buildDirectory(request.service.getContext());
                URLConnection connection = request.service.getURL().openConnection();
                String path = request.service.getURL().getPath();
                // File directory = request.service.getContext().getDir(path.substring(path.lastIndexOf(File.separatorChar) + 1) + ".d", Context.MODE_PRIVATE);

                File directory = new File(persistentRoot + File.separator + path.substring(path.lastIndexOf(File.separatorChar) + 1) + ".d");
                if(!directory.exists()) {
                    directory.mkdirs();
                }
                cleanDirectory(directory);
                String fileName = path.substring(path.lastIndexOf(File.separatorChar) + 1);
                Log.v(TAG, "File Path from URL " + path + " target directory " + directory + " target fileName " + fileName);
                try (InputStream input = connection.getInputStream();
                     OutputStream output = new FileOutputStream(directory + File.separator + fileName)) {
                    byte[] buffer = new byte[READ_BUFFER_SIZE];
                    int n;
                    int total = 0;
                    while ((n = input.read(buffer)) != -1) {
                        output.write(buffer, 0, n);
                        total += n;
                    }
                    Log.v(TAG, "transferred " + total);
                }

                request.kmzFilePath = directory + File.separator + fileName;
                request.kmzDirectory = directory;
            } catch (IOException ioe) {
                Log.e(TAG, "KMLSProcessor-copyKMZ failed " + request.service.getURL(), ioe);
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
                    } else {
                        file.delete();
                    }
                }
            }
        }

        /**
         * https://developers.google.com/kml/documentation/kmzarchives for structure of KMZ file, Important thing to note is:
         *     a KMZ file can refer to other kmz files - we don;t support that
         *     There shouldn't be more than one kml file in the archive
         *     File references are relative.
         *     kml file is always in the root folder.
         * @param request
         * @throws EMP_Exception
         */
        private void unzipKMZFile(KMLSRequest request) throws EMP_Exception {

            try {
                String kmlFilePath = null;
                ZipFile zipFile = new ZipFile(request.kmzFilePath);
                Enumeration<? extends ZipEntry> entries = zipFile.entries();

                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    Log.v(TAG, "zipEntry " + zipEntry.getName());

                    if(zipEntry.isDirectory()) {
                        File directory = new File(request.kmzDirectory + File.separator + zipEntry.getName());
                        directory.mkdirs();
                        continue;
                    }

                    if(((null == kmlFilePath) || (0 == kmlFilePath.length())) && (zipEntry.getName().endsWith(".kml"))) {
                        request.kmlFilePath = request.kmzDirectory + File.separator + zipEntry.getName();
                        Log.d(TAG, "kmlFilePath " + request.kmlFilePath);
                    }

                    try (BufferedInputStream bis = new  BufferedInputStream(zipFile.getInputStream(zipEntry));
                         BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(new File(request.kmzDirectory, zipEntry.getName())))) {
                        byte[] buf = new byte[READ_BUFFER_SIZE];
                        int ii;
                        while ((ii = bis.read(buf, 0, READ_BUFFER_SIZE)) != -1) {
                            bos.write(buf, 0, ii);
                        }
                        bos.flush();
                    }
                }

            } catch (IOException | SecurityException e) {
                Log.e(TAG, "KMLProcessor-unzipKMZFile " + request.kmzFilePath, e);
                throw new EMP_Exception(EMP_Exception.ErrorDetail.OTHER, e.getMessage());
            }
        }

        /**
         * Recursively lists files in a directory.
         * @param directory
         */
        private void listFiles(File directory) {
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
    }

    /**
     * KMLS Reporter is responsible for reporting KMLS events to the application.
     */
    class KMLSReporter implements Runnable {

        private BlockingQueue<KMLSReport> reportQueue = new LinkedBlockingQueue<>();
        private Thread reporterThread = null;

        /**
         * Start thread if it is not already started.
         */
        private void startThread() {
            if(null == reporterThread) {
                reporterThread = new Thread(this);
                reporterThread.start();
            }
        }
        /**
         * Used to report current status of the request via events to the client application
         */
        class KMLSReport extends KMLSEvent {
            private final IKMLSEventListener listener;
            public KMLSReport(IMap clientMap, IKMLS oTarget, KMLSEventEnum eEvent, IKMLSEventListener listener) {
                super(eEvent, oTarget, clientMap);
                this.listener = listener;
            }

            public IKMLSEventListener getListener() {
                return listener;
            }
        }

        /**
         * Create the event object and queue it up for processing
         * @param clientMap
         * @param service
         * @param event
         */
        void generateEvent(IMap clientMap, IKMLS service, KMLSEventEnum event) {
            KMLSReport report = new KMLSReport(clientMap, service, event, service.getListener());
            reportQueue.add(report);
            startThread();
        }

        /**
         * Takes the next event from the queue and invokes user installed listener.
         */
        @Override
        public void run() {
            while(!Thread.interrupted()) {
                try {
                    KMLSReport report = reportQueue.take();
                    Log.d(TAG, "KMLSReporter reporting " + report.getTarget().getURL() + " " + report.getEvent());
                    if(null != report.getListener()) {
                        report.getListener().onEvent(report);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch(Exception e) {
                    Log.e(TAG, "KMLSProcessor run", e);
                }
            }
        }
    }
}
