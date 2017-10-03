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
import mil.emp3.api.utils.EmpGeoPosition;
import mil.emp3.api.utils.GeographicLib;
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

    public MilStdDCRouteEditor(IMapInstance map, MilStdSymbol feature, IDrawEventListener oEventListener, armyc2.c2sd.renderer.utilities.SymbolDef symDef, boolean newFeature) throws EMP_Exception {
        super(map, feature, oEventListener, symDef, newFeature);
        this.initializeDraw();
    }

    @Override
    protected void prepareForDraw() throws EMP_Exception {
        List<IGeoPosition> posList = this.getPositions();

        if (!this.isNewFeature()) {
            // A feature that already exists should have all of its properties set already.
            return;
        }
/*
        IGeoPosition cameraPos = this.getMapCameraPosition();
        // We set the initial line segment to 2/6 of the camera altitude.
        double segmentLength = cameraPos.getAltitude() / 6.0;
        IGeoPosition pos = new GeoPosition();

        if (segmentLength > 2609340.0) {
            // If its to large set it to 1000 miles which makes the segment 2000 miles long.
            segmentLength = 2609340.0;
        }

        // Calulate the point.
        GeographicLib.computePositionAt(270.0, segmentLength, cameraPos, pos);
        pos.setAltitude(0);
        posList.add(pos);
        // Calulate the end.
        pos = new GeoPosition();
        GeographicLib.computePositionAt(90.0, segmentLength, cameraPos, pos);
        posList.add(pos);
        // Calulate the width position.
        pos = new GeoPosition();
        GeographicLib.computePositionAt(240.0, segmentLength * 0.7, cameraPos, pos);
        posList.add(pos);

        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0,1,2});
*/
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

        List<ControlPoint> cpList = new ArrayList<>();

        if (this.inEditMode()) {
            // In Edit mode we do not add CP. The user needs to drag new CP.
            return cpList;
        }

        // Increment the index of all CP.
        this.increaseControlPointIndexes(0);

        // Set the position and create the control point.
        pos = new EmpGeoPosition(oLatLng.getLatitude(), oLatLng.getLongitude());
        controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 0, -1);
        controlPoint.setPosition(pos);
        cpList.add(controlPoint);
        posList.add(0, pos);

        if (posList.size() > 1) {
            // Compute the new CP between the last position and the new one.
            controlPoint = this.createCPBetween(pos, posList.get(1), ControlPoint.CPTypeEnum.NEW_POSITION_CP, 0, 1);
            cpList.add(controlPoint);
        }

        if (posList.size() == 2) {
            // We have the 2nd position we need to add the width CP.
            IGeoPosition cameraPos = this.getMapCameraPosition();
            double arrowLength = cameraPos.getAltitude() / 8.5;
            double arrowHeadAzimuth = GeographicLib.computeBearing(posList.get(0), posList.get(1)) + 45.0;

            if (arrowLength > 1126300.0) {
                // If its to large set it to 700 miles.
                arrowLength = 1126300.0;
            }

            pos = GeographicLib.computePositionAt(arrowHeadAzimuth, arrowLength, posList.get(0));
            controlPoint = new ControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, 2, -1);
            controlPoint.setPosition(pos);
            cpList.add(controlPoint);
            posList.add(pos);
            // Add the update data
            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0, 2});
        } else if (posList.size() > 3) {
            // We need to move the arrow width CP.
            controlPoint = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, posList.size() - 1, -1);
            if (null != controlPoint) {
                double arrowHeadAzimuth = GeographicLib.computeBearing(posList.get(1), controlPoint.getPosition()) -
                        GeographicLib.computeBearing(posList.get(1), posList.get(2)) +
                        GeographicLib.computeBearing(posList.get(0), posList.get(1));
                double arrowHeadLength = GeographicLib.computeDistanceBetween(posList.get(1), controlPoint.getPosition());

                GeographicLib.computePositionAt(arrowHeadAzimuth, arrowHeadLength, posList.get(0), controlPoint.getPosition());
                // Add the update data
                this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{posList.size() - 1});
            }
            // Add the update data
            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0});
        } else {
            // Add the update data
            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_ADDED, new int[]{0});
        }

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

            if (cpIndex == tailIndex) {
                // We can't delete the last CP. Its the arrow width.
                return cpList;
            }

            cpList.add(oCP);

            this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_DELETED, new int[]{cpIndex});

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

                if (cpIndex != 0) {
                    // Now we add a new control point between the CP before and after the one removed.
                    ControlPoint beforeCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, beforeIndex, -1);
                    ControlPoint afterCP = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, afterIndex, -1);
                    this.createCPBetween(beforeCP.getPosition(), afterCP.getPosition(), ControlPoint.CPTypeEnum.NEW_POSITION_CP, beforeIndex, afterIndex);
                }
            }

            // Remove the position from the feature position list.
            posList.remove(cpIndex);
            tailIndex = posList.size() - 1;

            this.decreaseControlPointIndexes(cpIndex);

            if (cpIndex == 0) {
                // The 1st coordinate was deleted, we need to move the arrow width CP.
                ControlPoint arrowHeadPos = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, tailIndex, -1);
                if (null != arrowHeadPos) {
                    double arrowHeadAzimuth = GeographicLib.computeBearing(oCP.getPosition(), arrowHeadPos.getPosition()) -
                            GeographicLib.computeBearing(oCP.getPosition(), posList.get(0)) +
                            GeographicLib.computeBearing(posList.get(0), posList.get(1));
                    double arrowHeadLength = GeographicLib.computeDistanceBetween(oCP.getPosition(), arrowHeadPos.getPosition());

                    GeographicLib.computePositionAt(arrowHeadAzimuth, arrowHeadLength, posList.get(0), arrowHeadPos.getPosition());
                    // Add the update data
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{tailIndex});
                }
            } else if (cpIndex == 1) {
                // The 2nd coordinate was deleted, we need to move the arrow width CP.
                ControlPoint arrowHeadPos = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, tailIndex, -1);
                if (null != arrowHeadPos) {
                    double arrowHeadAzimuth = GeographicLib.computeBearing(posList.get(0), arrowHeadPos.getPosition()) -
                            GeographicLib.computeBearing(posList.get(0), oCP.getPosition()) +
                            GeographicLib.computeBearing(posList.get(0), posList.get(1));
                    double arrowHeadLength = GeographicLib.computeDistanceBetween(posList.get(0), arrowHeadPos.getPosition());

                    GeographicLib.computePositionAt(arrowHeadAzimuth, arrowHeadLength, posList.get(0), arrowHeadPos.getPosition());
                    // Add the update data
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{tailIndex});
                }
            }
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
                if (cpIndex < 2) {
                    // The 1st or 2nd position was moved we need to move the arrow head CP.
                    ControlPoint arrowHeadPos = this.findControlPoint(ControlPoint.CPTypeEnum.POSITION_CP, posList.size() - 1, -1);
                    if (null != arrowHeadPos) {
                        double arrowHeadLength = GeographicLib.computeDistanceBetween(posList.get(0), arrowHeadPos.getPosition());
                        double arrowHeadAzimuth = GeographicLib.computeBearing(posList.get(0), arrowHeadPos.getPosition()) - GeographicLib.computeBearing(posList.get(0), posList.get(1));

                        if (cpIndex == 0) {
                            arrowHeadAzimuth += GeographicLib.computeBearing(oLatLon, posList.get(1));
                            GeographicLib.computePositionAt(arrowHeadAzimuth, arrowHeadLength, oLatLon, arrowHeadPos.getPosition());
                        } else {
                            arrowHeadAzimuth += GeographicLib.computeBearing(posList.get(0), oLatLon);
                            GeographicLib.computePositionAt(arrowHeadAzimuth, arrowHeadLength, posList.get(0), arrowHeadPos.getPosition());
                        }
                        // Add the update data
                        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{cpIndex, posList.size() - 1});
                    } else {
                        // Add the update data
                        this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{cpIndex});
                    }
                    currentPosition.setLatitude(oLatLon.getLatitude());
                    currentPosition.setLongitude((oLatLon.getLongitude()));
                } else {
                    // Set the control points coordinates.
                    currentPosition.setLatitude(oLatLon.getLatitude());
                    currentPosition.setLongitude((oLatLon.getLongitude()));
                    // Add the update data
                    this.addUpdateEventData(FeatureEditUpdateTypeEnum.COORDINATE_MOVED, new int[]{cpIndex});
                }
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
