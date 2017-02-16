package mil.emp3.core.events;

import mil.emp3.api.enums.FeaturePropertyChangedEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import org.cmapi.primitives.IGeoAirControlMeasure;
import org.cmapi.primitives.IGeoMilSymbol;

import java.io.Serializable;

/**
 * This class implements the IEditUpdateData interface. It used by the core to generate Feature Edit events.
 */
public class EditUpdateData implements IEditUpdateData, Serializable {
    private final FeatureEditUpdateTypeEnum eUpdateType;
    private final int[] aIndexes;
    private final IGeoMilSymbol.Modifier eModifier;
    private final IGeoAirControlMeasure.Attribute eAttribute;
    private final FeaturePropertyChangedEnum eFeatureProperty;

    private EditUpdateData(FeatureEditUpdateTypeEnum eType, int[] aIndexes, IGeoMilSymbol.Modifier eModifier,
                           IGeoAirControlMeasure.Attribute eAttribute, FeaturePropertyChangedEnum eProperty) throws EMP_Exception {
        switch (eType) {
            case COORDINATE_ADDED:
            case COORDINATE_DELETED:
            case COORDINATE_MOVED:
                if (aIndexes == null) {
                    aIndexes = new int[0];
                }
                break;
        }

        this.eUpdateType = eType;
        this.aIndexes = aIndexes;
        this.eModifier = eModifier;
        this.eAttribute = eAttribute;
        this.eFeatureProperty = eProperty;
    }

    public EditUpdateData(FeaturePropertyChangedEnum eProperty) throws EMP_Exception {
        this(FeatureEditUpdateTypeEnum.FEATURE_PROPERTY_UPDATED, null, null, null, eProperty);
    }

    public EditUpdateData(FeatureEditUpdateTypeEnum eType, int[] aIndexes) throws EMP_Exception {
        this(eType, aIndexes, null, null, null);
        switch (eType) {
            case COORDINATE_ADDED:
            case COORDINATE_DELETED:
            case COORDINATE_MOVED:
                break;
            default:
                throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "Invalid FeatureEditUpdateTypeEnum.");
        }
    }

    public EditUpdateData(IGeoMilSymbol.Modifier eModifier) throws EMP_Exception {
        this(FeatureEditUpdateTypeEnum.MILSTD_MODIFIER_UPDATED, null, eModifier, null, null);
    }

    public EditUpdateData(IGeoAirControlMeasure.Attribute eAttribute) throws EMP_Exception {
        this(FeatureEditUpdateTypeEnum.MILSTD_MODIFIER_UPDATED, null, null, eAttribute, null);
    }

    public EditUpdateData(FeatureEditUpdateTypeEnum updateType) throws EMP_Exception {
        this(updateType, null, null, null, null);

        switch(updateType) {
            case POSITION_UPDATED:
                break;
            default:
                throw new IllegalArgumentException("Update Type " + updateType.toString() + " not supported");
        }
    }

    public EditUpdateData(IEditUpdateData edit) {
        eUpdateType = edit.getUpdateType();
        int [] indexes = edit.getCoordinateIndexes();
        switch (edit.getUpdateType()) {
            case COORDINATE_ADDED:
            case COORDINATE_DELETED:
            case COORDINATE_MOVED:
                if (edit.getCoordinateIndexes() == null ||
                        edit.getCoordinateIndexes().length == 0) {
                    indexes = new int[0];
                } else {
                    indexes = new int[edit.getCoordinateIndexes().length];
                    System.arraycopy(edit.getCoordinateIndexes(), 0, indexes, 0,
                            edit.getCoordinateIndexes().length);
                }
                break;
        }
        aIndexes = indexes;
        eModifier = edit.getChangedModifier();
        eAttribute = edit.getChangedAttribute();
        eFeatureProperty = edit.getChangedProperty();
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

    @Override
    public FeaturePropertyChangedEnum getChangedProperty() {
        return this.eFeatureProperty;
    }
}
