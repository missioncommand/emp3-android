package mil.emp3.core.editors;

import android.util.Log;

import org.cmapi.primitives.GeoPosition;
import org.cmapi.primitives.IGeoPosition;

import java.util.ArrayList;
import java.util.List;

import mil.emp3.api.Path;
import mil.emp3.api.Polygon;
import mil.emp3.api.enums.FeatureEditUpdateTypeEnum;
import mil.emp3.api.exceptions.EMP_Exception;
import mil.emp3.api.listeners.IDrawEventListener;
import mil.emp3.api.listeners.IEditEventListener;
import mil.emp3.api.utils.GeoLibrary;
import mil.emp3.mapengine.interfaces.IMapInstance;

/**
 * This class implements the polygon editor. In draw mode no initial positions are added, the user starts clicking
 * (tapping) to create all the points. Added CPs are placed at the end of the features position list.
 */
public class PolygonEditor extends AbstractDrawEditEditor<Polygon> {
    private final static String TAG = PolygonEditor.class.getSimpleName();

    public PolygonEditor(IMapInstance map, Polygon feature, IEditEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, true);

        this.initializeEdit();
    }

    public PolygonEditor(IMapInstance map, Polygon feature, IDrawEventListener oEventListener) throws EMP_Exception {
        super(map, feature, oEventListener, true);

        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        // we need to remove al positions in the feature.
        List<IGeoPosition> posList = this.getPositions();
        posList.clear();
    }

    @Override
    protected void assembleControlPoints() {
        List<IGeoPosition> posList = this.getPositions();
        int index = 0;
        int posCnt = posList.size();
        IGeoPosition pos;
        IGeoPosition prevPos = null;
        IGeoPosition newPos;
        ControlPoint controlPoint;

        if (posList.size() > 0) {
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

            if (posList.size() > 2) {
                // This new cp is between the last one and the first.
                pos = posList.get(0); // Get the first position.
                this.createCPBetween(prevPos, pos, ControlPoint.CPTypeEnum.NEW_POSITION_CP, index - 1, 0);
            }
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
        
        // Set the position control point.
        pos = new GeoPosition();
        pos.setAltitude(0);
        pos.setLatitude(oLatLng.getLatitude());
        pos.setLongitude(oLatLng.getLongitude());
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, posCnt, -1);
        controlPoint.setPosition(pos);
        cpList.add(controlPoint);
        // Add the new position to the feature position list.
        posList.add(pos);

        posCnt = posList.size();

        if (posCnt > 1) {
            // There is more than 1 CP so we can add the new CP between this new one and the previous one.
            this.createCPBetween(posList.get(posCnt - 2), oLatLng, ControlPoint.CPTypeEnum.NEW_POSITION_CP, lastIndex, posCnt - 1);
        }

        if (posCnt == 3) {
            // We just added the 3rd point. We need to add the new CP between the last and the first.
            this.createCPBetween(oLatLng, posList.get(0), ControlPoint.CPTypeEnum.NEW_POSITION_CP, 2, 0);
        } else {
            // We added a new point. We need to move the new CP that is beetween the previous last and the first CPs.
            // Find the CP.
            controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, lastIndex, 0);
            if (null != controlPoint) {
                controlPoint.setCPIndex(posCnt - 1);
                controlPoint.moveCPBetween(oLatLng, posList.get(0));
            }
        }

        // Add the update data
        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{posCnt - 1});

        return cpList;
    }

    protected List<ControlPoint>  doDeleteControlPoint(ControlPoint oCP) {
        List<IGeoPosition> posList = this.getPositions();

        List<ControlPoint> cpList = new ArrayList<>();

        if (posList.size() > 3) {
            // We only remove points beyond the minimum # of positions.

            if (oCP.getCPType() != ControlPoint.CPTypeEnum.POSITION_CP) {
                // We only remove position CP.
                return cpList;
            }

            cpList.add(oCP);
            // Add update event data.
            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_DELETED, new int[]{oCP.getCPIndex()});

            // Calculate the before and after index of the new PC before this CP.
            int beforeIndex = (oCP.getCPIndex() + posList.size() - 1) % posList.size();
            int afterIndex = (oCP.getCPIndex() + 1) % posList.size();

            // Now find and add the before new position CP to the list.
            ControlPoint newCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeIndex, oCP.getCPIndex());
            cpList.add(newCP);

            // Now find and add the after new position CP to the list.
            newCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, oCP.getCPIndex(), afterIndex);
            cpList.add(newCP);

            // Remove the position from the feature position list.
            posList.remove(oCP.getCPIndex());

            // Now we add a new control point between the CP before and after the one removed.
            ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, beforeIndex, -1);
            ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, afterIndex, -1);
            this.createCPBetween(beforeCP.getPosition(), afterCP.getPosition(), ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeIndex, afterIndex);

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
                this.increaseControlPointIndexes(cpIndex + 1);

                // Set the index of this new position CP.
                oCP.setCPIndex(cpIndex + 1);

                // Add this CP position to the features position list.
                posList.add(cpIndex + 1, oCP.getPosition());

                // Now that we added the new position we need to create the new CP between them.
                // Create the new CP between the beforeCP and this one.
                // Get the distance and bearing between the beforeCP and this one and create the new CP
                this.createCPBetween(beforeCP.getPosition(), oLatLon, ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex, cpIndex + 1);

                // Create the new CP between this one and the afterCP.
                // Get the distance and bearing between this one and the afterCP and create the new CP
                this.createCPBetween(oLatLon, afterCP.getPosition(), ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex + 1, (cpIndex + 2) % posList.size());

                // Now we add event update data.
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{oCP.getCPIndex()});
                moved = true;
                break;
            }
            case POSITION_CP: {
                int beforeCPIndex = (cpIndex + posList.size() - 1) % posList.size();
                int afterCPIndex = (cpIndex + 1) % posList.size();

                // Set the control points coordinates.
                currentPosition.setLatitude(oLatLon.getLatitude());
                currentPosition.setLongitude((oLatLon.getLongitude()));
                // Add the update data
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{oCP.getCPIndex()});
                moved = true;

                // Now we need to move the new CP that is before the one moved.
                ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, beforeCPIndex, -1);
                ControlPoint newBeforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeCPIndex, cpIndex);
                // Move the newBeforeCP
                newBeforeCP.moveCPBetween(beforeCP.getPosition(), currentPosition);

                // Now we move the new CP that is after the one moved.
                ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, afterCPIndex, -1);
                ControlPoint newAfterCP = this.findControlPoint(ControlPoint.CPTypeEnum.NEW_POSITION_CP, cpIndex, afterCPIndex);
                // Move the newAfterCP
                newAfterCP.moveCPBetween(currentPosition, afterCP.getPosition());
                break;
            }
        }

        return moved;
    }
}
