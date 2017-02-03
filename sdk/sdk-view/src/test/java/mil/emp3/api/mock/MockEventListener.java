package mil.emp3.api.mock;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import mil.emp3.api.events.FeatureUserInteractionEvent;
import mil.emp3.api.events.MapStateChangeEvent;
import mil.emp3.api.events.MapUserInteractionEvent;
import mil.emp3.api.events.MapViewChangeEvent;
import mil.emp3.api.interfaces.IEvent;
import mil.emp3.api.listeners.IFeatureInteractionEventListener;
import mil.emp3.api.listeners.IMapInteractionEventListener;
import mil.emp3.api.listeners.IMapStateChangeEventListener;
import mil.emp3.api.listeners.IMapViewChangeEventListener;

public class MockEventListener {
    private static String TAG = MockEventListener.class.getSimpleName();
    BlockingQueue<IEvent> receivedEventQueue;

    public MockEventListener(BlockingQueue<IEvent> receivedEventQueue) {
        this.receivedEventQueue = receivedEventQueue;
    }

    public boolean validateReceivedEvents(IEvent event) throws InterruptedException {
        java.util.List<IEvent> events = new ArrayList<>();
        events.add(event);
        return validateReceivedEvents(events);
    }

    public boolean validateReceivedEvents(List<IEvent> events) throws InterruptedException {
        if((null == events) || (0 == events.size())) {
            if(receivedEventQueue.size() == 0) return true;
            else {
                Log.e(TAG, "receivedEventQueue should but empty but has " + receivedEventQueue.size() + " elements");
                return false;
            }
        }

        while(!events.isEmpty()) {
            if(receivedEventQueue.isEmpty()) {
                Log.e(TAG, "received doesn't have required events events remaining " + events.size());
            }
            IEvent queuedEvent = receivedEventQueue.take();
            IEvent expectedEvent = events.remove(0);

            if(queuedEvent.getClass().getCanonicalName().compareTo(expectedEvent.getClass().getCanonicalName()) == 0) {
                continue;
            } else {
                Log.e(TAG, "Events don match Expected " + expectedEvent.getClass().getCanonicalName() + " received "
                    + queuedEvent.getClass().getCanonicalName());
                return false;
            }
        }

        if(!receivedEventQueue.isEmpty()) {
            Log.e(TAG, "There are remaining receivedEventQueue remaining count " + receivedEventQueue.size());
            return false;
        } else if(!events.isEmpty()) {
            Log.e(TAG, "Number of events not found in the receivedEventQueue count " + events.size());
            return false;
        }
        return true;
    }

    public void cleanQueues() {
        receivedEventQueue.clear();
    }

    private class MapStateChangeEventListener implements IMapStateChangeEventListener {
        @Override
        public void onEvent(MapStateChangeEvent event) {
            if(null != receivedEventQueue)
                receivedEventQueue.add(event);
        }
    }

    private class MapViewChangeEventListener implements IMapViewChangeEventListener {
        @Override
        public void onEvent(MapViewChangeEvent event) {
            if(null != receivedEventQueue)
                receivedEventQueue.add(event);
        }
    }

    private class MapInteractionEventListener implements IMapInteractionEventListener {
        @Override
        public void onEvent(MapUserInteractionEvent event) {
            if(null != receivedEventQueue)
                receivedEventQueue.add(event);
        }
    }

    private class FeatureInteractionEventListener implements IFeatureInteractionEventListener {
        @Override
        public void onEvent(FeatureUserInteractionEvent event) {
            if(null != receivedEventQueue)
                receivedEventQueue.add(event);
        }
    }
}
