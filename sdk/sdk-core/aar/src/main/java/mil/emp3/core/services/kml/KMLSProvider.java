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
            KMLSRequest request = mapMapping.getKmlsRequestMap().get(mapService.getGeoId());
            if(request != null) {
                mapMapping.removeMapService(request.getService());
                mapMapping.getMapInstance().removeMapService(request.getService());
                mapMapping.getKmlsRequestMap().remove(mapService.getGeoId());
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
                    Log.d(TAG, "KMLSProcessor processing " + request.getService().getURL());

                    try {
                        ((KMLS) request.getService()).setStatus(request.getMap(), KMLSStatusEnum.FETCHING);
                        request.copyKMZ();  // Fetch the KMZ either file URL or network URL
                        reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_RETRIEVED);
                        request.listFiles(request.getKmzDirectory()); // This is just for debugging
                    } catch (Exception e) {
                        Log.e(TAG, "run:copyKMZ failed ", e);
                        reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_RETRIEVAL_FAILED);
                    }

                    // File could be either a KMZ file or KML file.
                    if((null != request.getKmzDirectory()) && (null != request.getKmzFilePath())) {
                        try {
                            ((KMLS) request.getService()).setStatus(request.getMap(), KMLSStatusEnum.EXPLODING);
                            KMZFile kmzFile = new KMZFile();
                            kmzFile.unzipKMZFile(request);
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_EXPLODED);
                            request.listFiles(request.getKmzDirectory());
                        } catch (EMP_Exception e) {
                            Log.i(TAG, "KMLProcessor-run " + e.getMessage(), e);
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_INVALID);
                        }
                    }

                    // Parse the KML file and build a KML Feature and pass it on to the MapInstance for drawing.
                    if((null != request.getKmlFilePath()) && (0 != request.getKmlFilePath().length())) {
                        try {
                            ((KMLS) request.getService()).setStatus(request.getMap(), KMLSStatusEnum.PARSING);
                            KML kmlFeature = new KML(new File(request.getKmlFilePath()).toURI().toURL(), request.getKmzDirectory().getAbsolutePath());
                            Log.d(TAG, "kmlFeature created " + request.getKmlFilePath());
                            request.getService().setFeature(kmlFeature);
                            if(null != kmlFeature) {
                                kmlFeature.setName(request.getService().getName());
                            }
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FILE_PARSED);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to parse request.kmlFilePath ", e);
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_PARSE_FAILED);
                            reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_INSTALL_FAILED);
                        }

                        if(null != request.getService().getFeature()) {
                            ClientMapToMapInstance mapMapping = (ClientMapToMapInstance) storageManager.getMapMapping(request.getMap());
                            mapMapping.getKmlsRequestMap().put(request.getService().getGeoId(), request);
                            if ((null != mapMapping) && (null != mapMapping.getMapInstance())) {
                                mapMapping.getMapInstance().addMapService(request.getService());
                                ((KMLS) request.getService()).setStatus(request.getMap(), KMLSStatusEnum.DRAWN);
                                reporter.generateEvent(request.getMap(), request.getService(), KMLSEventEnum.KML_SERVICE_FEATURES_DRAWN);
                            }
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
