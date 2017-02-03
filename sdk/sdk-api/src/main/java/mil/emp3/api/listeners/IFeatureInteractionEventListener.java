package mil.emp3.api.listeners;


import mil.emp3.api.events.FeatureUserInteractionEvent;

/**
 * The is the interface a class must implement to receive feature interaction events.
 */
public interface IFeatureInteractionEventListener extends IEventListener<FeatureUserInteractionEvent> {
}
