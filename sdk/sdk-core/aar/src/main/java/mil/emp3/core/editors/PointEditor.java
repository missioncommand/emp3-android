package mil.emp3.core.editors;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class handles the Point feature editing.
 */
public class PointEditor extends AbstractSinglePointEditor {
    public PointEditor(IMapInstance map, mil.emp3.api.Point feature, IEditEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, false);
        this.initializeEdit();
    }

    public PointEditor(IMapInstance map, mil.emp3.api.Point feature, IDrawEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, false);
        this.initializeDraw();
    }
}
