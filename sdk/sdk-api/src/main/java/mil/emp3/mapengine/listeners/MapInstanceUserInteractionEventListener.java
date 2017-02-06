package mil.emp3.mapengine.listeners;

import mil.emp3.mapengine.events.MapInstanceUserInteractionEvent;

/**
 * Listener interface for event generated when a user interacts with the map
 */
public interface MapInstanceUserInteractionEventListener {
    /**
     * Callback method called when MapInstanceUserInteractionEvent is triggered.
     * @param event
     */
    void onEvent(MapInstanceUserInteractionEvent event);
}
