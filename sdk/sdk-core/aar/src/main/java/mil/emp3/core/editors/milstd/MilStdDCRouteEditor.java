package mil.emp3.core.editors.milstd;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.MilStdSymbol;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.core.editors.ControlPoint;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the DC Route editor which handles the following graphics.
 *
 * Tactical Graphics
 *      Tasks
 *          Counterattack (CATK)
 *              Counterattack by Fire
 *      Command and Control and General Maneuver
 *          Deception
 *              Axis of Advance for Feint
 *          Offense
 *              Lines
 *                  Axis of Advance
 *                      Axis of Advance Aviation
 *                      Axis of Advance Airborne
 *                      Axis of Advance Attack, Rotary Wing
 *                      Axis of Advance Ground
 *                          Axis of Advance Ground Main Attack
 *                          Axis of Advance Ground Supporting Attack
 */
public class MilStdDCRouteEditor extends AbstractMilStdMultiPointEditor {

    public MilStdDCRouteEditor(IMapInstance map, MilStdSymbol feature, IEditEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
        super(map, feature, oEventListener, symDef);
        this.initializeEdit();
    }

    public MilStdDCRouteEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef) throws EMP_Exception {
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

        // If it does not have enough positions, clear them.
        posList.clear();

        // Calulate the point.
        GeoLibrary.computePositionAt(270.0, segmentLength, cameraPos, pos);
        pos.setAltitude(0);
        posList.add(pos);
        // Calulate the end.
        pos = new GeoPosition();
        GeoLibrary.computePositionAt(90.0, segmentLength, cameraPos, pos);
        posList.add(pos);
        // Calulate the width position.
        pos = new GeoPosition();
        GeoLibrary.computePositionAt(240.0, segmentLength * 0.7, cameraPos, pos);
        posList.add(pos);

        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0,1,2});
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

    @Override
    protected List<ControlPoint> doAddControlPoint(IGeoPosition oLatLng) {
        ControlPoint controlPoint;
        List<IGeoPosition> posList = this.getPositions();
        IGeoPosition pos;
        int posCnt = posList.size();
        int lastIndex = posCnt - 1;

        List<ControlPoint> cpList = new ArrayList<>();

        // Increment the index => lastIndex.
        this.increaseControlPointIndexes(lastIndex);

        // Set the position and create the control point.
        pos = new GeoPosition();
        pos.setAltitude(0);
        pos.setLatitude(oLatLng.getLatitude());
        pos.setLongitude(oLatLng.getLongitude());
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, lastIndex, -1);
        controlPoint.setPosition(pos);
        cpList.add(controlPoint);
        posList.add(lastIndex, pos);

        // Compute the new CP between the last position and the new one.
        controlPoint = this.createCPBetween(posList.get(lastIndex - 1), pos, ControlPoint.CPTypeEnum.NEW_POSITION_CP, lastIndex - 1, lastIndex);
        cpList.add(controlPoint);

        // Add the update data
        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{lastIndex});

        return cpList;
    }

    protected List<ControlPoint>  doDeleteControlPoint(ControlPoint oCP) {
        List<IGeoPosition> posList = this.getPositions();
        int tailIndex = posList.size() - 1;
        int cpIndex = oCP.getCPIndex();

        List<ControlPoint> cpList = new ArrayList<>();

        if (posList.size() > this.getMinPoints()) {
            // We only remove points beyond the minimum # of positions.

            if (oCP.getCPType() != ControlPoint.CPTypeEnum.POSITION_CP) {
                // We only remove position CP.
                return cpList;
            }

            if (oCP.getCPIndex() == tailIndex) {
                // We can't delete the last CP. Its the arrow width.
                return cpList;
            }

            if (oCP.getCPIndex() == 0) {
                // We can't remove the first position.
                return cpList;
            }

            cpList.add(oCP);

            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_DELETED, new int[]{oCP.getCPIndex()});

            // Calculate the index of the NEW_POSITION CP before this CP and find it.
            int beforeIndex = (cpIndex + posList.size() - 1) % posList.size();
            ControlPoint newBeforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeIndex, cpIndex);
            if (newBeforeCP != null) {
                // We put it on the list so it gets removed.
                cpList.add(newBeforeCP);
            }

            // Calculate the index of the POSITION CP after this CP. If the index == tailIndex there is not one.
            int afterIndex = (cpIndex + 1) % posList.size();
            if (afterIndex != tailIndex) {
                ControlPoint newAfterCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex, afterIndex);
                if (newAfterCP != null) {
                    cpList.add(newAfterCP);
                }

                // Now we add a new control point between the CP before and after the one removed.
                ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, beforeIndex, -1);
                ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, afterIndex, -1);
                this.createCPBetween(beforeCP.getPosition(), afterCP.getPosition(), ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeIndex, afterIndex);
            }

            // Remove the position from the feature position list.
            posList.remove(oCP.getCPIndex());

            this.decreaseControlPointIndexes(oCP.getCPIndex());

            return cpList;
        }

        return cpList;
    }

    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        IGeoPosition currentPosition = oCP.getPosition();
        int cpIndex = oCP.getCPIndex();
        int cpSubIndex = oCP.getCPSubIndex();
        List<IGeoPosition> posList = this.getPositions();
        int tailIndex = posList.size() - 2;
        int widthIndex = posList.size() - 1;

        boolean moved = false;

        switch (oCP.getCPType()) {
            case NEW_POSITION_CP: {
                //Get the control point before this new CP.
                ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, cpIndex, -1);
                //Get the control point that is after this new CP.
                ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, cpSubIndex, -1);

                //Change the new CP to a position CP and update its position.
                oCP.setCPType(ControlPoint.CPTypeEnum.POSITION_CP);
                oCP.setCPSubIndex(-1);
                currentPosition.setLatitude(oLatLon.getLatitude());
                currentPosition.setLongitude(oLatLon.getLongitude());

                // Increase the indexes of all CP after this one.
                this.increaseControlPointIndexes(cpSubIndex);

                // Set the index of this new position CP.
                oCP.setCPIndex(cpSubIndex);

                // Add this CP position to the features position list.
                posList.add(cpSubIndex, oCP.getPosition());

                // Now that we added the new position we need to create the new CP between them.
                // Create the new CP between the beforeCP and this one.
                // Get the distance and bearing between the beforeCP and this one and create the new CP
                this.createCPBetween(beforeCP.getPosition(), oLatLon, ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex, cpSubIndex);

                // Create the new CP between this one and the afterCP.
                // Get the distance and bearing between this one and the afterCP and create the new CP
                this.createCPBetween(oLatLon, afterCP.getPosition(), ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpSubIndex, cpSubIndex + 1);

                // Now we add event update data.
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{cpSubIndex});
                moved = true;
                break;
            }
            case POSITION_CP: {
                // Set the control points coordinates.
                currentPosition.setLatitude(oLatLon.getLatitude());
                currentPosition.setLongitude((oLatLon.getLongitude()));
                // Add the update data
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{cpIndex});
                moved = true;

                // Now we need to move the new CP that may be before and after this one.
                if ((cpIndex > 0) && (cpIndex <= tailIndex)) {
                    // It is not the first CP so it has a new CP before it and a position CP before that.
                    ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, cpIndex - 1, -1);
                    ControlPoint newBeforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex - 1, cpIndex);

                    // Compute and set the location of the newBeforeCP
                    newBeforeCP.moveCPBetween(beforeCP.getPosition(), currentPosition);
                }

                if (cpIndex < tailIndex) {
                    // It is not the last one so it has a new CP after it and a position CP after that.
                    ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, cpIndex + 1, -1);
                    ControlPoint newAfterCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex, cpIndex + 1);

                    // Compute and set the location of the newAfterCP
                    newAfterCP.moveCPBetween(currentPosition, afterCP.getPosition());
                }
                break;
            }
        }

        return moved;
    }
}
