package mil.emp3.api.interfaces;

import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.enums.FeaturePropertyChangedEnum;

import org.cmapi.primitives.IGeoAirControlMeasure;
import org.cmapi.primitives.IGeoMilSymbol;

/**
 * This interface allows access to the edit update event data.
 */
public interface IEditUpdateData {

    /**
     * This method return the type of update the EDIT_UPDATE event is indicating.
     * @return {@link FeatureEditUpdateTypeEnum}
     */
    FeatureEditUpdateTypeEnum getUpdateType();
    
    /**
     * This method returns an array of indexes of coordinates affected by the update operation.
     * @return An array of indexes if getUpdateType return  FeatureEditUpdateTypeEnum.COORDINATE_ADDED, COORDINATE_MOVED, or COORDINATE_DELETED. See @see FeatureEditUpdateTypeEnum.
     */
    int[] getCoordinateIndexes();

    /**
     * This method return the MilStd modifier that was updated. This only applies to MilStd features.
     * @return IGeoMilSymbol.Modifier enumerated value if getUpdateType return {@link FeatureEditUpdateTypeEnum}.MILSTD_MODIFIER_UPDATED}, null otherwise.
     */
    IGeoMilSymbol.Modifier getChangedModifier();

    /**
     * This method return the ACM attribute that was updated. This only applies to ACM features.
     * @return IGeoAirControlMeasure.Attribute enumerated value if getUpdateType return FeatureEditUpdateTypeEnum.ACM_ATTRIBUTE_UPDATED, null otherwise.
     */
    IGeoAirControlMeasure.Attribute getChangedAttribute();

    /**
     * This method returns the feature property that was updated. This only applies to feature update events where the getUpdateType returns FeatureEditUpdateTypeEnum.FEATURE_PROPERTY_UPDATED.
     * @return FeaturePropertyChangedEnum value if getUpdateType returns FeatureEditUpdateTypeEnum.FEATURE_PROPERTY_UPDATED, null otherwise.
     */
    public FeaturePropertyChangedEnum getChangedProperty();
}
