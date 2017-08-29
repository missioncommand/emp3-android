package mil.emp3.api.interfaces;

import mil.emp3.api.utils.EmpGeoColor;
import mil.emp3.api.utils.EmpGeoPosition;

public interface ILineOfSight extends IMapService {

    EmpGeoPosition getPosition();
    double getRange();
    EmpGeoColor getVisibleAttributes();
    EmpGeoColor getOccludeAttributes();
}
