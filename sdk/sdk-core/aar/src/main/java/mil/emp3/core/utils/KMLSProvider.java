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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
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
 * 3. Events are generated and reported via the listener as service processing goes through phases.
 * 4. KMZ referring to another KMZ is not supported as our parser skips over network links.
 */

public class KMLSProvider {
    private static String TAG = KMLSProvider.class.getSimpleName();
    private static KMLSProvider instance = null;

    private final IStorageManager storageManager;
    private KMLSProcessor processor;
    private KMLSReporter reporter;

    private Map<UUID, KMLSProcessor.KMLSRequest> uuidkmlsRequestMap = new HashMap<>();

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
                }
            }
        }
        return instance;
    }

    private KMLSProvider(IStorageManager storageManager) {
        this.storageManager = storageManager;
        processor = new KMLSProcessor();
        reporter = new KMLSReporter();
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
            processor.generateRequest(map, mapService);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "addMapService ", e);
        }
        return false;
    }

    public boolean removeMapService(IMap map, IKMLS mapService) throws EMP_Exception
    {
        try {
            ClientMapToMapInstance mapMapping = (ClientMapToMapInstance) storageManager.getMapMapping(map);
            if (!mapMapping.serviceExists(mapService.getGeoId())) {
                Log.i(TAG, "Attempting remove KMLS Service that was never added");
                return false;
            }
            KMLSProcessor.KMLSRequest request = uuidkmlsRequestMap.get(mapService.getGeoId());
            if(request != null) {
                mapMapping.removeMapService(request.getService());
                mapMapping.getMapInstance().removeMapService(request.getService());
                request.clean();
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "addMapService ", e);
        }
        return false;
    }
    /**
     * Waits on the queue and serially process incoming KML Service Requests.
     */
    private class KMLSProcessor implements Runnable {
        private final int READ_BUFFER_SIZE = 4096;

        private final String KMLS_ROOT = "KMLS";
        private final String PERSISTENT_ROOT = "PERSISTENT";
        private final String VOLATILE_ROOT = "VOLATILE";

        private boolean directoryBuilt = false;
        private File persistentRoot;
        private File volatileRoot;

        /**
         * Holder for KML Request.
         */
        class KMLSRequest implements KMZFile.IKMZFileRerquest{
            private IMap map;
            private IKMLS service;
            private String kmzFilePath = null;
            private File kmzDirectory = null;
            private String kmlFilePath = null;
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

            public String getKmzFilePath() {
                return kmzFilePath;
            }

            public void setKmzFilePath(String kmzFilePath) {
                this.kmzFilePath = kmzFilePath;
            }

            public File getKmzDirectory() {
                return kmzDirectory;
            }

            public void setKmzDirectory(File kmzDirectory) {
                this.kmzDirectory = kmzDirectory;
            }

            public void clean() {
                if(null != kmzDirectory) {
                    cleanDirectory(kmzDirectory);
                }
            }
        }

        private BlockingQueue<KMLSRequest> queue = new LinkedBlockingQueue<>();
        private Thread processorThread;

        /**
         * Start thread if it is not already started.
         */
        private void startThread() {
            if(null == processorThread) {
                processorThread = new Thread(this);
                processorThread.start();
            }
        }

        void generateRequest(IMap map, IKMLS mapService) {
            queue.add(new KMLSRequest(map, mapService));
            if(mapService instanceof KMLS) {
                KMLS kmls = (KMLS) mapService;
                kmls.setStatus(map, KMLSStatusEnum.QUEUED);
            }
            startThread();
        }

        @Override
        public void run() {
            while(!Thread.interrupted()) {
                try {
                    KMLSRequest request = queue.take();
                    Log.d(TAG, "KMLSProcessor processing " + request.service.getURL());

                    try {
                        ((KMLS) request.service).setStatus(request.map, KMLSStatusEnum.FETCHING);
                        copyKMZ(request);  // Fetch the KMZ either file URL or network URL
                        reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED);
                        listFiles(request.kmzDirectory); // This is just for debugging
                    } catch (Exception e) {
                        Log.e(TAG, "run:copyKMZ failed ", e);
                        reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FILE_RETRIEVAL_FAILED);
                    }

                    // File could be either a KMZ file or KML file.
                    if((null != request.kmzDirectory) && (null != request.kmzFilePath)) {
                        try {
                            ((KMLS) request.service).setStatus(request.map, KMLSStatusEnum.EXPLODING);
                            KMZFile kmzFile = new KMZFile();
                            kmzFile.unzipKMZFile(request);
                            reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FILE_EXPLODED);
                            listFiles(request.kmzDirectory);
                        } catch (EMP_Exception e) {
                            Log.i(TAG, "KMLProcessor-run " + e.getMessage(), e);
                            reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FILE_INVALID);
                        }
                    }

                    // Parse the KML file and build a KML Feature and pass it on to the MapInstance for drawing.
                    if((null != request.kmlFilePath) && (0 != request.kmlFilePath.length())) {
                        try {
                            ((KMLS) request.service).setStatus(request.map, KMLSStatusEnum.PARSING);
                            KML kmlFeature = new KML(new File(request.kmlFilePath).toURI().toURL(), request.kmzDirectory.getAbsolutePath());
                            Log.d(TAG, "kmlFeature created " + request.kmlFilePath);
                            request.service.setFeature(kmlFeature);
                            if(null != kmlFeature) {
                                kmlFeature.setName(request.service.getName());
                            }
                            reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FILE_PARSED);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse request.kmlFilePath ", e);
                            reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_PARSE_FAILED);
                            reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_INSTALL_FAILED);
                        }

                        if(null != request.service.getFeature()) {
                            ClientMapToMapInstance mapMapping = (ClientMapToMapInstance) storageManager.getMapMapping(request.map);
                            if ((null != mapMapping) && (null != mapMapping.getMapInstance())) {
                                mapMapping.getMapInstance().addMapService(request.service);
                                ((KMLS) request.service).setStatus(request.map, KMLSStatusEnum.DRAWN);
                                reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_FEATURES_DRAWN);
                            }
                        }
                    } else {
                        reporter.generateEvent(request.map, request.service, KMLSEventEnum.KML_SERVICE_INSTALL_FAILED);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch(Exception e) {
                    Log.e(TAG, "KMLSProcessor run", e);
                }
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
                    cleanDirectory(volatileRoot);   // Cleanup the space used by volatile service from previous run.
                    directoryBuilt = true;
                }
            } catch (Exception e) {
                Log.e(TAG, "failed to build directory ", e);
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

                String forwardSlahsStd = "/";
                char forwardSlashChar = '/';

                Log.d(TAG, "protocol " + request.service.getURL().getProtocol() + " HOST " + request.service.getURL().getHost()
                        + " File " + request.service.getURL().getFile());
                Log.d(TAG, "File.separator " + File.separator);
                URLConnection connection = request.service.getURL().openConnection();
                String path = request.service.getURL().getPath();

                String hostName = request.service.getURL().getHost();
                if(null == hostName) {
                    hostName = "file";
                }

                File directory;
                String fileName;
                String targetFilePath;
                if(request.service.isPersistent()) {
                    if(request.service.getURL().getProtocol().equals("file")) {
                        String file = request.service.getURL().getFile();
                        if(file.contains(":")) {
                            String split[] = file.split(":");
                            file = split[split.length -1];
                        }
                        fileName = file.substring(file.lastIndexOf(forwardSlashChar) + 1);
                        directory = new File(persistentRoot + File.separator + file.replace(forwardSlashChar, '.'));
                    } else {
                        directory = new File(persistentRoot + File.separator + hostName.replace(File.separatorChar, '.') + path.replace(File.separatorChar, '.'));
                        fileName = path.substring(path.lastIndexOf(File.separatorChar) + 1);
                    }
                } else {
                    if(request.service.getURL().getProtocol().equals("file")) {
                        String file = request.service.getURL().getFile();
                        if(file.contains(":")) {
                            String split[] = file.split(":");
                            file = split[split.length -1];
                        }
                        fileName = file.substring(file.lastIndexOf(forwardSlashChar) + 1);
                        directory = new File(volatileRoot + File.separator + file.replace(forwardSlashChar, '.'));
                    } else {
                        directory = new File(volatileRoot + File.separator + hostName.replace(File.separatorChar, '.') + path.replace(File.separatorChar, '.'));
                        fileName = path.substring(path.lastIndexOf(File.separatorChar) + 1);
                    }
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

                request.kmzFilePath = targetFilePath;
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
