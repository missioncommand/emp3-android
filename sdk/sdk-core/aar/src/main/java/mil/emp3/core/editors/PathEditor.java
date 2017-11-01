package mil.emp3.core.editors;

import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * this class implements the path editor.
 */
public class PathEditor extends AbstractDrawEditEditor<Path> {

    public PathEditor(IMapInstance map, Path feature, IEditEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, true);

        this.initializeEdit();
    }

    public PathEditor(IMapInstance map, Path feature, IDrawEventListener oEventListener, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, true, newFeature);

        this.initializeDraw();
    }

    @Override
    protected void assembleControlPoints() {
        List<IGeoPosition> posList = this.getPositions();
        int index = 0;
        int posCnt = posList.size();
        IGeoPosition pos;
        IGeoPosition prevPos = null;
        ControlPoint controlPoint;

        //Add the control points for each position on the list.
        for (index = 0; index < posCnt; index++) {
            pos = posList.get(index);
            if (index > 0) {
                // There ware position before the current one so we add a new CP.
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

        if (this.inEditMode()) {
            // In Edit mode we do not add CP. The user needs to drag new CP.
            return cpList;
        }

        // Compute the position control point.
        pos = new EmpGeoPosition(oLatLng.getLatitude(), oLatLng.getLongitude());
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, posCnt, -1);
        controlPoint.setPosition(pos);
        cpList.add(controlPoint);
        // Add the new position to the feature position list.
        posList.add(pos);

        if (posList.size() > 1) {
            // Compute the new CP between the last 2 positions.
            controlPoint = this.createCPBetween(posList.get(lastIndex), oLatLng, ControlPoint.CPTypeEnum.NEW_POSITION_CP, lastIndex, posCnt);
            cpList.add(controlPoint);
        }

        // Add the update data
        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{posCnt});

        return cpList;
    }

    protected List<ControlPoint>  doDeleteControlPoint(ControlPoint oCP) {
        List<IGeoPosition> posList = this.getPositions();
        int lastIndex = posList.size() - 1;

        List<ControlPoint> cpList = new ArrayList<>();

        if (posList.size() > 2) {
            // We only remove points beyond the minimum # of positions.

            if (oCP.getCPType() != ControlPoint.CPTypeEnum.POSITION_CP) {
                // We only remove position CP.
                return cpList;
            }

            cpList.add(oCP);

            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_DELETED, new int[]{oCP.getCPIndex()});

            // Calculate the before and after index of the new PC before this CP.
            int beforeIndex = (oCP.getCPIndex() + posList.size() - 1) % posList.size();
            int afterIndex = (oCP.getCPIndex() + 1) % posList.size();

            // Now find and add the before new position CP to the list.
            ControlPoint newCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeIndex, oCP.getCPIndex());
            if (newCP != null) {
                // The first point does not have a new CP before it.
                cpList.add(newCP);
            }

            // Now find and add the after new position CP to the list.
            newCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, oCP.getCPIndex(), afterIndex);
            if (newCP != null) {
                // The last point does not have a new CP after it.
                cpList.add(newCP);
            }

            // Remove the position from the feature position list.
            posList.remove(oCP.getCPIndex());

            if ((beforeIndex != lastIndex) && (afterIndex != 0)) {
                // Now we add a new control point between the CP before and after the one removed.
                ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, beforeIndex, -1);
                ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, afterIndex, -1);
                this.createCPBetween(beforeCP.getPosition(), afterCP.getPosition(), ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeIndex, afterIndex);
            }

            this.decreaseControlPointIndexes(oCP.getCPIndex());
        }

        return cpList;
    }

    protected boolean doControlPointMoved(ControlPoint oCP, IGeoPosition oLatLon) {
        double dDistance;
        double dBearing;
        ControlPoint newCP;
        IGeoPosition newPosition;
        IGeoPosition currentPosition = oCP.getPosition();
        int cpIndex = oCP.getCPIndex();
        int cpSubIndex = oCP.getCPSubIndex();
        List<IGeoPosition> posList = this.getPositions();

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
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{oCP.getCPIndex()});
                moved = true;
                break;
            }
            case POSITION_CP: {
                // Set the control points coordinates.
                currentPosition.setLatitude(oLatLon.getLatitude());
                currentPosition.setLongitude((oLatLon.getLongitude()));
                // Add the update data
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{oCP.getCPIndex()});
                moved = true;

                // Now we need to move the new CP that may be before and after this one.
                if (cpIndex > 0) {
                    // It is not the first CP so it has a new CP before it and a position CP before that.
                    ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, cpIndex - 1, -1);
                    ControlPoint newBeforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex - 1, cpIndex);

                    // Compute and set the location of the newBeforeCP
                    newBeforeCP.moveCPBetween(beforeCP.getPosition(), currentPosition);
                }

                if (cpIndex < (posList.size() - 1)) {
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
