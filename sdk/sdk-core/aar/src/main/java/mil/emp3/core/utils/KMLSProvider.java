package mil.emp3.core.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IKMLS;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.core.storage.ClientMapToMapInstance;

/**
 * Processes KML service requests from the client application. Basic interfaces for the application are
 * addMapService and removeMapService. Request is queued to a Thread and application call is returned.
 */

public class KMLSProvider {
    private static String TAG = KMLSProvider.class.getSimpleName();
    private static KMLSProvider instance = null;
    private final IStorageManager storageManager;
    private BlockingQueue<KMLSRequest> queue = new LinkedBlockingQueue<>();
    private KMLSProcessor processor;
    private Thread processorThread;

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

    private void init() {
        if(null == processor) {
            processor = new KMLSProcessor();
            processorThread = new Thread(processor);
            processorThread.start();
        }
    }
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
            return true;
        } catch (Exception e) {
            Log.e(TAG, "addMapService ", e);
        }
        return false;
    }

    class KMLSRequest {
        IMap map;
        IKMLS service;
        KMLSRequest(IMap map, IKMLS service) {
            this.map = map;
            this.service = service;
        }
    }
    class KMLSProcessor implements Runnable {
        @Override
        public void run() {
            while(!Thread.interrupted()) {
                try {
                    KMLSRequest request = queue.take();
                    Log.d(TAG, "KMLSProcessor processing " + request.service.getURL());

                    URLConnection connection = request.service.getURL().openConnection();

                    try (InputStream input = connection.getInputStream();
                         OutputStream output = new FileOutputStream( request.service.getContext().getFilesDir() + "/information.html" ) )
                    {
                        byte[] buffer = new byte[4096];
                        int n;
                        int total = 0;
                        while ((n = input.read(buffer)) != -1)
                        {
                            output.write(buffer, 0, n);
                            total += n;
                        }
                        Log.d(TAG, "transferred " + total);
                    }

                    Log.d(TAG, "getFilesDir " + request.service.getContext().getFilesDir().getAbsolutePath());
                    File[] files = request.service.getContext().getFilesDir().listFiles();
                    Log.d("Files", "Size: "+ files.length);
                    for (int i = 0; i < files.length; i++)
                    {
                        Log.d("Files", "FileName:" + files[i].getName() + " " + files[i].length());
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
