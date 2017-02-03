package mil.emp3.api.interfaces.core.storage;

import java.util.List;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.IMapInstanceEventHandler;
import mil.emp3.mapengine.interfaces.IMapInstance;

/*
 * This is an internal interface class.  The app developer must not implement this interface.
 */
public interface IClientMapToMapInstance extends IMapStatus, IMapInstanceEventHandler {
    void copy(IClientMapToMapInstance from);

    IMap getClientMap();

    void setMapInstance(IMapInstance mapInstance);
    IMapInstance getMapInstance();

    boolean selectFeature(IFeature feature);
    boolean deselectFeature(java.util.UUID featureId);
    List<IFeature> getSelected();
    void clearSelected();
    boolean isSelected(IFeature feature);
}
