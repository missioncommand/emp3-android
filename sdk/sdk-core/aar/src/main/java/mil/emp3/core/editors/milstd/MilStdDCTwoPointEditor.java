package mil.emp3.core.editors.milstd;

import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.core.editors.ControlPoint;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the two point category editor.
 *
 * TACTICAL GRAPHICS
 *      TASKS
 *          FIX
 *          Follow and Assume
 *              Follow and Support
 * Tactical Graphics
 *      Mobility-Survivability
 *          Obstacles
 *              Obstacle Effect
 *                  Fix (Obstacle Effect)
 * Tactical Graphics
 *      Combat Service Support
 *          Lines
 *              Convoys
 *                  Moving Convoy
 *                  Halted Convoy
 */
public class MilStdDCTwoPointEditor extends AbstractMilStdMultiPointEditor {

    public MilStdDCTwoPointEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdDCTwoPointEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, SymbolDef symDef, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, symDef, newFeature);
        this.initializeDraw();
    }

    @Override
    protected void assembleControlPoints() {
        List<IGeoPosition> posList = this.getPositions();
        int index = 0;
        int posCnt = posList.size();
        IGeoPosition pos;
        ControlPoint controlPoint;

        //Add the control points for each position on the list.
        for (index = 0; index < posCnt; index++) {
            pos = posList.get(index);
            controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, index, -1);
            controlPoint.setPosition(pos);
            this.addControlPoint(controlPoint);
        }
    };

    /**
     * The editor must override this method to handle control point motion
     * We can't have any new points, just move the point.
     * @param oCP The control point that was moved.
     * @param oLatLon The new location for the control point.
     * @return A list of control points that have been affected.
     */
    @Override
    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        IGeoPosition currentPosition = oCP.getPosition();
        currentPosition.setLatitude(oLatLon.getLatitude());
        currentPosition.setLongitude((oLatLon.getLongitude()));
        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{oCP.getCPIndex()});
        return true;
    }

    @Override
    protected List<ControlPoint> doAddControlPoint(IGeoPosition oNewPos) {
        ControlPoint controlPoint;
        List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;
        int posCnt = posList.size();

        if (this.inEditMode() || (posCnt >= this.getMaxPoints())) {
            return null;
        }

        List<ControlPoint> cpList = new ArrayList<>();

        // Increment the index of all CP.
        this.increaseControlPointIndexes(0);

        // Set the position and create the control point.
        pos = new EmpGeoPosition(oNewPos.getLatitude(), oNewPos.getLongitude());
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        controlPoint.setPosition(pos);
        cpList.add(controlPoint);
        posList.add(0, pos);

        return cpList;
    }
}
