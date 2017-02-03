package mil.emp3.core.editors.milstd;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.UnitDef;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.editors.AbstractSinglePointEditor;
import mil.emp3.core.utils.milstd2525.MilStdUtilities;
import mil.emp3.core.editors.AbstractDrawEditEditor;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the editor for single point MilStd symbols.
 */
public class MilStdSinglePointEditor extends AbstractSinglePointEditor {
    private final mil.emp3.api.MilStdSymbol getSymbolIcon() {
        return (mil.emp3.api.MilStdSymbol) this.oFeature;
    }

    public MilStdSinglePointEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, false);
        this.initializeEdit();
    }

    public MilStdSinglePointEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, false);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        String sSymbolCode = this.getSymbolIcon().getBasicSymbol();
        int iMilStdVersion = MilStdUtilities.geoMilStdVersionToRendererVersion(this.getSymbolIcon().getSymbolStandard());
        UnitDef oUnitDef = armyc2.c2sd.renderer.utilities.UnitDefTable.getInstance().getUnitDef(sSymbolCode, iMilStdVersion);

        if (null == oUnitDef) {
            // It may be a single point TG I.E. single point target.
            SymbolDef symbolDef =  armyc2.c2sd.renderer.utilities.SymbolDefTable.getInstance().getSymbolDef(sSymbolCode, iMilStdVersion);

            if (symbolDef.getDrawCategory() != SymbolDef.DRAW_CATEGORY_POINT) {
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid symbol code.");
            }
        } else if (oUnitDef.getDrawCategory() == UnitDef.DRAW_CATEGORY_DONOTDRAW) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid symbol code.");
        }

        super.prepareForDraw();
    }
}
