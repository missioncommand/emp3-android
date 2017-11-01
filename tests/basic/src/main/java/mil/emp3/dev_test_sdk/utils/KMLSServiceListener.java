package mil.emp3.dev_test_sdk.utils;

import android.util.Log;

import java.util.concurrent.BlockingQueue;

import mil.emp3.api.enums.KMLSEventEnum;
import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IKMLSEventListener;

/**
 * Basic event listener that looks for KMLSEvents.  This a simple template listener.  In general
 * clients should provide their own listener.  This will allow them to monitor the status of the
 * KMLS request.
 * Created by Matt.Miller on 9/29/2017.
 */

public class KMLSServiceListener implements IKMLSEventListener {
    public static String TAG = KMLSServiceListener.class.getSimpleName();
    IMap map;

    BlockingQueue<KMLSEventEnum> queue;
    public KMLSServiceListener(final BlockingQueue<KMLSEventEnum> queue) {
        this.queue = queue;
    }


    @Override
    public void onEvent(KMLSEvent event) {
        try {
            Log.d(TAG, "KMLSServiceListener-onEvent " + event.getEvent().toString() + " status " + event.getTarget().getStatus(map));
        } catch(EMP_Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }
}
