package mil.emp3.core.editors;


import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the editor for Text features.
 */

public class TextEditor extends AbstractSinglePointEditor {
    public TextEditor(IMapInstance map, mil.emp3.api.Text feature, IEditEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, false);
        this.initializeEdit();
    }

    public TextEditor(IMapInstance map, mil.emp3.api.Text feature, IDrawEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, false);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        super.prepareForDraw();

        if ((null == this.oFeature.getName()) || this.oFeature.getName().isEmpty()) {
            this.oFeature.setName("New Text Feature");
        }
    }
}
