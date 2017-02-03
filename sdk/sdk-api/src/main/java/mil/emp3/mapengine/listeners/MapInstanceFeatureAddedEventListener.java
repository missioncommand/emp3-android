package mil.emp3.mapengine.listeners;

import mil.emp3.mapengine.events.MapInstanceFeatureAddedEvent;

/**
 * Listener interface for event generated when a feature is added to the map
 */
public interface MapInstanceFeatureAddedEventListener {
    /**
     * Callback method called when MapInstanceFeatureAddedEvent is triggered.
     * @param event
     */
    void onEvent(MapInstanceFeatureAddedEvent event);
}
