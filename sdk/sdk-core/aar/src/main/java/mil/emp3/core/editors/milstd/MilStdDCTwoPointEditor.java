package mil.emp3.core.editors.milstd;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import armyc2.c2sd.renderer.utilities.SymbolDef;
import armyc2.c2sd.renderer.utilities.UnitDef;
import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.interfaces.IFeature;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.editors.ControlPoint;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the two point catgegory editor.
 */
public class MilStdDCTwoPointEditor extends AbstractMilStdMultiPointEditor {

    public MilStdDCTwoPointEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdDCTwoPointEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        IGeoPosition cameraPos = this.getMapCameraPosition();
        List<IGeoPosition> posList = this.getPositions();
        // We set the initial line segment to 2/6 of the camera altitude.
        double segmentLength = cameraPos.getAltitude() / 6.0;
        IGeoPosition pos = new GeoPosition();

        if (segmentLength > 2609340.0) {
            // If its to large set it to 1000 miles which makes the segment 2000 miles long.
            segmentLength = 2609340.0;
        }

        if (posList.size() >= this.getMinPoints()) {
            // The feature has enough points.
            return;
        }

        // If it does not have enough positions, clear them.
        posList.clear();

        // Calculate the point.
        GeoLibrary.computePositionAt(270.0, segmentLength, cameraPos, pos);
        pos.setAltitude(0);
        posList.add(pos);
        // Calculate the end.
        pos = new GeoPosition();
        GeoLibrary.computePositionAt(90.0, segmentLength, cameraPos, pos);
        posList.add(pos);

        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0,1});
    }

    @Override
    protected void assembleControlPoints() {
        List<IGeoPosition> posList = this.getPositions();
        int index = 0;
        int posCnt = posList.size();
        int lastIndex = posCnt - 1;
        IGeoPosition pos;
        IGeoPosition prevPos = null;
        ControlPoint controlPoint;

        //Add the control points for each position on the list.
        for (index = 0; index < posCnt; index++) {
            pos = posList.get(index);
            if ((index > 0) && (index != lastIndex)) {
                // There was a position before the current one so we add a new CP.
                // But there is no new CP before the last position.
                this.createCPBetween(prevPos, pos, ControlPoint.CPTypeEnum.NEW_POSITION_CP, index - 1, index);
            }
            controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, index, -1);
            controlPoint.setPosition(pos);
            this.addControlPoint(controlPoint);
            prevPos = pos;
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
}
