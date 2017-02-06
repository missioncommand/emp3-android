package mil.emp3.mapengine.listeners;

import mil.emp3.mapengine.events.MapInstanceFeatureUserInteractionEvent;

/**
 * Listener interface for event generated when a user interacts with a feature on the map
 */
public interface MapInstanceFeatureUserInteractionEventListener {
    /**
     * Callback method called when MapInstanceFeatureUserInteractionEvent is triggered.
     * @param event
     */
    void onEvent(MapInstanceFeatureUserInteractionEvent event);
}
