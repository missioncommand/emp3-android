package mil.emp3.api.interfaces.core.storage;

import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.IMapInstanceEventHandler;
import mil.emp3.mapengine.interfaces.IMapInstance;

public interface IClientMapToMapInstance extends IMapStatus, IMapInstanceEventHandler {
    void copy(IClientMapToMapInstance from);

    IMap getClientMap();

    void setMapInstance(IMapInstance mapInstance);
    IMapInstance getMapInstance();

    boolean selectFeature(IFeature feature);
    boolean deselectFeature(java.util.UUID featureId);
    java.util.List<IFeature> getSelected();
    void clearSelected();
    boolean isSelected(IFeature feature);
}
