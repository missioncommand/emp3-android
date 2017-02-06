package mil.emp3.api.listeners;

import mil.emp3.api.events.FeatureEvent;

/**
 * This listener is invoked when application calls the following methods of IMap API:
 *     selectFeature, selectFeatures, deselectFeature, deselectFeatures and clearSelected.
 */
public interface IFeatureEventListener extends IEventListener<FeatureEvent> {
}
