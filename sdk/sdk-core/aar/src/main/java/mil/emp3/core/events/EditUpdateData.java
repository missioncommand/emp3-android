package mil.emp3.core.events;

import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import org.cmapi.primitives.IGeoAirControlMeasure;
import org.cmapi.primitives.IGeoMilSymbol;

/**
 * This class implements the IEditUpdateData interface. It used by the core to generate Feature Edit events.
 */
public class EditUpdateData implements IEditUpdateData {
    private final FeatureEditUpdateTypeEnum eUpdateType;
    private final int[] aIndexes;
    private final IGeoMilSymbol.Modifier eModifier;
    private final IGeoAirControlMeasure.Attribute eAttribute;

    public EditUpdateData(FeatureEditUpdateTypeEnum eType, int[] aIndexes) throws EMP_Exception {
        switch (eType) {
            case COORDINATE_ADDED:
            case COORDINATE_DELETED:
            case COORDINATE_MOVED:
                if (aIndexes == null) {
                    aIndexes = new int[0];
                }
                break;
            default:
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid FeatureEditUpdateTypeEnum.");
        }
        this.eUpdateType = eType;
        this.aIndexes = aIndexes;
        this.eModifier = null;
        this.eAttribute = null;
    }

    public EditUpdateData(IGeoMilSymbol.Modifier eModifier) {
        this.eUpdateType = FeatureEditUpdateTypeEnum.MILSTD_MODIFIER_UPDATED;
        this.aIndexes = null;
        this.eModifier = eModifier;
        this.eAttribute = null;
    }

    public EditUpdateData(IGeoAirControlMeasure.Attribute eAttribute) {
        this.eUpdateType = FeatureEditUpdateTypeEnum.MILSTD_MODIFIER_UPDATED;
        this.aIndexes = null;
        this.eModifier = null;
        this.eAttribute = eAttribute;
    }

    @Override
    public FeatureEditUpdateTypeEnum getUpdateType() {
        return this.eUpdateType;
    }

    @Override
    public int[] getCoordinateIndexes() {
        return this.aIndexes;
    }

    @Override
    public IGeoMilSymbol.Modifier getChangedModifier() {
        return this.eModifier;
    }

    @Override
    public IGeoAirControlMeasure.Attribute getChangedAttribute() {
        return this.eAttribute;
    }
}
