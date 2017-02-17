package mil.emp3.api.interfaces.core;

import mil.emp3.api.enums.MapGridTypeEnum;
import mil.emp3.mapengine.listeners.MapInstanceFeatureAddedEventListener;
import mil.emp3.mapengine.listeners.MapInstanceFeatureRemovedEventListener;
import mil.emp3.mapengine.listeners.MapInstanceFeatureUserInteractionEventListener;
import mil.emp3.mapengine.listeners.MapInstanceStateChangeEventListener;
import mil.emp3.mapengine.listeners.MapInstanceUserInteractionEventListener;
import mil.emp3.mapengine.listeners.MapInstanceViewChangeEventListener;

/*
 * This is an internal interface class.  The app developer must not implement this interface.
 */
public interface IMapInstanceEventHandler extends MapInstanceFeatureUserInteractionEventListener,
                                                  MapInstanceUserInteractionEventListener,
                                                  MapInstanceStateChangeEventListener,
                                                  MapInstanceViewChangeEventListener,
                                                  MapInstanceFeatureAddedEventListener,
                                                  MapInstanceFeatureRemovedEventListener {


    void setMapGridType(MapGridTypeEnum gridType);
    MapGridTypeEnum getMapGridType();
}
