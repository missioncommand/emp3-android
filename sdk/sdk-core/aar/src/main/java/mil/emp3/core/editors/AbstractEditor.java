package mil.emp3.core.editors;

import android.util.Log;

import org.cmapi.primitives.IGeoBounds;
import org.cmapi.primitives.IGeoPosition;

import java.util.List;
import java.util.UUID;

import mil.emp3.api.enums.EditorMode;
import mil.emp3.api.global;
import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.interfaces.IEmpBoundingArea;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;
import mil.emp3.api.interfaces.core.IEventManager;
import mil.emp3.api.interfaces.core.IStorageManager;
import mil.emp3.api.interfaces.core.storage.IClientMapToMapInstance;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.api.utils.ManagerFactory;
import mil.emp3.mapengine.events.MapInstanceFeatureUserInteractionEvent;
import mil.emp3.mapengine.events.MapInstanceUserInteractionEvent;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This abstract class is the basic interface to the draw and edit editors.
 */
public abstract class AbstractEditor<T extends IFeature> {

    private static String TAG = AbstractEditor.class.getSimpleName();
    final protected IStorageManager storageManager = ManagerFactory.getInstance().getStorageManager();
    final protected IEventManager eventManager     = ManagerFactory.getInstance().getEventManager();

    protected final IMap oClientMap;
    protected final IMapInstance mapInstance;
    private final EditorMode editorMode;
    private final boolean newFeature;

    protected final T oFeature;

    protected AbstractEditor(IMapInstance instance, T feature, EditorMode eMode, boolean newFeature) {
        this.oFeature = feature;
        this.mapInstance = instance;
        this.oClientMap = storageManager.getMapMapping(this.mapInstance).getClientMap();
        this.editorMode = eMode;
        this.newFeature = newFeature;
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

    protected boolean isNewFeature() {
        return this.newFeature;
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

    /**
     * Some editors start by adding the feature at the center. Depending on the value of Tilt/Roll it is possible
     * that Camera position is not in the visible section of the view, in that case we will return center of the
     * EmpBoundingArea
     * @return
     */
    protected IGeoPosition getCenter() {
        IGeoPosition center;
        IGeoBounds bounds = mapInstance.getMapBounds();
        if((null != bounds) && (bounds instanceof IEmpBoundingArea) && !((IEmpBoundingArea) bounds).cameraPositionIsVisible()) {
            IEmpBoundingArea area = (IEmpBoundingArea) bounds;
            center = area.getGeometricCenter();
        } else {
            ICamera camera = mapInstance.getCamera();
            if(null != camera) {
                center = new EmpGeoPosition(camera.getLatitude(), camera.getLongitude());
            } else {
                center = new EmpGeoPosition();
                Log.e(TAG, "getCenter return 0, 0 camera is null");
            }
        }
        return center;
    }

    /**
     * When drawing Basic Shapes (Circle, Ellipse ..) We need to start with some value for applicable geometric dimension.
     * We take the reference distance calculated here and multiply it with some factor.
     * @return
     */
    double getReferenceDistance() {
        double refDistance = -global.MINIMUM_DISTANCE;
        IGeoBounds bounds = mapInstance.getMapBounds();
        if (null != bounds) {
            EmpGeoPosition centerWest = new EmpGeoPosition((bounds.getNorth() + bounds.getSouth())/2, bounds.getWest());
            EmpGeoPosition centerEast = new EmpGeoPosition((bounds.getNorth() + bounds.getSouth())/2, bounds.getEast());
            refDistance = GeoLibrary.computeDistanceBetween(centerWest, centerEast);
        }
        return refDistance;
    }
}
