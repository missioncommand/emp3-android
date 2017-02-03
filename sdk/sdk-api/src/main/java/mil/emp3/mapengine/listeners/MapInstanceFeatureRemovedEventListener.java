package mil.emp3.mapengine.listeners;

import mil.emp3.mapengine.events.MapInstanceFeatureRemovedEvent;

/**
 * Listener interface for event generated when a feature is removed from the map
 */
public interface MapInstanceFeatureRemovedEventListener {
    /**
     * Callback method called when MapInstanceFeatureRemovedEvent is triggered.
     * @param event
     */
    void onEvent(MapInstanceFeatureRemovedEvent event);
}
