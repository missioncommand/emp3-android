package mil.emp3.mapengine.listeners;

import mil.emp3.mapengine.events.MapInstanceViewChangeEvent;

/**
 * Listener interface for event generated when the map view changes.
 */
public interface MapInstanceViewChangeEventListener {
    /**
     * Callback method called when MapInstanceViewChangeEvent is triggered.
     * @param event
     */
    void onEvent(MapInstanceViewChangeEvent event);
}
