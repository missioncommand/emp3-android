package mil.emp3.api.listeners;

import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;

/**
 * A class must implement this interface to process draw events.
 */
public interface IDrawEventListener {
    /**
     * This method is called when the draw session begins.
     * @param map This parameter indicates the map the event occurred on.
     */
    public void onDrawStart(IMap map);
    /**
     * This method is called each time the feature is changed.
     * @param map This parameter indicates the map the event occurred on.
     * @param updateList This parameter contains a list of changes. see {@link IEditUpdateData}
     */
    public void onDrawUpdate(IMap map, IFeature feature, java.util.List<IEditUpdateData> updateList);
    /**
     * This method is called when the draw session is finished.
     * @param map This parameter indicates the map the event occurred on.
     * @param feature This parameter is the newly edited feature.
     */
    public void onDrawComplete(IMap map, IFeature feature);
    /**
     * This method is called when the draw session is canceled.
     * @param map This parameter indicates the map the event occurred on.
     * @param originalFeature This parameter is the feature as it was before the session started.
     */
    public void onDrawCancel(IMap map, IFeature originalFeature);
    /**
     * This method is called if an error is encountered.
     * @param map This parameter indicates the map the event occurred on.
     * @param errorMessage A text description of the error.
     */
    public void onDrawError(IMap map, String errorMessage);
}
