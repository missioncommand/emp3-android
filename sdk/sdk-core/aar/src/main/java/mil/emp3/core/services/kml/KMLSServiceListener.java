package mil.emp3.core.services.kml;

import android.util.Log;

import java.util.concurrent.BlockingQueue;

import mil.emp3.api.enums.KMLSEventEnum;
import mil.emp3.api.events.KMLSEvent;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.listeners.IKMLSEventListener;

/**
 * Created by Matt.Miller on 9/29/2017.
 */

public class KMLSServiceListener implements IKMLSEventListener {
    public static String TAG = KMLSServiceListener.class.getSimpleName();
    IMap map;

    BlockingQueue<KMLSEventEnum> queue;
    public KMLSServiceListener(BlockingQueue<KMLSEventEnum> queue) {
        this.queue = queue;
    }

    KMLSServiceListener(IMap whichMap) {
        this.map = whichMap;
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
