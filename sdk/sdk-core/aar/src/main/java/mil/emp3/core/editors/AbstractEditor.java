package mil.emp3.core.editors;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;
import java.util.UUID;

import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.core.storage.IClientMapToMapInstance;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.mapengine.events.MapInstanceFeatureUserInteractionEvent;
import mil.emp3.mapengine.events.MapInstanceUserInteractionEvent;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This abstract class is the basic interface to the draw and edit editors.
 */
public abstract class AbstractEditor<T extends IFeature> {

    final protected IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final protected IEventManager eventManager     = ManagerFactory.getInstance().getEventManager();

    protected final IMap oClientMap;
    protected final IMapInstance mapInstance;
    protected final EditorMode editorMode;

    protected final T oFeature;

    protected AbstractEditor(IMapInstance instance, T feature, EditorMode eMode) {
        this.oFeature = feature;
        this.mapInstance = instance;
        this.oClientMap = storageManager.getMapMapping(this.mapInstance).getClientMap();
        this.editorMode = eMode;
    }

    public T getFeature() {
        return this.oFeature;
    }

    /**
     * The editor implements this handler if it needs map user interaction events.
     * @param oEvent
     * @return True if the event is handled and false otherwise.
     */
    public boolean onEvent(MapInstanceUserInteractionEvent oEvent) {
        return false;
    }

    /**
     * The editor implements this handler if it needs feature user interaction events.
     * @param oEvent
     * @return True if the event is handled and false otherwise.
     */
    public boolean onEvent(MapInstanceFeatureUserInteractionEvent oEvent) {
        return false;
    }

    /**
     * The editor implements this method to handle cancels.
     */
    public void Cancel() {

    }

    /**
     * The editor must implement this method. It instructs the editor to complete the operation and
     * exit edit/draw mode.
     */
    public abstract void Complete();

    public EditorMode getEditorMode() {
        return this.editorMode;
    }
    
    protected List<IGeoPosition> getPositions() {
        return this.oFeature.getPositions();
    }

    protected IClientMapToMapInstance getMapMapping() {
        return storageManager.getMapMapping(this.mapInstance);
    }

    protected IGeoPosition getMapCameraPosition() {
        return this.getMapMapping().getCameraPosition();
    }

    protected IGeoBounds getMapBounds() {
        return this.getMapMapping().getBounds();
    }

    public boolean inDrawMode() {
        return (this.getEditorMode() == EditorMode.DRAW_MODE);
    }

    public boolean inEditMode() {
        return (this.getEditorMode() == EditorMode.EDIT_MODE);
    }

    public boolean inFreehandDrawMode() {
        return (this.getEditorMode() == EditorMode.FREEHAND_MODE);
    }

    public UUID getFeatureGeoId() {
        return oFeature.getGeoId();
    }

    /**
     * If application has issued cancel, complete, exit on the editor then editor should return true from this method.
     * This avoid issuing of multiple cancel events or complete/cancel event in response to complete. As editor is exited
     * we ask storage manager to remove the feature(s) from the map, storage manager thinks that feature is in edit mode and
     * tries to cancel the dit mode causing multiple events. Please see issues EMP-2740 and EMP-2741
     * @return
     */
    public abstract boolean isFinishing();
}
