package mil.emp3.mapengine.listeners;

import mil.emp3.mapengine.events.MapInstanceStateChangeEvent;

/**
 * Listener interface for event generated the map state changes
 */
public interface MapInstanceStateChangeEventListener {
    /**
     * Callback method called when MapInstanceStateChangeEvent is triggered.
     * @param event
     */
    void onEvent(MapInstanceStateChangeEvent event);
}
