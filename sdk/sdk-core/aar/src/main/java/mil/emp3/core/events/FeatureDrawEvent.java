package mil.emp3.core.events;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.enums.FeatureDrawEventEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IEditUpdateData;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.interfaces.IMap;

/**
 *
 * This class is used by the core to generate feature draw events.
 */
public class FeatureDrawEvent extends mil.emp3.api.events.FeatureDrawEvent {
    private final List<IEditUpdateData> oChange;
    private final IMap oMapEditing;

    public FeatureDrawEvent(FeatureDrawEventEnum eEvent, IFeature oTarget, IMap oMap) throws EMP_Exception {
        super(eEvent, oTarget);
        if ((eEvent == FeatureDrawEventEnum.DRAW_UPDATE)
                || (oTarget == null) || (oMap == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "For UPDATE events oTarget and oMap must NOT be null.");
        }
        this.oChange = null;
        this.oMapEditing = oMap;
    }

    public FeatureDrawEvent(IFeature oTarget, IMap oMap,  IEditUpdateData change) throws EMP_Exception {
        super(FeatureDrawEventEnum.DRAW_UPDATE, oTarget);
        if ((change == null) || (oTarget == null) || (oMap == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "For UPDATE events oTarget, oMap, and change must NOT be null.");
        }
        this.oChange = new ArrayList<>();
        this.oChange.add(change);
        this.oMapEditing = oMap;
    }

    public FeatureDrawEvent(IFeature oTarget, IMap oMap, List<IEditUpdateData> oChanges) throws EMP_Exception {
        super(FeatureDrawEventEnum.DRAW_UPDATE, oTarget);
        if ((oChanges == null) || (oTarget == null) || (oMap == null)) {
            throw new EMP_Exception(EMP_Exception.ErrorDetail.INVALID_PARAMETER, "For UPDATE events oTarget, oMap, and change must NOT be null.");
        }
        this.oChange = oChanges;
        this.oMapEditing = oMap;
    }

    @Override
    public List<IEditUpdateData> getUpdateData() {
        return this.oChange;
    }

    @Override
    public IMap getMap() {
        return this.oMapEditing;
    }
}
