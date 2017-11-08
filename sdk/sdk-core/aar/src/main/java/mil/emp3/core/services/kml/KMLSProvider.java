package mil.emp3.core.services.kml;

import android.util.Log;

import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import mil.emp3.api.KML;
import mil.emp3.api.KMLS;
import mil.emp3.api.enums.KMLSEventEnum;
import mil.emp3.api.enums.KMLSStatusEnum;
import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.core.storage.IKMLSRequest;
import mil.emp3.api.listeners.IKMLSEventListener;
import mil.emp3.api.utils.FileUtility;
import mil.emp3.core.storage.ClientMapRestoreData;
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
 *
 * This class contains two nested classes that perform the actual work. Each of those classes run on their own thread and interface
 * is via a Blocking Queue.
 *
 * KMLSProcessor - Process the Service Request
 * KMLSReporter - Reports events to the client application via the user supplied listener.
 *
 */

public class KMLSProvider {
    private static String TAG = KMLSProvider.class.getSimpleName();
    private static KMLSProvider instance = null;

    private final IStorageManager storageManager;
    private KMLSProcessor processor;
    private KMLSReporter reporter;

    /**
     * KMLProvider is singleton. All client maps are services by one instance of the class.
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
     * @param cmrd
     * @throws EMP_Exception
     */
    public void addMapService(IMap map, IKMLS mapService, ClientMapRestoreData cmrd) throws EMP_Exception
    {
        try {
            ClientMapToMapInstance mapMapping = (ClientMapToMapInstance) storageManager.getMapMapping(map);
            if (!mapMapping.serviceExists(mapService.getGeoId())) {

                mapMapping.addMapService(mapService);   // Add the MapStatus in-line so that duplicate cannot be added

                // We will need this when client does a swap engine or activity is restored.
                // When activity is restored we still save the entire list anyway, that may be redundant
                if (null != cmrd) {
                    cmrd.addMapService(mapService);
                }

                try {
                    KMLSRequest request = processor.generateRequest(map, mapService); // Process the actual request on a background thread.
                    mapMapping.addKmlRequest(request);
                    if (null != cmrd) {
                        cmrd.addKmlRequest(request);
                    }
                } catch (IllegalArgumentException iae) {
                    Log.e(TAG, "addMapService ", iae);
                    mapMapping.removeMapService(mapService);
                    if(null != cmrd) {
                        cmrd.removeMapService(mapService);
                    }
                }
            } else {
                Log.i(TAG, "Attempting to add KML Service that already exists " + mapService.getGeoId().toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "addMapService ", e);
        }
    }

    /**
     * This is processed in-line like any other removeFeature. Update the state by removing map service from MapStatus and
     * Restore Data. Remove it from Map Engine if it was added.
     * @param map
     * @param mapService
     * @throws EMP_Exception
     */
    public void removeMapService(IMap map, IKMLS mapService, ClientMapRestoreData cmrd) throws EMP_Exception
    {
        try {
            ClientMapToMapInstance mapMapping = (ClientMapToMapInstance) storageManager.getMapMapping(map);
            if (mapMapping.serviceExists(mapService.getGeoId())) {
                // Update State
                mapMapping.removeMapService(mapService);
                if (null != cmrd) {
                    cmrd.removeMapService(mapService);
                }

                IKMLSRequest request = mapMapping.getKmlRequest(mapService.getGeoId());
                if (null != request) {
                    // Remove the request from the queue
                    mapMapping.removeKmlRequest(request);
                    if (null != cmrd) {
                        cmrd.removeKmlRequest(request);
                    }
                    // Remove from map engine
                    if (null != request.getFeature()) {
                        // Build a KMLS request and set the Feature to be removed and then invoke map engine API.
                        IKMLS tmpKMLS = new KMLS(request.getService().getContext(), request.getService().getURL().toString(), request.getService().getListener());
                        tmpKMLS.setFeature(request.getFeature());
                        mapMapping.getMapInstance().removeMapService(tmpKMLS);
                    }
                    //When removing a feature from the map, delete the copied folder in order to save space on the device
                    FileUtility.deleteFolder(request.getKmzDirectory());
                }
            } else {
                Log.i(TAG, "Attempting remove KMLS Service that was never added " + mapService.getGeoId().toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "addMapService ", e);
        }
    }

    /**
     * Restores a KMLS service when activity is restarted. We use the the generated feature rather than recreate it.
     * You can look at {@link mil.emp3.api.Emp3LifeCycleManager} for further details.
     * @param map
     * @param mapService
     * @param request this can be null if KMLS service installation had failed
     * @throws EMP_Exception
     */
    public void restoreMapService(IMap map, IKMLS mapService, KMLSRequest request) throws EMP_Exception
    {
        try {
            ClientMapToMapInstance mapMapping = (ClientMapToMapInstance) storageManager.getMapMapping(map);
            if (!mapMapping.serviceExists(mapService.getGeoId())) {
                // Restore state
                mapMapping.addMapService(mapService);

                // Draw on Map
                // Pass the KML Feature to map engine. We are not storing the generated KML Feature in the KMLS service object
                // supplied by the client application as we don't want the application to act on this feature in any manner.
                // So a temporary KMLS object is generated and passed on to the Map Engine.

                if(request != null) {
                    mapMapping.addKmlRequest(request);
                }
                if ((null != request) && (null != request.getFeature())) {
                    try {
                        KMLS tmpKmls = new KMLS(mapService.getContext(), mapService.getURL().toString(), mapService.getListener());
                        tmpKmls.setFeature(request.getFeature());
                        mapMapping.getMapInstance().addMapService(tmpKmls);
                    } catch (Exception e) {
                        Log.e(TAG, "Trying to redraw KMLS", e);
                    }
                }
            } else {
                Log.i(TAG, "Attempting to restore KML Service that already exists " + mapService.getGeoId().toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "addMapService ", e);
        }
    }

    /**
     * Waits on the queue and serially process incoming KML Service Requests.
     */
    private class KMLSProcessor implements Runnable {

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

        /**
         * Adds a request to the queue and starts the thread if it is not already started.
         * @param map
         * @param mapService
         */
        KMLSRequest generateRequest(IMap map, IKMLS mapService) {
            KMLSRequest kmlsRequest = new KMLSRequest(map, mapService);
            queue.add(kmlsRequest);
            if(mapService instanceof KMLS) {
                KMLS kmls = (KMLS) mapService;
                kmls.setStatus(map, KMLSStatusEnum.QUEUED);
            }
            startThread();
            return kmlsRequest;
        }

        /**
         * This is where the actual request processing takes place.
         *   1. Copy the file using the application specified URL
         *   2. Explode the file if necessary
         *   3. Parse the file and generate KML Feature.
         *   4. Add the map service to map engine
         *   5. Update status and generate events for application along the way.
         */
        @Override
        public void run() {
            while(!Thread.interrupted()) {
                KMLSRequest request = null;
                try {
                    request = queue.take();
                    Log.d(TAG, "KMLSProcessor processing " + request.getService().getURL());

                    // Copy the file to application private folder, this could a file or network URL.
                    try {
                        ((KMLS) request.getService()).setStatus(request.getMap(), KMLSStatusEnum.FETCHING);
                        request.copyKMZ();  // Fetch the KMZ either file URL or network URL
                        reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED);
                        request.listFiles(request.getKmzDirectory()); // This is just for debugging
                    } catch (Exception e) {
                        Log.e(TAG, "run:copyKMZ failed ", e);
                        reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_RETRIEVAL_FAILED);
                    }

                    // If it is a KMZ file then it will be unzipped (exploded), remember that this could simply be
                    // a KML file also.
                    if((null != request.getKmzDirectory()) && (null != request.getKmzFilePath())) {
                        try {
                            ((KMLS) request.getService()).setStatus(request.getMap(), KMLSStatusEnum.EXPLODING);
                            KMZFile kmzFile = new KMZFile();
                            kmzFile.unzipKMZFile(request);
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_EXPLODED);
                            request.listFiles(request.getKmzDirectory());
                        } catch (EMP_Exception e) {
                            Log.e(TAG, "KMLProcessor-run " + e.getMessage(), e);
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_INVALID);
                        }
                    }

                    // Parse the KML file and build a KML Feature
                    if((null != request.getKmlFilePath()) && (0 != request.getKmlFilePath().length())) {
                        try {
                            ((KMLS) request.getService()).setStatus(request.getMap(), KMLSStatusEnum.PARSING);
                            KML kmlFeature = new KML(new File(request.getKmlFilePath()).toURI().toURL(), request.getKmzDirectory().getAbsolutePath());
                            Log.d(TAG, "kmlFeature created " + request.getKmlFilePath());
                            request.setFeature(kmlFeature);
                            if(null != kmlFeature) {
                                kmlFeature.setName(request.getService().getName());
                            }
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_PARSED);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse request.kmlFilePath ", e);
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_PARSE_FAILED);
                        }
                    }

                    // Pass the KML Feature to map engine. We are not storing the generated KML Feature in the KMLS service object
                    // supplied by the client application as we don't want the application to act on this feature in any manner.
                    // So a temporary KMLS object is generated and passed on to the Map Engine.
                    if(null != request.getFeature()) {
                        ClientMapToMapInstance mapMapping = (ClientMapToMapInstance) storageManager.getMapMapping(request.getMap());
                        if ((null != mapMapping) && (null != mapMapping.getMapInstance())) {
                            IKMLS tmpKMLS = new KMLS(request.getService().getContext(), request.getService().getURL().toString(), request.getService().getListener());
                            tmpKMLS.setFeature(request.getFeature());
                            mapMapping.getMapInstance().addMapService(tmpKMLS);
                            ((KMLS) request.getService()).setStatus(request.getMap(), KMLSStatusEnum.DRAWN);
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FEATURES_DRAWN);
                        }
                    } else {
                        reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_INSTALL_FAILED);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch(Exception e) {
                    Log.e(TAG, "KMLSProcessor run", e);
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
