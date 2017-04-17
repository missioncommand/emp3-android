package mil.emp3.core.editors;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class handles the Point feature editing.
 */
public class PointEditor extends AbstractSinglePointEditor<mil.emp3.api.Point> {
    public PointEditor(IMapInstance map, mil.emp3.api.Point feature, IEditEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, false);
        this.initializeEdit();
    }

    public PointEditor(IMapInstance map, mil.emp3.api.Point feature, IDrawEventListener oEventListener, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, false, newFeature);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        super.prepareForDraw();

        if (!this.isNewFeature()) {
            // A feature that already exists should have all of its properties set already.
            return;
        }

        this.oFeature.setPosition(getCenter());
    }
}
