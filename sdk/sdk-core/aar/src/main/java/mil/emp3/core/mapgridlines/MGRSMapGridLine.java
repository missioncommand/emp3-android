package mil.emp3.core.mapgridlines;

import mil.emp3.api.interfaces.ICamera;
import mil.emp3.api.utils.EmpBoundingBox;

/**
 * This class implements the main MGRS grid line generator class.
 */

public class MGRSMapGridLine extends UTMBaseMapGridLine {
    private static final String TAG = MGRSMapGridLine.class.getSimpleName();

    private static final double MAX_GRID_ALTITUDE = 5e6;

    @Override
    protected void processViewChange(EmpBoundingBox mapBounds, ICamera camera, int viewWidth, int viewHeight) {
        if (camera.getAltitude() > MAX_GRID_ALTITUDE) {
            super.processViewChange(mapBounds, camera, viewWidth, viewHeight);
            return;
        }
    }
}
